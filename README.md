# VEYROX — Website & AI Backend (Version 1.0)

**YOUR AI. YOUR SPACE.**  
*INTELLIGENCE. WITHOUT LIMITS.*

VEYROX is a futuristic, dark-first AI web platform built with a clean separation between its responsive frontend (`website/`) and its secure, Gemini-ready REST backend (`backend/`).

---

## 1. Overview & Features

- **Pages & Navigation**: Home, Features (with AI Mode Inspector), AI Chat, Community, About, and Settings.
- **4 Specialized AI Modes**:
  - `GENERAL` — General conversations, explanations, and brainstorming.
  - `WRITING` — Drafting, rewriting, emails, and polished copy.
  - `CODING` — Clean code generation, debugging, and technical guidance.
  - `SUMMARIZER` — Concise TL;DRs, key takeaways, and action items.
- **Persistent Conversation Management (IndexedDB)**:
  - Create, search, auto-title, rename, clear, delete, and export conversations as JSON.
  - Responsive desktop sidebar and mobile slide-out history drawer.
- **Rich Chat Experience**:
  - GitHub Flavored Markdown rendering, syntax-highlighted code blocks with copy button, message copy, timestamps, auto-scroll, `Enter` to send / `Shift + Enter` for newline, and error banners.
- **Transparent Demo Mode**:
  - When no live backend is connected or `GEMINI_API_KEY` is unset, VEYROX clearly displays **DEMO MODE** instead of faking live AI.
- **Official Discord Integration**:
  - Centralized in `website/src/config/veyrox.ts` (`VEYROX_DISCORD_URL = "https://discord.gg/WTctmpDXVc"`) and `backend/src/config/env.ts`.
- **Functional Settings**:
  - Dark & Light theme switcher, chat behavior toggles, default AI mode, live backend URL tester (`/api/health`), IndexedDB history export/clear, and privacy details.

---

## 2. Project Structure

```text
website/                  # React + TypeScript + Vite + Tailwind CSS + Framer Motion + Lucide
  src/
    components/           # AmbientBackdrop, DiscordButton, MarkdownRenderer, ModeSelector, VeyroxLogo
    config/               # Centralized branding, AI modes, and VEYROX_DISCORD_URL
    hooks/                # useTheme, useVeyroxWorkspace
    layouts/              # Navbar, Footer
    pages/                # HomePage, FeaturesPage, ChatPage, CommunityPage, AboutPage, SettingsPage
    services/             # aiService (BackendAiClient, DemoAiClient, health checks)
    styles/               # Tailwind CSS v4 + VEYROX dark/light tokens + code highlighting
    types/                # TypeScript domain models
    utils/                # IndexedDB persistence (storage.ts) & unit tests

backend/                  # Node.js + TypeScript + Express REST API
  src/
    config/               # Environment configuration loader (env.ts)
    middleware/           # Rate limiting, Bearer auth, input validation (security.ts)
    providers/            # AI mode instructions (aiModes.ts) & Demo/Gemini providers (aiProvider.ts)
    store/                # Conversation store (conversationStore.ts)
    types/                # Shared TypeScript interfaces (ai.ts)
    app.ts                # Express app with Helmet, CORS, validation, and API routes
    server.ts             # HTTP server entrypoint
  test/                   # Automated API integration tests
```

---

## 3. Prerequisites & Installation

Requires **Node.js 20+** and **npm**.

Install dependencies for both the website and backend:

```bash
npm --prefix website install
npm --prefix backend install
```

---

## 4. Running Locally

### Option A: Run Website in Demo Mode (Frontend Only)

```bash
npm --prefix website run dev
```

Open `http://localhost:5173` in your browser. Without a backend URL configured, the website automatically runs in clearly labeled **DEMO MODE**.

### Option B: Run Backend + Website Together

1. Configure backend environment variables:
   ```bash
   cp backend/.env.example backend/.env
   ```
   - Leave `GEMINI_API_KEY=` blank to test backend **Demo Mode**, **or** add your server-side Gemini API key to `backend/.env` for live Gemini responses.
   - Never put `GEMINI_API_KEY` in `website/.env` or any `VITE_*` variable.

2. Start the Express TypeScript backend (`http://localhost:8787`):
   ```bash
   npm --prefix backend run dev
   ```

3. Start the Vite website (`http://localhost:5173`):
   ```bash
   npm --prefix website run dev
   ```

