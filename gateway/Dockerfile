FROM node:20-alpine

WORKDIR /app

# Copiar package files
COPY package*.json ./

# Instalar dependencias
RUN npm install --legacy-peer-deps

# Copiar código
COPY . .

# Make entrypoint executable
RUN chmod +x docker-entrypoint.sh

# Exponer puerto
EXPOSE 3000

# Health check
HEALTHCHECK --interval=15s --timeout=5s --retries=5 \
  CMD node -e "require('http').get('http://localhost:3000/health', (r) => {if (r.statusCode !== 200) throw new Error(r.statusCode)})"

# Use entrypoint to handle compilation and startup
ENTRYPOINT ["./docker-entrypoint.sh"]
