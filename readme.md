# Terminal Utils

**Terminal Utils** is a lightweight Java utility designed for interactive terminal applications. It currently provides a flexible and visually appealing **console-based progress spinner**, which enhances the user experience during long-running tasks.

---

## Features

- Interactive progress spinner for terminal-based applications.
- UTF-8 and DOS-style spinner frame support.
- Thread-safe handling of multiple concurrent tasks.
- Environment-aware behavior (auto-disables when not in an interactive terminal).
- Optionally controlled via the `TTY` environment variable.

---

## Getting Started

### Requirements

- Java 21+ (uses virtual threads).
- Maven build system.
- Terminal with UTF-8 support (recommended).

### Installation

To use `terminal-utils` in your Maven project, include it as a dependency once the library is published to a repository:

```xml
<dependency>
  <groupId>de.rub.nds</groupId>
  <artifactId>terminal-utils</artifactId>
  <version>1.0.0</version>
</dependency>
```

