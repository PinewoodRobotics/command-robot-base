"use strict";

import * as fs from "fs";
import { TBinaryProtocol, TBufferedTransport } from "thrift";
import * as path from "path";
import { pathToFileURL } from "url";

type Protocol = "binary" | "json" | "json-binary";

const initialCwd = process.env.INIT_CWD || process.cwd();

try {
  process.chdir(initialCwd);
} catch {}

function resolveAbsolutePath(p: string): string {
  if (path.isAbsolute(p)) return p;
  return path.resolve(initialCwd, p);
}

function getRequiredArg(flag: "--dir" | "--protocol" | "--schema"): string {
  const args = process.argv.slice(2);
  const index = args.findIndex((arg) => arg === flag);
  const value =
    index !== -1 && index + 1 < args.length ? args[index + 1] : null;

  if (!value) {
    throw new Error(
      `Missing required argument ${flag}.\nUsage: tsx scripts/compile-config.ts --dir <config-dir> --protocol <binary|json|json-binary> --schema <generated-schema-module>`,
    );
  }

  return value;
}

function parseProtocol(protocol: string): Protocol {
  if (
    protocol === "binary" ||
    protocol === "json" ||
    protocol === "json-binary"
  ) {
    return protocol;
  }

  throw new Error(
    `Unsupported protocol "${protocol}". Expected one of: binary, json, json-binary.`,
  );
}

async function generateConfig(
  configDir: string,
  protocol: Protocol,
  schemaPath: string,
) {
  const resolvedConfigDir = resolveAbsolutePath(configDir);
  if (
    !fs.existsSync(resolvedConfigDir) ||
    !fs.statSync(resolvedConfigDir).isDirectory()
  ) {
    throw new Error(`Config directory not found: ${resolvedConfigDir}`);
  }

  const configModulePath = path.join(resolvedConfigDir, "index.ts");
  if (!fs.existsSync(configModulePath)) {
    throw new Error(`Config module not found: ${configModulePath}`);
  }

  const configModule = await import(pathToFileURL(configModulePath).href);

  const resolvedSchemaPath = resolveAbsolutePath(schemaPath);
  if (!fs.existsSync(resolvedSchemaPath)) {
    throw new Error(`Schema module not found: ${resolvedSchemaPath}`);
  }

  const schemaModule = await import(pathToFileURL(resolvedSchemaPath).href);
  const { Config } = schemaModule;
  if (!Config) {
    throw new Error(
      `Schema module does not export Config: ${resolvedSchemaPath}`,
    );
  }

  const configData =
    configModule.default ?? configModule.config ?? configModule;
  const configInstance = new Config(configData);
  let binaryData: Buffer = Buffer.alloc(0);

  const transport = new TBufferedTransport(undefined, (buf) => {
    binaryData = Buffer.from(buf ?? "");
  });
  const protocol_ = new TBinaryProtocol(transport);

  configInstance[Symbol.for("write")](protocol_);
  transport.flush();

  if (protocol === "json") {
    console.log(JSON.stringify(configInstance, null, 2));
    return;
  }

  if (protocol === "json-binary") {
    const output = {
      json: JSON.stringify(configInstance),
      binary_base64: binaryData.toString("base64"),
    };
    console.log(JSON.stringify(output));
    return;
  }

  console.log(binaryData.toString("base64"));
}

export { generateConfig };

const configDirArg = getRequiredArg("--dir");
const protocolArg = parseProtocol(getRequiredArg("--protocol"));
const schemaArg = getRequiredArg("--schema");

generateConfig(configDirArg, protocolArg, schemaArg).catch((error) => {
  console.error(error instanceof Error ? error.message : error);
  process.exit(1);
});
