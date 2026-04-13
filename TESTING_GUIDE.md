# 🧪 EAMS — Guía Completa de Testing Multirol con OTP

**Guía paso-a-paso para testing exhaustivo de todos los roles y funcionalidades, incluyendo MFA (OTP).**

---

## 📊 Base de Datos de Prueba

### Instituciones Disponibles

| ID | Nombre | Email Domain | Admin | Teacher | Guardian |
|-----|--------|--------------|-------|---------|----------|
| `b716fa...` | Instituto Técnico Metropolitano | — | admin@example.com | teacher@example.com | guardian@example.com |
| `a1b2c3...` | Colegio San José | sanjose.edu.co | admin.sanjose@example.com | prof.carlos@example.com | padre.luis@, madre.ana@ |
| `b2c3d4...` | Instituto Técnico Industrial | itindi.edu.co | admin.itti@example.com | prof.juan@example.com | guardiana.maria@ |
| `c3d4e5...` | Escuela de Artes y Oficios | eao.edu.co | admin.eao@example.com | prof.diego@example.com | — |

---

## 🔑 Tabla Completa de Usuarios

### INSTITUCIÓN 1: Instituto Técnico Metropolitano

| Rol | Email | Contraseña | MFA | Estudiantes |
|-----|-------|-----------|-----|-------------|
| **GUARDIAN** | guardian@example.com | `password123` | ❌ No | (sin datos) |
| **TEACHER** | teacher@example.com | `password123` | ❌ No | — |
| **ADMIN** | admin@example.com | `password123` | ❌ No | — |

### INSTITUCIÓN 2: Colegio San José

| Rol | Email | Contraseña | MFA | Estudiantes | Acceso |
|-----|-------|-----------|-----|-------------|--------|
| **ADMIN** | admin.sanjose@example.com | `password123` | ❌ No | — | Todas las features admin |
| **TEACHER** | prof.carlos@example.com | `password123` | ❌ No | — | Crear/cerrar sesiones asistencia |
| **GUARDIAN #1** | padre.luis@example.com | `password123` | ❌ No | Santiago (9A), Valentina (8B) | Inscribir en Fútbol ✅, Danza, Ajedrez |
| **GUARDIAN #2** | madre.ana@example.com | `password123` | ❌ No | Martín (10A) | Inscribir en actividades |

### INSTITUCIÓN 3: Instituto Técnico Industrial

| Rol | Email | Contraseña | MFA | Estudiantes |
|-----|-------|-----------|-----|-------------|
| **ADMIN** | admin.itti@example.com | `password123` | ❌ No | — |
| **TEACHER** | prof.juan@example.com | `password123` | ❌ No | — |
| **GUARDIAN** | guardiana.maria@example.com | `password123` | ❌ No | Andrés (11A), Catalina (9C) |

### INSTITUCIÓN 4: Escuela de Artes y Oficios

| Rol | Email | Contraseña | MFA |
|-----|-------|-----------|-----|
| **ADMIN** | admin.eao@example.com | `password123` | ❌ No |
| **TEACHER** | prof.diego@example.com | `password123` | ❌ No |

---

## 📋 Actividades Disponibles por Institución

### Colegio San José (4 actividades)

| Estado | Nombre | Cupos Total | Cupos Libres | Visible? |
|--------|--------|------------|-------------|----------|
| **DRAFT** | Taller de Electrónica | 15 | 15 | ❌ No (solo admin) |
| **PUBLISHED** | Fútbol Profesional | 20 | 8 | ✅ Sí |
| **PUBLISHED** | Ajedrez Avanzado | 10 | 0 | ✅ Sí (sin cupos) |
| **PUBLISHED** | Danza Contemporánea | 25 | 12 | ✅ Sí |
| **DISABLED** | Debate Académico | 20 | 15 | ❌ No |

### ITTI (3 actividades)

| Estado | Nombre | Cupos Total | Cupos Libres | Visible? |
|--------|--------|------------|-------------|----------|
| **PUBLISHED** | Soldadura Industrial | 12 | 5 | ✅ Sí |
| **PUBLISHED** | Mecánica Automotriz | 18 | 9 | ✅ Sí |
| **DRAFT** | Programación en Python | 25 | 25 | ❌ No |

