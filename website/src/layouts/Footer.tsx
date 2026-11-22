import React from "react";
import { VEYROX_DISCORD_URL, VEYROX_SLOGAN, VEYROX_VERSION } from "../config/veyrox";
import { AppRoute } from "../types/chat";
import { DiscordIcon } from "../components/DiscordButton";
import { VeyroxLogo } from "../components/VeyroxLogo";

interface FooterProps {
  onNavigate: (route: AppRoute) => void;
}

export const Footer: React.FC<FooterProps> = ({ onNavigate }) => {
  return (
    <footer className="relative z-10 border-t border-[var(--vx-border)] bg-[var(--vx-bg-elevated)]/90">
      <div className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
        <div className="flex flex-col gap-6 md:flex-row md:items-center md:justify-between">
          <div>
            <VeyroxLogo size={28} />
            <p className="mt-2 font-mono-code text-xs tracking-widest text-[var(--vx-primary-bright)]">
              {VEYROX_SLOGAN}
            </p>
          </div>

          <nav className="flex flex-wrap items-center gap-x-6 gap-y-2 text-sm text-[var(--vx-text-muted)]">
            <button
              type="button"
              onClick={() => onNavigate("home")}
              className="hover:text-[var(--vx-text)] transition-colors cursor-pointer"
            >
              Home
            </button>
            <button
              type="button"
              onClick={() => onNavigate("features")}
              className="hover:text-[var(--vx-text)] transition-colors cursor-pointer"
            >
              Features
            </button>
            <button
              type="button"
              onClick={() => onNavigate("chat")}
              className="hover:text-[var(--vx-text)] transition-colors cursor-pointer"
            >
              AI Chat
            </button>
            <button
              type="button"
              onClick={() => onNavigate("community")}
              className="hover:text-[var(--vx-text)] transition-colors cursor-pointer"
            >
              Community
            </button>
            <button
              type="button"
              onClick={() => onNavigate("about")}
              className="hover:text-[var(--vx-text)] transition-colors cursor-pointer"
            >
              About
            </button>
            <button
              type="button"
              onClick={() => onNavigate("settings")}
              className="hover:text-[var(--vx-text)] transition-colors cursor-pointer"
            >
              Settings
            </button>
            <a
              href={VEYROX_DISCORD_URL}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-1.5 text-[#7289DA] hover:text-[#8EA1E1] font-medium transition-colors"
            >
              <DiscordIcon className="w-4 h-4" />
              <span>Discord</span>
            </a>
          </nav>
        </div>

        <div className="mt-8 flex flex-col gap-2 border-t border-[var(--vx-border)] pt-6 text-xs text-[var(--vx-text-subtle)] sm:flex-row sm:items-center sm:justify-between">
          <p>
            Conversations are persisted locally in your browser (IndexedDB). No API keys are ever embedded in client code.
          </p>
          <p className="font-mono-code">VEYROX Web v{VEYROX_VERSION}</p>
        </div>
      </div>
    </footer>
  );
};
