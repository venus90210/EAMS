FROM node:20-alpine AS builder

WORKDIR /app

# Copiar package files
COPY package*.json ./

# Instalar dependencias
RUN npm install --legacy-peer-deps

# Copiar código
COPY . .

# Build — webpack requerido: next-pwa usa webpack plugins, incompatible con Turbopack (Next.js 16 default)
RUN npx next build --webpack

# ── Runtime stage ──────────────────────────────────────────────────────────

FROM node:20-alpine

WORKDIR /app

# Copiar package files
COPY package*.json ./

# Instalar dependencias de producción solamente
RUN npm install --production --legacy-peer-deps

# Copiar build del builder
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public

# Exponer puerto
EXPOSE 3000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD node -e "require('http').get('http://localhost:3000', (r) => {if (r.statusCode !== 200) throw new Error(r.statusCode)})"

# Start - usar puerto 3000
CMD ["npm", "run", "start", "--", "-p", "3000"]
