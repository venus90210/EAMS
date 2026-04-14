# 🧪 Credenciales de Prueba - EAMS

> **Nota**: Todos los usuarios usan la contraseña: `password123`  
> **MFA/TOTP**: Requerido después del login inicial
> **Generador TOTP**: Usa https://www.authgear.com/tools/totp-authenticator

---

## ⚠️ Importante sobre MFA

Todos los usuarios requieren **Multi-Factor Authentication (MFA)** con código TOTP después de ingresar email y contraseña:

1. Inicia sesión con email + password
2. El sistema te pedirá un código TOTP de 6 dígitos
3. Abre https://www.authgear.com/tools/totp-authenticator
4. Pega el **Secret TOTP** de abajo
5. Copia el código de 6 dígitos que se genera
6. Ingresa ese código en la app

---

## 👨‍💼 ADMIN

| Campo | Valor |
|-------|-------|
| **Email** | `admin@example.com` |
| **Contraseña** | `password123` |
| **Rol** | ADMIN |
| **Secret TOTP** | `OKRV2AZG25UJLQDWT7UJKKZD57RCKWD7` |
| **Generador TOTP** | https://www.authgear.com/tools/totp-authenticator |

---

## 👨‍🏫 TEACHERS (Docentes)

### teacher@example.com
| Campo | Valor |
|-------|-------|
| **Email** | `teacher@example.com` |
| **Contraseña** | `password123` |
| **Rol** | TEACHER |
| **Secret TOTP** | `JBSWY3DPEHPK3PXP` |
| **Generador TOTP** | https://www.authgear.com/tools/totp-authenticator |

### prof.carlos@example.com
| Campo | Valor |
|-------|-------|
| **Email** | `prof.carlos@example.com` |
| **Contraseña** | `password123` |
| **Rol** | TEACHER |
| **Secret TOTP** | `QXQWQOB7QZVXYEDT5CECXYYCNP33QWO7` |
| **Generador TOTP** | https://www.authgear.com/tools/totp-authenticator |

---

## 👨‍👩‍👧 GUARDIANS (Acudientes)

### guardian@example.com
| Campo | Valor |
|-------|-------|
| **Email** | `guardian@example.com` |
| **Contraseña** | `password123` |
| **Rol** | GUARDIAN |
| **Secret TOTP** | `ERJ2WCRVSFUQJGJ6GVPUCYMEAGWMSAUE` |
| **Generador TOTP** | https://www.authgear.com/tools/totp-authenticator |
| **Estudiantes** | Juan Pérez, Carlos López |

### padre.luis@example.com
| Campo | Valor |
|-------|-------|
| **Email** | `padre.luis@example.com` |
| **Contraseña** | `password123` |
| **Rol** | GUARDIAN |
| **Secret TOTP** | `XMZBHKA7RCZZ55WZKEIH5P6PKBYPHTGB` |
| **Generador TOTP** | https://www.authgear.com/tools/totp-authenticator |
| **Estudiantes** | Santiago Gómez, Valentina Gómez |

### madre.ana@example.com
| Campo | Valor |
|-------|-------|
| **Email** | `madre.ana@example.com` |
| **Contraseña** | `password123` |
| **Rol** | GUARDIAN |
| **Secret TOTP** | `F43CUJLHTBLLDISVLHLM5PB5XJZ4DOFM` |
| **Generador TOTP** | https://www.authgear.com/tools/totp-authenticator |
| **Estudiantes** | Martín López |

---

## 🔐 Cómo usar TOTP

### Opción 1: Google Authenticator / Microsoft Authenticator (Móvil)
1. Abre la app de autenticación en tu teléfono
2. Tap en **"+"** para agregar cuenta
3. Selecciona **"Ingrese una clave de configuración"** (Enter a setup key)
4. Copia el **Secret TOTP** de la tabla anterior
5. Ingresa el email del usuario
6. El código se genera automáticamente cada 30 segundos

### Opción 2: AuthGear TOTP Authenticator (Web)
1. Ve a: https://www.authgear.com/tools/totp-authenticator
2. Pega el **Secret TOTP** de la tabla
3. El código se genera automáticamente cada 30 segundos

