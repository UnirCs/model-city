-- Model City Monolith - consolidated seed data

-- ===== core =====
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

-- Sample users referenced by the seed data of the other verticals (answers, operation_authorizations).
-- The new FKs in the monolith schema require these rows to exist.
INSERT INTO users (id, name, email, neighbourhood_id, role)
SELECT v.id, v.name, v.email, n.id, v.role
FROM (VALUES
          ('auth0|65a1b2c3d4e5f6a7b8c9d0e1', 'Lucia Garcia',   'lucia.garcia@example.com',   'casco-antiguo',   'MODEL-CITY-CITIZEN'),
          ('auth0|65a1b2c3d4e5f6a7b8c9d0e2', 'Marco Lopez',    'marco.lopez@example.com',    'casco-antiguo',   'MODEL-CITY-CITIZEN'),
          ('auth0|65a1b2c3d4e5f6a7b8c9d0e3', 'Sofia Ruiz',     'sofia.ruiz@example.com',     'jardin-del-principe', 'MODEL-CITY-CITIZEN'),
          ('auth0|65a1b2c3d4e5f6a7b8c9d0e4', 'Daniel Perez',   'daniel.perez@example.com',   'jardin-del-principe', 'MODEL-CITY-CITIZEN'),
          ('auth0|65a1b2c3d4e5f6a7b8c9d0e5', 'Elena Diaz',     'elena.diaz@example.com',     'el-mirador',      'MODEL-CITY-CITIZEN'),
          ('auth0|65a1b2c3d4e5f6a7b8c9d0e6', 'Pablo Sanchez',  'pablo.sanchez@example.com',  'narvaez',         'MODEL-CITY-CITIZEN'),
          ('auth0|65a1b2c3d4e5f6a7b8c9d0e7', 'Ana Fernandez',  'ana.fernandez@example.com',  'puente-barcas',   'MODEL-CITY-CITIZEN'),
          ('auth0|abc123',                   'Test User',      'test.user@example.com',      'casco-antiguo',   'MODEL-CITY-CITIZEN')
     ) AS v(id, name, email, neighbourhood_name, role)
         LEFT JOIN neighbourhoods n ON n.name = v.neighbourhood_name
    ON CONFLICT (id) DO NOTHING;

-- ===== engagement =====
-- Seed data: model-city-engagement microservice
-- Current date of reference: 2026-05-20
-- Three questions in the context of Aranjuez:
--   1. Closed    — opened 2026-01-10, closed 2026-03-15 (with sample answers)
--   2. Current   — opened 2026-05-20, closes 2026-06-20
--   3. Upcoming  — opens  2026-09-01, closes 2026-11-30
-- Zone/neighbourhood mapping (see zones and neighbourhoods tables):
--   Q1: Jardín del Príncipe   → zone_id=3 (Barrio Este),   neighbourhood_id=16
--   Q2: Casco Antiguo         → zone_id=5 (Barrio Centro), neighbourhood_id=25
--   Q3: Casco Antiguo         → zone_id=5 (Barrio Centro), neighbourhood_id=25
-- Safe to re-run: WHERE NOT EXISTS guards each block.

-- 1. CLOSED question: Estanque de los Chinescos

INSERT INTO civic_questions (title, description, image_url, open_date, close_date, zone_id, neighbourhood_id)
SELECT
    'Mejora y revitalización del Estanque de los Chinescos',
    'El Ayuntamiento de Aranjuez propone acometer una restauración integral del '
        || 'Estanque de los Chinescos en el Jardín del Príncipe. El proyecto incluye la '
        || 'limpieza y mejora de la lámina de agua, la rehabilitación de los dos quioscos '
        || 'históricos, el acondicionamiento de los embarcaderos y la renovación de las '
        || 'zonas verdes circundantes para mejorar el disfrute de vecinos y visitantes.',
    'https://media.istockphoto.com/id/2183691108/es/foto/quioscos-en-el-estanque-de-los-chinescos-aranjuez-espa%C3%B1a.jpg?s=612x612&w=0&k=20&c=bfUzq1-okHkp6vnxwARHlL3k_P1oToJW0RVqqd7skpM=',
    '2026-01-10',
    '2026-03-15',
    3,
    16
    WHERE NOT EXISTS (
    SELECT 1 FROM civic_questions
    WHERE title = 'Mejora y revitalización del Estanque de los Chinescos'
);

INSERT INTO objectives (question_id, objective, sort_order)
SELECT q.id, vals.objective, vals.sort_order
FROM civic_questions q
         CROSS JOIN (VALUES
                         ('Recuperar el valor paisajístico del jardín histórico UNESCO', 0),
                         ('Mejorar la experiencia de vecinos y turistas en el entorno natural', 1),
                         ('Proteger la biodiversidad acuática y la vegetación autóctona', 2)
) AS vals(objective, sort_order)
WHERE q.title = 'Mejora y revitalización del Estanque de los Chinescos'
  AND NOT EXISTS (
    SELECT 1 FROM objectives o WHERE o.question_id = q.id
);

