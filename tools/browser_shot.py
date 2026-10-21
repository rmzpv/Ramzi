"""Capture d'une URL dans une fausse fenêtre de navigateur (barre d'adresse) via Playwright/Chromium."""
import html
import sys
from playwright.sync_api import sync_playwright

url, out = sys.argv[1], sys.argv[2]
w = int(sys.argv[3]) if len(sys.argv) > 3 else 900
h = int(sys.argv[4]) if len(sys.argv) > 4 else 400
full = len(sys.argv) > 5 and sys.argv[5] == "full"

with sync_playwright() as p:
    b = p.chromium.launch(executable_path="/opt/pw-browsers/chromium-1194/chrome-linux/chrome",
                          args=["--no-proxy-server"])
    page = b.new_page(viewport={"width": w, "height": h})
    page.goto(url)
    page.wait_for_timeout(2500)
    if "--pretty" in sys.argv:
        try:
            page.check("input[type=checkbox]", timeout=2000)
        except Exception:
            pass
        page.wait_for_timeout(300)
    content = page.screenshot(full_page=full)
    import base64
    data = base64.b64encode(content).decode()
    shown = html.escape(url.replace("http://", ""))
    wrapper = f"""<html><body style="margin:0;font-family:Arial,sans-serif;background:#dee1e6">
<div style="height:34px;display:flex;align-items:center;padding:0 10px;gap:14px;color:#5f6368;font-size:18px">
<span>&#8592;</span><span>&#8594;</span><span>&#8635;</span>
<div style="flex:1;background:#fff;border-radius:16px;height:24px;display:flex;align-items:center;padding:0 12px;font-size:13px;color:#202124">
&#9432;&nbsp;&nbsp;{shown}</div></div>
<img src="data:image/png;base64,{data}" style="display:block"></body></html>"""
    page2 = b.new_page(viewport={"width": w, "height": 100})
    page2.set_content(wrapper)
    page2.wait_for_timeout(300)
    page2.screenshot(path=out, full_page=True)
    b.close()
print("->", out)
