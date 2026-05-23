import base64
import json
from enum import Enum
from typing import Any, Callable, Protocol, Type, TypeVar, Union
from backend.python.common.util.typing_stub import FilePath


class ThriftReadable(Protocol):
    def read(self, iprot: Any) -> None: ...


T = TypeVar("T")
TThrift = TypeVar("TThrift", bound=ThriftReadable)


def thrift_load_class_b64(contents: str, thrift_class: Type[TThrift]) -> TThrift:
    buffer = base64.b64decode(contents)

    from thrift.transport import TTransport
    from thrift.protocol import TBinaryProtocol

    transport = TTransport.TMemoryBuffer(buffer)
    protocol = TBinaryProtocol.TBinaryProtocol(transport)
    config = thrift_class()
    config.read(protocol)

    return config


class DefaultLoaders(Enum):
    JSON = "json"

    @staticmethod
    def THRIFT(thrift_class: Type[TThrift]) -> Callable[[str], TThrift]:
        return lambda contents: thrift_load_class_b64(contents, thrift_class)


def load_backend_config(
    path: FilePath,
    load_function: Union[Callable[[str], T], DefaultLoaders],
) -> Any:
    with open(path, "r") as f:
        config_content = f.read()

    if load_function == DefaultLoaders.JSON:
        return json.loads(config_content)

    return load_function(config_content)
