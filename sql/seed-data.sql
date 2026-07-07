-- 智行云火车票系统 - 测试种子数据
-- 执行后请调用 gen-seat 和 gen-daily 接口生成座位与每日车次

USE train_business;

-- ========== 车站 ==========
INSERT INTO `station` (`id`, `name`, `name_pinyin`, `name_py`, `create_time`, `update_time`) VALUES
(1001, '北京南', 'beijingnan', 'bjn', NOW(3), NOW(3)),
(1002, '天津南', 'tianjinnan', 'tjn', NOW(3), NOW(3)),
(1003, '济南西', 'jinanxi', 'jnx', NOW(3), NOW(3)),
(1004, '南京南', 'nanjingnan', 'njn', NOW(3), NOW(3)),
(1005, '上海虹桥', 'shanghaihongqiao', 'shhq', NOW(3), NOW(3)),
(1006, '杭州东', 'hangzhoudong', 'hzd', NOW(3), NOW(3)),
(1007, '武汉', 'wuhan', 'wh', NOW(3), NOW(3)),
(1008, '广州南', 'guangzhounan', 'gzn', NOW(3), NOW(3)),
(1009, '深圳北', 'shenzhenbei', 'szb', NOW(3), NOW(3)),
(1010, '成都东', 'chengdudong', 'cdd', NOW(3), NOW(3)),
(1011, '西安北', 'xianbei', 'xab', NOW(3), NOW(3)),
(1012, '重庆北', 'chongqingbei', 'cqb', NOW(3), NOW(3));

-- ========== 车次 ==========
INSERT INTO `train` (`id`, `code`, `type`, `start`, `start_pinyin`, `start_time`, `end`, `end_pinyin`, `end_time`, `create_time`, `update_time`) VALUES
(2001, 'G1',  'G', '北京南', 'beijingnan', '09:00:00', '上海虹桥', 'shanghaihongqiao', '14:28:00', NOW(3), NOW(3)),
(2002, 'G2',  'G', '上海虹桥', 'shanghaihongqiao', '09:00:00', '北京南', 'beijingnan', '14:28:00', NOW(3), NOW(3)),
(2003, 'G3',  'G', '广州南', 'guangzhounan', '08:00:00', '深圳北', 'shenzhenbei', '08:35:00', NOW(3), NOW(3)),
(2004, 'D1',  'D', '杭州东', 'hangzhoudong', '07:30:00', '南京南', 'nanjingnan', '09:10:00', NOW(3), NOW(3)),
(2005, 'G101','G', '北京南', 'beijingnan', '08:00:00', '广州南', 'guangzhounan', '16:30:00', NOW(3), NOW(3)),
(2006, 'G201','G', '上海虹桥', 'shanghaihongqiao', '10:00:00', '成都东', 'chengdudong', '18:30:00', NOW(3), NOW(3));

-- ========== 途经站 G1 北京南->上海虹桥 ==========
INSERT INTO `train_station` (`id`, `train_code`, `index`, `name`, `name_pinyin`, `in_time`, `out_time`, `stop_time`, `km`, `create_time`, `update_time`) VALUES
(3001, 'G1', 1, '北京南', 'beijingnan', NULL, '09:00:00', NULL, 0.00, NOW(3), NOW(3)),
(3002, 'G1', 2, '天津南', 'tianjinnan', '09:31:00', '09:33:00', '00:02:00', 120.00, NOW(3), NOW(3)),
(3003, 'G1', 3, '济南西', 'jinanxi', '10:57:00', '10:59:00', '00:02:00', 350.00, NOW(3), NOW(3)),
(3004, 'G1', 4, '南京南', 'nanjingnan', '12:58:00', '13:00:00', '00:02:00', 350.00, NOW(3), NOW(3)),
(3005, 'G1', 5, '上海虹桥', 'shanghaihongqiao', '14:28:00', NULL, NULL, 300.00, NOW(3), NOW(3));

-- ========== 途经站 G2 上海虹桥->北京南 ==========
INSERT INTO `train_station` (`id`, `train_code`, `index`, `name`, `name_pinyin`, `in_time`, `out_time`, `stop_time`, `km`, `create_time`, `update_time`) VALUES
(3011, 'G2', 1, '上海虹桥', 'shanghaihongqiao', NULL, '09:00:00', NULL, 0.00, NOW(3), NOW(3)),
(3012, 'G2', 2, '南京南', 'nanjingnan', '10:28:00', '10:30:00', '00:02:00', 300.00, NOW(3), NOW(3)),
(3013, 'G2', 3, '济南西', 'jinanxi', '12:29:00', '12:31:00', '00:02:00', 350.00, NOW(3), NOW(3)),
(3014, 'G2', 4, '天津南', 'tianjinnan', '13:55:00', '13:57:00', '00:02:00', 350.00, NOW(3), NOW(3)),
(3015, 'G2', 5, '北京南', 'beijingnan', '14:28:00', NULL, NULL, 120.00, NOW(3), NOW(3));

