import { motion } from "framer-motion";
import {
  ArrowRight,
  CheckCircle2,
  Cpu,
  History,
  Layers,
  Shield,
  Terminal
} from "lucide-react";
import React from "react";
import { AI_MODES } from "../config/veyrox";
import { AiModeId, AppRoute } from "../types/chat";
import { ModeSelector, getModeIcon } from "../components/ModeSelector";

interface FeaturesPageProps {
  selectedMode: AiModeId;
  onSelectMode: (mode: AiModeId) => void;
  onStartModeChat: (mode: AiModeId) => void;
  onNavigate: (route: AppRoute) => void;
}

const FEATURE_PILLARS = [
  {
    icon: Layers,
    title: "Four Dedicated AI Modes",
    bullets: [
      "General AI for reasoning, planning, and ideation",
      "Writing AI for emails, essays, and copy editing",
      "Coding AI for clean code blocks and debugging",
      "Summarizer for TL;DR extraction and action items"
    ]
  },
  {
    icon: History,
    title: "IndexedDB Conversation Storage",
    bullets: [
      "Persists across browser refreshes automatically",
      "Auto-generates descriptive conversation titles",
      "Rename, search, clear, export JSON, or delete threads",
      "Desktop sidebar & mobile slide-out drawer"
    ]
  },
  {
    icon: Terminal,
    title: "Rich Markdown & Syntax Highlighting",
    bullets: [
      "GitHub Flavored Markdown tables, lists, and blockquotes",
      "Language-aware syntax highlighting in code blocks",
      "One-click Copy Code and Copy Message actions",
      "Readable spacing on phones, tablets, and desktops"
    ]
  },
  {
    icon: Shield,
    title: "Modular Backend & Demo Transparency",
    bullets: [
      "Express + TypeScript API ready for Gemini integration",
      "Rate limiting, Helmet security headers, and strict validation",
      "API keys stay on the server — never in browser JS",
      "Clear DEMO MODE indicator whenever offline"
    ]
  }
];

