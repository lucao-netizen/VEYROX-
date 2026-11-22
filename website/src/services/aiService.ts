import { AI_MODES } from "../config/veyrox";
import { AiChatResult, AiModeId, BackendHealthStatus, ChatMessage } from "../types/chat";

export interface ChatRequestPayload {
  message: string;
  mode: AiModeId;
  history: Array<{ role: "user" | "assistant"; content: string }>;
}

export interface AiClientProvider {
  readonly id: string;
  sendMessage(payload: ChatRequestPayload): Promise<AiChatResult>;
}

export function sanitizeBackendUrl(rawUrl: string): string {
  const trimmed = rawUrl.trim().replace(/\/+$/, "");
  if (!trimmed) return "";
  try {
    const parsed = new URL(trimmed);
    if (parsed.protocol !== "http:" && parsed.protocol !== "https:") {
      return "";
    }
    return parsed.origin + (parsed.pathname === "/" ? "" : parsed.pathname);
  } catch {
    return "";
  }
}

export class DemoAiClient implements AiClientProvider {
  readonly id = "demo-browser";

  async sendMessage(payload: ChatRequestPayload): Promise<AiChatResult> {
    // Brief realistic response latency for smooth UX
    await new Promise((resolve) => setTimeout(resolve, 420));

    const prompt = payload.message.trim();
    const modeInfo = AI_MODES[payload.mode];

    let reply = "";
    switch (payload.mode) {
      case "WRITING":
        reply =
          `> **DEMO MODE** — Simulated **${modeInfo.name}** output (no live backend connected)\n\n` +
          `### Polished Draft Preview\n` +
          `Your thought (*"${prompt.slice(0, 90)}${prompt.length > 90 ? "..." : ""}"*) structured for clarity:\n\n` +
          `**Headline:** Precision Turns Ideas Into Impact\n\n` +
          `Strong communication removes friction. When every sentence has a clear purpose, your audience understands both the vision and the next step immediately.\n\n` +
          `- **Tone:** Crisp, modern, and confident\n` +
          `- **Connect Live AI:** Add your deployed VEYROX backend URL in **Settings** to generate live Gemini rewrites.`;
        break;

      case "CODING":
        reply =
          `> **DEMO MODE** — Simulated **${modeInfo.name}** output (no live backend connected)\n\n` +
          `Here is a clean TypeScript starter pattern inspired by your request (*"${prompt.slice(0, 72)}${prompt.length > 72 ? "..." : ""}"*):\n\n` +
          "```typescript\n" +
          `export interface VeyroxHealth {\n` +
          `  status: "ok";\n` +
          `  provider: string;\n` +
          `  demoMode: boolean;\n` +
          `}\n\n` +
          `export async function fetchVeyroxHealth(apiBaseUrl: string): Promise<VeyroxHealth> {\n` +
          `  const response = await fetch(\`\${apiBaseUrl}/api/health\`, {\n` +
          `    signal: AbortSignal.timeout(8000),\n` +
          `  });\n` +
          `  if (!response.ok) {\n` +
          `    throw new Error(\`VEYROX backend error: HTTP \${response.status}\`);\n` +
          `  }\n` +
          `  return response.json();\n` +
          `}\n` +
          "```\n\n" +
          `Connect your VEYROX Node.js + Express backend with \`GEMINI_API_KEY\` configured on the server for live code generation and debugging.`;
        break;

      case "SUMMARIZER":
        reply =
          `> **DEMO MODE** — Simulated **${modeInfo.name}** output (no live backend connected)\n\n` +
          `### Executive TL;DR\n` +
          `Analyzed **${prompt.length} characters** under **Summarizer Mode**.\n\n` +
          `- **Core Signal:** Your local workspace is preserving conversations in IndexedDB.\n` +
          `- **Mode Isolation:** Summarizer applies a structured TL;DR & action-item system instruction.\n` +
          `- **Live Processing:** Configure your VEYROX backend URL in **Settings** to summarize real documents via Gemini.`;
        break;

      case "GENERAL":
      default:
        reply =
          `> **DEMO MODE** — Simulated **${modeInfo.name}** output (no live backend connected)\n\n` +
          `VEYROX received your message (${prompt.length} characters, ${payload.history.length} prior turns in context).\n\n` +
          `**What is active right now:**\n` +
          `- Persistent local chat history via **IndexedDB**\n` +
          `- Active mode: **${modeInfo.name}**\n` +
          `- Safe architecture: Zero API keys stored in browser code\n\n` +
          `To switch from **Demo Mode** to live **Gemini AI**, run the VEYROX Express backend and enter its URL in **Settings → Backend Connection**.`;
        break;
    }

    return {
      reply,
      mode: payload.mode,
      demo: true,
      model: "veyrox-demo-v1",
      provider: this.id,
      timestamp: Date.now()
    };
  }
}

