# 1. fázis: Buildelés
FROM rust:1.82-slim AS builder
LABEL authors="csigi"

WORKDIR /usr/src/app

# Először csak a Cargo.toml-t másoljuk be, hogy a cache-elést kihasználjuk
COPY Cargo.toml Cargo.lock ./
# Hozzáadunk egy üres main.rs-t, hogy a függőségeket letöltse
RUN mkdir src && echo "fn main() {}" > src/main.rs
RUN cargo build --release --quiet

# Másoljuk a teljes forráskódot
COPY src ./src
# Buildeljük a valós projektet
# A --release flag optimalizált binárist készít
RUN touch src/main.rs && cargo build --release --quiet

# 2. fázis: Futtató image
# Kisebb image-et használunk a futtatáshoz
FROM debian:12-slim

# Szükséges lehet a futtatáshoz (pl. SSL)
RUN apt-get update && apt-get install -y ca-certificates && rm -rf /var/lib/apt/lists/*

# Másoljuk a buildelt binárist a builder image-ből
COPY --from=builder /usr/src/app/target/release/validation-service /usr/local/bin/validation-service

# A 8081-es portot exponáljuk
EXPOSE 8081

# Indítás
CMD ["validation-service"]