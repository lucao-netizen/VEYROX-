import { DEFAULT_CHAT_PREFERENCES } from "../config/veyrox";
import { AiModeId, ChatPreferences, Conversation } from "../types/chat";

const DB_NAME = "veyrox-db";
const DB_VERSION = 1;
const STORE_CONVERSATIONS = "conversations";
const STORE_SETTINGS = "settings";
const PREFS_KEY = "chat_preferences";

const LS_CONVERSATIONS_KEY = "veyrox_conversations_fallback";
const LS_PREFS_KEY = "veyrox_preferences_fallback";

function openDatabase(): Promise<IDBDatabase | null> {
  if (typeof indexedDB === "undefined") {
    return Promise.resolve(null);
  }
  return new Promise((resolve) => {
    try {
      const request = indexedDB.open(DB_NAME, DB_VERSION);
      request.onupgradeneeded = () => {
        const db = request.result;
        if (!db.objectStoreNames.contains(STORE_CONVERSATIONS)) {
          const convStore = db.createObjectStore(STORE_CONVERSATIONS, { keyPath: "id" });
          convStore.createIndex("updatedAt", "updatedAt", { unique: false });
        }
        if (!db.objectStoreNames.contains(STORE_SETTINGS)) {
          db.createObjectStore(STORE_SETTINGS, { keyPath: "key" });
        }
      };
      request.onsuccess = () => resolve(request.result);
      request.onerror = () => resolve(null);
    } catch {
      resolve(null);
    }
  });
}

function readLocalFallbackConversations(): Conversation[] {
  try {
    const raw = localStorage.getItem(LS_CONVERSATIONS_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

function writeLocalFallbackConversations(items: Conversation[]): void {
  try {
    localStorage.setItem(LS_CONVERSATIONS_KEY, JSON.stringify(items));
  } catch {
    // Ignore storage quota issues gracefully
  }
}

export async function getAllConversations(): Promise<Conversation[]> {
  const db = await openDatabase();
  if (!db) {
    return readLocalFallbackConversations().sort((a, b) => b.updatedAt - a.updatedAt);
  }

  return new Promise((resolve) => {
    try {
      const tx = db.transaction(STORE_CONVERSATIONS, "readonly");
      const store = tx.objectStore(STORE_CONVERSATIONS);
      const req = store.getAll();
      req.onsuccess = () => {
        const list = (req.result as Conversation[]) || [];
        resolve(list.sort((a, b) => b.updatedAt - a.updatedAt));
      };
      req.onerror = () => resolve(readLocalFallbackConversations());
    } catch {
      resolve(readLocalFallbackConversations());
    }
  });
}

export async function saveConversation(conversation: Conversation): Promise<void> {
  const db = await openDatabase();
  if (!db) {
    const existing = readLocalFallbackConversations().filter((c) => c.id !== conversation.id);
    writeLocalFallbackConversations([conversation, ...existing]);
    return;
  }

  return new Promise((resolve) => {
    try {
      const tx = db.transaction(STORE_CONVERSATIONS, "readwrite");
      tx.objectStore(STORE_CONVERSATIONS).put(conversation);
      tx.oncomplete = () => resolve();
      tx.onerror = () => {
        const existing = readLocalFallbackConversations().filter((c) => c.id !== conversation.id);
        writeLocalFallbackConversations([conversation, ...existing]);
        resolve();
      };
    } catch {
      resolve();
    }
  });
}

export async function deleteConversationById(id: string): Promise<void> {
  const db = await openDatabase();
  if (!db) {
    writeLocalFallbackConversations(readLocalFallbackConversations().filter((c) => c.id !== id));
    return;
  }

  return new Promise((resolve) => {
    try {
      const tx = db.transaction(STORE_CONVERSATIONS, "readwrite");
      tx.objectStore(STORE_CONVERSATIONS).delete(id);
      tx.oncomplete = () => resolve();
      tx.onerror = () => resolve();
    } catch {
      resolve();
    }
  });
}

export async function clearAllConversationsStorage(): Promise<void> {
  writeLocalFallbackConversations([]);
  const db = await openDatabase();
  if (!db) return;

  return new Promise((resolve) => {
    try {
      const tx = db.transaction(STORE_CONVERSATIONS, "readwrite");
      tx.objectStore(STORE_CONVERSATIONS).clear();
      tx.oncomplete = () => resolve();
      tx.onerror = () => resolve();
    } catch {
      resolve();
    }
  });
}

export async function getPreferences(): Promise<ChatPreferences> {
  const readFallback = (): ChatPreferences => {
    try {
      const raw = localStorage.getItem(LS_PREFS_KEY);
      if (!raw) return DEFAULT_CHAT_PREFERENCES;
      return { ...DEFAULT_CHAT_PREFERENCES, ...JSON.parse(raw) };
    } catch {
      return DEFAULT_CHAT_PREFERENCES;
    }
  };

  const db = await openDatabase();
  if (!db) return readFallback();

  return new Promise((resolve) => {
    try {
      const tx = db.transaction(STORE_SETTINGS, "readonly");
      const req = tx.objectStore(STORE_SETTINGS).get(PREFS_KEY);
      req.onsuccess = () => {
        const record = req.result as { key: string; value: ChatPreferences } | undefined;
        resolve(record?.value ? { ...DEFAULT_CHAT_PREFERENCES, ...record.value } : readFallback());
      };
      req.onerror = () => resolve(readFallback());
    } catch {
      resolve(readFallback());
    }
  });
}

export async function savePreferences(prefs: ChatPreferences): Promise<void> {
  try {
    localStorage.setItem(LS_PREFS_KEY, JSON.stringify(prefs));
  } catch {
    // Ignore
  }

  const db = await openDatabase();
  if (!db) return;

  return new Promise((resolve) => {
    try {
      const tx = db.transaction(STORE_SETTINGS, "readwrite");
      tx.objectStore(STORE_SETTINGS).put({ key: PREFS_KEY, value: prefs });
      tx.oncomplete = () => resolve();
      tx.onerror = () => resolve();
    } catch {
      resolve();
    }
  });
}

export function generateConversationTitle(firstPrompt: string, mode: AiModeId): string {
  const cleaned = firstPrompt.replace(/\s+/g, " ").trim();
  if (!cleaned) {
    return `New ${mode.charAt(0) + mode.slice(1).toLowerCase()} Chat`;
  }
  const words = cleaned.split(" ").slice(0, 7).join(" ");
  return words.length > 44 ? `${words.slice(0, 41)}...` : words;
}

export function createNewConversation(mode: AiModeId, title?: string): Conversation {
  const now = Date.now();
  const id =
    typeof crypto !== "undefined" && typeof crypto.randomUUID === "function"
      ? crypto.randomUUID()
      : `conv_${now}_${Math.random().toString(36).slice(2, 8)}`;

  const modeLabel = mode.charAt(0) + mode.slice(1).toLowerCase();
  return {
    id,
    title: title?.trim() || `New ${modeLabel} Chat`,
    mode,
    messages: [],
    createdAt: now,
    updatedAt: now,
    autoTitleGenerated: Boolean(title?.trim()),
    metadata: {
      messageCount: 0,
      demoOnly: true
    }
  };
}
