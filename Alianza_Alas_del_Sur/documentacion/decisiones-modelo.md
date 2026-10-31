# Modelo canónico y decisiones de diseño

## Estructura de ReservaCanonica

- `idReserva`: UUID generado por el servicio común. Es la identidad global, estable e independiente del sistema emisor.
- `aerolinea`: enum con AERONORTE, SURJET o CARIBEAIR.
- `referenciaOrigen`: identificador original conservado como texto para no perder ceros, prefijos o formato.
- `vuelo`: `fechaVuelo` obligatoria, `horaLocal` opcional y `desfaseUtc` opcional.
- `pasajero`: especialización ADULTO, MENOR o INFANTE mediante discriminador `tipo`; los atributos comunes son nombre, apellido y edad opcional.
- `tarifa`: importe decimal y moneda explícita.
- `equipaje`: peso en kg o cantidad de piezas, con unidad y valor de origen para rastrear la conversión. El límite de 23 kg por pieza se conserva como límite informado por CaribeAir.

## Identificador global

Se utiliza UUID para `idReserva`. Los tres localizadores originales tienen formatos incompatibles: seis caracteres alfanuméricos, `SJ-` más cinco dígitos y un entero consecutivo. No es correcto tratar esos valores como una misma clave. El UUID evita colisiones entre aerolíneas y `referenciaOrigen` mantiene el vínculo con el sistema fuente. La combinación `(aerolinea, referenciaOrigen)` se trata como única para impedir procesar dos veces la misma referencia.

## Especialización de pasajero

El modelo usa herencia: `Pasajero` como clase base y `Adulto`, `Menor`, `Infante` como subclases. El JSON la representa con `tipo`. El XSD la representa mediante tipos derivados que usan `xs:extension` sobre `PasajeroBaseType`.

## Normalización sin pérdida semántica

- Las fechas se emiten en ISO `YYYY-MM-DD`.
- Las horas y desfases se conservan solamente cuando existen en el origen.
- Las tarifas son números decimales con moneda explícita; no se aplica tipo de cambio.
- El peso se expresa en kg. El valor y unidad de origen se conservan como metadatos de equipaje.
- Si el origen solamente informa piezas, se registra el número de piezas y no se calcula un peso real.