### EAO (2 actividades)

| Estado | Nombre | Cupos Total | Cupos Libres | Visible? |
|--------|--------|------------|-------------|----------|
| **PUBLISHED** | Pintura Acrílica | 20 | 15 | ✅ Sí |
| **PUBLISHED** | Cerámica y Alfarería | 15 | 7 | ✅ Sí |

---

## 🔐 MFA / OTP — Cómo Funciona

### Acerca de OTP en EAMS

- **¿Qué es?** OTP = One-Time Password basado en TOTP (Time-based)
- **Algoritmo:** GoogleAuthenticator compatible
- **Duración:** 30 segundos por código
- **Ventana:** ±1 período (60 segundos total de tolerancia)

### Flujo MFA en Registro

```
Usuario → Crea cuenta → Backend genera MFA Secret (base32) 
→ User recibe QR + Secret en clave → Escanea con Google Authenticator
→ Configura en su teléfono → Listo
```

### Flujo MFA en Login

```
POST /auth/login {email, password}
↓
✅ Credenciales OK + MFA habilitado 
→ Retorna: sessionToken (JWT 5 min) + QR setup (si nueva)
↓
User abre Google Authenticator → Lee código 6 dígitos
↓
POST /auth/mfa/verify {sessionToken, code}
↓
✅ Código correcto → Retorna: accessToken + refreshToken
❌ Código incorrecto → 401 Unauthorized (reintentar)
```

---

## 🧪 Flujos de Testing Recomendados

### FLUJO 1: Login Sin MFA (RECOMENDADO PARA EMPEZAR)

**Usuarios sin MFA:** Todos los usuarios de testing actual

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "guardian@example.com",
    "password": "password123"
  }'

# Respuesta esperada:
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "refresh_token_abc123...",
  "user": {
    "id": "uuid...",
    "email": "guardian@example.com",
    "role": "GUARDIAN"
  }
}
```

---

### FLUJO 2: Testing por Rol

#### 🎓 GUARDIAN (Padre/Acudiente)

**Usar:** `padre.luis@example.com` / `password123`

**Pasos:**
1. ✅ Login exitoso
2. ✅ Ver listado de actividades PUBLISHED de su institución
3. ✅ Inscribir estudiante "Santiago" en "Fútbol" (tiene cupos)
4. ✅ Intentar inscribir en "Ajedrez" → Error (sin cupos)
5. ✅ Ver "Mis Inscripciones" → Santiago aparece en Fútbol
6. ✅ Ver "Seguimiento" → Actividades del estudiante + asistencia
7. ✅ Logout

**Que NO debería poder hacer:**
- ❌ Ver actividades DRAFT
- ❌ Ver actividades DISABLED
- ❌ Crear actividades
- ❌ Registrar asistencia
- ❌ Administrar usuarios

#### 👨‍🏫 TEACHER (Docente)

**Usar:** `prof.carlos@example.com` / `password123`

**Pasos:**
1. ✅ Login exitoso
2. ✅ Ir a "Asistencia"
3. ✅ Abrir sesión de asistencia para una actividad
4. ✅ Ver listado de estudiantes inscritos
5. ✅ Marcar "Santiago" como presente
6. ✅ Marcar "Valentina" como ausente
7. ✅ Agregar observaciones (ej: "Excelente técnica")
8. ✅ Cerrar sesión de asistencia
9. ✅ Logout

**Que NO debería poder hacer:**
- ❌ Crear actividades (solo ver las que dicta)
- ❌ Modificar roles de usuarios
- ❌ Ver datos de otras instituciones
- ❌ Cambiar estado de actividades

#### 🔧 ADMIN (Administrador Institucional)

**Usar:** `admin.sanjose@example.com` / `password123`

**Pasos:**
1. ✅ Login exitoso
2. ✅ Ir a "Gestión de Actividades"
3. ✅ Crear nueva actividad: 
   - Nombre: "Taller de Robótica"
   - Descripción: "Robotics 101"
   - Total Spots: 16
4. ✅ Publicar actividad (DRAFT → PUBLISHED)
5. ✅ Ver cambio en disponibilidad inmediata
6. ✅ Deshabilitar actividad (PUBLISHED → DISABLED)
7. ✅ Verificar que desaparece de catálogo GUARDIAN
8. ✅ Logout

**Que NO debería poder hacer:**
- ❌ Crear usuarios de otras instituciones
- ❌ Ver datos de otras instituciones
- ❌ Cambiar estado de usuarios
- ❌ Eliminar datos permanentemente (solo deshabilitar)

#### 👑 SUPERADMIN (Sistema)

**Nota:** No hay SUPERADMIN en datos de testing. Para crear uno, ejecuta:

```sql
INSERT INTO users (id, email, password_hash, role, institution_id, mfa_secret, is_active, created_at, updated_at) 
VALUES (
  gen_random_uuid(),
  'superadmin@system.com',
  '$2a$12$OvYdddyxC5sXJf/Jf72lAOP01KlXRzMJH4eV0JbYdPW0Ly5pRpYJi',  -- password123
  'SUPERADMIN',
  NULL,  -- sin institución (acceso global)
  NULL,
  true,
  now(),
  now()
);
```

**Capacidades:**
- ✅ Ver/crear/modificar todas las instituciones
- ✅ Ver/crear usuarios en cualquier institución
- ✅ Ver actividades y datos de todas las instituciones
- ✅ Auditoría global

---

## 🔑 Testing con OTP (MFA)

### Opción 1: Habilitar OTP en Usuario Existente (Manual)

```sql
-- Generar secreto TOTP base32 (usando herramienta online)
-- Ej: JBSWY3DPEBLW64TMMQ======

