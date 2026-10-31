# Alianza Alas del Sur

Servicio de integración con un **modelo canónico de reservas** para las aerolíneas ficticias AeroNorte, SurJet y CaribeAir. El proyecto transforma los mensajes de origen al mismo formato, conserva las referencias originales y valida la estructura mediante XSD y JSON Schema.

## 1. Descripción

Los tres sistemas representan de manera diferente fechas, monedas, nombres, equipaje, tipos de pasajero e identificadores. La API implementa adaptadores para convertir cada muestra al modelo canónico `ReservaCanonica` sin inventar información que no esté presente.

El servicio ofrece endpoints REST que reciben XML de AeroNorte y JSON de SurJet o CaribeAir. Las reservas transformadas quedan disponibles en memoria mientras la aplicación permanece encendida.

## 2. Tecnologías utilizadas

- Java 17.
- Spring Boot 3.4.5.
- Spring Web para exponer la API REST.
- Jackson para interpretar y generar JSON.
- DOM, JAXP y XML Schema para validar y procesar XML.
- XSD 1.0 para validar el XML canónico y el XML de AeroNorte.
- JSON Schema Draft 2020-12 para validar mensajes JSON.
- Maven para gestionar dependencias y ejecutar el proyecto.
- JUnit y Spring Boot Test para pruebas automatizadas.

No requiere una base de datos ni credenciales externas. Las reservas se guardan en memoria y se pierden al detener la aplicación.

## 3. Requisitos

- JDK 17 o superior.
- Maven 3.9 o Maven Daemon compatible.
- Postman o una herramienta equivalente para probar la API.

Comprueba Java y Maven:

```powershell
java -version
mvn -version
```

Si utilizas Maven Daemon desde PowerShell, puedes ejecutar el proyecto con la ruta de tu archivo `mvnd.cmd`:

```powershell
& "$env:USERPROFILE\Downloads\maven-mvnd-2.0.0-rc-3-windows-amd64in\mvnd.cmd" spring-boot:run
```

Ajusta la ruta si descomprimiste Maven Daemon en otra carpeta.

## 4. Instalación y ejecución

1. Descarga y descomprime `Alianza_Alas_del_Sur_Completo.zip`.
2. Abre la carpeta `Alianza_Alas_del_Sur` en Visual Studio Code.
3. Abre Terminal > New Terminal.
4. Comprueba que la terminal está en la carpeta que contiene `pom.xml`.
5. Ejecuta:

```bash
mvn spring-boot:run
```

6. Espera el mensaje de inicio de Spring Boot.
7. Comprueba el servicio en:

```text
http://localhost:8080/api/v1/health
```

Respuesta:

```json
{
  "status": "OK",
  "servicio": "Alianza Alas del Sur"
}
```

Para ejecutar pruebas automatizadas:

```bash
mvn test
```

Para empaquetar el proyecto:

```bash
mvn clean package
```

## 5. Variables de entorno

No se requieren credenciales ni variables obligatorias. La única configuración opcional es el puerto HTTP:

| Variable | Obligatoria | Valor predeterminado | Descripción |
|---|---|---:|---|
| `SERVER_PORT` | No | `8080` | Puerto donde inicia Spring Boot. |

Ejemplo en PowerShell:

```powershell
$env:SERVER_PORT="8090"
mvn spring-boot:run
```

En ese caso, la API queda disponible en `http://localhost:8090`.

No se incluyen contraseñas, tokens ni claves privadas. No agregues archivos `.env` con secretos al repositorio.

## 6. Endpoints

Base URL: `http://localhost:8080/api/v1`

| Método | Endpoint | Descripción | Respuesta habitual |
|---|---|---|---|
| GET | `/health` | Comprueba que la API esté activa. | 200 |
| GET | `/reservas` | Lista las reservas transformadas durante la ejecución. | 200 |
| GET | `/reservas/{id}` | Consulta una reserva mediante UUID global. | 200 / 404 |
| POST | `/transformaciones/aeronorte` | Recibe XML de AeroNorte, valida XSD y devuelve modelo canónico JSON. | 201 / 400 / 409 |
| POST | `/transformaciones/surjet` | Recibe JSON SurJet y lo transforma al modelo canónico. | 201 / 400 / 409 |
| POST | `/transformaciones/caribeair` | Recibe JSON CaribeAir y lo transforma al modelo canónico. | 201 / 400 / 409 |

