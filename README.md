# UniWay - Plataforma Estudiantil Completa

Aplicación móvil Android con backend Spring Boot para la Universidad Pascual Bravo. Plataforma integral que incluye foro estudiantil, sistema de recomendaciones de profesores, autenticación segura y gestión de contenido académico.

## 🏗️ Arquitectura

```
App Android (Kotlin) ↔ API REST (Spring Boot) ↔ Base de Datos MySQL
                     ↕
              Sistema de Reacciones
```

## ✨ Características Principales

### 🔐 **Sistema de Autenticación Completo**
- **JWT Authentication** con roles diferenciados (Estudiante/Administración)
- **Registro con verificación por email** (sin campo de teléfono)
- **Recuperación de contraseña** funcional
- **Gestión de sesiones** persistente

### 📝 **Foro Estudiantil Avanzado**
- **Publicaciones** con tipos y prioridades
- **Sistema de reacciones inteligente** (like/dislike con toggle)
- **Comentarios completos** con edición y eliminación
- **Permisos granulares** (solo autor o admin puede editar/eliminar)
- **Moderación de contenido** por administración

### 👨‍🏫 **Sistema de Recomendaciones de Profesores**
- **Recomendaciones con calificación** (sistema de 1-5 estrellas)
- **Referencias detalladas** de estudiantes
- **Sistema de likes/dislikes** en recomendaciones
- **Filtros por materia** y búsqueda avanzada
- **Permisos de eliminación** (solo autor o admin)
- **Estadísticas personales** y globales

### 🛡️ **Seguridad y Permisos**
- **Validación de permisos** en frontend y backend
- **Tokens JWT** con expiración automática
- **Roles diferenciados** con funcionalidades específicas
- **Logs detallados** para debugging y auditoría

## 📱 Funcionalidades de la App Android

### Autenticación

- **Registro**: Selección de cargo (Estudiante, Administración) - **Campo de teléfono eliminado**
- **Verificación por email**: Sistema completo con códigos de verificación
- **Inicio de sesión**: Con correo institucional y contraseña
- **Recuperación de contraseña**: Funcionalidad completa implementada
- **Validaciones**: Formato de email, contraseñas coincidentes, campos obligatorios

### Perfil de Usuario

- **Información personal**: Nombre, email, teléfono, dirección
- **Carnet estudiantil digital**: Con diseño similar al carnet físico
- **Actualización de datos**: Edición de información personal

### Foro Estudiantil

- **Publicaciones**: Crear, editar, eliminar posts con tipos y prioridades
- **Sistema de reacciones inteligente**:
  - ✅ **Toggle de likes/dislikes**: Un usuario solo puede dar una reacción por post
  - ✅ **Cambio de reacción**: Cambiar de like a dislike y viceversa
  - ✅ **Contadores precisos**: Basados en tabla de reacciones en BD
  - ✅ **Estado persistente**: Las reacciones se mantienen al reiniciar la app
- **Sistema de comentarios completo**:
  - ✅ **Comentarios persistentes**: Se guardan en base de datos MySQL
  - ✅ **Edición y eliminación**: Solo autor o administrador
  - ✅ **Contadores automáticos**: Se actualizan en tiempo real
  - ✅ **Relaciones correctas**: Usuario-Post-Comentario vinculados
  - ✅ **API REST completa**: Crear, leer, actualizar, eliminar comentarios
  - ✅ **Validación de permisos**: Backend y frontend sincronizados
- **Sistema de roles**:
  - **Estudiantes**: Publicar, reaccionar, comentar, guardar posts
  - **Administración**: Publicar noticias, fijar posts, moderar contenido
- **Moderación avanzada**: Aprobación, fijado y eliminación de contenido

## 🗄️ Base de Datos MySQL

### Tablas Principales

- `users`: Información de usuarios con roles y datos personales (sin campo phone)
- `posts`: Publicaciones del foro con tipos, prioridades y contadores
- `reactions`: **Sistema de reacciones** (likes/dislikes) con restricción única por usuario-post
- `comments`: **Sistema de comentarios** completamente funcional con relaciones
- `student_teachers`: **Recomendaciones de profesores** con sistema de rating (1-5 estrellas)
- `teacher_recommendation_reactions`: **Likes/dislikes** en recomendaciones de profesores

- `moderation_logs`: Logs de moderación (estructura lista)

### Esquema de Reacciones y Comentarios

