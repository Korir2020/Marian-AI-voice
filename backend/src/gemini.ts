import express from "express";
import type { Express } from "express";
import path from "node:path";
import { MARIAN_INSTRUCTIONS } from "./persona.js";

const MODEL = process.env.GEMINI_LIVE_MODEL ?? "gemini-3.8-live";
const VOICE = process.env.GEMINI_VOICE ?? "Aoede";

export function registerGemini(app: Express): void {
  // Mints a one-use, short-lived token. The real key never leaves the server.
  app.post("/api/live/session", async (_req, res) => {
    const key = process.env.GEMINI_API_KEY;
    if (!key || key === "replace_me") {
      res.status(503).json({ error: "Backend is missing GEMINI_API_KEY." });
      return;
    }

    const config = {
      responseModalities: ["AUDIO"],
      systemInstruction: { parts: [{ text: MARIAN_INSTRUCTIONS }] },
      speechConfig: { voiceConfig: { prebuiltVoiceConfig: { voiceName: VOICE } } },
      sessionResumption: {},
      inputAudioTranscription: {},
      outputAudioTranscription: {},
    };

    try {
      const now = Date.now();
      const upstream = await fetch(
        "https://generativelanguage.googleapis.com/v1beta/auth_tokens",
        {
          method: "POST",
          headers: { "x-goog-api-key": key, "Content-Type": "application/json" },
          body: JSON.stringify({
            uses: 1,
            expireTime: new Date(now + 30 * 60 * 1000).toISOString(),
            newSessionExpireTime: new Date(now + 60 * 1000).toISOString(),
            liveConnectConstraints: { model: `models/${MODEL}`, config },
          }),
        },
      );
      const payload = (await upstream.json()) as { name?: string };
      if (!upstream.ok || !payload.name) {
        console.error("Gemini token request failed:", upstream.status, JSON.stringify(payload).slice(0, 300));
        res.status(502).json({ error: "Could not create a Gemini session." });
        return;
      }
      res.json({ token: payload.name, model: MODEL, config });
    } catch (error) {
      console.error("Gemini session error:", error instanceof Error ? error.message : "unknown");
      res.status(502).json({ error: "Voice service is temporarily unavailable." });
    }
  });

  // Serves the phone test page from backend/public
  app.use(express.static(path.resolve(process.cwd(), "public")));
}
