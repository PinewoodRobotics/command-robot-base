namespace py drone.config.common

struct GenericVector {
    1: required list<double> values,
    2: required i32 size,
}

struct GenericMatrix {
    1: required list<list<double>> values,
    2: required i32 rows,
    3: required i32 cols,
}
struct Point3 {
    1: required GenericVector position,
    2: required GenericMatrix rotation,
}

struct UnitConversion {
    1: required double non_unit_to_unit,
    2: required double unit_to_non_unit,
}

struct MapData {
    1: required list<bool> map_data,
    2: required i32 map_size_x,
    3: required i32 map_size_y,
}