INSERT INTO answers (question_id, citizen_id, dni_hash, vote, answered_at)
SELECT q.id, vals.citizen_id, encode(sha256(('DNI-' || vals.citizen_id)::bytea), 'hex'), vals.vote, vals.answered_at::TIMESTAMPTZ
FROM civic_questions q
         CROSS JOIN (VALUES
                         ('auth0|65a1b2c3d4e5f6a7b8c9d0e1', 'YES', '2026-01-12 09:15:00+00'),
                         ('auth0|65a1b2c3d4e5f6a7b8c9d0e2', 'YES', '2026-01-14 11:30:00+00'),
                         ('auth0|65a1b2c3d4e5f6a7b8c9d0e3', 'NO',  '2026-01-15 16:45:00+00'),
                         ('auth0|65a1b2c3d4e5f6a7b8c9d0e4', 'YES', '2026-02-03 08:20:00+00'),
                         ('auth0|65a1b2c3d4e5f6a7b8c9d0e5', 'YES', '2026-02-10 14:00:00+00'),
                         ('auth0|65a1b2c3d4e5f6a7b8c9d0e6', 'NO',  '2026-02-18 17:10:00+00'),
                         ('auth0|65a1b2c3d4e5f6a7b8c9d0e7', 'YES', '2026-03-01 10:05:00+00')
) AS vals(citizen_id, vote, answered_at)
WHERE q.title = 'Mejora y revitalización del Estanque de los Chinescos'
    ON CONFLICT (question_id, dni_hash) DO NOTHING;

-- 2. CURRENT question: Renovación del Ayuntamiento

INSERT INTO civic_questions (title, description, image_url, open_date, close_date, zone_id, neighbourhood_id)
SELECT
    'Renovación de la fachada del Ayuntamiento de Aranjuez',
    'Se propone una intervención de restauración integral en la fachada del '
        || 'edificio consistorial de la Plaza de la Constitución. Los trabajos incluirían '
        || 'la limpieza y consolidación de la piedra caliza, la rehabilitación de los '
        || 'balcones de forja y la actualización de la iluminación del conjunto para '
        || 'resaltar su valor patrimonial en el corazón del casco histórico.',
    'https://madrid365.es/wp-content/uploads/2020/08/Ayuntamiento-Aranjuez.jpg',
    '2026-05-20',
    '2026-06-20',
    5,
    25
    WHERE NOT EXISTS (
    SELECT 1 FROM civic_questions
    WHERE title = 'Renovación de la fachada del Ayuntamiento de Aranjuez'
);

INSERT INTO objectives (question_id, objective, sort_order)
SELECT q.id, vals.objective, vals.sort_order
FROM civic_questions q
         CROSS JOIN (VALUES
                         ('Preservar el patrimonio arquitectónico del centro histórico', 0),
                         ('Mejorar la imagen representativa del municipio', 1),
                         ('Garantizar la seguridad estructural y la accesibilidad del edificio', 2)
) AS vals(objective, sort_order)
WHERE q.title = 'Renovación de la fachada del Ayuntamiento de Aranjuez'
  AND NOT EXISTS (
    SELECT 1 FROM objectives o WHERE o.question_id = q.id
);

-- 3. UPCOMING question: Carril bici Palacio Real - Jardín del Príncipe

INSERT INTO civic_questions (title, description, image_url, open_date, close_date, zone_id, neighbourhood_id)
SELECT
    'Ampliación del carril bici: Palacio Real - Jardín del Príncipe',
    'El Ayuntamiento de Aranjuez propone extender la red ciclista actual para '
        || 'conectar el acceso principal al Palacio Real con el Jardín del Príncipe a '
        || 'través de un carril bici separado del tráfico. La iniciativa busca reducir '
        || 'el flujo de vehículos en la zona declarada Patrimonio de la Humanidad y '
        || 'ofrecer una alternativa de movilidad sostenible para residentes y turistas.',
    'https://fly-over-the-world-media.s3.amazonaws.com/uploads/highlights/61d84dc4b552b-6322e0f7752c7_1280_720.jpg',
    '2026-09-01',
    '2026-11-30',
    5,
    25
    WHERE NOT EXISTS (
    SELECT 1 FROM civic_questions
    WHERE title = 'Ampliación del carril bici: Palacio Real - Jardín del Príncipe'
);

INSERT INTO objectives (question_id, objective, sort_order)
SELECT q.id, vals.objective, vals.sort_order
FROM civic_questions q
         CROSS JOIN (VALUES
                         ('Reducir el tráfico de vehículos en el entorno Patrimonio de la Humanidad', 0),
                         ('Fomentar el turismo sostenible y la movilidad activa', 1),
                         ('Conectar los principales puntos de interés del municipio en bicicleta', 2),
                         ('Mejorar la calidad del aire y reducir emisiones en la zona monumental', 3)
) AS vals(objective, sort_order)
WHERE q.title = 'Ampliación del carril bici: Palacio Real - Jardín del Príncipe'
  AND NOT EXISTS (
    SELECT 1 FROM objectives o WHERE o.question_id = q.id
);

-- Security alerts (6 examples in the context of Aranjuez)
-- Reference date: 2026-05-27. Active alerts have expires_at in the future.
-- zone_id / neighbourhood_id are soft references — values aligned with the zones service seed.
-- 3 active alerts, 3 already expired (kept for reference).

