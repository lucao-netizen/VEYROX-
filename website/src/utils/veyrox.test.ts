import assert from "node:assert/strict";
import test from "node:test";
import { AI_MODES, VEYROX_DISCORD_URL } from "../config/veyrox.ts";
import { DemoAiClient, sanitizeBackendUrl } from "../services/aiService.ts";
import { createNewConversation, generateConversationTitle } from "./storage.ts";

test("VEYROX config centralizes official Discord URL and 4 AI modes", () => {
  assert.equal(VEYROX_DISCORD_URL, "https://discord.gg/WTctmpDXVc");
  assert.deepEqual(Object.keys(AI_MODES), ["GENERAL", "WRITING", "CODING", "SUMMARIZER"]);
  for (const mode of Object.values(AI_MODES)) {
    assert(mode.systemInstruction.length > 20);
    assert(mode.starterPrompts.length >= 3);
  }
});

test("sanitizeBackendUrl allows http/https and rejects unsafe protocols", () => {
  assert.equal(sanitizeBackendUrl("https://api.veyrox.example.com/"), "https://api.veyrox.example.com");
  assert.equal(sanitizeBackendUrl("http://localhost:8787"), "http://localhost:8787");
  assert.equal(sanitizeBackendUrl("javascript:alert(1)"), "");
  assert.equal(sanitizeBackendUrl("ftp://invalid.host"), "");
  assert.equal(sanitizeBackendUrl("   "), "");
});

test("conversation helpers generate clean auto-titles and metadata", () => {
  const title = generateConversationTitle("   Build a responsive TypeScript navbar with Tailwind   ", "CODING");
  assert.equal(title, "Build a responsive TypeScript navbar with...");

  const conv = createNewConversation("WRITING");
  assert.equal(conv.mode, "WRITING");
  assert.equal(conv.messages.length, 0);
  assert.equal(conv.metadata.messageCount, 0);
});

test("DemoAiClient produces clearly labeled demo responses for each mode", async () => {
  const client = new DemoAiClient();
  const codingReply = await client.sendMessage({
    message: "Write a fetch helper",
    mode: "CODING",
    history: []
  });
  assert.equal(codingReply.demo, true);
  assert.equal(codingReply.mode, "CODING");
  assert(codingReply.reply.includes("DEMO MODE"));
  assert(codingReply.reply.includes("```typescript"));
});
