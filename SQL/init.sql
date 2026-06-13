DROP TABLE IF EXISTS `file_items`;
CREATE TABLE `file_items`
(
    `id`          int(10) unsigned                                             NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `file_key`    varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件key',
    `file_type`   varchar(64) COLLATE utf8mb4_general_ci                       NOT NULL COMMENT '文件所属类型',
    `filename`    varchar(255) COLLATE utf8mb4_general_ci                      NOT NULL COMMENT '文件名',
    `extension`   varchar(50) COLLATE utf8mb4_general_ci                       NOT NULL COMMENT '文件后缀',
    `path`        varchar(255) COLLATE utf8mb4_general_ci                      NOT NULL COMMENT '文件路径',
    `deleted`     tinyint(1) unsigned                                          NOT NULL DEFAULT '0' COMMENT '是否删除',
    `create_by`   varchar(255) COLLATE utf8mb4_general_ci                               DEFAULT NULL COMMENT '创建人',
    `create_time` datetime                                                              DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `file_key` (`file_key`),
    KEY `file_type` (`file_type`) USING BTREE
) ENGINE = MyISAM
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='文件历史记录';

DROP TABLE IF EXISTS `water_quality`;
CREATE TABLE `water_quality`
(
    `period`      varchar(255) NOT NULL COMMENT '期数',
    `file_key`    varchar(64)  NOT NULL COMMENT '文件key',
    `data`        json         DEFAULT NULL COMMENT '文件解析的json',
    `create_by`   varchar(255) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
    UNIQUE KEY `water_quality_unique` (`period`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='水质检测表';

DROP TABLE IF EXISTS `over_saturation`;
CREATE TABLE `over_saturation`
(
    `period`      varchar(255) NOT NULL COMMENT '期数',
    `file_key`    varchar(64)  NOT NULL COMMENT '文件key',
    `data`        json         DEFAULT NULL COMMENT '文件解析的json',
    `create_by`   varchar(255) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
    UNIQUE KEY `oversaturation_period_IDX` (`period`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='过饱和检测';

DROP TABLE IF EXISTS `env_measures`;
CREATE TABLE `env_measures`
(
    `id`             int(11) unsigned                                              NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `event_type`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '事件类型',
    `title`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '主要措施',
    `weigh`          int(10) unsigned                                              NOT NULL DEFAULT '1' COMMENT '序号',
    `content`        varchar(1000) COLLATE utf8mb4_general_ci                               DEFAULT NULL COMMENT '内容',
    `impl_condition` tinyint(1) unsigned                                           NOT NULL DEFAULT '1' COMMENT '落实情况',
    `process`        varchar(3000) COLLATE utf8mb4_general_ci                               DEFAULT NULL COMMENT '实施进度',
    `create_time`    datetime                                                               DEFAULT NULL COMMENT '创建时间',
    `update_time`    datetime                                                               DEFAULT NULL COMMENT '更新时间',
    `create_by`      varchar(255) COLLATE utf8mb4_general_ci                                DEFAULT NULL COMMENT '创建者',
    `update_by`      varchar(255) COLLATE utf8mb4_general_ci                                DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='环保措施进度';


DROP TABLE IF EXISTS `temperature_error_log`;
CREATE TABLE `temperature_error_log` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
`sn` varchar(100) DEFAULT NULL COMMENT '设备sn',
`date` date DEFAULT NULL COMMENT '异常日期',
`max_temp` double DEFAULT NULL COMMENT '最大温度',
`min_temp` double DEFAULT NULL COMMENT '最小温度',
`temp_diff` double DEFAULT NULL COMMENT '最大温差',
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COMMENT='温度异常日志表';

DROP TABLE IF EXISTS `water_temperature_plan`;
CREATE TABLE `water_temperature_plan`
(
    `id`                    int(11) unsigned                                                NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `plan_name`             varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci    NOT NULL COMMENT '方案名称',
    `plan_type`             int(10) unsigned                                                NOT NULL COMMENT '方案名称',
    `progress_status`       int(10) unsigned                                                NOT NULL DEFAULT '1' COMMENT '方案执行进度状态',
    `process`               varchar(3000) COLLATE utf8mb4_general_ci                        DEFAULT '方案执行开始' COMMENT '方案执行进度描述',
    `plan_start_time`       datetime                                                        DEFAULT NULL COMMENT '方案时段开始时间',
    `plan_end_time`         datetime                                                        DEFAULT NULL COMMENT '方案时段结束时间',
    `create_time`           datetime                                                        DEFAULT NULL COMMENT '创建时间',
    `update_time`           datetime                                                        DEFAULT NULL COMMENT '更新时间',
    `create_by`             varchar(255) COLLATE utf8mb4_general_ci                         DEFAULT NULL COMMENT '创建者',
    `update_by`             varchar(255) COLLATE utf8mb4_general_ci                         DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='水温方案记录表';


DROP TABLE IF EXISTS `water_temperature_plan_file_items`;
CREATE TABLE `water_temperature_plan_file_items`
(
    `id`                    int(11) unsigned                                                NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `plan_id`               int(11) unsigned                                                NOT NULL COMMENT '方案关联id',
    `file_id`               int(11) unsigned                                                NOT NULL COMMENT '文件列表id',
    `file_key`              varchar(255) COLLATE utf8mb4_general_ci                         DEFAULT NULL COMMENT '文件key',
    `create_time`           datetime                                                        DEFAULT NULL COMMENT '创建时间',
    `update_time`           datetime                                                        DEFAULT NULL COMMENT '更新时间',
    `create_by`             varchar(255) COLLATE utf8mb4_general_ci                         DEFAULT NULL COMMENT '创建者',
    `update_by`             varchar(255) COLLATE utf8mb4_general_ci                         DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='水温方案记录表关联文件表';

INSERT INTO `env_measures` VALUES (1, 'ENV_APPROVAL', '严格落实生态流量下泄措施', 1, '工程初期蓄水通过导流洞、生态旁通洞及大坝中孔下泄生态流量；运行期通过机组发电或生态放水孔下泄生态流量。', 3, '1.导流洞及生态旁通洞已施工完成并正常过流；\n2.计划2028年6月完成坝身中孔施工，2028年11月导流洞下闸蓄水，2029年2月完成坝身生态放水孔施工。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (2, 'ENV_APPROVAL', '严格落实生态流量下泄措施', 2, '同步建设坝下生态流量在线监测系统。', 2, '1.视频监控系统已布设，流量实时监测系统尚未布设；\n2.计划蓄水前完成流量实时监测系统布设。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (3, 'ENV_APPROVAL', '严格落实水环境保护措施', 3, '蓄水前开展水库生态环保清理。', 3, '计划2027年10月完成库底清理招标，2028年10月完成库底清理。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (4, 'ENV_APPROVAL', '严格落实水环境保护措施', 4, '严格控制污水和畜禽粪便等进入水库，控制库区渔业养殖防止对水环境造成不利影响。', 3, '计划2028年10月完成库区漂浮物清理标招标，配备打捞船，开展水库日常清理，并与地方渔政部门沟通合作。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (5, 'ENV_APPROVAL', '严格落实水环境保护措施', 5, '结合水工模型实验，进一步优化前置挡墙设计。', 1, '1.总体设计阶段，开展了水工模型实验，并开展了前置挡墙优化设计，进一步明确了在进水塔前16m设置前置挡墙；\n2.前置挡墙土建施工已纳入引水发电系统工程合同；\n3.计划2027年12月完成前置挡墙施工。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (6, 'ENV_APPROVAL', '严格落实水环境保护措施', 6, '设置库区、坝前及坝下水温监测系统。', 2, '1.2023年5月已签订安全监测施工合同，坝前水温在线监测系统已纳入该合同，目前暂不具备实施条件；\n2.2023年12月已签订环境水体监测合同，包括施工期水温人工监测、运行期水温在线监测人工复核，目前已沿昌波水电站至奔子栏河段共计布设6个水温监测站点，施工期水温监测正常进行中；\n3.计划2028年1月完成坝前水温监测系统布设。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (7, 'ENV_APPROVAL', '严格落实水环境保护措施', 7, '加强地下工程施工区周边地下水监测。', 1, '地下水监测措施已纳入环境监测服务标每季度进行一次监测。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (8, 'ENV_APPROVAL', '严格落实水生生态保护措施', 8, '制定鱼类栖息地保护规划。', 2, '1.鱼类栖息地保护规划（初稿）已编制完成；\n2.计划2025年12月前完成专家评审。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (9, 'ENV_APPROVAL', '严格落实水生生态保护措施', 9, '定曲河干流约98公里河段划为栖息地保护河段，原则上不再建设拦河建筑物。', 1, '19年甘孜州政府已发文明确栖息地范围，承诺不再建设拦河建筑。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (10, 'ENV_APPROVAL', '严格落实水生生态保护措施', 10, '在鱼根水电站和古学水电站补建过鱼设施，对现有挖沙区域进行生态修复。', 2, '1.鱼根水电站已废弃暂未拆除，定曲河上游原有2座沙场已拆除，但尚未恢复原河道生境；\n2.计划2026年3月完成鱼类栖息地保护工程设计及招标。计划2027年3月完成连通性改善工程以及生态修复工程。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (11, 'ENV_APPROVAL', '严格落实水生生态保护措施', 11, '开展升鱼机系统专项设计和相关水工模型实验，并制定过鱼规程。', 2, '1.目前过鱼设施土建施工已纳入引水发电系统标，施工单位根据招标设计完成施工组织设计编报；\n2.设计单位正在开展技施阶段升鱼机系统的施工图设计工作，针对集鱼系统，建立了1:25大比例尺水工物理模型，针对鱼类过坝系统及鱼类放流系统，对轨道线路、码头 选址等内容进行了优化；\n3.计划2028年1月前完成过鱼设施建设，并制定过鱼规程，待蓄水后正式投入运行。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (12, 'ENV_APPROVAL', '严格落实水生生态保护措施', 12, '工程蓄水前在业主营地内建成鱼类增殖放流站。', 2, '1.鱼类增殖站土建已纳入引水发电系统标，设计单位完成区域景观绿化及场平施工图，建筑单体全专业施工图正在开展设计工作，计划2025年6月底完成供图，施工单位完成施工组织设计编报；\n2.场地内移民补偿及房屋拆除均完成，初步平整及围挡施工完成；\n3.计划在2026年6月完成增殖站土建施工，2026年12月完成设备采购及安装，2027年1月增殖站投入运行。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (13, 'ENV_APPROVAL', '严格落实水生生态保护措施', 13, '放流规模为52万尾/年，结合鱼类栖息地保护需要选择合适河段开展长期增殖放流。', 3, '1.增殖站尚未建成，尚不具备鱼类繁殖条件，暂未开展增值放流；\n2.2026年3月至2026年6月完成鱼类增殖站运维标招标，增值放流工作纳入运行维护标实施。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (14, 'ENV_APPROVAL', '严格落实水生生态保护措施', 14, '加快狭孔金线鲃人工繁育技术研究，确保蓄水前取得突破。', 3, '1.环评报告书未提及开展狭孔金线鲃人工繁育技术研究，但批复文件中要求开展，根据水生生态调查结果，旭龙水电站涉及水域未发现狭孔金线鲃；\n2.计划鱼类增值站建成后开展狭孔金线鲃人工繁育技术研究。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (15, 'ENV_APPROVAL', '落实陆生生态保护措施', 15, '施工前开展动植物详细调查，针对发现的珍稀濒危保护动植物采取有效的保护措施。', 1, '1.2023年2月签订生态监测合同，已进场完成首期动植物调查，暂未发现重点保护动植物；\n2.后续若发现珍稀濒危保护动植物及时采取有效的保护措施。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (16, 'ENV_APPROVAL', '落实陆生生态保护措施', 16, '严禁随意破坏植被和捕杀野生动物。', 1, '1.常态化开展陆生生态宣传教育；\n2.未发现破坏植被和捕杀野生动物。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (17, 'ENV_APPROVAL', '落实陆生生态保护措施', 17, '优化施工布置，严格控制工程占地和施工活动范围，创新施工方法和工艺，尽量减少地表开挖和扰动，减少对植被的占用和对动物生境的破坏。\n', 1, '1.通过优化施工布置，优化设计，工程占地面积从2352公顷降低到2178公顷，降低了约7.4%，工程土石方总开挖量从1950万方降低到1500万方，降低了约23%。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (18, 'ENV_APPROVAL', '落实陆生生态保护措施', 18, '蓄水前建成种质资源园。', 2, '1.种质资源园设计工作已纳入环保总体设计报告，根据现场建设管理营地施工布置，种质资源园的建设面积约0.45hm2，包括金荞麦移栽面积不小于0.01hm2；\n2.旭龙公司在建设管理营地已临时规划相应场地作为种质资源园临时用地，设计单位已完成种质资源园场地规划布置图，计划4月25日前完成种质资源园场地规划标识牌安装；\n3.计划2025年12月完成种质资源园设计及招标，2026年10月完成种质资源园建设。\n', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (19, 'ENV_APPROVAL', '落实陆生生态保护措施', 19, '蓄水前建成动物救护站。', 1, '2025年1月签订野生动物救护救治服务合同，委托白马雪山管护局开展野生动物救护及宣传教育工作，2025年3月27日已进场开展完成2025年一季度野生动物日常巡护工作。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (20, 'ENV_APPROVAL', '落实陆生生态保护措施', 20, '针对受影响的野生动物，采取必要的廊道连通措施。', 3, '1.生态廊道措施已纳入总体设计报告；\n2.计划2027年12月完成生态廊道设计及招标，2028年10月完成生态廊道建设，蓄水前建成。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (21, 'ENV_APPROVAL', '落实陆生生态保护措施', 21, '弃渣场应先挡后弃，弃渣应运至规定的弃渣场，不得随意向河流倾倒。', 1, '1.严格落实了先挡后弃、坡面防护、临时截排水等水保措施；\n2.施工过程中，弃渣集中运至规定的弃渣场。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (22, 'ENV_APPROVAL', '落实陆生生态保护措施', 22, '施工前对表层土壤进行剥离，单独堆存并回用。', 1, '落实了表土剥离，收集至表土堆存场堆存约8万方。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (23, 'ENV_APPROVAL', '落实陆生生态保护措施', 23, '施工中采取水土保持措施，在施工结束后及时实施生态修复。', 2, '1.2024年6月签订水土保持综合治理工程（一期）合同，对前期工程施工扰动场地共计25万平方米进行绿化恢复，现施工单位已启动科研试验及现场绿化施工；\n2.前期扰动边坡临时草籽绿化约7.6hm2，营地绿化面积1.1hm2；\n3.计划2025年全面完成水保一期工程迹地绿化修复。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (24, 'ENV_APPROVAL', '其他环境保护措施', 24, '对施工废水和生活污水、扬尘、噪声、固体废物等采取有效防治措施，危险废物交有资质单位处置。', 1, '1.在施工合同中签订环保水保管理协议，将环境影响报告书相关环保措施分解到各施工合同，确保三同时实施；\n2.施工区已累计配置一体化污水处理设施18套、中水回用管线3.1km、隔油池11套、化粪池14座，环保厕所12座、吸粪车1台、5座洞室废水沉淀池；   \n3.投入高压喷枪10套，远程雾炮机6台，潜孔钻机除尘器累14套，洒水车15台，雾炮机25台，道路喷淋（喷雾）降尘管线约2千米；\n4.生活垃圾热解站已建成投入使用； 施工区已布设垃圾桶98个，垃圾箱20个；                                               5.拌和站、砂石系统落实了封闭措施，已实施砂石加工系统隔声防护棚1135.85m2，隔声墙1348.29m2 ；\n6.已建成危险废物暂存仓库3座，危险废物集中收集暂存后交有资质的单位处置。\n\n', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (25, 'ENV_APPROVAL', '其他环境保护措施', 25, '严格落实各项移民安置环保措施。', 2, '1.移民安置工程正在建设中，由地方政府负责实施，目前已编制完成环保措施专项设计报告，并完成地方备案；\n2.后续将按照设计方案逐步落实移民安置区环保措施。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (26, 'ENV_APPROVAL', '落实生态环境保护主体责任', 26, '建立企业内部生态环境管理体系，明确机构、人员、职责和制度。', 1, '1.成立了生态环境领导小组负责工程环保工作统筹推进，成立环保水保管理中心归口管理工程生态环境保护事务，形成了的多位一体环保管理组织机构；\n2.先后印发了《生态环境保护与水土保持管理办法》《生态环境保护与水土保持考核实施细则》等环保管理文件近20份，实现了环保管理制度化及规范化；\n3.签订环境监理及环境监测服务合同，同步加强工程建设期环境管理，按要求开展环境监测工作。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (27, 'ENV_APPROVAL', '落实生态环境保护主体责任', 27, '项目建设必须严格执行环境保护“三同时”制度。', 1, '见第24项。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (28, 'ENV_APPROVAL', '落实生态环境保护主体责任', 28, '与主体工程同步进行生态环境保护总体设计。', 1, '2023年3月，完成《旭龙水电站环境保护总体设计报告》。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (29, 'ENV_APPROVAL', '落实生态环境保护主体责任', 29, '在蓄水前应自行开展阶段环境保护验收，验收合格后方可蓄水。', 3, '暂不涉及，电站蓄水前及时开展蓄水阶段环境保护验收。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (30, 'ENV_APPROVAL', '落实生态环境保护主体责任', 30, '工程建成后，应按规定程序自行开展竣工环境保护验收。', 3, '暂不涉及，电站建成后及时开展竣工环境保护验收。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (31, 'ENV_APPROVAL', '落实生态环境保护主体责任', 31, '就生态调度、增殖放流、水温恢复、过鱼、栖息地保护、植物种质资源保护等措施有效性开展长期跟踪监测并评估。', 1, '相关工作有序推进，后续适时对监测结果进行评估。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (32, 'ENV_APPROVAL', '落实生态环境保护主体责任', 32, '实施竣工环境保护验收后运行5年，应按规定开展环境影响后评价。', 0, '暂不涉及，电站竣工验收后运行5年，及时开展后评价。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (33, 'ENV_EFFECT', '气体过饱和影响缓解措施', 1, '加强梯级泄洪联合调度，优化泄洪时间控制与泄洪过程，优选泄洪建筑物运用方式，合理分配泄洪流量与发电流量。', 2, '1.2023年12月签订《环境水体监测及评估分析合同》，气体过饱和监测工作内容已纳入该合同，坝下溶解性气体监测（水温、DO、TDG、气压），监测周期为2024年1月1日至2032年12月31日；\n2.目前监测单位已开展2024年度溶解性气体监测工作，对汛期（三季度）坝址上游至奔子栏河段溶解性气体进行人工监测，并形成《2024年第3季度环境水体监测季报》；\n3.后续按照合同约定开展气体过饱和监测工作，并根据监测工作完善运行调度及泄洪优化措施。\n4.运行期加强泄洪联合调度及泄洪过程优化。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (34, 'ENV_EFFECT', '环保科研项目，共9项', 2, '包括旭龙库区特有鱼类驯养繁殖技术研究、电站进水口前置挡墙优化设计及效果研究、旭龙水电站生态流量过程优化及调控措施研究、定曲河栖息地保护效果适应性管理研究、旭龙水电站升鱼机系统优化研究、库区水环境风险源调查及风险评估与研究、嘎金雪山自然保护区重点保护动物通道调查研究、旭龙水电站建设对干暖河谷植被影响机理及恢复模式研究、旭龙水电站库区外来物种入侵控制研究等。', 2, '1.已根据环保专项工程进度，开展了进水口前置挡墙优化设计及效果研究、升鱼机系统优化研究；\n2.植被恢复模式研究纳入水土保持综合治理工程（一期)科研部分正在落实中；\n3.其余环保科研工作将结合环保专项工程进度及时落实。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (35, 'ENV_EFFECT', '环境风险与应急措施', 3, '开展环境风险辨识，制定风险防范措施，形成事故应急预案。', 1, '1.环境风险与应急工作正常开展，制定了风险辨识清单及防范措施；\n2.已完成突发环境事件应急预案报备。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (36, 'ENV_EFFECT', '环境监测', 4, '包含施工期及运行期环境监测。', 1, '1.2020年11月通过招标引入中南院开展环境监测工作，包括工程建设期及运行初期环境监测任务，已形成监测年报4期、季报16期、月报47期，各项环境指标总体满足相应标准；\n2.运行期环境监测暂不涉及。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (37, 'ENV_EFFECT', '社会环境保护', 5, '增设警示牌，加强车辆维护和保养；加强施工人员宣传教育与培训，规范施工人员行为，与工程区居民和谐相处。', 1, '1.施工区已结合场内道路布设了限速牌及禁止鸣笛标识牌，车辆正常保养维护；\n2.持续强化施工人员宣传教育，维持好企地关系。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (38, 'ENV_EFFECT', '重点保护植物（金荞麦）保护措施', 6, '可在金荞麦果期（9-10月）采集种子，选择适宜生境进行播种繁殖，以保护其种质资源；将金荞麦移栽至种质资源园，种质资源园在建设管理营地内建设。', 1, '1.施工区未发现金荞麦等重点保护植物；\n2.目前已在业主建设管理营地规划种植资源园，后续发现金荞麦等重点保护植物及时采取保护措施。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (39, 'EXTRA', '低线混凝土拌和系统升级改造实现废料及废水循环利用', 1, '该套废水处理系统采用“机械预处理+高效浓密机+污泥池+箱式压滤机”的工艺，实现混凝土罐车废料的再生利用及混凝土拌和系统生产废水的循环利用“零排放”。', 1, '目前该套系统运行效果良好，相较于环评报告书中废水沉淀池“中和沉淀法”工艺，做到了废水“零排放”和砂石回收，经济效益与环境效益显著。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (40, 'EXTRA', '垃圾热解站扩容升级，实现企业与地方垃圾同步处置', 2, '垃圾热解站已建成，处理规模6t/d，高于设计标准3t/d，并将周边居民生活垃圾一并处置。', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (41, 'EXTRA', '超前开展施工迹地修复工作，打造生态恢复示范区', 3, '2024年6月，在前期工程施工收尾阶段，超前招标引进水土保持综合治理工程，大力打造生态恢复示范区。', 2, '目前正在进行4#公路边坡、2#公路DE段边坡、出渣道路边坡、低线区域7等部位绿化覆土施工，并启动现场绿化科研试验。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (42, 'EXTRA', '优化设计和施工布置，从源头减少生态破坏', 4, '工程占地面积从2352公顷降低到2178公顷，降低了约7.4%，工程土石方总开挖量从1950万方降低到1500万方，降低了约23%。', 1, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (43, 'EXTRA', '构建水环境信息动态展示及评估系统，实现数字赋能', 5, '2023年12月签订了《金沙江旭龙水电站环境水体监测及评估分析合同》，负责旭龙水电站水环境模型模块建设及模拟分析评估。构建的水环境模型和相应的水环境模型展示系统，既能动态评估旭龙水电站的水环境变化；同时又可模拟多种低温水减缓措施效果，为低温水减缓措施的设计优化提供建议；还能为旭龙水电站智慧工程信息化管理系统提供关键模块支持。', 2, '目前水环境模型模块（一期）基本建设完成，正在进行展示系统研发，后续按照合同约定，继续完成水环境模型模块（一期、二期）开发建设。', NULL, NULL, NULL, NULL);
INSERT INTO `env_measures` VALUES (44, 'EXTRA', '开展砂石废水处理与回用关键技术研究，实现废水零排放', 6, '主要开展生产废水回用标准研究、砂石加工系统生产废水处理模型试验研究、废水处理关键技术研究、废水处理原型监测、废水回用对混凝土性能的试验研究等，最终形成《金沙江旭龙水电站导流洞砂石加工系统生产废水处理与回用关键技术及现场试验研究报告》，并获得了长江委科技进步奖一等奖和湖北省技术发明二等奖。环境效益、经济效益与社会效益显著。', 1, '目前该科研工作已全部完成。', NULL, NULL, NULL, NULL);

-- ========================================
-- 设备数据表
-- ========================================
DROP TABLE IF EXISTS `device_data`;
CREATE TABLE `device_data`
(
    `id`          BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `clientid`    BIGINT(20)                   DEFAULT NULL COMMENT '客户端ID',
    `device_no`   VARCHAR(100)                 DEFAULT NULL COMMENT '设备编号',
    `timestamp`   BIGINT(20)                   DEFAULT NULL COMMENT '时间戳',
    `nox`         DOUBLE                       DEFAULT NULL COMMENT '氮氧化物浓度(mg/m³)',
    `co`          DOUBLE                       DEFAULT NULL COMMENT '一氧化碳浓度(mg/m³)',
    `hcl`         DOUBLE                       DEFAULT NULL COMMENT '氯化氢浓度(mg/m³)',
    `so2`         DOUBLE                       DEFAULT NULL COMMENT '二氧化硫浓度(mg/m³)',
    `dust`        DOUBLE                       DEFAULT NULL COMMENT '粉尘浓度(mg/m³)',
    `create_time` DATETIME                     DEFAULT NULL COMMENT '创建时间',
    `update_time` DATETIME                     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_device_no` (`device_no`),
    KEY `idx_timestamp` (`timestamp`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='设备实时监测数据表';

-- ========================================
-- 过饱和监测点表
-- ========================================
DROP TABLE IF EXISTS `over_saturation_point`;
CREATE TABLE `over_saturation_point`
(
    `id`          INT(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `period`      VARCHAR(100)              DEFAULT NULL COMMENT '期数',
    `point_name`  VARCHAR(200)              DEFAULT NULL COMMENT '监测点名称',
    `data`        JSON                      DEFAULT NULL COMMENT '监测点数据JSON',
    `create_time` VARCHAR(50)               DEFAULT NULL COMMENT '创建时间',
    `create_by`   VARCHAR(100)              DEFAULT NULL COMMENT '创建人',
    PRIMARY KEY (`id`),
    KEY `idx_period` (`period`),
    KEY `idx_point_name` (`point_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='过饱和监测点位表';

-- ========================================
-- 水温方案主表
-- ========================================
DROP TABLE IF EXISTS `wa_schema`;
CREATE TABLE `wa_schema`
(
    `id`          INT(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`        VARCHAR(200)              DEFAULT NULL COMMENT '方案名称',
    `type`        VARCHAR(100)              DEFAULT NULL COMMENT '方案类型',
    `create_time` VARCHAR(50)               DEFAULT NULL COMMENT '创建时间',
    `create_by`   VARCHAR(100)              DEFAULT NULL COMMENT '创建人',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='水温评估方案主表';

-- ========================================
-- 水温方案明细表
-- ========================================
DROP TABLE IF EXISTS `wa_schema_detail`;
CREATE TABLE `wa_schema_detail`
(
    `id`          INT(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `schema_id`   INT(11)                   DEFAULT NULL COMMENT '方案ID',
    `key`         VARCHAR(200)              DEFAULT NULL COMMENT '参数键',
    `value`       TEXT                      DEFAULT NULL COMMENT '参数值',
    `file_key`    VARCHAR(100)              DEFAULT NULL COMMENT '文件key',
    `file_data`   LONGTEXT                  DEFAULT NULL COMMENT '文件数据',
    `start_time`  VARCHAR(50)               DEFAULT NULL COMMENT '开始时间',
    `end_time`    VARCHAR(50)               DEFAULT NULL COMMENT '结束时间',
    `duration`    VARCHAR(50)               DEFAULT NULL COMMENT '持续时间',
    `create_time` VARCHAR(50)               DEFAULT NULL COMMENT '创建时间',
    `create_by`   VARCHAR(100)              DEFAULT NULL COMMENT '创建人',
    PRIMARY KEY (`id`),
    KEY `idx_schema_id` (`schema_id`),
    KEY `idx_key` (`key`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='水温评估方案明细表';

-- ========================================
-- 水温方案计算结果表
-- ========================================
DROP TABLE IF EXISTS `wa_schema_calculate_result`;
CREATE TABLE `wa_schema_calculate_result`
(
    `id`                INT(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `schema_id`         INT(11)                   DEFAULT NULL COMMENT '方案ID',
    `status`            VARCHAR(50)               DEFAULT NULL COMMENT '计算状态',
    `complete_rate`     VARCHAR(20)               DEFAULT NULL COMMENT '完成率',
    `result_file_key`   VARCHAR(100)              DEFAULT NULL COMMENT '结果文件key',
    `result_file_data`  LONGTEXT                  DEFAULT NULL COMMENT '结果文件数据',
    `log_id`            VARCHAR(100)              DEFAULT NULL COMMENT '日志ID',
    `create_by`         VARCHAR(100)              DEFAULT NULL COMMENT '创建人',
    `create_time`       VARCHAR(50)               DEFAULT NULL COMMENT '创建时间',
    `trace_id`          VARCHAR(100)              DEFAULT NULL COMMENT '追踪ID',
    PRIMARY KEY (`id`),
    KEY `idx_schema_id` (`schema_id`),
    KEY `idx_status` (`status`),
    KEY `idx_trace_id` (`trace_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='水温方案计算结果表';

-- ========================================
-- 水温报告表
-- ========================================
DROP TABLE IF EXISTS `water_temperature_reports`;
CREATE TABLE `water_temperature_reports`
(
    `id`                BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `report_name`       VARCHAR(300)                DEFAULT NULL COMMENT '报告名称',
    `report_create_time` DATETIME                   DEFAULT NULL COMMENT '报告生成时间',
    `related_task_ids`  VARCHAR(500)                DEFAULT NULL COMMENT '关联任务ID列表',
    `report_status`     INT(11)                     DEFAULT NULL COMMENT '报告状态：0-草稿,1-已完成,2-已发布',
    `create_time`       DATETIME                    DEFAULT NULL COMMENT '创建时间',
    `update_time`       DATETIME                    DEFAULT NULL COMMENT '更新时间',
    `creator`           VARCHAR(100)                DEFAULT NULL COMMENT '创建人',
    `report_url`        VARCHAR(500)                DEFAULT NULL COMMENT '报告文件URL',
    PRIMARY KEY (`id`),
    KEY `idx_report_status` (`report_status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='水温分析报告表';

-- 砂石设备监测数据表
CREATE TABLE IF NOT EXISTS `sand_equipment_monitor` (
                                                        `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `device_id` INT(11) DEFAULT NULL COMMENT '设备地址/设备ID',
    `node` INT(11) DEFAULT NULL COMMENT '节点',
    `pm2` FLOAT DEFAULT NULL COMMENT 'PM2.5 (ug/m³)',
    `pm10` FLOAT DEFAULT NULL COMMENT 'PM10 (ug/m³)',
    `tsp` FLOAT DEFAULT NULL COMMENT '总悬浮颗粒物 (ug/m³)',
    `noise` FLOAT DEFAULT NULL COMMENT '噪声 (dB)',
    `temperature` FLOAT DEFAULT NULL COMMENT '温度',
    `humidity` FLOAT DEFAULT NULL COMMENT '湿度',
    `wind_power` FLOAT DEFAULT NULL COMMENT '风力 (m/s)',
    `wind_speed` FLOAT DEFAULT NULL COMMENT '风速 (m/s)',
    `wind_direct` FLOAT DEFAULT NULL COMMENT '风向(方位数值)',
    `wind_direct_degrees` FLOAT DEFAULT NULL COMMENT '风向(角度)',
    `lng` FLOAT DEFAULT NULL COMMENT '经度',
    `lat` FLOAT DEFAULT NULL COMMENT '纬度',
    `coordinate_type` INT(11) DEFAULT NULL COMMENT '坐标类型：0-百度经纬度,1-联通基站,2-移动基站,3-GPS',
    `relay_status` VARCHAR(50) DEFAULT NULL COMMENT '继电器状态',
    `create_time` DATETIME DEFAULT NULL COMMENT '数据采集时间',
    `device_name` VARCHAR(100) DEFAULT NULL COMMENT '设备名称',
    `position` VARCHAR(200) DEFAULT NULL COMMENT '设备位置',
    `wind_dir` VARCHAR(50) DEFAULT NULL COMMENT '风向(中文描述)',
    `sync_time` DATETIME DEFAULT NULL COMMENT '数据同步时间',
    PRIMARY KEY (`id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_sync_time` (`sync_time`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='砂石设备监测数据表';
