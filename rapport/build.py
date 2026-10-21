"""Construit le rapport : rapport_src.html -> rapport.html (code + captures intégrés) -> PDF (Chromium) + DOCX (pandoc).

Marqueurs disponibles dans la source :
  [[code chemin]]                       fichier complet
  [[method chemin|texte de la 1re ligne]]   extrait d'un bloc (méthode/case) jusqu'à l'accolade fermante
  [[img nom|légende]]                   capture rapport/img/nom.png
"""
import base64
import html
import re
import subprocess
import sys
from pathlib import Path

from pygments import highlight
from pygments.formatters import HtmlFormatter
from pygments.lexers import get_lexer_for_filename, TextLexer

ROOT = Path("/home/user/Ramzi")
HERE = ROOT / "rapport"
fig_counter = [0]


def hl(code, filename):
    try:
        lexer = get_lexer_for_filename(filename)
    except Exception:
        lexer = TextLexer()
    return highlight(code, lexer, HtmlFormatter(nowrap=False, cssclass="hl"))


def code_block(path):
    p = ROOT / path
    code = p.read_text(encoding="utf-8").rstrip()
    return f'<div class="file"><div class="fname">{html.escape(path)}</div>{hl(code, p.name)}</div>'


def method_block(arg):
    path, start = arg.split("|", 1)
    p = ROOT / path.strip()
    lines = p.read_text(encoding="utf-8").split("\n")
    idx = next(i for i, l in enumerate(lines) if start.strip() in l)
    # remonte les commentaires / annotations juste au-dessus
    while idx > 0 and lines[idx - 1].strip().startswith(("/*", "*", "//", "@")):
        idx -= 1
    depth, seen, out = 0, False, []
    for l in lines[idx:]:
        out.append(l)
        depth += l.count("{") - l.count("}")
        if "{" in l:
            seen = True
        if seen and depth <= 0:
            break
    indent = min(len(l) - len(l.lstrip()) for l in out if l.strip())
    code = "\n".join(l[indent:] for l in out)
    return f'<div class="file"><div class="fname">{html.escape(path.strip())} (extrait)</div>{hl(code, p.name)}</div>'


def img_block(arg):
    name, caption = [s.strip() for s in arg.split("|", 1)]
    fig_counter[0] += 1
    data = base64.b64encode((HERE / "img" / f"{name}.png").read_bytes()).decode()
    return (f'<figure><img src="data:image/png;base64,{data}" alt="{html.escape(name)}">'
            f'<figcaption>Figure {fig_counter[0]} : {caption}</figcaption></figure>')


def build():
    src = (HERE / "rapport_src.html").read_text(encoding="utf-8")

    def repl(m):
        kind, arg = m.group(1), m.group(2)
        return {"code": code_block, "method": method_block, "img": img_block}[kind](arg.strip())

    out = re.sub(r"\[\[(code|method|img) (.+?)\]\]", repl, src)
    out = out.replace("/*PYGMENTS*/", HtmlFormatter(style="friendly").get_style_defs(".hl"))
    (HERE / "rapport.html").write_text(out, encoding="utf-8")
    print("rapport.html ok")


def pdf():
    from playwright.sync_api import sync_playwright
    with sync_playwright() as p:
        b = p.chromium.launch(executable_path="/opt/pw-browsers/chromium-1194/chrome-linux/chrome")
        page = b.new_page()
        page.goto((HERE / "rapport.html").as_uri())
        page.wait_for_timeout(500)
        page.pdf(path=str(HERE / "TP3_JDBC_DAO_JPA_Rapport.pdf"), format="A4", print_background=True,
                 margin={"top": "18mm", "bottom": "18mm", "left": "16mm", "right": "16mm"},
                 display_header_footer=True,
                 header_template="<div></div>",
                 footer_template='<div style="font-size:8px;width:100%;text-align:center;color:#888">'
                                 'TP 3 – JDBC / DAO / JPA &amp; Spring / Connection Pool — page '
                                 '<span class="pageNumber"></span> / <span class="totalPages"></span></div>')
        b.close()
    print("pdf ok")


def docx():
    subprocess.run(["pandoc", str(HERE / "rapport.html"), "-o", str(HERE / "TP3_JDBC_DAO_JPA_Rapport.docx")],
                   check=True)
    print("docx ok")


if __name__ == "__main__":
    build()
    if "--no-pdf" not in sys.argv:
        pdf()
        docx()
