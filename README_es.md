# MCP Selenium - El Estándar de la Industria

[![Maven Central](https://img.shields.io/maven-central/v/io.github.abarrac/mcp-selenium.svg)](https://search.maven.org/artifact/io.github.abarrac/mcp-selenium)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-11%2B-orange.svg)](https://openjdk.java.net/)

> El servidor MCP de Selenium WebDriver de nivel profesional para agentes de IA y herramientas de automatización.

MCP Selenium proporciona una implementación completa del Protocolo de Contexto de Modelo (MCP) para automatización de navegadores, haciendo que sea muy fácil para **cualquier agente de IA o herramienta de automatización** interactuar con aplicaciones web. Construido pensando en la fiabilidad y el rendimiento de nivel empresarial.

## 📍 Requisitos Previos

Antes de instalar MCP Selenium, asegúrate de tener:

- **Java 11+**
- **Navegador Chrome** - [Descargar aquí](https://www.google.com/chrome/)
- **Claude Desktop** - [Descargar aquí](https://claude.ai/download) (para integración con Claude)

### Verificación Rápida
```bash
# Verificar instalación de Java
java -version
# Debería mostrar Java 11 o superior

# Verificar instalación de Chrome  
google-chrome --version  # Linux
# o
"/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" --version  # macOS
```

## 🚀 Inicio Rápido

### Para Usuarios de Claude Desktop
1. **Instalar y Configurar:**
   - **Windows**: `powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/abarrac/mcp-selenium/main/install.bat' -OutFile 'install.bat'; .\install.bat"`
   - **macOS/Linux**: `curl -sSL https://raw.githubusercontent.com/abarrac/mcp-selenium/main/install.sh | bash`
2. **Reiniciar:** Claude Desktop
3. **Probar:** Pregunta a Claude "utilizando las herramientas de selenium MCP, navega a google.com y toma una captura de pantalla"

Para opciones de instalación detalladas, consulta [Configuración](#-configuración).

## 🧠 ¿Qué es MCP?

El **Protocolo de Contexto de Modelo (MCP)** es un estándar abierto para conectar agentes de IA con herramientas y fuentes de datos externas. Piénsalo como una API universal que permite a los sistemas de IA:

- 🔗 **Conectar** con bases de datos, APIs y servicios
- 🚀 **Ejecutar** herramientas y scripts de automatización
- 📄 **Acceder** a archivos y recursos
- 🧩 **Comunicarse** mediante protocolos estandarizados

MCP Selenium implementa este estándar específicamente para **automatización de navegadores web**, convirtiéndose en el puente entre agentes de IA y la web.

## ✨ Características

- **🔄 Espera Inteligente de Elementos** - Esperas inteligentes con tiempos de espera configurables
- **📸 Capturas Avanzadas** - Capturas de viewport, elementos específicos y página completa
- **🎯 Interacciones Precisas** - Click, escritura, hover, arrastrar con precisión de píxel
- **📜 Ejecución de JavaScript** - Ejecución de scripts síncronos/asíncronos con formato de resultados
- **🍪 Gestión de Estado** - Gestión de cookies, localStorage y sesiones
- **🔍 Búsqueda Inteligente** - Selectores CSS, XPath, ID, Class, Name
- **📊 Documentación Visual** - Captura automática de pantallas con metadatos
- **⚡ Alto Rendimiento** - Optimizado para velocidad y eficiencia de recursos
- **🛡️ Seguridad Empresarial** - Características anti-detección y ejecución segura

## 🛠️ Arquitectura

MCP Selenium sigue una arquitectura modular con manejadores especializados:

```
MCP Selenium Core
├── SeleniumMCP - Orquestador principal
├── SeleniumServer - Implementación del protocolo MCP
└── Manejadores/
    ├── NavigationHandler - Navegación de páginas
    ├── ElementHandler - Interacciones DOM
    ├── ScreenshotHandler - Captura visual
    └── ScriptHandler - Ejecución de JavaScript
```

## 🎯 Herramientas Soportadas

### Control del Navegador
- `start_browser` - Inicializar sesión del navegador
- `close_session` - Cierre limpio

### Navegación
- `navigate` - Ir a URL
- `goBack` / `goForward` - Navegación del historial
- `refresh` - Recargar página
- `getCurrentUrl` / `getTitle` - Información de la página

### Interacción con Elementos
- `find_element` / `findElements` - Localizar elementos
- `click_element` - Interacciones de click
- `send_keys` - Entrada de texto
- `select` - Selección de dropdown
- `hover` - Hover del ratón
- `doubleClick` / `rightClick` - Clicks avanzados

### Comprobación de Estado
- `isVisible` / `isEnabled` / `isSelected` - Estado del elemento
- `getText` / `getAttribute` - Extracción de contenido
- `waitForElement` - Espera inteligente

### Capturas de Pantalla
- `take_screenshot` - Captura del viewport
- `elementScreenshot` - Específica del elemento
- `fullPageScreenshot` - Página completa

### JavaScript y Datos
- `executeScript` / `executeAsyncScript` - Ejecución de JS
- `evaluateXPath` - Consultas XPath
- `getPageSource` / `getPageInfo` - Datos de la página
- `getCookie` / `setCookie` - Gestión de cookies
- `getLocalStorage` / `setLocalStorage` - Almacenamiento
- `scrollTo` / `scrollBy` - Control del viewport
- `getConsoleLog` - Información de depuración

## 🔧 Configuración

### Configuración de Claude Desktop

#### Paso 1: Instalar MCP Selenium
Elige uno de estos métodos de instalación:

**Opción A: Instalación Rápida (Recomendada)**

**Windows:**
```powershell
powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/abarrac/mcp-selenium/main/install.bat' -OutFile 'install.bat'; .\install.bat"
```

**macOS/Linux:**
```bash
curl -sSL https://raw.githubusercontent.com/abarrac/mcp-selenium/main/install.sh | bash
```
*Estos scripts manejan automáticamente la configuración del Paso 2. Salta al Paso 3 después de la instalación.*

**Opción B: Descarga Manual**
```bash
# Descargar la última versión
wget https://github.com/abarrac/mcp-selenium/releases/latest/download/mcp-selenium-1.0.1.jar

# Mover a una ubicación permanente
mkdir -p ~/.mcp-selenium
mv mcp-selenium-1.0.1.jar ~/.mcp-selenium/mcp-selenium.jar
```

**Opción C: Compilar desde el Código Fuente**
```bash
git clone https://github.com/abarrac/mcp-selenium.git
cd mcp-selenium
mvn clean package
cp target/mcp-selenium-1.0.1.jar ~/.mcp-selenium/mcp-selenium.jar
```

#### Paso 2: Configurar Claude Desktop (Solo Instalación Manual)
*Omite este paso si usaste la Opción A (script automático) arriba.*

Añade a tu archivo de configuración de Claude Desktop:

**En macOS:** `~/Library/Application Support/Claude/claude_desktop_config.json`

**En Windows:** `%APPDATA%\Claude\claude_desktop_config.json`

**En Linux:** `~/.config/Claude/claude_desktop_config.json`

```json
{
  "mcpServers": {
    "selenium": {
      "command": "java",
      "args": ["-jar", "~/.mcp-selenium/mcp-selenium.jar"]
    }
  }
}
```

#### Paso 3: Reiniciar Claude Desktop
Reinicia Claude Desktop para que los cambios surtan efecto.

#### Paso 4: Verificar la Instalación
Abre Claude Desktop y prueba preguntando:
```
"Toma una captura de pantalla de google.com utilizando las herramientas de selenium MCP"
```

¡Si ves que la automatización del navegador está funcionando, ya está todo listo! 🎉

### Solución de Problemas

**❌ "java: command not found"**
- Instala Java 11+
- En macOS: `brew install openjdk@11`
- En Ubuntu: `sudo apt install openjdk-11-jdk`

**❌ "Chrome not found"**
- Instala el navegador Chrome

**❌ "Las herramientas no aparecen en Claude"**
- Reinicia Claude Desktop completamente
- Verifica la sintaxis JSON en el archivo de configuración
- Comprueba la ubicación del archivo de configuración para tu sistema operativo

### Clientes MCP Personalizados
Para otras herramientas compatibles con MCP, usa la configuración estándar del servidor MCP:

```json
{
  "servers": {
    "selenium": {
      "command": "java",
      "args": ["-jar", "/ruta/a/mcp-selenium.jar"],
      "env": {
        "SELENIUM_HEADLESS": "true"
      }
    }
  }
}
```

### Variables de Entorno
- `SELENIUM_HEADLESS=true` - Ejecutar en modo sin cabeza
- `SELENIUM_TIMEOUT=30` - Establecer tiempo de espera por defecto (segundos)
- `SELENIUM_BROWSER=chrome` - Elección del navegador (chrome/firefox)

## 🌍 Tipos de Selectores

MCP Selenium soporta todas las estrategias principales de selectores:

| Tipo | Formato | Ejemplo |
|------|---------|---------|
| CSS | `selector` | `#myId`, `.myClass`, `div > p` |
| XPath | `//xpath` | `//div[@class='content']` |
| ID | `id=value` | `id=submit-button` |
| Name | `name=value` | `name=username` |
| Class | `class=value` | `class=btn-primary` |
| Tag | `tag=value` | `tag=button` |

## 📋 Requisitos

- **Java**: 11 o superior
- **Navegador**: Chrome
- **Memoria**: 512MB RAM mínimo
- **SO**: Windows, macOS, Linux

## 🤝 Contribuir

¡Damos la bienvenida a las contribuciones! Por favor, consulta nuestra [Guía de Contribución](CONTRIBUTING.md) para más detalles.

### Configuración de Desarrollo
```bash
git clone https://github.com/abarrac/mcp-selenium.git
cd mcp-selenium
mvn clean install
```

### Ejecutar Pruebas
```bash
mvn test
```

## 📄 Licencia

Este proyecto está licenciado bajo la Licencia MIT - consulta el archivo [LICENSE](LICENSE) para más detalles.

## 🙏 Reconocimientos

- [Selenium WebDriver](https://selenium.dev/) - La base de la automatización web
- [Model Context Protocol](https://modelcontextprotocol.io/) - Estandarizando las interacciones de herramientas de IA
- [Anthropic](https://anthropic.com/) - Pioneros de la especificación MCP

## 📞 Soporte

- 🐛 [Reportar Errores](https://github.com/abarrac/mcp-selenium/issues)
- 💡 [Solicitudes de Características](https://github.com/abarrac/mcp-selenium/discussions)
- 📧 [Soporte por Email](mailto:abarragancosto@gmail.com)

---

<div align="center">

**⭐ Dale una estrella a este repositorio si MCP Selenium te ayuda a crear automatizaciones increíbles!**
</div>