-- 1. IMPORTANT (active) — water cut in Casco Antiguo (whole zone)
INSERT INTO security_alerts (title, severity, description, latitude, longitude, zone_id, neighbourhood_id, created_at, expires_at)
SELECT 'Corte de agua en Casco Antiguo',
       'IMPORTANT',
       'Corte de suministro de agua potable en el Casco Antiguo por rotura de tubería en la Calle del Príncipe. '
           || 'Se estima la reparación para las 20:00 h. Camiones cisterna disponibles en la Plaza de la Constitución.',
       40.032500, -3.602778,
       5, NULL,
       '2026-05-27 08:00:00+00',
       '2026-05-27 22:00:00+00'
    WHERE NOT EXISTS (
    SELECT 1 FROM security_alerts
    WHERE description LIKE 'Corte de suministro de agua potable%'
);

-- 2. MEDIUM (active) — traffic cut near Palacio Real (neighbourhood level)
INSERT INTO security_alerts (title, severity, description, latitude, longitude, zone_id, neighbourhood_id, created_at, expires_at)
SELECT 'Corte de tráfico en Calle de la Reina',
       'MEDIUM',
       'Corte de tráfico en la Calle de la Reina por rodaje cinematográfico. '
           || 'Se habilitarán itinerarios alternativos por la Calle del Capitán y la Avenida de las Infantas.',
       40.038333, -3.605833,
       5, 25,
       '2026-05-27 06:00:00+00',
       '2026-05-28 14:00:00+00'
    WHERE NOT EXISTS (
    SELECT 1 FROM security_alerts
    WHERE description LIKE 'Corte de tráfico en la Calle de la Reina%'
);

-- 3. MILD (active) — works near Jardín del Príncipe
INSERT INTO security_alerts (title, severity, description, latitude, longitude, zone_id, neighbourhood_id, created_at, expires_at)
SELECT 'Obras en acera junto al Jardín del Príncipe',
       'MILD',
       'Obras de mantenimiento en la acera de la Calle de la Reina a la altura del Jardín del Príncipe. '
           || 'Acceso peatonal restringido al carril izquierdo. Previsión de fin: 30 de mayo.',
       40.041944, -3.598889,
       3, 16,
       '2026-05-26 07:00:00+00',
       '2026-05-30 18:00:00+00'
    WHERE NOT EXISTS (
    SELECT 1 FROM security_alerts
    WHERE description LIKE 'Obras de mantenimiento en la acera%'
);

-- 4. IMPORTANT (expired) — power outage resolved
INSERT INTO security_alerts (title, severity, description, latitude, longitude, zone_id, neighbourhood_id, created_at, expires_at)
SELECT 'Corte eléctrico en barrio de la Estación',
       'IMPORTANT',
       'Corte eléctrico en el barrio de la Estación por avería en la subestación de la Calle Infantas. '
           || 'Restaurado el suministro a las 11:30 h.',
       40.034444, -3.611667,
       5, 22,
       '2026-05-20 08:00:00+00',
       '2026-05-20 12:00:00+00'
    WHERE NOT EXISTS (
    SELECT 1 FROM security_alerts
    WHERE description LIKE 'Corte eléctrico en el barrio de la Estación%'
);

-- 5. MEDIUM (expired) — gas smell resolved
INSERT INTO security_alerts (title, severity, description, latitude, longitude, zone_id, neighbourhood_id, created_at, expires_at)
SELECT 'Aviso por olor a gas en Plaza de la Constitución',
       'MEDIUM',
       'Aviso preventivo por olor a gas en la Plaza de la Constitución. '
           || 'Operarios de Naturgy han localizado y reparado la fuga. Zona despejada.',
       40.032500, -3.602778,
       5, 25,
       '2026-05-15 14:00:00+00',
       '2026-05-15 17:00:00+00'
    WHERE NOT EXISTS (
    SELECT 1 FROM security_alerts
    WHERE description LIKE 'Aviso preventivo por olor a gas%'
);

-- 6. MILD (expired) — slippery path in Mar de Ontígola
INSERT INTO security_alerts (title, severity, description, latitude, longitude, zone_id, neighbourhood_id, created_at, expires_at)
SELECT 'Sendero del Mar de Ontígola en mal estado',
       'MILD',
       'Sendero principal del Mar de Ontígola en mal estado tras las lluvias. '
           || 'Se recomienda calzado de montaña. Estado normalizado tras el mantenimiento del día 10.',
       40.020556, -3.589722,
       2, NULL,
       '2026-05-08 09:00:00+00',
       '2026-05-10 20:00:00+00'
    WHERE NOT EXISTS (
    SELECT 1 FROM security_alerts
    WHERE description LIKE 'Sendero principal del Mar de Ontígola%'
);

-- ===== leisure =====
-- Seed data: model-city-leisure microservice
-- Context: Aranjuez (Comunidad de Madrid, España)
-- Safe to re-run: WHERE NOT EXISTS guards each block.

-- City places

INSERT INTO city_places (name, latitude, longitude, description, address, photo_url_1, photo_url_2, photo_url_3,
                         access_info, accessibility_info, category, visit_duration_minutes)