-- ========== 途经站 G3 广州南->深圳北 ==========
INSERT INTO `train_station` (`id`, `train_code`, `index`, `name`, `name_pinyin`, `in_time`, `out_time`, `stop_time`, `km`, `create_time`, `update_time`) VALUES
(3021, 'G3', 1, '广州南', 'guangzhounan', NULL, '08:00:00', NULL, 0.00, NOW(3), NOW(3)),
(3022, 'G3', 2, '深圳北', 'shenzhenbei', '08:35:00', NULL, NULL, 140.00, NOW(3), NOW(3));

-- ========== 途经站 D1 杭州东->南京南 ==========
INSERT INTO `train_station` (`id`, `train_code`, `index`, `name`, `name_pinyin`, `in_time`, `out_time`, `stop_time`, `km`, `create_time`, `update_time`) VALUES
(3031, 'D1', 1, '杭州东', 'hangzhoudong', NULL, '07:30:00', NULL, 0.00, NOW(3), NOW(3)),
(3032, 'D1', 2, '南京南', 'nanjingnan', '09:10:00', NULL, NULL, 280.00, NOW(3), NOW(3));

-- ========== 途经站 G101 北京南->广州南 ==========
INSERT INTO `train_station` (`id`, `train_code`, `index`, `name`, `name_pinyin`, `in_time`, `out_time`, `stop_time`, `km`, `create_time`, `update_time`) VALUES
(3041, 'G101', 1, '北京南', 'beijingnan', NULL, '08:00:00', NULL, 0.00, NOW(3), NOW(3)),
(3042, 'G101', 2, '武汉', 'wuhan', '12:00:00', '12:05:00', '00:05:00', 1200.00, NOW(3), NOW(3)),
(3043, 'G101', 3, '广州南', 'guangzhounan', '16:30:00', NULL, NULL, 1000.00, NOW(3), NOW(3));

-- ========== 途经站 G201 上海虹桥->成都东 ==========
INSERT INTO `train_station` (`id`, `train_code`, `index`, `name`, `name_pinyin`, `in_time`, `out_time`, `stop_time`, `km`, `create_time`, `update_time`) VALUES
(3051, 'G201', 1, '上海虹桥', 'shanghaihongqiao', NULL, '10:00:00', NULL, 0.00, NOW(3), NOW(3)),
(3052, 'G201', 2, '武汉', 'wuhan', '13:30:00', '13:35:00', '00:05:00', 800.00, NOW(3), NOW(3)),
(3053, 'G201', 3, '重庆北', 'chongqingbei', '16:30:00', '16:35:00', '00:05:00', 900.00, NOW(3), NOW(3)),
(3054, 'G201', 4, '成都东', 'chengdudong', '18:30:00', NULL, NULL, 350.00, NOW(3), NOW(3));

-- ========== 车厢 G1 ==========
INSERT INTO `train_carriage` (`id`, `train_code`, `index`, `seat_type`, `seat_count`, `row_count`, `col_count`, `create_time`, `update_time`) VALUES
(4001, 'G1', 1, '1', 40, 10, 4, NOW(3), NOW(3)),
(4002, 'G1', 2, '1', 40, 10, 4, NOW(3), NOW(3)),
(4003, 'G1', 3, '2', 75, 15, 5, NOW(3), NOW(3)),
(4004, 'G1', 4, '2', 75, 15, 5, NOW(3), NOW(3)),
(4005, 'G1', 5, '2', 75, 15, 5, NOW(3), NOW(3));

-- ========== 车厢 G2 ==========
INSERT INTO `train_carriage` (`id`, `train_code`, `index`, `seat_type`, `seat_count`, `row_count`, `col_count`, `create_time`, `update_time`) VALUES
(4011, 'G2', 1, '1', 40, 10, 4, NOW(3), NOW(3)),
(4012, 'G2', 2, '1', 40, 10, 4, NOW(3), NOW(3)),
(4013, 'G2', 3, '2', 75, 15, 5, NOW(3), NOW(3)),
(4014, 'G2', 4, '2', 75, 15, 5, NOW(3), NOW(3)),
(4015, 'G2', 5, '2', 75, 15, 5, NOW(3), NOW(3));

-- ========== 车厢 G3 ==========
INSERT INTO `train_carriage` (`id`, `train_code`, `index`, `seat_type`, `seat_count`, `row_count`, `col_count`, `create_time`, `update_time`) VALUES
(4021, 'G3', 1, '1', 32, 8, 4, NOW(3), NOW(3)),
(4022, 'G3', 2, '2', 50, 10, 5, NOW(3), NOW(3)),
(4023, 'G3', 3, '2', 50, 10, 5, NOW(3), NOW(3));

