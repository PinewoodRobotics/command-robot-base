from backend.generated.proto.python.util.position_pb2 import Accel2d as _Accel2d
from backend.generated.proto.python.util.position_pb2 import Accel3d as _Accel3d
from numpy.typing import NDArray
import numpy as np


def _as_vector(
    values: NDArray[np.float64], expected_size: int, name: str
) -> NDArray[np.float64]:
    vector = np.asarray(values, dtype=np.float64)
    if vector.shape != (expected_size,):
        raise ValueError(
            f"{name} requires shape ({expected_size},), got {vector.shape}"
        )

    return vector


class Accel2d(_Accel2d):
    def to_linear_numpy(self) -> NDArray[np.float64]:
        return np.array(
            [
                self.linear_acceleration.x,
                self.linear_acceleration.y,
            ],
            dtype=np.float64,
        )

    def set_linear_from_numpy(self, values: NDArray[np.float64]) -> None:
        vector = _as_vector(values, 2, "Accel2d.linear_acceleration")
        self.linear_acceleration.x = vector[0]
        self.linear_acceleration.y = vector[1]

    def to_components(self) -> tuple[NDArray[np.float64], float]:
        return self.to_linear_numpy(), float(self.angular_acceleration_rad_s_2)

    def from_components(
        self,
        linear_acceleration: NDArray[np.float64],
        angular_acceleration_rad_s_2: float,
    ) -> None:
        self.set_linear_from_numpy(linear_acceleration)
        self.angular_acceleration_rad_s_2 = angular_acceleration_rad_s_2

    def to_numpy(self) -> NDArray[np.float64]:
        return np.array(
            [
                self.linear_acceleration.x,
                self.linear_acceleration.y,
                self.angular_acceleration_rad_s_2,
            ],
            dtype=np.float64,
        )

    def from_numpy(self, values: NDArray[np.float64]) -> None:
        vector = _as_vector(values, 3, "Accel2d")
        self.linear_acceleration.x = vector[0]
        self.linear_acceleration.y = vector[1]
        self.angular_acceleration_rad_s_2 = vector[2]

    def get_x(self) -> float:
        return self.linear_acceleration.x

    def get_y(self) -> float:
        return self.linear_acceleration.y

    def get_angular(self) -> float:
        return self.angular_acceleration_rad_s_2


class Accel3d(_Accel3d):
    def to_linear_numpy(self) -> NDArray[np.float64]:
        return np.array(
            [
                self.linear_acceleration.x,
                self.linear_acceleration.y,
                self.linear_acceleration.z,
            ],
            dtype=np.float64,
        )

    def angular_to_numpy(self) -> NDArray[np.float64]:
        return np.array(
            [
                self.angular_acceleration_rad_s_2.x,
                self.angular_acceleration_rad_s_2.y,
                self.angular_acceleration_rad_s_2.z,
            ],
            dtype=np.float64,
        )

    def set_linear_from_numpy(self, values: NDArray[np.float64]) -> None:
        vector = _as_vector(values, 3, "Accel3d.linear_acceleration")
        self.linear_acceleration.x = vector[0]
        self.linear_acceleration.y = vector[1]
        self.linear_acceleration.z = vector[2]

    def set_angular_from_numpy(self, values: NDArray[np.float64]) -> None:
        vector = _as_vector(values, 3, "Accel3d.angular_acceleration_rad_s_2")
        self.angular_acceleration_rad_s_2.x = vector[0]
        self.angular_acceleration_rad_s_2.y = vector[1]
        self.angular_acceleration_rad_s_2.z = vector[2]

    def to_components(self) -> tuple[NDArray[np.float64], NDArray[np.float64]]:
        return self.to_linear_numpy(), self.angular_to_numpy()

    def from_components(
        self,
        linear_acceleration: NDArray[np.float64],
        angular_acceleration_rad_s_2: NDArray[np.float64],
    ) -> None:
        self.set_linear_from_numpy(linear_acceleration)
        self.set_angular_from_numpy(angular_acceleration_rad_s_2)
