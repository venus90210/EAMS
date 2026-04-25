# 📄 IMPRIMIR TESTING_REPORT_FINAL.html A PDF

## Archivo a usar:
**`TESTING_REPORT_FINAL.html`** ← Usa ESTE archivo

Contiene:
- ✅ Portada profesional (Grupo 5, profesores, universidad)
- ✅ Tabla de contenidos clickeable
- ✅ 11 secciones numeradas
- ✅ 23 tablas formateadas
- ✅ Todo el contenido (1,200+ líneas)
- ✅ Resultará en **40-50+ páginas en PDF**

---

## 🖨️ Opción 1: Navegador (RECOMENDADO)

### macOS:
```bash
open specs/TESTING_REPORT_FINAL.html
# Luego: ⌘P → Save as PDF
```

### Windows/Linux:
```bash
# Haz clic derecho en el archivo
# → Abrir con → Navegador (Chrome/Firefox)
# Luego: Ctrl+P → Save as PDF
```

### Configuración de impresión recomendada:
```
Márgenes: Normal (25mm)
Escala: 100%
Orientación: Vertical (Portrait)
Tamaño papel: A4
Encabezado/Pie: Desactivado (opcional)
Background: Activado (para tablas coloridas)
```

---

## 🔧 Opción 2: Línea de comandos (Python)

### Instalar:
```bash
pip install weasyprint
```

### Convertir:
```bash
cd /Users/angelica/workspace/EAMS/specs
weasyprint TESTING_REPORT_FINAL.html TESTING_REPORT_FINAL.pdf
```

Resultado: `TESTING_REPORT_FINAL.pdf` (automático, 40-50 páginas)

---

## 📋 Contenido que Esperar

El PDF incluirá:

✅ **Portada** (1 página)
   - Grupo 5, integrantes con emails
   - Profesores
   - Universidad Nacional de Colombia

✅ **Tabla de Contenidos** (1-2 páginas)
   - Todos los capítulos numerados
   - Índice clickeable (en PDF interactivos)

✅ **Capítulo 1: Pruebas Unitarias** (8-10 páginas)
   - Stack por contenedor
   - Cobertura por módulo
   - Frontend tests

✅ **Capítulo 2: Pruebas Integración** (5-7 páginas)
   - 4 escenarios críticos (IT-01 a IT-04)
   - Infraestructura Testcontainers

✅ **Capítulo 3: Pruebas Funcionales** (10-12 páginas)
   - 5 features Gherkin
   - Ejecución Cucumber
   - Mapeo requisito → feature

✅ **Cobertura y Métricas** (3-5 páginas)
   - Tablas de cobertura
   - CI/CD pipeline gates
   - Matriz de trazabilidad

✅ **Capítulo 5: Validación de Requisitos** (15-20 páginas)
   - Mapeo RF/RNF a tests
   - Casos de negocio con narrativa
   - 3 casos críticos que bloquean
   - Checklist pre-release

✅ **Conclusiones** (1-2 páginas)

**TOTAL ESPERADO: 40-50+ páginas**

---

## ✨ Características del PDF Final

| Característica | Status |
|---|---|
| Portada profesional | ✅ |
| Tabla de contenidos | ✅ |
| Secciones numeradas | ✅ 11 |
| Tablas formateadas | ✅ 23 |
| Código coloreado | ✅ |
| Bloques destacados | ✅ |
| Enlaces funcionales | ✅ |
| Márgenes profesionales | ✅ |
| Print-ready | ✅ |

---

## 📝 Si algo se ve mal en el PDF:

1. **Tablas cortadas**: Aumenta márgenes o reduce escala a 95%
2. **Colores débiles**: Asegúrate que "Background graphics" esté activado
3. **Portada mal**: Algunos navegadores necesitan "Márgenes personalizados"

**Solución**: Usa Chrome en lugar de Safari (mejor CSS support en print)

---

## 🎯 Próximos pasos

1. ✅ Abre `TESTING_REPORT_FINAL.html` en navegador
2. ✅ Presiona ⌘P (o Ctrl+P)
3. ✅ Configura márgenes "Normal"
4. ✅ Click "Save as PDF"
5. ✅ Guarda como `TESTING_REPORT_FINAL.pdf`
6. ✅ Listo para entregar! 🎉

---

**Fecha**: 15 de Abril de 2026  
**Proyecto**: EAMS - Sistema de Gestión de Actividades Extracurriculares  
**Grupo**: 5 - ISIA 2026-1
