use axum::{
    routing::post,
    Router,
};
use std::net::SocketAddr;
use tower_http::cors::{Any, CorsLayer};
use tracing_subscriber::{layer::SubscriberExt, util::SubscriberInitExt};

// Behozzuk a modulokat
mod handler;
mod model;
mod validator;

#[tokio::main]
async fn main() {
    // Logolás inicializálása
    tracing_subscriber::registry()
        .with(
            tracing_subscriber::EnvFilter::try_from_default_env()
                .unwrap_or_else(|_| "validation_service=debug,tower_http=debug".into()),
        )
        .with(tracing_subscriber::fmt::layer())
        .init();

    tracing::debug!("Starting validation service...");

    // CORS réteg, ami mindent megenged (fejlesztéshez)
    let cors = CorsLayer::new()
        .allow_origin(Any)
        .allow_methods(Any)
        .allow_headers(Any);

    // Az alkalmazás routere
    let app = Router::new()
        .route("/validate", post(handler::validate_handler))
        .layer(cors);

    // A szerver indítása (port 8081, hogy ne ütközzön a Javaval)
    let addr = SocketAddr::from(([0, 0, 0, 0], 8081));
    tracing::debug!("Listening on {}", addr);

    let listener = tokio::net::TcpListener::bind(addr).await.unwrap();
    axum::serve(listener, app).await.unwrap();
}