"""Regenerate only the committed Fusion models. Does not invoke Gradle or Minecraft."""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/paintingmod'
PALETTE_SOURCE = ROOT / 'src/main/java/net/nicomar2009/paintingmod/paint/Paint.java'
PALETTE = {paint: texture for paint, texture in enumerate(
    re.findall(r'^    [A-Z_0-9]+\((null|"[^"\n]+")\)', PALETTE_SOURCE.read_text(), re.MULTILINE)
) if texture != 'null'}
PALETTE = {paint: json.loads(texture) for paint, texture in PALETTE.items()}
FACES = ('down', 'up', 'north', 'south', 'west', 'east')
BLOCKS = ((1, 'paintable_block'), (2, 'paintable_custom_block'),
          (4, 'paintable_mini_cube_block'))


def write(relative, data):
    target = ASSETS / relative
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(data, separators=(',', ':')) + '\n')


def surface(divisions, cube, face):
    size = 16 // divisions
    x, y, z = (cube % divisions) * size, (cube // divisions % divisions) * size, (cube // divisions**2) * size
    uv = {
        'down': [x, 16-z-size, x+size, 16-z], 'up': [x, z, x+size, z+size],
        'north': [16-x-size, 16-y-size, 16-x, 16-y], 'south': [x, 16-y-size, x+size, 16-y],
        'west': [z, 16-y-size, z+size, 16-y], 'east': [16-z-size, 16-y-size, 16-z, 16-y],
    }[face]
    definition = {'texture': '#surface', 'uv': uv}
    outer = {'down': y == 0, 'up': y+size == 16, 'north': z == 0,
             'south': z+size == 16, 'west': x == 0, 'east': x+size == 16}[face]
    # Fine cells use the neighbor occupancy predicate. Coarse outer faces retain native culling.
    if divisions != 4 and outer:
        definition['cullface'] = face
    return {'from': [x, y, z], 'to': [x+size, y+size, z+size], 'faces': {face: definition}}


def generate():
    for divisions, name in BLOCKS:
        groups = []
        for cube in range(divisions**3):
            for face in FACES:
                shape = f'paintingmod:block/surfaces/{divisions}/{cube}_{face}_0'
                write(f'models/block/surfaces/{divisions}/{cube}_{face}_0.json', {
                    'parent': 'minecraft:block/block', 'render_type': 'minecraft:solid',
                    'textures': {'surface': PALETTE[0], 'particle': '#surface'},
                    'elements': [surface(divisions, cube, face)],
                })
                choices = []
                for paint, texture in PALETTE.items():
                    model = f'paintingmod:block/surfaces/{divisions}/{cube}_{face}_{paint}'
                    if paint:
                        write(f'models/block/surfaces/{divisions}/{cube}_{face}_{paint}.json', {
                            'parent': shape, 'textures': {'surface': texture},
                        })
                    choices.append({'model': model, 'condition': {
                        'type': 'paintingmod:painted_surface', 'divisions': divisions,
                        'cube': cube, 'face': face, 'paint': paint,
                    }})
                groups.append(choices)
        groups.append([{'model': 'paintingmod:block/empty'}])
        write(f'models/block/{name}.json', {
            'loader': 'fusion:model', 'type': 'composite',
            'textures': {'particle': PALETTE[0]}, 'models': groups,
        })


if __name__ == '__main__':
    generate()
