from enum import Enum


from backend.python.common.util.config.backend import (
    DefaultLoaders,
    load_backend_config,
)
from backend.python.common.util.config.blitz import SystemConfig, load_system_config
from backend.python.common.util.process import get_default_process_parser
from backend.python.common.util.system import DeploymentStates
from backend.python.common.util.typing_stub import FilePath, FolderPath
from backend.generated.thrift.frc4765.config.ttypes import Config


class _Internal:
    ARGS = get_default_process_parser().parse_args()

    CONFIG_PATH: FilePath = ARGS.config_path
    BASIC_SYSTEM_CONFIG_PATH: FilePath = ARGS.basic_system_config_path


SYSTEM_STATUS: DeploymentStates = DeploymentStates.PRODUCTION
BLITZ_PATH: FolderPath = _Internal.ARGS.blitz_path
BUNDLE_FOLDER_PATH: FolderPath = _Internal.ARGS.bundle_folder_path
SYSTEM_NAME: str = _Internal.ARGS.system_name

SYSTEM_CONFIG: SystemConfig = load_system_config(_Internal.CONFIG_PATH)
BACKEND_CONFIG: Config = load_backend_config(
    _Internal.CONFIG_PATH, DefaultLoaders.THRIFT(Config)
)
