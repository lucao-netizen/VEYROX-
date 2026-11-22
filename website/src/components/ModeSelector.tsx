import { Code2, FileText, PenLine, Sparkles } from "lucide-react";
import React from "react";
import { AI_MODE_LIST } from "../config/veyrox";
import { AiModeId } from "../types/chat";

export function getModeIcon(mode: AiModeId, className = "w-4 h-4") {
  switch (mode) {
    case "WRITING":
      return <PenLine className={className} />;
    case "CODING":
      return <Code2 className={className} />;
    case "SUMMARIZER":
      return <FileText className={className} />;
    case "GENERAL":
    default:
      return <Sparkles className={className} />;
  }
}

interface ModeSelectorProps {
  selectedMode: AiModeId;
  onSelectMode: (mode: AiModeId) => void;
  variant?: "cards" | "pills";
}

export const ModeSelector: React.FC<ModeSelectorProps> = ({
  selectedMode,
  onSelectMode,
  variant = "pills"
}) => {
  if (variant === "cards") {
    return (
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3.5">
        {AI_MODE_LIST.map((mode) => {
          const active = mode.id === selectedMode;
          return (
            <button
              key={mode.id}
              type="button"
              onClick={() => onSelectMode(mode.id)}
              aria-pressed={active}
              className={`group text-left rounded-2xl p-4 border transition-all duration-200 cursor-pointer ${
                active
                  ? "bg-[var(--vx-primary-soft)] border-[var(--vx-primary)] shadow-[0_0_25px_-10px_rgba(52,120,255,0.65)]"
                  : "bg-[var(--vx-surface)] border-[var(--vx-border)] hover:border-[var(--vx-border-strong)] hover:bg-[var(--vx-surface-hover)]"
              }`}
            >
              <div className="flex items-center justify-between mb-2.5">
                <span
                  className={`inline-flex items-center justify-center w-9 h-9 rounded-xl ${
                    active
                      ? "bg-[var(--vx-primary)] text-white"
                      : "bg-[var(--vx-primary-soft)] text-[var(--vx-primary-bright)]"
                  }`}
                >
                  {getModeIcon(mode.id, "w-4 h-4")}
                </span>
                <span
                  className={`font-mono-code text-[10px] tracking-widest uppercase px-2 py-0.5 rounded-full ${
                    active
                      ? "bg-[var(--vx-primary)]/25 text-[var(--vx-primary-bright)]"
                      : "text-[var(--vx-text-subtle)]"
                  }`}
                >
                  {mode.id}
                </span>
              </div>
              <h3 className="font-display font-semibold text-sm text-[var(--vx-text)] mb-1">
                {mode.name}
              </h3>
              <p className="text-xs leading-relaxed text-[var(--vx-text-muted)]">
                {mode.description}
              </p>
            </button>
          );
        })}
      </div>
    );
  }

  return (
    <div className="flex flex-wrap items-center gap-1.5" role="tablist" aria-label="AI Mode">
      {AI_MODE_LIST.map((mode) => {
        const active = mode.id === selectedMode;
        return (
          <button
            key={mode.id}
            type="button"
            role="tab"
            aria-selected={active}
            onClick={() => onSelectMode(mode.id)}
            title={mode.description}
            className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-display font-semibold border transition-all cursor-pointer min-h-[36px] ${
              active
                ? "bg-[var(--vx-primary)] text-white border-[var(--vx-primary)] shadow-[0_6px_16px_-6px_rgba(52,120,255,0.7)]"
                : "bg-[var(--vx-surface)] text-[var(--vx-text-muted)] border-[var(--vx-border)] hover:text-[var(--vx-text)] hover:border-[var(--vx-border-strong)]"
            }`}
          >
            {getModeIcon(mode.id, "w-3.5 h-3.5")}
            <span>{mode.shortLabel}</span>
          </button>
        );
      })}
    </div>
  );
};
