from typing import Optional, cast
import numpy as np
from numpy.typing import NDArray

from backend.generated.thrift.frc4765.config.common.ttypes import (
    GenericVector,
    GenericMatrix,
)


def get_translation_rotation_components(
    transformation_matrix: NDArray[np.float64],
) -> tuple[NDArray[np.float64], NDArray[np.float64]]:
    translation = transformation_matrix[0:3, 3]
    rotation = transformation_matrix[0:3, 0:3]
    return translation, rotation


def normalize_vector(vector: NDArray[np.float64]) -> NDArray[np.float64]:
    return vector / np.linalg.norm(vector)


def make_transformation_matrix_p_d(
    *,
    position: NDArray[np.float64],
    direction_vector: NDArray[np.float64],
    z_axis: NDArray[np.float64] = np.array([0, 0, 1]),
) -> NDArray[np.float64]:
    x_axis = normalize_vector(direction_vector)
    y_axis = normalize_vector(
        np.cross(z_axis, x_axis)
    )  # pyright: ignore[reportArgumentType]
    return create_transformation_matrix(
        rotation_matrix=np.column_stack((x_axis, y_axis, z_axis)),
        translation_vector=np.array([position[0], position[1], position[2]]),
    )


def create_transformation_matrix(
    *,
    rotation_matrix: NDArray[np.float64],
    translation_vector: NDArray[np.float64],
) -> NDArray[np.float64]:
    transformation_matrix = np.eye(4)
    transformation_matrix[:3, :3] = rotation_matrix
    transformation_matrix[:3, 3] = translation_vector
    return transformation_matrix


def ensure_proper_rotation(rotation_matrix: NDArray[np.float64]) -> NDArray[np.float64]:
    u, _, vt = np.linalg.svd(rotation_matrix)
    r = u @ vt
    if np.linalg.det(r) < 0:
        u[:, -1] *= -1
        r = u @ vt
    return r


# T_bbb_in_aaa = T_###_in_aaa @ T_bbb_in_###
def get_robot_in_world(
    *,
    T_tag_in_camera: NDArray[np.float64],
    T_camera_in_robot: NDArray[np.float64],
    T_tag_in_world: NDArray[np.float64],
    R_robot_rotation_world: NDArray[np.float64] | None = None,
) -> NDArray[np.float64]:
    if R_robot_rotation_world is not None:
        # note this is work in process and, thus, may not work as expected
        # Extract position from tag detection
        tag_position_camera = T_tag_in_camera[:3, 3]

        # Transform tag position to robot frame
        camera_position_robot = T_camera_in_robot[:3, 3]
        camera_rotation_robot = T_camera_in_robot[:3, :3]
        tag_position_robot = (
            camera_position_robot + camera_rotation_robot @ tag_position_camera
        )

        # Transform tag position to world frame using robot's known rotation
        tag_position_world = T_tag_in_world[:3, 3]
        robot_position_world = (
            tag_position_world - R_robot_rotation_world @ tag_position_robot
        )

        return create_transformation_matrix(
            rotation_matrix=R_robot_rotation_world,
            translation_vector=robot_position_world,
        )
    else:
        T_tag_in_robot = T_camera_in_robot @ T_tag_in_camera
        T_robot_in_tag = np.linalg.inv(T_tag_in_robot)
        T_robot_in_world = T_tag_in_world @ T_robot_in_tag
        return T_robot_in_world


def from_float_list(
    flat_list: list[float], rows: int, cols: int
) -> NDArray[np.float64]:
    if not flat_list or len(flat_list) != rows * cols:
        raise ValueError("The provided list does not match the specified dimensions.")
    return np.array(flat_list).reshape(rows, cols)


def get_np_from_vector(vector: GenericVector) -> NDArray[np.float64]:
    return np.array(vector.values)


def transform_vector_to_size(
    vector: NDArray[np.float64],
    used_indices: list[bool],
) -> NDArray[np.float64]:
    return np.array([v for v, i in zip(vector, used_indices) if i])
