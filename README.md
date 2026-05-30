# AcademicApp

AcademicApp es una aplicacion Android para el seguimiento academico del alumnado, con backend Spring Boot y base de datos MySQL.

## Estructura

- `app/`: aplicacion Android nativa.
- `backend/`: API REST Spring Boot.
- `gradle/`, `gradlew`, `build.gradle.kts`: build Android.

## Requisitos Android

- Android Studio o Gradle Wrapper incluido.
- Backend AcademicApp arrancado y accesible desde el dispositivo.
- Java/Android toolchain compatible con el proyecto.

## Configurar URL de la API Android

La app usa `ACADEMIC_APP_API_URL` para generar `BuildConfig.API_BASE_URL`.

En debug, si no se define nada, usa:

```text
http://192.168.18.41:8080/
```

Para compilar contra otra API:

```powershell
.\gradlew.bat :app:assembleDebug -PACADEMIC_APP_API_URL=http://TU_IP:8080/
```

Para release, `ACADEMIC_APP_API_URL` es obligatorio.

## Compilar Android

```powershell
.\gradlew.bat :app:assembleDebug
```

## Backend

El backend esta en `backend/`.

Requisitos:

- Java 21.
- MySQL accesible.
- Configuracion local basada en `backend/application-local.example.properties`.

Arranque local:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Compilacion:

```powershell
cd backend
.\mvnw.cmd package -DskipTests
```

En produccion, el backend puede ejecutarse con:

```bash
java -jar target/AcademicApp-0.0.1-SNAPSHOT.jar
```

## Flujos principales a probar

- Login y cambio entre cuentas.
- Foto de perfil independiente por cuenta.
- Chat del curso y aviso de mensajes nuevos.
- Asignaturas > unidades > notas.
- Crear, editar y eliminar notas.
- Evaluacion por trimestre y descarga de boletin PDF.
- Progreso y graficas.
- Perfil y companeros del mismo curso.