UPDATE users 
SET mfa_secret = 'JBSWY3DPEBLW64TMMQ======'
WHERE email = 'guardian@example.com';
```

### Opción 2: Generar OTP desde CLI

**Con Node.js + speakeasy:**

```bash
npm install -g speakeasy qrcode-terminal

node -e "
const speakeasy = require('speakeasy');
const qrcode = require('qrcode-terminal');

const secret = speakeasy.generateSecret({
  name: 'EAMS (guardian@example.com)',
  issuer: 'EAMS'
});

console.log('Secret (base32):', secret.base32);
console.log('Secret (hex):', secret.hex);
console.log('\nQR Code:');
qrcode.generate(secret.otpauth_url, {small: true});

// Generar código para test inmediato
const token = speakeasy.totp({
  secret: secret.base32,
  encoding: 'base32',
  time: Math.floor(Date.now() / 1000)
});
console.log('\nCódigo TOTP actual:', token);
console.log('(válido por ~30 segundos)');
"
```

### Opción 3: Usar Google Authenticator (Real)

```
1. Instala Google Authenticator en tu teléfono
2. En backend, obtén el secret desde DB: 
   SELECT mfa_secret FROM users WHERE email='guardian@example.com';
3. En Google Authenticator: toca el + → "Enter a setup key"
4. Pega el secret en base32
5. La app genera códigos cada 30 segundos
```

### Flujo de Login CON OTP

```bash
# PASO 1: Login
curl -X POST http://localhost:3001/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "guardian@example.com",
    "password": "password123"
  }'

# Respuesta:
{
  "sessionToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "requiresMfa": true
}

# PASO 2: Abrir Google Authenticator → leer código (ej: 123456)
# PASO 3: Verificar MFA

curl -X POST http://localhost:3001/auth/mfa/verify \
  -H "Content-Type: application/json" \
  -d '{
    "sessionToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "mfaCode": "123456"
  }'

# Respuesta OK:
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "refresh_token_xyz...",
  "user": { ... }
}

