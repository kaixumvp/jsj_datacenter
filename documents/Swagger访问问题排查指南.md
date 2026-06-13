# Swagger 访问问题排查指南

## 🔍 问题诊断步骤

### 步骤1：确认应用是否启动成功

查看控制台日志，应该看到类似信息：

```
==========================================
Started DatacenterApplication in X.XXX seconds (JVM running for Y.YYY)
==========================================
Tomcat started on port(s): 8080 (http) with context path ''
==========================================
```

**如果没有看到这些信息**：
- 检查是否有错误日志
- 检查数据库连接是否正常
- 检查端口是否被占用

---

### 步骤2：确认实际运行端口

在控制台日志中查找：
```
Tomcat started on port(s): XXXX (http)
```

如果端口不是 8080，而是其他端口（如 8081），则访问地址应该是：
```
http://localhost:XXXX/doc.html
```

**常见端口**：
- `application-dev.yml` 中没有配置端口 → 默认 **8080**
- `application-fixed.yml` 中配置了 `port: 8081` → **8081**

---

### 步骤3：尝试不同的访问地址

根据您的 Knife4j 版本（3.0.3），可以尝试以下地址：

#### 地址1：Knife4j 增强版（推荐）
```
http://localhost:8080/doc.html
```

#### 地址2：原生 Swagger UI
```
http://localhost:8080/swagger-ui/index.html
```

#### 地址3：Swagger JSON 文档
```
http://localhost:8080/v3/api-docs
```

#### 地址4：旧版 Swagger UI
```
http://localhost:8080/swagger-ui.html
```

---

### 步骤4：检查是否有拦截器或过滤器

您的项目中有 Spring Security 配置，但已经允许所有路径访问：

```java
.antMatchers("/**").permitAll()
```

所以理论上不应该被拦截。

---

### 步骤5：检查 Knife4j 配置

确认 `application-dev.yml` 中有：

```yaml
knife4j:
  enabled: true
  production: false
```

✅ 已确认配置正确

---

## 🛠️ 解决方案

### 方案1：使用正确的端口

如果您的应用运行在 **8081** 端口（从 `application-fixed.yml` 看出），则应该访问：

```
http://localhost:8081/doc.html
```

**如何确认端口**：
1. 查看启动日志中的 `Tomcat started on port(s): XXXX`
2. 或者在浏览器中访问 `http://localhost:8080` 和 `http://localhost:8081`，看哪个能访问

---

### 方案2：添加 server.port 配置到 application-dev.yml

如果您希望明确使用 8080 端口，可以在 `application-dev.yml` 开头添加：

```yaml
server:
  port: 8080
```

然后重启应用。

---

### 方案3：检查依赖冲突

Knife4j 3.0.3 需要与 Spring Boot 2.x 配合使用。

您的 pom.xml 中：
- Spring Boot 版本：通过 `spring-boot-dependencies` 管理
- Knife4j 版本：3.0.3

这个组合应该是兼容的。

---

### 方案4：清除浏览器缓存

有时候浏览器缓存会导致页面加载失败。

**解决方法**：
1. 按 `Ctrl + Shift + Delete` 清除缓存
2. 或使用无痕模式访问
3. 或换一个浏览器（Chrome、Edge、Firefox）

---

## 📋 快速测试清单

请按顺序执行以下步骤：

### ✅ 1. 启动应用
```bash
# 方式1：IDE 中运行 DatacenterApplication.main()
# 方式2：Maven 命令
mvn spring-boot:run -Dspring.profiles.active=dev
```

### ✅ 2. 等待启动完成
查看日志，确认看到：
```
Started DatacenterApplication
Tomcat started on port(s): 8080 (http)
```

### ✅ 3. 测试基础访问
在浏览器中访问：
```
http://localhost:8080
```

如果能看到空白页或 404，说明应用已启动。

### ✅ 4. 访问 Swagger
尝试以下地址（按顺序）：

1. `http://localhost:8080/doc.html` ⭐ 推荐
2. `http://localhost:8080/swagger-ui/index.html`
3. `http://localhost:8080/v3/api-docs`
4. `http://localhost:8080/swagger-ui.html`

### ✅ 5. 如果都不行，检查端口
查看启动日志中的实际端口，然后替换地址中的端口号。

---

## 🎯 最可能的原因

根据我的分析，**最可能的原因是端口问题**：

1. 您的 `application-fixed.yml` 中配置了 `port: 8081`
2. 如果当前激活的 profile 是 `fixed`，则应用运行在 **8081** 端口
3. 您访问的是 `localhost:8080`，所以无法访问

**解决方法**：
1. 查看启动日志确认实际端口
2. 使用正确的端口访问：`http://localhost:XXXX/doc.html`

---

##  需要帮助？

如果以上方法都无法解决，请提供以下信息：

1. **启动日志**：完整的启动日志（特别是最后几行）
2. **访问结果**：访问 `http://localhost:8080/doc.html` 时的具体错误（404、空白页、还是其他）
3. **浏览器控制台**：按 F12 打开开发者工具，查看 Console 和 Network 标签页的错误信息

我会根据这些信息进一步帮您排查！
