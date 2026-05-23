from backend.generated.proto.python.util.position_pb2 import Twist2d as _Twist2d
from backend.generated.proto.python.util.position_pb2 import Twist3d as _Twist3d
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


def _as_matrix(
    values: NDArray[np.float64], expected_shape: tuple[int, int], name: str
) -> NDArray[np.float64]:
    matrix = np.asarray(values, dtype=np.float64)
    if matrix.shape != expected_shape:
        raise ValueError(f"{name} requires shape {expected_shape}, got {matrix.shape}")

    return matrix


def _vector_to_skew_matrix(values: NDArray[np.float64]) -> NDArray[np.float64]:
    vector = _as_vector(values, 3, "angular_velocity_rad_s")
    x, y, z = vector
    return np.array(
        [
            [0.0, -z, y],
            [z, 0.0, -x],
            [-y, x, 0.0],
        ],
        dtype=np.float64,
    )


def _skew_matrix_to_vector(
    values: NDArray[np.float64], name: str
) -> NDArray[np.float64]:
    matrix = _as_matrix(values, (3, 3), name)
    if not np.allclose(matrix, -matrix.T):
        raise ValueError(f"{name} must be skew-symmetric")

    return np.array(
        [
            matrix[2, 1],
            matrix[0, 2],
            matrix[1, 0],
        ],
        dtype=np.float64,
    )


def _rotation_2d(angle_rad: float) -> NDArray[np.float64]:
    return np.array(
        [
            [np.cos(angle_rad), -np.sin(angle_rad)],
            [np.sin(angle_rad), np.cos(angle_rad)],
        ],
        dtype=np.float64,
    )


def _left_jacobian_so2(angle_rad: float) -> NDArray[np.float64]:
    if np.isclose(angle_rad, 0.0):
        return np.array(
            [
                [1.0, -0.5 * angle_rad],
                [0.5 * angle_rad, 1.0],
            ],
            dtype=np.float64,
        )

    a = np.sin(angle_rad) / angle_rad
    b = (1.0 - np.cos(angle_rad)) / angle_rad
    return np.array(
        [
            [a, -b],
            [b, a],
        ],
        dtype=np.float64,
    )


def _rotation_3d(rotation_vector: NDArray[np.float64]) -> NDArray[np.float64]:
    phi = _as_vector(rotation_vector, 3, "rotation_vector")
    phi_hat = _vector_to_skew_matrix(phi)
    theta = np.linalg.norm(phi)

    if np.isclose(theta, 0.0):
        return np.eye(3, dtype=np.float64) + phi_hat + 0.5 * (phi_hat @ phi_hat)

    phi_hat_squared = phi_hat @ phi_hat
    return (
        np.eye(3, dtype=np.float64)
        + (np.sin(theta) / theta) * phi_hat
        + ((1.0 - np.cos(theta)) / (theta**2)) * phi_hat_squared
    )


def _left_jacobian_so3(rotation_vector: NDArray[np.float64]) -> NDArray[np.float64]:
    phi = _as_vector(rotation_vector, 3, "rotation_vector")
    phi_hat = _vector_to_skew_matrix(phi)
    theta = np.linalg.norm(phi)

    if np.isclose(theta, 0.0):
        return (
            np.eye(3, dtype=np.float64)
            + 0.5 * phi_hat
            + (1.0 / 6.0) * (phi_hat @ phi_hat)
        )

    phi_hat_squared = phi_hat @ phi_hat
    return (
        np.eye(3, dtype=np.float64)
        + ((1.0 - np.cos(theta)) / (theta**2)) * phi_hat
        + ((theta - np.sin(theta)) / (theta**3)) * phi_hat_squared
    )


