import numpy as np
from numpy.typing import NDArray
from backend.generated.thrift.frc4765.config.common.ttypes import GenericMatrix


def from_theta_to_3x3_mat(theta: float):
    """
    Convert a rotation angle in degrees to a 3x3 rotation matrix.
    Theta is the rotation angle around the z-axis in degrees [0, 360].
    """

    theta_rad = np.deg2rad(theta)
    return np.array(
        [
            [np.cos(theta_rad), -np.sin(theta_rad), 0],
            [np.sin(theta_rad), np.cos(theta_rad), 0],
            [0, 0, 1],
        ]
    )


def get_np_from_matrix(
    matrix: GenericMatrix,
) -> NDArray[np.float64]:
    return np.array(matrix.values)


def swap_rotation_components(
    *, T_one: NDArray[np.float64], T_two: NDArray[np.float64], R_side_size: int
):
    # NOTE: This function swaps the rotation (top-left R_side_size x R_side_size) blocks of T_one and T_two,
    # returning new matrices with the swapped rotation components and all other elements preserved.
    T_one_new = T_one.copy()
    T_two_new = T_two.copy()
    T_one_new[:R_side_size, :R_side_size], T_two_new[:R_side_size, :R_side_size] = (
        T_two[:R_side_size, :R_side_size].copy(),
        T_one[:R_side_size, :R_side_size].copy(),
    )
    return T_one_new, T_two_new


def transform_matrix_to_size(
    used_diagonals: list[bool],
    matrix: NDArray[np.float64] = np.eye(6),
) -> NDArray[np.float64]:
    indices = [i for i, used in enumerate(used_diagonals) if used]
    return matrix[indices, :]


def transform_matrix_to_size_square(
    used_diagonals: list[bool],
    matrix: NDArray[np.float64] = np.eye(6),
) -> NDArray[np.float64]:
    indices = [i for i, used in enumerate(used_diagonals) if used]
    return matrix[np.ix_(indices, indices)]
