import { AiModeDefinition, AiModeId, ChatPreferences } from "../types/chat";

export const VEYROX_BRAND_NAME = "VEYROX";
export const VEYROX_SLOGAN = "YOUR AI. YOUR SPACE.";
export const VEYROX_HERO_HEADLINE = "INTELLIGENCE. WITHOUT LIMITS.";
export const VEYROX_HERO_DESCRIPTION =
  "Your AI space for creativity, knowledge, productivity and ideas. Everything you need, in one intelligent experience.";
export const VEYROX_VERSION = "1.0.0";

// Centralized official Discord invitation — never modify or scatter elsewhere
export const VEYROX_DISCORD_URL = "https://discord.gg/WTctmpDXVc";

// Optional public backend URL from Vite environment (never store API keys here)
export const VEYROX_DEFAULT_API_URL = (
  ((import.meta as unknown as { env?: Record<string, string | undefined> }).env?.VITE_VEYROX_API_URL) ||
  ""
).trim();

export const DEFAULT_CHAT_PREFERENCES: ChatPreferences = {
  theme: "dark",
  enterToSend: true,
  showTimestamps: true,
  autoGenerateTitles: true,
  defaultMode: "GENERAL",
  customBackendUrl: VEYROX_DEFAULT_API_URL
};

export const AI_MODES: Record<AiModeId, AiModeDefinition> = {
  GENERAL: {
    id: "GENERAL",
    name: "General AI",
    shortLabel: "General",
    description: "General conversations, questions, explanations, and ideas.",
    systemInstruction:
      "You are VEYROX General AI — a crisp, intelligent, reliable assistant. Provide clear, accurate, well-structured answers.",
    starterPrompts: [
      "Explain how vector databases work in plain English",
      "Brainstorm 5 high-impact SaaS product ideas for developers",
      "Help me plan a focused 2-hour deep work session"
    ]
  },
  WRITING: {
    id: "WRITING",
    name: "Writing AI",
    shortLabel: "Writing",
    description: "Writing, rewriting, improving texts, emails, creative writing, and summaries.",
    systemInstruction:
      "You are VEYROX Writing AI — an expert editor and copywriter. Polish prose, draft sharp emails, and elevate tone with clarity.",
    starterPrompts: [
      "Rewrite a product launch announcement to sound bold and concise",
      "Draft a polite follow-up email after a technical interview",
      "Polish this paragraph for executive clarity and flow"
    ]
  },
  CODING: {
    id: "CODING",
    name: "Coding AI",
    shortLabel: "Coding",
    description: "Programming, code generation, debugging, and explaining code.",
    systemInstruction:
      "You are VEYROX Coding AI — a principal software engineer. Deliver clean, idiomatic code blocks, debugging steps, and architectural insight.",
    starterPrompts: [
      "Write a TypeScript debounce hook for React with cleanup",
      "Design an Express rate-limiter and validation middleware",
      "Explain why race conditions happen in async React effects"
    ]
  },
  SUMMARIZER: {
    id: "SUMMARIZER",
    name: "Summarizer",
    shortLabel: "Summarizer",
    description: "Summarizing text and extracting important information.",
    systemInstruction:
      "You are VEYROX Summarizer — condense dense content into a sharp TL;DR, key takeaways, and clear action items.",
    starterPrompts: [
      "Summarize key engineering takeaways from a post-mortem report",
      "Extract action items and decisions from meeting notes",
      "Condense a technical specification into 5 executive bullets"
    ]
  }
};

export const AI_MODE_LIST: AiModeDefinition[] = [
  AI_MODES.GENERAL,
  AI_MODES.WRITING,
  AI_MODES.CODING,
  AI_MODES.SUMMARIZER
];
