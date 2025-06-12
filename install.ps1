# MCP Selenium Installation Script for Windows
# PowerShell script for native Windows installation

Write-Host "🚀 Installing MCP Selenium for Windows..." -ForegroundColor Green

# Check if Java is installed
try {
    $javaVersion = java -version 2>&1 | Select-String "version" | ForEach-Object { $_.ToString().Split('"')[1] }
    $majorVersion = $javaVersion.Split('.')[0]
    if ([int]$majorVersion -lt 11) {
        Write-Host "❌ Java 11+ required. Current version: $javaVersion" -ForegroundColor Red
        Write-Host "Please download Java 11*" -ForegroundColor Yellow
        exit 1
    }
    Write-Host "✅ Java $javaVersion found" -ForegroundColor Green
}
catch {
    Write-Host "❌ Java is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install Java 11+" -ForegroundColor Yellow
    exit 1
}

# Create installation directory
$installDir = "$env:USERPROFILE\.mcp-selenium"
if (-not (Test-Path $installDir)) {
    New-Item -ItemType Directory -Path $installDir -Force | Out-Null
}

Write-Host "📦 Downloading MCP Selenium..." -ForegroundColor Blue

# Download the JAR file
$jarUrl = "https://github.com/abarrac/mcp-selenium/releases/latest/download/mcp-selenium-1.0.0.jar"
$jarPath = "$installDir\mcp-selenium.jar"

try {
    Invoke-WebRequest -Uri $jarUrl -OutFile $jarPath -UseBasicParsing
    Write-Host "✅ MCP Selenium downloaded successfully" -ForegroundColor Green
}
catch {
    Write-Host "❌ Failed to download MCP Selenium JAR" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Claude Desktop config path for Windows
$configDir = "$env:APPDATA\Claude"
$configFile = "$configDir\claude_desktop_config.json"

Write-Host "📁 Detected OS: Windows" -ForegroundColor Blue
Write-Host "📄 Config location: $configFile" -ForegroundColor Blue

# Create Claude config directory if it doesn't exist
if (-not (Test-Path $configDir)) {
    New-Item -ItemType Directory -Path $configDir -Force | Out-Null
}

# Convert path to proper Windows format and escape backslashes for JSON
$jarPathJson = $jarPath -replace '\\', '\\'

# Check if config file exists
if (Test-Path $configFile) {
    Write-Host "⚠️⚠️⚠️⚠️  Claude Desktop config already exists." -ForegroundColor Yellow
    Write-Host "   Please manually add MCP Selenium to: $configFile" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "   Add this configuration to the mcpServers section:" -ForegroundColor Yellow
    Write-Host '   "selenium": {' -ForegroundColor Cyan
    Write-Host '     "command": "java",' -ForegroundColor Cyan
    Write-Host "     `"args`": [`"-jar`", `"$jarPathJson`"]" -ForegroundColor Cyan
    Write-Host '   }' -ForegroundColor Cyan
} else {
    Write-Host "📝 Creating Claude Desktop config..." -ForegroundColor Blue
    
    $configContent = @"
{
  "mcpServers": {
    "selenium": {
      "command": "java",
      "args": ["-jar", "$jarPathJson"]
    }
  }
}
"@
    
    $configContent | Out-File -FilePath $configFile -Encoding UTF8
    Write-Host "✅ Config created at: $configFile" -ForegroundColor Green
}

Write-Host ""
Write-Host "🎉 Installation completed!" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Restart Claude Desktop" -ForegroundColor White
Write-Host "2. Test with: 'Take a screenshot of google.com'" -ForegroundColor White
Write-Host ""
Write-Host "For support, visit: https://github.com/abarrac/mcp-selenium" -ForegroundColor Blue

# Pause to let user read the output
Write-Host ""
Write-Host "Press any key to continue..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
