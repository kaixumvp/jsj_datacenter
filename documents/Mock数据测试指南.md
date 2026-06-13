# 砂石设备监测数据 - Mock 数据测试指南

## 📋 概述

为了方便本地开发和测试，系统提供了 **Mock 数据模式**。启用后，定时任务将使用模拟数据而不是调用真实的外部接口。

---

## 🔧 配置说明

### 配置文件位置

`src/main/resources/application-dev.yml`

### 配置项

```yaml
environment:
  api:
    base-url: http://localhost:8080  # 实际API地址（Mock模式下不使用）
    mock-enabled: true  # 是否启用Mock数据模式
```

### 配置说明

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `mock-enabled` | boolean | true | 是否启用Mock数据模式 |
| `base-url` | String | http://localhost:8080 | 外部API的基础URL |

---

## 🎯 Mock 数据特性

### 1. 数据生成规则

Mock 数据会生成 **3个设备** 的监测数据，包含以下字段：

#### 设备信息
- **设备ID**: 1001, 1002, 1003
- **设备名称**: 砂石监测设备01, 02, 03
- **节点**: 1, 2, 3
- **设备位置**: 采石场A区、破碎车间、筛分车间等

#### 环境监测数据（随机生成）
| 字段 | 范围 | 单位 |
|------|------|------|
| PM2.5 | 20-50 | ug/m³ |
| PM10 | 40-80 | ug/m³ |
| TSP | 60-100 | ug/m³ |
| 噪声 | 45-65 | dB |
| 温度 | 15-35 | ℃ |
| 湿度 | 40-80 | % |
| 风力 | 1-5 | m/s |
| 风速 | 1-5 | m/s |
| 风向 | 0-360 | 度 |
| 经度 | 104.0-104.5 | - |
| 纬度 | 30.5-31.0 | - |

#### 其他信息
- **坐标类型**: 3 (GPS)
- **继电器状态**: 正常
- **创建时间**: 当前时间往前推 0-30 分钟
- **风向描述**: 东风、东南风、南风等（随机）

---

## 🚀 使用方法

### 方式一：启用 Mock 模式（推荐用于本地开发）

保持配置文件中的设置：
```yaml
environment:
  api:
    mock-enabled: true
```

启动应用后，定时任务会自动使用 Mock 数据。

### 方式二：禁用 Mock 模式（连接真实API）

修改配置文件：
```yaml
environment:
  api:
    mock-enabled: false
    base-url: http://your-real-api-server:port
```

---

## 📊 测试步骤

### 1. 执行建表SQL

```bash
mysql -u root -p datacenter < SQL/init.sql
mysql -u root -p datacenter < SQL/sand_equipment_monitor.sql
```

### 2. 启动应用

```bash
mvn spring-boot:run -P dev
```

### 3. 观察日志输出

启动后，您应该看到类似以下的日志：

```
========== 定时任务触发：砂石设备监测数据同步 ==========
Mock模式: 启用
使用Mock数据模式
生成Mock数据 3 条
========== 开始同步砂石设备监测数据 ==========
成功获取 3 条监测数据
成功保存 3 条监测数据
========== 砂石设备监测数据同步完成，共同步 3 条数据 ==========
```

### 4. 手动触发测试

访问 Swagger UI：
```
http://localhost:8080/doc.html
```

找到 **"砂石设备监测数据管理"** → **"手动触发砂石设备监测数据同步"**

或者使用 curl：
```bash
curl -X POST http://localhost:8080/api/sand-monitor/sync
```

### 5. 查询数据库验证

```sql
-- 查看同步的数据
SELECT * FROM sand_equipment_monitor ORDER BY sync_time DESC LIMIT 10;

-- 查看数据统计
SELECT 
    COUNT(*) as total_records,
    MIN(create_time) as earliest_data,
    MAX(create_time) as latest_data
FROM sand_equipment_monitor;
```

---

## 🔍 Mock 数据示例

每次生成的数据会有所不同，以下是示例：

```json
{
  "id": 1,
  "deviceId": 1001,
  "node": 1,
  "pm2": 35.7,
  "pm10": 62.3,
  "tsp": 78.9,
  "noise": 52.1,
  "temperature": 23.5,
  "humidity": 65.2,
  "windPower": 2.8,
  "windSpeed": 2.8,
  "windDirect": 135.6,
  "windDirectDegrees": 135.6,
  "lng": 104.23,
  "lat": 30.67,
  "coordinateType": 3,
  "relayStatus": "正常",
  "createTime": "2026-06-12 15:45:30",
  "deviceName": "砂石监测设备01",
  "position": "采石场A区",
  "windDir": "东南风"
}
```

---

## ⚙️ 切换真实 API

当您准备好连接真实的 API 时：

### 1. 修改配置

```yaml
environment:
  api:
    mock-enabled: false
    base-url: http://your-api-server:port
```

### 2. 重启应用

```bash
mvn spring-boot:run -P dev
```

### 3. 观察日志

现在应该会看到真实的 API 调用日志：
```
开始调用环境监测接口: http://your-api-server:port/prod-api/environmentNew/list
```

---

## 🐛 常见问题

### Q1: Mock 数据没有生成？

**A**: 检查以下几点：
1. 确认 `mock-enabled: true` 配置正确
2. 查看日志中是否有 "使用Mock数据模式" 的输出
3. 确认应用已重新启动

### Q2: 数据保存到数据库失败？

**A**: 检查：
1. 数据库是否已创建 `sand_equipment_monitor` 表
2. 数据库连接配置是否正确
3. 查看日志中的错误信息

### Q3: 如何调整 Mock 数据的数量？

**A**: 修改 `SandEquipmentMonitorService.getMockData()` 方法中的循环次数：
```java
// 将 3 改为您想要的数量
for (int i = 1; i <= 3; i++) {
    // ...
}
```

### Q4: 如何自定义 Mock 数据的范围？

**A**: 修改 `getMockData()` 方法中的随机数范围，例如：
```java
// 修改 PM2.5 的范围为 10-40
vo.setPm2(10.0f + (float)(Math.random() * 30));
```

---

## 📝 注意事项

1. **开发环境建议启用 Mock 模式**
   - 避免依赖外部服务
   - 提高测试效率
   - 减少网络请求

2. **生产环境务必禁用 Mock 模式**
   ```yaml
   mock-enabled: false
   ```

3. **Mock 数据是随机生成的**
   - 每次执行生成的数据都不同
   - 适合功能测试，不适合数据准确性验证

4. **定时任务每15分钟执行一次**
   - 可以通过手动触发接口立即测试
   - 也可以等待自动执行

---

## 🎉 总结

- ✅ Mock 模式已启用，无需外部 API 即可测试
- ✅ 自动生成 3 个设备的模拟数据
- ✅ 数据范围符合实际情况
- ✅ 可随时切换到真实 API
- ✅ 支持手动触发和自动定时执行

现在您可以启动应用进行测试了！🚀
