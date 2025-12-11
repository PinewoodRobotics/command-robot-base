import type { Config } from "generated/thrift/gen-nodejs/config_types";
import { april_tag_detection_config } from "./april_tags_detection";
import lidar_configs from "./lidar";
import pathfinding_config from "./pathfinding";
import { pose_extrapolator } from "./pos_extrapolator";
import logitech_cam from "./cameras/logitech_cam";

const config: Config = {
  pos_extrapolator: pose_extrapolator,
  cameras: [logitech_cam],
  april_detection: april_tag_detection_config,
  lidar_configs: lidar_configs,
  pathfinding: pathfinding_config,
  record_replay: false,
  replay_folder_path: "replays",
  object_recognition: {
    cameras_to_use: [],
    model_path: "",
    output_topic: "",
    objects_to_detect: [],
    device: "",
    iou_threshold: 0,
    conf_threshold: 0,
  },
};

export default config;
