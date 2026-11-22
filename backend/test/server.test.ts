import assert from "node:assert/strict";
import http from "node:http";
import test from "node:test";
import { createVeyroxApp } from "../src/app.js";

async function startTestServer(env: Record<string, string | undefined>) {
  const app = createVeyroxApp(env);
  const server = http.createServer(app);
  await new Promise<void>((resolve) => server.listen(0, "127.0.0.1", resolve));
  const address = server.address();
  assert(address && typeof address === "object");
  return {
    baseUrl: `http://127.0.0.1:${address.port}`,
    close: () => new Promise<void>((resolve) => server.close(() => resolve()))
  };
}

test("VEYROX Express backend: health, modes, validation, demo chat, and conversation CRUD", async () => {
  const { baseUrl, close } = await startTestServer({
    PORT: "0",
    GEMINI_API_KEY: "",
    CORS_ORIGIN: "*"
  });

  try {
    const healthRes = await fetch(`${baseUrl}/api/health`);
    assert.equal(healthRes.status, 200);
    const health = (await healthRes.json()) as {
      status: string;
      demoMode: boolean;
      discordUrl: string;
    };
    assert.equal(health.status, "ok");
    assert.equal(health.demoMode, true);
    assert.equal(health.discordUrl, "https://discord.gg/WTctmpDXVc");

    const modesRes = await fetch(`${baseUrl}/api/modes`);
    assert.equal(modesRes.status, 200);
    const modesBody = (await modesRes.json()) as { modes: Array<{ id: string }> };
    assert.equal(modesBody.modes.length, 4);

    // Reject empty message
    const invalidChat = await fetch(`${baseUrl}/api/chat`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ message: "   ", mode: "GENERAL" })
    });
    assert.equal(invalidChat.status, 400);

    // Reject invalid mode
    const invalidMode = await fetch(`${baseUrl}/api/chat`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ message: "Hello", mode: "UNKNOWN" })
    });
    assert.equal(invalidMode.status, 400);

    // Demo chat works across modes
    const demoChat = await fetch(`${baseUrl}/api/chat`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        message: "Build a health check helper",
        mode: "CODING",
        history: [{ role: "user", content: "Hi" }]
      })
    });
    assert.equal(demoChat.status, 200);
    const chatBody = (await demoChat.json()) as { demo: boolean; mode: string; reply: string };
    assert.equal(chatBody.demo, true);
    assert.equal(chatBody.mode, "CODING");
    assert(chatBody.reply.includes("DEMO MODE"));

    // Conversation CRUD
    const createConv = await fetch(`${baseUrl}/api/conversations`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ title: "Architecture Plan", mode: "CODING" })
    });
    assert.equal(createConv.status, 201);
    const created = (await createConv.json()) as { id: string; title: string };

    const listConv = await fetch(`${baseUrl}/api/conversations`);
    assert.equal(listConv.status, 200);
    const listed = (await listConv.json()) as { conversations: Array<{ id: string }> };
    assert.equal(listed.conversations.length, 1);

    const patchConv = await fetch(`${baseUrl}/api/conversations/${created.id}`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ title: "VEYROX v1 Architecture" })
    });
    assert.equal(patchConv.status, 200);
    const patched = (await patchConv.json()) as { title: string };
    assert.equal(patched.title, "VEYROX v1 Architecture");

    const deleteConv = await fetch(`${baseUrl}/api/conversations/${created.id}`, {
      method: "DELETE"
    });
    assert.equal(deleteConv.status, 200);

    const missingDelete = await fetch(`${baseUrl}/api/conversations/${created.id}`, {
      method: "DELETE"
    });
    assert.equal(missingDelete.status, 404);
  } finally {
    await close();
  }
});

test("VEYROX Express backend: enforces optional Bearer auth & rate limit", async () => {
  const { baseUrl, close } = await startTestServer({
    PORT: "0",
    AUTH_REQUIRED: "true",
    AUTH_BEARER_TOKEN: "test-secret-token",
    CORS_ORIGIN: "http://localhost:5173",
    RATE_LIMIT_MAX_REQUESTS: "4",
    RATE_LIMIT_WINDOW_MS: "60000"
  });

  try {
    const unauthorized = await fetch(`${baseUrl}/api/chat`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ message: "Hello", mode: "GENERAL" })
    });
    assert.equal(unauthorized.status, 401);

    const authorized = await fetch(`${baseUrl}/api/chat`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: "Bearer test-secret-token"
      },
      body: JSON.stringify({ message: "Hello", mode: "GENERAL" })
    });
    assert.equal(authorized.status, 200);
  } finally {
    await close();
  }
});
