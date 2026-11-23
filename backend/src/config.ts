export const config = {
  port: Number(process.env.PORT ?? 3000),
  openAiApiKey: process.env.OPENAI_API_KEY ?? "",
  // Keep model and voice server-controlled; never accept these from an untrusted client.
  realtimeModel: process.env.OPENAI_REALTIME_MODEL ?? "gpt-realtime",
  voice: process.env.OPENAI_REALTIME_VOICE ?? "marin",
  allowedOrigins: (process.env.ALLOWED_ORIGIN ?? "*").split(",").map((v) => v.trim()),
};
