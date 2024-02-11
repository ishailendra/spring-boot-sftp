# SFTP Auto Configurer

The project is designed to make the process of setting up new SFTP flow between servers quick and easy with no code changes.

The project uses [Spring Integration](https://spring.io/projects/spring-integration) to achieve the SFTP functionality.

## Feature
Send file to remote directory

Copy files from remote directory

## How to Add new SFTP flow

### Using properties file

To add new SFTP flow, add the details about the server, remote directory, local directory, and schedule to run the process in the given format below.

```properties
## Server Details - Server 1 ##
sftp.server[0].name=rpiServer
sftp.server[0].host=raspberrypi
sftp.server[0].port=22
sftp.server[0].user=pi
sftp.server[0].password=
sftp.server[0].privateKeyPath=
sftp.server[0].privateKeyPassphrase=

## Inbound Flow Details - 1 ##
sftp.server[0].inbound[0].remoteDir=/home/pi/sftp/incoming/patient/
sftp.server[0].inbound[0].localDir=src/main/resources/rpi/incoming/patient/
sftp.server[0].inbound[0].schedule=* */3 * * * *

## Outbound Flow Details - 1 ##
sftp.server[0].outbound[0].remoteDir=/home/pi/sftp/outbound/patient/
sftp.server[0].outbound[0].localDir=src/main/resources/rpi/outbound/patient/
sftp.server[0].outbound[0].schedule=* */3 * * * *
sftp.server[0].outbound[0].tempDir=src/main/resources/rpi/outbound/patient/temp/
sftp.server[0].outbound[0].archDir=src/main/resources/rpi/outbound/patient/arch/

## Server Details - Server 2 ##
sftp.server[1].name=dellServer
sftp.server[1].host=192.168.1.10
sftp.server[1].port=22
sftp.server[1].user=shail
sftp.server[1].password=
sftp.server[1].privateKeyPath=
sftp.server[1].privateKeyPassphrase=

## Inbound Flow Details - 1 ##
sftp.server[1].inbound[0].remoteDir=/home/shail/sftp_dir/incoming/patient/
sftp.server[1].inbound[0].localDir=src/main/resources/dell/incoming/patient/
sftp.server[1].inbound[0].schedule=* */3 * * * *

## Outbound Flow Details - 1 ##
sftp.server[1].outbound[0].remoteDir=/home/shail/sftp_dir/outbound/patient/
sftp.server[1].outbound[0].localDir=src/main/resources/dell/outbound/patient/
sftp.server[1].outbound[0].schedule=* */3 * * * *
sftp.server[1].outbound[0].tempDir=src/main/resources/dell/outbound/patient/temp/
sftp.server[1].outbound[0].archDir=src/main/resources/dell/outbound/patient/arch/

## Inbound Flow Details - 2 ##
sftp.server[1].inbound[1].remoteDir=/home/shail/sftp_dir/incoming/claim/
sftp.server[1].inbound[1].localDir=src/main/resources/dell/incoming/claim/
sftp.server[1].inbound[1].schedule=* */3 * * * *

## Outbound Flow Details - 2 ##
sftp.server[1].outbound[1].remoteDir=/home/shail/sftp_dir/outbound/claim/
sftp.server[1].outbound[1].localDir=src/main/resources/dell/outbound/claim/
sftp.server[1].outbound[1].schedule=* */3 * * * *
sftp.server[1].outbound[1].tempDir=src/main/resources/dell/outbound/claim/temp/
sftp.server[1].outbound[1].archDir=src/main/resources/dell/outbound/claim/arch/
```
If a new server is needed to be added, then it should be added with the new index and should follow the same patter as above.
Similarly, for adding new inbound/outbound flow next index value should be used and properties should be added in the above format.

> **NOTE**
>  After adding the properties a restart of the service is needed.

### Using Controller

To register SFTP flow at runtime without restarting the services, REST APIs can be used via swagger.

**Swagger Url**

http://localhost:8978/swagger-ui/index.html

#### APIs to add new SFTP Flow

To register new SFTP server:

http://localhost:8978/api/register/server

```json
{
    "serverName":"dellServer",
    "host":"192.168.1.10",
    "port":22,
    "user":"shail",
    "password":"password"
}
```

To register outbound flow

http://localhost:8978/api/register/outbound
```json
{
    "remoteDir":"/home/shail/sftp_dir/outbound/claim/",
    "localDir":"src/main/resources/dell/outbound/claim/",
    "schedule":"* */3 * * * *",
    "serverName":"dellServer"
}
```

To register inbound flow

http://localhost:8978/api/register/inbound

```json
{
    "remoteDir":"/home/shail/sftp_dir/incoming/patient/",
    "localDir":"src/main/resources/dell/incoming/patient/",
    "schedule":"* */3 * * * *",
    "serverName":"dellServer"
}
```

> **NOTE**
> 
> 1. While registering new inbound or outbound flow ```serverName``` should be same as ```serverName``` provided for Server details.
> 2. If SFTP flow is added using REST APIs, still add the flow details in the properties file so that if application is restarted, it will be automatically picked up. 