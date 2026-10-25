"""Carga datos de ejemplo en el sistema para poder recorrerlo con contenido.

Uso (con el sistema levantado):

    python datos-demo/cargar_datos.py

Solo necesita Python 3, sin paquetes extra. Todo pasa por la API del gateway, igual que si lo hiciera
una persona desde la app, así que respeta las reglas de negocio. Se corre una sola vez sobre una base
nueva: si los datos ya están cargados, avisa y no hace nada.

Para empezar de cero: `docker compose down -v`, `docker compose up -d` y volver a correr este script.
"""
import json
import os
import random
import sys
import time
import urllib.error
import urllib.request
import uuid

sys.stdout.reconfigure(encoding='utf-8')
API = os.environ.get('API_URL', 'http://localhost:8080/api')
FOTOS = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'fotos')

ADMIN = ('admin@concesionaria.com', 'Admin12345')
CLAVE_VENDEDOR = 'Vendedor12345'
CLIENTE_DEMO = ('cliente.demo@mail.com', 'Cliente12345')


# ---------------------------------------------------------------------------------------------------
# Llamadas a la API
# ---------------------------------------------------------------------------------------------------

def req(metodo, ruta, cuerpo=None, token=None):
    """Hace un pedido y devuelve (status, json). No corta en los errores HTTP."""
    pedido = urllib.request.Request(
        API + ruta, method=metodo,
        data=json.dumps(cuerpo).encode() if cuerpo is not None else None,
        headers={'Content-Type': 'application/json', **({'Authorization': 'Bearer ' + token} if token else {})})
    try:
        with urllib.request.urlopen(pedido, timeout=30) as resp:
            texto = resp.read()
            return resp.status, (json.loads(texto) if texto else None)
    except urllib.error.HTTPError as e:
        texto = e.read()
        try:
            return e.code, json.loads(texto) if texto else None
        except ValueError:
            return e.code, texto.decode(errors='replace')


def ok(respuesta, que):
    """Corta el script si el pedido falló."""
    status, cuerpo = respuesta
    if status >= 300:
        raise SystemExit(f'Falló "{que}": {status} {cuerpo}')
    return cuerpo


def login(email, password):
    return ok(req('POST', '/auth/login', {'email': email, 'password': password}), 'login ' + email)['accessToken']


def esperar_sistema():
    """Espera hasta 5 minutos a que el gateway y el auth-service respondan."""
    print(f'Esperando a que el sistema responda en {API}', end='', flush=True)
    for _ in range(100):
        try:
            status, cuerpo = req('POST', '/auth/login', {'email': ADMIN[0], 'password': ADMIN[1]})
            if status == 200:
                print(' listo.')
                return cuerpo['accessToken']
        except (urllib.error.URLError, ConnectionError, TimeoutError):
            pass
        print('.', end='', flush=True)
        time.sleep(3)
    raise SystemExit('\nEl sistema no respondió. ¿Está levantado? Probá con `docker compose ps`.')


def reintentar(funcion, que, intentos=60):
    """Algunos datos se crean por eventos (RabbitMQ) y tardan un momento en aparecer."""
    for _ in range(intentos):
        status, cuerpo = funcion()
        if status == 200:
            return cuerpo
        time.sleep(0.5)
    raise SystemExit(f'No apareció {que}')


def subir_foto(token, vehiculo_id, ruta, principal):
    limite = uuid.uuid4().hex
    with open(ruta, 'rb') as f:
        datos = f.read()
    cuerpo = (f'--{limite}\r\nContent-Disposition: form-data; name="archivo"; filename="{os.path.basename(ruta)}"\r\n'
              'Content-Type: image/jpeg\r\n\r\n').encode() + datos + f'\r\n--{limite}--\r\n'.encode()
    pedido = urllib.request.Request(
        f'{API}/vehiculos/{vehiculo_id}/imagenes?principal={str(principal).lower()}', method='POST', data=cuerpo,
        headers={'Content-Type': f'multipart/form-data; boundary={limite}', 'Authorization': 'Bearer ' + token})
    with urllib.request.urlopen(pedido, timeout=60) as resp:
        return json.loads(resp.read())


