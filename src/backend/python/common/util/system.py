from enum import Enum
import os
import sys
from types import ModuleType
from typing import NewType
import psutil
import json
import re
import netifaces
import socket

from backend.python.common.config import from_uncertainty_config
from backend.generated.thrift.frc4765.config.ttypes import Config
import importlib
import importlib.util


class DeploymentStates(Enum):
    PRODUCTION = "production"
    DEVELOPMENT_LOCAL = "development_local"
    DEVELOPMENT = "development_remote"
    SIMULATION = "simulation"


def get_top_10_processes() -> list[psutil.Process]:
    processes = sorted(
        [
            p
            for p in psutil.process_iter(attrs=["pid", "name", "cpu_percent"])
            if p.info["cpu_percent"] is not None
        ],
        key=lambda p: p.info["cpu_percent"],
        reverse=True,
    )

    return processes[:10]


def get_local_ip(iface: str = "eth0") -> str | None:
    """
    Returns the IPv4 address for the given interface (e.g. "eth0" or "en0"),
    or None if the interface isn't found or has no IPv4 address.
    """
    try:
        addrs = netifaces.ifaddresses(iface)
        ipv4 = addrs.get(netifaces.AF_INET, [])
        if ipv4 and "addr" in ipv4[0]:
            return ipv4[0]["addr"]
    except ValueError:
        pass
    return None


def get_local_hostname(include_local_suffix: bool = True) -> str:
    hostname = socket.gethostname()
    if include_local_suffix and not hostname.endswith(".local"):
        return f"{hostname}.local"
    return hostname


def setup_shared_library_python_extension(
    *,
    module_name: str,
    py_lib_searchpath: str,
    module_basename: str | None = None,
) -> ModuleType:
    module_basename = module_basename if module_basename else module_name

    module_parent = str(os.path.dirname(str(py_lib_searchpath)))
    if module_parent not in sys.path:
        sys.path.insert(0, module_parent)

    module_path = os.path.join(str(py_lib_searchpath), module_basename)
    extension_file: str | None = None
    dir_path = os.path.dirname(module_path)
    base_stem = os.path.basename(module_path)
    if os.path.isdir(dir_path):
        for fname in os.listdir(dir_path):
            if (
                fname.startswith(base_stem)
                and (fname.endswith(".so") or fname.endswith(".pyd"))
                and os.path.isfile(os.path.join(dir_path, fname))
            ):
                extension_file = os.path.join(dir_path, fname)
                break

    spec = importlib.util.spec_from_file_location(module_name, extension_file)
    if spec is None or spec.loader is None:
        raise ImportError(
            f"Failed to create spec for {module_name} from {extension_file}"
        )
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module
