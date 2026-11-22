import { BackendConfig } from "../types/ai.js";

const DEFAULT_DISCORD_URL = "https://discord.gg/WTctmpDXVc";

function parsePositiveInt(value: string | undefined, fallback: number): number {
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed > 0 ? Math.floor(parsed) : fallback;
}

function parseCorsOrigins(raw: string | undefined): string[] {
  const trimmed = (raw || "").trim();
  if (!trimmed) {
    return ["http://localhost:5173", "http://127.0.0.1:5173"];
  }
  return trimmed
    .split(",")
    .map((item) => item.trim())
    .filter(Boolean);
}

export function loadConfig(env: Record<string, string | undefined> = process.env): BackendConfig {
  return {
    port: parsePositiveInt(env.PORT, 8787),
    host: (env.HOST || "0.0.0.0").trim(),
    geminiApiKey: (env.GEMINI_API_KEY || "").trim(),
    geminiModel: (env.GEMINI_MODEL || "gemini-2.5-flash").trim(),
    corsOrigins: parseCorsOrigins(env.CORS_ORIGIN),
    rateLimitWindowMs: parsePositiveInt(env.RATE_LIMIT_WINDOW_MS, 60_000),
    rateLimitMaxRequests: parsePositiveInt(env.RATE_LIMIT_MAX_REQUESTS, 30),
    authRequired: String(env.AUTH_REQUIRED || "false").toLowerCase() === "true",
    authBearerToken: (env.AUTH_BEARER_TOKEN || "").trim(),
    discordUrl: (env.VEYROX_DISCORD_URL || DEFAULT_DISCORD_URL).trim() || DEFAULT_DISCORD_URL
  };
}
