Aaf unmanaged extension
=======================

Build and fetch dependencies

```
mvn clean package -Dmaven.test.skip=true
```

Add dependencies into `NEO4J_HOME/plugins`

```
aaf-0.1-SNAPSHOT.jar
jackson-annotations-2.2.2.jar
jackson-core-2.2.2.jar
jackson-databind-2.2.2.jar
vertx-core-2.0.0-final.jar
```

Edit file NEO4J_HOME/conf/neo4j-server.properties
Add line : `org.neo4j.server.thirdparty_jaxrs_classes=fr.wseduc.aaf=/aaf`

