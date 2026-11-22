import React, { useEffect, useState } from "react";
import { AmbientBackdrop } from "./components/AmbientBackdrop";
import { useTheme } from "./hooks/useTheme";
import { useVeyroxWorkspace } from "./hooks/useVeyroxWorkspace";
import { Footer } from "./layouts/Footer";
import { Navbar } from "./layouts/Navbar";
import { AboutPage } from "./pages/AboutPage";
import { ChatPage } from "./pages/ChatPage";
import { CommunityPage } from "./pages/CommunityPage";
import { FeaturesPage } from "./pages/FeaturesPage";
import { HomePage } from "./pages/HomePage";
import { SettingsPage } from "./pages/SettingsPage";
import { AiModeId, AppRoute } from "./types/chat";

function parseHashRoute(): AppRoute {
  const raw = window.location.hash.replace(/^#\/?/, "").toLowerCase();
  if (
    raw === "home" ||
    raw === "features" ||
    raw === "chat" ||
    raw === "community" ||
    raw === "about" ||
    raw === "settings"
  ) {
    return raw;
  }
  return "home";
}

export const App: React.FC = () => {
  const [route, setRoute] = useState<AppRoute>(() => parseHashRoute());
  const workspace = useVeyroxWorkspace();

  useTheme(workspace.preferences.theme);

  useEffect(() => {
    const onHashChange = () => {
      setRoute(parseHashRoute());
    };
    window.addEventListener("hashchange", onHashChange);
    return () => window.removeEventListener("hashchange", onHashChange);
  }, []);

  const navigate = (nextRoute: AppRoute) => {
    setRoute(nextRoute);
    const targetHash = `#/${nextRoute}`;
    if (window.location.hash !== targetHash) {
      window.location.hash = targetHash;
    }
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleStartModeChat = async (mode: AiModeId) => {
    await workspace.createConversation(mode);
    navigate("chat");
  };

  return (
    <div className="min-h-screen flex flex-col bg-[var(--vx-bg)] text-[var(--vx-text)]">
      <AmbientBackdrop isLight={workspace.preferences.theme === "light"} />

      <Navbar
        activeRoute={route}
        onNavigate={navigate}
        isDemoMode={workspace.backendStatus.demoMode}
      />

      <main className="flex-1">
        {route === "home" && (
          <HomePage
            selectedMode={workspace.selectedMode}
            onSelectMode={(mode) => {
              void workspace.changeMode(mode);
            }}
            onNavigate={navigate}
            onStartModeChat={(mode) => {
              void handleStartModeChat(mode);
            }}
          />
        )}

        {route === "features" && (
          <FeaturesPage
            selectedMode={workspace.selectedMode}
            onSelectMode={(mode) => {
              void workspace.changeMode(mode);
            }}
            onStartModeChat={(mode) => {
              void handleStartModeChat(mode);
            }}
            onNavigate={navigate}
          />
        )}

        {route === "chat" && (
          <ChatPage
            conversations={workspace.conversations}
            activeConversation={workspace.activeConversation}
            selectedMode={workspace.selectedMode}
            preferences={workspace.preferences}
            isGenerating={workspace.isGenerating}
            chatError={workspace.chatError}
            backendStatus={workspace.backendStatus}
            onDismissError={() => workspace.setChatError(null)}
            onCreateConversation={workspace.createConversation}
            onSelectConversation={workspace.selectConversation}
            onSelectMode={workspace.changeMode}
            onRenameConversation={workspace.renameConversation}
            onDeleteConversation={workspace.deleteConversation}
            onClearConversationMessages={workspace.clearConversationMessages}
            onSendMessage={workspace.sendMessage}
            onOpenSettings={() => navigate("settings")}
          />
        )}

        {route === "community" && <CommunityPage />}

        {route === "about" && <AboutPage onNavigate={navigate} />}

        {route === "settings" && (
          <SettingsPage
            preferences={workspace.preferences}
            conversations={workspace.conversations}
            backendStatus={workspace.backendStatus}
            onUpdatePreferences={workspace.updatePreferences}
            onRefreshBackend={workspace.refreshBackendStatus}
            onExportConversations={workspace.exportConversationsJson}
            onClearAllConversations={workspace.clearAllConversations}
          />
        )}
      </main>

      {route !== "chat" && <Footer onNavigate={navigate} />}
    </div>
  );
};

export default App;
