export type AiModeId = "GENERAL" | "WRITING" | "CODING" | "SUMMARIZER";

export type AppRoute = "home" | "features" | "chat" | "community" | "about" | "settings";

export type ThemePreference = "dark" | "light";

export type MessageRole = "user" | "assistant";

export interface ChatMessage {
  id: string;
  role: MessageRole;
  content: string;
  timestamp: number;
  mode: AiModeId;
  isDemo?: boolean;
  model?: string;
  isError?: boolean;
}

export interface ConversationMetadata {
  messageCount: number;
  lastModel?: string;
  demoOnly?: boolean;
}

export interface Conversation {
  id: string;
  title: string;
  mode: AiModeId;
  messages: ChatMessage[];
  createdAt: number;
  updatedAt: number;
  autoTitleGenerated?: boolean;
  metadata: ConversationMetadata;
}

export interface ChatPreferences {
  theme: ThemePreference;
  enterToSend: boolean;
  showTimestamps: boolean;
  autoGenerateTitles: boolean;
  defaultMode: AiModeId;
  customBackendUrl: string;
}

export interface AiModeDefinition {
  id: AiModeId;
  name: string;
  shortLabel: string;
  description: string;
  systemInstruction: string;
  starterPrompts: string[];
}

export interface BackendHealthStatus {
  state: "demo" | "connected" | "error" | "checking";
  provider: string;
  demoMode: boolean;
  model?: string;
  message: string;
  checkedAt?: number;
}

export interface AiChatResult {
  reply: string;
  mode: AiModeId;
  demo: boolean;
  model: string;
  provider: string;
  timestamp: number;
}
