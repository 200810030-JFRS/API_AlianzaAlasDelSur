# Guía de pruebas de Postman - Alianza Alas del Sur

## Preparación

1. Inicia la API desde la carpeta donde está `pom.xml` con `mvn spring-boot:run` o el comando de Maven Daemon documentado en el README.
2. Verifica que `http://localhost:8080/api/v1/health` responda con `200 OK`.
3. Abre Postman e importa `postman/Alas_del_Sur.postman_collection.json`.
4. Ejecuta las solicitudes en el orden numérico. La colección guarda los UUID devueltos por las transformaciones para reutilizarlos en las consultas.
5. Para repetir toda la colección, reinicia primero la aplicación: los registros se guardan en memoria y las referencias de origen duplicadas se rechazan.

## Prueba 1. Comprobar el estado de la API

- Método: `GET`
- URL: `http://localhost:8080/api/v1/health`
- Body: ninguno.
- Resultado esperado: `200 OK`.

Respuesta esperada:

```json
{
  "status": "OK",
  "servicio": "Alianza Alas del Sur"
}
```

## Prueba 2. Transformar AeroNorte

- Método: `POST`
- URL: `http://localhost:8080/api/v1/transformaciones/aeronorte`
- Header: `Content-Type: application/xml`
- Body: `raw` con formato XML.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<reserva>
  <pnr>QX7Z2A</pnr>
  <pasajero>GARCIA/ANA</pasajero>
  <fecha>03/04/2026</fecha>
  <tarifa>4500</tarifa>
  <equipaje>23</equipaje>
  <menor>N</menor>
</reserva>
```

- Resultado esperado: `201 Created`.
- Comprobación: `aerolinea=AERONORTE`, `referenciaOrigen=QX7Z2A`, fecha `2026-04-03`, moneda `MXN`, equipaje `23.00 kg` y pasajero `ADULTO`.

## Prueba 3. Transformar SurJet

- Método: `POST`
- URL: `http://localhost:8080/api/v1/transformaciones/surjet`
- Header: `Content-Type: application/json`
- Body: `raw` con formato JSON.

```json
{
  "booking": "SJ-88412",
  "firstName": "Ana",
  "lastName": "García",
  "flightDate": "2026-04-03T08:30:00-06:00",
  "fare": {
    "amount": 260,
    "currency": "USD"
  },
  "bagsLb": 50,
  "age": 34
}
```

- Resultado esperado: `201 Created`.
- Comprobación: hora `08:30:00`, desfase `-06:00`, tarifa `260.00 USD`, peso convertido `22.68 kg` y pasajero adulto de 34 años.

## Prueba 4. Transformar CaribeAir

- Método: `POST`
- URL: `http://localhost:8080/api/v1/transformaciones/caribeair`
- Header: `Content-Type: application/json`
- Body: `raw` con formato JSON.

```json
{
  "id": 99812,
  "nombre": "Ana Garcia",
  "fecha": "04/03/2026",
  "precio": "4,500.00",
  "maleta": "1 pieza",
  "tipoPasajero": "ADT"
}
```

- Resultado esperado: `201 Created`.
- Comprobación: fecha `2026-04-03`, tarifa `4500.00 MXN`, una pieza y categoría `ADULTO`. El máximo de 23 kg por pieza se conserva como límite, no como peso real.

## Prueba 5. Listar todas las reservas transformadas

- Método: `GET`
- URL: `http://localhost:8080/api/v1/reservas`
- Body: ninguno.
- Resultado esperado: `200 OK` y `total=3` si se ejecutaron correctamente las pruebas 2, 3 y 4.

## Prueba 6. Consultar la reserva AeroNorte

- Método: `GET`
- URL: `http://localhost:8080/api/v1/reservas/{idReserva}`
- Sustituye `{idReserva}` por el UUID que devolvió la prueba 2 o usa la variable `{{idReservaAeroNorte}}` de la colección.
- Resultado esperado: `200 OK` y la reserva AeroNorte completa.

## Prueba 7. Consultar la reserva SurJet

- Método: `GET`
- URL: `http://localhost:8080/api/v1/reservas/{idReserva}`
- Sustituye `{idReserva}` por el UUID que devolvió la prueba 3 o usa `{{idReservaSurJet}}`.
- Resultado esperado: `200 OK` y la reserva SurJet completa.

## Prueba 8. Consultar la reserva CaribeAir

- Método: `GET`
- URL: `http://localhost:8080/api/v1/reservas/{idReserva}`
- Sustituye `{idReserva}` por el UUID que devolvió la prueba 4 o usa `{{idReservaCaribeAir}}`.
- Resultado esperado: `200 OK` y la reserva CaribeAir completa.

## Prueba 9. Rechazar una referencia duplicada

Envía nuevamente el mismo XML de AeroNorte de la prueba 2.

- Método: `POST`
- URL: `http://localhost:8080/api/v1/transformaciones/aeronorte`
- Header: `Content-Type: application/xml`
- Resultado esperado: `409 Conflict`.
- Comprobación: el servicio impide almacenar dos veces la combinación de aerolínea y referencia de origen.

## Prueba 10. Rechazar un localizador SurJet inválido

- Método: `POST`
- URL: `http://localhost:8080/api/v1/transformaciones/surjet`
- Header: `Content-Type: application/json`
- Body:

```json
{
  "booking": "SJ-123",
  "firstName": "Ana",
  "lastName": "Garcia",
  "flightDate": "2026-04-03T08:30:00-06:00",
  "fare": {
    "amount": 260,
    "currency": "USD"
  },
  "bagsLb": 50,
  "age": 34
}
```

- Resultado esperado: `400 Bad Request` porque el identificador SurJet debe tener el prefijo `SJ-` seguido de cinco dígitos.

## Evidencias para la entrega

Guarda una captura de Postman en cada prueba donde se vean el método, URL, código HTTP y respuesta. En las pruebas negativas, la respuesta 409 o 400 es el resultado correcto porque demuestra que la regla de validación se aplicó. Para repetir la colección completa desde cero, reinicia la aplicación antes de ejecutarla.
