package com.jsj.datacenter.application.environment.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jsj.datacenter.adapter.dto.environment.EnvironmentMonitorEchartsResp;
import com.jsj.datacenter.adapter.dto.environment.EnvironmentMonitorNewVo;
import com.jsj.datacenter.adapter.dto.environment.SandEquipmentMonitorQueryReq;
import com.jsj.datacenter.adapter.dto.environment.SandEquipmentMonitorResp;
import com.jsj.datacenter.application.environment.domain.SandEquipmentMonitor;
import com.jsj.datacenter.application.environment.mapper.SandEquipmentMonitorMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 砂石设备监测数据服务
 */
@Slf4j
@Service
public class SandEquipmentMonitorService {
    
    @Autowired
    private SandEquipmentMonitorMapper monitorMapper;
    
    @Value("${environment.api.base-url:http://localhost:8080}")
    private String baseUrl;
    
    @Value("${environment.api.mock-enabled:true}")
    private boolean mockEnabled;
    
    private static final String API_PATH = "/prod-api/environmentNew/list";
    
    private final OkHttpClient httpClient;
    
    public SandEquipmentMonitorService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }
    
    /**
     * 调用外部接口获取监测数据
     */
    public List<EnvironmentMonitorNewVo> fetchMonitorData() {
        // 如果启用Mock模式，返回模拟数据
        if (mockEnabled) {
            log.info("使用Mock数据模式");
            return getMockData();
        }
        
        String url = baseUrl + API_PATH;
        log.info("开始调用环境监测接口: {}", url);
        
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("调用接口失败，状态码: {}", response.code());
                    return null;
                }
                
                String responseBody = response.body().string();
                log.debug("接口响应: {}", responseBody);
                
                JSONObject result = JSON.parseObject(responseBody);
                Integer code = result.getInteger("code");
                
                if (code != null && code == 200) {
                    JSONArray dataArray = result.getJSONArray("data");
                    if (dataArray != null && !dataArray.isEmpty()) {
                        List<EnvironmentMonitorNewVo> dataList = dataArray.toJavaList(EnvironmentMonitorNewVo.class);
                        log.info("成功获取 {} 条监测数据", dataList.size());
                        return dataList;
                    } else {
                        log.warn("接口返回数据为空");
                        return new ArrayList<>();
                    }
                } else {
                    String msg = result.getString("msg");
                    log.error("接口返回错误: code={}, msg={}", code, msg);
                    return null;
                }
            }
        } catch (IOException e) {
            log.error("调用接口异常: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 保存监测数据到数据库
     */
    public int saveMonitorData(List<EnvironmentMonitorNewVo> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            log.warn("没有数据需要保存");
            return 0;
        }
        
        int successCount = 0;
        LocalDateTime syncTime = LocalDateTime.now();
        
        for (EnvironmentMonitorNewVo vo : dataList) {
            try {
                SandEquipmentMonitor monitor = new SandEquipmentMonitor();
                BeanUtils.copyProperties(vo, monitor);
                monitor.setSyncTime(syncTime);
                monitor.setId(null); // 清除ID，每次都插入新记录，让数据库自动生成ID
                
                monitorMapper.insert(monitor);
                successCount++;
            } catch (Exception e) {
                log.error("保存数据失败，设备ID: {}, 错误: {}", vo.getDeviceId(), e.getMessage(), e);
            }
        }
        
        log.info("成功保存 {} 条监测数据", successCount);
        return successCount;
    }
    
    /**
     * 同步监测数据（调用接口并保存）
     */
    public int syncMonitorData() {
        log.info("========== 开始同步砂石设备监测数据 ==========");
        log.info("Mock模式: {}", mockEnabled ? "启用" : "禁用");
        
        List<EnvironmentMonitorNewVo> dataList = fetchMonitorData();
        if (dataList == null) {
            log.error("获取数据失败，同步终止");
            return 0;
        }
        
        int savedCount = saveMonitorData(dataList);
        
        log.info("========== 砂石设备监测数据同步完成，共同步 {} 条数据 ==========", savedCount);
        return savedCount;
    }
    
    /**
     * 生成Mock数据用于本地测试
     */
    private List<EnvironmentMonitorNewVo> getMockData() {
        List<EnvironmentMonitorNewVo> mockList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        // 定义5种不同类型的设备配置
        String[] deviceTypes = {"采石场", "破碎机", "筛分机", "输送带", "料堆场"};
        String[] positions = {"采石场A区-钻孔作业点", "破碎车间-粗碎口", "筛分车间-振动筛", "皮带输送廊道-中段", "成品料堆场-北侧"};
        float[] baseLat = {30.67f, 30.68f, 30.69f, 30.70f, 30.71f};
        float[] baseLng = {104.23f, 104.24f, 104.25f, 104.26f, 104.27f};
        
        // 模拟5个不同设备的数据
        for (int i = 0; i < 5; i++) {
            EnvironmentMonitorNewVo vo = new EnvironmentMonitorNewVo();
            vo.setId((long) (i + 1));
            vo.setDeviceId(2000 + i);
            vo.setNode(i + 1);
            
            // 根据不同设备类型设置不同的数据范围
            switch (i) {
                case 0: // 采石场 - 粉尘较高
                    vo.setPm2(45.0f + (float)(Math.random() * 25));  // 45-70
                    vo.setPm10(70.0f + (float)(Math.random() * 30)); // 70-100
                    vo.setTsp(90.0f + (float)(Math.random() * 40));  // 90-130
                    vo.setNoise(65.0f + (float)(Math.random() * 15)); // 65-80 dB
                    break;
                    
                case 1: // 破碎机 - 噪声和粉尘都很高
                    vo.setPm2(50.0f + (float)(Math.random() * 30));  // 50-80
                    vo.setPm10(80.0f + (float)(Math.random() * 40)); // 80-120
                    vo.setTsp(100.0f + (float)(Math.random() * 50)); // 100-150
                    vo.setNoise(75.0f + (float)(Math.random() * 15)); // 75-90 dB
                    break;
                    
                case 2: // 筛分机 - 中等粉尘，较高噪声
                    vo.setPm2(35.0f + (float)(Math.random() * 20));  // 35-55
                    vo.setPm10(55.0f + (float)(Math.random() * 25)); // 55-80
                    vo.setTsp(70.0f + (float)(Math.random() * 30));  // 70-100
                    vo.setNoise(60.0f + (float)(Math.random() * 15)); // 60-75 dB
                    break;
                    
                case 3: // 输送带 - 较低粉尘，中等噪声
                    vo.setPm2(25.0f + (float)(Math.random() * 15));  // 25-40
                    vo.setPm10(40.0f + (float)(Math.random() * 20)); // 40-60
                    vo.setTsp(50.0f + (float)(Math.random() * 25));  // 50-75
                    vo.setNoise(50.0f + (float)(Math.random() * 10)); // 50-60 dB
                    break;
                    
                case 4: // 料堆场 - 粉尘中等，噪声较低
                    vo.setPm2(30.0f + (float)(Math.random() * 20));  // 30-50
                    vo.setPm10(50.0f + (float)(Math.random() * 25)); // 50-75
                    vo.setTsp(65.0f + (float)(Math.random() * 30));  // 65-95
                    vo.setNoise(45.0f + (float)(Math.random() * 10)); // 45-55 dB
                    break;
            }
            
            // 温湿度（所有设备相似）
            vo.setTemperature(18.0f + (float)(Math.random() * 15)); // 18-33 ℃
            vo.setHumidity(45.0f + (float)(Math.random() * 35));    // 45-80 %
            
            // 风力风速（所有设备相似）
            vo.setWindPower(1.5f + (float)(Math.random() * 3.5));   // 1.5-5 m/s
            vo.setWindSpeed(1.5f + (float)(Math.random() * 3.5));   // 1.5-5 m/s
            
            // 风向
            float windDegree = (float)(Math.random() * 360);
            vo.setWindDirect(windDegree);
            vo.setWindDirectDegrees(windDegree);
            
            // 经纬度（基于设备位置略有偏移）
            vo.setLng(baseLng[i] + (float)(Math.random() * 0.01));
            vo.setLat(baseLat[i] + (float)(Math.random() * 0.01));
            
            // 坐标类型：3-GPS
            vo.setCoordinateType(3);
            
            // 继电器状态（大部分正常，偶尔异常）
            vo.setRelayStatus(Math.random() > 0.9 ? "异常" : "正常");
            
            // 创建时间（最近30分钟内）
            vo.setCreateTime(now.minusMinutes((long)(Math.random() * 30)));
            
            // 设备名称和位置
            vo.setDeviceName(deviceTypes[i] + "监测点" + (i + 1));
            vo.setPosition(positions[i]);
            
            // 风向描述
            String[] windDirs = {"东风", "东南风", "南风", "西南风", "西风", "西北风", "北风", "东北风"};
            int windDirIndex = (int)(windDegree / 45) % 8;
            vo.setWindDir(windDirs[windDirIndex]);
            
            mockList.add(vo);
        }
        
        log.info("生成Mock数据 {} 条（5种设备类型）", mockList.size());
        return mockList;
    }
    
    /**
     * 根据设备ID获取所有的历史数据
     */
    public List<EnvironmentMonitorNewVo> getHistoryData(SandEquipmentMonitorQueryReq queryReq) {
        log.info("查询历史数据，请求参数: {}", JSON.toJSONString(queryReq));
        
        // 查询历史数据（支持设备和日期筛选）
        List<SandEquipmentMonitor> historyRecords = queryHistoryData(queryReq);
        
        // 转换为VO
        return historyRecords.stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取所有设备的最新数据
     */
    public List<EnvironmentMonitorNewVo> getLatestData() {
        log.info("查询所有设备最新数据");
        
        // 获取5类设备的最新数据
        return getLatestDataForEachDevice();
    }
    
    /**
     * 获取历史数据的ECharts格式（用于图表展示）
     */
    public EnvironmentMonitorEchartsResp getHistoryDataForEcharts(SandEquipmentMonitorQueryReq queryReq) {
        log.info("查询历史数据并转换为ECharts格式，请求参数: {}", JSON.toJSONString(queryReq));
        
        // 查询历史数据
        List<SandEquipmentMonitor> historyRecords = queryHistoryData(queryReq);
        
        if (historyRecords == null || historyRecords.isEmpty()) {
            log.warn("未查询到历史数据");
            return buildEmptyEchartsResponse();
        }
        
        // 转换为VO列表
        List<EnvironmentMonitorNewVo> voList = historyRecords.stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());
        
        // 构建ECharts格式数据
        return buildEchartsResponse(voList);
    }
    
    /**
     * 构建ECharts格式响应数据
     */
    private EnvironmentMonitorEchartsResp buildEchartsResponse(List<EnvironmentMonitorNewVo> dataList) {
        // 提取时间轴（使用createTime或syncTime格式化）
        List<String> timeAxis = dataList.stream()
                .map(vo -> vo.getCreateTime() != null ? 
                    vo.getCreateTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "")
                .collect(Collectors.toList());
        
        // 1. 温度监测数据
        EnvironmentMonitorEchartsResp.TemperatureData temperatureData = 
            EnvironmentMonitorEchartsResp.TemperatureData.builder()
                .timeAxis(timeAxis)
                .temperature(dataList.stream()
                    .map(EnvironmentMonitorNewVo::getTemperature)
                    .collect(Collectors.toList()))
                .humidity(dataList.stream()
                    .map(EnvironmentMonitorNewVo::getHumidity)
                    .collect(Collectors.toList()))
                .build();
        
        // 2. 颗粒物监测数据（PM2.5和PM10）
        EnvironmentMonitorEchartsResp.ParticulateMatterData particulateMatterData = 
            EnvironmentMonitorEchartsResp.ParticulateMatterData.builder()
                .timeAxis(timeAxis)
                .pm25(dataList.stream()
                    .map(EnvironmentMonitorNewVo::getPm2)
                    .collect(Collectors.toList()))
                .pm10(dataList.stream()
                    .map(EnvironmentMonitorNewVo::getPm10)
                    .collect(Collectors.toList()))
                .build();
        
        // 3. TSP监测数据
        EnvironmentMonitorEchartsResp.TspData tspData = 
            EnvironmentMonitorEchartsResp.TspData.builder()
                .timeAxis(timeAxis)
                .tsp(dataList.stream()
                    .map(EnvironmentMonitorNewVo::getTsp)
                    .collect(Collectors.toList()))
                .build();
        
        // 4. 风速监测数据
        EnvironmentMonitorEchartsResp.WindSpeedData windSpeedData = 
            EnvironmentMonitorEchartsResp.WindSpeedData.builder()
                .timeAxis(timeAxis)
                .windSpeed(dataList.stream()
                    .map(EnvironmentMonitorNewVo::getWindSpeed)
                    .collect(Collectors.toList()))
                .windPower(dataList.stream()
                    .map(EnvironmentMonitorNewVo::getWindPower)
                    .collect(Collectors.toList()))
                .build();
        
        // 5. 噪音分布监测数据（按区间统计）
        EnvironmentMonitorEchartsResp.NoiseDistributionData noiseDistributionData = 
            calculateNoiseDistribution(dataList);
        
        // 6. 风险监测数据
        EnvironmentMonitorEchartsResp.RiskData riskData = 
            calculateRiskData(dataList);
        
        return EnvironmentMonitorEchartsResp.builder()
                .temperatureData(temperatureData)
                .particulateMatterData(particulateMatterData)
                .tspData(tspData)
                .windSpeedData(windSpeedData)
                .noiseDistributionData(noiseDistributionData)
                .riskData(riskData)
                .build();
    }
    
    /**
     * 计算噪音分布数据
     */
    private EnvironmentMonitorEchartsResp.NoiseDistributionData calculateNoiseDistribution(
            List<EnvironmentMonitorNewVo> dataList) {
        
        // 定义噪音区间
        String[] ranges = {"0-20dB", "20-40dB", "40-60dB", "60-80dB", "80-100+dB"};
        int[] counts = new int[5];
        
        // 统计各区间的数量
        for (EnvironmentMonitorNewVo vo : dataList) {
            Float noise = vo.getNoise();
            if (noise != null) {
                if (noise < 20) {
                    counts[0]++;
                } else if (noise < 40) {
                    counts[1]++;
                } else if (noise < 60) {
                    counts[2]++;
                } else if (noise < 80) {
                    counts[3]++;
                } else {
                    counts[4]++;
                }
            }
        }
        
        // 计算总数量和百分比
        int totalCount = Arrays.stream(counts).sum();
        List<Float> percentages = new ArrayList<>();
        for (int count : counts) {
            if (totalCount > 0) {
                percentages.add(Math.round((count * 100.0f / totalCount) * 10) / 10.0f);
            } else {
                percentages.add(0.0f);
            }
        }
        
        return EnvironmentMonitorEchartsResp.NoiseDistributionData.builder()
                .ranges(Arrays.asList(ranges))
                .percentages(percentages)
                .counts(Arrays.asList(counts[0], counts[1], counts[2], counts[3], counts[4]))
                .build();
    }
    
    /**
     * 计算风险监测数据
     */
    private EnvironmentMonitorEchartsResp.RiskData calculateRiskData(
            List<EnvironmentMonitorNewVo> dataList) {
        
        // 获取设备列表
        List<String> devices = dataList.stream()
                .map(EnvironmentMonitorNewVo::getDeviceName)
                .distinct()
                .collect(Collectors.toList());
        
        // 计算各风险等级的数量
        int lowRisk = 0;      // 低风险（绿色）
        int mediumRisk = 0;   // 中风险（黄色）
        int highRisk = 0;     // 高风险（橙色）
        int criticalRisk = 0; // 严重风险（红色）
        
        for (EnvironmentMonitorNewVo vo : dataList) {
            float riskScore = calculateRiskScore(vo);
            
            if (riskScore < 30) {
                lowRisk++;
            } else if (riskScore < 60) {
                mediumRisk++;
            } else if (riskScore < 80) {
                highRisk++;
            } else {
                criticalRisk++;
            }
        }
        
        int total = lowRisk + mediumRisk + highRisk + criticalRisk;
        
        List<EnvironmentMonitorEchartsResp.RiskLevelData> riskLevels = new ArrayList<>();
        
        if (total > 0) {
            riskLevels.add(EnvironmentMonitorEchartsResp.RiskLevelData.builder()
                    .levelName("低风险")
                    .value(Math.round((lowRisk * 100.0f / total) * 10) / 10.0f)
                    .color("#52c41a")
                    .build());
            
            riskLevels.add(EnvironmentMonitorEchartsResp.RiskLevelData.builder()
                    .levelName("中风险")
                    .value(Math.round((mediumRisk * 100.0f / total) * 10) / 10.0f)
                    .color("#faad14")
                    .build());
            
            riskLevels.add(EnvironmentMonitorEchartsResp.RiskLevelData.builder()
                    .levelName("高风险")
                    .value(Math.round((highRisk * 100.0f / total) * 10) / 10.0f)
                    .color("#ff7a45")
                    .build());
            
            riskLevels.add(EnvironmentMonitorEchartsResp.RiskLevelData.builder()
                    .levelName("严重风险")
                    .value(Math.round((criticalRisk * 100.0f / total) * 10) / 10.0f)
                    .color("#ff4d4f")
                    .build());
        }
        
        return EnvironmentMonitorEchartsResp.RiskData.builder()
                .devices(devices)
                .riskLevels(riskLevels)
                .build();
    }
    
    /**
     * 计算单个监测点的风险评分
     */
    private float calculateRiskScore(EnvironmentMonitorNewVo vo) {
        float score = 0;
        
        // PM2.5风险权重
        if (vo.getPm2() != null) {
            if (vo.getPm2() > 75) score += 30;
            else if (vo.getPm2() > 50) score += 20;
            else if (vo.getPm2() > 35) score += 10;
        }
        
        // PM10风险权重
        if (vo.getPm10() != null) {
            if (vo.getPm10() > 150) score += 25;
            else if (vo.getPm10() > 100) score += 15;
            else if (vo.getPm10() > 50) score += 8;
        }
        
        // TSP风险权重
        if (vo.getTsp() != null) {
            if (vo.getTsp() > 200) score += 20;
            else if (vo.getTsp() > 150) score += 12;
            else if (vo.getTsp() > 100) score += 6;
        }
        
        // 噪声风险权重
        if (vo.getNoise() != null) {
            if (vo.getNoise() > 85) score += 15;
            else if (vo.getNoise() > 70) score += 10;
            else if (vo.getNoise() > 60) score += 5;
        }
        
        return Math.min(score, 100);
    }
    
    /**
     * 构建空的ECharts响应
     */
    private EnvironmentMonitorEchartsResp buildEmptyEchartsResponse() {
        return EnvironmentMonitorEchartsResp.builder()
                .temperatureData(EnvironmentMonitorEchartsResp.TemperatureData.builder()
                        .timeAxis(new ArrayList<>())
                        .temperature(new ArrayList<>())
                        .humidity(new ArrayList<>())
                        .build())
                .particulateMatterData(EnvironmentMonitorEchartsResp.ParticulateMatterData.builder()
                        .timeAxis(new ArrayList<>())
                        .pm25(new ArrayList<>())
                        .pm10(new ArrayList<>())
                        .build())
                .tspData(EnvironmentMonitorEchartsResp.TspData.builder()
                        .timeAxis(new ArrayList<>())
                        .tsp(new ArrayList<>())
                        .build())
                .windSpeedData(EnvironmentMonitorEchartsResp.WindSpeedData.builder()
                        .timeAxis(new ArrayList<>())
                        .windSpeed(new ArrayList<>())
                        .windPower(new ArrayList<>())
                        .build())
                .noiseDistributionData(EnvironmentMonitorEchartsResp.NoiseDistributionData.builder()
                        .ranges(Arrays.asList("0-20dB", "20-40dB", "40-60dB", "60-80dB", "80-100+dB"))
                        .percentages(Arrays.asList(0.0f, 0.0f, 0.0f, 0.0f, 0.0f))
                        .counts(Arrays.asList(0, 0, 0, 0, 0))
                        .build())
                .riskData(EnvironmentMonitorEchartsResp.RiskData.builder()
                        .devices(new ArrayList<>())
                        .riskLevels(new ArrayList<>())
                        .build())
                .build();
    }
    
    /**
     * 获取监测数据（包含最新数据和历史数据）
     * @deprecated 此方法已废弃，请使用getHistoryData和getLatestData方法
     */
    @Deprecated
    public SandEquipmentMonitorResp getMonitorData(SandEquipmentMonitorQueryReq queryReq) {
        log.info("查询监测数据，请求参数: {}", JSON.toJSONString(queryReq));
        
        // 1. 获取5类设备的最新数据
        List<EnvironmentMonitorNewVo> latestDataList = getLatestDataForEachDevice();
        
        // 2. 根据条件查询历史数据（用于图表展示）
        List<SandEquipmentMonitor> historyRecords = queryHistoryData(queryReq);
        
        // 3. 转换为VO
        List<EnvironmentMonitorNewVo> historyDataList = historyRecords.stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());
        
        // 4. 构建响应
        return SandEquipmentMonitorResp.builder()
                .latestDataList(latestDataList)
                .historyDataList(historyDataList)
                .build();
    }
    
    /**
     * 获取每个设备的最新一条数据
     */
    private List<EnvironmentMonitorNewVo> getLatestDataForEachDevice() {
        // 使用子查询获取每个设备的最新记录
        List<SandEquipmentMonitor> latestRecords = monitorMapper.selectList(
            new LambdaQueryWrapper<SandEquipmentMonitor>()
                .inSql(SandEquipmentMonitor::getId, 
                    "SELECT id FROM sand_equipment_monitor m1 " +
                    "WHERE sync_time = (" +
                    "  SELECT MAX(sync_time) FROM sand_equipment_monitor m2 " +
                    "  WHERE m2.device_id = m1.device_id" +
                    ")")
                .orderByAsc(SandEquipmentMonitor::getDeviceId)
        );
        
        return latestRecords.stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());
    }
    
    /**
     * 查询历史数据（支持设备和日期筛选，用于图表展示）
     */
    private List<SandEquipmentMonitor> queryHistoryData(SandEquipmentMonitorQueryReq queryReq) {
        LambdaQueryWrapper<SandEquipmentMonitor> wrapper = new LambdaQueryWrapper<>();
        
        // 设备筛选
        if (queryReq.getDeviceId() != null) {
            wrapper.eq(SandEquipmentMonitor::getDeviceId, queryReq.getDeviceId());
        }
        
        // 日期筛选
        if (queryReq.getStartDate() != null && !queryReq.getStartDate().isEmpty()) {
            LocalDate startDate = LocalDate.parse(queryReq.getStartDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            wrapper.ge(SandEquipmentMonitor::getSyncTime, startDate.atStartOfDay());
        }
        
        if (queryReq.getEndDate() != null && !queryReq.getEndDate().isEmpty()) {
            LocalDate endDate = LocalDate.parse(queryReq.getEndDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            wrapper.le(SandEquipmentMonitor::getSyncTime, endDate.atTime(LocalTime.MAX));
        }
        
        // 按同步时间正序排列（图表需要按时间顺序展示）
        wrapper.orderByAsc(SandEquipmentMonitor::getSyncTime);
        
        return monitorMapper.selectList(wrapper);
    }
    
    /**
     * 将实体转换为VO
     */
    private EnvironmentMonitorNewVo convertToVo(SandEquipmentMonitor monitor) {
        EnvironmentMonitorNewVo vo = new EnvironmentMonitorNewVo();
        BeanUtils.copyProperties(monitor, vo);
        return vo;
    }
}