def buscar_o_crear(token, tipo, param, datos):
    """Reutiliza la categoría, marca o modelo si ya existe con ese nombre."""
    for e in ok(req('GET', f'/{tipo}?{param}=true', token=token), tipo):
        misma_marca = tipo != 'modelos' or e.get('marcaId', e.get('marca', {}).get('id')) == datos['marcaId']
        if e['nombre'] == datos['nombre'] and misma_marca:
            return e['id']
    return ok(req('POST', f'/{tipo}', datos, token), tipo)['id']


# ---------------------------------------------------------------------------------------------------
# Datos
# ---------------------------------------------------------------------------------------------------

admin = esperar_sistema()

if req('POST', '/auth/login', {'email': 'vera.gomez@concesionaria.com', 'password': CLAVE_VENDEDOR})[0] == 200:
    print('Los datos de ejemplo ya están cargados. Para empezar de cero: docker compose down -v')
    sys.exit(0)

# Vendedores
for email, nombre, apellido in [('vera.gomez@concesionaria.com', 'Vera', 'Gómez'),
                                ('bruno.diaz@concesionaria.com', 'Bruno', 'Díaz')]:
    ok(req('POST', '/usuarios', {'email': email, 'password': CLAVE_VENDEDOR, 'nombre': nombre, 'apellido': apellido,
                                 'rol': 'VENDEDOR'}, admin), 'vendedor ' + email)
vera = login('vera.gomez@concesionaria.com', CLAVE_VENDEDOR)
bruno = login('bruno.diaz@concesionaria.com', CLAVE_VENDEDOR)
print('2 vendedores')

# Catálogo
categorias = {n: buscar_o_crear(admin, 'categorias', 'incluirInactivas', {'nombre': n})
              for n in ['Sedán', 'SUV', 'Pickup', 'Hatchback']}
marcas = {n: buscar_o_crear(admin, 'marcas', 'incluirInactivas', {'nombre': n})
          for n in ['Toyota', 'Volkswagen', 'Ford', 'Peugeot']}
modelos = {}
for marca, nombre, categoria in [
        ('Toyota', 'Corolla', 'Sedán'), ('Toyota', 'Hilux', 'Pickup'), ('Toyota', 'Yaris', 'Hatchback'),
        ('Volkswagen', 'Amarok', 'Pickup'), ('Volkswagen', 'T-Cross', 'SUV'), ('Volkswagen', 'Polo', 'Hatchback'),
        ('Ford', 'Ranger', 'Pickup'), ('Ford', 'Territory', 'SUV'),
        ('Peugeot', '208', 'Hatchback'), ('Peugeot', '2008', 'SUV')]:
    modelos[nombre] = buscar_o_crear(admin, 'modelos', 'incluirInactivos',
                                     {'nombre': nombre, 'marcaId': marcas[marca], 'categoriaId': categorias[categoria]})
print('4 marcas y 10 modelos')

