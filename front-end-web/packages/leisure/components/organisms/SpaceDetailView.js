import Link from 'next/link';
import Icon from '@modelcity/core/components/atoms/Icon';
import DeleteEntityButton from '@modelcity/leisure/components/molecules/DeleteEntityButton';
import AdminSection from '@modelcity/core/components/molecules/AdminSection';
import PhotoGallery from '@modelcity/leisure/components/molecules/PhotoGallery';
import LocationSection from '@modelcity/leisure/components/molecules/LocationSection';
import SpaceResourcesPanel from '@modelcity/leisure/components/organisms/SpaceResourcesPanel';
import { removePublicSpace } from '@modelcity/leisure/lib/actions/publicSpaces';

/**
 * Organism: SpaceDetailView
 *
 * Renders the body of a public (sports) space detail page: banner (or title
 * fallback), description, photo gallery, the reservable-resources panel, the
 * location aside and the staff administration controls.
 *
 * The back button is intentionally NOT rendered here so the page owns it.
 *
 * @param {{
 *   space: object,                     // public space payload from the leisure service
 *   resources: object[],               // reservable resources of the space
 *   t: object,                         // leisure.spaceDetail dictionary
 *   tAdmin: object,                    // leisure.adminActions dictionary
 *   tRes: object,                      // leisure.resources dictionary
 *   tResForm: object,                  // leisure.resourceForm dictionary
 *   tTypes: object,                    // leisure.resourceTypes dictionary
 *   lang: string,
 *   canManageSpace: boolean,
 *   canManageResources: boolean,
 * }} props
 */
export default function SpaceDetailView({
  space,
  resources,
  t,
  tAdmin,
  tRes,
  tResForm,
  tTypes,
  lang,
  canManageSpace,
  canManageResources,
}) {
  const photos = space.photoUrls ?? [];
  const bannerImage = photos[0];
  const otherPhotos = photos.slice(1);

  return (
    <>
      {/* Banner */}
      {bannerImage ? (
        <section className="relative rounded-md overflow-hidden shadow-md h-64 md:h-80">
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img src={bannerImage} alt={space.name} className="w-full h-full object-cover" />
          <div
            className="absolute inset-0 bg-linear-to-t from-primary/85 to-transparent"
            aria-hidden="true"
          />
          <div className="absolute bottom-md left-md right-md">
            <h1 className="text-h2 text-white leading-tight">{space.name}</h1>
          </div>
        </section>
      ) : (
        <header className="space-y-xs">
          <h1 className="text-h2 text-primary leading-tight">{space.name}</h1>
        </header>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-10 gap-lg">
        {/* LEFT — description + gallery */}
        <div className="lg:col-span-6 space-y-md">
          <section
            aria-labelledby="space-description-heading"
            className="bg-surface-container-lowest rounded-md border border-outline-variant p-md shadow-sm"
          >
            <h2
              id="space-description-heading"
              className="text-h3 text-primary mb-sm pb-xs border-b border-outline-variant"
            >
              {t.descriptionTitle}
            </h2>
            <p className="text-body-md text-on-surface-variant whitespace-pre-line">
              {space.description}
            </p>
          </section>

          {otherPhotos.length > 0 && (
            <section
              aria-labelledby="space-gallery-heading"
              className="bg-surface-container-lowest rounded-md border border-outline-variant p-md shadow-sm"
            >
              <h2
                id="space-gallery-heading"
                className="text-h3 text-primary mb-md pb-xs border-b border-outline-variant flex items-center gap-sm"
              >
                <Icon name="photo_library" size={20} className="text-secondary" />
                {t.gallery}
              </h2>
              <PhotoGallery
                photos={otherPhotos}
                alt={space.name}
                closeLabel={t.galleryClose}
                prevLabel={t.galleryPrev}
                nextLabel={t.galleryNext}
              />
            </section>
          )}

          {/* Resources panel — directly beneath the service description,
              with a left-side accent line per citizen-services-rules */}
          <SpaceResourcesPanel
            spaceId={space.id}
            resources={resources}
            canManage={canManageResources}
            t={tRes}
            tForm={tResForm}
            tTypes={tTypes}
            lang={lang}
          />
        </div>

        {/* RIGHT — location + admin controls */}
        <aside className="lg:col-span-4 space-y-md">
          <LocationSection
            latitude={space.latitude}
            longitude={space.longitude}
            name={space.name}
            address={space.address}
            mapTitle={t.mapTitle}
            openInMapsLabel={t.openInMaps}
          />
          {canManageSpace && (
            <AdminSection title={tAdmin.sectionTitle}>
              <Link
                href={`/${lang}/sports-spaces/${space.id}/edit`}
                className="inline-flex items-center justify-center gap-sm px-md py-sm rounded-md border border-primary text-primary bg-surface text-label-md font-semibold hover:bg-primary/5 transition-all active:scale-95 w-full"
              >
                <Icon name="edit" size={18} />
                {tAdmin.editSpace}
              </Link>
              <DeleteEntityButton
                id={space.id}
                action={removePublicSpace}
                onSuccessHref={`/${lang}/sports-spaces`}
                className="w-full"
                labels={{
                  button:       tAdmin.deleteSpace,
                  confirmTitle: tAdmin.deleteSpaceConfirmTitle,
                  confirmBody:  tAdmin.deleteSpaceConfirmBody,
                  confirm:      tAdmin.confirm,
                  cancel:       tAdmin.cancel,
                  deleting:     tAdmin.deleting,
                  error:        tAdmin.deleteError,
                }}
              />
            </AdminSection>
          )}
        </aside>
      </div>
    </>
  );
}
