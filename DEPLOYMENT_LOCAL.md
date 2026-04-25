# 🚀 EAMS — Guía de Deployment Local

**Para que cualquiera (incluso un "dummy") pueda levantar el proyecto en su máquina local en 5 minutos.**

---

## 📋 Prerequisitos (Instalar una sola vez)

### Opción A: Instalación Manual (Recomendado para aprender)

| Requisito | Versión Mínima | ¿Cómo verificar? |
|-----------|------------------|------------------|
| **Docker** | 20.10+ | `docker --version` |
| **Docker Compose** | 1.29+ | `docker compose version` |
| **Git** | 2.25+ | `git --version` |
| **Java** | 21+ | `java -version` |
| **Maven** | 3.6+ | `mvn --version` |
| **Node.js** | 18+ | `node --version` |
| **npm** | 9+ | `npm --version` |

**Instalación rápida (macOS):**
```bash
# Usar Homebrew
brew install docker docker-compose git openjdk@21 maven node
```

**Instalación rápida (Ubuntu/Debian):**
```bash
sudo apt update && sudo apt install -y docker.io docker-compose git openjdk-21-jdk maven nodejs npm
```

**Instalación rápida (Windows):**
- Descargar [Docker Desktop](https://www.docker.com/products/docker-desktop)
- Descargar [Git for Windows](https://git-scm.com)
- Descargar [OpenJDK 21](https://adoptium.net/)
- Descargar [Node.js LTS](https://nodejs.org/)

---

## 🎯 Paso 1: Clonar el Repositorio

```bash
# Clonar repo
git clone https://github.com/tu-usuario/EAMS.git
cd EAMS

# Cambiar a rama develop (donde está todo)
git checkout develop
```

**¿Qué acaba de pasar?**
- Descargaste todo el código del proyecto
- Estás en la rama `develop` donde están los cambios más recientes

---

## ⚙️ Paso 2: Preparar Variables de Entorno

```bash
# Copiar template de variables
cp .env.example .env

# Si estás en Windows, usa:
copy .env.example .env
```

**Archivo `.env` creado con contenido como:**
```
# Backend
SPRING_PROFILES_ACTIVE=dev
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/eams_dev
SPRING_DATASOURCE_USERNAME=eams
SPRING_DATASOURCE_PASSWORD=eams

# Redis
SPRING_REDIS_HOST=redis
SPRING_REDIS_PORT=6379

# Gateway
JWT_SECRET=your-secret-key-for-development
BACKEND_URL=http://backend:8080

# Frontend
NEXT_PUBLIC_API_URL=http://localhost:3000
```

**⚠️ IMPORTANTE:**
- Este `.env` es SOLO para desarrollo local
- NUNCA uses estos valores en producción
- `.env` está en `.gitignore`, no se sube a git

---

## 🐳 Paso 3: Levantar los Servicios con Docker Compose

```bash
# Levantar TODOS los servicios (postgres, redis, backend, gateway, frontend)
docker compose up

# Si quieres levantar en background (sin ver logs):
docker compose up -d

# Ver logs en tiempo real:
docker compose logs -f

# Ver logs de un servicio específico:
docker compose logs -f backend
docker compose logs -f gateway
docker compose logs -f frontend
```

**¿Qué está pasando?**
- Docker descarga imágenes (primera vez tarda ~2-3 min)
- 5 servicios levantándose:
  1. **postgres** (base de datos en puerto 5432)
  2. **redis** (caché en puerto 6379)
  3. **backend** (Java/Spring Boot en puerto 8082)
  4. **gateway** (NestJS en puerto 3000)
  5. **frontend** (Next.js en puerto 3001)

**Espera a ver estos mensajes:**
```
eams_backend  | ... Tomcat started on port 8080
eams_gateway  | 🚀 Gateway listening on port 3001
eams_frontend | ✓ Ready in 450ms
```

**Status esperado (docker compose ps):**
```
NAME              STATUS
eams_postgres     Up (healthy)
eams_redis        Up (healthy)
eams_backend      Up (healthy)
eams_gateway      Up (healthy)
eams_frontend     Up (healthy)
```

---

## ✅ Paso 4: Verificar que Todo Funciona

### A) Verificar que los servicios están corriendo

```bash
# En OTRA terminal (sin cerrar docker compose up):
docker compose ps

# Deberías ver:
# NAME         STATUS
# postgres     Up (healthy)
# redis        Up (healthy)
# backend      Up
# gateway      Up
# frontend     Up
```

### B) Hacer pruebas HTTP

```bash
# Test 1: Backend está corriendo (interno 8080, externo 8082)
curl http://localhost:8082/actuator/health

# Deberías ver: {"status":"UP"}

# Test 2: Gateway está corriendo (puerto 3000)
curl http://localhost:3000/health

# Deberías ver: {"status":"ok"}

# Test 3: Frontend está corriendo (puerto 3001)
curl http://localhost:3001

# Deberías ver: HTML de la página Next.js
```

---

## 🌐 Paso 5: Acceder a las Aplicaciones

| Aplicación | URL | Credenciales de Prueba |
|------------|-----|------------------------|
| **Frontend (PWA)** | http://localhost:3001 | email: `guardian@example.com`<br/>password: `password123` |
| **API Gateway** | http://localhost:3000 | (usado por frontend automáticamente) |
| **Backend API** | http://localhost:8082 | Spring Boot app |
| **Backend Health** | http://localhost:8082/actuator/health | Status del backend |
| **PostgreSQL** | localhost:5432 | user: `eams`<br/>pass: `eams` |
| **Redis** | localhost:6379 | (sin contraseña) |

---

## 👥 Usuarios de Prueba Disponibles

Estos usuarios se crean automáticamente en desarrollo (`TestDataInitializer`). 

**⚠️ IMPORTANTE:** Todos requieren **Multi-Factor Authentication (MFA)** con código TOTP después del login inicial.

### Guardian (Padre/Acudiente) - Principal
```
Email:        guardian@example.com
Password:     password123
Rol:          GUARDIAN
Institución:  Colegio San José
TOTP Secret:  ERJ2WCRVSFUQJGJ6GVPUCYMEAGWMSAUE
Estudiantes:  Juan Pérez, Carlos López
```

### Guardian - Secundarios
```
Email:        padre.luis@example.com
Password:     password123
TOTP Secret:  XMZBHKA7RCZZ55WZKEIH5P6PKBYPHTGB
Estudiantes:  Santiago Gómez, Valentina Gómez

Email:        madre.ana@example.com
Password:     password123
TOTP Secret:  F43CUJLHTBLLDISVLHLM5PB5XJZ4DOFM
Estudiantes:  Martín López
```

### Teacher (Docente)
```
Email:        teacher@example.com
Password:     password123
Rol:          TEACHER
TOTP Secret:  JBSWY3DPEHPK3PXP
Institución:  Colegio San José
```

### Teacher - Secundario
```
Email:        prof.carlos@example.com
Password:     password123
TOTP Secret:  QXQWQOB7QZVXYEDT5CECXYYCNP33QWO7
```

### Admin (Administrador Institucional)
```
Email:        admin@example.com
Password:     password123
Rol:          ADMIN
TOTP Secret:  OKRV2AZG25UJLQDWT7UJKKZD57RCKWD7
Institución:  Colegio San José
```

**Cómo usar MFA (TOTP):**
1. Inicia sesión con email y contraseña
2. Te pedirá un código TOTP de 6 dígitos
3. Usa https://www.authgear.com/tools/totp-authenticator
4. Pega el TOTP Secret del usuario
5. Copia el código de 6 dígitos que aparece
6. Ingresa el código en la app

**Flujos de prueba recomendados:**
1. **Login como GUARDIAN** → Ver actividades → Inscribir estudiante
2. **Login como TEACHER** → Registrar asistencia de estudiantes
3. **Login como ADMIN** → Crear/publicar/deshabilitar actividades

---

## 🛑 Detener Servicios

```bash
# Si levantaste en foreground (Ctrl+C):
# Press Ctrl+C en la terminal

# Si levantaste en background (-d):
docker compose down

# Borrar volúmenes (CUIDADO: borra la BD):
docker compose down -v
```

---

## 🐛 Troubleshooting

### ❌ "docker: command not found"
**Solución:** Docker no está instalado.
```bash
# macOS:
brew install docker

# Luego inicia Docker Desktop desde Applications
```

### ❌ "Error: the input device is not a TTY"
**Solución:** Probablemente estés en Git Bash (Windows).
```bash
# Usa PowerShell en lugar de Git Bash
# O levanta con:
docker compose up -d
```

### ❌ "Puertos ya están en uso" (Port 3000, 3001, 8082, 5432, etc.)
**Solución:** Otro servicio está usando esos puertos.
```bash
# Encuentra qué proceso usa puerto 3000:
lsof -i :3000  # macOS/Linux
lsof -i :3001  # Frontend
lsof -i :8082  # Backend
netstat -ano | findstr :3000  # Windows

# O simplemente cambia puertos en docker-compose.yml:
# ports:
#   - "3000:3001"  → cambia a "3002:3001" (si puerto 3000 está en uso)
#   - "3001:3000"  → cambia a "3003:3000" (si puerto 3001 está en uso)
```

### ❌ "Database is initializing" error en backend
**Solución:** PostgreSQL aún no está listo. Espera 10 segundos y recarga.
- Docker Compose tiene healthchecks, pero a veces tardan

### ❌ "No se ven usuarios de prueba"
**Solución:** TestDataInitializer solo corre en perfil `dev`.
```bash
# Verifica que SPRING_PROFILES_ACTIVE=dev en .env
```

### ❌ "Frontend no carga" (Error "Cannot GET /")
**Solución:** Frontend aún está compilando (primera vez tarda ~1 min).
```bash
# Espera y recarga el navegador
# O mira los logs:
docker compose logs frontend
```

### ❌ "Error al hacer login" (401 Unauthorized)
**Solución:** Verifica que estés usando credenciales correctas.
```bash
# Usuarios disponibles:
# guardian@example.com / password123
# teacher@example.com / password123
# admin@example.com / password123
```

---

## 📊 Verificar Logs

```bash
# Ver todos los logs
docker compose logs

# Ver logs en tiempo real
docker compose logs -f

# Ver logs de un servicio
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f gateway

# Ver últimas 100 líneas
docker compose logs --tail 100
```

---

## 🔄 Desarrollo: Hacer Cambios en el Código

### Frontend (cambios en tiempo real)
```bash
# El frontend usa Next.js con hot reload
# Si cambias código en src/, se recarga automáticamente
# Solo espera 2-3 segundos y recarga el navegador
```

### Backend (requiere recompilación)
```bash
# Si cambias código Java:

# Opción 1: Recompila y reinicia solo backend
docker compose up --build backend

# Opción 2: Parar y levantar todo de nuevo
docker compose down
docker compose up

# Opción 3: Local (sin Docker)
cd backend
mvn clean spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

### Gateway (requiere recompilación)
```bash
docker compose up --build gateway
```

---

## 🧪 Pruebas Unitarias Locales

### Backend (JaCoCo ≥95%)
```bash
cd backend
mvn clean test
mvn test jacoco:report
# Reporte en: backend/target/site/jacoco/index.html
```

### Frontend (Jest ≥95%)
```bash
cd frontend
npm test -- --coverage
# Reporte en: frontend/coverage/index.html
```

---

## 📚 Rutas Útiles del Proyecto

```
EAMS/
├── docker-compose.yml          ← Define los 5 servicios
├── .env.example                ← Variables de entorno
├── backend/                    ← Spring Boot (Java 21)
│   ├── src/main/               ← Código fuente
│   ├── src/test/               ← Tests unitarios
│   └── pom.xml                 ← Dependencias Maven
├── gateway/                    ← NestJS (Node.js)
│   ├── src/                    ← Código fuente
│   ├── tests/                  ← Tests
│   └── package.json            ← Dependencias npm
├── frontend/                   ← Next.js (React)
│   ├── src/app/                ← Rutas (App Router)
│   ├── src/components/         ← Componentes React
│   ├── src/__tests__/          ← Tests unitarios
│   └── package.json            ← Dependencias npm
└── specs/                      ← Especificaciones
    ├── functional/             ← Features Gherkin
    ├── technical/adr/          ← Decisiones arquitectónicas
    └── technical/openapi/      ← Especificaciones OpenAPI
```

---

## 🎓 Primeros Pasos (Tutorial Rápido)

### 1. Login como Guardian
1. Abre **http://localhost:3001** (Frontend)
2. Haz clic en "Iniciar Sesión"
3. Email: `guardian@example.com`
4. Password: `password123`
5. Código TOTP: 
   - Ve a https://www.authgear.com/tools/totp-authenticator
   - Pega: `ERJ2WCRVSFUQJGJ6GVPUCYMEAGWMSAUE`
   - Copia el código de 6 dígitos
   - Pégalo en la app
6. Haz clic en "Verificar"

### 2. Ver Actividades
- Verás un listado de actividades disponibles (Fútbol, Matemáticas, Arte, etc.)
- Cada actividad muestra: nombre, descripción, cupos disponibles, estado

### 3. Inscribir Estudiante
- Haz clic en "Inscribirse" en cualquier actividad
- Selecciona un estudiante (ej: Juan Pérez, Carlos López)
- Confirma la inscripción
- ¡Listo! Aparecerá en tu seguimiento

### 4. Ver Seguimiento
- Haz clic en "Ver mis inscripciones"
- Verás todas las actividades en las que te inscribiste
- Muestra asistencia y observaciones del docente

### 5. Login como Teacher (Docente)
1. Logout: haz clic en el avatar → "Salir"
2. Login con: `teacher@example.com` / `password123`
3. Código TOTP: 
   - Usa: `JBSWY3DPEHPK3PXP` en https://www.authgear.com/tools/totp-authenticator
4. Ve a "Registro de Asistencia"
5. Abre una sesión de asistencia para una actividad
6. Marca estudiantes como "✓ Presente" o "✕ Ausente"
7. Agrega observaciones (ej: "Excelente desempeño")

### 6. Login como Admin
1. Logout
2. Login con: `admin@example.com` / `password123`
3. Código TOTP:
   - Usa: `OKRV2AZG25UJLQDWT7UJKKZD57RCKWD7`
4. Ve a "Administración de Actividades"
5. Crea una actividad nueva:
   - Nombre, descripción, cupos
   - Crea en estado DRAFT
6. Publica la actividad (DRAFT → PUBLISHED)
7. Opcionalmente, deshabilítala (PUBLISHED → DISABLED)

---

## 🔐 Notas de Seguridad

- ✅ **Desarrollo local:** Usa contraseñas simple (`password123`)
- ❌ **NUNCA en producción:** Los valores en `.env` son solo para desarrollo
- 🔒 **JWT Secret:** En producción debe ser una string aleatoria de 32+ caracteres
- 🔐 **Datos sensibles:** `.env` está en `.gitignore`, no se commitea

---

## 📞 Soporte

### Si algo no funciona:
1. **Lee los logs:** `docker compose logs -f`
2. **Resetea todo:** `docker compose down -v && docker compose up`
3. **Limpia caché Docker:** `docker system prune -a`
4. **Verifica prerequisites:** `docker --version`, `java -version`, etc.

### Links útiles:
- 📖 [Documentación EAMS](./specs/PLAN.md)
- 🏗️ [Decisiones Arquitectónicas](./specs/technical/adr/)
- 📊 [Especificaciones OpenAPI](./specs/technical/openapi/)
- 🧪 [Estrategia de Testing](./specs/technical/testing-strategy.md)

---

---

## 📝 Cambios Recientes (v1.1)

✅ **Corregido:**
- Puertos correctos documentados (3000 Gateway, 3001 Frontend, 8082 Backend)
- Instrucciones MFA/TOTP agregadas
- Todos los usuarios de prueba con TOTP secrets
- Variables de entorno actualizadas
- Tutorial paso a paso con detalles reales
- Database credentials correctas (eams/eams)

✅ **Verificado en:**
- Docker Compose v1.29+
- Todas las actividades listándose correctamente en admin
- MFA funcionando con TOTP

---

**Última actualización:** 14 de Abril de 2026
**Versión:** 1.1
**Estado:** ✅ Listo para desarrollo local — Todas las pruebas verificadas
