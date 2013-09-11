## Summary

This module provides REST services for working with contacts, that are stored in directory service.

All services require basic authentication. Therefore, you should use HTTPS in production to avoid security gap.

## Framework

This module is based on [Spring][framework:spring] framework.

## Getting Started

To work on this project you can use: [Git][tool:git], [Maven][tool:maven], [Eclipse][tool:eclipse], [Tomcat][tool:tomcat] and [Open DJ][tool:opendj].

To run application locally, follow next steps:

1. Install and configure local directory service (for example, [Open DJ][tool:opendj]). File [test.ldif](https://github.com/grytsenko/contacts/blob/master/modules/rest/config/test.ldif) contains test data for directory service (password for all users is `pass`).
1. Build module and deploy it on web server.
1. Open `http://localhost:8080/contacts/my.json` in browser and enter your credentials for authentication.

## REST API

Each object has version. Versions can be compared for equality only.

### GET my.json

Returns contact of user.

##### JSON

```json
{"uid":"grytsenko","firstName":"Anton","lastName":"Grytsenko","photoUrl":"","mail":"grytsenko@test.com","phone":"3800000004","location":"Donetsk","version":"20130722110100Z"}
```

### GET coworkers.json

Returns contacts of people from one location with user.

##### JSON

```json
[{"uid":"ivanov","firstName":"Ivan","lastName":"Ivanov","photoUrl":"","mail":"ivanov@test.com","phone":"+3800000000","location":"Donetsk","version":"20130722110100Z"},
{"uid":"petrov","firstName":"Petr","lastName":"Petrov","photoUrl":"","mail":"petrov@test.ua.com","phone":"+3800000001","location":"Donetsk","version":"20130722110100Z"},
{"uid":"kuznetsov","firstName":"Kuzma","lastName":"Kuznetsov","photoUrl":"","mail":"kuznetsov@test.com","phone":"+3800000002","location":"Donetsk","version":"20130722110100Z"},
{"uid":"popov","firstName":"Pavel","lastName":"Popov","photoUrl":"","mail":"popov@test.com","phone":"","location":"Donetsk","version":"20130722110100Z"},
{"uid":"grytsenko","firstName":"Anton","lastName":"Grytsenko","photoUrl":"","mail":"grytsenko@test.com","phone":"+3800000004","location":"Donetsk","version":"20130722110100Z"}]
```

[framework:spring]: http://www.springsource.org/

[tool:git]: http://git-scm.com/
[tool:maven]: http://maven.apache.org/
[tool:tomcat]: http://tomcat.apache.org/
[tool:eclipse]: http://www.eclipse.org/
[tool:opendj]: http://forgerock.com/what-we-offer/open-identity-stack/opendj/
