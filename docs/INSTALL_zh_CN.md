# 安装

## 可运行JAR

* 稳定版(Stable)

Java 22 和 JavaFx 24 是运行 Mindolph 稳定版的最低版本要求.   	

先下载支持您的系统的 JavaFX SDK 并解压缩到某个目录，例如: `c:\javafx-sdk-24`, 按照以下方式运行:     

```
java --module-path c:\javafx-sdk-24\lib --add-modules java.sql,javafx.controls,javafx.fxml,javafx.swing,javafx.web,jdk.crypto.ec -jar  Mindolph-1.12.10.jar
```

* 不稳定版(Unstable)

Java 23 和 JavaFx 25 是运行 Mindolph 不稳定版的最低版本要求.   	

先下载支持您的系统的 JavaFX SDK 并解压缩到某个目录，例如: `c:\javafx-sdk-25`, 按照以下方式运行:     

```
java --module-path c:\javafx-sdk-25\lib --add-modules java.base,java.sql,java.logging,java.desktop,java.net.http,java.management,java.naming,java.security.jgss,javafx.graphics,javafx.controls,javafx.fxml,javafx.swing,javafx.web,jdk.crypto.ec,jdk.unsupported -jar Mindolph-1.13.2.jar
```

---
> Created at 2025-10-19 11:47:09
