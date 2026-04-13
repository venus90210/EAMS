# 🧪 Credenciales de Prueba - EAMS

> **Nota**: Todos los usuarios usan la contraseña: `password123`  
> **Para TOTP**: Usa https://www.authgear.com/tools/totp-authenticator

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

| Caso de Uso | Usuario Recomendado |
|-------------|---------------------|
| Gestionar actividades | `admin@example.com` |
| Registrar asistencia | `teacher@example.com` o `prof.carlos@example.com` |
| Ver seguimiento de estudiantes | `guardian@example.com` o `padre.luis@example.com` |
| Inscribirse en actividades | `padre.luis@example.com` o `madre.ana@example.com` |

---

**Última actualización**: 13 de abril de 2026
