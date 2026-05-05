#!/usr/bin/env node
"use strict";
const { execSync } = require("child_process");
const fs = require("fs");
const path = require("path");

if (process.argv.length < 4) {
  console.error(
    "Please provide a schema directory path and output directory path as arguments\n" +
      "Usage: node scripts/thrift-to-ts.js <schema_dir> <output_dir>",
  );
  process.exit(1);
}

const SCHEMA_DIR = process.argv[2];
const OUTPUT_DIR = process.argv[3];

function ensureDirectoryExists(dirPath) {
  return fs.existsSync(dirPath) || fs.mkdirSync(dirPath, { recursive: true });
}

function findThriftFiles(dir) {
  const files = [];
  const items = fs.readdirSync(dir);

  for (const item of items) {
    const fullPath = path.join(dir, item);
    const stat = fs.statSync(fullPath);
    console.log(fullPath);

    if (stat.isDirectory()) {
      files.push(...findThriftFiles(fullPath));
    } else if (item.endsWith(".thrift")) {
      files.push(fullPath);
    }
  }

  return files;
}

ensureDirectoryExists(OUTPUT_DIR);
const thriftFiles = findThriftFiles(SCHEMA_DIR);
for (const thriftFile of thriftFiles) {
  const cmd = `thrift --gen js:ts,node -o ${OUTPUT_DIR} -I ${SCHEMA_DIR} ${thriftFile}`;
  execSync(cmd, { stdio: "inherit" });
}
