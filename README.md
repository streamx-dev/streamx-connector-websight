# WebSight StreamX Connector

This project is designed to facilitate the publishing of resources from WebSight to StreamX.

## Modules

This project deliver modules:

### streamx-connector-websight

This bundle provides components responsible for publishing application and content resources to StreamX.


### streamx-connector-websight-blueprints

This bundle provides publication handlers for the StreamX Blueprints.

## Build

To build this project run:

```bash
mvn clean install
```

To build with local deployment run:

```bash 
mvn clean install -P autoInstallBundle
```

## Usage

Using the connector in the project requires adding proper bundles and the `streamx-connector-websight-blueprints` service user.

Example:

```json
{
  "bundles": [
    {
      "id": "org.jsoup:jsoup:1.16.1",
      "start-order": "25"
    },
    {
      "id": "org.apache.commons:commons-compress:1.25.0",
      "start-order": "25"
    },
    {
      "id": "org.apache.avro:avro:1.11.3",
      "start-order": "25"
    },
    {
      "id":"com.fasterxml.jackson.core:jackson-annotations:2.13.3",
      "start-order":"20"
    },
    {
      "id":"com.fasterxml.jackson.core:jackson-core:2.13.3",
      "start-order":"20"
    },
    {
      "id": "com.fasterxml.jackson.core:jackson-databind:2.13.3",
      "start-order": "20"
    },
    {
      "id":"com.fasterxml.jackson.core:jackson-annotations:2.15.3",
      "start-order":"20"
    },
    {
      "id":"com.fasterxml.jackson.core:jackson-core:2.15.3",
      "start-order":"20"
    },
    {
      "id": "com.fasterxml.jackson.core:jackson-databind:2.15.3",
      "start-order": "20"
    },
    {
      "id":"org.apache.commons:commons-lang3:3.12.0",
      "start-order":"25"
    },
    {
      "id":"org.apache.commons:commons-lang3:3.14.0",
      "start-order":"25"
    },
    {
      "id":"org.jboss.logging:jboss-logging:3.5.3.Final",
      "start-order":"25"
    },
    {
      "id": "dev.streamx:ingestion-client:${streamx.ingestion-client.version}",
      "start-order": "25"
    },
    {
      "id": "dev.streamx:streamx-connector-sling:${streamx.connector.sling.version}",
      "start-order": "25"
    },
    {
      "id": "dev.streamx:streamx-connector-websight:${streamx.connector.websight.version}",
      "start-order": "25"
    },
    {
      "id": "dev.streamx:streamx-connector-websight-blueprints:${streamx.connector.websight.version}",
      "start-order": "25"
    }
  ],
  "configurations": {
    "org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended~streamx-connector-websight-blueprints": {
      "user.mapping": [
        "streamx-connector-websight-blueprints=[streamx-connector-websight-blueprints]"
      ]
    },
    "dev.streamx.sling.connector.impl.StreamxPublicationServiceImpl": {
      "enabled": "$[env:STREAMX_PUBLICATION_ENABLE;default=false]"
    },
    "dev.streamx.sling.connector.impl.StreamxClientFactoryImpl": {
      "streamxUrl": "$[env:STREAMX_PUBLICATION_SERVER_URL]",
      "authToken": "$[env:STREAMX_PUBLICATION_AUTH_TOKEN]"
    }
  },
  "repoinit:TEXT|true": "@file"
}

```

Repo init with service user setup:

```
create service user streamx-connector-websight-blueprints with path system/websight

set ACL for streamx-connector-websight-blueprints
    allow   jcr:read    on /content,/published
end
```

Note: make sure you added `<filesInclude>*.json</filesInclude>` after base feature.
Your repo init entry will be added to resulting repo init script according to the order
of `<aggregate>` items. If you add `<filesInclude>*.json</filesInclude>` at the beginning
service user will be getting created before `/content` and `/published` resources will be crated
which will result with error. Example `slingfeature-maven-plugin` config:

```xml
<plugin>
  <groupId>org.apache.sling</groupId>
  <artifactId>slingfeature-maven-plugin</artifactId>
  <version>1.6.6</version>
  <extensions>true</extensions>
  <configuration>
    <aggregates>
      <aggregate>
        <classifier>my-classifier</classifier>
        <title>My project</title>
        <includeArtifact>
          <groupId>pl.ds.websight</groupId>
          <artifactId>websight-cms-ce-feature</artifactId>
          <version>${websight.cms.version}</version>
          <classifier>cms</classifier>
          <type>slingosgifeature</type>
        </includeArtifact>
        <includeArtifact>
          <groupId>pl.ds.websight</groupId>
          <artifactId>websight-cms-ce-feature</artifactId>
          <version>${websight.cms.version}</version>
          <classifier>oak-document-store</classifier>
          <type>slingosgifeature</type>
        </includeArtifact>
        <filesInclude>*.json</filesInclude>
      </aggregate>
    </aggregates>
  </configuration>
</plugin>
```

## Configuration

Useful configuration options:
https://github.com/apache/felix-dev/tree/master/configadmin-plugins/interpolation

Configure enabling publication (using environment
variable is useful to enable the requests only in runtime and not send them during e2e tests, etc).
Example:

```json
{
  "configurations": {
    "dev.streamx.sling.connector.impl.StreamxPublicationServiceImpl": {
      "enabled": "$[env:STREAMX_PUBLICATION_ENABLE;default=false]"
    }
  }
}
```

Configure client factory.
Example:

```json
{
  "configurations": {
    "dev.streamx.sling.connector.impl.StreamxClientFactoryImpl": {
      "streamxUrl": "$[env:STREAMX_PUBLICATION_SERVER_URL]",
      "authToken": "$[env:STREAMX_PUBLICATION_AUTH_TOKEN]"
    }
  }
}
```