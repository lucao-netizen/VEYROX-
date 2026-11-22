import { AnimatePresence, motion } from "framer-motion";
import {
  AlertCircle,
  ArrowDown,
  Check,
  Copy,
  Eraser,
  Menu,
  MessageSquarePlus,
  Pencil,
  Search,
  Send,
  Sparkles,
  Trash2,
  X
} from "lucide-react";
import React, { useEffect, useMemo, useRef, useState } from "react";
import { AI_MODES } from "../config/veyrox";
import {
  AiModeId,
  BackendHealthStatus,
  ChatMessage,
  ChatPreferences,
  Conversation
} from "../types/chat";
import { DiscordButton } from "../components/DiscordButton";
import { MarkdownRenderer } from "../components/MarkdownRenderer";
import { ModeSelector, getModeIcon } from "../components/ModeSelector";
import { VeyroxLogo } from "../components/VeyroxLogo";

interface ChatPageProps {
  conversations: Conversation[];
  activeConversation: Conversation | null;
  selectedMode: AiModeId;
  preferences: ChatPreferences;
  isGenerating: boolean;
  chatError: string | null;
  backendStatus: BackendHealthStatus;
  onDismissError: () => void;
  onCreateConversation: (mode?: AiModeId) => Promise<Conversation>;
  onSelectConversation: (id: string) => void;
  onSelectMode: (mode: AiModeId) => Promise<void>;
  onRenameConversation: (id: string, title: string) => Promise<void>;
  onDeleteConversation: (id: string) => Promise<void>;
  onClearConversationMessages: (id: string) => Promise<void>;
  onSendMessage: (text: string) => Promise<void>;
  onOpenSettings: () => void;
}

function formatTime(ts: number): string {
  try {
    return new Date(ts).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
  } catch {
    return "";
  }
}

function formatShortDate(ts: number): string {
  try {
    const date = new Date(ts);
    const now = new Date();
    const sameDay = date.toDateString() === now.toDateString();
    return sameDay
      ? date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })
      : date.toLocaleDateString([], { month: "short", day: "numeric" });
  } catch {
    return "";
  }
}