-- ========== 车厢 D1 ==========
INSERT INTO `train_carriage` (`id`, `train_code`, `index`, `seat_type`, `seat_count`, `row_count`, `col_count`, `create_time`, `update_time`) VALUES
(4031, 'D1', 1, '2', 60, 12, 5, NOW(3), NOW(3)),
(4032, 'D1', 2, '2', 60, 12, 5, NOW(3), NOW(3)),
(4033, 'D1', 3, '2', 60, 12, 5, NOW(3), NOW(3));

-- ========== 车厢 G101 ==========
INSERT INTO `train_carriage` (`id`, `train_code`, `index`, `seat_type`, `seat_count`, `row_count`, `col_count`, `create_time`, `update_time`) VALUES
(4041, 'G101', 1, '1', 40, 10, 4, NOW(3), NOW(3)),
(4042, 'G101', 2, '1', 40, 10, 4, NOW(3), NOW(3)),
(4043, 'G101', 3, '2', 75, 15, 5, NOW(3), NOW(3)),
(4044, 'G101', 4, '2', 75, 15, 5, NOW(3), NOW(3)),
(4045, 'G101', 5, '2', 75, 15, 5, NOW(3), NOW(3)),
(4046, 'G101', 6, '2', 75, 15, 5, NOW(3), NOW(3));

-- ========== 车厢 G201 ==========
INSERT INTO `train_carriage` (`id`, `train_code`, `index`, `seat_type`, `seat_count`, `row_count`, `col_count`, `create_time`, `update_time`) VALUES
(4051, 'G201', 1, '1', 40, 10, 4, NOW(3), NOW(3)),
(4052, 'G201', 2, '1', 40, 10, 4, NOW(3), NOW(3)),
(4053, 'G201', 3, '2', 75, 15, 5, NOW(3), NOW(3)),
(4054, 'G201', 4, '2', 75, 15, 5, NOW(3), NOW(3)),
(4055, 'G201', 5, '2', 75, 15, 5, NOW(3), NOW(3));

USE train_member;

-- ========== 会员 ==========
INSERT INTO `member` (`id`, `mobile`) VALUES
(1000000000000000001, '13000000001'),
(1000000000000000002, '13000000002'),
(1000000000000000003, '13000000003'),
(1000000000000000004, '13000000004'),
(1000000000000000005, '13000000005'),
(1000000000000000006, '13000000006'),
(1000000000000000007, '13000000007'),
(1000000000000000008, '13000000008'),
(1000000000000000009, '13000000009'),
(1000000000000000010, '13000000010');

-- ========== 乘车人 ==========
INSERT INTO `passenger` (`id`, `member_id`, `name`, `id_card`, `type`, `create_time`, `update_time`) VALUES
(2000000000000000001, 1000000000000000001, '张三', '110101199001011234', '1', NOW(3), NOW(3)),
(2000000000000000002, 1000000000000000001, '张小明', '110101201501011234', '2', NOW(3), NOW(3)),
(2000000000000000003, 1000000000000000002, '李四', '310101199002022345', '1', NOW(3), NOW(3)),
(2000000000000000004, 1000000000000000002, '李芳', '310101199103033456', '1', NOW(3), NOW(3)),
(2000000000000000005, 1000000000000000003, '王五', '440101199004044567', '1', NOW(3), NOW(3)),
(2000000000000000006, 1000000000000000003, '王小红', '440101201002025678', '2', NOW(3), NOW(3)),
(2000000000000000007, 1000000000000000004, '赵六', '330101199005056789', '1', NOW(3), NOW(3)),
(2000000000000000008, 1000000000000000005, '孙七', '420101199006067890', '1', NOW(3), NOW(3)),
(2000000000000000009, 1000000000000000005, '孙八', '420101199007078901', '3', NOW(3), NOW(3)),
(2000000000000000010, 1000000000000000006, '周九', '510101199008089012', '1', NOW(3), NOW(3)),
(2000000000000000011, 1000000000000000007, '吴十', '500101199009090123', '1', NOW(3), NOW(3)),
(2000000000000000012, 1000000000000000008, '郑十一', '610101199010101234', '1', NOW(3), NOW(3)),
(2000000000000000013, 1000000000000000009, '陈十二', '320101199011111345', '1', NOW(3), NOW(3)),
(2000000000000000014, 1000000000000000010, '林十三', '350101199012121456', '1', NOW(3), NOW(3)),
(2000000000000000015, 1000000000000000010, '林小华', '350101201503031457', '2', NOW(3), NOW(3));
