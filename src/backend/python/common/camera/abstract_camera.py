import time
import numpy as np
from cscore import CvSink, UsbCamera, VideoProperty, VideoSource
from numpy.typing import NDArray

from backend.generated.thrift.config.camera.ttypes import CameraParameters, CameraType
from backend.python.common.debug.logger import error, success
from backend.python.common.util.math import get_np_from_matrix, get_np_from_vector

ABS_EXPOSURE_PROPERTIES = [
    "raw_exposure_absolute",
    "raw_exposure_time_absolute",
    "exposure",
    "raw_Exposure",
]

AUTO_EXPOSURE_PROPERTIES = [
    "auto_exposure",
    "exposure_auto",
]

BRIGHTNESS_PROPERTIES = [
    "brightness",
    "raw_brightness",
]

AUTO_EXPOSURE_ENABLED = 3
AUTO_EXPOSURE_DISABLED = 1


class AbstractCaptureDevice:
    _registry: dict[CameraType, type["AbstractCaptureDevice"]] = {}

    def __init_subclass__(cls, type: CameraType, **kwargs):
        super().__init_subclass__(**kwargs)
        AbstractCaptureDevice._registry[type] = cls

    def __init__(
        self,
        camera_port: int | str,
        width: int,
        height: int,
        max_fps: float,
        camera_name: str,
        camera_matrix: NDArray[np.float64] = np.eye(3),
        dist_coeff: NDArray[np.float64] = np.zeros(5),
        hard_fps_limit: float | None = None,
        exposure_time: float = -1,
        brightness: int | None = None,
    ) -> None:
        self.port: int | str = camera_port
        self.camera_name: str = camera_name
        self.width: int = width
        self.height: int = height
        self.max_fps: float = max_fps
        self.hard_limit: float | None = hard_fps_limit
        self.camera_matrix: NDArray[np.float64] = camera_matrix
        self.dist_coeff: NDArray[np.float64] = dist_coeff
        self.frame: NDArray[np.uint8] = np.ascontiguousarray(
            np.zeros((self.height, self.width, 3), dtype=np.uint8)
        )
        self.exposure_time: float = exposure_time
        self.brightness: int | None = brightness
        camera, sink = self._setup_video_source_sink()

        self.camera: VideoSource = camera
        self.sink: CvSink = sink

        self.manual_exposure_property: VideoProperty | None = self.find_property(
            ABS_EXPOSURE_PROPERTIES
        )
        self.auto_exposure_property: VideoProperty | None = self.find_property(
            AUTO_EXPOSURE_PROPERTIES
        )
        self.brightness_property: VideoProperty | None = self.find_property(
            BRIGHTNESS_PROPERTIES
        )

        self._is_ready: bool = False
        self._initialize_camera()

        self._last_ts: float = time.time()

    def _setup_video_source_sink(self) -> tuple[VideoSource, CvSink]:
        camera = UsbCamera("CAMERA", self.port)
        sink = CvSink(camera.getName())
        sink.setSource(camera)
        return camera, sink

    def _is_within_range(self, value: int, min_value: int, max_value: int) -> bool:
        return value >= min_value and value <= max_value

    def get_name(self) -> str:
        return self.camera_name

    def _initialize_camera(self):
        self.__configure_camera()
        if self.camera:
            self.camera.setConnectionStrategy(
                VideoSource.ConnectionStrategy.kConnectionKeepOpen
            )

            max_attempts = 5
            attempt = 0
            while attempt < max_attempts:
                if self.camera.isConnected():
                    test_frame = np.ascontiguousarray(
                        np.zeros((self.height, self.width, 3), dtype=np.uint8)
                    )
                    assert test_frame.flags["C_CONTIGUOUS"]
                    ts, frame = self.sink.grabFrame(test_frame)
                    frame = np.ascontiguousarray(frame, dtype=np.uint8)
                    if ts > 0:
                        self._is_ready = True
                        success(f"Camera successfully connected and initialized")
                        return
                attempt += 1
                error(
                    f"Waiting for camera to connect (attempt {attempt}/{max_attempts})..."
                )
                time.sleep(1.0)

            error(f"WARNING: Failed to initialize camera after {max_attempts} attempts")

    def set_auto_exposure(self, enabled: bool):
        if self.auto_exposure_property is not None:
            self.auto_exposure_property.set(
                AUTO_EXPOSURE_ENABLED if enabled else AUTO_EXPOSURE_DISABLED
            )
        else:
            error(f"Auto exposure property not found")
            return

    def set_exposure_time(self, exposure_time: float):
        if exposure_time == -1:
            return

        self.set_auto_exposure(False)

        if self.manual_exposure_property is not None:
            self.manual_exposure_property.set(exposure_time)
        else:
            error(f"Manual exposure property not found")
            return

    def set_brightness(self, brightness: int):
        if self.brightness_property is not None:
            self.brightness_property.set(brightness)
        else:
            error(f"Brightness property not found")
            return

    def default_params(self):
        self.soft_set("image_stabilization", 0)
        self.soft_set("power_line_frequency", 2)
        self.soft_set("scene_mode", 0)
        self.soft_set("exposure_metering_mode", 0)
        self.soft_set("exposure_dynamic_framerate", 0)
        self.soft_set("focus_auto", 0)
        self.soft_set("focus_absolute", 0)

    def soft_set(self, property: str, value: float):
        prop = self.camera.getProperty(property)
        if prop.getKind() != VideoProperty.Kind.kNone:
            prop.set(value)
        else:
            error(f"Property {property} not found")
            return

    def get_frame(self) -> tuple[bool, NDArray[np.uint8] | None]:
        start = time.time()

        if not self._is_ready:
            self._is_ready = False
            self._initialize_camera()
            return False, self.frame

        ts, fr = self.sink.grabFrame(self.frame)
        self.frame = np.ascontiguousarray(fr, dtype=np.uint8)
        assert self.frame.flags["C_CONTIGUOUS"]
        now = time.time()

        if ts == 0:
            error_msg = self.sink.getError()
            error(f"Error grabbing frame: {error_msg}")
            self._is_ready = False
            self._initialize_camera()
            self._last_ts = now
            return False, None

        if self.hard_limit:
            interval = 1.0 / self.hard_limit
            took = now - start
            if took < interval:
                time.sleep(interval - took)

        self._last_ts = time.time()

        return True, self.frame

    def release(self):
        self._is_ready = False
        self.sink.setEnabled(False)
        self.camera.setConnectionStrategy(
            VideoSource.ConnectionStrategy.kConnectionForceClose
        )

    def get_matrix(self) -> NDArray[np.float64]:
        return self.camera_matrix

    def get_dist_coeff(self) -> NDArray[np.float64]:
        return self.dist_coeff

    def __configure_camera(self):
        self.default_params()
        self.setup_custom_properties()

    def find_property(self, properties: list[str]) -> VideoProperty | None:
        for prop in properties:
            property = self.camera.getProperty(prop)
            if property.getKind() != VideoProperty.Kind.kNone:
                return property

        return None

    def setup_custom_properties(self):
        raise NotImplementedError()


def get_camera_capture_device(camera: CameraParameters) -> AbstractCaptureDevice:
    return AbstractCaptureDevice._registry[camera.camera_type](
        camera.camera_path,
        camera.width,
        camera.height,
        camera.max_fps,
        camera.name,
        get_np_from_matrix(camera.camera_matrix),
        get_np_from_vector(camera.dist_coeff),
        exposure_time=camera.exposure_time,
        brightness=camera.brightness or -1,
    )
