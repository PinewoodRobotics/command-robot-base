from argparse import ArgumentParser


def get_default_process_parser() -> ArgumentParser:
    parser = ArgumentParser()

    parser.add_argument("--config-path", type=str)
    parser.add_argument("--basic-system-config-path", type=str)
    parser.add_argument("--blitz-path", type=str)
    parser.add_argument("--bundle-folder-path", type=str)
    parser.add_argument("--system-name", type=str)

    return parser
