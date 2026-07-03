const fs = require("node:fs");
const http = require("node:http");
const path = require("node:path");
const { URL } = require("node:url");

const port = Number(process.env.PORT || 5173);
const apiTarget = new URL(process.env.API_TARGET || "http://gateway-service:9000");
const distDir = path.join(__dirname, "dist");

const mimeTypes = {
  ".html": "text/html; charset=utf-8",
  ".js": "text/javascript; charset=utf-8",
  ".css": "text/css; charset=utf-8",
  ".json": "application/json; charset=utf-8",
  ".png": "image/png",
  ".jpg": "image/jpeg",
  ".jpeg": "image/jpeg",
  ".svg": "image/svg+xml",
  ".ico": "image/x-icon",
  ".woff": "font/woff",
  ".woff2": "font/woff2"
};

function sendFile(res, filePath) {
  fs.readFile(filePath, (error, content) => {
    if (error) {
      res.writeHead(500, { "Content-Type": "text/plain; charset=utf-8" });
      res.end("Internal Server Error");
      return;
    }

    res.writeHead(200, {
      "Content-Type": mimeTypes[path.extname(filePath)] || "application/octet-stream"
    });
    res.end(content);
  });
}

function serveStatic(req, res) {
  const requestPath = decodeURIComponent(new URL(req.url, `http://${req.headers.host}`).pathname);
  const relativePath = requestPath === "/" ? "index.html" : requestPath.slice(1);
  const candidate = path.normalize(path.join(distDir, relativePath));

  if (!candidate.startsWith(distDir)) {
    res.writeHead(403, { "Content-Type": "text/plain; charset=utf-8" });
    res.end("Forbidden");
    return;
  }

  fs.stat(candidate, (error, stat) => {
    if (!error && stat.isFile()) {
      sendFile(res, candidate);
      return;
    }

    sendFile(res, path.join(distDir, "index.html"));
  });
}

function proxyApi(req, res) {
  const targetUrl = new URL(req.url, apiTarget);
  const proxyReq = http.request(
    {
      protocol: targetUrl.protocol,
      hostname: targetUrl.hostname,
      port: targetUrl.port,
      path: `${targetUrl.pathname}${targetUrl.search}`,
      method: req.method,
      headers: {
        ...req.headers,
        host: apiTarget.host
      }
    },
    (proxyRes) => {
      res.writeHead(proxyRes.statusCode || 502, proxyRes.headers);
      proxyRes.pipe(res, { end: true });
    }
  );

  proxyReq.on("error", () => {
    res.writeHead(502, { "Content-Type": "application/json; charset=utf-8" });
    res.end(JSON.stringify({ code: 502, message: "Gateway unavailable", data: null }));
  });

  req.pipe(proxyReq, { end: true });
}

http.createServer((req, res) => {
  if (req.url && req.url.startsWith("/api/")) {
    proxyApi(req, res);
    return;
  }

  serveStatic(req, res);
}).listen(port, "0.0.0.0", () => {
  console.log(`Frontend server listening on ${port}, proxying API to ${apiTarget.href}`);
});