# Vehículos con 3 fotos cada uno (las fotos y sus autores están en fotos/CREDITOS.md).
# La foto principal es la que mejor muestra el auto de frente.
FOTO_PRINCIPAL = {'Yaris': 2, 'Territory': 2, 'Hilux': 2}
VEHICULOS = [
    ('Corolla', 2024, 0, 38_500_000, 'Blanco perla', 'Versión híbrida con caja automática CVT, control de crucero adaptativo y pantalla de 10".'),
    ('Hilux', 2023, 25_000, 52_000_000, 'Gris plata', 'Doble cabina 4x4, motor 2.8 turbodiésel. Service oficial al día, único dueño.'),
    ('Yaris', 2022, 41_000, 21_500_000, 'Rojo', 'Hatchback ideal para la ciudad, muy económico. Cubiertas nuevas.'),
    ('Amarok', 2021, 68_000, 45_000_000, 'Negro', 'V6 Extreme 4Motion, tapizado de cuero, cámara de retroceso y lona marítima.'),
    ('T-Cross', 2025, 0, 36_900_000, 'Azul', 'SUV compacto 0 km, motor 1.0 TSI, techo panorámico y asistente de estacionamiento.'),
    ('Polo', 2020, 72_000, 16_800_000, 'Gris', 'Motor 1.6, aire acondicionado y dirección asistida. Excelente estado general.'),
    ('Ranger', 2024, 12_000, 55_500_000, 'Blanco', 'Wildtrak V6, caja automática de 10 marchas, todavía en garantía de fábrica.'),
    ('Territory', 2025, 0, 41_200_000, 'Gris oscuro', 'SUV 0 km con pantalla de 12", cargador inalámbrico y seis airbags.'),
    ('208', 2023, 18_000, 23_900_000, 'Blanco', 'Versión Allure con i-Cockpit 3D y faros full LED.'),
    ('2008', 2022, 35_000, 28_700_000, 'Negro', 'GT-Line, motor 1.6 THP, techo bitono y llantas de 17".'),
    ('Corolla', 2019, 95_000, 19_500_000, 'Gris', 'Sedán confiable con mantenimiento registrado. Ideal primer auto familiar.'),
    ('Hilux', 2025, 0, 61_000_000, 'Negro', 'SRX 0 km, la versión más completa de la gama, entrega inmediata.'),
    ('Polo', 2025, 0, 24_500_000, 'Rojo', 'Polo Track 0 km, el más económico de la línea para arrancar.'),
    ('Ranger', 2020, 110_000, 33_000_000, 'Azul', 'XLT 4x2, uso particular, papeles al día y listo para transferir.'),
    ('T-Cross', 2023, 30_000, 31_000_000, 'Blanco', 'Highline con techo panorámico, un solo dueño.'),
    ('208', 2025, 0, 26_400_000, 'Gris', 'Active 0 km con Apple CarPlay y Android Auto.'),
]
random.seed(2026)
CARACTERES_VIN = 'ABCDEFGHJKLMNPRSTUVWXYZ0123456789'
autos = []
for modelo, anio, km, precio, color, descripcion in VEHICULOS:
    v = ok(req('POST', '/vehiculos', {
        'vin': ''.join(random.choice(CARACTERES_VIN) for _ in range(17)), 'modeloId': modelos[modelo], 'anio': anio,
        'color': color, 'kilometraje': km, 'precio': precio, 'condicion': 'NUEVO' if km == 0 else 'USADO',
        'descripcion': descripcion}, admin), 'vehículo ' + modelo)
    principal = FOTO_PRINCIPAL.get(modelo, 1)
    for n in [principal] + [n for n in (1, 2, 3) if n != principal]:
        ruta = os.path.join(FOTOS, f'{modelo}-{n}.jpg')
        if os.path.exists(ruta):
            subir_foto(admin, v['id'], ruta, n == principal)
    autos.append(v)
print(f'{len(autos)} vehículos con fotos')
(corolla24, hilux23, yaris, amarok, tcross25, polo20, ranger24, territory,
 p208, p2008, corolla19, hilux25, polo25, ranger20, tcross23, p208_25) = autos

# Planes de financiación
plan_clasico = ok(req('POST', '/planes', {
    'nombre': 'Plan Clásico', 'descripcion': 'Tasa fija en pesos, cuotas mensuales.', 'tasaAnual': 55,
    'plazos': [12, 24, 36], 'porcentajeAnticipoMinimo': 30, 'porcentajeMoraDiaria': 0.1}, admin), 'plan clásico')
