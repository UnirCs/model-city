import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import { canCreateQuestion } from '@modelcity/engagement/lib/auth/roles';
import BackButton from '@modelcity/core/components/atoms/BackButton';
import Icon from '@modelcity/core/components/atoms/Icon';
import CreateQuestionForm from '@modelcity/engagement/components/organisms/CreateQuestionForm';
import UnauthorizedView from '@modelcity/core/components/organisms/UnauthorizedView';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.participationCreateQuestion');

// Always SSR
export const dynamic = 'force-dynamic';

/**
 * Create Question page — `/{lang}/participation/questions/new`
 *
 * Only accessible to platform administrators.
 * Renders a form to create a new civic consultation sent to the
 * citizen-engagement microservice via POST /public-questions.
 *
 * @param {{ params: Promise<{ lang: string }> }} props
 */
export default async function CreateQuestionPage({ params }) {
  const { lang } = await params;
  const dict = await getDictionary(lang, 'participation');
  const t = dict.participation.createQuestion;

  const session = await auth0.getSession();
  if (!canCreateQuestion(session)) {
    return <UnauthorizedView t={dict.unauthorized} lang={lang} />;
  }

  return (
    <div className="space-y-lg">
      <BackButton />

      {/* Page header */}
      <div className="flex items-center gap-sm">
        <div className="w-10 h-10 rounded-md bg-primary/10 flex items-center justify-center shrink-0">
          <Icon name="add_circle" fill size={24} className="text-primary" />
        </div>
        <div>
          <h1 className="text-h2 text-primary font-semibold">{t.pageTitle}</h1>
          <p className="text-body-sm text-on-surface-variant">{t.pageSubtitle}</p>
        </div>
      </div>

      {/* Form card */}
      <div className="bg-surface-container-lowest rounded-md border border-outline-variant shadow-sm p-md">
        <CreateQuestionForm t={t} lang={lang} />
      </div>
    </div>
  );
}

