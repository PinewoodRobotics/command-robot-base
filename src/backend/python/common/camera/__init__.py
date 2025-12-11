import importlib
import pkgutil
import os


def _import_all_modules():
    current_dir = os.path.dirname(os.path.abspath(__file__))
    # Import modules directly in the camera directory
    for _, name, _ in pkgutil.iter_modules([current_dir]):
        if name not in ["abstract_camera", "__init__"]:
            importlib.import_module(f".{name}", package=__package__)

    # Import subdirectories (like type_camera)
    for item in os.listdir(current_dir):
        item_path = os.path.join(current_dir, item)
        if (
            os.path.isdir(item_path)
            and not item.startswith("__")
            and not item.startswith(".")
        ):
            # Check if it's a Python package (has __init__.py)
            init_path = os.path.join(item_path, "__init__.py")
            if os.path.exists(init_path):
                importlib.import_module(f".{item}", package=__package__)


_import_all_modules()
