import { createVeyroxApp } from "./app.js";
import { loadConfig } from "./config/env.js";

const config = loadConfig();
const app = createVeyroxApp();

app.listen(config.port, config.host, () => {
  console.info(
    `VEYROX backend listening on http://${config.host}:${config.port} (provider: ${
      config.geminiApiKey ? "GEMINI" : "DEMO"
    })`
  );
});
