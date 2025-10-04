# 📋 Instrucciones Completas - UniWay Foro Estudiantil

## 🎯 **Descripción del Proyecto**

UniWay es un foro estudiantil completo con sistema de recomendaciones de profesores. Incluye:
- **Frontend**: Aplicación Android (Kotlin)
- **Backend**: API REST con Spring Boot (Java)
- **Base de Datos**: MySQL
- **Funcionalidades**: Foro, comentarios, likes, recomendaciones con rating, autenticación JWT

---

## 🛠️ **Requisitos Previos**

### **Software Necesario:**

1. **Java Development Kit (JDK) 17+**
   - Descargar: https://adoptium.net/
   - Verificar: `java -version`

2. **Android Studio**
   - Descargar: https://developer.android.com/studio
   - Incluye Android SDK y herramientas

3. **MySQL Server**
   - Descargar: https://dev.mysql.com/downloads/mysql/
   - O usar XAMPP: https://www.apachefriends.org/

4. **Maven** (opcional, incluido en la mayoría de IDEs)
   - Descargar: https://maven.apache.org/download.cgi
   - Verificar: `mvn -version`

5. **Git**
   - Descargar: https://git-scm.com/
   - Verificar: `git --version`

### **Hardware Recomendado:**
- **RAM**: 8GB mínimo, 16GB recomendado
- **Almacenamiento**: 10GB libres
- **Procesador**: Intel i5 o AMD Ryzen 5 equivalente

---

## 📥 **1. Clonar el Proyecto**

```bash
# Clonar el repositorio
git clone https://github.com/ocampox/Uniway

# Navegar al directorio
cd uniway

# Verificar estructura
ls -la
```

**Estructura esperada:**
```
uniway-foro-estudiantil/
├── app/                    # Frontend Android
├── backend/               # Backend Spring Boot
├── database/              # Scripts de BD
├── .gitignore
├── LICENSE
├── README.md
└── INSTRUCCIONES.md
```

---

## 🗄️ **2. Configurar Base de Datos**


### **XAMPP**

1. **Descargar e instalar XAMPP**
   - https://www.apachefriends.org/

2. **Iniciar servicios**
   - Abrir XAMPP Control Panel
   - Iniciar **Apache** y **MySQL**

3. **Crear base de datos**
   - Ir a http://localhost/phpmyadmin
   - Ir a "Nueva"
   - Importar archivo: `database/uniway_db.sql`

---

## ⚙️ **3. Configurar Backend**

### **Paso 1: Configurar Propiedades**

1. **Crear archivo de configuración local**
   ```bash
   # Navegar al backend
   cd backend/src/main/resources/
   
   # Crear archivo de configuración local
   cp application.yml
   ```

2. **Editar `application.yml`**
   ```properties
   # Configuración de Base de Datos
   datasource:
    url: jdbc:mysql://localhost:3306/uniway_db
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
   
   # JPA/Hibernate
   jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
    open-in-view: false
   
   # Puerto del servidor
   server.port=8080
   
   # JWT Secret (cambiar por uno seguro)
   jwt.secret=mi_jwt_secret_super_seguro_de_al_menos_32_caracteres
   
   # CORS (permitir frontend)
   cors.allowed.origins=*
   
   # Logs
   logging:
  level:
    com.uniway: DEBUG
    org.springframework.security: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
   ```

### **Paso 2: Compilar y Ejecutar**

```bash
# Navegar al directorio backend
cd backend/

# Limpiar y compilar
mvn clean compile

# Ejecutar con perfil local
mvn spring-boot:run


```

### **Paso 3: Verificar Backend**

1. **Verificar que esté corriendo**
   - Abrir: http://localhost:8080
   - Debería mostrar página de error (normal, no hay frontend web)

2. **Probar API**
   ```bash
   # Verificar salud del servidor
   curl http://localhost:8080/actuator/health
   
   # O usar navegador: http://localhost:8080/swagger-ui.html
   ```

---

## 📱 **4. Configurar Frontend Android**

### **Paso 1: Abrir en Android Studio**

1. **Abrir Android Studio**
2. **File → Open**
3. **Seleccionar la carpeta raíz del proyecto** (no solo /app)
4. **Esperar sincronización de Gradle**

### **Paso 2: Configurar SDK**

1. **File → Project Structure**
2. **SDK Location**
   - Android SDK: Verificar ruta (ej: `/Users/tu-usuario/Library/Android/sdk`)
   - JDK: Verificar Java 17+

### **Paso 3: Crear local.properties**

```bash
# En la raíz del proyecto (no en /app)
echo "sdk.dir=/ruta/a/tu/android/sdk" > local.properties

# Ejemplo Windows:
echo "sdk.dir=C:\\Users\\TuUsuario\\AppData\\Local\\Android\\Sdk" > local.properties
```

### **Paso 4: Configurar Conexión al Backend**

1. **Editar `app/src/main/java/com/universidad/uniway/network/ApiClient.kt`**
   ```kotlin
   companion object {
       // Para emulador Android
       private const val BASE_URL = "http://10.0.2.2:8080/"
       
       // Para dispositivo físico (cambiar por tu IP local)
       // private const val BASE_URL = "http://192.168.1.100:8080/"
   }
   ```

2. **Obtener tu IP local (si usas dispositivo físico)**
   ```bash
   # Windows
   ipconfig
   
   # macOS/Linux
   ifconfig
   # Buscar tu IP en la red local (ej: 192.168.1.100)
   ```