```sql
-- Tabla de reacciones (likes/dislikes)
CREATE TABLE reactions (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    post_id VARCHAR(36) NOT NULL,
    type ENUM('LIKE', 'DISLIKE') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_post (user_id, post_id)
);

-- Tabla de comentarios
CREATE TABLE comments (
    id VARCHAR(36) PRIMARY KEY,
    post_id VARCHAR(36) NOT NULL,
    author_id VARCHAR(36) NOT NULL,
    content TEXT NOT NULL,
    is_approved BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
);
```

### Instalación de la Base de Datos

#### 🚀 Instalación Automática (Recomendada)

```bash
# Ejecutar script de inicialización completa
bash init_local_development.sh
```

Este script automáticamente:

- ✅ Verifica requisitos (MySQL, Java, Maven)
- ✅ Crea la base de datos completa **sin tabla notifications** (simplificada)
- ✅ Configura sistema de recomendaciones con **rating de estrellas**
- ✅ Configura todas las tablas, vistas y procedimientos
- ✅ Compila el backend
- ✅ Verifica que todo esté funcionando

#### 📋 Instalación Manual

1. **Instalar MySQL**:

   ```bash
   # Ubuntu/Debian
   sudo apt update
   sudo apt install mysql-server

   # Windows
   # Descargar desde: https://dev.mysql.com/downloads/mysql/
   ```

2. **Configurar MySQL**:

   ```bash
   sudo mysql_secure_installation
   ```

3. **Crear la base de datos**:

   ```bash
   mysql -u root -p < database/uniway_db.sql
   ```

   El script `uniway_db.sql` incluye:

   - ✅ **Todas las tablas del foro estudiantil** (sin notifications)
   - ✅ **Sistema completo de recomendaciones** con rating de estrellas
   - ✅ **Tabla `teacher_recommendation_reactions`** para likes/dislikes
   - ✅ **Vista `teacher_recommendations_with_reactions`** optimizada
   - ✅ **6 procedimientos almacenados** para consultas complejas
   - ✅ **6 triggers de auditoría** automática
   - ✅ **Índices optimizados** para máximo rendimiento
   - ✅ **Constraints de validación** (rating 1-5, unique keys)

4. **Configurar usuario** (opcional):
   ```sql
   CREATE USER 'uniway_user'@'localhost' IDENTIFIED BY 'tu_password';
   GRANT ALL PRIVILEGES ON uniway_db.* TO 'uniway_user'@'localhost';
   FLUSH PRIVILEGES;
   ```

## 🚀 Backend Spring Boot

### Requisitos

- Java 17+
- Maven 3.6+
- MySQL 8.0+

### Instalación

1. **Clonar el repositorio**:

   ```bash
   git clone <repository-url>
   cd uniway
   ```

