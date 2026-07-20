import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { isStaffUser } from '@modelcity/core/lib/auth/roles';
import { canViewParticipation, canManageParticipation } from '@modelcity/engagement/lib/auth/roles';
import { getPublicQuestion } from '@modelcity/engagement/lib/api/client';
import BackButton from '@modelcity/core/components/atoms/BackButton';
import InlineStatus from '@modelcity/core/components/molecules/InlineStatus';
import QuestionDetailView from '@modelcity/engagement/components/organisms/QuestionDetailView';
import UnauthorizedView from '@modelcity/core/components/organisms/UnauthorizedView';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.participationQuestions');

// Always SSR — vote counts and dates must be fresh
export const dynamic = 'force-dynamic';

/**
 * Question detail page — `/{lang}/participation/questions/{id}`
 *
 * Fetches the full consultation from the citizen-engagement microservice and
 * delegates rendering to the {@link QuestionDetailView} organism.
 *
 * @param {{ params: Promise<{ lang: string, id: string }> }} props
 */
export default async function QuestionDetailPage({ params }) {
  const { lang, id } = await params;
  const dict = await getDictionary(lang, 'participation');
  const t = dict.participation.questionDetail;

  /* ── Session & role guard ───────────────────────────────── */
  const session = await auth0.getSession();
  if (!canViewParticipation(session)) {
    return <UnauthorizedView t={dict.unauthorized} lang={lang} />;
  }

  /* ── Auth token & fetch via citizen-engagement client ───── */
  const accessToken = session?.tokenSet?.accessToken;
  const result = await getPublicQuestion(id, accessToken, canManageParticipation(session));

  /* ── Error / not-found states ───────────────────────────── */
  if ('error' in result) {
    return <InlineStatus icon="error_outline" tone="error" message={t.loadError} />;
  }

  if ('notFound' in result) {
    return <InlineStatus icon="search_off" message={t.notFound} />;
  }

  return (
    <div className="space-y-lg">
      <BackButton />
      <QuestionDetailView
        question={result.data}
        t={t}
        tOtp={dict.otp}
        tCert={dict.certificate}
        lang={lang}
        staff={isStaffUser(session)}
        canManage={canManageParticipation(session)}
        accessToken={accessToken}
      />
    </div>
  );
}
