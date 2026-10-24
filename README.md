# Sistema de Gestión para Concesionarios

Backend de una concesionaria de autos hecho con microservicios: inventario y catálogo público,
clientes, cotizaciones, reservas, ventas (contado, financiadas y con trade-in), financiamiento
con cuotas y mora, notificaciones por email y reportes.

Los requerimientos funcionales (RF-01 a RF-42) están en
[`docs/requerimientos-funcionales.txt`](docs/requerimientos-funcionales.txt).

## Tecnologías

- Java 25, Spring Boot 4.1, Spring Cloud 2025.1 (Config Server, Eureka, Gateway, OpenFeign)
- Spring Security con JWT (HS256), Spring Data JPA (Hibernate 7) y MySQL 8.4
- RabbitMQ para los eventos entre servicios
- Swagger UI (springdoc) para la documentación de la API
- Docker y Docker Compose: todo el sistema se levanta con un comando

## Arquitectura

```
                         ┌──────────────┐
  Frontend / Postman ──► │  api-gateway │ :8080  (valida el JWT, CORS, Swagger UI)
                         └──────┬───────┘
                                │ rutea por Eureka
   ┌──────────┬─────────┬──────┴────┬───────────┬───────────────┬──────────────┬──────────┐
   auth      vehiculo   cliente     venta       financiamiento  notificacion   reporte
   :8081     :8082      :8083       :8084       :8085           :8086          :8087
   db_auth   db_vehic.  db_client.  db_ventas   db_financ.      db_notific.    db_reportes
      │                                 │  ▲ Feign (token SISTEMA)               ▲
      └──────── eventos ───────► RabbitMQ (exchange "concesionaria.eventos") ────┘

  config-server :8888 (lee config-repo/)      eureka-server :8761
```

- **Una base de datos por servicio.** Un servicio nunca lee la base de otro.
- **Llamadas sincrónicas (Feign).** Se usan cuando la respuesta hace falta en el momento, por ejemplo
  cuando venta-service marca un vehículo como vendido. Van con un JWT interno de rol `SISTEMA`.
- **Eventos (RabbitMQ).** Para todo lo demás: el cronograma de cuotas, los emails, los reportes y el
  perfil del cliente que se registra. Cada consumidor tiene su cola de errores `.dlq`.
