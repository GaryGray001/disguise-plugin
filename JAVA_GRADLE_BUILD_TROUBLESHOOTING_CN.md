# Java / Gradle 项目构建故障排查与修复记录

> 适用场景：Java、Minecraft Paper 插件等使用 Gradle 的项目，在 VS Code
> 中出现 `Gradle: Build Error`、Java Workspace 长时间初始化、Gradle
> 下载失败，或者无法生成 JAR 时参考。

## 1. 这次遇到的问题

项目在 VS Code 中长期出现：

-   `Java: Initialize Workspace` 卡住
-   `Gradle: Build Error`
-   Gradle 项目无法正常导入
-   VS Code 反复尝试下载 Gradle

最关键的错误是：

``` text
Could not install Gradle distribution from
https://services.gradle.org/distributions/gradle-9.2.0-bin.zip
```

首先要判断：这是 Java 源代码错误，还是开发环境 / 构建工具错误。

## 2. 为什么会出现这个问题

本次故障链路可以概括为：

``` text
项目的 Gradle Wrapper 不完整或无法正常使用
        ↓
VS Code / Gradle 扩展尝试获取指定 Gradle Distribution
        ↓
网络访问 Gradle → GitHub Release Asset 很慢或中断
        ↓
Gradle Distribution 安装失败
        ↓
Gradle 项目无法正常导入
        ↓
Java Language Server 无法得到完整 classpath
        ↓
Workspace 初始化卡住并显示 Build Error
```

所以，VS Code 报红并不意味着 Java 代码一定写错了。

本次还有一个容易混淆的现象：系统终端中的 `java -version` 曾显示 Java
25，但最终 Gradle Wrapper 实际使用的是 Java 21：

``` text
Launcher JVM: 21.0.11
Daemon JVM: /usr/lib/jvm/java-21-openjdk-amd64
```

判断构建环境时，应同时检查 Gradle 实际使用的 JVM。

## 3. 正确的排错顺序

### Step 1：确认项目目录

``` bash
pwd
ls
```

正常 Gradle 项目通常至少应看到：

``` text
src/
build.gradle
settings.gradle
```

完善的项目还应该包含：

``` text
gradlew
gradlew.bat
gradle/
└── wrapper/
    ├── gradle-wrapper.jar
    └── gradle-wrapper.properties
```

### Step 2：检查 Java

``` bash
java -version
javac -version
```

Java 无法执行时，先解决 JDK、`JAVA_HOME` 或 PATH。

### Step 3：检查 Gradle Wrapper

Linux / WSL：

``` bash
./gradlew --version
```

Windows PowerShell：

``` powershell
.\gradlew.bat --version
```

### Step 4：绕开 IDE，直接构建

``` bash
./gradlew clean build
```

如果得到：

``` text
BUILD SUCCESSFUL
```

说明项目已经能够由 Gradle 成功构建。如果 VS Code 仍报错，应优先检查 IDE
导入、缓存和 Java Language Server。

代码错误常见形式：

``` text
cannot find symbol
incompatible types
';' expected
```

环境 / 构建错误常见形式：

``` text
JAVA_HOME is not set
Could not install Gradle distribution
Could not resolve dependency
Connection timed out
```

## 4. 我们这次是怎么修好的

### 第一阶段：确认网络问题

执行：

``` bash
curl -IL --max-time 30 https://services.gradle.org/distributions/gradle-9.2.0-bin.zip
```

URL 可以访问，但大文件下载需要经过 Gradle 服务、GitHub 和 Release
Asset，实际下载非常慢，最终出现：

``` text
curl: (18) Transferred a partial file
```

因此停止反复下载。

### 第二阶段：寻找机器上已有 Gradle

执行：

``` bash
ls -R ~/.gradle/wrapper/dists | head -80
find ~/.gradle -type f -name gradle 2>/dev/null | head -20
```

发现机器已有完整 Gradle 9.5.0，于是直接使用它构建：

``` bash
/path/to/gradle-9.5.0/bin/gradle clean build
```

结果：

``` text
BUILD SUCCESSFUL
```

这一结果证明项目本身可以构建，问题主要集中在 Wrapper / IDE 环境。

### 第三阶段：修复项目 Gradle Wrapper

使用本地 Gradle 生成 Wrapper：

