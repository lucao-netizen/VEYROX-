import { AiProvider, BackendConfig, ChatRequest, ChatResponse } from "../types/ai.js";
import { AI_MODES, normalizeMode } from "./aiModes.js";

export class DemoAiProvider implements AiProvider {
  readonly id = "demo";
  readonly isDemo = true;

  async generateChat(request: ChatRequest): Promise<ChatResponse> {
    const safeMode = normalizeMode(request.mode);
    const modeInfo = AI_MODES[safeMode];
    const trimmed = request.message.trim();

    let reply: string;
    switch (safeMode) {
      case "WRITING":
        reply =
          `**[BACKEND DEMO MODE — ${modeInfo.name}]**\n\n` +
          `Here is a structured draft template based on your prompt (*${trimmed.slice(0, 80)}${trimmed.length > 80 ? "..." : ""}*):\n\n` +
          `> Clarity elevates every message. Configure \`GEMINI_API_KEY\` on the VEYROX backend to generate live tailored prose.\n\n` +
          `- **Tone:** Executive & concise\n` +
          `- **Next step:** Add your server-side \`GEMINI_API_KEY\` in \`backend/.env\`.`;
        break;
      case "CODING":
        reply =
          `**[BACKEND DEMO MODE — ${modeInfo.name}]**\n\n` +
          `Simulated response for your engineering prompt:\n\n` +
          "```typescript\n" +
          `// VEYROX health check example\n` +
          `export async function checkVeyroxHealth(baseUrl: string) {\n` +
          `  const res = await fetch(\`\${baseUrl}/api/health\`);\n` +
          `  if (!res.ok) throw new Error(\`HTTP \${res.status}\`);\n` +
          `  return res.json();\n` +
          `}\n` +
          "```\n\n" +
          `Set \`GEMINI_API_KEY\` on the server to enable live Gemini code generation.`;
        break;
      case "SUMMARIZER":
        reply =
          `**[BACKEND DEMO MODE — ${modeInfo.name}]**\n\n` +
          `**TL;DR:** Simulated summary generated for ${trimmed.length} characters of input.\n\n` +
          `- **Key Point 1:** Backend API pipeline and validation succeeded.\n` +
          `- **Key Point 2:** Live Gemini processing activates automatically once \`GEMINI_API_KEY\` is set in \`backend/.env\`.`;
        break;
      case "GENERAL":
      default:
        reply =
          `**[BACKEND DEMO MODE — ${modeInfo.name}]**\n\n` +
          `Your request reached the VEYROX backend cleanly (${trimmed.length} chars, ${request.history?.length ?? 0} history turns).\n\n` +
          `This is a transparent **Demo Mode** response because \`GEMINI_API_KEY\` is not configured on the server.`;
        break;
    }

    return {
      reply,
      mode: safeMode,
      demo: true,
      model: "veyrox-demo-v1",
      provider: this.id,
      timestamp: Date.now()
    };
  }
}

interface GeminiCandidatePart {
  text?: string;
}

interface GeminiGenerateResponse {
  candidates?: Array<{
    content?: {
      parts?: GeminiCandidatePart[];
    };
  }>;
}

export class GeminiAiProvider implements AiProvider {
  readonly id = "gemini";
  readonly isDemo = false;
  private readonly apiKey: string;
  private readonly model: string;

  constructor(options: { apiKey: string; model: string }) {
    this.apiKey = options.apiKey;
    this.model = options.model;
  }

  async generateChat(request: ChatRequest): Promise<ChatResponse> {
    const safeMode = normalizeMode(request.mode);
    const systemInstruction = AI_MODES[safeMode].systemInstruction;
    const historyTurns = (request.history ?? []).slice(-20).map((turn) => ({
      role: turn.role === "user" ? "user" : "model",
      parts: [{ text: String(turn.content || "") }]
    }));

    const contents = [
      ...historyTurns,
      { role: "user", parts: [{ text: request.message.trim() }] }
    ];

    const endpoint = `https://generativelanguage.googleapis.com/v1beta/models/${encodeURIComponent(
      this.model
    )}:generateContent`;

    const response = await fetch(endpoint, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "x-goog-api-key": this.apiKey
      },
      body: JSON.stringify({
        systemInstruction: { parts: [{ text: systemInstruction }] },
        contents
      }),
      signal: AbortSignal.timeout(45_000)
    });

    if (!response.ok) {
      throw new Error(`Upstream Gemini service returned HTTP ${response.status}`);
    }

    const data = (await response.json()) as GeminiGenerateResponse;
    const reply = data.candidates?.[0]?.content?.parts
      ?.map((part) => part.text ?? "")
      .join("")
      .trim();

    if (!reply) {
      throw new Error("Upstream Gemini service returned an empty response.");
    }

    return {
      reply,
      mode: safeMode,
      demo: false,
      model: this.model,
      provider: this.id,
      timestamp: Date.now()
    };
  }
}

export function createAiProvider(config: BackendConfig): AiProvider {
  if (config.geminiApiKey) {
    return new GeminiAiProvider({
      apiKey: config.geminiApiKey,
      model: config.geminiModel
    });
  }
  return new DemoAiProvider();
}
