import { motion } from "framer-motion";
import { ArrowRight, Cpu, Database, Lock, Server, ShieldCheck } from "lucide-react";
import React from "react";
import { VEYROX_SLOGAN, VEYROX_VERSION } from "../config/veyrox";
import { AppRoute } from "../types/chat";
import { DiscordButton } from "../components/DiscordButton";

interface AboutPageProps {
  onNavigate: (route: AppRoute) => void;
}

export const AboutPage: React.FC<AboutPageProps> = ({ onNavigate }) => {
  return (
    <div className="relative z-10 mx-auto max-w-6xl px-4 py-12 sm:px-6 sm:py-16 lg:px-8 space-y-12">
      <motion.section
        initial={{ opacity: 0, y: 14 }}
        animate={{ opacity: 1, y: 0 }}
        className="mx-auto max-w-3xl text-center"
      >
        <span className="font-mono-code text-xs uppercase tracking-[0.2em] text-[var(--vx-primary-bright)]">
          ABOUT VEYROX • VERSION {VEYROX_VERSION}
        </span>
        <h1 className="mt-3 font-display text-3xl font-bold tracking-tight text-[var(--vx-text)] sm:text-5xl">
          {VEYROX_SLOGAN}
        </h1>
        <p className="mt-4 text-sm sm:text-base leading-relaxed text-[var(--vx-text-muted)]">
          VEYROX was designed to feel like a dedicated, high-craft workspace rather than a noisy generic template. Every surface prioritizes clarity, speed, and honest architecture.
        </p>
      </motion.section>

      <section className="grid grid-cols-1 gap-6 md:grid-cols-3">
        <div className="rounded-2xl border border-[var(--vx-border)] bg-[var(--vx-surface)] p-6">
          <div className="mb-3 inline-flex h-10 w-10 items-center justify-center rounded-xl bg-[var(--vx-primary-soft)] text-[var(--vx-primary-bright)]">
            <Cpu className="w-5 h-5" />
          </div>
          <h2 className="font-display text-lg font-bold text-[var(--vx-text)]">
            1. React + TypeScript Frontend
          </h2>
          <p className="mt-2 text-sm leading-relaxed text-[var(--vx-text-muted)]">
            Responsive dark-first interface built with React, TypeScript, Vite, Tailwind CSS, Framer Motion, and Lucide icons.
          </p>
        </div>

        <div className="rounded-2xl border border-[var(--vx-border)] bg-[var(--vx-surface)] p-6">
          <div className="mb-3 inline-flex h-10 w-10 items-center justify-center rounded-xl bg-[var(--vx-primary-soft)] text-[var(--vx-primary-bright)]">
            <Database className="w-5 h-5" />
          </div>
          <h2 className="font-display text-lg font-bold text-[var(--vx-text)]">
            2. Local IndexedDB Storage
          </h2>
          <p className="mt-2 text-sm leading-relaxed text-[var(--vx-text-muted)]">
            Conversations, titles, timestamps, AI modes, and chat preferences stay locally in your browser via IndexedDB so your workspace survives refreshes.
          </p>
        </div>

        <div className="rounded-2xl border border-[var(--vx-border)] bg-[var(--vx-surface)] p-6">
          <div className="mb-3 inline-flex h-10 w-10 items-center justify-center rounded-xl bg-[var(--vx-primary-soft)] text-[var(--vx-primary-bright)]">
            <Server className="w-5 h-5" />
          </div>
          <h2 className="font-display text-lg font-bold text-[var(--vx-text)]">
            3. Modular Express + Gemini API
          </h2>
          <p className="mt-2 text-sm leading-relaxed text-[var(--vx-text-muted)]">
            A clean Node.js + TypeScript + Express backend handles rate limiting, input validation, CORS, and server-side Gemini API requests.
          </p>
        </div>
      </section>

      <section className="rounded-3xl border border-[var(--vx-border)] bg-[var(--vx-bg-elevated)] p-6 sm:p-9 space-y-5">
        <div className="flex items-center gap-2.5 text-[var(--vx-primary-bright)]">
          <ShieldCheck className="w-5 h-5" />
          <h2 className="font-display text-xl font-bold text-[var(--vx-text)]">
            Security & Data Transparency
          </h2>
        </div>

        <div className="grid grid-cols-1 gap-5 md:grid-cols-2 text-sm leading-relaxed text-[var(--vx-text-muted)]">
          <div className="rounded-2xl border border-[var(--vx-border)] bg-[var(--vx-surface)] p-5">
            <h3 className="font-display font-semibold text-[var(--vx-text)] mb-2 flex items-center gap-2">
              <Database className="w-4 h-4 text-[var(--vx-primary-bright)]" />
              What stays on your device
            </h3>
            <p>
              All conversation threads, titles, timestamps, and preferences are stored locally in browser IndexedDB (`veyrox-db`). You can export or delete everything anytime in Settings.
            </p>
          </div>

          <div className="rounded-2xl border border-[var(--vx-border)] bg-[var(--vx-surface)] p-5">
            <h3 className="font-display font-semibold text-[var(--vx-text)] mb-2 flex items-center gap-2">
              <Lock className="w-4 h-4 text-[var(--vx-primary-bright)]" />
              What is sent to the backend
            </h3>
            <p>
              In Demo Mode, nothing leaves your browser. When you connect a VEYROX backend URL, only the active prompt, selected mode, and recent context turns are sent over HTTP/HTTPS to generate a reply. No API keys ever live in frontend JavaScript.
            </p>
          </div>
        </div>

        <div className="pt-2 flex flex-wrap items-center gap-3">
          <button
            type="button"
            onClick={() => onNavigate("chat")}
            className="inline-flex items-center gap-2 rounded-xl bg-[var(--vx-primary)] hover:bg-[var(--vx-primary-bright)] px-5 py-3 text-sm font-display font-semibold text-white cursor-pointer min-h-[44px]"
          >
            <span>Open AI Chat</span>
            <ArrowRight className="w-4 h-4" />
          </button>
          <DiscordButton label="Join VEYROX Discord" variant="outline" />
        </div>
      </section>
    </div>
  );
};
