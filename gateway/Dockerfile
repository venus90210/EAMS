FROM node:20-alpine

WORKDIR /app

# Copiar package files
COPY package*.json ./

# Instalar dependencias
RUN npm install --legacy-peer-deps

# Copiar código (excluye *.tsbuildinfo via .dockerignore)
COPY . .

# Build: tsbuildinfo no está presente, tsc emite JS correctamente
RUN npm run build

# Exponer puerto
EXPOSE 3001

# Health check
HEALTHCHECK --interval=15s --timeout=5s --retries=5 \
  CMD node -e "require('http').get('http://localhost:3001/health', (r) => {if (r.statusCode !== 200) throw new Error(r.statusCode)})"

# Start production
CMD ["npm", "run", "start:prod"]
