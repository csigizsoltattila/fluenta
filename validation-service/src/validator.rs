use crate::model::{ValidationError, ValidationRule};
use regex::Regex;
use serde_json::Value;

/// Fő validációs funkció. Végigmegy a szabályokon és lefuttatja őket.
pub fn validate_data(data: &Value, rules: &[ValidationRule]) -> Vec<ValidationError> {
    let mut errors = Vec::new();

    for rule in rules {
        // 1. Megkeressük az értéket a JSON-ban a "dot.notation.path" alapján
        let value_node = get_node_by_path(data, rule.target_field());

        // 2. Futtatjuk a szabálytípust
        let is_valid = match rule.rule_type().to_uppercase().as_str() {
            "NOT_NULL" => {
                value_node.is_some() && !value_node.unwrap().is_null()
            }
            "IS_EMAIL" => {
                // Egy egyszerűsített email regex.
                // Élesben egy robusztusabb crate (pl. `validator`) jobb lehet.
                let email_regex = Regex::new(r"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$").unwrap();
                match value_node.and_then(|v| v.as_str()) {
                    Some(s) => email_regex.is_match(s),
                    None => false, // Ha nem string (vagy null), akkor nem email
                }
            }
            "REGEX" => {
                match rule.parameters().as_ref().and_then(|params| params.get("pattern")) {
                    Some(pattern_str) => {
                        match (Regex::new(pattern_str), value_node.and_then(|v| v.as_str())) {
                            (Ok(re), Some(s)) => re.is_match(s),
                            _ => false // Helytelen regex vagy nem string az érték
                        }
                    },
                    None => false // Nincs "pattern" paraméter
                }
            }
            // ... TODO: További szabályok implementálása
            _ => {
                // Ismeretlen szabálytípus, fogadjuk el
                tracing::warn!("Unknown validation rule: {}", rule.rule_type());
                true
            }
        };

        if !is_valid {
            errors.push(ValidationError::new(
                rule.target_field(),
                &format!("Rule {} failed", rule.rule_type()),
            ));
        }
    }

    errors
}

/// Segédfüggvény: Érték lekérése "dot.notation" (pl. "client.personal.firstName") alapján.
/// Leegyszerűsített, nem kezeli a tömböket.
fn get_node_by_path<'a>(root: &'a Value, path: &str) -> Option<&'a Value> {
    let parts: Vec<&str> = path.split('.').collect();
    let mut current = root;

    for part in parts {
        match current.get(part) {
            Some(node) => current = node,
            None => return None,
        }
    }

    Some(current)
}