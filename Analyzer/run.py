#!/usr/bin/env python3
"""Cross-platform launcher for the offline AKPP Health Analyzer."""

from __future__ import annotations

import http.server
import socket
import socketserver
import sys
import webbrowser
from functools import partial
from pathlib import Path

HOST = "127.0.0.1"
START_PORT = 8080
PORT_SCAN_COUNT = 101  # 8080..8180 inclusive


class ThreadingHTTPServer(socketserver.ThreadingMixIn, http.server.HTTPServer):
    daemon_threads = True
    allow_reuse_address = True


class NoCacheRequestHandler(http.server.SimpleHTTPRequestHandler):
    """Serve static files with no-cache headers to always show fresh UI updates."""

    def end_headers(self) -> None:
        self.send_header("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0")
        self.send_header("Pragma", "no-cache")
        self.send_header("Expires", "0")
        super().end_headers()


def find_free_port(host: str, start_port: int, scan_count: int) -> int | None:
    for port in range(start_port, start_port + scan_count):
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sock:
            sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            try:
                sock.bind((host, port))
            except OSError:
                continue
            return port
    return None


def main() -> int:
    project_dir = Path(__file__).resolve().parent
    port = find_free_port(HOST, START_PORT, PORT_SCAN_COUNT)
    if port is None:
        print(
            f"ERROR: No free port found in range {START_PORT}..{START_PORT + PORT_SCAN_COUNT - 1}.",
            file=sys.stderr,
        )
        return 1

    handler = partial(NoCacheRequestHandler, directory=str(project_dir))
    server = ThreadingHTTPServer((HOST, port), handler)
    url = f"http://{HOST}:{port}/index.html"

    print("AKPP Health Analyzer local server")
    print(f"Serving directory: {project_dir}")
    print(f"Selected port: {port}")
    print(f"Open URL: {url}")

    try:
        opened = webbrowser.open(url, new=2)
        if not opened:
            print("Warning: could not auto-open browser. Open the URL above manually.")
    except Exception as exc:  # pragma: no cover - browser availability is environment-dependent
        print(f"Warning: browser auto-open failed ({exc}). Open the URL above manually.")

    print("Press Ctrl+C to stop.")

    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nStopping server...")
    finally:
        server.server_close()
        print("Server stopped.")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