SELECT
    'Palacio Real de Aranjuez',
    40.038333, -3.605833,
    'Antigua residencia real de los Borbones a orillas del Tajo. Declarado Patrimonio de la Humanidad por la UNESCO en 2001 '
        || 'como parte del Paisaje Cultural de Aranjuez. Su salón de porcelana, el gabinete árabe y los jardines circundantes '
        || 'son visita obligada.',
    'Plaza de Parejas, s/n, 28300 Aranjuez, Madrid',
    'https://upload.wikimedia.org/wikipedia/commons/3/3b/Palacio_Real_de_Aranjuez_-_03.jpg',
    'https://upload.wikimedia.org/wikipedia/commons/0/05/Palacio_Real_de_Aranjuez_-_01.jpg',
    NULL,
    'Acceso peatonal desde la Plaza de Parejas. Aparcamiento público en Calle del Capitán. Apertura de martes a domingo, '
        || 'de 10:00 a 18:00 (verano hasta 20:00). Entrada gratuita para residentes de la UE los miércoles y jueves de 17:00 a 19:00.',
    'Entrada accesible por la fachada sur con rampa. Ascensor disponible para acceder a la planta principal. '
        || 'Aseos adaptados en la planta baja. Sillas de ruedas en préstamo en la taquilla.',
    'MONUMENT', 90
    WHERE NOT EXISTS (SELECT 1 FROM city_places WHERE name = 'Palacio Real de Aranjuez');

INSERT INTO city_places (name, latitude, longitude, description, address, photo_url_1, photo_url_2, photo_url_3,
                         access_info, accessibility_info, category, visit_duration_minutes)
SELECT
    'Jardín del Príncipe',
    40.041944, -3.598889,
    'Uno de los jardines históricos más extensos de Europa, encargado por Carlos IV. Más de 150 hectáreas a orillas del Tajo '
        || 'que albergan la Casa del Labrador, el Real Embarcadero y el Estanque de los Chinescos. Ideal para pasear, hacer '
        || 'picnic o avistar fauna.',
    'Calle de la Reina, s/n, 28300 Aranjuez, Madrid',
    'https://upload.wikimedia.org/wikipedia/commons/8/86/Jardin_del_Principe_Aranjuez.jpg',
    NULL, NULL,
    'Entrada principal por la Puerta de la Reina, en la Calle de la Reina. Abierto todos los días: invierno 8:00-18:30, '
        || 'verano 8:00-21:30. Entrada gratuita.',
    'Caminos principales pavimentados aptos para sillas de ruedas y carritos. Aseos adaptados junto a la Casa del Labrador.',
    'PARK', 120
    WHERE NOT EXISTS (SELECT 1 FROM city_places WHERE name = 'Jardín del Príncipe');

INSERT INTO city_places (name, latitude, longitude, description, address, photo_url_1, photo_url_2, photo_url_3,
                         access_info, accessibility_info, category, visit_duration_minutes)
SELECT
    'Casa del Labrador',
    40.043889, -3.594444,
    'Pequeño palacete neoclásico construido por Carlos IV dentro del Jardín del Príncipe. Destaca el gabinete de platino, '
        || 'la sala de billar y la riqueza de su decoración interior.',
    'Jardín del Príncipe, 28300 Aranjuez, Madrid',
    'https://upload.wikimedia.org/wikipedia/commons/6/6e/Casa_del_Labrador_Aranjuez.jpg',
    NULL, NULL,
    'Acceso a pie desde la Puerta de la Reina (unos 20 minutos andando). Visitas guiadas con reserva previa, de martes '
        || 'a domingo de 10:00 a 17:00.',
    'Acceso con rampa lateral. Recorrido interior parcialmente accesible: planta baja sí, planta alta solo por escalera.',
    'MONUMENT', 45
    WHERE NOT EXISTS (SELECT 1 FROM city_places WHERE name = 'Casa del Labrador');

INSERT INTO city_places (name, latitude, longitude, description, address, photo_url_1, photo_url_2, photo_url_3,
                         access_info, accessibility_info, category, visit_duration_minutes)
SELECT
    'Jardín de la Isla',
    40.038056, -3.609444,
    'Jardín renacentista del siglo XVI rodeado por el río Tajo y la ría. Famoso por sus fuentes mitológicas (Hércules, '
        || 'Apolo, Baco) y su frondosa arboleda.',
    'Plaza de Parejas, s/n, 28300 Aranjuez, Madrid',
    'https://upload.wikimedia.org/wikipedia/commons/1/13/Jardin_de_la_Isla_Aranjuez.jpg',
    NULL, NULL,
    'Acceso libre junto al Palacio Real. Horario invierno 8:00-18:30, verano 8:00-21:30.',
    'Caminos de tierra compactada, accesibles con asistencia. Sin barreras arquitectónicas significativas.',
    'PARK', 60
    WHERE NOT EXISTS (SELECT 1 FROM city_places WHERE name = 'Jardín de la Isla');

INSERT INTO city_places (name, latitude, longitude, description, address, photo_url_1, photo_url_2, photo_url_3,
                         access_info, accessibility_info, category, visit_duration_minutes)
