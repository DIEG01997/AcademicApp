# AcademicApp Backend

Backend Spring Boot para la app Android AcademicApp.

## Requisitos

- Java 21.
- MySQL accesible.
- Variables/propiedades locales configuradas.

## Configuracion local

El backend importa de forma opcional:

```text
./application-local.properties
```

Puedes partir de:

```text
application-local.example.properties
```

Propiedades importantes:

```properties
spring.datasource.url=jdbc:mysql://HOST:3306/seguimiento_academico?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=USUARIO
spring.datasource.password=PASSWORD
academicapp.jwt.secret=SECRETO_DE_AL_MENOS_32_CARACTERES
```

Tambien se pueden usar variables de entorno:

```text
ACADEMIC_APP_SHOW_SQL=false
ACADEMIC_APP_JWT_EXPIRATION_MS=86400000
```

## Arrancar

```powershell
.\mvnw.cmd spring-boot:run
```

## Compilar

```powershell
.\mvnw.cmd -DskipTests compile
```

## Notas de entrega

- `spring.jpa.show-sql` esta desactivado por defecto para evitar ruido en consola.
- El script SQL de la base de datos se mantiene fuera del backend, segun la decision del proyecto.
- El chat de curso solo devuelve mensajes enviados desde la fecha de registro del alumno autenticado.
