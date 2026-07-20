/**
 * Thin server-only client for Google's Gemini generative-language REST API.
 *
 * We deliberately talk to the public REST endpoint with `fetch` instead of
 * pulling in the `@google/generative-ai` SDK: the only capability we need is a
 * single text-in / text-out generation call, so a dependency would be dead
 * weight. Keeping this isolated also means the API key never leaves the server.
 *
 * ⚠️  Server-only. The API key is read from `process.env` and must never reach
 *     the browser — only import this from server actions / server components.
 *
 * @see https://ai.google.dev/api/generate-content
 */

/** Default model — fast and cheap, good enough for short field translations. */
const GEMINI_MODEL = process.env.GEMINI_MODEL ?? 'gemini-2.5-flash';

const GEMINI_ENDPOINT = (model) =>
  `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent`;

/**
 * Sends a single prompt to Gemini and returns the generated plain text.
 *
 * @param {string} prompt
 * @returns {Promise<{ text: string } | { error: string }>}
 */
export async function generateText(prompt) {
  const apiKey = process.env.GEMINI_API_KEY;
  if (!apiKey) return { error: 'not_configured' };

  let response;
  try {
    response = await fetch(GEMINI_ENDPOINT(GEMINI_MODEL), {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'x-goog-api-key': apiKey,
      },
      body: JSON.stringify({
        contents: [{ parts: [{ text: prompt }] }],
        // Low temperature — we want faithful translations, not creativity.
        generationConfig: { temperature: 0.2 },
      }),
      // Translations are never cached: the source text changes per request.
      cache: 'no-store',
    });
  } catch (err) {
    console.error('[gemini] network error:', err?.message ?? err);
    return { error: 'network_error' };
  }

  if (!response.ok) {
    console.error('[gemini] HTTP', response.status, await safeText(response));
    return { error: 'gemini_error' };
  }

  let data;
  try {
    data = await response.json();
  } catch {
    return { error: 'gemini_error' };
  }

  const text = data?.candidates?.[0]?.content?.parts
    ?.map((p) => p?.text ?? '')
    .join('')
    .trim();

  if (!text) return { error: 'empty_response' };
  return { text };
}

async function safeText(response) {
  try {
    return await response.text();
  } catch {
    return '<unreadable body>';
  }
}
