export type AiModeId = "GENERAL" | "WRITING" | "CODING" | "SUMMARIZER";

export interface AiModeConfig {
  id: AiModeId;
  name: string;
  description: string;
  systemInstruction: string;
}

export interface ChatTurn {
  role: "user" | "assistant";
  content: string;
}

export interface ChatRequest {
  message: string;
  mode: AiModeId;
  history?: ChatTurn[];
}

export interface ChatResponse {
  reply: string;
  mode: AiModeId;
  demo: boolean;
  model: string;
  provider: string;
  timestamp: number;
}

export interface ConversationMessage {
  id: string;
  role: "user" | "assistant";
  content: string;
  timestamp: number;
  demo?: boolean;
}

export interface ConversationRecord {
  id: string;
  title: string;
  mode: AiModeId;
  messages: ConversationMessage[];
  createdAt: number;
  updatedAt: number;
}

export interface AiProvider {
  readonly id: string;
  readonly isDemo: boolean;
  generateChat(request: ChatRequest): Promise<ChatResponse>;
}

export interface BackendConfig {
  port: number;
  host: string;
  geminiApiKey: string;
  geminiModel: string;
  corsOrigins: string[];
  rateLimitWindowMs: number;
  rateLimitMaxRequests: number;
  authRequired: boolean;
  authBearerToken: string;
  discordUrl: string;
}
