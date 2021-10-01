[![Build Status](https://travis-ci.org/sogis/gb2av-web-service.svg?branch=master)](https://travis-ci.org/sogis/gb2av-web-service)

# gb2av-web-service

## TODO
- Validierung Update `grundbucheintrag LIKE...` in `controlling_av2gb_mutationen`
- Validierung: Records werden erst INSERTED wenn es Geometrien dazu gibt. Sollte konzeptionell funktionieren. Sie tauchen eventuell einfach später in der Tabelle.
- Remove CurveToLine etc. in GDI (da AV ohne Kreisbogen)
- Layer in Web GIS Client erfassen
- Aufräumen 
- Doku nachführen 
  * GRETL-Job nachführen resp. alter GRETL-Job (`agi_gb2av`) löschen.
  * ...

## Beschreibung
Importiert die AV-Mutationen vom Nachführungsgeometer an das Grundbuch und die Vollzugsmeldungen des Grundbuches an die Nachführungsgeometer in die Edit-Datenbank. Die INTERLIS-Dateien werden vom Infogrips-FTP heruntergeladen, auf AWS-S3 archiviert und anschliessend importiert. Da die AV-Mutationen nur sehr kurz auf dem FTP-Server liegen, wird alle 10 Sekunden geprüft, ob neue Mutationen vorliegen. Es ist aber nicht klar, ob so wirklich alle Mutationen behandelt werden können. Die Vollzugsmeldungen werden nicht gelöscht und sind langfristig verfügbar.

Umgesetzt ist der Prozess als Apache Camel Pipeline, die in Spring Boot läuft.

Es steht ein RSS-Feed für die Vollzugsmeldungen zur Verfügung. Dieser kann z.B. in Outlook importiert werden damit man bei einer neuen Vollzugsmeldung benachrichtigt wird. Es werden jeweils die 100 aktuellsten Vollzugsmeldungen im RSS-Feed publiziert. Sie werden nicht nach NF-Geometer o.ä. gefiltert.

Siehe GRETL-Job [agi_gb2av_controlling](https://github.com/sogis/gretljobs/tree/master/agi_gb2av_controlling) für Informationen über die Auswertungen etc.

## Betriebsdokumentation
Bei jedem Git-Push wird mittels Travis das Docker-Image neu gebuildet und als `sogis/gb2av` mit den Tags `latest` und "Travis-Buildnummer" auf Docker Hub abgelegt. Auf der AGI-Testumgebung wird viertelstündlich das `latest`-Image deployed.

### Konfiguration
Die Datenbankverbindungsparameter (ohne Benutzer und Passwort) werden über Spring Boot Profile gesteuert. Für jede Umgebung gibt es eine `application-[dev|test|int|prod].properties`-Datei. Diese spezielle, zur "normalen" Properties-Datei zusätzliche Datei kann mit der speziellen Spring-Boot-Umgebungsvariable `SPRING_PROFILES_ACTIVE` gesteuert werden. Zum jetzigen Zeitpunkt werden diese Properties-Dateien in das Image gebrannt.

Zusätzlich müssen ENV-Variablen für Logins gesetzt werden und die korrekte Timezone:

- awsAccessKey
- awsSecretKey
- ftpUserInfogrips
- ftpPwdInfogrips
- dbUser
- dbPwd
- TZ  = Europe/Amsterdam

### Persistenz
Apache Camel verwendet sogenannte Idempotent-Repositories. In diesen Repositories wird gespeichert, welche Nachricht in welchem Prozess / in welcher Route bereits behandelt wurde. Dazu muss eine Tabelle in der Datenbank angelegt werden. Der benötigte DDL-Befehl ist im `postscript.sql` gespeichert. Das verwendete JDBC-Idempotent-Repository ist nicht besonders performant: Bei sehr vielen Dateien auf dem FTP-Server macht er vor jedem Pull für jede einzelne Datei ein `SELECT`, was früher oder später lange dauern kann.

### Zusätzliche DB-Tabelle(n)
Entgegen der Gepflogenheit, dass alle DB-Tabellen mit INTERLIS modelliert werden müssen, gibt es eine Auswertungstabelle, die "manuell" angelegt wird. Ebenfalls wie bei der Tabelle für das Idempotent-Repository steht das DDL in der `postscript.sql` Datei. Zu einem späteren Zeitpunkt soll diese Auswertungstabelle in einem INTERLIS-Modell für die sogenannten Fileverifikation aufgehen.

### Docker
```
docker run --restart always -p 8080:8080 \
-e "SPRING_PROFILES_ACTIVE=dev" \
-e "awsAccessKey=XXXXXX" \
-e "awsSecretKey=XXXXXX" \
-e "ftpUserInfogrips=XXXXXX" \
-e "ftpPwdInfogrips=XXXXXX" \
-e "dbUserEdit=XXXXXX" \
-e "dbPwdEdit=XXXXXX" \
-e "TZ=Europe/Amsterdam" \
sogis/gb2av
```

### SQL (ili2pg)

**ACHTUNG:** Die für die GDI verwendeteten Dateien liegen im Ordner `G:\sogis\daten_tools\skripte\db_schema_definition_edit\agi_gb2av(_controlling)`.

#### agi_gb2av
```
ILI2PG_PATH=/Users/stefan/apps/ili2pg-4.3.1/ili2pg-4.3.1.jar  
java -jar ${ILI2PG_PATH} \
--dbschema agi_gb2av --models GB2AV \
--defaultSrsCode 2056 --createGeomIdx --createFk --createFkIdx --createEnumTabs --beautifyEnumDispName --createMetaInfo --createNumChecks --nameByTopic --strokeArcs \
--createBasketCol --createDatasetCol \
--createscript agi_gb2av.sql
```

#### agi_gb2av_controlling
````
ILI2PG_PATH=/Users/stefan/apps/ili2pg-4.3.1/ili2pg-4.3.1.jar  
java -jar /usr/local/ili2pg-4.3.1/ili2pg.jar \
--dbschema agi_gb2av_controlling_pub --models SO_AGI_GB2AV_Controlling_20201002 \
--defaultSrsCode 2056 --createGeomIdx --createFk --createFkIdx --createUnique --createEnumTabs --beautifyEnumDispName --createMetaInfo --createNumChecks --nameByTopic --strokeArcs \
--coalesceJson \
--modeldir ".;http://models.geo.admin.ch" \
--createscript agi_gb2av_controlling_pub.sql
````

### AWS-S3
Es gibt einen Benutzer `gb2av`, welcher der Gruppe `gb2av-group` gegehört. Der Grupps ist die Policy `gb2av-S3` zugewiesen:

```
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "s3:GetBucketLocation",
                "s3:ListAllMyBuckets"
            ],
            "Resource": "*"
        },
        {
            "Effect": "Allow",
            "Action": [
                "s3:PutObject",
                "s3:PutObjectAcl",
                "s3:GetObject",
                "s3:GetObjectAcl",
                "s3:DeleteObject"
            ],
            "Resource": [
                "arn:aws:s3:::ch.so.agi.av.gb2av-dev/*",
                "arn:aws:s3:::ch.so.agi.av.gb2av-test/*",
                "arn:aws:s3:::ch.so.agi.av.gb2av-int/*",
                "arn:aws:s3:::ch.so.agi.av.gb2av/*"
            ]
        },
        {
            "Effect": "Allow",
            "Action": [
                "s3:ListBucketMultipartUploads",
                "s3:AbortMultipartUpload",
                "s3:ListMultipartUploadParts"
            ],
            "Resource": [
                "arn:aws:s3:::ch.so.agi.av.gb2av-dev",
                "arn:aws:s3:::ch.so.agi.av.gb2av-dev/*",
                "arn:aws:s3:::ch.so.agi.av.gb2av-test",
                "arn:aws:s3:::ch.so.agi.av.gb2av-test/*",
                "arn:aws:s3:::ch.so.agi.av.gb2av-int",
                "arn:aws:s3:::ch.so.agi.av.gb2av-int/*",
                "arn:aws:s3:::ch.so.agi.av.gb2av",
                "arn:aws:s3:::ch.so.agi.av.gb2av/*"
            ]
        },
        {
            "Effect": "Allow",
            "Action": "s3:ListBucket",
            "Resource": "*"
        }
    ]
}
```

### GRETL-Job

Für das Controlling wird der GRETL-Job [https://github.com/sogis/gretljobs/tree/master/agi_gb2av](https://github.com/sogis/gretljobs/tree/master/agi_gb2av) benötigt. Er berechnet täglich die Differenz zwischen dem Grundbucheintrag (=Vollzugsmeldung) und dem heutigen Tag für Objekte der Tabelle `lsnachfuehrung` in der amtlichen Vermessung, die noch keinen Wert im Attribut `gbeintrag` haben. Neue werden hinzugefügt, bereits bestehende upgedatet ("UPSERT").

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

Wir verwenden aus Bequemlichkeit das `sogis/oereb-db` Image. Jedes andere PostgreSQL/PostGIS-Image tut es auch.

```
ILI2PG_PATH=/Users/stefan/apps/ili2pg-4.3.0/ili2pg-4.3.0.jar  
java -jar ${ILI2PG_PATH} \
--dbschema agi_gb2av --models GB2AV \
--defaultSrsCode 2056 --createGeomIdx --createFk --createFkIdx --createEnumTabs --beautifyEnumDispName --createMetaInfo --createNumChecks --nameByTopic --strokeArcs \
--createBasketCol --createDatasetCol \
--createscript agi_gb2av.sql
```

SQL für Jdbc Idempotency Repository:
```
CREATE TABLE 
    agi_gb2av.CAMEL_MESSAGEPROCESSED 
    ( 
        id int GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
        processorName VARCHAR(255),
        messageId VARCHAR(255), 
        createdAt TIMESTAMP 
    )
;
```
Muss im `postscript.sql` bei der Inbetriebnahme ausgeführt werden.

## Lokale adhoc Auswertung

### Import AV-Daten
Dazu müssen die AV-Daten mittels GRETL-Job in die Datenbank importiert werden. Die AV-Daten werden heruntergeladen, die Nachführungskreise ändern nie bis selten und werden hier im Repo als XTF vorgehalten. Den DB-Container eventuell mit einem länger lebenden `pgdata`-Verzeichnis starten.

```
docker-compose up
```

```
export ORG_GRADLE_PROJECT_dbUriEdit="jdbc:postgresql://edit-db/edit"
export ORG_GRADLE_PROJECT_dbUserEdit="gretl"
export ORG_GRADLE_PROJECT_dbPwdEdit="gretl"
```

```
./start-gretl.sh --docker-image sogis/gretl-runtime:latest --docker-network gb2av-web-service_gb2av --job-directory $(pwd)/dev/ -b build.gradle createSchemaAdminEinteilung importAdminEinteilung createSchemaCadastralSurveying replaceCadastralSurveyingData
```
### Import Vollzugsmeldungen
Der Importprozess der Vollzugsmeldungen wird in der IDE gestartet. Will man sich das Hochladen nach S3 ersparen, kommentiert man diese Route aus.

### SQL-Auswertungen
...

## Migrationen
### Controlling-Tabelle (non-INTERLIS zu INTERLIS)

```
java -jar /usr/local/ili2pg-4.3.1/ili2pg.jar \
--dbschema agi_gb2av_controlling --models SO_AGI_GB2AV_Controlling_20201002 \
--defaultSrsCode 2056 --createGeomIdx --createFk --createFkIdx --createUnique --createEnumTabs --beautifyEnumDispName --createMetaInfo --createNumChecks --nameByTopic --strokeArcs \
--modeldir ".;http://models.geo.admin.ch" \
--createscript agi_gb2av_controlling.sql
```

Mit dbeaver Daten minimal umbauen und dann als INSERT-Befehle exportieren:

```
SELECT 
    mutationsnummer,
    nbident,
    delta,
    gb_status,
    gb_bemerkungen,
    gb_grundbucheintrag
    gb_tagebucheintrag,
    gb_tagebuchbeleg,
    av_beschreibung,
    av_gueltigkeit,
    av_gueltigereintrag,
    av_firma,
    t_datasetname AS datasetname,
    '-' AS grundstuecksart
FROM 
    agi_gb2av.vollzugsmeldung_av_delta
;
```

```
java -jar /usr/local/ili2pg-4.3.1/ili2pg.jar \
--dbhost localhost --dbport 54321 --dbdatabase edit --dbusr admin --dbpwd admin \
--dbschema agi_gb2av_controlling --models SO_AGI_GB2AV_Controlling_20201002 \
--modeldir "./model/;http://models.geo.admin.ch" \
--export agi_gb2av_controlling_export.xtf
```

Schema löschen, anschliessend Testimport:

```
java -jar /usr/local/ili2pg-4.3.1/ili2pg.jar \
--dbhost localhost --dbport 54321 --dbdatabase edit --dbusr admin --dbpwd admin \
--dbschema agi_gb2av_controlling --models SO_AGI_GB2AV_Controlling_20201002 \
--modeldir "../model/;http://models.geo.admin.ch" \
--import agi_gb2av_controlling_export.xtf
```



