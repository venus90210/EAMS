# ⚡ QUICK START - EAMS (2-3 minutos)

**Para: Desarrolladores con Docker instalado**

---

## 1️⃣ Clonar y Configurar (30 segundos)

```bash
git clone <repo-url>
cd EAMS
cp .env.example .env
```

---

## 2️⃣ Levantar Servicios (1 minuto)

```bash
docker compose up -d
# Espera a que aparezca "healthy" en todos los servicios
docker compose ps
```

**Esperado:**
```
NAME           STATUS
eams_postgres  Up (healthy)
eams_redis     Up (healthy)
eams_backend   Up (healthy)
eams_gateway   Up (healthy)
eams_frontend  Up (healthy)
```

---

## 3️⃣ Acceder a la App (30 segundos)

| Rol | URL | Email | Password | TOTP Secret |
|-----|-----|-------|----------|-------------|
| **Guardian** | http://localhost:3001 | guardian@example.com | password123 | ERJ2WCRVSFUQJGJ6GVPUCYMEAGWMSAUE |
| **Teacher** | http://localhost:3001 | teacher@example.com | password123 | JBSWY3DPEHPK3PXP |
| **Admin** | http://localhost:3001 | admin@example.com | password123 | OKRV2AZG25UJLQDWT7UJKKZD57RCKWD7 |

**Pasos para login:**
1. Abre http://localhost:3001
2. Ingresa email + password
3. El sistema te pide código TOTP
4. Ve a https://www.authgear.com/tools/totp-authenticator
5. Pega el TOTP Secret
6. Copia el código de 6 dígitos
7. Ingresa el código en la app

---

## 🎯 Flujos Rápidos

### Guardian: Ver actividades e inscribir
```
Login → Actividades → Inscribirse → Ver Seguimiento
```

### Teacher: Registrar asistencia
```
Login → Registro de Asistencia → Abrir sesión → Marcar presentes
```

### Admin: Crear actividad
```
Login → Administración → Nueva actividad → Publicar
```

---

## 🛑 Detener

```bash
docker compose down
```

**Con reset completo (borra BD):**
```bash
docker compose down -v
```

---

## 🐛 Issues Comunes

| Problema | Solución |
|----------|----------|
| Puerto 3000/3001 en uso | `lsof -i :3001` y mata el proceso |
| Servicios no healthy | `docker compose logs -f` y espera 30s |
| Login falla | Verifica credenciales exactas (sin espacios) |
| TOTP inválido | Genera código nuevo cada 30s en authgear.com |

---

## 📚 Documentación Completa

- 📖 [DEPLOYMENT_LOCAL.md](./DEPLOYMENT_LOCAL.md) - Guía detallada
- 🧪 [TEST_CREDENTIALS.md](./TEST_CREDENTIALS.md) - Todos los usuarios

---

**Listo en 2-3 minutos ✅**
