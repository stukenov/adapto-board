#!/usr/bin/env node
/**
 * MCP stdio↔HTTP proxy for Playout Edge.
 * Reads JSON-RPC from stdin, forwards to the remote MCP endpoint, writes responses to stdout.
 *
 * Env vars: PE_URL, PE_EMAIL, PE_PASSWORD
 */

import { createInterface } from "node:readline";

const BASE_URL = process.env.PE_URL || "https://tv.adapto.kz";
const EMAIL = process.env.PE_EMAIL;
const PASSWORD = process.env.PE_PASSWORD;
const MCP_ENDPOINT = `${BASE_URL}/api/mcp`;

let accessToken = null;
let refreshCookie = null;

async function login() {
  const res = await fetch(`${BASE_URL}/api/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email: EMAIL, password: PASSWORD }),
  });
  if (!res.ok) throw new Error(`Login failed: ${res.status}`);
  const cookie = res.headers.get("set-cookie");
  if (cookie) refreshCookie = cookie.split(";")[0];
  const data = await res.json();
  accessToken = data.accessToken;
}

async function refreshToken() {
  if (!refreshCookie) return login();
  const res = await fetch(`${BASE_URL}/api/auth/refresh`, {
    method: "POST",
    headers: { Cookie: refreshCookie },
  });
  if (!res.ok) return login();
  const data = await res.json();
  accessToken = data.accessToken;
}

async function sendRpc(message) {
  const headers = {
    "Content-Type": "application/json",
    Authorization: `Bearer ${accessToken}`,
  };

  let res = await fetch(MCP_ENDPOINT, {
    method: "POST",
    headers,
    body: JSON.stringify(message),
  });

  // Auto-refresh on 401
  if (res.status === 401) {
    await refreshToken();
    headers.Authorization = `Bearer ${accessToken}`;
    res = await fetch(MCP_ENDPOINT, {
      method: "POST",
      headers,
      body: JSON.stringify(message),
    });
  }

  if (message.method === "notifications/initialized") {
    // Notification — no response body expected
    return null;
  }

  return await res.json();
}

async function main() {
  await login();

  const rl = createInterface({ input: process.stdin });

  for await (const line of rl) {
    if (!line.trim()) continue;
    let message;
    try {
      message = JSON.parse(line);
    } catch {
      process.stderr.write(`Invalid JSON: ${line}\n`);
      continue;
    }

    try {
      const response = await sendRpc(message);
      if (response !== null) {
        process.stdout.write(JSON.stringify(response) + "\n");
      }
    } catch (err) {
      // Return JSON-RPC error
      if (message.id != null) {
        process.stdout.write(
          JSON.stringify({
            jsonrpc: "2.0",
            id: message.id,
            error: { code: -32000, message: err.message },
          }) + "\n"
        );
      }
    }
  }
}

main().catch((err) => {
  process.stderr.write(`Fatal: ${err.message}\n`);
  process.exit(1);
});
