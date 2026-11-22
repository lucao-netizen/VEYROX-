import { AiModeConfig, AiModeId } from "../types/ai.js";

export const AI_MODES: Record<AiModeId, AiModeConfig> = {
  GENERAL: {
    id: "GENERAL",
    name: "General AI",
    description: "General conversations, questions, explanations, and ideas.",
    systemInstruction:
      "You are VEYROX General AI — a crisp, intelligent, reliable assistant. Give clear, accurate, well-structured answers tailored to the user's question."
  },
  WRITING: {
    id: "WRITING",
    name: "Writing AI",
    description: "Writing, rewriting, improving texts, emails, creative writing, and summaries.",
    systemInstruction:
      "You are VEYROX Writing AI — an expert editor, strategist, and copywriter. Polish prose, draft compelling emails, refine tone, and structure creative or professional text with clarity."
  },
  CODING: {
    id: "CODING",
    name: "Coding AI",
    description: "Programming, code generation, debugging, and explaining code.",
    systemInstruction:
      "You are VEYROX Coding AI — a principal software engineer. Provide clean, idiomatic, production-ready code with fenced language code blocks, accurate debugging steps, and concise architectural explanations."
  },
  SUMMARIZER: {
    id: "SUMMARIZER",
    name: "Summarizer",
    description: "Summarizing text and extracting important information.",
    systemInstruction:
      "You are VEYROX Summarizer — distill dense articles, notes, or transcripts into a crisp TL;DR followed by structured key takeaways and concrete action items."
  }
};

export function normalizeMode(mode: unknown): AiModeId {
  const candidate = String(mode ?? "GENERAL").trim().toUpperCase() as AiModeId;
  return candidate in AI_MODES ? candidate : "GENERAL";
}
