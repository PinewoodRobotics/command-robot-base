import { MatrixUtil, VectorUtil } from "../../util/math";

export const comp_lab = {
  1: {
    position: VectorUtil.fromArray([1, 0, 0]),
    rotation: MatrixUtil.buildMatrix([
      [-1, 0, 0],
      [0, 1, 0],
      [0, 0, 1],
    ]),
  },
};
