from backend.deployment.deployer import BlitzNetworkDeployer, PresetConfigSuppliers
from backend.deployment.misc import output
from backend.deployment.module.supported import SupportedModules
from backend.deployment.network_api.utils import FolderPath
from backend.deployment.processes import ProcessPlan, WeightedProcess


class ProcessType(WeightedProcess):
    # Example:
    # APRILTAG = "apriltag", 1.0
    TEST = "test", 1.0


def pi_name_to_process_types(pi_names: list[str]) -> dict[str, list[ProcessType]]:
    return (
        ProcessPlan[ProcessType]().add(ProcessType.TEST)
        # .add(ProcessType.APRILTAG)
        # .pin(ProcessType.APRILTAG, "pi-name")
        .assign(pi_names)
    )


def get_modules() -> list[SupportedModules._Generic]:
    return [
        SupportedModules.GeneratedModule(
            name="generated-python",
            project_root_folder_path=FolderPath("src/backend/generated/"),
        ),
        SupportedModules.PythonModule(
            name="test",
            extra_run_args=[],
            equivalent_run_definition=ProcessType.TEST,
            module_folder_path=FolderPath("src/backend/python/test"),
        ),
    ]


if __name__ == "__main__":
    output.set_verbosity(True)

    config = (
        BlitzNetworkDeployer.Options()
        .set_local_backend_path(FolderPath("src/backend/"))
        .should_bundle_dependencies(True)
        .set_discovery_timeout(2)
        .set_config_supplier(PresetConfigSuppliers.NPM_CONFIG_COMMAND)
        .build()
    )

    BlitzNetworkDeployer.deploy(
        get_modules(),
        pi_name_to_process_types,
        config=config,
    )
