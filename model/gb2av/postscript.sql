CREATE TABLE 
    agi_gb2av.CAMEL_MESSAGEPROCESSED 
    ( 
        id int GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
        processorName VARCHAR(255),
        messageId VARCHAR(255), 
        createdAt TIMESTAMP 
    )
;

COMMENT ON SCHEMA
    agi_gb2av
IS
    'Schema für das Verwalten der Vollzugsmeldungen des Grundbuchs. Fragen: andrea.luescher@bd.so.ch, stefan.ziegler@bd.so.ch'
;

GRANT USAGE ON SCHEMA agi_gb2av TO public, gretl;

GRANT SELECT ON ALL TABLES IN SCHEMA agi_gb2av TO public;

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA agi_gb2av TO gretl;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA agi_gb2av TO gretl;
