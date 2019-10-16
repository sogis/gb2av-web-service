# gb2av-web-service

## Beschreibung
Importiert die Vollzugsmeldungen des Grundbuches an die Nachführungsgeometer in die Edit-Datenbank. Die Vollzugsmeldungen (INTERLIS-Dateien) werden vom Infogrips-FTP heruntergeladen, auf AWS-S3 archiviert und anschliessend importiert.

Umgesetzt ist der Prozess als Apache Camel Pipeline, die in Spring boot läuft.

## Betriebsdokumentation
Bei jedem Git-Push wird mittels Travis das Docker-Image neu gebuildet und als `sogis/gb2av-web-service` mit den Tags `latest` und "Travis-Buildnummer" auf Docker Hub abgelegt. Auf der AGI-Testumgebung wird viertelstündlich das `latest`-Image deployed.

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
Apache Camel verwendet sogenannte Idempotent-Repositories. In diesen Repositories wird gespeichert, welche Nachricht in welchem Prozess / in welcher Route bereits behandelt wurde. Dazu muss eine Tabelle in der Datenbank angelegt werden. Der benötigte DDL-Befehl ist im `postscript.sql` gespeichert.

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
sogis/gb2av-web-service
```

### SQL (ili2pg)
```
ILI2PG_PATH=/Users/stefan/apps/ili2pg-4.3.0/ili2pg-4.3.0.jar  
java -jar ${ILI2PG_PATH} \
--dbschema agi_gb2av --models GB2AV \
--defaultSrsCode 2056 --createGeomIdx --createFk --createFkIdx --createEnumTabs --beautifyEnumDispName --createMetaInfo --createNumChecks --nameByTopic --strokeArcs \
--createBasketCol --createDatasetCol \
--createscript agi_gb2av.sql
```

Die Dateien `agi_gb2av.sql`, `prescript.sql` und `postscript.sql` liegen im Ordner `sql`.

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