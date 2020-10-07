COMMENT ON SCHEMA
   agi_gb2av_controlling
IS
   'Dieses Schema wird für Controlling-Tabellen der amtlichen Vermessung und des Grundbuches verwendet. Fragen: stefan.ziegler@bd.so.ch'
;
GRANT USAGE ON SCHEMA agi_gb2av_controlling TO public, ogc_server, sogis_service, gretl
;
GRANT SELECT ON ALL TABLES IN SCHEMA agi_gb2av_controlling TO public, ogc_server, sogis_service
;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA agi_gb2av_controlling TO gretl
;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA agi_gb2av_controlling TO gretl
;