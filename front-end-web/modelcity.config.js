/**
 * Model City — city-level presentation & reference configuration.
 *
 * The single source of truth for the "soft" city identity that is not a secret
 * and not an environment concern: branding assets, the landing artwork, the
 * default map focus and the neighbourhood catalogue. It lives at the project
 * root next to `modules.config.mjs` (the module registry) and is imported by
 * platform components through the `@modelcity/config` alias declared in
 * `jsconfig.json`, so both the platform monorepo and a scaffolded city resolve
 * it identically.
 *
 * This is deliberately NOT wired through environment variables: these values
 * are structured (arrays/objects), safe to bake into the client bundle and
 * change per city, not per deployment. Runtime/secret configuration (Auth0,
 * gateway URLs, Stripe…) stays in env vars; feature-module selection stays in
 * `modules.config.mjs`.
 *
 * A city customises its portal by editing this file (translated copy still
 * lives in the i18n dictionaries / `overrides/`).
 */

/** @typedef {{ lng: number, lat: number }} LngLat */

/**
 * @typedef {Object} ModelCityConfig
 * @property {string} cityName          Display name of the city. Used in image alt text and
 *   interpolated into every locale string that carries the `{city}` token (see
 *   `packages/core/lib/i18n/dictionaries.js`).
 * @property {string} coatOfArmsUrl     URL of the city coat of arms shown in the top bar.
 * @property {string} backgroundImageUrl Decorative low-opacity backdrop for authenticated views.
 * @property {Array<{ url: string, alt: string, objectPosition?: string }>} landingImages
 *   The public landing banner photos, in composition order (base + two diagonal strips).
 * @property {{ center: LngLat }} map    Default map focus when no marker/coordinate is provided.
 * @property {Array<{
 *   id: number, zone: string, zoneName: string,
 *   neighbourhoods: Array<{ id: number, displayName: string, name: string }>
 * }>} neighbourhoods  Neighbourhood catalogue, grouped by city zone. `id` fields mirror the
 *   numeric primary keys expected by the backend API.
 */

/** @type {ModelCityConfig} */
const modelCityConfig = {
  cityName: 'Aranjuez',

  coatOfArmsUrl:
    'https://upload.wikimedia.org/wikipedia/commons/a/a3/Escudo_de_Aranjuez_%28Madrid%29.svg',

  backgroundImageUrl:
    'https://www.spain.info/export/sites/segtur/.content/imagenes/rutas/escapada-aranjuez/palacio-real-aranjuez-s1089121742.jpg',

  landingImages: [
    {
      // Layer 1 — full-bleed base.
      url: 'https://www.spain.info/export/sites/segtur/.content/imagenes/rutas/escapada-aranjuez/palacio-real-aranjuez-s1089121742.jpg',
      alt: 'Real Palacio de Aranjuez',
      objectPosition: '68% center',
    },
    {
      // Layer 2 — diagonal strip, middle-right (decorative).
      url: 'https://visitmadrid-files.s3.eu-west-1.amazonaws.com/files/2023-12/jardines-Aranjuez.jpg',
      alt: '',
      objectPosition: 'center 35%',
    },
    {
      // Layer 3 — narrow diagonal strip, far right (decorative).
      url: 'https://visita.aranjuez.es/wp-content/uploads/2022/01/PicsArt_09-27-09.34.50-1024x768.jpg',
      alt: '',
      objectPosition: 'center center',
    },
  ],

  map: {
    // Aranjuez town centre.
    center: { lng: -3.6033, lat: 40.0322 },
  },

  neighbourhoods: [
    {
      id: 1,
      zone: 'Barrio Oeste',
      zoneName: 'barrio-oeste',
      neighbourhoods: [
        { id: 1,  displayName: 'Narvaez',                                   name: 'narvaez' },
        { id: 2,  displayName: 'Las Aves',                                  name: 'las-aves' },
        { id: 3,  displayName: 'Olivas - Vergel - Pinar',                   name: 'olivas-vergel-pinar' },
        { id: 4,  displayName: 'Penicilina',                                name: 'penicilina' },
        { id: 5,  displayName: 'Residencia El Deleite - Estadio Municipal', name: 'residencia-el-deleite-estadio-municipal' },
      ],
    },
    {
      id: 2,
      zone: 'Barrio Sur',
      zoneName: 'barrio-sur',
      neighbourhoods: [
        { id: 6,  displayName: 'El Mirador',                  name: 'el-mirador' },
        { id: 7,  displayName: 'Montecillo - Plaza de Toros', name: 'montecillo-plaza-de-toros' },
        { id: 8,  displayName: 'Cáritas',                     name: 'caritas' },
        { id: 9,  displayName: 'Ciudad de las Artes',         name: 'ciudad-de-las-artes' },
        { id: 10, displayName: 'Polígono Moreras',             name: 'poligono-moreras' },
      ],
    },
    {
      id: 3,
      zone: 'Barrio Este',
      zoneName: 'barrio-este',
      neighbourhoods: [
        { id: 11, displayName: 'Nuevo Aranjuez',          name: 'nuevo-aranjuez' },
        { id: 12, displayName: 'Las Palomitas',           name: 'las-palomitas' },
        { id: 13, displayName: 'El Pino',                 name: 'el-pino' },
        { id: 14, displayName: 'La Arboleda de la Reina', name: 'la-arboleda-de-la-reina' },
        { id: 15, displayName: 'Agfa',                    name: 'agfa' },
        { id: 16, displayName: 'Jardín del Príncipe',     name: 'jardin-del-principe' },
        { id: 17, displayName: 'Moreras - Almansa',       name: 'moreras-almansa' },
        { id: 18, displayName: 'Col. de Aviación',        name: 'col-de-aviacion' },
      ],
    },
    {
      id: 4,
      zone: 'Barrio Norte',
      zoneName: 'barrio-norte',
      neighbourhoods: [
        { id: 19, displayName: 'Academia de la Guardia Civil', name: 'academia-de-la-guardia-civil' },
        { id: 20, displayName: 'Polígono del Automóvil',       name: 'poligono-del-automovil' },
        { id: 21, displayName: 'La Montaña',                   name: 'la-montana' },
        { id: 22, displayName: 'Real Cortijo de San Isidro',   name: 'real-cortijo-de-san-isidro' },
      ],
    },
    {
      id: 5,
      zone: 'Barrio Centro',
      zoneName: 'barrio-centro',
      neighbourhoods: [
        { id: 23, displayName: 'Puente Barcas',       name: 'puente-barcas' },
        { id: 24, displayName: 'Raso de la Estrella', name: 'raso-de-la-estrella' },
        { id: 25, displayName: 'Casco Antiguo',       name: 'casco-antiguo' },
      ],
    },
  ],
};

export default modelCityConfig;