class Twist2d(_Twist2d):
    def to_linear_numpy(self) -> NDArray[np.float64]:
        return np.array(
            [
                self.linear_velocity.x,
                self.linear_velocity.y,
            ],
            dtype=np.float64,
        )

    def set_linear_from_numpy(self, values: NDArray[np.float64]) -> None:
        vector = _as_vector(values, 2, "Twist2d.linear_velocity")
        self.linear_velocity.x = vector[0]
        self.linear_velocity.y = vector[1]

    def to_components(self) -> tuple[NDArray[np.float64], float]:
        return self.to_linear_numpy(), float(self.angular_velocity_rad_s)

    def from_components(
        self, linear_velocity: NDArray[np.float64], angular_velocity_rad_s: float
    ) -> None:
        self.set_linear_from_numpy(linear_velocity)
        self.angular_velocity_rad_s = angular_velocity_rad_s

    def to_matrix(self) -> NDArray[np.float64]:
        angular = float(self.angular_velocity_rad_s)
        return np.array(
            [
                [0.0, -angular, self.linear_velocity.x],
                [angular, 0.0, self.linear_velocity.y],
                [0.0, 0.0, 0.0],
            ],
            dtype=np.float64,
        )

    def to_numpy(self) -> NDArray[np.float64]:
        return self.to_matrix()

    def from_matrix(self, values: NDArray[np.float64]) -> None:
        matrix = _as_matrix(values, (3, 3), "Twist2d")
        if not np.allclose(matrix[2], 0.0):
            raise ValueError("Twist2d matrix bottom row must be zero")
        if not np.allclose(matrix[:2, :2], -matrix[:2, :2].T):
            raise ValueError("Twist2d rotation block must be skew-symmetric")

        self.linear_velocity.x = matrix[0, 2]
        self.linear_velocity.y = matrix[1, 2]
        self.angular_velocity_rad_s = matrix[1, 0]

    def from_numpy(self, values: NDArray[np.float64]) -> None:
        self.from_matrix(values)

    def to_transform(self, dt: float) -> NDArray[np.float64]:
        theta = float(self.angular_velocity_rad_s) * dt
        transform = np.eye(3, dtype=np.float64)
        transform[:2, :2] = _rotation_2d(theta)
        transform[:2, 2] = _left_jacobian_so2(theta) @ (self.to_linear_numpy() * dt)
        return transform


class Twist3d(_Twist3d):
    def to_linear_numpy(self) -> NDArray[np.float64]:
        return np.array(
            [
                self.linear_velocity.x,
                self.linear_velocity.y,
                self.linear_velocity.z,
            ],
            dtype=np.float64,
        )

    def angular_to_numpy(self) -> NDArray[np.float64]:
        return np.array(
            [
                self.angular_velocity_rad_s.x,
                self.angular_velocity_rad_s.y,
                self.angular_velocity_rad_s.z,
            ],
            dtype=np.float64,
        )

    def set_linear_from_numpy(self, values: NDArray[np.float64]) -> None:
        vector = _as_vector(values, 3, "Twist3d.linear_velocity")
        self.linear_velocity.x = vector[0]
        self.linear_velocity.y = vector[1]
        self.linear_velocity.z = vector[2]

    def set_angular_from_numpy(self, values: NDArray[np.float64]) -> None:
        vector = _as_vector(values, 3, "Twist3d.angular_velocity_rad_s")
        self.angular_velocity_rad_s.x = vector[0]
        self.angular_velocity_rad_s.y = vector[1]
        self.angular_velocity_rad_s.z = vector[2]

    def to_matrix(self) -> NDArray[np.float64]:
        skew = _vector_to_skew_matrix(self.angular_to_numpy())
        matrix = np.zeros((4, 4), dtype=np.float64)
        matrix[:3, :3] = skew
        matrix[:3, 3] = self.to_linear_numpy()
        return matrix

    def from_matrix(self, values: NDArray[np.float64]) -> None:
        matrix = _as_matrix(values, (4, 4), "Twist3d")
        if not np.allclose(matrix[3], 0.0):
            raise ValueError("Twist3d matrix bottom row must be zero")

        self.set_linear_from_numpy(matrix[:3, 3])
        self.set_angular_from_numpy(
            _skew_matrix_to_vector(matrix[:3, :3], "Twist3d rotation block")
        )

    def to_numpy(self) -> NDArray[np.float64]:
        return self.to_matrix()

    def from_numpy(self, values: NDArray[np.float64]) -> None:
        self.from_matrix(values)

    def to_transform(self, dt: float) -> NDArray[np.float64]:
        phi = self.angular_to_numpy() * dt
        transform = np.eye(4, dtype=np.float64)
        transform[:3, :3] = _rotation_3d(phi)
        transform[:3, 3] = _left_jacobian_so3(phi) @ (self.to_linear_numpy() * dt)
        return transform