Las transformaciones exitosas se guardan en memoria. El identificador global se genera como UUID. La pareja `aerolinea` + `referenciaOrigen` debe ser única; enviar dos veces la misma referencia devuelve `409 Conflict`. La colección de Postman `postman/Alas_del_Sur.postman_collection.json` contiene diez solicitudes para probar el flujo en orden. Reinicia la aplicación antes de volver a ejecutar la colección completa, ya que el repositorio es en memoria y rechaza referencias duplicadas.

## 7. Ejemplos de peticiones y respuestas

### 7.1 Transformar AeroNorte

Método: `POST`

URL:

```text
http://localhost:8080/api/v1/transformaciones/aeronorte
```

Header:

```text
Content-Type: application/xml
```

Body:

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

Respuesta representativa `201 Created`:

```json
{
  "idReserva": "UUID-GENERADO-POR-LA-API",
  "aerolinea": "AERONORTE",
  "referenciaOrigen": "QX7Z2A",
  "vuelo": { "fechaVuelo": "2026-04-03" },
  "pasajero": { "tipo": "ADULTO", "nombre": "Ana", "apellido": "Garcia" },
  "tarifa": { "importe": 4500.00, "moneda": "MXN" },
  "equipaje": { "pesoTotalKg": 23.00, "unidadOrigen": "KG", "valorOrigen": "23" }
}
```

El UUID cambia en cada nueva referencia procesada. AeroNorte no entrega hora ni desfase UTC, por lo que no se agregan esos datos.

### 7.2 Transformar SurJet

Método: `POST`

URL:

```text
http://localhost:8080/api/v1/transformaciones/surjet
```

Header: `Content-Type: application/json`

Body:

```json
{
  "booking": "SJ-88412",
  "firstName": "Ana",
  "lastName": "García",
  "flightDate": "2026-04-03T08:30:00-06:00",
  "fare": { "amount": 260, "currency": "USD" },
  "bagsLb": 50,
  "age": 34
}
```

Respuesta representativa `201 Created`:

```json
{
  "idReserva": "UUID-GENERADO-POR-LA-API",
  "aerolinea": "SURJET",
  "referenciaOrigen": "SJ-88412",
  "vuelo": { "fechaVuelo": "2026-04-03", "horaLocal": "08:30:00", "desfaseUtc": "-06:00" },
  "pasajero": { "tipo": "ADULTO", "nombre": "Ana", "apellido": "García", "edad": 34 },
  "tarifa": { "importe": 260.00, "moneda": "USD" },
  "equipaje": { "pesoTotalKg": 22.68, "unidadOrigen": "LB", "valorOrigen": "50" }
}
```

La conversión de peso aplica `50 × 0.45359237 = 22.6796185 kg`, redondeado a `22.68 kg`.

### 7.3 Transformar CaribeAir

Método: `POST`

URL:

```text
http://localhost:8080/api/v1/transformaciones/caribeair
```

Header: `Content-Type: application/json`

Body:

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

Respuesta representativa `201 Created`:

```json
{
  "idReserva": "UUID-GENERADO-POR-LA-API",
  "aerolinea": "CARIBEAIR",
  "referenciaOrigen": "99812",
  "vuelo": { "fechaVuelo": "2026-04-03" },
  "pasajero": { "tipo": "ADULTO", "nombre": "Ana", "apellido": "Garcia" },
  "tarifa": { "importe": 4500.00, "moneda": "MXN" },
  "equipaje": { "numeroPiezas": 1, "unidadOrigen": "PIEZA", "valorOrigen": "1 pieza", "limiteKgPorPieza": 23.00 }
}
```

El límite de 23 kg por pieza es una condición documentada por CaribeAir; no se interpreta como el peso real de la maleta.

### 7.4 Consultar las reservas transformadas

```http
GET http://localhost:8080/api/v1/reservas
```

Respuesta representativa:

```json
{
  "total": 3,
  "reservas": [
    {
      "idReserva": "UUID-GENERADO-POR-LA-API",
      "aerolinea": "AERONORTE",
      "referenciaOrigen": "QX7Z2A"
    }
  ]
}
```

La respuesta real incluye el objeto completo de cada reserva.

