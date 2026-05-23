import json
from pydantic import AliasChoices, BaseModel, ConfigDict, Field

from backend.python.common.util.typing_stub import FilePath


class AutobahnConnectionConfig(BaseModel):
    host: str
    port: int


class WatchdogLoggingConfig(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    log_publish_topic: str = Field(
        validation_alias=AliasChoices(
            "log_publish_topic",
            "log_pub_topic",
            "global_log_pub_topic",
        )
    )
    publish_logs_over_autobahn: bool = Field(
        default=True,
        validation_alias=AliasChoices(
            "publish_logs_over_autobahn",
            "global_logging_publishing_enabled",
        ),
    )
    default_log_level: str = Field(
        validation_alias=AliasChoices(
            "default_log_level",
            "logging_level",
            "global_logging_level",
        )
    )


class WatchdogApiConfig(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    api_host: str = Field(
        default="0.0.0.0", validation_alias=AliasChoices("api_host", "host")
    )
    api_port: int = Field(validation_alias=AliasChoices("api_port", "port"))
    system_stats_publish_interval_seconds: float = Field(
        validation_alias=AliasChoices(
            "system_stats_publish_interval_seconds",
            "stats_pub_period_s",
        )
    )
    publish_system_stats: bool = Field(
        validation_alias=AliasChoices(
            "publish_system_stats",
            "send_system_stats",
            "send_stats",
        )
    )
    managed_process_state_file: str = Field(
        default="config/processes.json",
        validation_alias=AliasChoices(
            "managed_process_state_file",
            "process_memory_file",
        ),
    )


class WatchdogSystemConfig(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    autobahn_connection: AutobahnConnectionConfig = Field(
        validation_alias=AliasChoices(
            "autobahn_connection",
            "autobahn",
            "communication",
        )
    )
    logging: WatchdogLoggingConfig
    watchdog_api: WatchdogApiConfig = Field(
        validation_alias=AliasChoices("watchdog_api", "watchdog")
    )
    desired_config_base64_path: str = Field(
        validation_alias=AliasChoices(
            "desired_config_base64_path",
            "intended_config_path",
            "config_path",
        )
    )


SystemConfig = WatchdogSystemConfig


def load_system_config(path: FilePath) -> SystemConfig:
    with open(path, "r") as f:
        config_content = f.read()

    config_dict = json.loads(config_content)
    return WatchdogSystemConfig(**config_dict)
