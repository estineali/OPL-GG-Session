#!/usr/bin/env python3
"""Generates the 16x16 item textures for the red sword, blue axe and green hoe.

Each sprite is drawn as a grid of characters below; a dark outline is added automatically around every filled
pixel. Uses only the Python standard library (no Pillow needed).

Usage, from the project root:
    python3 scripts/gen_tool_textures.py
"""
import struct
import zlib
from pathlib import Path

OUT_DIR = Path(__file__).resolve().parent.parent / "src/main/resources/assets/opl_gg_msa/textures/item"

# Shape legend: L/M/D = light/mid/dark tool colour, H/h = handle light/dark, G/g = guard light/dark, '.' = empty.
SWORD = [
    "................",
    ".............LM.",
    "............LMD.",
    "...........LMD..",
    "..........LMD...",
    ".........LMD....",
    "........LMD.....",
    ".......LMD......",
    "......LMD.......",
    "..g..LMD........",
    "..gGLMD.........",
    "...Gg...........",
    "...HGg..........",
    "..Hh.Gg.........",
    "GHh.............",
    "GG..............",
]

AXE = [
    "................",
    "........LMMMDMD.",
    ".......LMMMMDHh.",
    "......LMMMMDHh..",
    "......LMMMDHh...",
    "......LMMDHh....",
    ".......LDHh.....",
    "........Hh......",
    ".......Hh.......",
    "......Hh........",
    ".....Hh.........",
    "....Hh..........",
    "...Hh...........",
    "..Hh............",
    ".Hh.............",
    "................",
]

HOE = [
    "................",
    "........LLMMMMD.",
    "........LMDDDDD.",
    "........LM..Hh..",
    "........LD.Hh...",
    "..........Hh....",
    ".........Hh.....",
    "........Hh......",
    ".......Hh.......",
    "......Hh........",
    ".....Hh.........",
    "....Hh..........",
    "...Hh...........",
    "..Hh............",
    ".Hh.............",
    "................",
]

HANDLE = {"H": "8b5a2b", "h": "5c3a1a"}

PALETTES = {
    "red": {"L": "ff8a8a", "M": "e02020", "D": "8c0f0f", "G": "c22a2a", "g": "7a1010", "outline": "2b0303",
            "H": "4a2a2a", "h": "2e1717"},
    "blue": {"L": "9ad6ff", "M": "2f7fe6", "D": "173f99", "outline": "06132e", **HANDLE},
    "green": {"L": "a6f5a3", "M": "2fbf3f", "D": "157528", "outline": "052a0b", **HANDLE},
}

ITEMS = {
    "red_sword": (SWORD, "red"),
    "blue_axe": (AXE, "blue"),
    "green_hoe": (HOE, "green"),
}


def rgba(hex_colour):
    return tuple(int(hex_colour[i:i + 2], 16) for i in (0, 2, 4)) + (255,)


def render(shape, palette):
    size = len(shape)
    assert all(len(row) == size for row in shape), "sprite rows must all be the same length"
    pixels = [[(0, 0, 0, 0)] * size for _ in range(size)]
    for y, row in enumerate(shape):
        for x, ch in enumerate(row):
            if ch != ".":
                pixels[y][x] = rgba(palette[ch])
    outline = rgba(palette["outline"])
    for y, row in enumerate(shape):
        for x, ch in enumerate(row):
            if ch != ".":
                continue
            neighbours = ((x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1))
            if any(0 <= nx < size and 0 <= ny < size and shape[ny][nx] != "." for nx, ny in neighbours):
                pixels[y][x] = outline
    return pixels


def write_png(path, pixels):
    height, width = len(pixels), len(pixels[0])
    raw = b"".join(b"\x00" + b"".join(bytes(p) for p in row) for row in pixels)

    def chunk(kind, data):
        return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data) & 0xFFFFFFFF)

    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(raw, 9))
    png += chunk(b"IEND", b"")
    path.write_bytes(png)


def main():
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    for name, (shape, palette_name) in ITEMS.items():
        path = OUT_DIR / f"{name}.png"
        write_png(path, render(shape, PALETTES[palette_name]))
        print(f"wrote {path}")


if __name__ == "__main__":
    main()
