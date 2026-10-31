# Conflictos semánticos identificados

| Dato | AeroNorte | SurJet | CaribeAir | Problema y resolución canónica |
|---|---|---|---|---|
| Fecha | DD/MM/AAAA, hora local | ISO 8601 con zona horaria | MM/DD/AAAA, hora local | Orden y precisión diferentes. Usar `fechaVuelo` en ISO 8601 `AAAA-MM-DD`; `horaLocal` y `desfaseUtc` son opcionales cuando el origen los entrega. |
| Moneda | MXN implícito | Importe y código de moneda | MXN como texto con separador de miles | El valor numérico no es comparable sin moneda. Usar `importe` decimal y `moneda` ISO de tres letras; nunca inferir una conversión. |
| Nombre | APELLIDO/NOMBRE en mayúsculas y sin acentos | Nombre y apellido separados, con acentos | Nombre completo sin acentos | Orden, capitalización y acentuación. Usar `nombre` y `apellido`; ajustar capitalización y conservar los acentos disponibles, sin inventarlos. |
| Equipaje | Peso en kilogramos | Peso en libras | Número de piezas, máximo 23 kg cada una | Unidades y significado distintos. Usar kg como peso canónico, convertir lb, o conservar piezas sin atribuir un peso real. Guardar unidad/valor de origen. |
| Tipo de pasajero | Indicador menor S/N (menor de 12) | Edad en años | Códigos ADT/CHD/INF | Representaciones no equivalentes. Usar especialización ADULTO/MENOR/INFANTE y documentar el supuesto de edad; AeroNorte no distingue infante. |
| Identificador | Localizador de 6 caracteres | Prefijo SJ- y 5 dígitos | Entero consecutivo | Formatos y ámbitos de unicidad diferentes. Asignar UUID global y conservar `aerolinea` + `referenciaOrigen`. |
| Tarifa | Número sin moneda declarada | Objeto amount/currency | Cadena con coma de miles y decimales | Parsear a decimal, explicitar moneda y preservar moneda original; sin tasa de cambio no se convierten importes. |
| Hora del vuelo | No viene hora exacta | Hora y desfase incluidos | No viene hora exacta | No se puede completar la hora de todas las reservas. Campos temporales precisos opcionales. |
