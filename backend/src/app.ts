import cors from "cors";
import express, { NextFunction, Request, Response } from "express";
import helmet from "helmet";
import { loadConfig } from "./config/env.js";
import {
  createApiRateLimiter,
  createAuthMiddleware,
  validateChatPayload,
  validateCreateConversationPayload,
  validateRenamePayload
} from "./middleware/security.js";
import { AI_MODES, normalizeMode } from "./providers/aiModes.js";
import { createAiProvider } from "./providers/aiProvider.js";
import { ConversationStore } from "./store/conversationStore.js";
import { ChatTurn } from "./types/ai.js";

export function createVeyroxApp(customEnv?: Record<string, string | undefined>) {
  const config = loadConfig(customEnv);
  const aiProvider = createAiProvider(config);
  const store = new ConversationStore();
  const app = express();

  app.disable("x-powered-by");
  app.use(helmet());

  const allowAllOrigins = !config.authRequired && config.corsOrigins.includes("*");
  app.use(
    cors({
      origin: (origin, callback) => {
        if (!origin) {
          callback(null, true);
          return;
        }
        if (allowAllOrigins || config.corsOrigins.includes(origin)) {
          callback(null, true);
          return;
        }
        callback(new Error("CORS origin not allowed"));
      },
      methods: ["GET", "POST", "PATCH", "DELETE", "OPTIONS"],
      allowedHeaders: ["Content-Type", "Authorization"]
    })
  );

  app.use(express.json({ limit: "256kb" }));

  // Handle malformed JSON bodies cleanly
  app.use((err: Error & { status?: number; type?: string }, _req: Request, res: Response, next: NextFunction) => {
    if (err.type === "entity.parse.failed") {
      res.status(400).json({ error: "Malformed JSON payload." });
      return;
    }
    if (err.type === "entity.too.large") {
      res.status(413).json({ error: "Payload exceeds 256KB limit." });
      return;
    }
    if (err.message === "CORS origin not allowed") {
      res.status(403).json({ error: "Origin not permitted by CORS policy." });
      return;
    }
    next(err);
  });

  app.use("/api", createApiRateLimiter(config));

  // Privacy-conscious request logger (logs method, path, status, latency only — never payloads)
  app.use("/api", (req: Request, res: Response, next: NextFunction) => {
    const start = Date.now();
    res.on("finish", () => {
      const durationMs = Date.now() - start;
      console.info(`[VEYROX API] ${req.method} ${req.originalUrl} -> ${res.statusCode} (${durationMs}ms)`);
    });
    next();
  });

  app.get("/api/health", (_req: Request, res: Response) => {
    res.json({
      status: "ok",
      service: "veyrox-backend",
      version: "1.0.0",
      provider: aiProvider.id,
      demoMode: aiProvider.isDemo,
      model: aiProvider.isDemo ? "veyrox-demo-v1" : config.geminiModel,
      discordUrl: config.discordUrl,
      timestamp: Date.now()
    });
  });

  app.get("/api/modes", (_req: Request, res: Response) => {
    res.json({
      modes: Object.values(AI_MODES)
    });
  });

  const authMiddleware = createAuthMiddleware(config);

  app.post("/api/chat", authMiddleware, async (req: Request, res: Response) => {
    const validationError = validateChatPayload(req.body);
    if (validationError) {
      res.status(400).json({ error: validationError });
      return;
    }

    const body = req.body as {
      message: string;
      mode?: string;
      history?: ChatTurn[];
    };

    try {
      const result = await aiProvider.generateChat({
        message: body.message.trim(),
        mode: normalizeMode(body.mode),
        history: body.history ?? []
      });
      res.json(result);
    } catch (error) {
      const msg = error instanceof Error ? error.message : "Unknown upstream error";
      console.error("[VEYROX AI ERROR]", msg);
      res.status(502).json({
        error: "AI provider is temporarily unavailable. Check backend configuration or try again."
      });
    }
  });

  app.get("/api/conversations", authMiddleware, (_req: Request, res: Response) => {
    res.json({ conversations: store.list() });
  });

  app.post("/api/conversations", authMiddleware, (req: Request, res: Response) => {
    const validationError = validateCreateConversationPayload(req.body);
    if (validationError) {
      res.status(400).json({ error: validationError });
      return;
    }
    const body = (req.body || {}) as { title?: string; mode?: string };
    const created = store.create({
      title: body.title,
      mode: body.mode ? normalizeMode(body.mode) : undefined
    });
    res.status(201).json(created);
  });

  app.patch("/api/conversations/:id", authMiddleware, (req: Request, res: Response) => {
    const validationError = validateRenamePayload(req.body);
    if (validationError) {
      res.status(400).json({ error: validationError });
      return;
    }

    const convId = String(req.params.id || "");
    const body = req.body as { title: string };
    const updated = store.rename(convId, body.title);

    if (updated === null) {
      res.status(404).json({ error: "Conversation not found." });
      return;
    }
    if (updated === undefined) {
      res.status(400).json({ error: "Field 'title' must be non-empty." });
      return;
    }
    res.json(updated);
  });

  app.delete("/api/conversations/:id", authMiddleware, (req: Request, res: Response) => {
    const convId = String(req.params.id || "");
    const removed = store.remove(convId);
    if (!removed) {
      res.status(404).json({ error: "Conversation not found." });
      return;
    }
    res.json({ deleted: true, id: convId });
  });

  app.use((_req: Request, res: Response) => {
    res.status(404).json({ error: "Endpoint not found." });
  });

  return app;
}
