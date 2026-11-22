import crypto from "node:crypto";
import type { NextFunction, Request, Response } from "express";
import rateLimit from "express-rate-limit";
import { AI_MODES } from "../providers/aiModes.js";
import { BackendConfig, ChatTurn } from "../types/ai.js";

export function createApiRateLimiter(config: BackendConfig) {
  return rateLimit({
    windowMs: config.rateLimitWindowMs,
    max: config.rateLimitMaxRequests,
    standardHeaders: true,
    legacyHeaders: false,
    handler: (_req: Request, res: Response) => {
      res.status(429).json({
        error: "Rate limit exceeded. Please wait a moment before sending more requests."
      });
    }
  });
}

function constantTimeEquals(a: string, b: string): boolean {
  const bufA = Buffer.from(a, "utf8");
  const bufB = Buffer.from(b, "utf8");
  if (bufA.length !== bufB.length) {
    return false;
  }
  return crypto.timingSafeEqual(bufA, bufB);
}

export function createAuthMiddleware(config: BackendConfig) {
  return (req: Request, res: Response, next: NextFunction): void => {
    if (!config.authRequired) {
      next();
      return;
    }

    if (!config.authBearerToken) {
      res.status(500).json({ error: "Server authentication token is not configured." });
      return;
    }

    const authHeader = req.headers.authorization || "";
    const expected = `Bearer ${config.authBearerToken}`;
    if (!constantTimeEquals(authHeader, expected)) {
      res.status(401).json({ error: "Unauthorized request." });
      return;
    }

    next();
  };
}

export function validateChatPayload(body: unknown): string | null {
  if (!body || typeof body !== "object" || Array.isArray(body)) {
    return "Request body must be a JSON object.";
  }

  const record = body as Record<string, unknown>;
  const message = typeof record.message === "string" ? record.message.trim() : "";

  if (!message) {
    return "Field 'message' must be a non-empty string.";
  }

  if (message.length > 16_000) {
    return "Message exceeds maximum length of 16,000 characters.";
  }

  if (record.mode !== undefined) {
    const modeStr = String(record.mode).trim().toUpperCase();
    if (!(modeStr in AI_MODES)) {
      return "Field 'mode' must be one of GENERAL, WRITING, CODING, or SUMMARIZER.";
    }
  }

  if (record.history !== undefined) {
    if (!Array.isArray(record.history)) {
      return "Field 'history' must be an array.";
    }
    if (record.history.length > 40) {
      return "Field 'history' cannot exceed 40 turns.";
    }
    for (const item of record.history) {
      if (!item || typeof item !== "object") {
        return "Each history entry must be an object.";
      }
      const turn = item as Partial<ChatTurn>;
      if (turn.role !== "user" && turn.role !== "assistant") {
        return "Each history entry role must be 'user' or 'assistant'.";
      }
      if (typeof turn.content !== "string") {
        return "Each history entry content must be a string.";
      }
    }
  }

  return null;
}

export function validateCreateConversationPayload(body: unknown): string | null {
  if (body === undefined || body === null) {
    return null;
  }
  if (typeof body !== "object" || Array.isArray(body)) {
    return "Request body must be a JSON object.";
  }
  const record = body as Record<string, unknown>;
  if (record.title !== undefined && typeof record.title !== "string") {
    return "Field 'title' must be a string.";
  }
  if (record.mode !== undefined) {
    const modeStr = String(record.mode).trim().toUpperCase();
    if (!(modeStr in AI_MODES)) {
      return "Field 'mode' must be one of GENERAL, WRITING, CODING, or SUMMARIZER.";
    }
  }
  return null;
}

export function validateRenamePayload(body: unknown): string | null {
  if (!body || typeof body !== "object" || Array.isArray(body)) {
    return "Request body must be a JSON object.";
  }
  const record = body as Record<string, unknown>;
  const title = typeof record.title === "string" ? record.title.trim() : "";
  if (!title) {
    return "Field 'title' must be a non-empty string.";
  }
  if (title.length > 120) {
    return "Field 'title' must be 120 characters or fewer.";
  }
  return null;
}
