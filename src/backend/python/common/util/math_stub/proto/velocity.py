from backend.generated.proto.python.util.vector_pb2 import Vector2 as _Vector2
from backend.generated.proto.python.util.vector_pb2 import Vector3 as _Vector3
from backend.python.common.util.proto_stub.twist import Twist2d, Twist3d

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


class Velocity2d(_Vector2):
    def to_numpy(self) -> NDArray[np.float64]:
        return np.array([self.x, self.y], dtype=np.float64)

    def from_numpy(self, values: NDArray[np.float64]) -> None:
        vector = _as_vector(values, 2, "Velocity2d")
        self.x = vector[0]
        self.y = vector[1]

    def from_proto(self, proto: Twist2d | _Vector2) -> None:
        vector: _Vector2 = proto  # type: ignore[assignment]
        if isinstance(proto, Twist2d):
            vector = proto.linear_velocity

        self.x = vector.x
        self.y = vector.y


class Velocity3d(_Vector3):
    def to_numpy(self) -> NDArray[np.float64]:
        return np.array([self.x, self.y, self.z], dtype=np.float64)

    def from_numpy(self, values: NDArray[np.float64]) -> None:
        vector = _as_vector(values, 3, "Velocity3d")
        self.x = vector[0]
        self.y = vector[1]
        self.z = vector[2]

    def from_proto(self, proto: Twist3d | _Vector3) -> None:
        vector: _Vector3 = proto  # type: ignore[assignment]
        if isinstance(proto, Twist3d):
            vector = proto.linear_velocity

        self.x = vector.x
        self.y = vector.y
        self.z = vector.z
