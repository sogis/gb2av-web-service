CREATE OR REPLACE VIEW agi_gb2av_controlling_pub.controlling_av2gb_mutationen_v AS
    SELECT 
        t_id,
        t_ili_tid,
        mutationsnummer,
        nbident,
        beschrieb,
        dateinameplan,
        endetechnbereit,
        CAST(meldungen AS text),
        grundbucheintrag,
        istprojektmutation,
        perimeter
    FROM 
       agi_gb2av_controlling_pub.controlling_av2gb_mutationen
;

COMMENT ON SCHEMA
   agi_gb2av_controlling_pub
IS
   'Dieses Schema wird für Controlling-Tabellen der amtlichen Vermessung und des Grundbuches verwendet. Fragen: stefan.ziegler@bd.so.ch'
;
GRANT USAGE ON SCHEMA agi_gb2av_controlling_pub TO public, ogc_server, sogis_service, gretl
;
GRANT SELECT ON ALL TABLES IN SCHEMA agi_gb2av_controlling_pub TO public, ogc_server, sogis_service
;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA agi_gb2av_controlling_pub TO gretl
;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA agi_gb2av_controlling_pub TO gretl
;