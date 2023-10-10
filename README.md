# Field Consents

## Background

Following approval of a Field Development Plan there are certain consents required to perform work within the Field. The Field Consents system is used to apply for these consents:
- Production
- Flaring and Venting

Following the initial consents a Field then follows the Annual Consents Exercise (ACE). More information on this process can be found here:

[Annual Consents Exercise](https://www.nstauthority.co.uk/licensing-consents/consents/annual-consents-exercise)

## Pre-requisites
- Java 17
- Node LTS + NPM
- [Docker for Windows](https://hub.docker.com/editions/community/docker-ce-desktop-windows)
  (See [Docker setup](https://confluence.fivium.co.uk/display/JAVA/Java+development+environment+setup#Javadevelopmentenvironmentsetup-Docker)
  for further information about adding your account to the `docker-users` group)

## Setup

>**Ensure that you have [Docker for Windows](https://hub.docker.com/editions/community/docker-ce-desktop-windows)
installed and running (or an alternative way of running docker).**

### 1. Generate Jooq types
```bash
chmod +x ./devtools/create_db_user.sh
chmod +x ./gradlew
./gradlew generateJooq
```

### 2. Run the backend services
```shell
docker-compose -f ./devtools/local-dev-compose.yml up -d
```

### 3. Add the required profile

#### Development
- In your IntelliJ run configuration for the Spring app, include `development` in your active profiles
- The following environment variables are required when using this profile:

| Environment Variable         | Description                                                               |
|------------------------------|---------------------------------------------------------------------------|
| **Digital Payments Library** |                                                                           |
| `GOV_UK_PAY_API_KEY`         | API Key for GOV.UK Pay - https://tpm.fivium.co.uk/index.php/pwd/view/2200 |

#### Production
- In your IntelliJ run configuration for the Spring app, include `production` in your active profiles
- The following environment variables are required when using this profile:

| Environment Variable             | Description                                                                                        |
|----------------------------------|----------------------------------------------------------------------------------------------------|
| **Service**                      |                                                                                                    |
| `FCS_SERVICE_BASE_URL`           | The service base URL excluding the context path (e.g. https://itportal.dev.fivium.local)           |
| `FCS_CONTEXT_PATH`               | The service URL context path (e.g. /fcs)                                                           |
|                                  |                                                                                                    |
| **Database**                     |                                                                                                    |
| `FCS_DATABASE_URL`               | The URL to the database the service connect to                                                     |
| `FCS_DATABASE_PASSWORD`          | Database schema password for the `fcs` user                                                        |
| `FCS_ENABLE_FLYWAY_OUT_OF_ORDER` | Set to `true` to allow flyway to run out of order, defaults to `false`                             |
|                                  |                                                                                                    |
| **SAML**                         |                                                                                                    |
| `FCS_SAML_ENTITY_ID`             | Fox instance URL (dev: https://itportal.dev.fivium.local/engedudev1/fox)                           |
| `FCS_SAML_CERTIFICATE`           | The x509 certificate string                                                                        |
| `FCS_SAML_LOGIN_URL`             | The URL to hit the `login` entry theme of the SAML login module                                    |
| `FCS_SAML_BASE_URL`              | The url prior to the `/${serverContext}` part of the url  (E.G: https://itportal.dev.fivium.local) |
|                                  |                                                                                                    |
| **File upload library**          |                                                                                                    |
| `S3_ACCESS_TOKEN`                | Access token to access for Amazon S3                                                               |
| `S3_SECRET_TOKEN`                | The accompanying secret token                                                                      |
| `S3_DEFAULT_BUCKET`              | The bucket where files will be uploaded to by default                                              |
| `S3_ENDPOINT`                    | The Amazon S3 endpoint. Defaults to `s3.eu-west-2.amazonaws.com`                                   |
| `S3_SIGNING_REGION`              | The signing region. Defaults to `eu-west-2`                                                        |
| `S3_PROXY_HOST`                  | Proxy host for Amazon S3                                                                           |
| `S3_PROXY_PORT`                  | Proxy port for Amazon S3                                                                           |
| `CLAMAV_HOST`                    | Host for ClamAv virus scanner                                                                      |
| `CLAMAV_PORT`                    | Port for ClamAv virus scanner                                                                      |
|                                  |                                                                                                    |
| **Digital Payments Library**     |                                                                                                    |
| `GOV_UK_PAY_API_KEY`             | API Key for GOV.UK Pay                                                                             |

### 4. Initialise the Fivium Design System
```bash
git submodule update --init --recursive
cd fivium-design-system-core && npm install && npx gulp build && cd ..
```

### 4a. Upgrade FDS (the developer doing the upgrade)
Update `.gitmodules` to reflect the new version of FDS, then
```bash
git submodule update --remote
cd fivium-design-system-core && npm install && npx gulp build && cd ..
```
To test that the update has worked locally you will need to rebuild the frontend into field consents, i.e.
```bash
npx gulp buildAll
```
> After upgrading FDS as above you should be committing the change to `.gitmodules` and a new commit hash for the `fivium-design-system-core` submodule only

### 4b. If you're a developer working on a project where another developer has upgraded FDS, you need to:
```bash
git submodule update
cd fivium-design-system-core && npm install && npx gulp build && cd ..
npx gulp buildAll
```

### 5. Build frontend components
```bash
npm install && npx gulp buildAll
```

### 6. Run the app
Create a run configuration for the Spring app and start the application.

The application will be running [here](http://localhost:8080/fcs)

## Development setup

### Checkstyle
1. In Intellij install the Checkstyle-IDEA plugin (from third-party repositories)
2. Go to File > Settings > Tools > Checkstyle
3. Click the plus icon under "Configuration File"
4. Select "Use a local Checkstyle file"
5. Select `devtools/checkstyle.xml`
6. Check the "Active" box next to the new profile

Note that Checkstyle rules are checked during the build process and any broken rules will fail the build.