# Respuesta ERROR (código incorrecto):
{
  "statusCode": 401,
  "message": "Invalid MFA code"
}
```

---

## 📋 Checklist de Testing Completo

### Setup Inicial
- [ ] Levantar docker compose: `docker compose up`
- [ ] Esperar que postgres, redis, backend estén ready (5-10 seg)
- [ ] Acceder a http://localhost:3000
- [ ] Verificar que no hay errores en console

### Testing GUARDIAN
- [ ] Login como `padre.luis@example.com`
- [ ] Ver 3 actividades PUBLISHED (Fútbol, Ajedrez sin cupos, Danza)
- [ ] Inscribir Santiago en Fútbol → ✅ Exitoso
- [ ] Intentar inscribir en Ajedrez → ❌ Error "Sin cupos"
- [ ] Ver "Mis Inscripciones" → Santiago en Fútbol
- [ ] Ver "Seguimiento" → Asistencia del estudiante
- [ ] Logout

### Testing TEACHER
- [ ] Login como `prof.carlos@example.com`
- [ ] Acceder a "Asistencia"
- [ ] Abrir sesión de asistencia para Fútbol
- [ ] Marcar Santiago como presente
- [ ] Agregar nota: "Buen desempeño"
- [ ] Cerrar sesión
- [ ] Logout

### Testing ADMIN
- [ ] Login como `admin.sanjose@example.com`
- [ ] Acceder a "Gestión de Actividades"
- [ ] Crear nueva actividad "Baloncesto" (20 cupos)
- [ ] Publicar actividad
- [ ] Verificar que aparece en catálogo GUARDIAN
- [ ] Deshabilitar actividad
- [ ] Verificar que desaparece del catálogo
- [ ] Logout

### Testing Multi-Institución
- [ ] Login como `admin.itti@example.com` (ITTI)
- [ ] Verificar que solo ve actividades de ITTI
- [ ] Verificar que no ve actividades de Colegio San José
- [ ] Logout

- [ ] Login como `padre.luis@example.com` (Colegio San José)
- [ ] Verificar que solo ve actividades de San José
- [ ] Logout

### Testing Sobrecupo (RF03: 0% duplicidad)
- [ ] Abrir 2 navegadores
- [ ] Ambos login como `madre.ana@example.com`
- [ ] Browser 1: Inscribir Martín en Danza (12 cupos libres)
- [ ] Browser 2: Intentar inscribir Martín en Danza al mismo tiempo
- [ ] Resultado esperado: 1 exitoso, 1 falla con "Sin cupos"

---

## 🚨 Casos de Error a Probar

| Caso | Acción | Respuesta Esperada |
|------|--------|-------------------|
| Credenciales inválidas | Login con contraseña falsa | 401 Unauthorized |
| Usuario inexistente | Login con email no registrado | 401 Unauthorized |
| Rol insuficiente | GUARDIAN intenta crear actividad | 403 Forbidden |
| Institución mismatch | Admin A accede datos institución B | 403 Forbidden |
| Sin cupos | GUARDIAN inscribe en actividad sin cupos | 409 Conflict |
| Actividad duplicada | GUARDIAN inscribe 2x al mismo student | 409 Conflict |
| MFA inválido | Código 6 dígitos incorrecto | 401 Unauthorized |
| Token expirado | Access token de 15min expirado | 401 → auto-refresh con refreshToken |

---

## 🔗 URLs Útiles

| URL | Propósito |
|-----|-----------|
| http://localhost:3000 | Frontend (PWA) |
| http://localhost:3001 | API Gateway |
| http://localhost:8080/swagger-ui.html | Backend Swagger/OpenAPI |
| http://localhost:5432 | PostgreSQL (host:5432, user: postgres, pass: postgres) |
| http://localhost:6379 | Redis (sin credenciales) |

---

## 📝 Logs Útiles

```bash
# Ver logs de autenticación
docker compose logs -f backend | grep -i "auth\|login\|mfa"

# Ver logs de inscripción
docker compose logs -f backend | grep -i "enroll"

# Ver logs de asistencia
docker compose logs -f backend | grep -i "attendance"

# Ver todo
docker compose logs -f
```

---

## 💡 Tips de Testing

1. **Abre DevTools (F12)** → Network tab → ve las requests/responses
2. **Postman/Insomnia** → importa OpenAPI desde http://localhost:8080/v3/api-docs
3. **JWT Decode** → Usa https://jwt.io para inspeccionar tokens
4. **Teléfono real** → Google Authenticator es más realista que online tools
5. **Limpia cookies** → Si hay problemas, limpia cookies/cache del navegador

---

**Última actualización:** 13 de Abril de 2026
**Versión:** 1.0 — Testing Multirol Completo
