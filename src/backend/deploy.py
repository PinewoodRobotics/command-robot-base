from backend.deployment.deployer import BlitzNetworkDeployer, PresetConfigSuppliers
from backend.deployment.misc import output
from backend.deployment.module.supported import SupportedModules
from backend.deployment.processes import ProcessPlan, WeightedProcess


class ProcessType(WeightedProcess):
    # Example:
    # APRILTAG = "apriltag", 1.0
    pass


def pi_name_to_process_types(pi_names: list[str]) -> dict[str, list[ProcessType]]:
    return (
        ProcessPlan[ProcessType]()
        # .add(ProcessType.APRILTAG)
        # .pin(ProcessType.APRILTAG, "pi-name")
        .assign(pi_names)
    )


def get_modules() -> list[SupportedModules.Generic]:
    return [
        # SupportedModules.PythonModule(
        #     name="example",
        #     extra_run_args=[],
        #     equivalent_run_definition=ProcessType.APRILTAG,
        #     module_folder_path="src/backend//python/example",
        # ),
    ]


if __name__ == "__main__":
    output.set_verbosity(False)

    config = (
        BlitzNetworkDeployer.Options()
        .set_local_backend_path("src/backend/")
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