2. **Configurar la base de datos**:
   Editar `backend/src/main/resources/application.yml`:

   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/uniway_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
       username: root # o tu usuario de MySQL
       password: tu_password_mysql
   ```

3. **Ejecutar el backend**:

   ```bash
   cd backend
   mvn spring-boot:run
   ```

4. **Verificar la API**:
   - Swagger UI: http://localhost:8080/api/swagger-ui.html
   - API Docs: http://localhost:8080/api/api-docs

### Endpoints Principales

#### Autenticación

- `POST /api/auth/register` - Registro de usuario
- `POST /api/auth/login` - Inicio de sesión

#### Posts

- `GET /posts` - Obtener todas las publicaciones
- `GET /posts/{id}` - Obtener publicación por ID
- `POST /posts` - Crear nueva publicación
- `POST /posts/dev` - Crear publicación (modo desarrollo)
- `PUT /posts/{id}` - Actualizar publicación
- `DELETE /posts/{id}` - Eliminar publicación

#### Reacciones (Sistema Inteligente)

- `POST /posts/{id}/like` - **Toggle like**: Dar/quitar like
- `POST /posts/{id}/dislike` - **Toggle dislike**: Dar/quitar dislike
- **Lógica implementada**:
  - Si no hay reacción → Crear nueva reacción
  - Si ya existe la misma reacción → Eliminar reacción (toggle off)
  - Si existe reacción diferente → Cambiar tipo de reacción

#### Comentarios (Sistema Completo)

- `GET /comments/post/{postId}` - Obtener comentarios de una publicación
- `POST /comments` - Crear nuevo comentario (con autenticación)
- `POST /comments/dev` - Crear comentario (modo desarrollo)
- `PUT /comments/{id}` - **Actualizar comentario** (con validación de permisos)
- `DELETE /comments/{id}` - **Eliminar comentario** (con validación de permisos)
- `POST /comments/{id}/approve` - Aprobar comentario (moderación)
- `GET /comments/pending` - Obtener comentarios pendientes

#### 🆕 Sistema de Recomendaciones de Profesores

- `GET /teacher-recommendations` - Obtener todas las recomendaciones
- `GET /teacher-recommendations?subject={materia}` - Filtrar por materia
- `GET /teacher-recommendations?userId={id}` - Con estado del usuario
- `GET /teacher-recommendations/user/{userId}` - Recomendaciones propias
- `POST /teacher-recommendations` - Crear nueva recomendación **con rating (1-5 estrellas)**
- `POST /teacher-recommendations/{id}/like` - **Toggle like**: Dar/quitar like
- `POST /teacher-recommendations/{id}/dislike` - **Toggle dislike**: Dar/quitar dislike
- `DELETE /teacher-recommendations/{id}` - **Eliminar recomendación** (con validación de permisos)
- `GET /teacher-recommendations/stats/{userId}` - Estadísticas del usuario
- `GET /teacher-recommendations/subjects` - Materias disponibles

#### Autenticación y Verificación

- `POST /auth/register` - Registro de usuario (sin campo teléfono)
- `POST /auth/login` - Inicio de sesión
- `POST /auth/verify-code` - Verificar código de email
- `POST /auth/send-code` - Enviar código de verificación
- `POST /auth/forgot-password` - Recuperación de contraseña

**Lógica de Reacciones Implementada**:

- Sin reacción → LIKE → Sin reacción
- Sin reacción → DISLIKE → Sin reacción
- LIKE → DISLIKE → LIKE
- DISLIKE → LIKE → DISLIKE

#### Utilidades

- `GET /posts/health` - Verificar estado de la base de datos
- `GET /posts/simple` - Obtener posts en formato simplificado

## 📱 App Android

### Requisitos

- Android Studio Arctic Fox+
- Android SDK 21+
- Kotlin 1.7+

### Instalación

1. **Abrir en Android Studio**:

   ```bash
   # Abrir el proyecto en Android Studio
   # File -> Open -> Seleccionar la carpeta del proyecto
   ```

2. **Configurar la URL del backend**:
   La app está configurada para desarrollo local:

   ```kotlin
   // ApiClient.kt
   private const val BASE_URL = "http://10.0.2.2:8080/" // Para emulador
   // Para dispositivo físico usar: "http://TU_IP_LOCAL:8080/"
   ```

3. **Ejecutar la aplicación**:
   - Conectar dispositivo Android o usar emulador
   - Ejecutar la app desde Android Studio

### Estructura del Proyecto

```
app/src/main/java/com/universidad/uniway/
├── data/                    # Modelos de datos
│   ├── User.kt
│   ├── Post.kt
│   ├── Comment.kt
│   ├── TeacherRecommendation.kt
│   ├── UserRole.kt
│   ├── PostType.kt
│   └── PostPriority.kt
├── network/                 # Capa de red
│   ├── ApiClient.kt        # Cliente Retrofit
│   ├── ApiService.kt       # Definición de endpoints (35+ endpoints)
│   ├── ApiRepository.kt    # Repositorio de API
│   └── TokenManager.kt     # Gestión de tokens JWT
├── ui/
│   ├── login/              # Autenticación
│   │   ├── LoginFragment.kt
│   │   └── LoginViewModel.kt
│   ├── register/           # Registro (sin teléfono)
│   │   ├── RegisterFragment.kt
│   │   ├── RegisterViewModel.kt
│   │   └── EmailVerificationFragment.kt
│   ├── forgotpassword/     # Recuperación de contraseña
│   │   ├── ForgotPasswordFragment.kt
│   │   └── ForgotPasswordViewModel.kt
│   ├── profile/            # Perfil de usuario
│   │   ├── ProfileFragment.kt
│   │   └── ProfileViewModel.kt
│   ├── forum/              # Foro estudiantil
│   │   ├── ForumFragment.kt
│   │   ├── ForumViewModel.kt
│   │   ├── PostAdapter.kt
│   │   ├── CreatePostDialog.kt
│   │   └── EditPostDialog.kt
│   ├── comments/           # Sistema de comentarios
│   │   ├── CommentsFragmentSimple.kt
│   │   ├── CommentsViewModel.kt
│   │   └── CommentAdapter.kt
│   ├── teacher/            # Recomendaciones de profesores
│   │   ├── AllRecommendationsFragment.kt
│   │   ├── AllRecommendationsViewModel.kt
│   │   ├── MyRecommendationsFragment.kt
│   │   ├── MyRecommendationsViewModel.kt
│   │   └── TeacherRecommendationAdapter.kt
│   └── addteacher/         # Agregar recomendaciones
│       ├── AddTeacherFragment.kt
│       └── AddTeacherViewModel.kt
├── MainActivity.kt         # Actividad principal con navegación
├── AuthActivity.kt         # Actividad de autenticación
└── SplashActivity.kt       # Pantalla de inicio
```

## 🔧 Configuración de Red

### Para desarrollo local:

1. **Backend**: Ejecutar en `http://localhost:8080`
2. **Android Emulador**: Usar `http://10.0.2.2:8080`
3. **Android Dispositivo**: Usar `http://TU_IP_LOCAL:8080`

