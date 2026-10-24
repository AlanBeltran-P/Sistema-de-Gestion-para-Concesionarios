-- Una base y un usuario por microservicio. Cada usuario solo tiene acceso a su base.
-- Las credenciales coinciden con config-repo/<servicio>.yml.
-- Las tablas las crea Hibernate al arrancar cada servicio (ddl-auto: update).

CREATE DATABASE IF NOT EXISTS db_auth           CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_vehiculos      CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_clientes       CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_ventas         CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_financiamiento CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_notificaciones CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_reportes       CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'auth_user'@'%'           IDENTIFIED BY 'cambiar_auth';
CREATE USER IF NOT EXISTS 'vehiculo_user'@'%'       IDENTIFIED BY 'cambiar_vehiculo';
CREATE USER IF NOT EXISTS 'cliente_user'@'%'        IDENTIFIED BY 'cambiar_cliente';
CREATE USER IF NOT EXISTS 'venta_user'@'%'          IDENTIFIED BY 'cambiar_venta';
CREATE USER IF NOT EXISTS 'financiamiento_user'@'%' IDENTIFIED BY 'cambiar_financiamiento';
CREATE USER IF NOT EXISTS 'notificacion_user'@'%'   IDENTIFIED BY 'cambiar_notificacion';
CREATE USER IF NOT EXISTS 'reporte_user'@'%'        IDENTIFIED BY 'cambiar_reporte';

GRANT ALL PRIVILEGES ON db_auth.*           TO 'auth_user'@'%';
GRANT ALL PRIVILEGES ON db_vehiculos.*      TO 'vehiculo_user'@'%';
GRANT ALL PRIVILEGES ON db_clientes.*       TO 'cliente_user'@'%';
GRANT ALL PRIVILEGES ON db_ventas.*         TO 'venta_user'@'%';
GRANT ALL PRIVILEGES ON db_financiamiento.* TO 'financiamiento_user'@'%';
GRANT ALL PRIVILEGES ON db_notificaciones.* TO 'notificacion_user'@'%';
GRANT ALL PRIVILEGES ON db_reportes.*       TO 'reporte_user'@'%';

FLUSH PRIVILEGES;