### **Paso 5: Compilar y Ejecutar**

1. **Sincronizar proyecto**
   - **File → Sync Project with Gradle Files**

2. **Crear AVD (Emulador)**
   - **Tools → AVD Manager**
   - **Create Virtual Device**
   - Seleccionar **Pixel 4** o similar
   - **API Level 30+** recomendado

3. **Ejecutar aplicación**
   - Seleccionar dispositivo/emulador
   - Presionar **Run** (▶️) o `Shift + F10`

---

## 🧪 **5. Probar la Aplicación**

### **Paso 1: Crear Usuario**

1. **Abrir la app**
2. **Ir a "Registrarse"**
3. **Llenar formulario:**
   - Email: `test@pascualbravo.edu.co`
   - Nombre: `Usuario Prueba`
   - ID Estudiante: `TL123456789`
   - Programa: `Ingeniería de Software`
   - Contraseña: `password123`

### **Paso 2: Probar Funcionalidades**

1. **Foro:**
   - Crear post
   - Dar like/dislike
   - Comentar

2. **Recomendaciones:**
   - Ir a "Profesores"
   - Agregar recomendación
   - Calificar con estrellas

### **Paso 3: Verificar en Base de Datos**

```sql
-- Conectar a MySQL
mysql -u uniway_user -p uniway_db

-- Verificar usuarios
SELECT * FROM users;

-- Verificar posts
SELECT * FROM posts;

-- Verificar recomendaciones
SELECT * FROM student_teachers;
```

---

## 🚨 **Solución de Problemas Comunes**

### **Backend no inicia**

1. **Verificar Java**
   ```bash
   java -version
   # Debe ser 17+
   ```

2. **Verificar MySQL**
   ```bash
   mysql -u uniway_user -p
   # Debe conectar sin errores
   ```

3. **Verificar puerto**
   ```bash
   # Linux/macOS
   lsof -i :8080
   
   # Windows
   netstat -an | findstr :8080
   ```

### **Android no compila**

1. **Limpiar proyecto**
   ```bash
   ./gradlew clean
   ./gradlew build
   ```

2. **Verificar SDK**
   - **File → Project Structure → SDK Location**

3. **Invalidar caché**
   - **File → Invalidate Caches and Restart**

### **App no conecta al backend**

1. **Verificar IP en ApiService.kt**
   - Emulador: `10.0.2.2:8080`
   - Dispositivo: Tu IP local

2. **Verificar firewall**
   ```bash
   # Permitir puerto 8080
   sudo ufw allow 8080
   ```

3. **Probar conexión**
   ```bash
   # Desde el dispositivo/emulador
   curl http://10.0.2.2:8080/actuator/health
   ```

### **Errores de Base de Datos**

1. **Verificar conexión**
   ```bash
   mysql -u uniway_user -p -h localhost
   ```

2. **Recrear base de datos**
   ```sql
   DROP DATABASE uniway_db;
   CREATE DATABASE uniway_db CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
   ```

3. **Reimportar esquema**
   ```bash
   mysql -u uniway_user -p uniway_db < database/uniway_db.sql
   ```

---

## 🔧 **Configuración para Desarrollo**

### **IDEs Recomendados**

1. **Backend:**
   - **IntelliJ IDEA** (recomendado)
   - **Eclipse** con Spring Tools
   - **VS Code** con extensiones Java

2. **Frontend:**
   - **Android Studio** (obligatorio)

### **Extensiones Útiles**

**VS Code:**
- Java Extension Pack
- Spring Boot Extension Pack
- Kotlin Language

**IntelliJ IDEA:**
- Spring Boot (incluido)
- Database Tools (incluido)


## 📚 **Recursos Adicionales**

### **Documentación**
- **Spring Boot**: https://spring.io/projects/spring-boot
- **Android Development**: https://developer.android.com/
- **MySQL**: https://dev.mysql.com/doc/

### **Herramientas de Prueba**
- **Postman**: Para probar APIs
- **MySQL Workbench**: Para gestionar BD
- **Android Device Monitor**: Para debugging

### **Comunidad**
- **Stack Overflow**: Para preguntas específicas
- **GitHub Issues**: Para reportar bugs del proyecto

---

## ✅ **Checklist de Verificación**

### **Backend ✓**
- [ ] Java 17+ instalado
- [ ] MySQL corriendo
- [ ] Base de datos creada e importada
- [ ] Backend compila sin errores
- [ ] API responde en http://localhost:8080

### **Frontend ✓**
- [ ] Android Studio instalado
- [ ] SDK configurado
- [ ] Proyecto sincroniza sin errores
- [ ] Emulador/dispositivo configurado
- [ ] App instala y abre correctamente

### **Integración ✓**
- [ ] App conecta al backend
- [ ] Registro de usuario funciona
- [ ] Login funciona
- [ ] Crear post funciona
- [ ] Sistema de likes funciona
- [ ] Recomendaciones funcionan

---

## 🎉 **¡Felicidades!**

Si llegaste hasta aquí y todo funciona, ¡tienes UniWay corriendo completamente!

### **Próximos Pasos:**
1. **Explorar el código** para entender la arquitectura
2. **Agregar nuevas funcionalidades**
3. **Personalizar el diseño**
4. **Desplegar en producción** (Railway, Heroku, etc.)
