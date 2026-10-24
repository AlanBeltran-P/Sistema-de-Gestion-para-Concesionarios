# syntax=docker/dockerfile:1
# Un solo Dockerfile para todos los servicios. El módulo se elige con el argumento MODULO:
#   docker build --build-arg MODULO=auth-service -t concesionaria/auth-service .
# docker compose ya lo hace para cada servicio.

# ---- 1. Compilar todo el proyecto (JDK + Maven) ----
# Esta etapa es igual para todos los servicios: Docker la ejecuta una sola vez y la reutiliza.
FROM eclipse-temurin:25-jdk AS build
WORKDIR /src
COPY . .
# Las dependencias de Maven quedan en una caché: en los próximos builds no se vuelven a bajar
RUN --mount=type=cache,target=/root/.m2 \
    sed -i 's/\r$//' mvnw && chmod +x mvnw && \
    ./mvnw -q -B package -DskipTests

# ---- 2. Imagen final: solo el JRE y el jar del servicio ----
FROM eclipse-temurin:25-jre
ARG MODULO
ENV TZ=America/Argentina/Buenos_Aires \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC"
WORKDIR /app
# Usuario sin privilegios; uploads/ es donde vehiculo-service guarda las fotos
RUN groupadd --system app && useradd --system --gid app --home /app app \
    && mkdir -p /app/uploads && chown -R app:app /app
COPY --from=build --chown=app:app /src/${MODULO}/target/${MODULO}-0.0.1-SNAPSHOT.jar app.jar
USER app
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
