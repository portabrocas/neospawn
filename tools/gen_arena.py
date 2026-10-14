"""Writes the GameTest arena structure: a 24x8x24 box with a stone floor and a water pool."""
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

SIZE = 24
palette = [
    compound([tag(8, "Name", string("minecraft:stone"))]),
    compound([tag(8, "Name", string("minecraft:water")),
              tag(10, "Properties", compound([tag(8, "level", string("0"))]))]),
]
blocks = []
for x in range(SIZE):
    for z in range(SIZE):
        blocks.append(compound([tag(9, "pos", int_list([x, 0, z])), tag(3, "state", struct.pack(">i", 0))]))
# A 6x3x6 water pool in one corner, walled by the floor.
for x in range(16, 22):
    for z in range(16, 22):
        for y in range(1, 4):
            blocks.append(compound([tag(9, "pos", int_list([x, y, z])), tag(3, "state", struct.pack(">i", 1))]))

root = tag(10, "", compound([
    tag(3, "DataVersion", struct.pack(">i", 3955)),
    tag(9, "size", int_list([SIZE, 8, SIZE])),
    tag(9, "palette", compound_list(palette)),
    tag(9, "blocks", compound_list(blocks)),
    tag(9, "entities", bytes([0]) + struct.pack(">i", 0)),
]))
with gzip.open(sys.argv[1], "wb") as f:
    f.write(root)
