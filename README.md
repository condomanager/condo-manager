# Condo Manager

## Overview

This project is **_REST API_** with operations to manage all the necessary activities in your condo, built using [Spring Boot](https://spring.io/projects/spring-boot). It runs on Java 8 and a MySQL or Postgres database (configurable on `src/main/resources/application.properties`). Dependecies are managed by [Apache Maven](https://maven.apache.org). 

A live sample version (using Postgres) of the master branch can be found on [Heroku](https://dashboard.heroku.com/apps) at the following adress: 

https://condo-manager-api-sample.herokuapp.com

For now, it's possible to:

* Create a new user profile
* Authenticate as an existent user
* Once authenticated as the system admin, manage the `groups` x `residences` hierarchy of the condominium (e.g. blocks and apartments on each block)
* Once authenticated as a concierge, register a new visit for a specific residence and authorize or deny the entrance of this visit
* Once authenticated as a dweller:
  * update its own user profile
  * see all visits registered for its own residence
  * manage a list of authorized visitor that should be allowed direclty when visiting

Check the wiki for more details about how it all works.
