import sys
import os
import subprocess
import shutil
import glob


def main():
    if len(sys.argv) <= 1:
        print("No Github libs found! Exiting...")
        exit(0)

    base_dir = os.getcwd()
    lib_dir = os.path.join(base_dir, "lib")
    vendor_dir = os.path.join(lib_dir, "vendor")
    build_dir = os.path.join(lib_dir, "build")

    os.makedirs(lib_dir, exist_ok=True)
    os.makedirs(vendor_dir, exist_ok=True)
    os.makedirs(build_dir, exist_ok=True)

    for lib in sys.argv[1:]:
        print("Cloning " + lib)

        os.chdir(lib_dir)
        os.chdir(vendor_dir)

        name = lib.split("/")[-1].replace(".git", "")
        print(f"Repository name: {name}")

        if not name in os.listdir("."):
            subprocess.run(["git", "clone", lib])

            os.chdir(name)
            subprocess.run(["git", "checkout", "master"])
            subprocess.run(["git", "pull"])
        else:
            os.chdir(name)

        if "build.gradle" in os.listdir("."):
            subprocess.run(["./gradlew", "clean"])
            subprocess.run(["./gradlew", "build"])

            jar_files = glob.glob("build/libs/*.jar")
            if jar_files:
                for jar_file in jar_files:
                    shutil.copy(jar_file, os.path.join(base_dir, "lib/build"))
                    print(f"Copied {jar_file} to {os.path.join(base_dir, 'lib/build')}")
            else:
                print("No .jar files found in build/libs!")
        else:
            print(f"No build.gradle found in {name}. Skipping this repository!")


if __name__ == "__main__":
    main()
    exit(0)
