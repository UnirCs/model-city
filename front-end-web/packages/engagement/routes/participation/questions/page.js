import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { canViewParticipation, canCreateQuestion } from '@modelcity/engagement/lib/auth/roles';
import { getPublicQuestions } from '@modelcity/engagement/lib/api/client';
import QuestionsBrowser from '@modelcity/engagement/components/organisms/QuestionsBrowser';
import UnauthorizedView from '@modelcity/core/components/organisms/UnauthorizedView';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.participationQuestions');

// Always SSR: data changes and we avoid stale pagination
export const dynamic = 'force-dynamic';

/**
 * Citizen Questions list page — `/{lang}/participation/questions`
 *
 * Fetches public consultations from the citizen-engagement microservice using
 * three parallel requests (active / future / past), each with independent
 * pagination via URL search params (?activePage=&futurePage=&pastPage=), and
 * delegates rendering to the {@link QuestionsBrowser} organism.
 *
 * @param {{ params: Promise<{ lang: string }>, searchParams: Promise<{ activePage?: string, futurePage?: string, pastPage?: string }> }} props
 */
export default async function QuestionsPage({ params, searchParams }) {
  const { lang } = await params;
  const searchParamsObj = await searchParams;
  const activePage = Math.max(0, parseInt(searchParamsObj.activePage ?? '0', 10) || 0);
  const futurePage = Math.max(0, parseInt(searchParamsObj.futurePage ?? '0', 10) || 0);
  const pastPage = Math.max(0, parseInt(searchParamsObj.pastPage ?? '0', 10) || 0);

  const dict = await getDictionary(lang, 'participation');
  const t = dict.participation.questions;

  /* ── Session & role guard ───────────────────────────── */
  const session = await auth0.getSession();
  if (!canViewParticipation(session)) {
    return <UnauthorizedView t={dict.unauthorized} lang={lang} />;
  }

  const canCreate = canCreateQuestion(session);
  const accessToken = session?.tokenSet?.accessToken;

  /* ── Fetch from citizen-engagement client (3 parallel requests) ── */
  const [activeData, futureData, pastData] = await Promise.all([
    getPublicQuestions({ status: 'active', page: activePage }, accessToken),
    getPublicQuestions({ status: 'future', page: futurePage }, accessToken),
    getPublicQuestions({ status: 'past',   page: pastPage   }, accessToken),
  ]);

  const fetchError = !activeData || !futureData || !pastData;

  return (
    <QuestionsBrowser
      lang={lang}
      t={t}
      fetchError={fetchError}
      canCreate={canCreate}
      sections={{
        active: {
          questions: activeData?.content ?? [],
          currentPage: activePage,
          totalPages: activeData?.page?.totalPages ?? 1,
        },
        future: {
          questions: futureData?.content ?? [],
          currentPage: futurePage,
          totalPages: futureData?.page?.totalPages ?? 1,
        },
        past: {
          questions: pastData?.content ?? [],
          currentPage: pastPage,
          totalPages: pastData?.page?.totalPages ?? 1,
        },
      }}
    />
  );
}
