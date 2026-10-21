"""Rend une capture tmux (avec codes ANSI) en image PNG façon fenêtre de terminal."""
import re
import sys
from PIL import Image, ImageDraw, ImageFont

FONT = "/usr/share/fonts/truetype/dejavu/DejaVuSansMono.ttf"
FONT_B = "/usr/share/fonts/truetype/dejavu/DejaVuSansMono-Bold.ttf"
BG = (30, 30, 30)
FG = (204, 204, 204)
PALETTE = {
    30: (40, 40, 40), 31: (224, 82, 82), 32: (122, 196, 90), 33: (220, 190, 80),
    34: (90, 140, 230), 35: (200, 110, 200), 36: (80, 190, 200), 37: (220, 220, 220),
    90: (128, 128, 128), 91: (255, 110, 110), 92: (150, 230, 120), 93: (255, 230, 110),
    94: (120, 170, 255), 95: (230, 140, 230), 96: (120, 220, 230), 97: (255, 255, 255),
}
SGR = re.compile(r"\x1b\[([0-9;]*)m")
OTHER = re.compile(r"\x1b\[[0-9;?]*[A-Za-z]|\x1b\][^\x07]*\x07|\x1b[()][A-Z0-9]")


def parse(line):
    """Retourne une liste de (texte, couleur, gras)."""
    out, color, bold, pos = [], FG, False, 0
    line = OTHER.sub("", line)
    for m in SGR.finditer(line):
        if m.start() > pos:
            out.append((line[pos:m.start()], color, bold))
        codes = [int(c) for c in m.group(1).split(";") if c] or [0]
        i = 0
        while i < len(codes):
            c = codes[i]
            if c == 0:
                color, bold = FG, False
            elif c == 1:
                bold = True
            elif c == 22:
                bold = False
            elif c == 39:
                color = FG
            elif c in PALETTE:
                color = PALETTE[c]
            elif c == 38 and i + 2 < len(codes) and codes[i + 1] == 5:
                n = codes[i + 2]
                color = PALETTE.get(30 + n if n < 8 else 82 + n, FG)
                i += 2
            i += 1
        pos = m.end()
    if pos < len(line):
        out.append((line[pos:], color, bold))
    return out


def render(text, out_path, title="Terminal", min_cols=80, max_cols=230):
    lines = text.rstrip("\n").split("\n")
    while lines and not OTHER.sub("", SGR.sub("", lines[-1])).strip():
        lines.pop()
    font = ImageFont.truetype(FONT, 14)
    fontb = ImageFont.truetype(FONT_B, 14)
    cw = font.getlength("M")
    lh = 18
    visible = [len(OTHER.sub("", SGR.sub("", l))) for l in lines]
    cols = max(min_cols, min(max_cols, max(visible) if visible else 0))
    pad, bar = 10, 28
    w = int(cols * cw + 2 * pad)
    h = bar + len(lines) * lh + 2 * pad
    img = Image.new("RGB", (w, h), BG)
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, w, bar], fill=(56, 56, 56))
    for i, col in enumerate([(255, 95, 86), (255, 189, 46), (39, 201, 63)]):
        d.ellipse([10 + i * 20, 8, 22 + i * 20, 20], fill=col)
    tf = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 13)
    tw = d.textlength(title, font=tf)
    d.text(((w - tw) / 2, 6), title, fill=(200, 200, 200), font=tf)
    y = bar + pad
    for line in lines:
        x = pad
        col_count = 0
        for txt, color, bold in parse(line):
            if col_count >= cols:
                break
            txt = txt[: cols - col_count]
            d.text((x, y), txt, fill=color, font=fontb if bold else font)
            x += len(txt) * cw
            col_count += len(txt)
        y += lh
    img.save(out_path)


if __name__ == "__main__":
    src, dst = sys.argv[1], sys.argv[2]
    title = sys.argv[3] if len(sys.argv) > 3 else "Terminal"
    with open(src, encoding="utf-8", errors="replace") as f:
        render(f.read(), dst, title)