SELECT
    'Plaza de Toros de Aranjuez',
    40.034167, -3.601389,
    'Plaza de toros del siglo XVIII, una de las más antiguas de España aún en uso. Alberga el Museo Taurino con piezas '
        || 'de los grandes maestros que pisaron su albero.',
    'Calle Naranja, 7, 28300 Aranjuez, Madrid',
    'https://upload.wikimedia.org/wikipedia/commons/8/8a/Plaza_de_toros_de_Aranjuez.jpg',
    NULL, NULL,
    'Visitas al museo de miércoles a domingo, 11:00 a 14:00 y 17:00 a 20:00. Entrada con descuento para residentes.',
    'Acceso principal sin escalones. Aseos adaptados. Plazas reservadas para personas con movilidad reducida en la grada.',
    'MONUMENT', 40
    WHERE NOT EXISTS (SELECT 1 FROM city_places WHERE name = 'Plaza de Toros de Aranjuez');

INSERT INTO city_places (name, latitude, longitude, description, address, photo_url_1, photo_url_2, photo_url_3,
                         access_info, accessibility_info, category, visit_duration_minutes)
SELECT
    'Plaza de la Constitución',
    40.032500, -3.602778,
    'Plaza porticada del siglo XVIII que articula el casco histórico, sede del Ayuntamiento de Aranjuez y de la Casa de '
        || 'Comedios. Punto de encuentro habitual para vecinos y visitantes.',
    'Plaza de la Constitución, 28300 Aranjuez, Madrid',
    'https://madrid365.es/wp-content/uploads/2020/08/Ayuntamiento-Aranjuez.jpg',
    NULL, NULL,
    'Acceso peatonal libre las 24 horas. Conexión directa con la Calle del Príncipe y la Calle Stuart.',
    'Plaza completamente llana, pavimentada y libre de barreras. Aparcamiento para personas con movilidad reducida en '
        || 'la Calle del Príncipe.',
    'SQUARE', 20
    WHERE NOT EXISTS (SELECT 1 FROM city_places WHERE name = 'Plaza de la Constitución');

INSERT INTO city_places (name, latitude, longitude, description, address, photo_url_1, photo_url_2, photo_url_3,
                         access_info, accessibility_info, category, visit_duration_minutes)
SELECT
    'Museo de Falúas Reales',
    40.038889, -3.608056,
    'Museo a orillas del Tajo que conserva las embarcaciones de recreo utilizadas por la familia real entre los siglos '
        || 'XVII y XX. Imprescindible para entender el papel del río en la vida palaciega.',
    'Jardín del Príncipe, 28300 Aranjuez, Madrid',
    'https://upload.wikimedia.org/wikipedia/commons/9/9f/Museo_de_Faluas_Reales_Aranjuez.jpg',
    NULL, NULL,
    'Abierto de martes a domingo, 10:00 a 18:00. Entrada conjunta con la Casa del Labrador disponible.',
    'Entrada accesible con rampa. Espacio expositivo en una sola planta sin desniveles.',
    'MUSEUM', 30
    WHERE NOT EXISTS (SELECT 1 FROM city_places WHERE name = 'Museo de Falúas Reales');

INSERT INTO city_places (name, latitude, longitude, description, address, photo_url_1, photo_url_2, photo_url_3,
                         access_info, accessibility_info, category, visit_duration_minutes)
SELECT
    'Estación de Aranjuez (Tren de la Fresa)',
    40.034444, -3.611667,
    'Estación histórica de estilo neomudéjar, terminal del célebre Tren de la Fresa que reproduce el segundo trayecto '
        || 'ferroviario más antiguo de España. Durante el viaje azafatas ofrecen fresas con nata.',
    'Calle Infantas, 1, 28300 Aranjuez, Madrid',
    'https://upload.wikimedia.org/wikipedia/commons/c/c9/Estacion_de_Aranjuez.jpg',
    NULL, NULL,
    'Tren de la Fresa: fines de semana de mayo a octubre, salida desde Madrid-Príncipe Pío. Recomendado reservar online '
        || 'con antelación.',
    'Vestíbulo con ascensor y rampa de acceso a los andenes. Tren con vagón habilitado para personas con movilidad reducida.',
    'STATION', 30
    WHERE NOT EXISTS (SELECT 1 FROM city_places WHERE name = 'Estación de Aranjuez (Tren de la Fresa)');

INSERT INTO city_places (name, latitude, longitude, description, address, photo_url_1, photo_url_2, photo_url_3,
                         access_info, accessibility_info, category, visit_duration_minutes)
SELECT
    'Mar de Ontígola',
    40.020556, -3.589722,
    'Reserva natural de unas 56 hectáreas en torno a un embalse del siglo XVI construido para abastecer las fuentes de '
        || 'los jardines del Palacio Real. Espacio protegido con miradores, sendas y observatorios de aves.',
    'Camino del Deleite, 28300 Aranjuez, Madrid',
    'https://upload.wikimedia.org/wikipedia/commons/0/03/Mar_de_Ontigola.jpg',
    NULL, NULL,
    'Acceso libre por el Camino del Deleite. Aparcamiento gratuito junto al área recreativa. Recomendado calzado cómodo.',
    'Sendero principal de unos 800 m con pavimento compactado, apto para sillas de ruedas con asistencia. Mirador '
        || 'principal accesible.',
    'NATURE', 90
    WHERE NOT EXISTS (SELECT 1 FROM city_places WHERE name = 'Mar de Ontígola');