export class BackendAiClient implements AiClientProvider {
  readonly id = "veyrox-backend";
  private readonly baseUrl: string;

  constructor(baseUrl: string) {
    this.baseUrl = sanitizeBackendUrl(baseUrl);
  }

  async sendMessage(payload: ChatRequestPayload): Promise<AiChatResult> {
    if (!this.baseUrl) {
      throw new Error("Backend URL is not configured or invalid.");
    }

    let response: Response;
    try {
      response = await fetch(`${this.baseUrl}/api/chat`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
        signal: AbortSignal.timeout(45_000)
      });
    } catch (error) {
      if (error instanceof DOMException && error.name === "TimeoutError") {
        throw new Error("Request timed out waiting for the VEYROX backend.");
      }
      throw new Error(
        "Unable to reach the VEYROX backend. Check your internet connection or verify the backend URL in Settings."
      );
    }

    let data: Record<string, unknown> = {};
    try {
      data = (await response.json()) as Record<string, unknown>;
    } catch {
      if (!response.ok) {
        throw new Error(`VEYROX backend returned HTTP ${response.status}.`);
      }
      throw new Error("Received an unreadable response from the VEYROX backend.");
    }

    if (!response.ok) {
      const serverMsg = typeof data.error === "string" ? data.error : `HTTP ${response.status}`;
      throw new Error(serverMsg);
    }

    return {
      reply: String(data.reply || ""),
      mode: (data.mode as AiModeId) || payload.mode,
      demo: Boolean(data.demo),
      model: String(data.model || "gemini"),
      provider: String(data.provider || this.id),
      timestamp: Number(data.timestamp || Date.now())
    };
  }
}

export async function checkBackendHealth(rawBaseUrl: string): Promise<BackendHealthStatus> {
  const baseUrl = sanitizeBackendUrl(rawBaseUrl);
  if (!baseUrl) {
    return {
      state: "demo",
      provider: "demo-browser",
      demoMode: true,
      model: "veyrox-demo-v1",
      message: "Demo Mode active — no backend URL configured.",
      checkedAt: Date.now()
    };
  }

  try {
    const response = await fetch(`${baseUrl}/api/health`, {
      signal: AbortSignal.timeout(8_000)
    });
    if (!response.ok) {
      return {
        state: "error",
        provider: "veyrox-backend",
        demoMode: true,
        message: `Backend returned HTTP ${response.status}.`,
        checkedAt: Date.now()
      };
    }

    const data = (await response.json()) as {
      status?: string;
      provider?: string;
      demoMode?: boolean;
      model?: string;
    };

    if (data.status !== "ok") {
      return {
        state: "error",
        provider: "veyrox-backend",
        demoMode: true,
        message: "Backend health check did not report 'ok'.",
        checkedAt: Date.now()
      };
    }

    return {
      state: "connected",
      provider: data.provider || "veyrox-backend",
      demoMode: Boolean(data.demoMode),
      model: data.model,
      message: data.demoMode
        ? "Connected to VEYROX backend (running in server Demo Mode — GEMINI_API_KEY not set)."
        : `Connected to live Gemini AI (${data.model || "gemini"}).`,
      checkedAt: Date.now()
    };
  } catch (error) {
    const isTimeout = error instanceof DOMException && error.name === "TimeoutError";
    return {
      state: "error",
      provider: "veyrox-backend",
      demoMode: true,
      message: isTimeout
        ? "Backend health check timed out."
        : "Backend unreachable. Check server status or switch to Demo Mode.",
      checkedAt: Date.now()
    };
  }
}

export async function dispatchAiChat(options: {
  backendUrl: string;
  message: string;
  mode: AiModeId;
  history: ChatMessage[];
}): Promise<AiChatResult> {
  const cleanUrl = sanitizeBackendUrl(options.backendUrl);
  const formattedHistory = options.history
    .filter((m) => !m.isError)
    .slice(-16)
    .map((m) => ({ role: m.role, content: m.content }));

  const payload: ChatRequestPayload = {
    message: options.message,
    mode: options.mode,
    history: formattedHistory
  };

  if (!cleanUrl) {
    const demoClient = new DemoAiClient();
    return demoClient.sendMessage(payload);
  }

  const backendClient = new BackendAiClient(cleanUrl);
  return backendClient.sendMessage(payload);
}
