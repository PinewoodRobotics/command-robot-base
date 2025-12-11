import {
  AprilTagConfig,
  TagDistanceDiscardMode,
  TagUseImuRotation,
} from "generated/thrift/gen-nodejs/pos_extrapolator_types";
import { reefscape_field } from "../tag_config/reefscape";
import { MatrixUtil, VectorUtil } from "../../util/math";

const april_tag_pos_config: AprilTagConfig = {
  tag_position_config: reefscape_field,
  tag_discard_mode: TagDistanceDiscardMode.ADD_WEIGHT,
  camera_position_config: {
    front_left: {
      position: VectorUtil.fromArray([0.33, 0.33, 0.0]),
      rotation: MatrixUtil.buildRotationMatrixFromYaw(45),
    },
    front_right: {
      position: VectorUtil.fromArray([0.33, -0.33, 0.0]),
      rotation: MatrixUtil.buildRotationMatrixFromYaw(-45),
    },
    rear_left: {
      position: VectorUtil.fromArray([-0.33, 0.33, 0.0]),
      rotation: MatrixUtil.buildRotationMatrixFromYaw(135),
    },
    rear_right: {
      position: VectorUtil.fromArray([-0.33, -0.33, 0.0]),
      rotation: MatrixUtil.buildRotationMatrixFromYaw(225),
    },
  },
  tag_use_imu_rotation: TagUseImuRotation.NEVER,
  discard_config: {
    distance_threshold: 4,
    angle_threshold_degrees: 10,
    weight_per_m_from_discard_distance: 2,
    weight_per_degree_from_discard_angle: 0.5,
  },
};

export default april_tag_pos_config;
