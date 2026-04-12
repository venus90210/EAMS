#!/bin/sh
set -e

# Ensure dist directory exists with compiled code
if [ ! -f dist/main.js ]; then
  echo "Compiling TypeScript..."
  npm run build
fi

# If still no dist/main.js, try direct tsc
if [ ! -f dist/main.js ]; then
  echo "Using direct tsc..."
  npx tsc
fi

# Start the application
echo "Starting NestJS Gateway..."
exec npm start
