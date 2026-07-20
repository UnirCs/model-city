import { auth0 } from '@modelcity/core/lib/auth/auth0';
import { getDictionary } from '@modelcity/core/lib/i18n/dictionaries';
import {
  getPublicSpace,
  getPublicSpaceResources,
} from '@modelcity/leisure/lib/api/client';
import { canManagePublicSpaces, canManageSpaceResources } from '@modelcity/leisure/lib/auth/roles';
import BackButton from '@modelcity/core/components/atoms/BackButton';
import InlineStatus from '@modelcity/core/components/molecules/InlineStatus';
import SpaceDetailView from '@modelcity/leisure/components/organisms/SpaceDetailView';

import { makeMetadata } from '@modelcity/core/lib/a11y/pageMetadata';

export const generateMetadata = makeMetadata('nav.bookingsFacilities');

export const dynamic = 'force-dynamic';

/**
 * Public space detail page — `/{lang}/sports-spaces/{id}`
 *
 * Fetches the space and its reservable resources from the leisure microservice
 * and delegates rendering to the {@link SpaceDetailView} organism.
 *
 * @param {{ params: Promise<{ lang: string, id: string }> }} props
 */
export default async function PublicSpaceDetailPage({ params }) {
  const { lang, id } = await params;

  const dict = await getDictionary(lang, 'leisure');
  const t = dict.leisure.spaceDetail;

  const session = await auth0.getSession();
  const accessToken = session?.tokenSet?.accessToken;

  const result = await getPublicSpace(id, accessToken);

  if ('error' in result) {
    return <InlineStatus icon="error_outline" tone="error" message={t.loadError} />;
  }
  if ('notFound' in result) {
    return <InlineStatus icon="search_off" message={t.notFound} />;
  }

  const space = result.data;
  const rawResources = await getPublicSpaceResources(id, accessToken, canManageSpaceResources(session));
  const resources = rawResources?.content ?? rawResources ?? [];

  return (
    <div className="space-y-lg">
      <BackButton />
      <SpaceDetailView
        space={space}
        resources={resources}
        t={t}
        tAdmin={dict.leisure.adminActions}
        tRes={dict.leisure.resources}
        tResForm={dict.leisure.resourceForm}
        tTypes={dict.leisure.resourceTypes}
        lang={lang}
        canManageSpace={canManagePublicSpaces(session)}
        canManageResources={canManageSpaceResources(session)}
      />
    </div>
  );
}
