# Architecture

## Trust boundaries

1. Android app is an untrusted client. It never contains the permanent OpenAI API key.
2. Backend authenticates and rate-limits clients (authentication/rate limiting must be added before public deployment).
3. Backend creates short-lived Realtime client secrets using the server-side API key.
4. Android uses the short-lived secret for the Realtime connection in the next transport milestone.

## Audio transport decision

Use WebRTC for interactive audio on mobile. The Android WebRTC implementation is intentionally a separate milestone because it needs native peer-connection setup, SDP exchange, audio device routing, event-channel handling and device testing. Do not substitute a raw permanent API key in a WebSocket client.

## Privacy

Do not persist audio by default. If transcript or memory storage is added, make it opt-in, provide deletion controls, and document retention.