-- City routes

INSERT INTO city_routes (name, description, target_audience, image_url, estimated_duration_minutes)
SELECT
    'Aranjuez Real: Patrimonio de la Humanidad',
    'Recorrido clásico por los principales hitos de la Aranjuez palaciega: el Palacio Real, sus jardines históricos y '
        || 'los museos vinculados a la corte. Ideal para una jornada completa con una visión integral del conjunto UNESCO.',
    'TOURIST',
    'https://upload.wikimedia.org/wikipedia/commons/3/3b/Palacio_Real_de_Aranjuez_-_03.jpg',
    300
    WHERE NOT EXISTS (SELECT 1 FROM city_routes WHERE name = 'Aranjuez Real: Patrimonio de la Humanidad');

INSERT INTO city_route_places (route_id, place_id, sort_order)
SELECT r.id, p.id, vals.sort_order
FROM city_routes r
         JOIN (VALUES
                   ('Palacio Real de Aranjuez',    0),
                   ('Jardín de la Isla',           1),
                   ('Museo de Falúas Reales',      2),
                   ('Jardín del Príncipe',         3),
                   ('Casa del Labrador',           4)
) AS vals(place_name, sort_order) ON TRUE
         JOIN city_places p ON p.name = vals.place_name
WHERE r.name = 'Aranjuez Real: Patrimonio de la Humanidad'
  AND NOT EXISTS (
    SELECT 1 FROM city_route_places crp
    WHERE crp.route_id = r.id AND crp.place_id = p.id
);

INSERT INTO city_routes (name, description, target_audience, image_url, estimated_duration_minutes)
SELECT
    'Aranjuez en familia: jardines y trenes',
    'Ruta pensada para disfrutar con niños: amplios jardines donde correr, el Tren de la Fresa y el Museo de Falúas '
        || 'Reales. Tramos cortos y muchas zonas de sombra.',
    'FAMILY',
    'https://upload.wikimedia.org/wikipedia/commons/8/86/Jardin_del_Principe_Aranjuez.jpg',
    240
    WHERE NOT EXISTS (SELECT 1 FROM city_routes WHERE name = 'Aranjuez en familia: jardines y trenes');

INSERT INTO city_route_places (route_id, place_id, sort_order)
SELECT r.id, p.id, vals.sort_order
FROM city_routes r
         JOIN (VALUES
                   ('Estación de Aranjuez (Tren de la Fresa)', 0),
                   ('Jardín del Príncipe',                     1),
                   ('Museo de Falúas Reales',                  2),
                   ('Plaza de la Constitución',                3)
) AS vals(place_name, sort_order) ON TRUE
         JOIN city_places p ON p.name = vals.place_name
WHERE r.name = 'Aranjuez en familia: jardines y trenes'
  AND NOT EXISTS (
    SELECT 1 FROM city_route_places crp
    WHERE crp.route_id = r.id AND crp.place_id = p.id
);

INSERT INTO city_routes (name, description, target_audience, image_url, estimated_duration_minutes)
SELECT
    'Aranjuez accesible: ruta sin barreras',
    'Itinerario seleccionado por sus condiciones de accesibilidad: pavimentos llanos, rampas, ascensores y aseos '
        || 'adaptados. Permite recorrer el casco monumental sin desniveles relevantes.',
    'ACCESSIBLE',
    'https://madrid365.es/wp-content/uploads/2020/08/Ayuntamiento-Aranjuez.jpg',
    180
    WHERE NOT EXISTS (SELECT 1 FROM city_routes WHERE name = 'Aranjuez accesible: ruta sin barreras');

INSERT INTO city_route_places (route_id, place_id, sort_order)
SELECT r.id, p.id, vals.sort_order
FROM city_routes r
         JOIN (VALUES
                   ('Plaza de la Constitución',    0),
                   ('Palacio Real de Aranjuez',    1),
                   ('Museo de Falúas Reales',      2),
                   ('Plaza de Toros de Aranjuez',  3)
) AS vals(place_name, sort_order) ON TRUE
         JOIN city_places p ON p.name = vals.place_name
WHERE r.name = 'Aranjuez accesible: ruta sin barreras'
  AND NOT EXISTS (
    SELECT 1 FROM city_route_places crp
    WHERE crp.route_id = r.id AND crp.place_id = p.id
);

INSERT INTO city_routes (name, description, target_audience, image_url, estimated_duration_minutes)
SELECT
    'Aranjuez natural: Tajo y Mar de Ontígola',
    'Recorrido alternativo orientado al disfrute del paisaje natural: la reserva del Mar de Ontígola y los jardines '
        || 'históricos a orillas del Tajo. Recomendable a primera hora del día.',
    'NATURE',
    'https://upload.wikimedia.org/wikipedia/commons/0/03/Mar_de_Ontigola.jpg',
    270
    WHERE NOT EXISTS (SELECT 1 FROM city_routes WHERE name = 'Aranjuez natural: Tajo y Mar de Ontígola');

INSERT INTO city_route_places (route_id, place_id, sort_order)
SELECT r.id, p.id, vals.sort_order
FROM city_routes r
         JOIN (VALUES
                   ('Mar de Ontígola',     0),
                   ('Jardín de la Isla',   1),
                   ('Jardín del Príncipe', 2)
) AS vals(place_name, sort_order) ON TRUE
         JOIN city_places p ON p.name = vals.place_name
