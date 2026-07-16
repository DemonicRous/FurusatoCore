"""Build the Forge splash animation strip from the Furusato source emblem."""

from math import pi, sin
from pathlib import Path

from PIL import Image, ImageEnhance, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "art" / "loading_emblem_source.png"
OUTPUT = ROOT / "src/main/resources/assets/furusatocore/textures/gui/loading_animation.png"
FRAME_SIZE = 128
FRAME_COUNT = 16
BACKGROUND = (2, 5, 20)


def build() -> None:
    source = Image.open(SOURCE).convert("RGB")
    strip = Image.new("RGB", (FRAME_SIZE, FRAME_SIZE * FRAME_COUNT), BACKGROUND)

    for index in range(FRAME_COUNT):
        phase = index * 2.0 * pi / FRAME_COUNT
        scale = 0.82 + 0.025 * sin(phase)
        size = int(FRAME_SIZE * scale)
        emblem = source.resize((size, size), Image.Resampling.NEAREST)
        emblem = ImageEnhance.Brightness(emblem).enhance(1.0 + 0.06 * sin(phase - pi / 2.0))

        frame = Image.new("RGB", (FRAME_SIZE, FRAME_SIZE), BACKGROUND)
        x = (FRAME_SIZE - size) // 2
        y = (FRAME_SIZE - size) // 2 + int(2 * sin(phase))
        frame.paste(emblem, (x, y))

        draw = ImageDraw.Draw(frame)
        for spark, (sx, sy) in enumerate(((27, 49), (102, 42), (18, 76), (109, 72))):
            intensity = max(0.0, sin(phase + spark * pi / 2.0))
            if intensity > 0.35:
                color = (255, int(145 + 80 * intensity), int(50 + 60 * intensity))
                draw.rectangle((sx, sy, sx + 1, sy + 1), fill=color)

        strip.paste(frame, (0, index * FRAME_SIZE))

    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    strip.save(OUTPUT, optimize=True)


if __name__ == "__main__":
    build()
