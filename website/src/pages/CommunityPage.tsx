import { motion } from "framer-motion";
import { Compass, MessageCircle, Rocket, Sparkles, Users } from "lucide-react";
import React from "react";
import { VEYROX_DISCORD_URL } from "../config/veyrox";
import { DiscordButton } from "../components/DiscordButton";

const COMMUNITY_CHANNELS = [
  {
    icon: Sparkles,
    title: "AI Workflows & Prompts",
    description:
      "Share General, Writing, Coding, and Summarizer prompt patterns with other VEYROX builders."
  },
  {
    icon: Rocket,
    title: "Releases & Roadmap",
    description:
      "Follow VEYROX 1.0 milestones, backend updates, and upcoming feature previews first."
  },
  {
    icon: MessageCircle,
    title: "Feedback & Feature Requests",
    description:
      "Suggest UI refinements, new AI modes, and integrations directly with the community."
  },
  {
    icon: Compass,
    title: "Self-Hosting & Setup Help",
    description:
      "Get help deploying the Node.js + Express + Gemini backend and configuring your workspace."
  }
];

export const CommunityPage: React.FC = () => {
  return (
    <div className="relative z-10 mx-auto max-w-7xl px-4 py-12 sm:px-6 sm:py-16 lg:px-8 space-y-12">
      <motion.section
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        className="rounded-3xl border border-[#5865F2]/35 bg-gradient-to-br from-[#5865F2]/18 via-[var(--vx-bg-elevated)] to-[var(--vx-surface)] p-8 sm:p-12 text-center shadow-[var(--vx-shadow)]"
      >
        <div className="mx-auto inline-flex items-center gap-2 rounded-full border border-[#5865F2]/40 bg-[#5865F2]/15 px-4 py-1.5 font-mono-code text-xs uppercase tracking-[0.18em] text-[#8EA1E1]">
          <Users className="w-3.5 h-3.5" />
          OFFICIAL VEYROX COMMUNITY
        </div>

        <h1 className="mt-5 font-display text-3xl font-bold tracking-tight text-[var(--vx-text)] sm:text-5xl">
          BUILD THE FUTURE WITH VEYROX.
        </h1>

        <p className="mx-auto mt-4 max-w-2xl text-sm sm:text-base leading-relaxed text-[var(--vx-text-muted)]">
          Connect with creators, engineers, and power users shaping VEYROX. Join our official Discord community to exchange ideas, ask questions, and follow new updates.
        </p>

        <div className="mt-8 flex flex-col sm:flex-row items-center justify-center gap-3.5">
          <DiscordButton label="Join VEYROX Discord" variant="primary" className="w-full sm:w-auto" />
          <DiscordButton label="Join Community Discord" variant="outline" className="w-full sm:w-auto" />
        </div>

        <p className="mt-5 font-mono-code text-xs text-[var(--vx-text-subtle)] break-all">
          Official Invite: {VEYROX_DISCORD_URL}
        </p>
      </motion.section>

      <section className="grid grid-cols-1 gap-5 md:grid-cols-2">
        {COMMUNITY_CHANNELS.map((channel) => {
          const Icon = channel.icon;
          return (
            <div
              key={channel.title}
              className="rounded-2xl border border-[var(--vx-border)] bg-[var(--vx-surface)] p-6"
            >
              <div className="mb-3 inline-flex h-10 w-10 items-center justify-center rounded-xl bg-[#5865F2]/15 text-[#8EA1E1]">
                <Icon className="w-5 h-5" />
              </div>
              <h2 className="font-display text-lg font-bold text-[var(--vx-text)]">
                {channel.title}
              </h2>
              <p className="mt-2 text-sm leading-relaxed text-[var(--vx-text-muted)]">
                {channel.description}
              </p>
            </div>
          );
        })}
      </section>
    </div>
  );
};
