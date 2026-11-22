import { useCallback, useEffect, useMemo, useState } from "react";
import { DEFAULT_CHAT_PREFERENCES } from "../config/veyrox";
import { checkBackendHealth, dispatchAiChat, sanitizeBackendUrl } from "../services/aiService";
import {
  AiModeId,
  BackendHealthStatus,
  ChatMessage,
  ChatPreferences,
  Conversation
} from "../types/chat";
import {
  clearAllConversationsStorage,
  createNewConversation,
  deleteConversationById,
  generateConversationTitle,
  getAllConversations,
  getPreferences,
  saveConversation,
  savePreferences
} from "../utils/storage";

function makeId(prefix: string): string {
  if (typeof crypto !== "undefined" && typeof crypto.randomUUID === "function") {
    return `${prefix}_${crypto.randomUUID()}`;
  }
  return `${prefix}_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`;
}

export function useVeyroxWorkspace() {
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [activeConversationId, setActiveConversationId] = useState<string | null>(null);
  const [selectedMode, setSelectedMode] = useState<AiModeId>(DEFAULT_CHAT_PREFERENCES.defaultMode);
  const [preferences, setPreferences] = useState<ChatPreferences>(DEFAULT_CHAT_PREFERENCES);
  const [isHydrated, setIsHydrated] = useState(false);
  const [isGenerating, setIsGenerating] = useState(false);
  const [chatError, setChatError] = useState<string | null>(null);
  const [backendStatus, setBackendStatus] = useState<BackendHealthStatus>({
    state: "demo",
    provider: "demo-browser",
    demoMode: true,
    model: "veyrox-demo-v1",
    message: "Demo Mode active — no backend URL configured."
  });

  const refreshBackendStatus = useCallback(async (urlToCheck?: string) => {
    const targetUrl = urlToCheck !== undefined ? urlToCheck : preferences.customBackendUrl;
    const cleanUrl = sanitizeBackendUrl(targetUrl);
    if (!cleanUrl) {
      setBackendStatus({
        state: "demo",
        provider: "demo-browser",
        demoMode: true,
        model: "veyrox-demo-v1",
        message: "Demo Mode active — no backend URL configured.",
        checkedAt: Date.now()
      });
      return;
    }

    setBackendStatus((prev) => ({
      ...prev,
      state: "checking",
      message: "Checking VEYROX backend connection..."
    }));
    const result = await checkBackendHealth(cleanUrl);
    setBackendStatus(result);
  }, [preferences.customBackendUrl]);

  // Hydrate conversations and preferences from IndexedDB on initial mount
  useEffect(() => {
    let active = true;
    (async () => {
      const [loadedPrefs, loadedConversations] = await Promise.all([
        getPreferences(),
        getAllConversations()
      ]);
      if (!active) return;
      setPreferences(loadedPrefs);
      setSelectedMode(loadedPrefs.defaultMode);
      setConversations(loadedConversations);
      if (loadedConversations.length > 0) {
        setActiveConversationId(loadedConversations[0].id);
        setSelectedMode(loadedConversations[0].mode);
      }
      setIsHydrated(true);

      const health = await checkBackendHealth(loadedPrefs.customBackendUrl);
      if (active) {
        setBackendStatus(health);
      }
    })();

    return () => {
      active = false;
    };
  }, []);

  const activeConversation = useMemo(
    () => conversations.find((c) => c.id === activeConversationId) ?? null,
    [conversations, activeConversationId]
  );

  const updatePreferences = useCallback(
    async (partial: Partial<ChatPreferences>) => {
      const next = { ...preferences, ...partial };
      setPreferences(next);
      await savePreferences(next);
      if (partial.customBackendUrl !== undefined) {
        const health = await checkBackendHealth(next.customBackendUrl);
        setBackendStatus(health);
      }
    },
    [preferences]
  );

  const createConversation = useCallback(
    async (modeOverride?: AiModeId): Promise<Conversation> => {
      const mode = modeOverride ?? selectedMode;
      const created = createNewConversation(mode);
      setConversations((prev) => [created, ...prev]);
      setActiveConversationId(created.id);
      setSelectedMode(mode);
      setChatError(null);
      await saveConversation(created);
      return created;
    },
    [selectedMode]
  );

  const selectConversation = useCallback(
    (id: string) => {
      const found = conversations.find((c) => c.id === id);
      if (!found) return;
      setActiveConversationId(found.id);
      setSelectedMode(found.mode);
      setChatError(null);
    },
    [conversations]
  );

  const changeMode = useCallback(
    async (mode: AiModeId) => {
      setSelectedMode(mode);
      if (!activeConversation) return;
      const updated: Conversation = {
        ...activeConversation,
        mode,
        updatedAt: Date.now()
      };
      setConversations((prev) => prev.map((c) => (c.id === updated.id ? updated : c)));
      await saveConversation(updated);
    },
    [activeConversation]
  );

  const renameConversation = useCallback(
    async (id: string, newTitle: string) => {
      const cleaned = newTitle.trim().slice(0, 120);
      if (!cleaned) return;
      let target: Conversation | undefined;
      setConversations((prev) =>
        prev.map((conv) => {
          if (conv.id !== id) return conv;
          target = {
            ...conv,
            title: cleaned,
            autoTitleGenerated: true,
            updatedAt: Date.now()
          };
          return target;
        })
      );
      if (target) {
        await saveConversation(target);
      }
    },
    []
  );

  const deleteConversation = useCallback(
    async (id: string) => {
      setConversations((prev) => {
        const remaining = prev.filter((c) => c.id !== id);
        if (activeConversationId === id) {
          setActiveConversationId(remaining[0]?.id ?? null);
          if (remaining[0]) {
            setSelectedMode(remaining[0].mode);
          }
        }
        return remaining;
      });
      await deleteConversationById(id);
    },
    [activeConversationId]
  );

  const clearConversationMessages = useCallback(async (id: string) => {
    let updated: Conversation | undefined;
    setConversations((prev) =>
      prev.map((conv) => {
        if (conv.id !== id) return conv;
        updated = {
          ...conv,
          messages: [],
          updatedAt: Date.now(),
          metadata: {
            ...conv.metadata,
            messageCount: 0
          }
        };
        return updated;
      })
    );
    setChatError(null);
    if (updated) {
      await saveConversation(updated);
    }
  }, []);

  const clearAllConversations = useCallback(async () => {
    setConversations([]);
    setActiveConversationId(null);
    setChatError(null);
    await clearAllConversationsStorage();
  }, []);

  const sendMessage = useCallback(
    async (rawText: string) => {
      const trimmed = rawText.trim();
      if (!trimmed) {
        setChatError("Please enter a message before sending.");
        return;
      }
      if (isGenerating) return;

      setChatError(null);

      // Ensure we have an active conversation
      let targetConv = activeConversation;
      const modeToUse = targetConv ? targetConv.mode : selectedMode;
      if (!targetConv) {
        targetConv = createNewConversation(modeToUse);
      }

      const userMessage: ChatMessage = {
        id: makeId("msg"),
        role: "user",
        content: trimmed,
        timestamp: Date.now(),
        mode: modeToUse
      };

      const shouldAutoTitle =
        preferences.autoGenerateTitles &&
        !targetConv.autoTitleGenerated &&
        targetConv.messages.length === 0;

      const updatedWithUser: Conversation = {
        ...targetConv,
        title: shouldAutoTitle
          ? generateConversationTitle(trimmed, modeToUse)
          : targetConv.title,
        autoTitleGenerated: targetConv.autoTitleGenerated || shouldAutoTitle,
        messages: [...targetConv.messages, userMessage],
        updatedAt: Date.now(),
        metadata: {
          ...targetConv.metadata,
          messageCount: targetConv.messages.length + 1
        }
      };

      setConversations((prev) => {
        const exists = prev.some((c) => c.id === updatedWithUser.id);
        const nextList = exists
          ? prev.map((c) => (c.id === updatedWithUser.id ? updatedWithUser : c))
          : [updatedWithUser, ...prev];
        return [...nextList].sort((a, b) => b.updatedAt - a.updatedAt);
      });
      setActiveConversationId(updatedWithUser.id);
      await saveConversation(updatedWithUser);

      setIsGenerating(true);

      try {
        const result = await dispatchAiChat({
          backendUrl: preferences.customBackendUrl,
          message: trimmed,
          mode: modeToUse,
          history: targetConv.messages
        });

        const assistantMessage: ChatMessage = {
          id: makeId("msg"),
          role: "assistant",
          content: result.reply,
          timestamp: result.timestamp,
          mode: result.mode,
          isDemo: result.demo,
          model: result.model
        };

        const finalConv: Conversation = {
          ...updatedWithUser,
          messages: [...updatedWithUser.messages, assistantMessage],
          updatedAt: Date.now(),
          metadata: {
            messageCount: updatedWithUser.messages.length + 1,
            lastModel: result.model,
            demoOnly: result.demo
          }
        };

        setConversations((prev) =>
          prev
            .map((c) => (c.id === finalConv.id ? finalConv : c))
            .sort((a, b) => b.updatedAt - a.updatedAt)
        );
        await saveConversation(finalConv);

        if (!result.demo) {
          setBackendStatus({
            state: "connected",
            provider: result.provider,
            demoMode: false,
            model: result.model,
            message: `Connected to live Gemini AI (${result.model}).`,
            checkedAt: Date.now()
          });
        }
      } catch (error) {
        const errMessage =
          error instanceof Error ? error.message : "An unexpected error occurred.";
        setChatError(errMessage);
        setBackendStatus((prev) => ({
          ...prev,
          state: sanitizeBackendUrl(preferences.customBackendUrl) ? "error" : "demo",
          message: errMessage,
          checkedAt: Date.now()
        }));

        const errorNotice: ChatMessage = {
          id: makeId("err"),
          role: "assistant",
          content: `**Connection Error:** ${errMessage}\n\nYou can retry once your backend is running, or clear the Backend URL in **Settings** to continue in offline **Demo Mode**.`,
          timestamp: Date.now(),
          mode: modeToUse,
          isError: true
        };

        const convWithError: Conversation = {
          ...updatedWithUser,
          messages: [...updatedWithUser.messages, errorNotice],
          updatedAt: Date.now(),
          metadata: {
            ...updatedWithUser.metadata,
            messageCount: updatedWithUser.messages.length + 1
          }
        };

        setConversations((prev) =>
          prev.map((c) => (c.id === convWithError.id ? convWithError : c))
        );
        await saveConversation(convWithError);
      } finally {
        setIsGenerating(false);
      }
    },
    [
      activeConversation,
      isGenerating,
      preferences.autoGenerateTitles,
      preferences.customBackendUrl,
      selectedMode
    ]
  );

  const exportConversationsJson = useCallback(() => {
    const payload = {
      exportedAt: new Date().toISOString(),
      source: "VEYROX Web v1.0.0",
      conversations
    };
    const blob = new Blob([JSON.stringify(payload, null, 2)], {
      type: "application/json"
    });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `veyrox-conversations-${Date.now()}.json`;
    link.click();
    URL.revokeObjectURL(url);
  }, [conversations]);

  return {
    conversations,
    activeConversation,
    activeConversationId,
    selectedMode,
    preferences,
    isHydrated,
    isGenerating,
    chatError,
    backendStatus,
    setChatError,
    refreshBackendStatus,
    updatePreferences,
    createConversation,
    selectConversation,
    changeMode,
    renameConversation,
    deleteConversation,
    clearConversationMessages,
    clearAllConversations,
    sendMessage,
    exportConversationsJson
  };
}
