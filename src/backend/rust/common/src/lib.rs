pub mod camera;
pub mod config;
pub mod device_info;
pub mod math;

pub mod proto {
    pub mod autobahn {
        include!(concat!(env!("OUT_DIR"), "/proto.autobahn.rs"));
    }
    pub mod sensor {
        include!(concat!(env!("OUT_DIR"), "/proto.sensor.rs"));
    }
    pub mod status {
        include!(concat!(env!("OUT_DIR"), "/proto.status.rs"));
    }
    pub mod util {
        include!(concat!(env!("OUT_DIR"), "/proto.util.rs"));
    }

    pub mod pathfind {
        include!(concat!(env!("OUT_DIR"), "/proto.pathfind.rs"));
    }
}

macro_rules! include_thrift_module {
    ($name:ident) => {
        pub mod $name {
            include!(concat!(
                env!("THRIFT_OUT_DIR"),
                "/",
                stringify!($name),
                ".rs"
            ));
        }
    };
}

#[allow(dead_code, unused_imports, unused_extern_crates)]
#[allow(
    clippy::too_many_arguments,
    clippy::type_complexity,
    clippy::vec_box,
    clippy::wrong_self_convention
)]
pub mod thrift {
    include_thrift_module!(common);
    include_thrift_module!(camera);
    include_thrift_module!(apriltag);
    include_thrift_module!(lidar);
    include_thrift_module!(pos_extrapolator);
    include_thrift_module!(kalman_filter);
    include_thrift_module!(pathfinding);
    include_thrift_module!(config);
    include_thrift_module!(image_recognition);
}
