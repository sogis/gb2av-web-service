COMMENT ON SCHEMA
    agi_av_gb_admin_einteilung
IS
    'Erfassungsmodell für die administrativen Einteilungen (Grundbuchkreise und Nachführungskreise). Fragen: andrea.luescher@bd.so.ch, stefan.ziegler@bd.so.ch'
;

GRANT USAGE ON SCHEMA agi_av_gb_admin_einteilung TO public, gretl;

GRANT SELECT ON ALL TABLES IN SCHEMA agi_av_gb_admin_einteilung TO public;

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA agi_av_gb_admin_einteilung TO gretl;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA agi_av_gb_admin_einteilung TO gretl;
