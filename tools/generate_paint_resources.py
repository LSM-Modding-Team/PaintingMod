"""Generate palette and paint-item resources from the catalog and an official client JAR.
Does not invoke Gradle, tests, data generators or Minecraft.
"""
import argparse
import json
from pathlib import Path
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[1]
RESOURCES = ROOT / 'src/main/resources'
ASSETS = RESOURCES / 'assets/paintingmod'


def write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, separators=(',', ':')) + '\n')


def copied_preview(definition):
    if definition['model'].get('type') != 'paintingmod:copied_pattern':
        definition['model'] = {'type': 'paintingmod:copied_pattern', 'fallback': definition['model']}
    return definition


def generate(client_jar):
    catalog = json.loads((ROOT / 'catalog/vanilla-paints-26.2.json').read_text())
    entries = catalog['entries']
    write(RESOURCES / 'data/paintingmod/paint_catalog.json', [
        {'id': row['paint_id'], 'block': row['block_id']} for row in entries
    ])
    languages = {locale: json.loads((ASSETS / f'lang/{locale}.json').read_text()) for locale in ('en_us', 'es_es')}
    with ZipFile(client_jar) as vanilla:
        files = set(vanilla.namelist())
        for row in entries:
            name = row['block_id'].split(':', 1)[1]
            item = name + '_paint'
            source = f'assets/minecraft/items/{name}.json'
            definition = json.loads(vanilla.read(source)) if source in files else {
                'model': {'type': 'minecraft:model', 'model': row['source_models'][0]}
            }
            write(ASSETS / f'items/{item}.json', copied_preview(definition))
            languages['en_us'][f'item.paintingmod.{item}'] = 'Paint: ' + row['name_en']
            languages['es_es'][f'item.paintingmod.{item}'] = 'Pintura: ' + row['name_es']
    checker = ASSETS / 'items/default_checker_paint.json'
    write(checker, copied_preview(json.loads(checker.read_text())))
    for locale, data in languages.items():
        (ASSETS / f'lang/{locale}.json').write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('client_jar', type=Path)
    generate(parser.parse_args().client_jar)
