# Glosario de la alianza Alas del Sur

| Término | Definición canónica |
|---|---|
| Reserva | Registro que vincula una aerolínea, una referencia de origen, un vuelo, un pasajero, una tarifa y el equipaje. |
| Identificador global | UUID generado por el servicio común; no sustituye la referencia operativa de la aerolínea. |
| Referencia de origen | Código que conserva el identificador original de la reserva en el sistema emisor. |
| Aerolínea | Sistema emisor: AERONORTE, SURJET o CARIBEAIR. |
| Vuelo | Información temporal conocida del servicio: fecha, hora local opcional y desfase UTC opcional. |
| Pasajero | Persona que viaja. Se especializa como ADULTO, MENOR o INFANTE. |
| Adulto | Especialización para pasajero adulto según la categoría recibida o la regla de edad documentada. |
| Menor | Especialización para pasajero menor de edad conforme al indicador o código del sistema de origen. |
| Infante | Especialización que se conserva cuando el sistema emisor proporciona explícitamente la categoría INF. |
| Tarifa | Importe de la reserva acompañado siempre de un código de moneda explícito. |
| Equipaje | Información de peso o número de piezas normalizada, conservando la unidad y el valor recibido para trazabilidad. |
| MXN | Peso mexicano, moneda implícita en AeroNorte y explícita por normalización en CaribeAir. |
| USD | Dólar estadounidense, moneda explícita en la muestra SurJet. |
| KG | Kilogramos, unidad canónica de peso. |
| LB | Libras, unidad de origen que se convierte a kilogramos. |
| Pieza | Unidad discreta de equipaje cuando el origen informa cantidad de maletas y no peso. |

## Reglas de interpretación documentadas

1. AeroNorte entrega el nombre como `APELLIDO/NOMBRE`; se reordena a nombre y apellido y se normaliza el uso de mayúsculas. No se inventan acentos que no estén presentes en el origen.
2. CaribeAir entrega el nombre completo sin acentos; para la muestra y para el adaptador se consideran todas las palabras salvo la última como nombre y la última como apellido. Es una regla de separación práctica, no una inferencia de acentos.
3. Las fechas de AeroNorte y CaribeAir no incluyen hora ni zona horaria. El modelo canónico conserva solamente la fecha y deja hora/desfase ausentes. No se inventan horarios.
4. SurJet entrega fecha y hora ISO 8601 con desfase; se separan fecha, hora local y desfase conservando la información.
5. La moneda nunca se convierte. Se hace explícita la MXN implícita y se conserva USD cuando el origen lo indica. No se calcula tipo de cambio porque el caso no proporciona una tasa.
6. El equipaje de SurJet se convierte de libras a kilogramos con `1 lb = 0.45359237 kg`; el resultado se redondea a dos decimales. El valor original y su unidad se conservan.
7. En CaribeAir, `1 pieza` no equivale a un peso medido de 23 kg. Se conserva `numeroPiezas=1` y se registra 23 kg como límite máximo por pieza, no como peso real.
8. Para la especialización derivada de edad en SurJet se adopta el supuesto operativo: menor de 2 años = INFANTE, de 2 a 11 = MENOR, 12 o más = ADULTO. El documento base solo indica edad en años, por lo que este umbral se declara como decisión de diseño y debe confirmarse con la alianza antes de producción.
9. AeroNorte solo informa si el pasajero es menor de 12 años; cuando el indicador es `S` se modela como MENOR y cuando es `N` como ADULTO. Como no distingue infantes, no se puede deducir una categoría INFANTE a partir de ese dato.
10. El identificador global es UUID. La referencia original se conserva junto con la aerolínea, ya que los formatos de origen no son compatibles ni garantizan unicidad entre sistemas.
