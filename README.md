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

### 1. Run the backend services
- Ensure that you have [Docker for Windows](https://hub.docker.com/editions/community/docker-ce-desktop-windows)
  installed and running (or an alternative way of running docker).  
- Run the backing services defined in the `local-dev-compose.yml`. This can be done by clicking the run icon
  next to `services` when in the file.
  - If IntelliJ doesn't detect the file as a docker compose file automatically you may need to 
    [Associate docker-compose as file type](https://intellij-support.jetbrains.com/hc/en-us/community/posts/360009394620-Associate-docker-compose-as-file-type) manually.

### 2. Add the required profile

### Development
- In your IntelliJ run configuration for the Spring app, include `development` in your active profiles

### Production
- In your IntelliJ run configuration for the Spring app, include `production` in your active profiles
- The following environment variables are required when using this profile:

| Environment Variable  | Description                                    |
|-----------------------|------------------------------------------------|
| FCS_DATABASE_URL      | The URL to the database the service connect to |
| FCS_DATABASE_PASSWORD | Database schema password for the `fcs` user    |

### 3. Initialise the Fivium Design System
- `git submodule update --init --recursive`
- `cd fivium-design-system-core && npm install && npx gulp build && cd ..`

### 4. Build frontend components
- `npm install`
- `npx gulp buildAll`

### 5. Run the app
Create a run configuration for the Spring app and start the application.

The application will be running on `localhost:8080/fcs/<endpoint>`

## Development setup

### Checkstyle
1. In Intellij install the Checkstyle-IDEA plugin (from third-party repositories)
2. Go to File > Settings > Tools > Checkstyle 
3. Click the plus icon under "Configuration File"
4. Select "Use a local Checkstyle file"
5. Select `devtools/checkstyle.xml`
6. Check the "Active" box next to the new profile

Note that Checkstyle rules are checked during the build process and any broken rules will fail the build.

