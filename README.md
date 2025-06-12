# MCP Selenium - The Industry Standard

[![Maven Central](https://img.shields.io/maven-central/v/io.github.abarrac/mcp-selenium.svg)](https://search.maven.org/artifact/io.github.abarrac/mcp-selenium)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-11%2B-orange.svg)](https://openjdk.java.net/)

> The professional-grade Selenium WebDriver MCP server for AI agents and automation tools.

MCP Selenium provides a comprehensive Model Context Protocol (MCP) implementation for browser automation, making it effortless for **any AI agent or automation tool** to interact with web applications. Built with enterprise-grade reliability and performance in mind.

## 📍 Prerequisites

Before installing MCP Selenium, ensure you have:

- **Java 11+** 
- **Chrome Browser** - [Download here](https://www.google.com/chrome/)
- **Claude Desktop** - [Download here](https://claude.ai/download) (for Claude integration)

### Quick Check
```bash
# Verify Java installation
java -version
# Should show Java 11 or higher

# Verify Chrome installation  
google-chrome --version  # Linux
# or
"/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" --version  # macOS
```

## 🚀 Quick Start

### For Claude Desktop Users
1. **Install & Configure:**
   - **Windows**: `powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/abarrac/mcp-selenium/main/install.bat' -OutFile 'install.bat'; .\install.bat"`
   - **macOS/Linux**: `curl -sSL https://raw.githubusercontent.com/abarrac/mcp-selenium/main/install.sh | bash`
2. **Restart:** Claude Desktop
3. **Test:** Ask Claude to "using selenium MCP tools, navigate to google.com and take a screenshot"

For detailed installation options, see [Configuration](#-configuration).

## 🧠 What is MCP?

The **Model Context Protocol (MCP)** is an open standard for connecting AI agents with external tools and data sources. Think of it as a universal API that allows AI systems to:

- 🔗 **Connect** to databases, APIs, and services
- 🚀 **Execute** tools and automation scripts  
- 📄 **Access** files and resources
- 🧩 **Communicate** through standardized protocols

MCP Selenium implements this standard specifically for **web browser automation**, making it the bridge between AI agents and the web.

## ✨ Features

- **🔄 Intelligent Element Waiting** - Smart waits with configurable timeouts
- **📸 Advanced Screenshots** - Viewport, element-specific, and full-page capture
- **🎯 Precise Element Interactions** - Click, type, hover, drag with pixel-perfect accuracy
- **📜 JavaScript Execution** - Sync/async script execution with result formatting
- **🍪 State Management** - Cookies, localStorage, session handling
- **🔍 Smart Element Finding** - CSS, XPath, ID, Class, Name selectors
- **📊 Visual Documentation** - Automated screenshot capture with metadata
- **⚡ High Performance** - Optimized for speed and resource efficiency
- **🛡️ Enterprise Security** - Anti-detection features and secure execution

## 🛠️ Architecture

MCP Selenium follows a modular architecture with specialized handlers:

```
MCP Selenium Core
├── SeleniumMCP - Main orchestrator
├── SeleniumServer - MCP protocol implementation
└── Handlers/
    ├── NavigationHandler - Page navigation
    ├── ElementHandler - DOM interactions
    ├── ScreenshotHandler - Visual capture
    └── ScriptHandler - JavaScript execution
```

## 🎯 Supported Tools

### Browser Control
- `start_browser` - Initialize browser session
- `close_session` - Clean shutdown

### Navigation
- `navigate` - Go to URL
- `goBack` / `goForward` - History navigation
- `refresh` - Reload page
- `getCurrentUrl` / `getTitle` - Page info

### Element Interaction
- `find_element` / `findElements` - Locate elements
- `click_element` - Click interactions
- `send_keys` - Text input
- `select` - Dropdown selection
- `hover` - Mouse hover
- `doubleClick` / `rightClick` - Advanced clicks

### State Checking
- `isVisible` / `isEnabled` / `isSelected` - Element state
- `getText` / `getAttribute` - Content extraction
- `waitForElement` - Intelligent waiting

### Screenshots
- `take_screenshot` - Viewport capture
- `elementScreenshot` - Element-specific
- `fullPageScreenshot` - Complete page

### JavaScript & Data
- `executeScript` / `executeAsyncScript` - JS execution
- `evaluateXPath` - XPath queries
- `getPageSource` / `getPageInfo` - Page data
- `getCookie` / `setCookie` - Cookie management
- `getLocalStorage` / `setLocalStorage` - Storage
- `scrollTo` / `scrollBy` - Viewport control
- `getConsoleLog` - Debug information

## 🔧 Configuration

### Claude Desktop Setup

#### Step 1: Install MCP Selenium
Choose one of these installation methods:

**Option A: Quick Install (Recommended)**

**Windows:**
```powershell
powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/abarrac/mcp-selenium/main/install.bat' -OutFile 'install.bat'; .\install.bat"
```

**macOS/Linux:**
```bash
curl -sSL https://raw.githubusercontent.com/abarrac/mcp-selenium/main/install.sh | bash
```
*These scripts automatically handle Step 2 configuration. Skip to Step 3 after installation.*

**Option B: Manual Download**
```bash
# Download the latest release
wget https://github.com/abarrac/mcp-selenium/releases/latest/download/mcp-selenium-1.0.1.jar

# Move to a permanent location
mkdir -p ~/.mcp-selenium
mv mcp-selenium-1.0.1.jar ~/.mcp-selenium/mcp-selenium.jar
```

**Option C: Build from Source**
```bash
git clone https://github.com/abarrac/mcp-selenium.git
cd mcp-selenium
mvn clean package
cp target/mcp-selenium-1.0.1.jar ~/.mcp-selenium/mcp-selenium.jar
```

#### Step 2: Configure Claude Desktop (Manual Installation Only)
*Skip this step if you used Option A (automatic script) above.*

Add to your Claude Desktop configuration file:

**On macOS:** `~/Library/Application Support/Claude/claude_desktop_config.json`
**On Windows:** `%APPDATA%\Claude\claude_desktop_config.json`
**On Linux:** `~/.config/Claude/claude_desktop_config.json`

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

#### Step 3: Restart Claude Desktop
Restart Claude Desktop for the changes to take effect.

#### Step 4: Verify Installation
Open Claude Desktop and try asking:
```
"Take a screenshot of google.com using selenium MCP tools"
```

If you see browser automation happening, you're all set! 🎉

### Troubleshooting

**❌ "java: command not found"**
- Install Java 11+
- On macOS: `brew install openjdk@11`
- On Ubuntu: `sudo apt install openjdk-11-jdk`

**❌ "Chrome not found"**
- Install Chrome browser

**❌ "Tools not appearing in Claude"**
- Restart Claude Desktop completely
- Verify JSON syntax in config file
- Check config file location for your OS

### Custom MCP Clients
For other MCP-compatible tools, use the standard MCP server configuration:

```json
{
  "servers": {
    "selenium": {
      "command": "java",
      "args": ["-jar", "/path/to/mcp-selenium.jar"],
      "env": {
        "SELENIUM_HEADLESS": "true"
      }
    }
  }
}
```

### Environment Variables
- `SELENIUM_HEADLESS=true` - Run in headless mode
- `SELENIUM_TIMEOUT=30` - Set default timeout (seconds)
- `SELENIUM_BROWSER=chrome` - Browser choice (chrome/firefox)

## 🌍 Selector Types

MCP Selenium supports all major selector strategies:

| Type | Format | Example |
|------|--------|---------|
| CSS | `selector` | `#myId`, `.myClass`, `div > p` |
| XPath | `//xpath` | `//div[@class='content']` |
| ID | `id=value` | `id=submit-button` |
| Name | `name=value` | `name=username` |
| Class | `class=value` | `class=btn-primary` |
| Tag | `tag=value` | `tag=button` |

## 📋 Requirements

- **Java**: 11 or higher
- **Browser**: Chrome
- **Memory**: 512MB RAM minimum
- **OS**: Windows, macOS, Linux

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

### Development Setup
```bash
git clone https://github.com/abarrac/mcp-selenium.git
cd mcp-selenium
mvn clean install
```

### Running Tests
```bash
mvn test
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [Selenium WebDriver](https://selenium.dev/) - The foundation of web automation
- [Model Context Protocol](https://modelcontextprotocol.io/) - Standardizing AI tool interactions
- [Anthropic](https://anthropic.com/) - Pioneers of the MCP specification

## 📞 Support

- 🐛 [Bug Reports](https://github.com/abarrac/mcp-selenium/issues)
- 💡 [Feature Requests](https://github.com/abarrac/mcp-selenium/discussions)
- 📧 [Email Support](mailto:abarragancosto@gmail.com)
---

<div align="center">

**⭐ Star this repository if MCP Selenium helps you build amazing automation!**
</div>