export const FeaturesPage: React.FC<FeaturesPageProps> = ({
  selectedMode,
  onSelectMode,
  onStartModeChat,
  onNavigate
}) => {
  const activeMode = AI_MODES[selectedMode];

  return (
    <div className="relative z-10 mx-auto max-w-7xl px-4 py-12 sm:px-6 sm:py-16 lg:px-8 space-y-14">
      <motion.div
        initial={{ opacity: 0, y: 14 }}
        animate={{ opacity: 1, y: 0 }}
        className="mx-auto max-w-3xl text-center"
      >
        <span className="font-mono-code text-xs uppercase tracking-[0.2em] text-[var(--vx-primary-bright)]">
          FEATURES & ARCHITECTURE
        </span>
        <h1 className="mt-3 font-display text-3xl font-bold tracking-tight text-[var(--vx-text)] sm:text-5xl">
          Everything you need in one intelligent space
        </h1>
        <p className="mt-4 text-sm sm:text-base text-[var(--vx-text-muted)]">
          Inspect how VEYROX pairs specialized system instructions with local browser persistence and a Gemini-ready backend.
        </p>
      </motion.div>

      {/* Interactive Mode Inspector */}
      <section className="rounded-3xl border border-[var(--vx-border)] bg-[var(--vx-bg-elevated)] p-6 sm:p-8 shadow-[var(--vx-shadow)]">
        <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between mb-6">
          <div>
            <h2 className="font-display text-xl font-bold text-[var(--vx-text)]">
              AI Mode Configuration Inspector
            </h2>
            <p className="mt-1 text-sm text-[var(--vx-text-muted)]">
              Select a mode to view its dedicated system instruction and starter prompts.
            </p>
          </div>
          <ModeSelector selectedMode={selectedMode} onSelectMode={onSelectMode} />
        </div>

        <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
          <div className="lg:col-span-2 rounded-2xl border border-[var(--vx-border)] bg-[var(--vx-surface)] p-5 sm:p-6">
            <div className="flex items-center gap-3 mb-4">
              <span className="inline-flex h-10 w-10 items-center justify-center rounded-xl bg-[var(--vx-primary)] text-white">
                {getModeIcon(activeMode.id, "w-5 h-5")}
              </span>
              <div>
                <h3 className="font-display text-lg font-bold text-[var(--vx-text)]">
                  {activeMode.name}
                </h3>
                <p className="text-xs text-[var(--vx-text-muted)]">{activeMode.description}</p>
              </div>
            </div>

            <div className="rounded-xl border border-[var(--vx-border)] bg-[var(--vx-bg-elevated)] p-4">
              <div className="flex items-center gap-2 font-mono-code text-[11px] uppercase tracking-wider text-[var(--vx-primary-bright)] mb-2">
                <Cpu className="w-3.5 h-3.5" />
                <span>System Instruction</span>
              </div>
              <p className="font-mono-code text-xs leading-relaxed text-[var(--vx-text-muted)]">
                {activeMode.systemInstruction}
              </p>
            </div>

            <div className="mt-5 flex flex-wrap items-center gap-3">
              <button
                type="button"
                onClick={() => onStartModeChat(activeMode.id)}
                className="inline-flex items-center gap-2 rounded-xl bg-[var(--vx-primary)] hover:bg-[var(--vx-primary-bright)] px-5 py-2.5 text-sm font-display font-semibold text-white cursor-pointer min-h-[44px]"
              >
                <span>Open {activeMode.name} in Chat</span>
                <ArrowRight className="w-4 h-4" />
              </button>
              <button
                type="button"
                onClick={() => onNavigate("settings")}
                className="inline-flex items-center gap-2 rounded-xl border border-[var(--vx-border)] px-4 py-2.5 text-sm font-medium text-[var(--vx-text-muted)] hover:text-[var(--vx-text)] cursor-pointer min-h-[44px]"
              >
                <span>Configure Default Mode</span>
              </button>
            </div>
          </div>

          <div className="rounded-2xl border border-[var(--vx-border)] bg-[var(--vx-surface)] p-5 sm:p-6">
            <h4 className="font-display text-sm font-semibold text-[var(--vx-text)] mb-3">
              Starter Prompts
            </h4>
            <div className="space-y-2.5">
              {activeMode.starterPrompts.map((prompt) => (
                <button
                  key={prompt}
                  type="button"
                  onClick={() => onStartModeChat(activeMode.id)}
                  className="w-full text-left rounded-xl border border-[var(--vx-border)] bg-[var(--vx-bg-elevated)] p-3 text-xs leading-relaxed text-[var(--vx-text-muted)] hover:border-[var(--vx-border-strong)] hover:text-[var(--vx-text)] transition-colors cursor-pointer"
                >
                  “{prompt}”
                </button>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* Feature Pillars */}
      <section className="grid grid-cols-1 gap-6 md:grid-cols-2">
        {FEATURE_PILLARS.map((pillar) => {
          const Icon = pillar.icon;
          return (
            <div
              key={pillar.title}
              className="rounded-2xl border border-[var(--vx-border)] bg-[var(--vx-surface)] p-6 sm:p-7"
            >
              <div className="flex items-center gap-3 mb-4">
                <span className="inline-flex h-10 w-10 items-center justify-center rounded-xl bg-[var(--vx-primary-soft)] text-[var(--vx-primary-bright)]">
                  <Icon className="w-5 h-5" />
                </span>
                <h3 className="font-display text-lg font-bold text-[var(--vx-text)]">
                  {pillar.title}
                </h3>
              </div>
              <ul className="space-y-2.5">
                {pillar.bullets.map((bullet) => (
                  <li key={bullet} className="flex items-start gap-2.5 text-sm text-[var(--vx-text-muted)]">
                    <CheckCircle2 className="w-4 h-4 shrink-0 mt-0.5 text-[var(--vx-primary-bright)]" />
                    <span>{bullet}</span>
                  </li>
                ))}
              </ul>
            </div>
          );
        })}
      </section>
    </div>
  );
};