- **`common`.** Librería compartida: validación del JWT, formato de errores
  ([ProblemDetail](https://www.rfc-editor.org/rfc/rfc9457)), publicación de eventos, Feign y Swagger.

| Servicio | Puerto | Qué hace | RF |
|---|---|---|---|
| config-server | 8888 | Configuración centralizada (`config-repo/`) | — |
| eureka-server | 8761 | Registro de servicios | — |
| api-gateway | 8080 | Punto de entrada único | — |
| auth-service | 8081 | Usuarios, login, JWT y recuperación de contraseña | 01–06 |
| vehiculo-service | 8082 | Marcas, modelos, vehículos, imágenes y catálogo | 07–13 |
| cliente-service | 8083 | Clientes, perfil del portal e historial | 14–17 |
| venta-service | 8084 | Cotizaciones, tasaciones, reservas, ventas, comisiones y PDF | 18–27 |
| financiamiento-service | 8085 | Planes, simulador, cuotas, pagos y mora | 28–33 |
| notificacion-service | 8086 | Emails por eventos e historial | 34–38 |
| reporte-service | 8087 | Reportes y dashboard | 39–42 |

## Cómo levantarlo

### Opción A: todo con Docker (recomendada)

Solo hace falta **Docker Desktop**. Si tenés un MySQL instalado en la máquina, frenalo, porque usa
el mismo puerto 3306.

```bash
docker compose up -d --build
```

- **Primera vez:** compila el proyecto adentro de Docker y tarda unos minutos.
- **Después:** con `docker compose up -d` arranca en menos de un minuto.
- **Qué levanta:** MySQL, RabbitMQ, Mailpit, config-server, Eureka, el gateway y los 7
  microservicios, en el orden correcto.
- **Listo:** cuando `docker compose ps` muestra todos como `healthy`.

| Comando | Para qué |
|---|---|
| `docker compose ps` | Ver el estado de cada contenedor |
| `docker compose logs -f venta-service` | Ver los logs de un servicio |
| `docker compose up -d --build venta-service` | Recompilar y reiniciar un servicio después de cambiar código |
| `docker compose restart venta-service` | Reiniciar un servicio (por ejemplo, después de editar `config-repo/`) |
| `docker compose down` | Bajar todo (los datos quedan) |
| `docker compose down -v` | Bajar todo y borrar los datos |

Desde la máquina solo se usa el gateway (http://localhost:8080). Los servicios no exponen sus
puertos: se hablan entre sí por la red interna de Docker.

### Opción B: los servicios desde el IDE o con `java -jar`

Sirve para depurar un servicio. Requiere **JDK 25** y los puertos 8080 a 8087, 8761 y 8888 libres.

**1. Infraestructura en Docker:**

```bash
docker compose up -d mysql rabbitmq mailpit
```

**2. Compilar:**

```bash
./mvnw clean install -DskipTests        # en Windows: mvnw.cmd clean install -DskipTests
```

**3. Arrancar los servicios** (desde la raíz del proyecto, en este orden):

```bash
java -jar config-server/target/config-server-0.0.1-SNAPSHOT.jar
java -jar eureka-server/target/eureka-server-0.0.1-SNAPSHOT.jar
# cuando los dos estén arriba, el resto en cualquier orden (cada uno en su terminal):
java -jar auth-service/target/auth-service-0.0.1-SNAPSHOT.jar
java -jar vehiculo-service/target/vehiculo-service-0.0.1-SNAPSHOT.jar
java -jar cliente-service/target/cliente-service-0.0.1-SNAPSHOT.jar
java -jar venta-service/target/venta-service-0.0.1-SNAPSHOT.jar
java -jar financiamiento-service/target/financiamiento-service-0.0.1-SNAPSHOT.jar
java -jar notificacion-service/target/notificacion-service-0.0.1-SNAPSHOT.jar
java -jar reporte-service/target/reporte-service-0.0.1-SNAPSHOT.jar
java -jar api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar
```

También se puede arrancar un servicio con `./mvnw -pl auth-service spring-boot:run`.

Después de arrancar, el gateway tarda unos segundos en ver cada servicio en Eureka. Mientras tanto
responde 503.

### Probar

Al arrancar por primera vez, auth-service crea un administrador:

| Email | Contraseña |
|---|---|
| `admin@concesionaria.com` | `Admin12345` |

Se puede cambiar con las variables `ADMIN_EMAIL` y `ADMIN_PASSWORD`.

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"admin@concesionaria.com","password":"Admin12345"}'

# Usar el accessToken de la respuesta
curl http://localhost:8080/api/usuarios -H "Authorization: Bearer <accessToken>"
```

## Direcciones útiles

| Qué | URL |
|---|---|
| **Swagger UI (todos los servicios)** | http://localhost:8080/swagger-ui.html |
| Eureka (servicios registrados) | http://localhost:8761 |
| RabbitMQ (colas y mensajes) | http://localhost:15672 (guest / guest) |
| Mailpit (emails enviados) | http://localhost:8025 |

## Documentación de la API (Swagger)

En http://localhost:8080/swagger-ui.html se elige el servicio en el selector de arriba a la derecha.
Cada endpoint dice qué hace y qué roles pueden usarlo. Los públicos no tienen candado.

Para probar los endpoints que requieren login:
1. Ejecutar `POST /api/auth/login` (en "Autenticación y usuarios").
2. Copiar el `accessToken` de la respuesta.
3. Tocar **Authorize** y pegarlo, sin escribir "Bearer".

Cada servicio también tiene su propio Swagger en `http://localhost:<puerto>/swagger-ui.html`.

## Roles

| Rol | Quién es | Ejemplos de lo que puede hacer |
|---|---|---|
| `ADMIN` | Administrador | Todo: empleados, catálogo, planes, anulaciones, reportes |
| `VENDEDOR` | Empleado de ventas | Clientes, vehículos, cotizaciones, reservas, ventas, pagos |
| `CLIENTE` | Se registra en el portal | Ver el catálogo, pedir cotizaciones, reservar, ver sus compras y cuotas |
| `SISTEMA` | Uso interno | Llamadas entre servicios. Ningún usuario tiene este rol |

El access token dura 15 minutos y el refresh token 7 días (`POST /api/auth/refresh`).

## Flujo de una venta

1. **Cotización.** El cliente pide una desde el portal o el vendedor la crea en el local.
   El vendedor responde con un descuento y, si el cliente entrega su auto, con la tasación (trade-in).
2. **Reserva** (opcional). Se paga una seña y el vehículo queda `RESERVADO`. Si vence sin
   concretarse, se libera solo.
3. **Venta.** Puede ser de contado o financiada, con o sin trade-in:
   - el vehículo pasa a `VENDIDO`;
   - el auto recibido entra al inventario como usado;
   - se calcula la comisión del vendedor.
4. **Financiación** (si es financiada). financiamiento-service genera el cronograma de cuotas.
   Un proceso diario vence las cuotas impagas, calcula la mora y manda recordatorios.
5. **Avisos y reportes.** El cliente recibe un email en cada paso, y reporte-service actualiza los
   reportes.

## Eventos (RabbitMQ)

| Evento | Lo publica | Lo consumen |
|---|---|---|
| `usuario.registrado` | auth | cliente (crea el perfil), notificacion (bienvenida) |
| `usuario.recuperacion-password` | auth | notificacion |
| `cliente.creado`, `cliente.actualizado` | cliente | — |
| `vehiculo.*` | vehiculo | reporte |
| `cotizacion.respondida` | venta | notificacion |
| `reserva.creada`, `reserva.vencida`, `reserva.anulada` | venta | notificacion (creada) |
| `venta.confirmada`, `venta.anulada` | venta | financiamiento, notificacion, reporte |
| `cuota.actualizada`, `cuota.por-vencer`, `cuota.vencida` | financiamiento | reporte, notificacion |

Todos los consumidores ignoran los eventos repetidos. Si un mensaje falla 3 veces, va a la cola
`<cola>.dlq` para revisarlo a mano.

## Configuración

Toda la configuración está en [`config-repo/`](config-repo): `application.yml` es común a todos y
hay un archivo por servicio. Los valores de desarrollo funcionan sin cambiar nada; en producción
se definen estas variables de entorno:

| Variable | Para qué | Valor por defecto (solo desarrollo) |
|---|---|---|
| `JWT_SECRET` | Clave para firmar los JWT (base64, mínimo 32 bytes) | una clave de prueba |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Administrador inicial | `admin@concesionaria.com` / `Admin12345` |
| `CORS_ALLOWED_ORIGINS` | Orígenes del frontend | `http://localhost:4200,http://localhost:5173` |
| `FRONTEND_URL` | Base del link de recuperación de contraseña | `http://localhost:4200` |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` | Servidor SMTP | Mailpit (`localhost:1025`) |
| `MAIL_REMITENTE` | Remitente de los emails | `Concesionaria Demo <no-responder@concesionaria.com>` |
| `IMAGENES_DIR` | Carpeta de las fotos de los vehículos | `uploads/vehiculos` |
| `MYSQL_ROOT_PASSWORD` | Contraseña de root de MySQL (docker compose) | `root` |

Con Docker, estas variables se agregan en el `environment` de cada servicio. Por ejemplo, en un
`docker-compose.override.yml`, que Docker Compose lee solo y conviene no subir al repositorio.

Las contraseñas de los usuarios de cada base (`<servicio>_user` / `cambiar_<servicio>`) se crean en
`docker/mysql/init/` y se configuran en el yml de cada servicio. Hay que cambiarlas en producción.

### Reglas de negocio configurables

Están en `config-repo/venta-service.yml` y `config-repo/financiamiento-service.yml`:

| Regla | Valor |
|---|---|
| Comisión del vendedor | 2,5 % autos nuevos, 4 % usados (sobre el precio final) |
| Descuento máximo de un vendedor | 15 % (el ADMIN no tiene límite) |
| Seña mínima de una reserva | 5 % del precio |
| Vigencia de una reserva | 7 días (máximo 30) |
| Validez de una cotización / tasación | 7 / 15 días |
| Precio de reventa del auto de trade-in | valor tasado + 20 % |
| Aviso de cuota por vencer | 3 días antes |
| Vencimiento de reservas | cada 5 minutos |
| Vencimiento de cuotas y mora | todos los días a la 1:00 |

La tasa, los plazos, el anticipo mínimo y la mora diaria se definen en cada plan de financiamiento.
La mora se calcula así: valor de la cuota × % diario del plan × días de atraso.

Los dos procesos automáticos se pueden correr a mano (ADMIN), indicando la fecha para probar:
`POST /api/reservas/procesar-vencimientos?fecha=...` y `POST /api/cuotas/procesar-vencimientos?fecha=...`.

## Estructura del código

Cada servicio se organiza por entidad. La entidad va en la raíz de su carpeta y adentro están sus
DTOs, enums, servicios, controllers y repositorios:

```
venta-service/src/main/java/com/concesionaria/venta/
├── cotizacion/
│   ├── Cotizacion.java
│   ├── controller/  dto/  enums/  repository/  service/
├── reserva/
├── venta/
├── config/          # seguridad, RabbitMQ, propiedades
├── evento/          # eventos que publica / escucha
└── vehiculo/, cliente/, plan/   # clientes Feign hacia otros servicios
```

## Errores

Todas las respuestas de error tienen el mismo formato:

```json
{ "status": 409, "title": "Conflict", "detail": "El vehículo ya tiene una reserva activa", "instance": "/api/reservas" }
```

Los errores de validación agregan `"errores": { "campo": "mensaje" }`. Si otro servicio interno no
responde, se devuelve 503.