export const ChatPage: React.FC<ChatPageProps> = ({
  conversations,
  activeConversation,
  selectedMode,
  preferences,
  isGenerating,
  chatError,
  backendStatus,
  onDismissError,
  onCreateConversation,
  onSelectConversation,
  onSelectMode,
  onRenameConversation,
  onDeleteConversation,
  onClearConversationMessages,
  onSendMessage,
  onOpenSettings
}) => {
  const [inputText, setInputText] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [mobileDrawerOpen, setMobileDrawerOpen] = useState(false);
  const [editingConvId, setEditingConvId] = useState<string | null>(null);
  const [editingTitle, setEditingTitle] = useState("");
  const [copiedMessageId, setCopiedMessageId] = useState<string | null>(null);
  const [showScrollBottom, setShowScrollBottom] = useState(false);

  const messagesEndRef = useRef<HTMLDivElement | null>(null);
  const scrollContainerRef = useRef<HTMLDivElement | null>(null);
  const textareaRef = useRef<HTMLTextAreaElement | null>(null);

  const activeModeDef = AI_MODES[activeConversation?.mode ?? selectedMode];
  const messages: ChatMessage[] = activeConversation?.messages ?? [];

  const filteredConversations = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();
    if (!q) return conversations;
    return conversations.filter(
      (conv) =>
        conv.title.toLowerCase().includes(q) ||
        conv.mode.toLowerCase().includes(q) ||
        conv.messages.some((m) => m.content.toLowerCase().includes(q))
    );
  }, [conversations, searchQuery]);

  // Auto-scroll on new message or typing indicator
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages.length, isGenerating, activeConversation?.id]);

  // Auto-resize textarea
  useEffect(() => {
    const el = textareaRef.current;
    if (!el) return;
    el.style.height = "auto";
    el.style.height = `${Math.min(el.scrollHeight, 160)}px`;
  }, [inputText]);

  const handleScroll = () => {
    const el = scrollContainerRef.current;
    if (!el) return;
    const distanceFromBottom = el.scrollHeight - el.scrollTop - el.clientHeight;
    setShowScrollBottom(distanceFromBottom > 180);
  };

  const handleStartRename = (conv: Conversation) => {
    setEditingConvId(conv.id);
    setEditingTitle(conv.title);
  };

  const handleCommitRename = async () => {
    if (editingConvId && editingTitle.trim()) {
      await onRenameConversation(editingConvId, editingTitle);
    }
    setEditingConvId(null);
    setEditingTitle("");
  };

  const handleSubmit = async (e?: React.FormEvent) => {
    e?.preventDefault();
    const trimmed = inputText.trim();
    if (!trimmed || isGenerating) return;
    setInputText("");
    await onSendMessage(trimmed);
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (preferences.enterToSend && e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      void handleSubmit();
    }
  };

  const handleCopyMessage = async (msg: ChatMessage) => {
    try {
      await navigator.clipboard.writeText(msg.content);
      setCopiedMessageId(msg.id);
      setTimeout(() => setCopiedMessageId(null), 1800);
    } catch {
      // Clipboard unavailable
    }
  };

  const statusBadge = (() => {
    if (backendStatus.state === "connected" && !backendStatus.demoMode) {
      return {
        label: `LIVE GEMINI • ${backendStatus.model || "gemini"}`,
        classes: "border-emerald-500/35 bg-emerald-500/10 text-emerald-400"
      };
    }
    if (backendStatus.state === "connected" && backendStatus.demoMode) {
      return {
        label: "BACKEND DEMO MODE",
        classes: "border-amber-500/35 bg-amber-500/10 text-amber-400"
      };
    }
    if (backendStatus.state === "error") {
      return {
        label: "BACKEND UNAVAILABLE",
        classes: "border-rose-500/35 bg-rose-500/10 text-rose-400"
      };
    }
    return {
      label: "DEMO MODE",
      classes: "border-amber-500/35 bg-amber-500/10 text-amber-400"
    };
  })();

  const renderSidebarContent = () => (
    <div className="flex h-full flex-col">
      <div className="p-3.5 border-b border-[var(--vx-border)] space-y-2.5">
        <button
          type="button"
          onClick={() => {
            void onCreateConversation(selectedMode);
            setMobileDrawerOpen(false);
          }}
          className="w-full inline-flex items-center justify-center gap-2 rounded-xl bg-[var(--vx-primary)] hover:bg-[var(--vx-primary-bright)] px-4 py-2.5 text-sm font-display font-semibold text-white shadow-[0_10px_24px_-8px_rgba(52,120,255,0.7)] transition-all cursor-pointer min-h-[44px]"
        >
          <MessageSquarePlus className="w-4 h-4" />
          <span>New Chat</span>
        </button>

        <div className="relative">
          <Search className="w-4 h-4 text-[var(--vx-text-subtle)] absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search conversations..."
            aria-label="Search conversations"
            className="w-full rounded-xl border border-[var(--vx-border)] bg-[var(--vx-bg)] pl-9 pr-3 py-2 text-xs text-[var(--vx-text)] placeholder:text-[var(--vx-text-subtle)] focus:border-[var(--vx-primary)] focus:outline-none min-h-[38px]"
          />
        </div>
      </div>

      <div className="flex-1 overflow-y-auto p-2.5 space-y-1">
        {filteredConversations.length === 0 ? (
          <div className="rounded-xl border border-dashed border-[var(--vx-border)] p-4 text-center">
            <p className="text-xs text-[var(--vx-text-muted)]">
              {conversations.length === 0
                ? "No saved chats yet. Send a message to begin."
                : "No conversations match your search."}
            </p>
          </div>
        ) : (
          filteredConversations.map((conv) => {
            const isActive = conv.id === activeConversation?.id;
            const isEditing = editingConvId === conv.id;

            return (
              <div
                key={conv.id}
                className={`group rounded-xl border p-2.5 transition-colors ${
                  isActive
                    ? "border-[var(--vx-border-strong)] bg-[var(--vx-primary-soft)]"
                    : "border-transparent hover:border-[var(--vx-border)] hover:bg-[var(--vx-surface-hover)]"
                }`}
              >
                {isEditing ? (
                  <div className="flex items-center gap-1.5">
                    <input
                      type="text"
                      value={editingTitle}
                      onChange={(e) => setEditingTitle(e.target.value)}
                      onKeyDown={(e) => {
                        if (e.key === "Enter") void handleCommitRename();
                        if (e.key === "Escape") setEditingConvId(null);
                      }}
                      autoFocus
                      aria-label="Rename conversation"
                      className="flex-1 min-w-0 rounded-lg border border-[var(--vx-primary)] bg-[var(--vx-bg)] px-2 py-1 text-xs text-[var(--vx-text)] focus:outline-none"
                    />
                    <button
                      type="button"
                      onClick={() => void handleCommitRename()}
                      aria-label="Save title"
                      className="inline-flex h-7 w-7 items-center justify-center rounded-lg bg-[var(--vx-primary)] text-white cursor-pointer"
                    >
                      <Check className="w-3.5 h-3.5" />
                    </button>
                    <button
                      type="button"
                      onClick={() => setEditingConvId(null)}
                      aria-label="Cancel rename"
                      className="inline-flex h-7 w-7 items-center justify-center rounded-lg border border-[var(--vx-border)] text-[var(--vx-text-muted)] cursor-pointer"
                    >
                      <X className="w-3.5 h-3.5" />
                    </button>
                  </div>
                ) : (
                  <div className="flex items-start justify-between gap-2">
                    <button
                      type="button"
                      onClick={() => {
                        onSelectConversation(conv.id);
                        setMobileDrawerOpen(false);
                      }}
                      className="flex-1 min-w-0 text-left cursor-pointer"
                    >
                      <div className="flex items-center gap-1.5">
                        <span className="text-[var(--vx-primary-bright)] shrink-0">
                          {getModeIcon(conv.mode, "w-3.5 h-3.5")}
                        </span>
                        <span className="truncate text-xs font-semibold text-[var(--vx-text)]">
                          {conv.title}
                        </span>
                      </div>
                      <div className="mt-1 flex items-center gap-2 text-[10px] text-[var(--vx-text-subtle)]">
                        <span className="font-mono-code uppercase">{conv.mode}</span>
                        <span>•</span>
                        <span>{conv.messages.length} msgs</span>
                        <span>•</span>
                        <span>{formatShortDate(conv.updatedAt)}</span>
                      </div>
                    </button>

                    <div className="flex items-center gap-0.5 opacity-80 sm:opacity-0 sm:group-hover:opacity-100 transition-opacity shrink-0">
                      <button
                        type="button"
                        onClick={() => handleStartRename(conv)}
                        aria-label={`Rename ${conv.title}`}
                        title="Rename conversation"
                        className="inline-flex h-7 w-7 items-center justify-center rounded-lg text-[var(--vx-text-muted)] hover:bg-[var(--vx-surface)] hover:text-[var(--vx-text)] cursor-pointer"
                      >
                        <Pencil className="w-3.5 h-3.5" />
                      </button>
                      <button
                        type="button"
                        onClick={() => void onDeleteConversation(conv.id)}
                        aria-label={`Delete ${conv.title}`}
                        title="Delete conversation"
                        className="inline-flex h-7 w-7 items-center justify-center rounded-lg text-[var(--vx-text-muted)] hover:bg-rose-500/15 hover:text-rose-400 cursor-pointer"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </div>
                )}
              </div>
            );
          })
        )}
      </div>

      <div className="border-t border-[var(--vx-border)] p-3 space-y-2">
        <DiscordButton label="Join VEYROX Discord" variant="outline" className="w-full text-xs py-2" />
      </div>
    </div>
  );

  return (
    <div className="relative z-10 mx-auto max-w-7xl px-2 py-3 sm:px-6 sm:py-5 lg:px-8">
      <div className="flex h-[calc(100dvh-6.25rem)] min-h-[540px] overflow-hidden rounded-3xl border border-[var(--vx-border)] bg-[var(--vx-bg-elevated)] shadow-[var(--vx-shadow)]">
        {/* Desktop Sidebar */}
        <aside className="hidden lg:flex w-72 shrink-0 flex-col border-r border-[var(--vx-border)] bg-[var(--vx-surface)]/60">
          {renderSidebarContent()}
        </aside>

        {/* Mobile Slide-out History Drawer */}
        <AnimatePresence>
          {mobileDrawerOpen && (
            <>
              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                onClick={() => setMobileDrawerOpen(false)}
                className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm lg:hidden"
              />
              <motion.aside
                initial={{ x: "-100%" }}
                animate={{ x: 0 }}
                exit={{ x: "-100%" }}
                transition={{ type: "spring", stiffness: 320, damping: 32 }}
                className="fixed inset-y-0 left-0 z-50 w-80 max-w-[86vw] border-r border-[var(--vx-border)] bg-[var(--vx-bg-elevated)] lg:hidden flex flex-col"
              >
                <div className="flex items-center justify-between border-b border-[var(--vx-border)] px-4 py-3">
                  <VeyroxLogo size={26} />
                  <button
                    type="button"
                    onClick={() => setMobileDrawerOpen(false)}
                    aria-label="Close chat history drawer"
                    className="inline-flex h-10 w-10 items-center justify-center rounded-xl border border-[var(--vx-border)] text-[var(--vx-text)] cursor-pointer"
                  >
                    <X className="w-5 h-5" />
                  </button>
                </div>
                <div className="flex-1 overflow-hidden">{renderSidebarContent()}</div>
              </motion.aside>
            </>
          )}
        </AnimatePresence>

        {/* Main Chat Pane */}
        <div className="flex min-w-0 flex-1 flex-col">
          {/* Chat Workspace Top Bar */}
          <div className="border-b border-[var(--vx-border)] bg-[var(--vx-surface)]/50 px-3 py-3 sm:px-5">
            <div className="flex flex-wrap items-center justify-between gap-2.5">
              <div className="flex items-center gap-2 min-w-0">
                <button
                  type="button"
                  onClick={() => setMobileDrawerOpen(true)}
                  aria-label="Open conversation history"
                  className="lg:hidden inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-xl border border-[var(--vx-border)] bg-[var(--vx-surface)] text-[var(--vx-text)] cursor-pointer"
                >
                  <Menu className="w-5 h-5" />
                </button>
                <div className="min-w-0">
                  <div className="flex items-center gap-2">
                    <h1 className="truncate font-display text-sm sm:text-base font-bold text-[var(--vx-text)]">
                      {activeConversation?.title || `New ${activeModeDef.name} Chat`}
                    </h1>
                    {activeConversation && (
                      <button
                        type="button"
                        onClick={() => handleStartRename(activeConversation)}
                        aria-label="Rename active conversation"
                        className="hidden sm:inline-flex h-7 w-7 items-center justify-center rounded-lg text-[var(--vx-text-muted)] hover:bg-[var(--vx-surface)] hover:text-[var(--vx-text)] cursor-pointer"
                      >
                        <Pencil className="w-3.5 h-3.5" />
                      </button>
                    )}
                  </div>
                  <p className="truncate text-[11px] text-[var(--vx-text-muted)]">
                    {activeModeDef.description}
                  </p>
                </div>
              </div>

              <div className="flex flex-wrap items-center gap-2">
                <button
                  type="button"
                  onClick={onOpenSettings}
                  className={`inline-flex items-center gap-1.5 rounded-full border px-2.5 py-1 font-mono-code text-[10px] font-medium tracking-wider cursor-pointer ${statusBadge.classes}`}
                  title={backendStatus.message}
                >
                  <span className="h-1.5 w-1.5 rounded-full bg-current" />
                  <span>{statusBadge.label}</span>
                </button>

                {activeConversation && messages.length > 0 && (
                  <button
                    type="button"
                    onClick={() => void onClearConversationMessages(activeConversation.id)}
                    title="Clear messages in this conversation"
                    className="inline-flex items-center gap-1 rounded-xl border border-[var(--vx-border)] bg-[var(--vx-surface)] px-2.5 py-1.5 text-xs text-[var(--vx-text-muted)] hover:text-[var(--vx-text)] cursor-pointer min-h-[34px]"
                  >
                    <Eraser className="w-3.5 h-3.5" />
                    <span className="hidden sm:inline">Clear</span>
                  </button>
                )}
              </div>
            </div>

            <div className="mt-3 flex flex-wrap items-center justify-between gap-2">
              <ModeSelector
                selectedMode={activeModeDef.id}
                onSelectMode={(mode) => {
                  void onSelectMode(mode);
                }}
              />
            </div>
          </div>

          {/* Messages Stream */}
          <div
            ref={scrollContainerRef}
            onScroll={handleScroll}
            className="relative flex-1 overflow-y-auto px-3 py-5 sm:px-6 space-y-5"
          >
            {messages.length === 0 ? (
              <div className="mx-auto flex h-full max-w-2xl flex-col items-center justify-center text-center py-8">
                <div className="mb-4 inline-flex h-14 w-14 items-center justify-center rounded-2xl border border-[var(--vx-border-strong)] bg-[var(--vx-primary-soft)] text-[var(--vx-primary-bright)] shadow-[0_0_30px_-10px_rgba(52,120,255,0.6)]">
                  {getModeIcon(activeModeDef.id, "w-7 h-7")}
                </div>
                <h2 className="font-display text-2xl font-bold text-[var(--vx-text)]">
                  {activeModeDef.name}
                </h2>
                <p className="mt-2 max-w-md text-sm text-[var(--vx-text-muted)]">
                  {activeModeDef.description}
                </p>

                {backendStatus.demoMode && (
                  <div className="mt-4 rounded-xl border border-amber-500/30 bg-amber-500/10 px-4 py-2 text-xs text-amber-300">
                    <strong>Demo Mode Active:</strong> Previewing offline VEYROX responses. Connect a deployed backend URL in Settings for live Gemini AI.
                  </div>
                )}

                <div className="mt-7 grid w-full grid-cols-1 gap-2.5 sm:grid-cols-3 text-left">
                  {activeModeDef.starterPrompts.map((prompt) => (
                    <button
                      key={prompt}
                      type="button"
                      onClick={() => void onSendMessage(prompt)}
                      className="rounded-2xl border border-[var(--vx-border)] bg-[var(--vx-surface)] p-3.5 text-xs leading-relaxed text-[var(--vx-text-muted)] hover:border-[var(--vx-border-strong)] hover:text-[var(--vx-text)] transition-all cursor-pointer"
                    >
                      <Sparkles className="w-3.5 h-3.5 text-[var(--vx-primary-bright)] mb-1.5" />
                      <span>{prompt}</span>
                    </button>
                  ))}
                </div>
              </div>
            ) : (
              messages.map((msg) => {
                const isUser = msg.role === "user";
                return (
                  <motion.div
                    key={msg.id}
                    initial={{ opacity: 0, y: 8 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.2 }}
                    className={`flex ${isUser ? "justify-end" : "justify-start"}`}
                  >
                    <div
                      className={`max-w-[92%] sm:max-w-[82%] rounded-2xl px-4 py-3.5 ${
                        isUser
                          ? "bg-[var(--vx-primary)] text-white shadow-[0_10px_28px_-12px_rgba(52,120,255,0.8)]"
                          : msg.isError
                          ? "border border-rose-500/40 bg-rose-500/10 text-[var(--vx-text)]"
                          : "border border-[var(--vx-border)] bg-[var(--vx-surface)] text-[var(--vx-text)]"
                      }`}
                    >
                      <div className="mb-1.5 flex items-center justify-between gap-4 text-[11px]">
                        <div className="flex items-center gap-2">
                          <span
                            className={`font-display font-semibold ${
                              isUser ? "text-blue-100" : "text-[var(--vx-primary-bright)]"
                            }`}
                          >
                            {isUser ? "You" : "VEYROX"}
                          </span>
                          {!isUser && msg.isDemo && !msg.isError && (
                            <span className="rounded-full border border-amber-500/35 bg-amber-500/15 px-2 py-0.5 font-mono-code text-[9px] uppercase tracking-wider text-amber-400">
                              DEMO
                            </span>
                          )}
                          {!isUser && !msg.isDemo && msg.model && !msg.isError && (
                            <span className="rounded-full border border-emerald-500/30 bg-emerald-500/10 px-2 py-0.5 font-mono-code text-[9px] text-emerald-400">
                              {msg.model}
                            </span>
                          )}
                        </div>

                        <div className="flex items-center gap-2">
                          {preferences.showTimestamps && (
                            <span
                              className={`font-mono-code text-[10px] ${
                                isUser ? "text-blue-100/80" : "text-[var(--vx-text-subtle)]"
                              }`}
                            >
                              {formatTime(msg.timestamp)}
                            </span>
                          )}
                          <button
                            type="button"
                            onClick={() => void handleCopyMessage(msg)}
                            aria-label="Copy message"
                            title="Copy message"
                            className={`inline-flex h-6 w-6 items-center justify-center rounded-md transition-colors cursor-pointer ${
                              isUser
                                ? "text-blue-100 hover:bg-white/15"
                                : "text-[var(--vx-text-muted)] hover:bg-[var(--vx-surface-hover)] hover:text-[var(--vx-text)]"
                            }`}
                          >
                            {copiedMessageId === msg.id ? (
                              <Check className="w-3.5 h-3.5 text-emerald-400" />
                            ) : (
                              <Copy className="w-3.5 h-3.5" />
                            )}
                          </button>
                        </div>
                      </div>

                      {isUser ? (
                        <p className="whitespace-pre-wrap text-sm leading-relaxed">{msg.content}</p>
                      ) : (
                        <MarkdownRenderer content={msg.content} />
                      )}
                    </div>
                  </motion.div>
                );
              })
            )}

            {isGenerating && (
              <div className="flex justify-start">
                <div className="inline-flex items-center gap-2.5 rounded-2xl border border-[var(--vx-border)] bg-[var(--vx-surface)] px-4 py-3 text-xs text-[var(--vx-text-muted)]">
                  <span className="flex gap-1">
                    <span className="h-2 w-2 rounded-full bg-[var(--vx-primary-bright)] animate-bounce [animation-delay:-0.3s]" />
                    <span className="h-2 w-2 rounded-full bg-[var(--vx-primary-bright)] animate-bounce [animation-delay:-0.15s]" />
                    <span className="h-2 w-2 rounded-full bg-[var(--vx-primary-bright)] animate-bounce" />
                  </span>
                  <span>VEYROX ({activeModeDef.shortLabel}) is thinking...</span>
                </div>
              </div>
            )}

            <div ref={messagesEndRef} />

            {showScrollBottom && (
              <button
                type="button"
                onClick={() => messagesEndRef.current?.scrollIntoView({ behavior: "smooth" })}
                aria-label="Scroll to latest message"
                className="sticky bottom-3 left-1/2 -translate-x-1/2 inline-flex items-center gap-1.5 rounded-full border border-[var(--vx-border-strong)] bg-[var(--vx-bg-elevated)] px-3.5 py-1.5 text-xs font-medium text-[var(--vx-text)] shadow-lg cursor-pointer"
              >
                <ArrowDown className="w-3.5 h-3.5 text-[var(--vx-primary-bright)]" />
                <span>Latest</span>
              </button>
            )}
          </div>

          {/* Error Banner */}
          <AnimatePresence>
            {chatError && (
              <motion.div
                initial={{ opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: "auto" }}
                exit={{ opacity: 0, height: 0 }}
                className="border-t border-rose-500/30 bg-rose-500/10 px-4 py-2.5 flex items-center justify-between gap-3 text-xs text-rose-300"
              >
                <div className="flex items-center gap-2 min-w-0">
                  <AlertCircle className="w-4 h-4 shrink-0 text-rose-400" />
                  <span className="truncate">{chatError}</span>
                </div>
                <button
                  type="button"
                  onClick={onDismissError}
                  aria-label="Dismiss error"
                  className="shrink-0 rounded-lg p-1 hover:bg-rose-500/20 cursor-pointer"
                >
                  <X className="w-3.5 h-3.5" />
                </button>
              </motion.div>
            )}
          </AnimatePresence>

          {/* Composer Input Bar */}
          <form
            onSubmit={(e) => void handleSubmit(e)}
            className="border-t border-[var(--vx-border)] bg-[var(--vx-surface)]/60 p-3 sm:p-4"
          >
            <div className="flex items-end gap-2.5 rounded-2xl border border-[var(--vx-border)] bg-[var(--vx-bg)] p-2 focus-within:border-[var(--vx-primary)] transition-colors">
              <textarea
                ref={textareaRef}
                rows={1}
                value={inputText}
                onChange={(e) => setInputText(e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder={`Message VEYROX (${activeModeDef.name})...`}
                aria-label="Message VEYROX"
                className="max-h-40 flex-1 resize-none bg-transparent px-2.5 py-2 text-sm text-[var(--vx-text)] placeholder:text-[var(--vx-text-subtle)] focus:outline-none"
              />
              <button
                type="submit"
                disabled={!inputText.trim() || isGenerating}
                aria-label="Send message"
                className="inline-flex h-11 min-w-[44px] items-center justify-center gap-1.5 rounded-xl bg-[var(--vx-primary)] hover:bg-[var(--vx-primary-bright)] disabled:opacity-40 disabled:cursor-not-allowed px-4 font-display text-xs font-semibold text-white transition-all cursor-pointer"
              >
                <Send className="w-4 h-4" />
                <span className="hidden sm:inline">{isGenerating ? "Sending" : "Send"}</span>
              </button>
            </div>
            <div className="mt-1.5 flex items-center justify-between px-1 text-[11px] text-[var(--vx-text-subtle)]">
              <span>
                {preferences.enterToSend
                  ? "Enter to send • Shift + Enter for new line"
                  : "Click Send to submit • Enter inserts new line"}
              </span>
              <span className="font-mono-code">{activeModeDef.id}</span>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};
