/**
 * Static neighbourhood reference data, grouped by city zone.
 *
 * This is fixed configuration data for the city (not a backend mock): the
 * portal renders these zones/neighbourhoods in registration, profile,
 * participation and security forms. Exported as an ES module constant rather
 * than a static JSON import, which would trigger "require is not defined"
 * errors in Turbopack's SSR bundle.
 *
 * `id` (and the per-neighbourhood `id`) mirror the numeric primary keys
 * expected by the backend API.
 *
 * @type {Array<{
 *   id:             number,
 *   zone:           string,
 *   zoneName:       string,
 *   neighbourhoods: Array<{ id: number, displayName: string, name: string }>
 * }>}
 */
const neighbourhoods = [
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
];

export default neighbourhoods;
