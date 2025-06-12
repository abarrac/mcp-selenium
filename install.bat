@echo off
setlocal enabledelayedexpansion

echo Installing MCP Selenium for Windows...
echo.

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 11+
    echo.
    pause
    exit /b 1
)

echo Java found
echo.

REM Create installation directory
set INSTALL_DIR=%USERPROFILE%\.mcp-selenium
if not exist "%INSTALL_DIR%" mkdir "%INSTALL_DIR%"

echo Downloading MCP Selenium...

REM Download using PowerShell (built into Windows)
powershell -Command "try { Invoke-WebRequest -Uri 'https://github.com/abarrac/mcp-selenium/releases/latest/download/mcp-selenium-1.0.1.jar' -OutFile '%INSTALL_DIR%\mcp-selenium.jar' -UseBasicParsing; Write-Host 'Download completed' } catch { Write-Host 'Download failed'; exit 1 }"

if not exist "%INSTALL_DIR%\mcp-selenium.jar" (
    echo ERROR: Failed to download MCP Selenium JAR
    pause
    exit /b 1
)

echo Download successful
echo.

REM Claude Desktop config path for Windows
set CONFIG_DIR=%APPDATA%\Claude
set CONFIG_FILE=%CONFIG_DIR%\claude_desktop_config.json

echo Config location: %CONFIG_FILE%
echo.

REM Create Claude config directory if it doesn't exist
if not exist "%CONFIG_DIR%" mkdir "%CONFIG_DIR%"

REM Prepare JAR path with escaped backslashes for JSON
set JAR_PATH=%INSTALL_DIR%\mcp-selenium.jar
set JAR_PATH_JSON=!JAR_PATH:\=\\!

REM Check if config file exists
if exist "%CONFIG_FILE%" (
    echo WARNING: Claude Desktop config already exists.
    echo Please manually add MCP Selenium to: %CONFIG_FILE%
    echo.
    echo Add this configuration to the mcpServers section:
    echo "selenium": {
    echo   "command": "java",
    echo   "args": ["-jar", "!JAR_PATH_JSON!"]
    echo }
    echo.
) else (
    echo Creating Claude Desktop config...
    (
        echo {
        echo   "mcpServers": {
        echo     "selenium": {
        echo       "command": "java",
        echo       "args": ["-jar", "!JAR_PATH_JSON!"]
        echo     }
        echo   }
        echo }
    ) > "%CONFIG_FILE%"
    echo Config created successfully
    echo.
)

echo Installation completed!
echo.
echo Next steps:
echo 1. Restart Claude Desktop
echo 2. Test with: 'Using selenium MCP tools, navigate to google.com and take a screenshot'
echo.
echo For support, visit: https://github.com/abarrac/mcp-selenium
echo.
pause
