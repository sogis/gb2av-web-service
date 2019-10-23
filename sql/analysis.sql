SELECT
    *
FROM
(
    SELECT
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

