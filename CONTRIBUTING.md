# Contributing to MCP Selenium

Thank you for your interest in contributing to MCP Selenium! This document provides guidelines and information for contributors.

## 🚀 Quick Start for Contributors

### Development Setup

1. **Fork and Clone**
   ```bash
   git clone https://github.com/yourusername/mcp-selenium.git
   cd mcp-selenium
   ```

2. **Requirements**
   - Java 11 or higher
   - Maven 3.6+
   - Chrome browser (for testing)

3. **Build**
   ```bash
   mvn clean compile
   ```

4. **Run Tests**
   ```bash
   mvn test
   ```

5. **Build JAR**
   ```bash
   mvn clean package
   ```

## 🏗️ Project Structure

```
mcp-selenium/
├── src/main/java/com/mcp/selenium/
│   ├── SeleniumMCP.java          # Main orchestrator
│   ├── SeleniumServer.java       # MCP protocol server
│   └── handlers/                 # Specialized handlers
│       ├── ElementHandler.java   # DOM interactions
│       ├── NavigationHandler.java # Page navigation
│       ├── ScreenshotHandler.java # Visual capture
│       └── ScriptHandler.java    # JavaScript execution
├── src/test/java/               # Unit tests
├── pom.xml                      # Maven configuration
└── README.md                    # Documentation
```

## 🎯 Areas for Contribution

### High Priority
- 🌐 **Browser Support**: Firefox, Edge, Safari improvements
- 📱 **Mobile**: Appium/mobile browser support
- 🔧 **Features**: New automation capabilities

### Medium Priority
- ⚡ **Performance**: Optimize element finding and interactions
- 🛡️ **Security**: Enhanced anti-detection measures
- 🧪 **Test Coverage**: Expand unit and integration tests
- 📚 **Documentation**: Improve examples and guides
- 🐛 **Bug Fixes**: Address reported issues

### Nice to Have
- 🎨 **UI**: Web dashboard for monitoring
- 📊 **Analytics**: Usage metrics and reporting
- 🔌 **Integrations**: CI/CD platform plugins

## 📝 Code Style

### Java Conventions
- Follow Google Java Style Guide
- Use meaningful variable and method names
- Add comprehensive JavaDoc for public methods
- Maximum line length: 120 characters

### Documentation
- All public APIs must have JavaDoc
- Include @param, @return, @throws annotations
- Add usage examples for complex methods
- Write in clear, professional English

### Example:
```java
/**
 * Performs a click operation on the specified element.
 * 
 * Locates the element, ensures it's visible and clickable, then performs
 * the click action. Includes automatic scrolling and wait conditions.
 * 
 * @param selector the element selector (supports all selector types)
 * @return success message indicating the click was performed
 * @throws RuntimeException if the element cannot be found or clicked
 */
public String click(String selector) {
    // Implementation
}
```

## 🧪 Testing Guidelines

### Unit Tests
- Test each handler independently
- Mock WebDriver for isolated testing
- Cover both success and error scenarios
- Maintain >80% code coverage

### Integration Tests
- Test against real browsers
- Use headless mode for CI/CD
- Test cross-browser compatibility
- Verify MCP protocol compliance

### Test Structure
```java
@Test
public void testClickElement_Success() {
    // Given
    String selector = "#submit-button";
    
    // When
    String result = elementHandler.click(selector);
    
    // Then
    assertThat(result).contains("Clicked element");
}
```

## 🔄 Pull Request Process

### Before Submitting
1. **Create Issue**: Discuss major changes first
2. **Fork Repository**: Work on your own fork
3. **Create Branch**: Use descriptive branch names
   ```bash
   git checkout -b feature/add-drag-and-drop
   git checkout -b fix/screenshot-timeout-issue
   ```

### PR Requirements
- ✅ All tests pass
- ✅ Code coverage maintained/improved
- ✅ Documentation updated
- ✅ No breaking changes (or clearly documented)
- ✅ Commits follow conventional format

### Commit Messages
Use [Conventional Commits](https://conventionalcommits.org/) format:

```
feat: add drag and drop support for elements
fix: resolve screenshot timeout in headless mode
docs: update installation instructions for Windows
test: add integration tests for navigation handler
```

### PR Template
```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests pass
- [ ] Manual testing completed

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] No breaking changes
```

## 🐛 Reporting Issues

### Bug Reports
Use the issue template and include:
- **Environment**: OS, Java version, browser
- **Steps to Reproduce**: Clear, numbered steps
- **Expected Behavior**: What should happen
- **Actual Behavior**: What actually happens
- **Logs**: Relevant error messages/stack traces

### Feature Requests
- **Use Case**: Why is this needed?
- **Proposed Solution**: How should it work?
- **Alternatives**: Other approaches considered
- **Additional Context**: Examples, mockups, etc.

## 🏷️ Release Process

### Versioning
We follow [Semantic Versioning](https://semver.org/):
- **MAJOR**: Breaking changes
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes (backward compatible)

### Release Checklist
1. Update version in `pom.xml`
2. Update `CHANGELOG.md`
3. Create GitHub release
4. Deploy to Maven Central
5. Update documentation

## 💡 Development Tips

### Debugging
- Use `System.err.println()` for debugging (goes to Claude logs)
- Set `SELENIUM_HEADLESS=false` to see browser actions
- Enable detailed logging in `logback.xml`

### Testing Locally
```bash
# Build and test locally
mvn clean package
java -jar target/mcp-selenium-1.0.0.jar

# Test with Claude Desktop
# Update your Claude config to point to local JAR
```

### Performance Tips
- Use explicit waits over Thread.sleep()
- Minimize DOM queries
- Reuse WebDriver instances when possible
- Optimize screenshot compression

## 🤝 Community

- **Discussions**: Use GitHub Discussions for questions
- **Issues**: Use GitHub Issues for bugs/features
- **Discord**: Join our community server (coming soon)

## 📄 License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

**Thank you for contributing to MCP Selenium! Together we're building the future of AI-powered web automation.** 🚀
