from pathlib import Path
import json
import sys
from lxml import etree
from jsonschema import Draft202012Validator, FormatChecker

ROOT = Path(__file__).resolve().parents[1]
XSD = ROOT / "src/main/resources/schemas/canonico/alianza.xsd"
JSON_SCHEMA = ROOT / "src/main/resources/schemas/canonico/reserva.schema.json"
ORIGEN_XSD = ROOT / "src/main/resources/schemas/origen/aeronorte-reserva.xsd"


def validar_json(path, schema):
    data = json.loads(path.read_text(encoding="utf-8"))
    errors = sorted(Draft202012Validator(schema, format_checker=FormatChecker()).iter_errors(data), key=lambda e: list(e.path))
    if errors:
        raise ValueError("; ".join(f"{list(e.path)}: {e.message}" for e in errors))


def validar_xml(path, schema_path):
    schema_doc = etree.parse(str(schema_path))
    schema = etree.XMLSchema(schema_doc)
    document = etree.parse(str(path))
    schema.assertValid(document)


def main():
    canonical_xsd = etree.XMLSchema(etree.parse(str(XSD)))
    source_xsd = etree.XMLSchema(etree.parse(str(ORIGEN_XSD)))
    canonical_json_schema = json.loads(JSON_SCHEMA.read_text(encoding="utf-8"))
    surjet_schema = json.loads((ROOT / "src/main/resources/schemas/origen/surjet.schema.json").read_text(encoding="utf-8"))
    caribe_schema = json.loads((ROOT / "src/main/resources/schemas/origen/caribeair.schema.json").read_text(encoding="utf-8"))
    total = 0

    validar_xml(ROOT / "ejemplos/origen/aeronorte.xml", ORIGEN_XSD)
    print("OK XML de origen: AeroNorte")
    total += 1

    validar_json(ROOT / "ejemplos/origen/surjet.json", surjet_schema)
    print("OK JSON de origen: SurJet")
    total += 1

    validar_json(ROOT / "ejemplos/origen/caribeair.json", caribe_schema)
    print("OK JSON de origen: CaribeAir")
    total += 1

    for airline in ("aeronorte", "surjet", "caribeair"):
        json_path = ROOT / f"ejemplos/canonicos/{airline}-canonico.json"
        xml_path = ROOT / f"ejemplos/canonicos/{airline}-canonico.xml"
        validar_json(json_path, canonical_json_schema)
        print(f"OK JSON canónico: {airline}")
        total += 1
        document = etree.parse(str(xml_path))
        canonical_xsd.assertValid(document)
        print(f"OK XML canónico: {airline}")
        total += 1

    print(f"\nValidaciones aprobadas: {total}")


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        print(f"ERROR: {error}", file=sys.stderr)
        sys.exit(1)
