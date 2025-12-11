from cscore import VideoMode

from backend.generated.thrift.config.camera.ttypes import CameraType
from backend.python.common.camera.abstract_camera import AbstractCaptureDevice
from backend.python.common.debug.logger import error


MAX_EXPOSURE_TIME = 140
MIN_EXPOSURE_TIME = 1
MIN_BRIGHTNESS = 0
MAX_BRIGHTNESS = 100


class OV2311Camera(AbstractCaptureDevice, type=CameraType.OV2311):
    def setup_custom_properties(self):
        _ = self.camera.setResolution(self.width, self.height)
        _ = self.camera.setFPS(self.max_fps)
        _ = self.camera.setPixelFormat(VideoMode.PixelFormat.kMJPEG)

        if (
            self.exposure_time < MIN_EXPOSURE_TIME
            or self.exposure_time > MAX_EXPOSURE_TIME
        ):
            error(
                f"Exposure time must be between {MIN_EXPOSURE_TIME} and {MAX_EXPOSURE_TIME}"
            )
        else:
            self.set_exposure_time(self.exposure_time)

        if self.brightness is not None:
            if not self._is_within_range(
                self.brightness, MIN_BRIGHTNESS, MAX_BRIGHTNESS
            ):
                error(
                    f"Brightness must be between {MIN_BRIGHTNESS} and {MAX_BRIGHTNESS}"
                )
                return

            self.set_brightness(self.brightness)
