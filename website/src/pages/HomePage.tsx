import { motion } from "framer-motion";
import {
  ArrowRight,
  Code2,
  Database,
  FileText,
  Lock,
  MessageSquare,
  PenLine,
  ShieldCheck,
  Sparkles,
  Zap
} from "lucide-react";
import React from "react";
import {
  VEYROX_HERO_DESCRIPTION,
  VEYROX_HERO_HEADLINE,
  VEYROX_SLOGAN
} from "../config/veyrox";
import { AiModeId, AppRoute } from "../types/chat";
import { DiscordButton } from "../components/DiscordButton";
import { ModeSelector } from "../components/ModeSelector";

interface HomePageProps {
  selectedMode: AiModeId;
  onSelectMode: (mode: AiModeId) => void;
  onNavigate: (route: AppRoute) => void;
  onStartModeChat: (mode: AiModeId) => void;
}

const CAPABILITIES = [
  {
    icon: MessageSquare,
    title: "AI Conversations",
    description:
      "Multi-turn dialogue with Markdown rendering, syntax-highlighted code blocks, timestamps, and one-click message copy."
  },
  {
    icon: PenLine,
    title: "Writing Assistance",
    description:
      "Draft executive emails, refine tone, polish creative copy, and structure clear announcements in seconds."
  },
  {
    icon: Code2,
    title: "Coding Assistance",
    description:
      "Generate clean TypeScript, debug edge cases, and inspect formatted code with instant clipboard copy."
  },
  {
    icon: FileText,
    title: "Summarization",
    description:
      "Distill long research notes, articles, and specifications into concise TL;DR takeaways and action items."
  },
  {
    icon: Zap,
    title: "Focused Productivity",
    description:
      "Switch AI modes cleanly per conversation without clutter, popups, or bloated dashboards."
  },
  {
    icon: Database,
    title: "Persistent Chat History",
    description:
      "Stored locally in IndexedDB with auto-generated titles, manual rename, search, export, and instant restoration."
  }
];

