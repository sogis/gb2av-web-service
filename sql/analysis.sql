WITH geometer AS 
(
    SELECT
        nfgemeinde.bfsnr,
        grundbuchkreis.nbident,
        standort.firma
    FROM
        agi_av_gb_admin_einteilung.grundbuchkreise_grundbuchkreis AS grundbuchkreis
        LEFT JOIN agi_av_gb_admin_einteilung.nachfuehrngskrise_gemeinde AS nfgemeinde
        ON nfgemeinde.bfsnr = grundbuchkreis.bfsnr
        LEFT JOIN agi_av_gb_admin_einteilung.nachfuehrngskrise_nachfuehrungsgeometer AS nfgeometer
        ON nfgeometer.t_id = nfgemeinde.r_geometer
        LEFT JOIN agi_av_gb_admin_einteilung.nachfuehrngskrise_standort AS standort
        ON standort.t_id = nfgemeinde.r_standort  
)
SELECT
    vollzugsgegenstand.t_id,
    vollzugsgegenstand.gb_mutnummer,
    vollzugsgegenstand.gb_nbident,
    vollzugsgegenstand.gb_t_datasetname,
    vollzugsgegenstand.gb_status,
    vollzugsgegenstand.gb_bemerkungen,
    vollzugsgegenstand.gb_grundbucheintrag,
    vollzugsgegenstand.gb_tagebucheintrag,
    vollzugsgegenstand.gb_tagebuchbeleg,
    lsnachfuerhung.beschreibung AS av_beschreibung,
    lsnachfuerhung.gueltigkeit AS av_gueltigkeit,
    lsnachfuerhung.gueltigereintrag AS av_gueltigereintrag,
    lsnachfuerhung.gbeintrag AS av_gbbeintrag,
    lsnachfuerhung.datum1 AS av_datum1,
    lsnachfuerhung.datum2 AS av_datum2,
    --projgrundstueck.nummer AS av_grundstuecknummer,
    --projgrundstueck.egris_egrid AS av_egris_egrid,
    geometer.firma AS av_firma
FROM
    (
        SELECT
            vollzugsgegenstand.t_id,
            mutationsnummer.nummer AS gb_mutnummer,
            mutationsnummer.nbident AS gb_nbident,
            vollzugsgegenstand.t_datasetname AS gb_t_datasetname,
            vollzugsgegenstand.astatus AS gb_status,
            vollzugsgegenstand.bemerkungen AS gb_bemerkungen,
            vollzugsgegenstand.grundbucheintrag AS gb_grundbucheintrag,
            vollzugsgegenstand.tagebucheintrag AS gb_tagebucheintrag,
            vollzugsgegenstand.tagebuchbeleg AS gb_tagebuchbeleg
        FROM
            agi_gb2av.vollzugsgegnstnde_vollzugsgegenstand AS vollzugsgegenstand
            LEFT JOIN agi_gb2av.mutationsnummer AS mutationsnummer
            ON mutationsnummer.vollzgsggnszgsggnstand_mutationsnummer = vollzugsgegenstand.t_id
        WHERE
            vollzugsgegenstand.astatus = 'Eintrag'
            AND 
            POSITION('SDR' IN mutationsnummer.nummer) = 0
            AND 
            POSITION('Fl' IN mutationsnummer.nummer) = 0
            AND 
            POSITION('LV95' IN mutationsnummer.nummer) = 0
    ) AS vollzugsgegenstand
    LEFT JOIN agi_dm01avso24.liegenschaften_lsnachfuehrung AS lsnachfuerhung
    ON (lsnachfuerhung.nbident = vollzugsgegenstand.gb_nbident AND lsnachfuerhung.identifikator = vollzugsgegenstand.gb_mutnummer)
    --LEFT JOIN agi_dm01avso24.liegenschaften_projgrundstueck AS projgrundstueck
    --ON projgrundstueck.entstehung = lsnachfuerhung.t_id
    LEFT JOIN geometer 
    ON geometer.nbident = vollzugsgegenstand.gb_nbident
WHERE
    gbeintrag IS NULL --AND lsnachfuerhung.t_id IS NOT NULL
ORDER BY 
    gb_grundbucheintrag DESC
;    
