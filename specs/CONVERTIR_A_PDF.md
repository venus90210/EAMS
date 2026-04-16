# Convertir TESTING_REPORT a PDF

Se han generado versiones en **HTML** del reporte que preservan perfecto el formato. Aquí están las opciones para convertir a PDF:

## 📱 Opción 1: Navegador (Más Fácil) ✅ RECOMENDADO

### En macOS:
```bash
# Abrir en navegador predeterminado
open specs/TESTING_REPORT_COMPLETE.html

# Luego: File → Print (⌘P) → Save as PDF
```

### En Windows/Linux:
```bash
# Abrir en navegador
firefox specs/TESTING_REPORT_COMPLETE.html

# Luego: Ctrl+P → Save as PDF
```

**Resultado**: PDF profesional con:
- ✅ Tabla de contenidos interactiva
- ✅ Tablas formateadas correctamente
- ✅ Código con colores
- ✅ Márgenes profesionales
- ✅ Saltos de página automáticos

---

## 🔧 Opción 2: Línea de Comandos (Python)

### Instalar dependencia:
```bash
pip install weasyprint
```

### Convertir:
```bash
cd /Users/angelica/workspace/EAMS/specs
weasyprint TESTING_REPORT_COMPLETE.html TESTING_REPORT.pdf
```

**Ventaja**: Automatizado, sin interacción manual

---

## 📎 Opción 3: Servicio Online (Sin Instalación)

Si prefieres no instalar nada:

1. Ir a: https://cloudconvert.com/html-to-pdf
2. Subir `TESTING_REPORT_COMPLETE.html`
3. Convertir a PDF
4. Descargar

**Ventaja**: Funciona desde cualquier navegador

---

## 📄 Archivos Disponibles

| Archivo | Formato | Tamaño | Uso |
|---------|---------|--------|-----|
| `TESTING_REPORT.md` | Markdown | 25 KB | Editable, control de versión |
| `TESTING_REPORT_COMPLETE.html` | HTML | 92 KB | Para impresión / PDF, lindo formato |
| `TESTING_REPORT.html` | HTML | 98 KB | Alternativa con GitHub markdown CSS |
| `TESTING_REPORT.pdf` | PDF | — | Generar con uno de los métodos arriba |

---

## ✨ Características del HTML

✅ **Tabla de contenidos** clickeable  
✅ **Títulos numerados** (1.1, 1.2, 2.1, etc.)  
✅ **Tablas con colores alternados**  
✅ **Código coloreado** con sintaxis  
✅ **Estilos EAMS** (verde #00a651)  
✅ **Responsive** (se ve bien en móvil/tablet)  
✅ **Print-friendly** (saltos de página automáticos)  

---

## 🎯 Recomendación

**Para documentos corporativos**: Usa **Opción 1 (Navegador → Print to PDF)**
- Mejor control de layout
- Previsualización antes de guardar
- Sin dependencias

**Para automatización**: Usa **Opción 2 (weasyprint)**
- Ideal para CI/CD
- Reproducible

**Para prueba rápida**: Usa **Opción 3 (Online)**
- Sin instalación
- Funciona ahora

---

## Resultado

Una vez convertido a PDF, tendrás un documento:
- 📊 Profesional (estilos EAMS)
- 🔒 Distribución inmediata (sin edición involuntaria)
- 📑 Tabla de contenidos
- ✅ Todos los tests documentados
- 🎯 Requisitos mapeados a tests
- 🔄 Casos críticos identificados

---

**Última actualización**: 2026-04-15  
**Generado con**: Pandoc + custom CSS
