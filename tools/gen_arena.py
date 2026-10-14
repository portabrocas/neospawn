"""Writes the GameTest arena structures.

  python3 tools/gen_arena.py <out.nbt>         land arena: 24x8x24, stone floor, small water pool
  python3 tools/gen_arena.py <out.nbt> sea     sea arena: 32x20x32 stone tank full of water
  python3 tools/gen_arena.py <out.nbt> tall    tall arena: 24x24x24 stone floor, open air (for trees and structures)
"""
import gzip
import struct
import sys

def tag(t, name, payload):
    n = name.encode()
    return bytes([t]) + struct.pack(">H", len(n)) + n + payload

def string(s):
    b = s.encode()
    return struct.pack(">H", len(b)) + b

def compound(items):
    return b"".join(items) + b"\x00"

def int_list(values):
    return bytes([3]) + struct.pack(">i", len(values)) + b"".join(struct.pack(">i", v) for v in values)

def compound_list(compounds):
    return bytes([10]) + struct.pack(">i", len(compounds)) + b"".join(compounds)

MODE = sys.argv[2] if len(sys.argv) > 2 else "land"
SEA = MODE == "sea"
SIZE, HEIGHT = {"sea": (32, 20), "tall": (24, 24)}.get(MODE, (24, 8))
palette = [
    compound([tag(8, "Name", string("minecraft:stone"))]),
    compound([tag(8, "Name", string("minecraft:water")),
              tag(10, "Properties", compound([tag(8, "level", string("0"))]))]),
]
blocks = []

def put(x, y, z, state):
    blocks.append(compound([tag(9, "pos", int_list([x, y, z])), tag(3, "state", struct.pack(">i", state))]))

for x in range(SIZE):
    for z in range(SIZE):
        put(x, 0, z, 0)
if SEA:
    for x in range(SIZE):
        for z in range(SIZE):
            wall = x in (0, SIZE - 1) or z in (0, SIZE - 1)
            for y in range(1, HEIGHT - 1):
                put(x, y, z, 0 if wall else 1)
elif MODE == "land":
    # A 6x3x6 water pool in one corner, walled by the floor.
    for x in range(16, 22):
        for z in range(16, 22):
            for y in range(1, 4):
                put(x, y, z, 1)

root = tag(10, "", compound([
    tag(3, "DataVersion", struct.pack(">i", 3955)),
    tag(9, "size", int_list([SIZE, HEIGHT, SIZE])),
    tag(9, "palette", compound_list(palette)),
    tag(9, "blocks", compound_list(blocks)),
    tag(9, "entities", bytes([0]) + struct.pack(">i", 0)),
]))
with gzip.open(sys.argv[1], "wb") as f:
    f.write(root)
