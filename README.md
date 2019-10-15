# gb2av-web-service

## Betriebsdokumentation

### Konfiguration

### Persistenz

### Docker

### SQL (ili2pg)

## Entwicklerdokumentation

Lokale Datenbank mit Docker für den Datenimport:
```
docker run --rm --name edit-db -p 54321:5432 --hostname primary \
-e PG_DATABASE=edit  -e PG_PRIMARY_PORT=5432 -e PG_MODE=primary \
-e PG_USER=admin -e PG_PASSWORD=admin \
-e PG_PRIMARY_USER=repl -e PG_PRIMARY_PASSWORD=repl \
-e PG_ROOT_PASSWORD=secret \
-e PG_WRITE_USER=gretl -e PG_WRITE_PASSWORD=gretl \
-e PG_READ_USER=ogc_server -e PG_READ_PASSWORD=ogc_server \
-v /tmp:/pgdata \
sogis/oereb-db
```

Wir verwenden aus Bequemlichkeit das  `sogis/oereb-db` Image. Jedes andere PostgreSQL/PostGIS-Image tut es auch.

Create schema and tables creation ddl with ili2pg:
```
ILI2PG_PATH=/Users/stefan/apps/ili2pg-4.3.0/ili2pg-4.3.0.jar  
java -jar ${ILI2PG_PATH} \
--dbschema agi_gb2av --models GB2AV \
--defaultSrsCode 2056 --createGeomIdx --createFk --createFkIdx --createEnumTabs --beautifyEnumDispName --createMetaInfo --createNumChecks --nameByTopic --strokeArcs \
--createscript agi_gb2av.sql
```

