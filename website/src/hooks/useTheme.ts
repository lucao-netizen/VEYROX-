import { useEffect } from "react";
import { ThemePreference } from "../types/chat";

export function useTheme(theme: ThemePreference) {
  useEffect(() => {
    document.documentElement.dataset.theme = theme;
  }, [theme]);
}
