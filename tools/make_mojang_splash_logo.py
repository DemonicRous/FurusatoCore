"""Reassemble the modern Mojang Studios atlas into Forge's square splash texture."""

from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "art" / "mojangstudios_atlas.png"
OUTPUT = ROOT / "src/main/resources/assets/furusatocore/textures/gui/mojang.png"


def build() -> None:
    atlas = Image.open(SOURCE).convert("RGBA")
    word_boxes = [
        (0, 10, 172, 163), (188, 8, 345, 165), (360, 10, 512, 163),
        (18, 266, 171, 419), (190, 266, 343, 419), (359, 266, 512, 419),
    ]
    word_glyphs = [atlas.crop(box) for box in word_boxes]
    word = Image.new("RGBA", (sum(g.width for g in word_glyphs) + 40, 157), (0, 0, 0, 0))
    x = 0
    for glyph in word_glyphs:
        word.alpha_composite(glyph, (x, (157 - glyph.height) // 2))
        x += glyph.width + 8

    wrapped_d = Image.new("RGBA", (46, 53), (0, 0, 0, 0))
    wrapped_d.alpha_composite(atlas.crop((501, 199, 512, 249)), (0, 1))
    wrapped_d.alpha_composite(atlas.crop((0, 455, 35, 505)), (11, 1))
    subtitle_glyphs = [
        atlas.crop((273, 197, 312, 250)), atlas.crop((344, 199, 388, 249)),
        atlas.crop((421, 199, 466, 250)), wrapped_d, atlas.crop((70, 455, 86, 505)),
        atlas.crop((120, 453, 173, 506)), atlas.crop((202, 453, 242, 506)),
    ]
    studios = Image.new("RGBA", (sum(g.width for g in subtitle_glyphs) + 42, 53), (0, 0, 0, 0))
    x = 0
    for glyph in subtitle_glyphs:
        studios.alpha_composite(glyph, (x, (53 - glyph.height) // 2))
        x += glyph.width + 7

    word.thumbnail((420, 82), Image.Resampling.LANCZOS)
    studios.thumbnail((190, 28), Image.Resampling.LANCZOS)
    output = Image.new("RGBA", (512, 512), (0, 0, 0, 0))
    output.alpha_composite(word, ((512 - word.width) // 2, 204))
    output.alpha_composite(studios, ((512 - studios.width) // 2, 294))
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    output.save(OUTPUT, optimize=True)


if __name__ == "__main__":
    build()
