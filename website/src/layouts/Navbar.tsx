import { AnimatePresence, motion } from "framer-motion";
import { Menu, MessageSquare, Settings, Sparkles, X } from "lucide-react";
import React, { useState } from "react";
import { AppRoute } from "../types/chat";
import { DiscordButton } from "../components/DiscordButton";
import { VeyroxLogo } from "../components/VeyroxLogo";

interface NavbarProps {
  activeRoute: AppRoute;
  onNavigate: (route: AppRoute) => void;
  isDemoMode: boolean;
}

const NAV_ITEMS: Array<{ id: AppRoute; label: string }> = [
  { id: "home", label: "Home" },
  { id: "features", label: "Features" },
  { id: "chat", label: "AI" },
  { id: "community", label: "Community" },
  { id: "about", label: "About" },
  { id: "settings", label: "Settings" }
];

export const Navbar: React.FC<NavbarProps> = ({ activeRoute, onNavigate, isDemoMode }) => {
  const [mobileOpen, setMobileOpen] = useState(false);

  const handleSelect = (route: AppRoute) => {
    onNavigate(route);
    setMobileOpen(false);
  };

  return (
    <header className="sticky top-0 z-50 border-b border-[var(--vx-border)] bg-[var(--vx-glass)] backdrop-blur-xl">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
        <button
          type="button"
          onClick={() => handleSelect("home")}
          className="cursor-pointer rounded-xl focus:outline-none focus-visible:ring-2 focus-visible:ring-[var(--vx-primary)]"
          aria-label="VEYROX Home"
        >
          <VeyroxLogo size={32} />
        </button>

        {/* Desktop Nav */}
        <nav className="hidden md:flex items-center gap-1" aria-label="Primary Navigation">
          {NAV_ITEMS.map((item) => {
            const active = activeRoute === item.id;
            return (
              <button
                key={item.id}
                type="button"
                onClick={() => handleSelect(item.id)}
                className={`px-3.5 py-2 rounded-xl text-sm font-medium transition-colors cursor-pointer ${
                  active
                    ? "bg-[var(--vx-primary-soft)] text-[var(--vx-primary-bright)]"
                    : "text-[var(--vx-text-muted)] hover:text-[var(--vx-text)] hover:bg-[var(--vx-surface)]"
                }`}
              >
                {item.label}
              </button>
            );
          })}
        </nav>

        {/* Desktop CTA Actions */}
        <div className="hidden md:flex items-center gap-2.5">
          {isDemoMode && (
            <span
              title="Backend not connected — running in transparent Demo Mode"
              className="hidden lg:inline-flex items-center gap-1.5 rounded-full border border-amber-500/30 bg-amber-500/10 px-2.5 py-1 font-mono-code text-[10px] font-medium tracking-wider text-amber-400"
            >
              <span className="h-1.5 w-1.5 rounded-full bg-amber-400" />
              DEMO MODE
            </span>
          )}
          <DiscordButton label="Discord" variant="compact" />
          <button
            type="button"
            onClick={() => handleSelect("chat")}
            className="inline-flex items-center gap-2 rounded-xl bg-[var(--vx-primary)] hover:bg-[var(--vx-primary-bright)] px-4 py-2 text-xs font-display font-semibold text-white shadow-[0_8px_22px_-6px_rgba(52,120,255,0.75)] transition-all hover:-translate-y-0.5 cursor-pointer min-h-[38px]"
          >
            <Sparkles className="w-3.5 h-3.5" />
            <span>Start Chatting</span>
          </button>
        </div>

        {/* Mobile Menu Button */}
        <div className="flex md:hidden items-center gap-2">
          <button
            type="button"
            onClick={() => handleSelect("chat")}
            className="inline-flex items-center gap-1.5 rounded-xl bg-[var(--vx-primary)] px-3 py-2 text-xs font-display font-semibold text-white min-h-[40px] cursor-pointer"
          >
            <MessageSquare className="w-3.5 h-3.5" />
            <span>Chat</span>
          </button>
          <button
            type="button"
            onClick={() => setMobileOpen((prev) => !prev)}
            aria-label={mobileOpen ? "Close navigation menu" : "Open navigation menu"}
            aria-expanded={mobileOpen}
            className="inline-flex h-11 w-11 items-center justify-center rounded-xl border border-[var(--vx-border)] bg-[var(--vx-surface)] text-[var(--vx-text)] cursor-pointer"
          >
            {mobileOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
          </button>
        </div>
      </div>

      {/* Mobile Dropdown */}
      <AnimatePresence>
        {mobileOpen && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: "auto" }}
            exit={{ opacity: 0, height: 0 }}
            transition={{ duration: 0.2 }}
            className="md:hidden overflow-hidden border-t border-[var(--vx-border)] bg-[var(--vx-bg-elevated)]"
          >
            <div className="flex flex-col gap-1.5 p-4">
              {NAV_ITEMS.map((item) => {
                const active = activeRoute === item.id;
                return (
                  <button
                    key={item.id}
                    type="button"
                    onClick={() => handleSelect(item.id)}
                    className={`flex items-center justify-between rounded-xl px-4 py-3 text-left text-sm font-medium transition-colors cursor-pointer min-h-[48px] ${
                      active
                        ? "bg-[var(--vx-primary-soft)] text-[var(--vx-primary-bright)]"
                        : "text-[var(--vx-text-muted)] hover:bg-[var(--vx-surface)] hover:text-[var(--vx-text)]"
                    }`}
                  >
                    <span>{item.label === "AI" ? "AI Chat" : item.label}</span>
                    {item.id === "settings" && <Settings className="w-4 h-4 opacity-70" />}
                  </button>
                );
              })}
              <div className="mt-2 grid grid-cols-1 gap-2 pt-2 border-t border-[var(--vx-border)]">
                <DiscordButton label="Join VEYROX Discord" variant="primary" className="w-full" />
              </div>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </header>
  );
};
