# Getting Started

### Setup
This project uses Spring Boot, JOOQ and PostgreSQL. It contains an embedded Maven executable, in case it is not installed.
Before the project can be run, the database needs to be setup. To do so:

1. Install PostgreSQL [PostgreSQL](https://www.postgresql.org/) with default port `5432`. This project was tested with Version 17.
2. Create a user "postgres" with password "admin". If another user/pw-setup is used, the `application.properties` file
   as well as the `pom.xml` file need to be adjusted accordingly.
3. Create the database "Tree", either with pgAdmin or with the following SQL statement:
    ```
    CREATE DATABASE "Tree"
      WITH
      OWNER = postgres
      ENCODING = 'UTF8'
      LOCALE_PROVIDER = 'libc'
      TABLESPACE = pg_default
      CONNECTION LIMIT = -1
    
    GRANT TEMPORARY, CONNECT ON DATABASE "Tree" TO PUBLIC;
    
    GRANT ALL ON DATABASE "Tree" TO postgres;
    ```
4. This project uses a test DB for its tests. Create the database "TreeTest", either with pgAdmin or with the following SQL statement:
    ```
    CREATE DATABASE "TreeTest"
        WITH
        OWNER = postgres
        ENCODING = 'UTF8'
        LC_COLLATE = 'German_Austria.1252'
        LC_CTYPE = 'German_Austria.1252'
        LOCALE_PROVIDER = 'libc'
        TABLESPACE = pg_default
        CONNECTION LIMIT = -1
        IS_TEMPLATE = False;
    
    GRANT TEMPORARY, CONNECT ON DATABASE "TreeTest" TO PUBLIC;
    
    GRANT ALL ON DATABASE "TreeTest" TO postgres;
    ```
  
After the database is setup accordingly, the project can be built with `mvn clean install`. This project contains a
Maven Wrapper to run the project without Maven installed. To do so, use `./mvnw clean install` instead. Afterwards, the
service can be started within the IDE via `TreeServiceApplication` class that will start up the application with an 
embedded web server. The local web server by default will run on port 8080. The endpoints will therefore by default be 
listening with base path `http://localhost:8080/tree`.

### Endpoints
See the [Postman collection file](postman/API_documentation.postman_collection.json).
