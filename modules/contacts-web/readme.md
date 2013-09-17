# Summary

The web module provides access to information about contacts.

This module provides REST API for applications and web interface for users.

### Frameworks and Libraries

This module is based on [Spring][framework:spring] framework.

[Hibernate][library:hibernate] and [Spring JPA][library:spring-jpa] are used to access data from DB.

[Bootstrap][framework:bootstrap] is used as front-end framework for web application.
And [Thymeleaf][library:thymeleaf] is used as view technology.

### Getting Started

To work on this project you can use: [Git][tool:git], [Maven][tool:maven], [Tomcat][tool:tomcat], [Open DJ][tool:opendj] and [MySQL][tool:mysql]

To run application locally, follow next steps:

1. Install and configure DS (file `test.ldif` contains test data, password for all users is `pass`).
1. Install and configure RDBMS.
1. Build module and deploy it on web server.
1. Open `http://localhost:8080/contacts/`.

# REST API

### Secutiry

REST API requires basic authentication.

Authenticated user and current user are synonyms.

### Synchronization

If client application uses REST API for synchronization, then it should rely on UIDs and versions.

UID of object can not be changed during its lifetime.
Therefore, application can use UIDs to identify different objects.

Versions of objects can be compared for equality.
If versions are not equal then object should be synchronized.

### Services

`GET my.json` - returns contact of current user.

`GET coworkers.json` - returns contacts of coworkers of current user.

[framework:spring]: http://www.springsource.org/

[framework:bootstrap]: http://getbootstrap.com/
[library:thymeleaf]: http://www.thymeleaf.org/

[library:hibernate]: http://www.hibernate.org/
[library:spring-jpa]: http://projects.spring.io/spring-data-jpa/

[tool:git]: http://git-scm.com/
[tool:maven]: http://maven.apache.org/
[tool:tomcat]: http://tomcat.apache.org/
[tool:opendj]: http://opendj.forgerock.org/
[tool:mysql]: http://www.mysql.com/