export const HomePage: React.FC<HomePageProps> = ({
  selectedMode,
  onSelectMode,
  onNavigate,
  onStartModeChat
}) => {
  return (
    <div className="relative z-10">
      {/* Hero Section */}
      <section className="mx-auto max-w-7xl px-4 pt-16 pb-20 sm:px-6 sm:pt-24 sm:pb-24 lg:px-8">
        <motion.div
          initial={{ opacity: 0, y: 18 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.45 }}
          className="mx-auto max-w-4xl text-center"
        >
          <span className="inline-flex items-center gap-2 rounded-full border border-[var(--vx-border-strong)] bg-[var(--vx-primary-soft)] px-4 py-1.5 font-mono-code text-xs tracking-[0.2em] text-[var(--vx-primary-bright)]">
            <Sparkles className="w-3.5 h-3.5" />
            {VEYROX_SLOGAN}
          </span>

          <h1 className="mt-7 font-display text-4xl font-bold tracking-tight text-[var(--vx-text)] sm:text-6xl lg:text-7xl leading-[1.05]">
            {VEYROX_HERO_HEADLINE}
          </h1>

          <p className="mx-auto mt-6 max-w-2xl text-base leading-relaxed text-[var(--vx-text-muted)] sm:text-lg">
            {VEYROX_HERO_DESCRIPTION}
          </p>

          <div className="mt-10 flex flex-col sm:flex-row items-center justify-center gap-3.5">
            <button
              type="button"
              onClick={() => onNavigate("chat")}
              className="w-full sm:w-auto inline-flex items-center justify-center gap-2 rounded-xl bg-[var(--vx-primary)] hover:bg-[var(--vx-primary-bright)] px-7 py-3.5 font-display text-sm font-semibold text-white shadow-[0_14px_34px_-10px_rgba(52,120,255,0.8)] transition-all hover:-translate-y-0.5 cursor-pointer min-h-[48px]"
            >
              <span>Start Chatting</span>
              <ArrowRight className="w-4 h-4" />
            </button>

            <button
              type="button"
              onClick={() => onNavigate("features")}
              className="w-full sm:w-auto inline-flex items-center justify-center gap-2 rounded-xl border border-[var(--vx-border)] bg-[var(--vx-surface)] hover:border-[var(--vx-border-strong)] hover:bg-[var(--vx-surface-hover)] px-7 py-3.5 font-display text-sm font-semibold text-[var(--vx-text)] transition-all hover:-translate-y-0.5 cursor-pointer min-h-[48px]"
            >
              <span>Explore VEYROX</span>
            </button>

            <DiscordButton label="Join Discord" variant="primary" className="w-full sm:w-auto" />
          </div>
        </motion.div>

        {/* Interactive Mode Preview Strip */}
        <motion.div
          initial={{ opacity: 0, y: 24 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.12 }}
          className="mt-16 rounded-3xl border border-[var(--vx-border)] bg-[var(--vx-bg-elevated)]/90 p-5 sm:p-7 shadow-[var(--vx-shadow)]"
        >
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between mb-5">
            <div>
              <p className="font-mono-code text-[11px] uppercase tracking-[0.18em] text-[var(--vx-primary-bright)]">
                FOUR INTELLIGENCE MODES
              </p>
              <h2 className="mt-1 font-display text-xl font-bold text-[var(--vx-text)]">
                Choose how VEYROX thinks
              </h2>
            </div>
            <button
              type="button"
              onClick={() => onStartModeChat(selectedMode)}
              className="inline-flex items-center gap-2 self-start sm:self-auto rounded-xl border border-[var(--vx-border-strong)] bg-[var(--vx-primary-soft)] px-4 py-2 text-xs font-display font-semibold text-[var(--vx-primary-bright)] hover:bg-[var(--vx-primary)] hover:text-white transition-colors cursor-pointer min-h-[40px]"
            >
              <span>Launch {selectedMode.charAt(0) + selectedMode.slice(1).toLowerCase()} Chat</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </button>
          </div>

          <ModeSelector
            selectedMode={selectedMode}
            onSelectMode={onSelectMode}
            variant="cards"
          />
        </motion.div>
      </section>

      {/* Capabilities Section */}
      <section className="mx-auto max-w-7xl px-4 py-16 sm:px-6 lg:px-8">
        <div className="mx-auto max-w-2xl text-center mb-12">
          <p className="font-mono-code text-xs uppercase tracking-[0.2em] text-[var(--vx-primary-bright)]">
            CAPABILITIES
          </p>
          <h2 className="mt-2 font-display text-3xl font-bold tracking-tight text-[var(--vx-text)] sm:text-4xl">
            Engineered for focused intelligence
          </h2>
          <p className="mt-3 text-sm sm:text-base text-[var(--vx-text-muted)]">
            Fast, deliberate tools designed to move ideas from rough prompt to finished work.
          </p>
        </div>

        <div className="grid grid-cols-1 gap-5 md:grid-cols-2 lg:grid-cols-3">
          {CAPABILITIES.map((item, idx) => {
            const Icon = item.icon;
            return (
              <motion.article
                key={item.title}
                initial={{ opacity: 0, y: 14 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true, amount: 0.2 }}
                transition={{ duration: 0.35, delay: idx * 0.04 }}
                className="rounded-2xl border border-[var(--vx-border)] bg-[var(--vx-surface)] p-6 transition-colors hover:border-[var(--vx-border-strong)]"
              >
                <div className="mb-4 inline-flex h-11 w-11 items-center justify-center rounded-xl bg-[var(--vx-primary-soft)] text-[var(--vx-primary-bright)]">
                  <Icon className="w-5 h-5" />
                </div>
                <h3 className="font-display text-lg font-semibold text-[var(--vx-text)]">
                  {item.title}
                </h3>
                <p className="mt-2 text-sm leading-relaxed text-[var(--vx-text-muted)]">
                  {item.description}
                </p>
              </motion.article>
            );
          })}
        </div>
      </section>

      {/* Security & Architecture Highlight */}
      <section className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
          <div className="rounded-3xl border border-[var(--vx-border)] bg-[var(--vx-surface)] p-7 sm:p-9">
            <div className="inline-flex items-center gap-2 rounded-full bg-[var(--vx-primary-soft)] px-3 py-1 font-mono-code text-[11px] text-[var(--vx-primary-bright)]">
              <Lock className="w-3.5 h-3.5" />
              SECURITY-FIRST ARCHITECTURE
            </div>
            <h3 className="mt-4 font-display text-2xl font-bold text-[var(--vx-text)]">
              Zero client-side API keys. Full transparency.
            </h3>
            <p className="mt-3 text-sm leading-relaxed text-[var(--vx-text-muted)]">
              VEYROX keeps Gemini credentials exclusively on the Express TypeScript backend. When no server is connected, VEYROX operates in a clearly labeled Demo Mode rather than faking live AI.
            </p>
            <div className="mt-6 flex flex-wrap gap-3">
              <button
                type="button"
                onClick={() => onNavigate("about")}
                className="inline-flex items-center gap-1.5 text-sm font-display font-semibold text-[var(--vx-primary-bright)] hover:underline cursor-pointer"
              >
                <span>Read Security & Privacy Model</span>
                <ArrowRight className="w-4 h-4" />
              </button>
            </div>
          </div>

          <div className="rounded-3xl border border-[#5865F2]/30 bg-gradient-to-br from-[#5865F2]/15 via-[var(--vx-surface)] to-[var(--vx-bg-elevated)] p-7 sm:p-9 flex flex-col justify-between">
            <div>
              <div className="inline-flex items-center gap-2 rounded-full bg-[#5865F2]/20 px-3 py-1 font-mono-code text-[11px] text-[#99AAB5]">
                <ShieldCheck className="w-3.5 h-3.5 text-[#7289DA]" />
                OFFICIAL VEYROX COMMUNITY
              </div>
              <h3 className="mt-4 font-display text-2xl font-bold text-[var(--vx-text)]">
                Build the future with VEYROX.
              </h3>
              <p className="mt-3 text-sm leading-relaxed text-[var(--vx-text-muted)]">
                Join builders, creators, and early adopters on the official VEYROX Discord server to share feedback, workflows, and release updates.
              </p>
            </div>
            <div className="mt-6 flex flex-wrap gap-3">
              <DiscordButton label="Join VEYROX Discord" variant="primary" />
              <button
                type="button"
                onClick={() => onNavigate("community")}
                className="inline-flex items-center gap-2 rounded-xl border border-[var(--vx-border)] bg-[var(--vx-surface)] px-5 py-3 text-sm font-display font-semibold text-[var(--vx-text)] hover:border-[var(--vx-border-strong)] cursor-pointer min-h-[44px]"
              >
                <span>Explore Community</span>
              </button>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
};
