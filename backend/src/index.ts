import "dotenv/config";
import express from "express";
import cors from "cors";
import helmet from "helmet";
import { config } from "./config.js";

const app = express();
app.disable("x-powered-by");
app.use(helmet());
app.use(express.json({ limit: "32kb" }));
app.use(cors({
  origin(origin, callback) {
    if (!origin || config.allowedOrigins.includes("*") || config.allowedOrigins.includes(origin)) {
      callback(null, true);
    } else {
      callback(new Error("Origin not allowed by CORS"));
    }
  },
}));

app.get("/health", (_req, res) => {
  res.json({ ok: true, service: "marian-voice-ai-backend" });
});

/**
 * Creates an ephemeral client secret for a Realtime session.
 * The permanent OpenAI API key is used only here on the backend.
 */
app.post("/api/realtime/session", async (_req, res) => {
  if (!config.openAiApiKey) {
    res.status(503).json({ error: "Backend is missing OPENAI_API_KEY." });
    return;
  }

  try {
    const upstream = await fetch("https://api.openai.com/v1/realtime/client_secrets", {
      method: "POST",
      headers: {
        Authorization: `Bearer ${config.openAiApiKey}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        session: {
          type: "realtime",
          model: config.realtimeModel,
          audio: {
            output: { voice: config.voice },
          },
        },
      }),
    });

    const payload: unknown = await upstream.json();
    if (!upstream.ok) {
      console.error("Realtime client secret request failed:", upstream.status);
      res.status(upstream.status).json({ error: "Could not create a Realtime session." });
      return;
    }

    // Pass through the short-lived credential response; never log or persist it.
    res.status(200).json(payload);
  } catch (error) {
    console.error("Realtime session error:", error instanceof Error ? error.message : "unknown");
    res.status(502).json({ error: "Realtime service is temporarily unavailable." });
  }
});

app.listen(config.port, "0.0.0.0", () => {
  console.log(`Marian Voice AI backend listening on port ${config.port}`);
});
