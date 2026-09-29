const fs = require('fs');
const path = require('path');

const pagesDir = path.join(__dirname, 'pages');
const files = fs.readdirSync(pagesDir).filter(f => f.endsWith('.html'));

const headerTarget1 = `fetch('../components/header.html')
            .then(r => r.text())
            .then(html => document.getElementById('header-placeholder').innerHTML = html);`;
const footerTarget1 = `fetch('../components/footer.html')
            .then(r => r.text())
            .then(html => document.getElementById('footer-placeholder').innerHTML = html);`;

const headerTarget2 = `fetch('../components/header.html').then(r => r.text()).then(html => document.getElementById('header-placeholder').innerHTML = html);`;
const footerTarget2 = `fetch('../components/footer.html').then(r => r.text()).then(html => document.getElementById('footer-placeholder').innerHTML = html);`;

const headerReplacement = `fetch('../components/header.html')
            .then(response => response.text())
            .then(data => {
                let html = data.replace(/href="index\\.html"/g, 'href="../index.html"');
                document.getElementById('header-placeholder').innerHTML = html;
            })
            .catch(error => console.error('Error loading header:', error));`;

const footerReplacement = `fetch('../components/footer.html')
            .then(response => response.text())
            .then(data => {
                let html = data.replace(/href="index\\.html"/g, 'href="../index.html"');
                document.getElementById('footer-placeholder').innerHTML = html;
            })
            .catch(error => console.error('Error loading footer:', error));`;

for (const file of files) {
    const filePath = path.join(pagesDir, file);
    let content = fs.readFileSync(filePath, 'utf8');
    let original = content;

    content = content.replace(headerTarget1, headerReplacement);
    content = content.replace(footerTarget1, footerReplacement);
    
    content = content.replace(headerTarget2, headerReplacement);
    content = content.replace(footerTarget2, footerReplacement);

    if (content !== original) {
        fs.writeFileSync(filePath, content, 'utf8');
        console.log(`Updated ${file}`);
    }
}
console.log('Done.');
