import {
  CheckCircle2,
  Database,
  Download,
  Info,
  Moon,
  RefreshCw,
  Server,
  Shield,
  Sliders,
  Sun,
  Trash2
} from "lucide-react";
import React, { useState } from "react";
import { AI_MODE_LIST, VEYROX_DISCORD_URL, VEYROX_VERSION } from "../config/veyrox";
import { sanitizeBackendUrl } from "../services/aiService";
import {
  AiModeId,
  BackendHealthStatus,
  ChatPreferences,
  Conversation
} from "../types/chat";
import { DiscordButton } from "../components/DiscordButton";

interface SettingsPageProps {
  preferences: ChatPreferences;
  conversations: Conversation[];
  backendStatus: BackendHealthStatus;
  onUpdatePreferences: (partial: Partial<ChatPreferences>) => Promise<void>;
  onRefreshBackend: (url?: string) => Promise<void>;
  onExportConversations: () => void;
  onClearAllConversations: () => Promise<void>;
}

export const SettingsPage: React.FC<SettingsPageProps> = ({
  preferences,
  conversations,
  backendStatus,
  onUpdatePreferences,
  onRefreshBackend,
  onExportConversations,
  onClearAllConversations
}) => {
  const [backendInput, setBackendInput] = useState(preferences.customBackendUrl);
  const [confirmClear, setConfirmClear] = useState(false);
  const [savedToast, setSavedToast] = useState<string | null>(null);

  const totalMessages = conversations.reduce((sum, c) => sum + c.messages.length, 0);

  const flashNotice = (msg: string) => {
    setSavedToast(msg);
    setTimeout(() => setSavedToast(null), 2600);
  };

  const handleSaveBackendUrl = async (e: React.FormEvent) => {
    e.preventDefault();
    const sanitized = sanitizeBackendUrl(backendInput);
    if (backendInput.trim() && !sanitized) {
      flashNotice("Enter a valid http:// or https:// URL, or leave blank for Demo Mode.");
      return;
    }
    setBackendInput(sanitized);
    await onUpdatePreferences({ customBackendUrl: sanitized });
    flashNotice(sanitized ? "Backend URL updated and tested." : "Switched to Demo Mode.");
  };

  const handleResetDemo = async () => {
    setBackendInput("");
    await onUpdatePreferences({ customBackendUrl: "" });
    flashNotice("Switched to offline Demo Mode.");
  };

  const handleConfirmClearAll = async () => {
    await onClearAllConversations();
    setConfirmClear(false);
    flashNotice("All local conversations cleared from IndexedDB.");
  };

  return (
    <div className="relative z-10 mx-auto max-w-4xl px-4 py-10 sm:px-6 sm:py-14 space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <span className="font-mono-code text-xs uppercase tracking-[0.2em] text-[var(--vx-primary-bright)]">
            WORKSPACE SETTINGS
          </span>
          <h1 className="mt-1 font-display text-3xl font-bold text-[var(--vx-text)]">
            Settings & Preferences
          </h1>
        </div>
        {savedToast && (
          <div className="inline-flex items-center gap-2 rounded-xl border border-emerald-500/35 bg-emerald-500/10 px-3.5 py-2 text-xs text-emerald-400">
            <CheckCircle2 className="w-4 h-4" />
            <span>{savedToast}</span>
          </div>
        )}
      </div>

      {/* Appearance */}
      <section className="rounded-2xl border border-[var(--vx-border)] bg-[var(--vx-surface)] p-5 sm:p-6 space-y-4">
        <div className="flex items-center gap-2.5">
          <Sliders className="w-5 h-5 text-[var(--vx-primary-bright)]" />
          <h2 className="font-display text-lg font-bold text-[var(--vx-text)]">Appearance</h2>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <button
            type="button"
            onClick={() => void onUpdatePreferences({ theme: "dark" })}
            aria-pressed={preferences.theme === "dark"}
            className={`flex items-center gap-3 rounded-xl border p-4 text-left transition-all cursor-pointer min-h-[60px] ${
              preferences.theme === "dark"
                ? "border-[var(--vx-primary)] bg-[var(--vx-primary-soft)]"
                : "border-[var(--vx-border)] bg-[var(--vx-bg-elevated)] hover:border-[var(--vx-border-strong)]"
            }`}
          >
            <Moon className="w-5 h-5 text-[var(--vx-primary-bright)]" />
            <div>
              <p className="font-display text-sm font-semibold text-[var(--vx-text)]">
                VEYROX Dark
              </p>
              <p className="text-xs text-[var(--vx-text-muted)]">
                Deep black & anthracite with electric-blue highlights
              </p>
            </div>
          </button>

          <button
            type="button"
            onClick={() => void onUpdatePreferences({ theme: "light" })}
            aria-pressed={preferences.theme === "light"}
            className={`flex items-center gap-3 rounded-xl border p-4 text-left transition-all cursor-pointer min-h-[60px] ${
              preferences.theme === "light"
                ? "border-[var(--vx-primary)] bg-[var(--vx-primary-soft)]"
                : "border-[var(--vx-border)] bg-[var(--vx-bg-elevated)] hover:border-[var(--vx-border-strong)]"
            }`}
          >
            <Sun className="w-5 h-5 text-[var(--vx-primary-bright)]" />
            <div>
              <p className="font-display text-sm font-semibold text-[var(--vx-text)]">
                VEYROX Light
              </p>
              <p className="text-xs text-[var(--vx-text-muted)]">
                High-clarity daylight canvas with crisp slate typography
              </p>
            </div>
          </button>
        </div>
      </section>

      {/* Chat Preferences */}
      <section className="rounded-2xl border border-[var(--vx-border)] bg-[var(--vx-surface)] p-5 sm:p-6 space-y-4">
        <h2 className="font-display text-lg font-bold text-[var(--vx-text)]">Chat Preferences</h2>

        <div className="space-y-3">
          <label className="flex items-center justify-between gap-4 rounded-xl border border-[var(--vx-border)] bg-[var(--vx-bg-elevated)] p-3.5 cursor-pointer">
            <div>
              <p className="text-sm font-semibold text-[var(--vx-text)]">Enter to send</p>
              <p className="text-xs text-[var(--vx-text-muted)]">
                Press Enter to send messages (Shift + Enter adds a new line)
              </p>
            </div>
            <input
              type="checkbox"
              checked={preferences.enterToSend}
              onChange={(e) => void onUpdatePreferences({ enterToSend: e.target.checked })}
              className="h-5 w-5 accent-[var(--vx-primary)] cursor-pointer"
            />
          </label>

          <label className="flex items-center justify-between gap-4 rounded-xl border border-[var(--vx-border)] bg-[var(--vx-bg-elevated)] p-3.5 cursor-pointer">
            <div>
              <p className="text-sm font-semibold text-[var(--vx-text)]">Show message timestamps</p>
              <p className="text-xs text-[var(--vx-text-muted)]">
                Display send/receive time inside every chat bubble
              </p>
            </div>
            <input
              type="checkbox"
              checked={preferences.showTimestamps}
              onChange={(e) => void onUpdatePreferences({ showTimestamps: e.target.checked })}
              className="h-5 w-5 accent-[var(--vx-primary)] cursor-pointer"
            />
          </label>

          <label className="flex items-center justify-between gap-4 rounded-xl border border-[var(--vx-border)] bg-[var(--vx-bg-elevated)] p-3.5 cursor-pointer">
            <div>
              <p className="text-sm font-semibold text-[var(--vx-text)]">
                Auto-generate conversation titles
              </p>
              <p className="text-xs text-[var(--vx-text-muted)]">
                Automatically title new conversations from your first prompt
              </p>
            </div>
            <input
              type="checkbox"
              checked={preferences.autoGenerateTitles}
              onChange={(e) =>
                void onUpdatePreferences({ autoGenerateTitles: e.target.checked })
              }
              className="h-5 w-5 accent-[var(--vx-primary)] cursor-pointer"
            />
          </label>

          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 rounded-xl border border-[var(--vx-border)] bg-[var(--vx-bg-elevated)] p-3.5">
            <div>
              <p className="text-sm font-semibold text-[var(--vx-text)]">Default AI Mode</p>
              <p className="text-xs text-[var(--vx-text-muted)]">
                Mode selected automatically when starting a fresh workspace
              </p>
            </div>
            <select
              value={preferences.defaultMode}
              onChange={(e) =>
                void onUpdatePreferences({ defaultMode: e.target.value as AiModeId })
              }
              aria-label="Default AI Mode"
              className="rounded-xl border border-[var(--vx-border)] bg-[var(--vx-surface)] px-3 py-2 text-sm text-[var(--vx-text)] focus:border-[var(--vx-primary)] focus:outline-none min-h-[42px]"
            >
              {AI_MODE_LIST.map((mode) => (
                <option key={mode.id} value={mode.id}>
                  {mode.name}
                </option>
              ))}
            </select>
          </div>
        </div>
      </section>

      {/* Backend Connection */}
      <section className="rounded-2xl border border-[var(--vx-border)] bg-[var(--vx-surface)] p-5 sm:p-6 space-y-4">
        <div className="flex items-center justify-between gap-2">
          <div className="flex items-center gap-2.5">
            <Server className="w-5 h-5 text-[var(--vx-primary-bright)]" />
            <h2 className="font-display text-lg font-bold text-[var(--vx-text)]">
              AI Backend Connection
            </h2>
          </div>
          <span
            className={`rounded-full border px-2.5 py-0.5 font-mono-code text-[10px] uppercase tracking-wider ${
              backendStatus.state === "connected" && !backendStatus.demoMode
                ? "border-emerald-500/35 bg-emerald-500/10 text-emerald-400"
                : backendStatus.state === "error"
                ? "border-rose-500/35 bg-rose-500/10 text-rose-400"
                : "border-amber-500/35 bg-amber-500/10 text-amber-400"
            }`}
          >
            {backendStatus.state === "connected" && !backendStatus.demoMode
              ? "LIVE GEMINI"
              : backendStatus.state === "error"
              ? "ERROR"
              : "DEMO MODE"}
          </span>
        </div>

        <p className="text-xs leading-relaxed text-[var(--vx-text-muted)]">
          Leave empty to run VEYROX in transparent offline <strong>Demo Mode</strong>, or enter your deployed Node.js + Express backend URL (e.g., <code className="font-mono-code">http://localhost:8787</code>). Never enter API keys here — keep <code className="font-mono-code">GEMINI_API_KEY</code> strictly on the backend server.
        </p>

        <form onSubmit={(e) => void handleSaveBackendUrl(e)} className="flex flex-col sm:flex-row gap-2.5">
          <input
            type="url"
            value={backendInput}
            onChange={(e) => setBackendInput(e.target.value)}
            placeholder="https://your-veyrox-backend.example.com"
            aria-label="VEYROX Backend URL"
            className="flex-1 rounded-xl border border-[var(--vx-border)] bg-[var(--vx-bg)] px-3.5 py-2.5 text-sm text-[var(--vx-text)] placeholder:text-[var(--vx-text-subtle)] focus:border-[var(--vx-primary)] focus:outline-none min-h-[44px]"
          />
          <button
            type="submit"
            className="rounded-xl bg-[var(--vx-primary)] hover:bg-[var(--vx-primary-bright)] px-5 py-2.5 text-sm font-display font-semibold text-white cursor-pointer min-h-[44px]"
          >
            Save & Test
          </button>
          <button
            type="button"
            onClick={() => void onRefreshBackend(backendInput)}
            className="inline-flex items-center justify-center gap-1.5 rounded-xl border border-[var(--vx-border)] bg-[var(--vx-bg-elevated)] px-4 py-2.5 text-sm text-[var(--vx-text)] hover:border-[var(--vx-border-strong)] cursor-pointer min-h-[44px]"
          >
            <RefreshCw className="w-4 h-4" />
            <span>Check</span>
          </button>
        </form>

        <div className="flex flex-wrap items-center justify-between gap-2 rounded-xl border border-[var(--vx-border)] bg-[var(--vx-bg-elevated)] px-3.5 py-2.5 text-xs text-[var(--vx-text-muted)]">
          <span>{backendStatus.message}</span>
          {preferences.customBackendUrl && (
            <button
              type="button"
              onClick={() => void handleResetDemo()}
              className="text-[var(--vx-primary-bright)] hover:underline font-medium cursor-pointer"
            >
              Reset to Demo Mode
            </button>
          )}
        </div>
      </section>

      {/* Conversation Storage Management */}
      <section className="rounded-2xl border border-[var(--vx-border)] bg-[var(--vx-surface)] p-5 sm:p-6 space-y-4">
        <div className="flex items-center gap-2.5">
          <Database className="w-5 h-5 text-[var(--vx-primary-bright)]" />
          <h2 className="font-display text-lg font-bold text-[var(--vx-text)]">
            Conversation History (IndexedDB)
          </h2>
        </div>

        <div className="grid grid-cols-2 gap-3">
          <div className="rounded-xl border border-[var(--vx-border)] bg-[var(--vx-bg-elevated)] p-3.5">
            <p className="font-mono-code text-[11px] uppercase text-[var(--vx-text-subtle)]">
              Saved Threads
            </p>
            <p className="mt-1 font-display text-2xl font-bold text-[var(--vx-text)]">
              {conversations.length}
            </p>
          </div>
          <div className="rounded-xl border border-[var(--vx-border)] bg-[var(--vx-bg-elevated)] p-3.5">
            <p className="font-mono-code text-[11px] uppercase text-[var(--vx-text-subtle)]">
              Stored Messages
            </p>
            <p className="mt-1 font-display text-2xl font-bold text-[var(--vx-text)]">
              {totalMessages}
            </p>
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          <button
            type="button"
            onClick={onExportConversations}
            disabled={conversations.length === 0}
            className="inline-flex items-center gap-2 rounded-xl border border-[var(--vx-border)] bg-[var(--vx-bg-elevated)] px-4 py-2.5 text-sm font-medium text-[var(--vx-text)] hover:border-[var(--vx-border-strong)] disabled:opacity-40 cursor-pointer min-h-[44px]"
          >
            <Download className="w-4 h-4" />
            <span>Export JSON</span>
          </button>

          {!confirmClear ? (
            <button
              type="button"
              onClick={() => setConfirmClear(true)}
              disabled={conversations.length === 0}
              className="inline-flex items-center gap-2 rounded-xl border border-rose-500/35 bg-rose-500/10 px-4 py-2.5 text-sm font-medium text-rose-400 hover:bg-rose-500/20 disabled:opacity-40 cursor-pointer min-h-[44px]"
            >
              <Trash2 className="w-4 h-4" />
              <span>Clear All History</span>
            </button>
          ) : (
            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={() => void handleConfirmClearAll()}
                className="rounded-xl bg-rose-600 hover:bg-rose-500 px-4 py-2.5 text-sm font-semibold text-white cursor-pointer min-h-[44px]"
              >
                Confirm Delete All
              </button>
              <button
                type="button"
                onClick={() => setConfirmClear(false)}
                className="rounded-xl border border-[var(--vx-border)] px-4 py-2.5 text-sm text-[var(--vx-text-muted)] cursor-pointer min-h-[44px]"
              >
                Cancel
              </button>
            </div>
          )}
        </div>
      </section>

      {/* Privacy & About */}
      <section className="grid grid-cols-1 md:grid-cols-2 gap-5">
        <div className="rounded-2xl border border-[var(--vx-border)] bg-[var(--vx-surface)] p-5 sm:p-6 space-y-2.5">
          <div className="flex items-center gap-2 text-[var(--vx-primary-bright)]">
            <Shield className="w-5 h-5" />
            <h2 className="font-display text-base font-bold text-[var(--vx-text)]">
              Privacy Information
            </h2>
          </div>
          <p className="text-xs leading-relaxed text-[var(--vx-text-muted)]">
            Your chat history and preferences are stored locally in IndexedDB inside your browser. Prompts are only transmitted to your configured VEYROX backend when live mode is active, and no API keys exist in client code.
          </p>
        </div>

        <div className="rounded-2xl border border-[var(--vx-border)] bg-[var(--vx-surface)] p-5 sm:p-6 space-y-3">
          <div className="flex items-center gap-2 text-[var(--vx-primary-bright)]">
            <Info className="w-5 h-5" />
            <h2 className="font-display text-base font-bold text-[var(--vx-text)]">
              About VEYROX & Community
            </h2>
          </div>
          <p className="text-xs text-[var(--vx-text-muted)]">
            VEYROX Website Version <strong className="text-[var(--vx-text)]">{VEYROX_VERSION}</strong> •{" "}
            <span className="font-mono-code break-all">{VEYROX_DISCORD_URL}</span>
          </p>
          <DiscordButton label="Join VEYROX Discord" variant="primary" className="w-full" />
        </div>
      </section>
    </div>
  );
};