4. In the website, open **Settings → AI Backend Connection**, enter `http://localhost:8787`, and click **Save & Test**. (In local Vite dev, `/api` also proxies to `http://localhost:8787`.)

---

## 5. Backend API Endpoints

- `GET /api/health` — Returns service status, active AI provider (`gemini` or `demo`), model, and Discord URL.
- `GET /api/modes` — Lists the 4 AI modes (`GENERAL`, `WRITING`, `CODING`, `SUMMARIZER`).
- `POST /api/chat` — Accepts `{ message, mode, history }`, validates input, applies mode system instruction, and returns `{ reply, mode, demo, model, provider, timestamp }`.
- `GET /api/conversations` — Lists conversations stored in the backend store.
- `POST /api/conversations` — Creates a conversation (`{ title?, mode? }`).
- `PATCH /api/conversations/:id` — Renames a conversation (`{ title }`).
- `DELETE /api/conversations/:id` — Deletes a conversation.

---

## 6. Environment Variables

### `backend/.env` (Server-Side Secrets Only)
| Variable | Purpose | Default |
|---|---|---|
| `PORT` | HTTP port | `8787` |
| `HOST` | Bind host | `0.0.0.0` |
| `GEMINI_API_KEY` | Server-side Google Gemini API key (leave empty for Demo Mode) | `""` |
| `GEMINI_MODEL` | Gemini model ID | `gemini-2.5-flash` |
| `CORS_ORIGIN` | Comma-separated allowed browser origins | `http://localhost:5173,http://127.0.0.1:5173` |
| `RATE_LIMIT_WINDOW_MS` | Rate limit window in ms | `60000` |
| `RATE_LIMIT_MAX_REQUESTS` | Max requests per IP per window | `30` |
| `AUTH_REQUIRED` | Require `Authorization: Bearer` header | `false` |
| `AUTH_BEARER_TOKEN` | Secret token when `AUTH_REQUIRED=true` | `""` |
| `VEYROX_DISCORD_URL` | Official Discord invite | `https://discord.gg/WTctmpDXVc` |

### `website/.env` (Public Config Only)
- `VITE_VEYROX_API_URL` — Optional deployed HTTPS backend URL (e.g., `https://api.yourdomain.com`).

---

## 7. Build & Automated Tests

```bash
# Website typecheck, production build, and unit tests
npm --prefix website run typecheck
npm --prefix website run build
npm --prefix website test

# Backend typecheck, TypeScript build, and API tests
npm --prefix backend run typecheck
npm --prefix backend run build
npm --prefix backend test
```

---

## 8. Deployment Instructions

> **Status Note:** The website and backend are built and verified locally; neither is automatically hosted online until you deploy them.

1. **Deploy the Backend (`backend/`)**:
   - Host on any Node.js 20+ platform (e.g., Cloud Run, Railway, Fly.io, Render, or a VPS behind HTTPS/Nginx).
   - Run `npm run build` and start with `npm start` (`node dist/server.js`).
   - Set `GEMINI_API_KEY` and `CORS_ORIGIN=https://your-veyrox-domain.com` in your host's secret environment settings.
2. **Deploy the Website (`website/`) to GitHub Pages**:
   - A ready-to-use GitHub Actions workflow is included at `.github/workflows/deploy-github-pages.yml` and `website/vite.config.ts` uses `base: "./"` with hash routing so it works on any repository path (`https://<username>.github.io/<repo>/`).
   - Push the repository to GitHub (branch `main`).
   - Open your GitHub repository **Settings → Pages**, and under **Build and deployment → Source**, select **GitHub Actions**.
   - *(Optional)* Once your backend is deployed, add a GitHub Repository Variable `VITE_VEYROX_API_URL` (never a secret key!) under **Settings → Secrets and variables → Actions → Variables** so the hosted website connects automatically instead of running in Demo Mode.
   - Note: GitHub Pages hosts static files only — host `backend/` separately on a Node.js 20+ server (Cloud Run, Render, Railway, Fly.io, or a VPS) if you want live Gemini AI.

---

## 9. Security & Privacy

- **Zero Browser Secrets**: API keys are never embedded in React/Vite client code.
- **Local Persistence**: Chats and preferences remain in your browser's IndexedDB (`veyrox-db`) and can be exported or cleared at any time.
- **Server Protections**: Express uses `helmet` security headers, origin-checked `cors`, `express-rate-limit`, `256kb` JSON size caps, strict payload validation, and privacy-conscious request logs (never logging prompt text).