plan_0km = ok(req('POST', '/planes', {
    'nombre': 'Plan 0 km', 'descripcion': 'Para unidades nuevas, con menor anticipo.', 'tasaAnual': 45,
    'plazos': [12, 24, 36, 48], 'porcentajeAnticipoMinimo': 20, 'porcentajeMoraDiaria': 0.08}, admin), 'plan 0 km')
print('2 planes de financiación')

# Clientes cargados por los vendedores
direccion = {'calle': 'Av. Corrientes', 'numero': '1234', 'ciudad': 'CABA', 'provincia': 'Ciudad Autónoma de Buenos Aires'}
CLIENTES = [('Lucía', 'Ferreyra', '30111222', 'lucia.ferreyra@mail.com'), ('Martín', 'Acosta', '28333444', 'martin.acosta@mail.com'),
            ('Sofía', 'Gómez', '35100200', 'sofia.gomez@mail.com'), ('Juan', 'Rodríguez', '33222111', None),
            ('Valentina', 'López', '40123456', 'valen.lopez@mail.com'), ('Mateo', 'Díaz', '38999000', None),
            ('Camila', 'Martínez', '36555777', 'camila.martinez@mail.com'), ('Tomás', 'Pérez', '31444888', None),
            ('Julieta', 'Sánchez', '39876543', 'juli.sanchez@mail.com'), ('Franco', 'Romero', '27666333', 'franco.romero@mail.com')]
clientes = []
for i, (nombre, apellido, dni, email) in enumerate(CLIENTES):
    clientes.append(ok(req('POST', '/clientes', {
        'nombre': nombre, 'apellido': apellido, 'tipoDocumento': 'DNI', 'numeroDocumento': dni,
        'email': email, 'telefono': f'11 5555-{i:04d}', 'direccion': direccion}, vera if i % 2 == 0 else bruno),
        'cliente ' + nombre))
lucia, martin, sofia, juan, valen, mateo, camila, tomas, julieta, franco = clientes

# Cliente que se registra solo desde el sitio, con el perfil completo
ok(req('POST', '/auth/registro', {'email': CLIENTE_DEMO[0], 'password': CLIENTE_DEMO[1],
                                  'nombre': 'Pablo', 'apellido': 'Portal'}), 'registro del cliente demo')
portal = login(*CLIENTE_DEMO)
reintentar(lambda: req('GET', '/clientes/me', token=portal), 'el perfil del cliente demo')
cliente_demo = ok(req('PUT', '/clientes/me', {
    'tipoDocumento': 'DNI', 'numeroDocumento': '34567890', 'telefono': '11 4444-5555', 'fechaNacimiento': '1990-05-12',
    'direccion': {'calle': 'Av. Santa Fe', 'numero': '2100', 'pisoDepto': '4B', 'ciudad': 'CABA',
                  'provincia': 'Ciudad Autónoma de Buenos Aires', 'codigoPostal': '1123'}}, portal), 'perfil del cliente demo')
print('11 clientes')


def financiada(vendedor, cliente, auto, plan, cuotas, financiado, cuotas_pagas, cotizacion=None):
    """Venta financiada con el anticipo pagado y las primeras cuotas al día."""
    venta = ok(req('POST', '/ventas', {
        'clienteId': cliente['id'], 'vehiculoId': auto['id'], 'formaPago': 'FINANCIADA', 'cotizacionId': cotizacion,
        'planFinanciamientoId': plan['id'], 'cantidadCuotas': cuotas, 'montoFinanciado': financiado}, vendedor),
        'venta financiada')
    cuenta = reintentar(lambda: req('GET', f"/financiamientos/venta/{venta['id']}", token=vendedor),
                        f"el financiamiento de la venta {venta['id']}")
    pago = {'financiamientoId': cuenta['financiamiento']['id'], 'medioPago': 'TRANSFERENCIA'}
    ok(req('POST', '/pagos', {**pago, 'tipo': 'ANTICIPO', 'monto': cuenta['resumen']['montoAnticipo'],
                              'referencia': f"ANT-{venta['id']}"}, vendedor), 'anticipo')
    for cuota in cuenta['cuotas'][:cuotas_pagas]:
        ok(req('POST', '/pagos', {**pago, 'tipo': 'CUOTA', 'monto': cuota['monto'],
                                  'referencia': f"CUO-{venta['id']}-{cuota['numero']}"}, vendedor), 'cuota')


