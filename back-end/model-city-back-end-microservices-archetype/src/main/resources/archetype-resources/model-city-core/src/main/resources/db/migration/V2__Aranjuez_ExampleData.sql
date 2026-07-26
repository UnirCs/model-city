-- Seed data: model-city-users microservice
-- Zones and neighbourhoods of Aranjuez.
-- Safe to re-run: ON CONFLICT DO NOTHING on unique name column.

-- Zones
INSERT INTO zones (display_name, name)
VALUES
    ('Barrio Oeste',  'barrio-oeste'),
    ('Barrio Sur',    'barrio-sur'),
    ('Barrio Este',   'barrio-este'),
    ('Barrio Norte',  'barrio-norte'),
    ('Barrio Centro', 'barrio-centro')
ON CONFLICT (name) DO NOTHING;

-- Neighbourhoods: Barrio Oeste
INSERT INTO neighbourhoods (display_name, name, zone_id)
SELECT vals.display_name, vals.name, z.id
FROM (VALUES
    ('Narvaez',                                  'narvaez'),
    ('Las Aves',                                 'las-aves'),
    ('Olivas - Vergel - Pinar',                  'olivas-vergel-pinar'),
    ('Penicilina',                               'penicilina'),
    ('Residencia El Deleite - Estadio Municipal', 'residencia-el-deleite-estadio-municipal')
) AS vals(display_name, name)
JOIN zones z ON z.name = 'barrio-oeste'
ON CONFLICT (name) DO NOTHING;

-- Neighbourhoods: Barrio Sur
INSERT INTO neighbourhoods (display_name, name, zone_id)
SELECT vals.display_name, vals.name, z.id
FROM (VALUES
    ('El Mirador',                 'el-mirador'),
    ('Montecillo - Plaza de Toros', 'montecillo-plaza-de-toros'),
    ('Caritas',                    'caritas'),
    ('Ciudad de las Artes',        'ciudad-de-las-artes'),
    ('Poligono Moreras',           'poligono-moreras')
) AS vals(display_name, name)
JOIN zones z ON z.name = 'barrio-sur'
ON CONFLICT (name) DO NOTHING;

-- Neighbourhoods: Barrio Este
INSERT INTO neighbourhoods (display_name, name, zone_id)
SELECT vals.display_name, vals.name, z.id
FROM (VALUES
    ('Nuevo Aranjuez',           'nuevo-aranjuez'),
    ('Las Palomitas',            'las-palomitas'),
    ('El Pino',                  'el-pino'),
    ('La Arboleda de la Reina',  'la-arboleda-de-la-reina'),
    ('Agfa',                     'agfa'),
    ('Jardin del Principe',      'jardin-del-principe'),
    ('Moreras - Almansa',        'moreras-almansa'),
    ('Col. de Aviacion',         'col-de-aviacion')
) AS vals(display_name, name)
JOIN zones z ON z.name = 'barrio-este'
ON CONFLICT (name) DO NOTHING;

-- Neighbourhoods: Barrio Norte
INSERT INTO neighbourhoods (display_name, name, zone_id)
SELECT vals.display_name, vals.name, z.id
FROM (VALUES
    ('Academia de la Guardia Civil', 'academia-de-la-guardia-civil'),
    ('Poligono del Automovil',       'poligono-del-automovil'),
    ('La Montana',                   'la-montana'),
    ('Real Cortijo de San Isidro',   'real-cortijo-de-san-isidro')
) AS vals(display_name, name)
JOIN zones z ON z.name = 'barrio-norte'
ON CONFLICT (name) DO NOTHING;

-- Neighbourhoods: Barrio Centro
INSERT INTO neighbourhoods (display_name, name, zone_id)
SELECT vals.display_name, vals.name, z.id
FROM (VALUES
    ('Puente Barcas',       'puente-barcas'),
    ('Raso de la Estrella', 'raso-de-la-estrella'),
    ('Casco Antiguo',       'casco-antiguo')
) AS vals(display_name, name)
JOIN zones z ON z.name = 'barrio-centro'
ON CONFLICT (name) DO NOTHING;

-- Platform admin user
INSERT INTO users (id, name, email, address, neighbourhood_id, created_at, role)
VALUES ('auth0|6a10523ed9abf7db4105eb3f', 'admin@aranjuez.es', 'admin@aranjuez.es', null, 1, '2026-05-22 13:28:57.599615 +00:00', 'MODEL-CITY-PLATFORM-ADMIN')
ON CONFLICT (id) DO NOTHING;
