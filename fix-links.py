import os
import re

pages_dir = os.path.join(os.path.dirname(__file__), 'pages')
files = [f for f in os.listdir(pages_dir) if f.endswith('.html')]

header_target1 = """fetch('../components/header.html')
            .then(r => r.text())
            .then(html => document.getElementById('header-placeholder').innerHTML = html);"""
footer_target1 = """fetch('../components/footer.html')
            .then(r => r.text())
            .then(html => document.getElementById('footer-placeholder').innerHTML = html);"""

header_target2 = """fetch('../components/header.html').then(r => r.text()).then(html => document.getElementById('header-placeholder').innerHTML = html);"""
footer_target2 = """fetch('../components/footer.html').then(r => r.text()).then(html => document.getElementById('footer-placeholder').innerHTML = html);"""

header_replacement = """fetch('../components/header.html')
            .then(response => response.text())
            .then(data => {
                let html = data.replace(/href="index\\.html"/g, 'href="../index.html"');
                document.getElementById('header-placeholder').innerHTML = html;
            })
            .catch(error => console.error('Error loading header:', error));"""

footer_replacement = """fetch('../components/footer.html')
            .then(response => response.text())
            .then(data => {
                let html = data.replace(/href="index\\.html"/g, 'href="../index.html"');
                document.getElementById('footer-placeholder').innerHTML = html;
            })
            .catch(error => console.error('Error loading footer:', error));"""

for f in files:
    file_path = os.path.join(pages_dir, f)
    with open(file_path, 'r', encoding='utf-8') as file:
        content = file.read()
    
    original = content
    content = content.replace(header_target1, header_replacement)
    content = content.replace(footer_target1, footer_replacement)
    content = content.replace(header_target2, header_replacement)
    content = content.replace(footer_target2, footer_replacement)

    if content != original:
        with open(file_path, 'w', encoding='utf-8') as file:
            file.write(content)
        print(f"Updated {f}")

print("Done.")
