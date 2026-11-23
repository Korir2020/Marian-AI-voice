# Marian Voice AI

An independent starter project for a human-like AI voice assistant with a native Android app and a secure Node.js backend.

## What is included

- Native Android app built with Kotlin and Jetpack Compose.
- Backend built with Node.js, TypeScript and Express.
- Backend endpoint that creates short-lived OpenAI Realtime client secrets.
- Android voice-session screen, microphone permission handling, connection state and conversation transcript UI.
- Environment-variable based API key handling.
- Setup instructions and a staged implementation plan.

## Important scope note

This is a **foundation ZIP**, not a finished Siri replacement. It establishes the project structure and secure session-credential flow. The Android client currently includes the UI and permission/session scaffolding; the actual native WebRTC audio transport is the next integration milestone. Do not put an OpenAI API key in the Android app.

## Requirements

- Node.js 20+
- Android Studio (current stable)
- Android SDK 35
- An OpenAI API account with API billing enabled and access to the Realtime API

## 1. Start the backend in GitHub Codespaces

Open the repository in Codespaces, then:

```bash
cd backend
cp .env.example .env
```

Put your API key in `backend/.env`:

```env
OPENAI_API_KEY=your_api_key_here
PORT=3000
ALLOWED_ORIGIN=*
```

Never commit `.env`.

Then run:

```bash
npm install
npm run dev
```

Check:

```bash
curl http://localhost:3000/health
```

For Android testing, expose port 3000 in Codespaces and set its visibility to **Private** where possible. Copy the forwarded HTTPS URL.

## 2. Open the Android project

In Android Studio, open the `android` folder.

The app uses `BuildConfig.BACKEND_BASE_URL`. For local emulator testing, the default is `http://10.0.2.2:3000/`. For a Codespaces forwarded URL, change `BACKEND_BASE_URL` in `android/app/build.gradle.kts` to your forwarded HTTPS URL, ending with `/`.

Build and run on an Android phone or emulator. Grant microphone permission when prompted.

## 3. Current backend endpoint

`POST /api/realtime/session`

Returns a short-lived Realtime client secret from OpenAI. The permanent API key remains on the server.

The endpoint accepts no client-supplied model or arbitrary upstream URL. Configure the model and voice on the server in `backend/src/config.ts`.

## 4. Security before public deployment

- Keep `OPENAI_API_KEY` only in backend environment secrets.
- Restrict CORS to your actual app/web origin when applicable.
- Add authentication, per-user rate limits, abuse controls and usage monitoring before allowing public users.
- Do not expose the Codespaces development server as a public production service.
- Add privacy controls before persisting transcripts or long-term memories.

## 5. Next implementation milestones

1. Complete native Android WebRTC audio transport and Realtime event handling.
2. Add interruption / turn-taking and transcript streaming.
3. Add authentication and per-user rate limits.
4. Add user-controlled memory and conversation history.
5. Add Android assistant role / shortcuts and approved phone actions.
6. Add optional wake-word detection. Background hotword operation is subject to Android battery and microphone restrictions.

## Project structure

```text
marian-voice-ai/
├── android/                 # Native Android application
├── backend/                 # Secure Realtime session backend
├── docs/                    # Architecture and roadmap
├── .gitignore
└── README.md
```
