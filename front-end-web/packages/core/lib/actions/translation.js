'use server';

import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { generateText } from '@modelcity/core/lib/ai/gemini';
import { DEFAULT_LANG, SUPPORTED_LANGS } from '@modelcity/core/lib/i18n/dictionaries';

/** Human-readable language names used to instruct the model. */
const LANG_NAMES = {
  es: 'Spanish',
  en: 'English',
  fr: 'French',
};

/** Hard cap on the source text length — guards against runaway API usage. */
const MAX_INPUT_LENGTH = 5000;

/**
 * Builds the translation prompt. Kept deliberately strict so the model returns
 * only the translation, ready to drop straight into the form field.
 *
 * @param {string} text
 * @param {string} targetLang
 */
function buildPrompt(text, targetLang) {
  return [
    'You are a professional translator for a smart-city public-administration platform.',
    `Translate the text delimited by <text> from ${LANG_NAMES[DEFAULT_LANG]} into ${LANG_NAMES[targetLang]}.`,
    'Preserve meaning, tone, line breaks and proper nouns.',
    'Return ONLY the translation, with no quotes, labels, notes or explanations.',
    '',
    `<text>${text}</text>`,
  ].join('\n');
}

/**
 * Server Action: translates a piece of Spanish source text into one of the
 * other supported languages using Gemini.
 *
 * Restricted to authenticated users (these forms are admin-only on the write
 * path; the action only consumes API quota, never mutates data).
 *
 * @param {{ text: string, targetLang: string }} input
 * @returns {Promise<{ text: string } | { error: string }>}
 */
export async function translateText({ text, targetLang } = {}) {
  const session = await auth0.getSession();
  if (!session) return { error: 'forbidden' };

  const source = (text ?? '').trim();
  if (!source) return { error: 'empty_input' };
  if (source.length > MAX_INPUT_LENGTH) return { error: 'too_long' };

  if (
    !SUPPORTED_LANGS.includes(targetLang) ||
    targetLang === DEFAULT_LANG
  ) {
    return { error: 'invalid_target' };
  }

  return generateText(buildPrompt(source, targetLang));
}