### Verificar conectividad:

```bash
# Desde el dispositivo/emulador Android
curl http://10.0.2.2:8080/posts/health

# Respuesta esperada:
{
  "status": "OK",
  "totalPosts": 1,
  "totalUsers": 3,
  "timestamp": "2024-01-15T10:30:00"
}
```

### Para producción:

1. **Backend**: Desplegar en servidor con IP pública
2. **Android**: Actualizar `BASE_URL` en `ApiClient.kt`

### Verificar sistema de comentarios:

```bash
# Probar endpoint de comentarios
curl -X GET http://localhost:8080/comments/post/POST_ID

# Crear comentario de prueba
curl -X POST http://localhost:8080/comments/dev \
  -H "Content-Type: application/json" \
  -d '{
    "postId": "POST_ID",
    "authorEmail": "student001@unipascualbravo.edu.co",
    "content": "Comentario de prueba"
  }'
```

## 🛡️ Seguridad

- **JWT**: Tokens para autenticación
- **Validación**: Validación de datos en frontend y backend
- **Roles**: Sistema de permisos por rol de usuario
- **Moderación**: Aprobación de contenido por administración

## 📊 Características del Foro

### Tipos de Publicaciones

- **GENERAL**: Publicaciones normales de estudiantes
- **NEWS**: Noticias oficiales de administración
- **ALERT**: Alertas de seguridad (aparecen destacadas)
- **ANNOUNCEMENT**: Anuncios oficiales importantes

### Prioridades

- **NORMAL**: Contenido estándar (por defecto)
- **HIGH**: Contenido importante
- **URGENT**: Contenido crítico (alertas de seguridad)

### Sistema de Reacciones Avanzado

- **Toggle inteligente**: Click en like activo lo desactiva
- **Cambio de reacción**: De like a dislike automáticamente
- **Restricción por usuario**: Solo una reacción por usuario por post
- **Contadores en tiempo real**: Actualizados desde la base de datos
- **Persistencia**: Las reacciones se mantienen entre sesiones

### Sistema de Comentarios Completo

- **Persistencia en BD**: Comentarios guardados permanentemente en MySQL
- **Contadores automáticos**: Se actualizan en tiempo real en los posts
- **API REST completa**: Endpoints para CRUD de comentarios
- **Relaciones correctas**: Usuario-Post-Comentario vinculados por FK
- **Interfaz Android**: Pantalla dedicada para ver y crear comentarios
- **Aprobación automática**: Comentarios se publican inmediatamente
- **Logs detallados**: Sistema de debugging completo

### Sistema de Moderación

- **Aprobación automática**: Posts se publican inmediatamente (configurable)
- **Fijado**: Administración puede fijar posts importantes
- **Eliminación**: Moderadores pueden eliminar contenido inapropiado
- **Logs de moderación**: Registro de todas las acciones de moderación

## 🚀 Despliegue

### Backend (Spring Boot)

```bash
# Compilar y ejecutar en desarrollo
cd backend
mvn spring-boot:run

# Compilar para producción
mvn clean package

# Ejecutar JAR en producción
java -jar target/uniway-backend-0.0.1-SNAPSHOT.jar

# Con perfil específico
java -jar target/uniway-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### Android

1. **Compilar en desarrollo**:

   ```bash
   ./gradlew assembleDebug
   ```

2. **Generar APK de producción**:

   ```bash
   ./gradlew assembleRelease
   ```

3. **Instalar en dispositivo**:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

### Docker (Opcional)

```dockerfile
# Dockerfile para el backend
FROM openjdk:17-jdk-slim
COPY target/uniway-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