# Ventas de contado
for vendedor, cliente, auto in [(vera, juan, polo20), (bruno, mateo, corolla19)]:
    ok(req('POST', '/ventas', {'clienteId': cliente['id'], 'vehiculoId': auto['id'], 'formaPago': 'CONTADO'}, vendedor),
       'venta de contado')

# Venta financiada que viene de una cotización con descuento
cotizacion = ok(req('POST', '/cotizaciones', {'clienteId': lucia['id'], 'vehiculoId': yaris['id'], 'descuento': 500_000,
                                             'respuesta': 'Te hacemos precio especial pagando este mes.'}, vera), 'cotización')
ok(req('POST', f"/cotizaciones/{cotizacion['id']}/aceptar", token=vera), 'aceptar cotización')
financiada(vera, lucia, yaris, plan_clasico, 12, 14_000_000, 2, cotizacion['id'])

# Más ventas financiadas, una de ellas del cliente demo
financiada(bruno, martin, amarok, plan_clasico, 24, 30_000_000, 1)
financiada(vera, sofia, p2008, plan_clasico, 36, 20_000_000, 0)
financiada(vera, cliente_demo, polo25, plan_0km, 24, 17_000_000, 3)
print('6 ventas (2 de contado, 4 financiadas)')

# Reservas con seña
ok(req('POST', '/reservas', {'clienteId': valen['id'], 'vehiculoId': hilux23['id'], 'montoAnticipo': 3_000_000}, bruno), 'reserva')
ok(req('POST', '/reservas', {'clienteId': camila['id'], 'vehiculoId': territory['id'], 'montoAnticipo': 2_500_000}, vera), 'reserva')
print('2 reservas')

# Cotizaciones abiertas: dos del panel y dos pedidas por el cliente demo (una ya respondida)
ok(req('POST', '/cotizaciones', {'clienteId': tomas['id'], 'vehiculoId': ranger24['id'], 'descuento': 1_000_000,
                                 'respuesta': 'Con el descuento por pago contado.'}, bruno), 'cotización')
ok(req('POST', '/cotizaciones', {'clienteId': julieta['id'], 'vehiculoId': p208['id'], 'respuesta': 'Precio de lista.'}, vera),
   'cotización')
ok(req('POST', '/cotizaciones/solicitudes', {'vehiculoId': tcross25['id'], 'mensaje': '¿Tienen financiación en 36 cuotas?'},
       portal), 'pedido de cotización')
pedido_hilux = ok(req('POST', '/cotizaciones/solicitudes', {
    'vehiculoId': hilux25['id'], 'mensaje': '¿Toman mi auto usado como parte de pago?'}, portal), 'pedido de cotización')
ok(req('PUT', f"/cotizaciones/{pedido_hilux['id']}/respuesta", {
    'descuento': 2_000_000, 'diasValidez': 10,
    'respuesta': 'Sí, tomamos tu usado. Traelo al local y lo tasamos en el momento. Te dejamos un descuento especial.'},
    vera), 'respuesta a la cotización')
print('4 cotizaciones abiertas')

print(f"""
Listo. Usuarios para probar:
  Admin      {ADMIN[0]} / {ADMIN[1]}
  Vendedor   vera.gomez@concesionaria.com / {CLAVE_VENDEDOR}
  Vendedor   bruno.diaz@concesionaria.com / {CLAVE_VENDEDOR}
  Cliente    {CLIENTE_DEMO[0]} / {CLIENTE_DEMO[1]}
""")