WHERE r.name = 'Aranjuez natural: Tajo y Mar de Ontígola'
  AND NOT EXISTS (
    SELECT 1 FROM city_route_places crp
    WHERE crp.route_id = r.id AND crp.place_id = p.id
);

-- Public spaces (Aranjuez sport centres) and reservable resources.

INSERT INTO public_spaces (name, description, address, latitude, longitude, photo_url_1, photo_url_2, photo_url_3, active)
SELECT
    'Polideportivo Las Olivas',
    'Instalación deportiva municipal en el barrio de Las Olivas. Dispone de campos de fútbol, pistas de pádel y tenis, '
        || 'así como una cancha polideportiva cubierta.',
    'Calle del Capitán Gómez Cortés, s/n, 28300 Aranjuez, Madrid',
    40.029722, -3.616667,
    'https://www.aranjuez.es/wp-content/uploads/2020/05/polideportivo-las-olivas.jpg',
    NULL, NULL, TRUE
    WHERE NOT EXISTS (SELECT 1 FROM public_spaces WHERE name = 'Polideportivo Las Olivas');

INSERT INTO public_spaces (name, description, address, latitude, longitude, photo_url_1, photo_url_2, photo_url_3, active)
SELECT
    'Polideportivo Municipal de Aranjuez',
    'Polideportivo municipal con piscina cubierta, gimnasio, pistas de pádel, tenis y baloncesto. Sede habitual de los '
        || 'eventos deportivos del municipio.',
    'Calle Almíbar, 65, 28300 Aranjuez, Madrid',
    40.035000, -3.605000,
    'https://www.aranjuez.es/wp-content/uploads/2020/05/polideportivo-municipal.jpg',
    NULL, NULL, TRUE
    WHERE NOT EXISTS (SELECT 1 FROM public_spaces WHERE name = 'Polideportivo Municipal de Aranjuez');

INSERT INTO reservable_resources (public_space_id, name, description, resource_type, active)
SELECT s.id, vals.name, vals.description, vals.resource_type, TRUE
FROM public_spaces s
         JOIN (VALUES
                   ('Campo de fútbol 7 - A',        'Campo de fútbol 7 con césped artificial.',        'FOOTBALL_FIELD'),
                   ('Pista de pádel - 1',           'Pista de pádel cubierta con iluminación LED.',    'PADEL_COURT'),
                   ('Pista de pádel - 2',           'Pista de pádel exterior.',                        'PADEL_COURT'),
                   ('Pista de tenis - 1',           'Pista de tenis de tierra batida.',                'TENNIS_COURT')
) AS vals(name, description, resource_type) ON TRUE
WHERE s.name = 'Polideportivo Las Olivas'
  AND NOT EXISTS (
    SELECT 1 FROM reservable_resources r
    WHERE r.public_space_id = s.id AND r.name = vals.name
);

INSERT INTO reservable_resources (public_space_id, name, description, resource_type, active)
SELECT s.id, vals.name, vals.description, vals.resource_type, TRUE
FROM public_spaces s
         JOIN (VALUES
                   ('Cancha de baloncesto - 1',     'Cancha cubierta de baloncesto reglamentaria.',    'BASKETBALL_COURT'),
                   ('Cancha de baloncesto - 2',     'Cancha exterior de baloncesto.',                  'BASKETBALL_COURT'),
                   ('Pista de pádel - 1',           'Pista de pádel cubierta.',                        'PADEL_COURT'),
                   ('Pista de tenis - 1',           'Pista de tenis de superficie dura.',              'TENNIS_COURT')
) AS vals(name, description, resource_type) ON TRUE
WHERE s.name = 'Polideportivo Municipal de Aranjuez'
  AND NOT EXISTS (
    SELECT 1 FROM reservable_resources r
    WHERE r.public_space_id = s.id AND r.name = vals.name
);

-- Events seeded against existing city_places.

INSERT INTO events (place_id, name, description, event_type, requires_ticket, paid, price, currency,
                    capacity, starts_at, ends_at, stripe_price_id, photo_url_1, active)
SELECT p.id,
       'Festival de Música Clásica de Aranjuez',
       'Ciclo anual de conciertos de música clásica celebrado en los jardines del Palacio Real. '
           || 'Programa con la Orquesta Sinfónica de la Comunidad de Madrid y solistas invitados.',
       'MUSIC', TRUE, TRUE, 25.00, 'EUR', 500,
       TIMESTAMP '2026-07-10 21:00:00', TIMESTAMP '2026-07-10 23:30:00',
       'price_1Td9opIOy3xJ982ilwk0bRcu',
       'https://upload.wikimedia.org/wikipedia/commons/3/3b/Palacio_Real_de_Aranjuez_-_03.jpg', TRUE
FROM city_places p
WHERE p.name = 'Palacio Real de Aranjuez'
  AND NOT EXISTS (SELECT 1 FROM events e WHERE e.name = 'Festival de Música Clásica de Aranjuez');

INSERT INTO events (place_id, name, description, event_type, requires_ticket, paid, price, currency,
                    capacity, starts_at, ends_at, stripe_price_id, photo_url_1, active)
