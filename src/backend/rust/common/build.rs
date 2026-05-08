use regex::Regex;
use std::collections::HashSet;
use std::env;
use std::fs;
use std::path::Path;
use std::path::PathBuf;
use walkdir::WalkDir;

extern crate prost_build;

fn main() {
    let workspace_dir = workspace_dir().expect("Failed to find workspace root");

    let proto_root_path = env::var("PROTO_ROOT_PATH").expect("PROTO_ROOT_PATH is not set");
    let proto_dir = workspace_path(&workspace_dir, proto_root_path);

    println!("proto_dir: {}", proto_dir.display());

    let proto_files: Vec<String> = WalkDir::new(&proto_dir)
        .follow_links(true)
        .into_iter()
        .filter_map(|e| e.ok())
        .filter(|e| e.path().extension().map_or(false, |ext| ext == "proto"))
        .map(|e| e.path().to_str().unwrap().to_string())
        .collect();

    let include_dirs: Vec<String> = WalkDir::new(&proto_dir)
        .follow_links(true)
        .into_iter()
        .filter_map(|e| e.ok())
        .filter(|e| e.path().is_dir())
        .map(|e| e.path().to_str().unwrap().to_string())
        .collect();

    prost_build::compile_protos(&proto_files, &include_dirs).unwrap();

    println!("cargo:rerun-if-changed={}", proto_dir.display());

    // Generate Thrift bindings
    generate_thrift_bindings();
}

fn find_workspace_root() -> Result<String, Box<dyn std::error::Error>> {
    let manifest_dir = env::var("CARGO_MANIFEST_DIR")?;
    let mut current = PathBuf::from(manifest_dir);

    loop {
        let cargo_toml = current.join("Cargo.toml");
        if cargo_toml.exists() {
            let content = fs::read_to_string(&cargo_toml)?;
            if content.contains("[workspace]") {
                return Ok(current.to_str().unwrap().to_string());
            }
        }

        if !current.pop() {
            break;
        }
    }

    Err("Could not find workspace root".into())
}

fn workspace_dir() -> Result<PathBuf, Box<dyn std::error::Error>> {
    env::var("CARGO_WORKSPACE_DIR")
        .map(PathBuf::from)
        .or_else(|_| find_workspace_root().map(PathBuf::from))
}

fn workspace_path(workspace_dir: &Path, path: impl AsRef<Path>) -> PathBuf {
    let path = path.as_ref();

    if path.is_absolute() {
        path.to_path_buf()
    } else {
        workspace_dir.join(path)
    }
}

fn generate_thrift_bindings() {
    let workspace_dir = workspace_dir().expect("Failed to find workspace root");
    let (schema_dir, config_thrift) = resolve_thrift_paths(&workspace_dir);

    println!("schema_dir: {}", schema_dir.display());

    let out_dir = env::var("OUT_DIR").unwrap();
    let thrift_out_dir = PathBuf::from(&out_dir).join("thrift");

    // Create output directory
    std::fs::create_dir_all(&thrift_out_dir).unwrap();

    println!("cargo:rerun-if-changed={}", schema_dir.display());
    println!("cargo:rerun-if-changed={}", config_thrift.display());

    // Generate Rust code using thrift compiler
    let mut cmd = std::process::Command::new("thrift");
    cmd.arg("--gen")
        .arg("rs")
        .arg("-r")
        .arg("-I")
        .arg(&schema_dir)
        .arg("-out")
        .arg(&thrift_out_dir)
        .arg(&config_thrift);

    let output = cmd
        .output()
        .expect("Failed to execute thrift compiler. Make sure 'thrift' is installed and in PATH.");

    if !output.status.success() {
        panic!(
            "Thrift compilation failed for {}: {}",
            config_thrift.display(),
            String::from_utf8_lossy(&output.stderr)
        );
    }

    // Post-process generated files to fix import paths
    fix_generated_imports(&thrift_out_dir);

    println!(
        "cargo:rustc-env=THRIFT_OUT_DIR={}",
        thrift_out_dir.display()
    );
}

