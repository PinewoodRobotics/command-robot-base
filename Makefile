export PYTHONPATH := $(shell pwd)
ARGS ?=

PYTHON ?= ./.venv/bin/python

PROTO_ROOT := src/proto
PROTO_FILES = $(shell $(PYTHON) -c "from pathlib import Path; print(' '.join(str(path) for path in sorted(Path('$(PROTO_ROOT)').rglob('*.proto'))))")
PROTO_SUBPROJECTS ?= src/backend

THRIFT_ROOT_DIR := config/schema
THRIFT_ROOT_FILE := $(THRIFT_ROOT_DIR)/config.thrift
THRIFT_OUT := src/backend/generated/thrift

.PHONY: prep-project generate-proto-python proto-generate proto-clean thrift-to-py thriftpy-generate thriftts-generate thrift-generate generate deploy-backend

prep-project:
	if [ ! -d ".venv" ]; then \
		python3 -m venv .venv; \
	fi

	.venv/bin/pip install -r requirements.txt

proto-generate:
	@set -e; \
	if [ -z "$(strip $(PROTO_SUBPROJECTS))" ]; then \
		echo "Set PROTO_SUBPROJECTS to one or more subproject paths."; \
		exit 1; \
	fi; \
	if [ -z "$(strip $(PROTO_FILES))" ]; then \
		echo "No .proto files found under $(PROTO_ROOT)"; \
		exit 1; \
	fi; \
	for subproject in $(PROTO_SUBPROJECTS); do \
		output_dir="$$subproject/generated/proto/python"; \
		mkdir -p "$$output_dir"; \
		echo "Generating protobuf files into $$output_dir"; \
		protoc -I "$(PROTO_ROOT)" \
			--python_out="$$output_dir" \
			--pyi_out="$$output_dir" \
			$(PROTO_FILES); \
		.venv/bin/fix-protobuf-imports "$$output_dir"; \
	done

generate-proto-python: proto-generate

proto-clean:
	@set -e; \
	for subproject in $(PROTO_SUBPROJECTS); do \
		output_dir="$$subproject/generated/proto"; \
		if [ -d "$$output_dir" ]; then \
			echo "Removing $$output_dir"; \
			rm -rf "$$output_dir"; \
		fi; \
	done

thriftpy-generate:
	mkdir -p $(THRIFT_OUT)
	thrift -r --gen py:type_hints,enum,package_prefix=backend.generated.thrift. \
		-I $(THRIFT_ROOT_DIR) \
		-out $(THRIFT_OUT) \
		$(THRIFT_ROOT_FILE);

thrift-to-py: thriftpy-generate

thriftts-generate:
	npm run generate-thrift

thrift-generate: thriftpy-generate thriftts-generate

generate: proto-generate thrift-generate

deploy-backend:
	PYTHONPATH="$(PWD)/src" $(PYTHON) -m backend.deploy
