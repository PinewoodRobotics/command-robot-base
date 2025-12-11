use nalgebra::{Isometry2, Point2, Transform2, UnitComplex};

use crate::proto::util::{robot_position::Position, Position2d, Vector2, Vector3};

macro_rules! impl_vector_conversions {
    ($proto_type:ty, $nalgebra_type:ty, $($field:ident => $accessor:ident),*) => {
        impl From<$proto_type> for $nalgebra_type {
            fn from(vector: $proto_type) -> Self {
                <$nalgebra_type>::new($(vector.$field.into()),*)
            }
        }

        impl From<$nalgebra_type> for $proto_type {
            fn from(vector: $nalgebra_type) -> Self {
                Self {
                    $($field: vector.$accessor.into()),*
                }
            }
        }
    };
}

impl_vector_conversions!(Vector3, nalgebra::Vector3<f32>, x => x, y => y, z => z);
impl_vector_conversions!(Vector2, nalgebra::Vector2<f32>, x => x, y => y);

impl From<Position2d> for Isometry2<f32> {
    fn from(pos: Position2d) -> Self {
        let translation = nalgebra::Vector2::from(pos.position.unwrap());
        let direction = nalgebra::Vector2::from(pos.direction.unwrap());

        let angle = direction.y.atan2(direction.x);
        let rotation = UnitComplex::new(angle);

        Isometry2::from_parts(translation.into(), rotation)
    }
}