SELECT p.id,
       'Noche en los Jardines del Príncipe',
       'Velada nocturna con DJs y food trucks en el Jardín del Príncipe. Entrada gratuita previa reserva.',
       'NIGHTLIFE', TRUE, FALSE, 0.00, 'EUR', 1500,
       TIMESTAMP '2026-08-15 22:00:00', TIMESTAMP '2026-08-16 02:00:00',
       NULL,
       'https://upload.wikimedia.org/wikipedia/commons/8/86/Jardin_del_Principe_Aranjuez.jpg', TRUE
FROM city_places p
WHERE p.name = 'Jardín del Príncipe'
  AND NOT EXISTS (SELECT 1 FROM events e WHERE e.name = 'Noche en los Jardines del Príncipe');

INSERT INTO events (place_id, name, description, event_type, requires_ticket, paid, price, currency,
                    capacity, starts_at, ends_at, stripe_price_id, photo_url_1, active)
SELECT p.id,
       'Teatro al aire libre: La vida es sueño',
       'Representación de la obra de Calderón de la Barca en la Plaza de la Constitución por la compañía '
           || 'Teatro del Real Sitio. Aforo limitado.',
       'PERFORMING_ARTS', TRUE, TRUE, 12.00, 'EUR', 200,
       TIMESTAMP '2026-06-20 20:00:00', TIMESTAMP '2026-06-20 22:00:00',
       'price_1Td9prIOy3xJ982i7njiUroO',
       'https://madrid365.es/wp-content/uploads/2020/08/Ayuntamiento-Aranjuez.jpg', TRUE
FROM city_places p
WHERE p.name = 'Plaza de la Constitución'
  AND NOT EXISTS (SELECT 1 FROM events e WHERE e.name = 'Teatro al aire libre: La vida es sueño');

INSERT INTO events (place_id, name, description, event_type, requires_ticket, paid, price, currency,
                    capacity, starts_at, ends_at, stripe_price_id, photo_url_1, active)
SELECT p.id,
       'Taller de fotografía de naturaleza',
       'Taller práctico de fotografía en la reserva del Mar de Ontígola. Imprescindible llevar cámara propia.',
       'HOBBIES', TRUE, TRUE, 35.00, 'EUR', 25,
       TIMESTAMP '2026-05-09 10:00:00', TIMESTAMP '2026-05-09 14:00:00',
       'price_1Td9qCIOy3xJ982iBu85Ljtm',
       'https://upload.wikimedia.org/wikipedia/commons/0/03/Mar_de_Ontigola.jpg', TRUE
FROM city_places p
WHERE p.name = 'Mar de Ontígola'
  AND NOT EXISTS (SELECT 1 FROM events e WHERE e.name = 'Taller de fotografía de naturaleza');

INSERT INTO events (place_id, name, description, event_type, requires_ticket, paid, price, currency,
                    capacity, starts_at, ends_at, stripe_price_id, photo_url_1, active)
SELECT p.id,
       'Foro de emprendimiento Aranjuez 2026',
       'Encuentro empresarial en el Palacio Real con ponencias sobre innovación, turismo cultural y patrimonio.',
       'BUSINESS', TRUE, TRUE, 80.00, 'EUR', 300,
       TIMESTAMP '2026-10-02 09:00:00', TIMESTAMP '2026-10-02 18:00:00',
       'price_1Td9pGIOy3xJ982i3uNwvXZj',
       'https://upload.wikimedia.org/wikipedia/commons/3/3b/Palacio_Real_de_Aranjuez_-_03.jpg', TRUE
FROM city_places p
WHERE p.name = 'Palacio Real de Aranjuez'
  AND NOT EXISTS (SELECT 1 FROM events e WHERE e.name = 'Foro de emprendimiento Aranjuez 2026');

INSERT INTO events (place_id, name, description, event_type, requires_ticket, paid, price, currency,
                    capacity, starts_at, ends_at, stripe_price_id, photo_url_1, active)
SELECT p.id,
       'Mercado de la Fresa y el Espárrago',
       'Feria gastronómica al aire libre con productores locales, catas de fresón y showcooking. '
           || 'Entrada libre sin necesidad de ticket.',
       'FOOD_AND_DRINK', FALSE, FALSE, 0.00, 'EUR', NULL,
       TIMESTAMP '2026-05-23 11:00:00', TIMESTAMP '2026-05-23 21:00:00',
       NULL,
       'https://upload.wikimedia.org/wikipedia/commons/c/c9/Estacion_de_Aranjuez.jpg', TRUE
FROM city_places p
WHERE p.name = 'Estación de Aranjuez (Tren de la Fresa)'
  AND NOT EXISTS (SELECT 1 FROM events e WHERE e.name = 'Mercado de la Fresa y el Espárrago');


-- Platform admin user
INSERT INTO users (id, name, email, address, neighbourhood_id, created_at, role)
VALUES ('auth0|6a10523ed9abf7db4105eb3f', 'admin@aranjuez.es', 'admin@aranjuez.es', null, 1, '2026-05-22 13:28:57.599615 +00:00', 'MODEL-CITY-PLATFORM-ADMIN')
    ON CONFLICT (id) DO NOTHING;