### 7.5 Consultar una reserva por UUID

Copia `idReserva` de una respuesta de transformación:

```http
GET http://localhost:8080/api/v1/reservas/{idReserva}
```

Si el UUID no existe, la API devuelve `404 Not Found`.

## 8. Modelo canónico

```text
ReservaCanonica
  idReserva: UUID
  aerolinea: AERONORTE | SURJET | CARIBEAIR
  referenciaOrigen: String
  vuelo: Vuelo
  pasajero: Pasajero
  tarifa: Tarifa
  equipaje: Equipaje

Vuelo
  fechaVuelo: LocalDate
  horaLocal: LocalTime opcional
  desfaseUtc: String opcional

Pasajero (abstracto)
  nombre: String
  apellido: String
  edad: Integer opcional
  Adulto | Menor | Infante

Tarifa
  importe: Decimal
  moneda: código de tres letras

Equipaje
  pesoTotalKg: Decimal opcional
  numeroPiezas: Integer opcional
  unidadOrigen: KG | LB | PIEZA
  valorOrigen: String
  limiteKgPorPieza: Decimal opcional
```

Diagrama UML: `documentacion/modelo-canonico.puml`, `documentacion/modelo-canonico.dot`, `documentacion/modelo-canonico.svg` y `documentacion/modelo-canonico.png`. Informe académico: `documentacion/Informe_Reto_2_Alianza_Alas_del_Sur.pdf`.

## 9. Esquemas y validación

- `src/main/resources/schemas/canonico/alianza.xsd`: XSD canónico. Usa restricciones de patrón, enumeraciones, mínimos y tipos derivados con `xs:extension`.
- `src/main/resources/schemas/canonico/reserva.schema.json`: JSON Schema Draft 2020-12 equivalente al modelo canónico.
- `src/main/resources/schemas/origen/aeronorte-reserva.xsd`: valida el XML original de AeroNorte; la API lo ejecuta antes de transformar.
- `src/main/resources/schemas/origen/surjet.schema.json`: contrato de la muestra SurJet.
- `src/main/resources/schemas/origen/caribeair.schema.json`: contrato de la muestra CaribeAir.

Las seis muestras canónicas están en `ejemplos/canonicos/`: cada aerolínea tiene un XML y un JSON. Las muestras de origen se encuentran en `ejemplos/origen/`.

Para validar los ejemplos con el script incluido:

```bash
python scripts/validar_ejemplos.py
```

El script necesita Python 3, `lxml` y `jsonschema`:

```bash
python -m pip install lxml jsonschema
```

## 10. Colección de Postman

Importa `postman/Alas_del_Sur.postman_collection.json` en Postman. La explicación paso a paso está en `documentacion/Guia_de_pruebas_Postman.md`. La colección usa `baseUrl=http://localhost:8080`, ejecuta primero las tres transformaciones, guarda automáticamente los UUID en variables de colección y después consulta las reservas. Las últimas dos solicitudes prueban el rechazo de una referencia duplicada y de un localizador SurJet inválido. Ejecuta las peticiones en el orden numerado.

## 11. Decisiones y limitaciones

- El identificador global es UUID; se preserva el identificador de origen en `referenciaOrigen` y el nombre de la aerolínea.
- No se convierte USD a MXN ni MXN a USD porque el caso no proporciona una tasa de cambio.
- Los ejemplos AeroNorte y CaribeAir solo proporcionan fecha; no se inventan hora ni zona horaria.
- La conversión de libras a kg usa el factor exacto `0.45359237` y redondea a dos decimales.
- CaribeAir informa número de piezas; el límite de 23 kg por pieza no se usa como peso medido.
- Para SurJet se adopta el supuesto de edad `0–1` infante, `2–11` menor y `12 o más` adulto. Debe confirmarse con la alianza porque el documento solo especifica que SurJet informa la edad.
- En AeroNorte, `menor=S/N` no distingue infantes; por ello no se infiere INFANTE.
- El servicio almacena registros en memoria. Para persistencia real sería necesario integrar una base de datos, fuera del alcance de las muestras.

## 12. Archivos que no deben subirse

El `.gitignore` excluye `target/`, logs, configuraciones locales y archivos `.env`. No agregues contraseñas, tokens, claves privadas ni archivos generados innecesarios.