## 🤝 Contribución

1. Fork el proyecto
2. Crear rama para feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit cambios (`git commit -am 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crear Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para detalles.

## � Estardo del Proyecto

### ✅ Funcionalidades Implementadas

#### 🔐 **Autenticación Completa**
- [x] **Registro simplificado** (sin campo de teléfono)
- [x] **Verificación por email** con códigos de verificación
- [x] **Inicio de sesión** con JWT
- [x] **Recuperación de contraseña** funcional
- [x] **Gestión de roles** (Estudiante/Administración)

#### 📝 **Foro Estudiantil Avanzado**
- [x] **Sistema de publicaciones** con tipos y prioridades
- [x] **Reacciones inteligentes** (like/dislike con toggle)
- [x] **Comentarios completos** con persistencia en BD
- [x] **Edición/eliminación** de posts y comentarios (solo autor o admin)
- [x] **Validación de permisos** en frontend y backend
- [x] **Moderación de contenido** por administración

#### 👨‍🏫 **Sistema de Recomendaciones de Profesores**
- [x] **Recomendaciones con rating** (sistema de 1-5 estrellas)
- [x] **Referencias detalladas** de estudiantes
- [x] **Sistema de likes/dislikes** con toggle inteligente
- [x] **Filtros por materia** y búsqueda avanzada
- [x] **Eliminación con permisos** (solo autor o admin)
- [x] **Vista optimizada** con contadores pre-calculados
- [x] **Procedimientos almacenados** para rendimiento
- [x] **10+ endpoints REST** completamente funcionales

#### 🛡️ **Seguridad y Arquitectura**
- [x] **API REST completa** con 35+ endpoints
- [x] **Base de datos MySQL** optimizada (sin tabla notifications)
- [x] **Manejo de errores** y logs detallados
- [x] **Interfaz Android nativa** con Material Design
- [x] **TokenManager** para gestión de sesiones
- [x] **Validaciones** en frontend y backend

### 🚧 Posibles Mejoras Futuras

- [ ] **Posts guardados/favoritos** (estructura de BD lista)
- [ ] **Notificaciones push** (estructura eliminada por simplicidad)
- [ ] **Búsqueda avanzada** en foro y recomendaciones
- [ ] **Modo offline** con sincronización
- [ ] **Estadísticas avanzadas** de uso
- [ ] **Sistema de reportes** de contenido inapropiado
- [ ] **Integración con calendario** académico
- [ ] **Chat directo** entre estudiantes

### 📈 Métricas del Proyecto

#### 📊 **Estadísticas Generales**
- **Líneas de código**: ~30,000+
- **Endpoints API**: 35+ (incluye autenticación, foro, comentarios, recomendaciones)
- **Pantallas Android**: 15+ (login, registro, foro, comentarios, recomendaciones, perfil)
- **Controladores Backend**: 7 (Auth, Post, Comment, TeacherRecommendation, User, Verification, Compatibility)

#### 🗄️ **Base de Datos**
- **Tablas principales**: 7 (users, posts, reactions, comments, student_teachers, teacher_recommendation_reactions, moderation_logs)
- **Vistas optimizadas**: 2 (post_with_author, teacher_recommendations_with_reactions)
- **Procedimientos almacenados**: 6 para consultas complejas
- **Triggers**: 6 para auditoría automática
- **Índices optimizados**: 25+ para rendimiento máximo

#### 🏗️ **Arquitectura Android**
- **Fragments**: 15+ con patrón MVVM
- **ViewModels**: 12+ con LiveData
- **Adapters**: 5+ para RecyclerViews
- **Layouts**: 20+ con Material Design
- **Navegación**: Bottom Navigation + Navigation Component

#### 🔧 **Backend Spring Boot**
- **Servicios**: 6+ con lógica de negocio
- **Repositorios**: 8+ con consultas JPA
- **DTOs**: 15+ para transferencia de datos
- **Entidades JPA**: 8+ con relaciones
- **Configuración**: JWT, CORS, Swagger, MySQL

## 👥 Autores

- **Desarrollador**: Equipo de Desarrollo UniWay
- **Universidad**: Institución Universitaria Pascual Bravo
- **Versión**: 2.0.0
- **Última actualización**: Octubre 2025

