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
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/eams
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# Redis
SPRING_REDIS_HOST=redis
SPRING_REDIS_PORT=6379

# Gateway
JWT_SECRET=your-secret-key-for-development
BACKEND_URL=http://backend:8080

# Frontend
NEXT_PUBLIC_GATEWAY_URL=http://localhost:3001
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
  1. **postgres** (base de datos)
  2. **redis** (caché)
  3. **backend** (Java/Spring Boot en puerto 8080)
  4. **gateway** (NestJS en puerto 3001)
  5. **frontend** (Next.js en puerto 3000)

**Espera a ver estos mensajes:**
```
backend  | ... started Spring Boot application
gateway  | [Nest] ... Nest application successfully started
frontend | ✓ ready - started server on 0.0.0.0:3000
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
# Test 1: Backend está corriendo
curl http://localhost:8080/actuator/health

# Deberías ver: {"status":"UP"}

# Test 2: Gateway está corriendo
curl http://localhost:3001/health

# Deberías ver: {"statusCode":200,"message":"OK"}

# Test 3: Frontend está corriendo
curl http://localhost:3000

# Deberías ver: HTML de la página
```

---

## 🌐 Paso 5: Acceder a las Aplicaciones

| Aplicación | URL | Credenciales de Prueba |
|------------|-----|------------------------|
| **Frontend (PWA)** | http://localhost:3000 | email: `guardian@example.com`<br/>password: `password123` |
| **API Gateway** | http://localhost:3001 | (usado por frontend automáticamente) |
| **Backend API** | http://localhost:8080/swagger-ui.html | (Swagger/OpenAPI docs) |
| **PostgreSQL** | localhost:5432 | user: `postgres`<br/>pass: `postgres` |
| **Redis** | localhost:6379 | (sin contraseña) |

---

## 👥 Usuarios de Prueba Disponibles

Estos usuarios se crean automáticamente en desarrollo (`TestDataInitializer`):

### Guardian (Padre/Acudiente)
```
Email: guardian@example.com
Password: password123
Rol: GUARDIAN
Institución: Instituto Técnico Metropolitano
```

### Teacher (Docente)
```
Email: teacher@example.com
Password: password123
Rol: TEACHER
Institución: Instituto Técnico Metropolitano
```

### Admin (Administrador Institucional)
```
Email: admin@example.com
Password: password123
Rol: ADMIN
Institución: Instituto Técnico Metropolitano
```

**Flujos de prueba recomendados:**
1. **Login como GUARDIAN** → Ver actividades → Inscribir estudiante
2. **Login como TEACHER** → Registrar asistencia
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

### ❌ "Puertos ya están en uso" (Port 3000, 3001, 8080, etc.)
**Solución:** Otro servicio está usando esos puertos.
```bash
# Encuentra qué proceso usa puerto 3000:
lsof -i :3000  # macOS/Linux
netstat -ano | findstr :3000  # Windows

# O simplemente cambia puertos en docker-compose.yml:
# ports:
#   - "3000:3000"  → cambia a "3001:3000"
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

# Opción 1: Parar y levantar de nuevo
docker compose down
docker compose up

# Opción 2: Solo recompila backend
docker compose up --build backend
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
1. Abre http://localhost:3000
2. Haz clic en "Iniciar Sesión"
3. Email: `guardian@example.com`
4. Password: `password123`
5. Haz clic en "Iniciar Sesión"

### 2. Ver Actividades
- Verás un listado de actividades disponibles
- Cada actividad muestra: nombre, descripción, cupos disponibles, horario

### 3. Inscribir Estudiante
- Haz clic en el botón "Inscribirse" de cualquier actividad
- Selecciona el estudiante (si tienes múltiples)
- Confirma la inscripción
- ¡Listo! Aparecerá en "Mis Inscripciones"

### 4. Login como Teacher (Docente)
1. Logout: haz clic en el avatar arriba a la derecha → "Logout"
2. Login con: `teacher@example.com` / `password123`
3. Ve a "Asistencia"
4. Abre una sesión de asistencia
5. Marca estudiantes como "Presente" o "Ausente"

### 5. Login como Admin
1. Logout
2. Login con: `admin@example.com` / `password123`
3. Ve a "Gestión de Actividades"
4. Crea una actividad nueva, publícala, deshabílitala

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

**Última actualización:** 13 de Abril de 2026
**Versión:** 1.0
**Estado:** Pronto para desarrollo local ✅
