import {
  OdomConfig,
  OdometryPositionSource,
} from "generated/thrift/gen-nodejs/pos_extrapolator_types";
import { MatrixUtil, VectorUtil } from "../../util/math";

export const swerve_odom_config: OdomConfig = {
  position_source: OdometryPositionSource.ABS_CHANGE,
  use_rotation: false,
  imu_robot_position: {
    position: VectorUtil.fromArray([0, 0, 0]),
    rotation: MatrixUtil.buildMatrix([
      [1, 0, 0],
      [0, 1, 0],
      [0, 0, 1],
    ]),
  },
};
