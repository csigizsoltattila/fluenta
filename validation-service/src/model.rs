use serde::{Deserialize, Serialize};
use std::collections::HashMap;

/// A bejövő validációs kérés struktúrája.
/// A `data` mező `serde_json::Value`, mert nem tudjuk előre a struktúráját.
#[derive(Debug, Deserialize)]
pub struct ValidationRequest {
    // `serde_json::Value` egy "bármilyen" JSON objektumot képvisel
    data: serde_json::Value,
    rules: Vec<ValidationRule>,
}

/// A validációs szabály. Ennek meg kell egyeznie a Java DTO-val.
/// `#[serde(rename_all = "camelCase")]` biztosítja, hogy a Rust (snake_case)
/// és a JSON (camelCase) között működjön a konverzió.
#[derive(Debug, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ValidationRule {
    target_field: String,
    rule_type: String,
    parameters: Option<HashMap<String, String>>,
}

/// A validációs válasz.
#[derive(Debug, Serialize)]
pub struct ValidationResponse {
    status: String,
    errors: Vec<ValidationError>,
}

/// Egyedi validációs hiba.
#[derive(Debug, Serialize)]
pub struct ValidationError {
    field: String,
    message: String,
}

// Getterek, hogy a privát mezőket elérje a handler és a validator
impl ValidationRequest {
    pub fn data(&self) -> &serde_json::Value {
        &self.data
    }
    pub fn rules(&self) -> &[ValidationRule] {
        &self.rules
    }
}

impl ValidationRule {
    pub fn target_field(&self) -> &str {
        &self.target_field
    }
    pub fn rule_type(&self) -> &str {
        &self.rule_type
    }
    pub fn parameters(&self) -> &Option<HashMap<String, String>> {
        &self.parameters
    }
}

impl ValidationResponse {
    // Könnyítő "new" funkciók
    pub fn valid() -> Self {
        Self {
            status: "valid".to_string(),
            errors: Vec::new(),
        }
    }

    pub fn invalid(errors: Vec<ValidationError>) -> Self {
        Self {
            status: "invalid".to_string(),
            errors,
        }
    }
}

impl ValidationError {
    pub fn new(field: &str, message: &str) -> Self {
        Self {
            field: field.to_string(),
            message: message.to_string(),
        }
    }
}