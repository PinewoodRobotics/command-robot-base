import sys
import os
import subprocess
import shutil
import glob
import argparse
import configparser
import logging
import stat


def check_folders():
    if (
        not os.path.exists("lib")
        or not os.path.isdir("lib/build")
        or not os.path.isdir("lib/vendor")
    ):
        os.makedirs("lib/build", exist_ok=True)
        os.makedirs("lib/vendor", exist_ok=True)


def delete_folder(folder_name):
    if os.path.exists(folder_name):
        shutil.rmtree(folder_name)


def print_separator(name: str, color: str = "cyan", char: str = "-", width: int = 32):
    """
    Prints a colored separator line with the given name centered.
    color: one of 'red', 'green', 'yellow', 'blue', 'magenta', 'cyan', 'white'
    char: the character to use for the separator
    width: number of chars on each side of the name
    """
    colors = {
        "red": "\033[91m",
        "green": "\033[92m",
        "yellow": "\033[93m",
        "blue": "\033[94m",
        "magenta": "\033[95m",
        "cyan": "\033[96m",
        "white": "\033[97m",
        "reset": "\033[0m",
    }
    print()
    color_code = colors.get(color, colors["cyan"])
    reset_code = colors["reset"]
    sep = f" {name} "
    total_len = width * 2 + len(sep)
    line = sep.center(total_len, char)
    print(f"{color_code}{line}{reset_code}")
    print()


def main():
    logging.basicConfig(
        level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s"
    )

    parser = argparse.ArgumentParser(description="Clone and build GitHub libraries")
    parser.add_argument("--config-file-path", type=str, help="Path to the config file")
    args = parser.parse_args()

    config = configparser.ConfigParser()
    config.read(args.config_file_path)

    check_folders()

    vendor_path = os.path.abspath("lib/vendor")
    build_jar_path = os.path.abspath("lib/build")

    for section in config.sections():
        os.chdir(vendor_path)
        if config.getboolean(section, "build_dynamically"):
            logging.info(f"Building {section}")
        else:
            logging.info(f"Skipping {section}")
            continue

        github_url = config.get(section, "github")
        use_branch = config.has_option(section, "branch")

        if config.getboolean(section, "force_clone") and os.path.exists(section):
            delete_folder(section)

        if not os.path.exists(section):
            print(f"Cloning {section}")
            subprocess.run(
                ["git", "clone", github_url, "--single-branch", section]
                + (
                    []
                    if not use_branch
                    else ["--branch", config.get(section, "branch")]
                )
            )
        else:
            logging.info(f"{section} already exists. Building from existing repo.")

        repo_path = os.path.join(vendor_path, section)
        os.chdir(repo_path)

        print_separator(section, "green")

        out = subprocess.run(["./gradlew", "build"], capture_output=True, text=True)
        print(out.stdout)
        if out.returncode != 0:
            logging.error(f"Failed to build {section}:\n{out.stderr}")
            exit(1)
        else:
            logging.info(f"Successfully built {section}")

        build_libs_path = os.path.join("build", "libs")
        for jar_file in glob.glob(os.path.join(build_libs_path, "*.jar")):
            dest_path = os.path.abspath(
                os.path.join(build_jar_path, os.path.basename(jar_file))
            )
            shutil.copy(jar_file, dest_path)


if __name__ == "__main__":
    main()
    exit(0)
