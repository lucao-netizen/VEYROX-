import crypto from "node:crypto";
import { AI_MODES, normalizeMode } from "../providers/aiModes.js";
import { AiModeId, ConversationRecord } from "../types/ai.js";

export class ConversationStore {
  private readonly conversations = new Map<string, ConversationRecord>();

  list(): ConversationRecord[] {
    return Array.from(this.conversations.values()).sort(
      (a, b) => b.updatedAt - a.updatedAt
    );
  }

  get(id: string): ConversationRecord | null {
    return this.conversations.get(id) ?? null;
  }

  create(options?: { title?: string; mode?: AiModeId | string }): ConversationRecord {
    const now = Date.now();
    const safeMode = normalizeMode(options?.mode);
    const defaultTitle = `New ${AI_MODES[safeMode].name} Chat`;
    const rawTitle = typeof options?.title === "string" ? options.title.trim() : "";
    const title = (rawTitle || defaultTitle).slice(0, 120);
    const id = crypto.randomUUID();

    const record: ConversationRecord = {
      id,
      title,
      mode: safeMode,
      messages: [],
      createdAt: now,
      updatedAt: now
    };

    this.conversations.set(id, record);
    return record;
  }

  rename(id: string, title: string): ConversationRecord | null | undefined {
    const existing = this.conversations.get(id);
    if (!existing) return null;

    const cleaned = title.trim().slice(0, 120);
    if (!cleaned) return undefined;

    existing.title = cleaned;
    existing.updatedAt = Date.now();
    return existing;
  }

  remove(id: string): boolean {
    return this.conversations.delete(id);
  }
}