### Opción 3: Línea de Comando (Python)
```bash
python3 -c "
import time, base64, hmac, hashlib, struct
secret = 'JBSWY3DPEHPK3PXP'  # Reemplaza con el secret del usuario
decoded = base64.b32decode(secret)
timestamp = int(time.time() // 30)
msg = struct.pack('>Q', timestamp)
digest = hmac.new(decoded, msg, hashlib.sha1).digest()
offset = digest[-1] & 0x0f
code = struct.unpack('>I', digest[offset:offset+4])[0] & 0x7fffffff
print(str(code % 1000000).zfill(6))
"
```

---

## 🎯 Casos de Uso Recomendados

| Caso de Uso | Usuario Recomendado | Pasos |
|-------------|---------------------|-------|
| **Gestionar actividades** | `admin@example.com` | Login → Admin → Crear/Publicar/Deshabilitar actividades |
| **Registrar asistencia** | `teacher@example.com` | Login → Asistencia → Abrir sesión → Marcar presente/ausente |
| **Ver seguimiento** | `guardian@example.com` | Login → Seguimiento de inscripciones → Ver historial |
| **Inscribir estudiante** | `padre.luis@example.com` | Login → Actividades → Inscribirse → Seleccionar estudiante |
| **Prueba multi-docente** | `prof.carlos@example.com` | Login → Asistencia (ver múltiples docentes) |
| **Prueba multi-acudiente** | `madre.ana@example.com` | Login → Seguimiento (estudiantes de madre.ana) |

---

## 📱 URLs de Acceso Local

| Aplicación | URL | Puerto |
|-----------|-----|--------|
| **Frontend (PWA)** | http://localhost:3001 | 3001 |
| **API Gateway** | http://localhost:3000 | 3000 |
| **Backend API** | http://localhost:8082 | 8082 |
| **Backend Health** | http://localhost:8082/actuator/health | 8082 |

---

## 🔄 Flujo Completo de Prueba (25 minutos)

### 1. Setup (5 min)
```bash
docker compose up -d
# Espera a que todos los servicios estén healthy (~2-3 min)
```

### 2. Prueba Guardian (5 min)
- Login: `guardian@example.com` / `password123` + TOTP
- Ve a Actividades
- Inscribir estudiante en una actividad
- Ve a Seguimiento
- Verifica que aparece la inscripción

### 3. Prueba Teacher (5 min)
- Login: `teacher@example.com` / `password123` + TOTP
- Ve a Registro de Asistencia
- Abre una sesión
- Marca estudiantes como Presente/Ausente
- Agrega observaciones

### 4. Prueba Admin (5 min)
- Login: `admin@example.com` / `password123` + TOTP
- Ve a Administración de Actividades
- Crea una actividad nueva (DRAFT)
- Publica la actividad
- Opcionalmente deshabilítala

### 5. Verificación Final (5 min)
- Guardian ve la nueva actividad del Admin
- Teacher puede registrar asistencia
- Todo funciona sin errores

---

## 🐛 Si algo no funciona

### Error: "Unauthorized" en login
- ✅ Verifica credenciales exactas (incluyendo mayúsculas en email)
- ✅ Espera a que backend esté healthy: `docker logs eams_backend`

### Error: "Invalid TOTP code"
- ✅ Genera código nuevo en https://www.authgear.com/tools/totp-authenticator
- ✅ El código expira cada 30 segundos, debes copiarlo rápido
- ✅ Verifica el TOTP Secret exacto (sin espacios)

### Error: "Activities not found"
- ✅ Verifica que las migraciones corrieron: `docker compose logs eams_postgres`
- ✅ Reconstruye backend: `docker compose up --build backend`

### Contenedores unhealthy
- ✅ Reinicia todo: `docker compose down -v && docker compose up`
- ✅ Verifica logs: `docker compose logs -f`

---

**Última actualización**: 14 de abril de 2026  
**Versión**: 1.1  
**Status**: ✅ Verificado y funcionando