``` bash
/path/to/gradle-9.5.0/bin/gradle \
  wrapper \
  --gradle-version 9.5.0 \
  --no-validate-url
```

结果：

``` text
BUILD SUCCESSFUL
```

项目随后具备：

``` text
gradlew
gradlew.bat
gradle/wrapper/gradle-wrapper.jar
gradle/wrapper/gradle-wrapper.properties
```

验证：

``` bash
./gradlew --version
```

得到 Gradle 9.5.0，并确认 Gradle 使用 Java 21。

最终执行：

``` bash
./gradlew clean build
```

再次得到：

``` text
BUILD SUCCESSFUL
```

### 第四阶段：让 VS Code 重新识别项目

Gradle 命令行成功后，在 VS Code 执行：

``` text
Java: Clean Java Language Server Workspace
```

重新加载项目后最终显示：

``` text
Importing Gradle project(s) [Done]
Validating Gradle wrapper checksum... [Done]
Building [Done]
Refreshing workspace [Done]
Java: Ready
```

至此故障闭环。

## 5. 为什么推荐 Gradle Wrapper

Gradle Wrapper 让项目自己规定 Gradle 版本。

不同电脑可能安装：

``` text
A：Gradle 8.9
B：Gradle 9.2
C：Gradle 9.5
```

直接运行系统 `gradle` 可能产生环境差异。

项目提供 Wrapper 后，统一运行：

``` bash
./gradlew build
```

可以显著减少"我的电脑能跑，你的电脑不能跑"的情况。这属于 **Reproducible
Build（可复现构建）** 的基本工程实践。

## 6. Gradle 的作用

Gradle 不只是生成 JAR。

``` text
Java 源代码
   ↓
Gradle 读取 build.gradle
   ↓
解析 / 下载依赖
   ↓
编译 Java
   ↓
执行测试和资源处理
   ↓
打包
   ↓
build/libs/*.jar
```

对于 Minecraft Paper 插件，通常将最终 JAR 放入服务器的：

``` text
plugins/
```

再启动服务器进行游戏内测试。

## 7. `clean build` 是什么意思

``` bash
./gradlew clean build
```

`clean`：删除旧的 `build/` 构建产物。

`build`：重新编译、测试、处理资源并打包。

因此正式验证或发布前，可以把它理解为：

> 从干净状态重新完整构建一次项目。

构建完成后查看：

``` bash
ls -lh build/libs/
```

## 8. 给学生使用的固定五条命令

遇到 Java / Gradle 项目报错时，先不要乱改代码：

``` bash
pwd
java -version
javac -version
./gradlew --version
./gradlew clean build
```

按照层次定位：

``` text
源代码
   ↓
JDK
   ↓
Gradle / Wrapper
   ↓
依赖与网络
   ↓
VS Code / Java Language Server
```

核心原则：

> **先用命令行确认项目能不能 build，再处理 VS Code。**

如果 `./gradlew clean build` 已经显示 `BUILD SUCCESSFUL`，不要因为 VS
Code 暂时显示红色就立即重写 Java 代码。

## 9. 学生遇到相同问题时的标准流程

``` text
VS Code 出现 Build Error
        ↓
确认 pwd 和项目目录
        ↓
java -version / javac -version
        ↓
./gradlew --version
        ↓
./gradlew clean build
        ↓
   构建成功？
     ↙     ↘
   YES      NO
    ↓        ↓
检查 VS Code   阅读第一处真实错误
Java Language   判断代码 / JDK /
Server 和缓存   Gradle / 网络 / 依赖
    ↓
Java: Clean Java Language Server Workspace
    ↓
重新导入 Gradle 项目
```

## 10. 本次最终状态

``` text
Gradle Wrapper        9.5.0       PASS
Gradle 使用 JVM       Java 21     PASS
./gradlew --version               PASS
./gradlew clean build             PASS
VS Code Gradle import             PASS
Java Workspace                    Ready
```

构建过程中仍出现 deprecated API / Gradle feature
warning。它们不等于构建失败，但后续维护时应单独检查，尤其是在升级 Gradle
10 或新版 API 前。

## 11. 最值得记住的一句话

> **看到 IDE 报错，先确定是哪一层出了问题。先验证 Java，再验证 Gradle
> Wrapper，再执行命令行构建，最后处理 VS Code。`BUILD SUCCESSFUL`
> 是判断构建链是否健康的重要证据。**
