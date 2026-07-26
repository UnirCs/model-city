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

-- Security alerts (16 examples in the context of Aranjuez)
-- Reference date: 2026-05-27. Active alerts have expires_at in the future.
-- zone_id / neighbourhood_id are soft references — values aligned with the zones service seed.

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

-- 7. IMPORTANT (active) — fire in industrial area
INSERT INTO security_alerts (title, severity, description, latitude, longitude, zone_id, neighbourhood_id, created_at, expires_at)
SELECT 'Incendio en Polígono Industrial',
       'IMPORTANT',
       'Incendio en nave industrial del Polígono de Moreras. Bomberos trabajando en el lugar. '
       || 'Evitar la zona. No se registran heridos.',
       40.025000, -3.620000,
       2, 13,
       '2026-06-06 10:30:00+00',
       '2026-06-07 18:00:00+00'
WHERE NOT EXISTS (
    SELECT 1 FROM security_alerts
    WHERE description LIKE 'Incendio en nave industrial%'
);

-- 8. MEDIUM (active) — road closure for event
INSERT INTO security_alerts (title, severity, description, latitude, longitude, zone_id, neighbourhood_id, created_at, expires_at)
SELECT 'Calle cerrada por festividad local',
       'MEDIUM',
       'Calle de la Reina cerrada al tráfico por celebración de la Festividad de San Fernando. '
       || 'Desvío por Calle del Capitán y Avenida de Andalucía.',
       40.038333, -3.605833,
       5, 25,
       '2026-06-06 08:00:00+00',
       '2026-06-06 24:00:00+00'
WHERE NOT EXISTS (
    SELECT 1 FROM security_alerts
    WHERE description LIKE 'Calle de la Reina cerrada al tráfico por celebración%'
);

-- 9. MILD (active) — park maintenance
INSERT INTO security_alerts (title, severity, description, latitude, longitude, zone_id, neighbourhood_id, created_at, expires_at)
SELECT 'Mantenimiento en Jardín de la Isla',
       'MILD',
       'Trabajos de poda y limpieza en el Jardín de la Isla. '
       || 'Algunos senderos temporalmente cerrados. Acceso restringido a zonas seguras.',
       40.038056, -3.609444,
       5, 25,
       '2026-06-05 07:00:00+00',
       '2026-06-12 19:00:00+00'
WHERE NOT EXISTS (
    SELECT 1 FROM security_alerts
    WHERE description LIKE 'Trabajos de poda y limpieza en el Jardín de la Isla%'
);

-- 10. IMPORTANT (active) — bridge inspection
INSERT INTO security_alerts (title, severity, description, latitude, longitude, zone_id, neighbourhood_id, created_at, expires_at)
SELECT 'Inspección de puente sobre el Tajo',
       'IMPORTANT',
       'Inspección técnica del puente de Barcas. Circulación restringida a un solo carril. '
       || 'Previsibles retenciones en horario punta.',
       40.032500, -3.602778,
       5, 25,
       '2026-06-06 06:00:00+00',
       '2026-06-09 20:00:00+00'
WHERE NOT EXISTS (
    SELECT 1 FROM security_alerts
    WHERE description LIKE 'Inspección técnica del puente de Barcas%'
);

-- 11. MEDIUM (active) — public transport disruption
INSERT INTO security_alerts (title, severity, description, latitude, longitude, zone_id, neighbourhood_id, created_at, expires_at)
SELECT 'Alteración servicio de autobuses',
       'MEDIUM',
       'Servicio de autobuses urbanos alterado por obras en Avenida de la Estación. '
       || 'Líneas 1 y 2 con itinerario modificado. Consultar paradas alternativas.',
       40.034444, -3.611667,
       5, 22,
       '2026-06-06 09:00:00+00',
       '2026-06-20 18:00:00+00'
WHERE NOT EXISTS (
    SELECT 1 FROM security_alerts
    WHERE description LIKE 'Servicio de autobuses urbanos alterado%'
);

-- 12. MILD (active) — mosquito control
INSERT INTO security_alerts (title, severity, description, latitude, longitude, zone_id, neighbourhood_id, created_at, expires_at)
SELECT 'Tratamiento antimosquitos en zonas húmedas',
       'MILD',
       'Tratamiento de control de mosquitos en zonas húmedas del Mar de Ontígola. '
       || 'Evitar exposición directa durante las 24 horas siguientes.',
       40.020556, -3.589722,
       2, NULL,
       '2026-06-06 05:00:00+00',
       '2026-06-07 05:00:00+00'
WHERE NOT EXISTS (
    SELECT 1 FROM security_alerts
    WHERE description LIKE 'Tratamiento de control de mosquitos%'
);

-- 13. IMPORTANT (expired) — chemical spill
INSERT INTO security_alerts (title, severity, description, latitude, longitude, zone_id, neighbourhood_id, created_at, expires_at)
SELECT 'Derrame químico en zona industrial',
       'IMPORTANT',
       'Derrame de productos químicos en nave del Polígono. Zona acordonada por Protección Civil. '
       || 'Situación controlada sin riesgo para la población.',
       40.025000, -3.620000,
       2, 13,
       '2026-05-25 14:00:00+00',
       '2026-05-25 20:00:00+00'
WHERE NOT EXISTS (
    SELECT 1 FROM security_alerts
    WHERE description LIKE 'Derrame de productos químicos%'
);

-- 14. MEDIUM (expired) — train delay
INSERT INTO security_alerts (title, severity, description, latitude, longitude, zone_id, neighbourhood_id, created_at, expires_at)
SELECT 'Retrasos en servicio de tren',
       'MEDIUM',
       'Retrasos de hasta 30 minutos en el servicio de Cercanías por avería técnica. '
       || 'Servicio restablecido con normalidad.',
       40.034444, -3.611667,
       5, 22,
       '2026-05-28 07:30:00+00',
       '2026-05-28 12:00:00+00'
WHERE NOT EXISTS (
    SELECT 1 FROM security_alerts
    WHERE description LIKE 'Retrasos de hasta 30 minutos en el servicio de Cercanías%'
);

-- 15. MILD (expired) — fallen tree
INSERT INTO security_alerts (title, severity, description, latitude, longitude, zone_id, neighbourhood_id, created_at, expires_at)
SELECT 'Árbol caído en Paseo de la Princesa',
       'MILD',
       'Árbol caído por viento fuerte en el Paseo de la Princesa. '
       || 'Servicios municipales retirando restos. Vía parcialmente habilitada.',
       40.036000, -3.608000,
       5, 25,
       '2026-05-30 16:00:00+00',
       '2026-05-30 20:00:00+00'
WHERE NOT EXISTS (
    SELECT 1 FROM security_alerts
    WHERE description LIKE 'Árbol caído por viento fuerte%'
);

-- 16. MEDIUM (expired) — market relocation
INSERT INTO security_alerts (title, severity, description, latitude, longitude, zone_id, neighbourhood_id, created_at, expires_at)
SELECT 'Mercado municipal reubicado temporalmente',
       'MEDIUM',
       'Mercado municipal reubicado a Plaza de San Antonio por obras de reforma. '
       || 'Servicio normalizado en ubicación provisional.',
       40.033000, -3.605000,
       5, 25,
       '2026-05-20 08:00:00+00',
       '2026-06-15 18:00:00+00'
WHERE NOT EXISTS (
    SELECT 1 FROM security_alerts
    WHERE description LIKE 'Mercado municipal reubicado%'
);
