COMMENT ON SCHEMA
   agi_gb2av_controlling
IS
   'Dieses Schema wird vom AWJF für die Publikation der Biotopbäume verwendet. Fragen: peter.senn@bd.so.ch, samuel.scheibler@vd.so.ch'
;
GRANT USAGE ON SCHEMA agi_gb2av_controlling TO public, ogc_server, sogis_service, gretl
;
GRANT SELECT ON ALL TABLES IN SCHEMA agi_gb2av_controlling TO public, ogc_server, sogis_service
;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA agi_gb2av_controlling TO gretl
;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA agi_gb2av_controlling TO gretl
;