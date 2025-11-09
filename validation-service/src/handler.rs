use axum::{http::StatusCode, response::IntoResponse, Json};
use crate::model::{ValidationRequest, ValidationResponse};
use crate::validator;
use tracing::debug;

/// A `/validate` végpont fő kezelőfüggvénye.
pub async fn validate_handler(
    Json(payload): Json<ValidationRequest>,
) -> (StatusCode, Json<ValidationResponse>) {

    debug!("Received validation request. Data: {:?}, Rules: {:?}", payload.data(), payload.rules());

    // Futtatjuk a validátort
    let errors = validator::validate_data(payload.data(), payload.rules());

    if errors.is_empty() {
        debug!("Validation successful.");
        (StatusCode::OK, Json(ValidationResponse::valid()))
    } else {
        debug!("Validation failed with errors: {:?}", errors);
        // A kérés sikeres volt, de a validáció nem,
        // ezért 200 OK-t adunk vissza, de a body-ban jelezzük a hibát.
        // Visszaadhatnánk (StatusCode::BAD_REQUEST, ...) is, ez dizájn kérdése.
        (StatusCode::OK, Json(ValidationResponse::invalid(errors)))
    }
}