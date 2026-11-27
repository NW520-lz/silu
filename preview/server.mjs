import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const ROOT_DIR = path.resolve(__dirname, '..');
const DRAWABLE_DIR = path.join(ROOT_DIR, 'app', 'src', 'main', 'res', 'drawable');
const APK_PATH = path.join(ROOT_DIR, 'app', 'build', 'outputs', 'apk', 'debug', 'app-debug.apk');
const PORT = 3000;

const MIME_TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.png': 'image/png',
  '.svg': 'image/svg+xml',
  '.json': 'application/json; charset=utf-8',
  '.apk': 'application/vnd.android.package-archive',
};

const server = http.createServer((req, res) => {
  const urlPath = (req.url || '/').split('?')[0];

  if (urlPath === '/health') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ status: 'ok', app: '入画 Silu' }));
    return;
  }

  if (urlPath === '/' || urlPath === '/index.html') {
    const htmlPath = path.join(__dirname, 'index.html');
    fs.readFile(htmlPath, (err, data) => {
      if (err) {
        res.writeHead(500, { 'Content-Type': 'text/plain; charset=utf-8' });
        res.end('Failed to load preview.');
        return;
      }
      res.writeHead(200, {
        'Content-Type': MIME_TYPES['.html'],
        'Cache-Control': 'no-cache',
      });
      res.end(data);
    });
    return;
  }

  if (urlPath.startsWith('/drawables/')) {
    const filename = path.basename(urlPath);
    const filePath = path.join(DRAWABLE_DIR, filename);
    if (!filePath.startsWith(DRAWABLE_DIR) || !fs.existsSync(filePath)) {
      res.writeHead(404);
      res.end('Not Found');
      return;
    }
    const ext = path.extname(filename).toLowerCase();
    res.writeHead(200, {
      'Content-Type': MIME_TYPES[ext] || 'application/octet-stream',
      'Cache-Control': 'public, max-age=86400',
    });
    fs.createReadStream(filePath).pipe(res);
    return;
  }

  if (urlPath === '/download/silu-debug.apk') {
    if (!fs.existsSync(APK_PATH)) {
      res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' });
      res.end('APK not built yet');
      return;
    }
    res.writeHead(200, {
      'Content-Type': MIME_TYPES['.apk'],
      'Content-Disposition': 'attachment; filename="silu-debug.apk"',
    });
    fs.createReadStream(APK_PATH).pipe(res);
    return;
  }

  res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' });
  res.end('Not Found');
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`[Silu Preview Server] listening on http://0.0.0.0:${PORT}`);
});
