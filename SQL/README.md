# 数据库表结构清单

## 📋 概述

本文档记录了项目中所有数据库表的建表脚本位置和说明。

---

## ✅ 已有 SQL 脚本的表

所有表的建表脚本都在 `SQL/init.sql` 文件中（除了砂石设备监测数据表单独存放）。

### 1. 基础数据表

| 序号 | 表名 | 中文名 | 脚本位置 | 说明 |
|------|------|--------|----------|------|
| 1 | `file_items` | 文件历史记录 | SQL/init.sql | 存储上传文件的元数据 |
| 2 | `device_data` | 设备实时监测数据 | SQL/init.sql | 存储设备实时监测的环境数据 |

### 2. 水质监测表

| 序号 | 表名 | 中文名 | 脚本位置 | 说明 |
|------|------|--------|----------|------|
| 3 | `water_quality` | 水质检测表 | SQL/init.sql | 存储水质检测数据 |
| 4 | `over_saturation` | 过饱和检测主表 | SQL/init.sql | 存储过饱和检测周期数据 |
| 5 | `over_saturation_point` | 过饱和监测点位表 | SQL/init.sql | 存储过饱和监测点详细数据 |

### 3. 环保措施表

| 序号 | 表名 | 中文名 | 脚本位置 | 说明 |
|------|------|--------|----------|------|
| 6 | `env_measures` | 环保措施进度 | SQL/init.sql | 存储环保措施及实施进度 |

### 4. 温度监测表

| 序号 | 表名 | 中文名 | 脚本位置 | 说明 |
|------|------|--------|----------|------|
| 7 | `temperature_error_log` | 温度异常日志表 | SQL/init.sql | 存储温度异常检测记录 |

### 5. 水温方案表

| 序号 | 表名 | 中文名 | 脚本位置 | 说明 |
|------|------|--------|----------|------|
| 8 | `water_temperature_plan` | 水温方案记录表 | SQL/init.sql | 水温计算方案主表 |
| 9 | `water_temperature_plan_file_items` | 水温方案关联文件表 | SQL/init.sql | 方案与文件的关联关系 |
| 10 | `wa_schema` | 水温评估方案主表 | SQL/init.sql | 评估方案基本信息 |
| 11 | `wa_schema_detail` | 水温评估方案明细表 | SQL/init.sql | 评估方案详细参数 |
| 12 | `wa_schema_calculate_result` | 水温方案计算结果表 | SQL/init.sql | 方案计算结果存储 |
| 13 | `water_temperature_reports` | 水温分析报告表 | SQL/init.sql | 水温分析报告信息 |

### 6. 砂石设备监测表

| 序号 | 表名 | 中文名 | 脚本位置 | 说明 |
|------|------|--------|----------|------|
| 14 | `sand_equipment_monitor` | 砂石设备监测数据 | SQL/sand_equipment_monitor.sql | 定时同步的砂石设备监测数据 |

---

## 📊 表分类统计

### 按功能模块分类

| 模块 | 表数量 | 表名列表 |
|------|--------|----------|
| 文件管理 | 1 | file_items |
| 设备监测 | 2 | device_data, sand_equipment_monitor |
| 水质监测 | 3 | water_quality, over_saturation, over_saturation_point |
| 环保措施 | 1 | env_measures |
| 温度监测 | 1 | temperature_error_log |
| 水温方案 | 6 | water_temperature_plan, water_temperature_plan_file_items, wa_schema, wa_schema_detail, wa_schema_calculate_result, water_temperature_reports |

**总计**: 14 张表

### 按脚本文件分类

| 脚本文件 | 表数量 | 说明 |
|----------|--------|------|
| SQL/init.sql | 13 | 主要建表脚本 |
| SQL/sand_equipment_monitor.sql | 1 | 砂石设备监测表（独立） |

---

## 🔧 使用说明

### 初始化数据库

执行以下命令初始化所有表：

```bash
# 方式1：使用 MySQL 命令行
mysql -u root -p datacenter < SQL/init.sql

# 方式2：分别执行
mysql -u root -p datacenter < SQL/init.sql
mysql -u root -p datacenter < SQL/sand_equipment_monitor.sql
```

### 检查表是否创建成功

```sql
-- 查看所有表
SHOW TABLES;

-- 查看特定表结构
DESCRIBE device_data;
DESCRIBE sand_equipment_monitor;
```

---

## 📝 注意事项

1. **字符集**: 所有表统一使用 `utf8mb4` 字符集
2. **引擎**: 
   - `file_items` 使用 MyISAM 引擎
   - 其他表使用 InnoDB 引擎
3. **自增ID**: 大部分表使用 AUTO_INCREMENT 主键
4. **索引**: 已为常用查询字段添加索引
5. **JSON字段**: `over_saturation`、`over_saturation_point` 等表使用 JSON 类型存储复杂数据

---

## 🔄 更新记录

| 日期 | 操作 | 说明 |
|------|------|------|
| 2026-06-12 | 新增 | 添加6个缺失表的建表脚本到 init.sql |
| 2026-06-12 | 新增 | 创建 sand_equipment_monitor.sql 独立脚本 |
| 2026-06-12 | 创建 | 创建本清单文档 |

---

## 📌 待办事项

- [ ] 根据实际运行情况优化索引
- [ ] 添加数据字典文档
- [ ] 建立数据库版本管理机制
- [ ] 添加测试数据脚本
