import { CameraType } from "generated/thrift/gen-nodejs/camera_types";

export const MapUtil = {
  fromRecord<K extends string | number | symbol, V>(
    record: Record<K, V>
  ): globalThis.Map<K, V> {
    return new globalThis.Map(Object.entries(record) as [K, V][]);
  },
};

export function fromCamType(type: CameraType) {
  switch (type) {
    case CameraType.OV2311:
      return 0;
    default:
      return 1;
  }
}