fn resolve_thrift_paths(workspace_dir: &Path) -> (PathBuf, PathBuf) {
    let configured_root_file = env::var("THRIFT_ROOT_FILE_PATH")
        .ok()
        .map(|path| workspace_path(workspace_dir, path));

    let configured_root_dir = env::var("THRIFT_ROOT_PATH")
        .ok()
        .map(|path| workspace_path(workspace_dir, path));

    let configured_root_file_name =
        env::var("THRIFT_ROOT_FILE").unwrap_or_else(|_| "config.thrift".to_string());

    let thrift_file = configured_root_file
        .or_else(|| {
            configured_root_dir
                .as_ref()
                .map(|dir| dir.join(&configured_root_file_name))
        })
        .unwrap_or_else(|| workspace_dir.join("config/schema/config.thrift"));

    let thrift_file = if thrift_file.exists() {
        thrift_file
    } else {
        let fallback = workspace_dir.join("config/schema/config.thrift");

        if fallback.exists() {
            println!(
                "cargo:warning=Configured Thrift root file does not exist: {}; using {}",
                thrift_file.display(),
                fallback.display()
            );
            fallback
        } else {
            thrift_file
        }
    };

    let schema_dir = thrift_file.parent().unwrap_or(workspace_dir).to_path_buf();

    (schema_dir, thrift_file)
}

fn fix_generated_imports(thrift_out_dir: &Path) {
    let mut module_names: HashSet<String> = HashSet::new();

    for entry in WalkDir::new(thrift_out_dir)
        .into_iter()
        .filter_map(|e| e.ok())
    {
        let path = entry.path();
        if path.is_file() && path.extension().and_then(|e| e.to_str()) == Some("rs") {
            if let Some(stem) = path.file_stem().and_then(|s| s.to_str()) {
                module_names.insert(stem.to_string());
            }
        }
    }

    // Regex: `use foo;`
    let bare_use_re = Regex::new(r"(?m)^(\s*)use\s+([a-zA-Z_][a-zA-Z0-9_]*)\s*;").unwrap();

    // Regex: `use crate::foo;`
    let crate_use_re = Regex::new(r"(?m)^(\s*)use\s+crate::([a-zA-Z_][a-zA-Z0-9_]*)\s*;").unwrap();

    for entry in WalkDir::new(thrift_out_dir)
        .into_iter()
        .filter_map(|e| e.ok())
    {
        let path = entry.path();
        if !path.is_file() || path.extension().and_then(|e| e.to_str()) != Some("rs") {
            continue;
        }

        let content =
            fs::read_to_string(path).unwrap_or_else(|e| panic!("Failed to read {:?}: {e}", path));

        // Keep your inner→outer attribute fix
        let mut fixed = content
            .replace("#![allow(", "#[allow(")
            .replace("#![cfg_attr(", "#[cfg_attr(");

        // Replace `use foo;` where foo is a generated module
        fixed = bare_use_re
            .replace_all(&fixed, |caps: &regex::Captures| {
                let indent = &caps[1];
                let ident = &caps[2];

                if module_names.contains(ident) {
                    format!("{indent}use crate::thrift::{ident};")
                } else {
                    caps[0].to_string()
                }
            })
            .into_owned();

        // Replace `use crate::foo;` similarly
        fixed = crate_use_re
            .replace_all(&fixed, |caps: &regex::Captures| {
                let indent = &caps[1];
                let ident = &caps[2];

                if module_names.contains(ident) {
                    format!("{indent}use crate::thrift::{ident};")
                } else {
                    caps[0].to_string()
                }
            })
            .into_owned();

        fs::write(path, fixed).unwrap_or_else(|e| panic!("Failed to write {:?}: {e}", path));
    }
}
