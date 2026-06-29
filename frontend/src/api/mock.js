export const mockOrderDashboard = {
  summary: {
    todayOrderCount: 1280,
    todayTradeAmount: 128000,
    pendingDeliveryCount: 23,
    exceptionOrderCount: 5
  },
  orders: [
    {
      orderNo: 'DD202505190001',
      productName: '机械键盘-Keychron K3',
      productNum: 2,
      totalAmount: 736,
      orderStatus: '已支付',
      stockService: 'stock:8003',
      payService: 'pay:8006',
      orderTime: '09:30'
    },
    {
      orderNo: 'DD202505190002',
      productName: '无线鼠标-罗技M720',
      productNum: 5,
      totalAmount: 642,
      orderStatus: '待支付',
      stockService: 'stock:8004',
      payService: '-',
      orderTime: '09:45'
    },
    {
      orderNo: 'DD202505190003',
      productName: 'HDMI高清线-绿联2米',
      productNum: 10,
      totalAmount: 350,
      orderStatus: '已发货',
      stockService: 'stock:8003',
      payService: 'pay:8006',
      orderTime: '10:00'
    },
    {
      orderNo: 'DD202505190004',
      productName: '铝合金笔记本支架',
      productNum: 3,
      totalAmount: 267,
      orderStatus: '库存不足',
      stockService: '扣减失败',
      payService: '-',
      orderTime: '10:15'
    },
    {
      orderNo: 'DD202505190005',
      productName: 'A4打印纸-得力70g',
      productNum: 20,
      totalAmount: 440,
      orderStatus: '已完成',
      stockService: 'stock:8004',
      payService: 'pay:8006',
      orderTime: '08:30'
    }
  ]
}

export const seataFlowMock = {
  steps: [
    { name: '订单服务', desc: 'TM', type: 'service' },
    { name: '@GlobalTransactional', desc: '开启全局事务', type: 'success' },
    { name: '库存服务', desc: 'RM', type: 'service' },
    { name: '扣减库存', desc: 'undo_log', type: 'warning' },
    { name: '支付服务', desc: 'RM', type: 'service' },
    { name: '创建支付单', desc: 'undo_log', type: 'success' }
  ],
  logs: [
    { type: 'success', text: 'DD202505190001：库存扣减成功（2件） → 支付单创建成功 → TC通知RM删除undo_log → 全局事务提交' },
    { type: 'danger', text: 'DD202505190004：库存扣减失败（库存不足） → 抛出异常 → TM通知TC回滚 → TC驱动RM用undo_log恢复数据 → 订单表回滚' }
  ]
}

export const sentinelRulesMock = [
  { resource: '/api/stock/deduct', threshold: 100, qps: 87, blocked: 0, status: '正常', strategy: '快速失败' },
  { resource: '/api/order/create', threshold: 200, qps: 156, blocked: 12, status: '警告', strategy: '匀速排队' },
  { resource: '/api/pay/callback', threshold: 50, qps: 48, blocked: 2, status: '正常', strategy: '快速失败' },
  { resource: '/api/product/list', threshold: 500, qps: 320, blocked: 0, status: '正常', strategy: '直接拒绝' }
]

export const governanceMock = {
  metrics: [
    { title: '微服务数', value: '5', tone: 'success', hint: '全部健康' },
    { title: '服务实例', value: '6', tone: 'primary', hint: '含2个库存实例' },
    { title: 'GATEWAY路由', value: '5', tone: 'warning', hint: '已配置' },
    { title: '熔断降级', value: '0', tone: 'danger', hint: '当前正常' }
  ],
  logs: [
    '[10:00:15] [INFO] Nacos Server started successfully in 3.2s',
    '[10:01:22] [INFO] Service registered: user-center 192.168.1.101:8001',
    '[10:01:25] [INFO] Service registered: product-center 192.168.1.101:8002',
    '[10:01:28] [INFO] Service registered: stock-center 192.168.1.101:8003',
    '[10:01:30] [WARN] Service registered: stock-center 192.168.1.101:8004 [集群实例2]',
    '[10:01:32] [INFO] Service registered: order-center 192.168.1.101:8005',
    '[10:01:35] [INFO] Service registered: pay-center 192.168.1.101:8006',
    '[10:02:10] [INFO] Service registered: gateway-service 192.168.1.101:9000',
    '[10:05:00] [WARN] Heartbeat check: all services healthy'
  ],
  instances: [
    { name: 'user-center', desc: '用户中心 · 8001 · 1实例', latency: '12ms' },
    { name: 'product-center', desc: '商品中心 · 8002 · 1实例', latency: '8ms' },
    { name: 'stock-center', desc: '库存中心 · 8003/8004 · 2实例', latency: '15ms' },
    { name: 'order-center', desc: '订单中心 · 8005 · 1实例', latency: '22ms' },
    { name: 'pay-center', desc: '支付中心 · 8006 · 1实例', latency: '18ms' },
    { name: 'gateway-service', desc: '网关服务 · 9000 · 统一入口', latency: '5ms' }
  ],
  routes: [
    'spring.cloud.gateway.routes[0].id=user_route',
    'spring.cloud.gateway.routes[0].uri=lb://user-center',
    'spring.cloud.gateway.routes[0].predicates[0]=Path=/api/user/**',
    'spring.cloud.gateway.routes[1].id=product_route',
    'spring.cloud.gateway.routes[1].uri=lb://product-center',
    'spring.cloud.gateway.routes[1].predicates[0]=Path=/api/product/**',
    'spring.cloud.gateway.routes[2].id=stock_route',
    'spring.cloud.gateway.routes[2].uri=lb://stock-center',
    'spring.cloud.gateway.routes[2].predicates[0]=Path=/api/stock/**',
    'spring.cloud.gateway.routes[3].id=order_route',
    'spring.cloud.gateway.routes[3].uri=lb://order-center',
    'spring.cloud.gateway.routes[3].predicates[0]=Path=/api/order/**',
    'spring.cloud.gateway.routes[4].id=pay_route',
    'spring.cloud.gateway.routes[4].uri=lb://pay-center',
    'spring.cloud.gateway.routes[4].predicates[0]=Path=/api/pay/**'
  ]
}

export const dashboardMock = {
  serviceCalls: [
    { name: 'Gateway', value: 820 },
    { name: 'Order', value: 650 },
    { name: 'Stock', value: 940 },
    { name: 'Product', value: 520 },
    { name: 'Pay', value: 350 },
    { name: 'User', value: 300 }
  ],
  status: [
    { label: '已支付', percent: 35, color: '#5b8def' },
    { label: '已完成', percent: 25, color: '#73bf69' },
    { label: '待支付', percent: 20, color: '#f1c40f' },
    { label: '异常', percent: 20, color: '#f0445e' }
  ],
  trend: [
    { date: '5/13', value: 420 },
    { date: '5/14', value: 580 },
    { date: '5/15', value: 480 },
    { date: '5/16', value: 740 },
    { date: '5/17', value: 630 },
    { date: '5/18', value: 900 },
    { date: '5/19', value: 790 }
  ]
}

export const productManagementMock = [
  { code: 'P202505190001', name: '机械键盘-Keychron K3', category: '办公外设', price: 368, status: '上架', stock: 48 },
  { code: 'P202505190002', name: '无线鼠标-罗技M720', category: '办公外设', price: 128.4, status: '上架', stock: 8 },
  { code: 'P202505190003', name: 'HDMI高清线-绿联2米', category: '数码配件', price: 35, status: '上架', stock: 120 },
  { code: 'P202505190004', name: '铝合金笔记本支架', category: '办公配件', price: 89, status: '缺货', stock: 0 },
  { code: 'P202505190005', name: 'A4打印纸-得力70g', category: '办公耗材', price: 22, status: '上架', stock: 300 }
]

export const stockQueryMock = [
  { warehouse: '北京仓', product: '机械键盘-Keychron K3', available: 48, locked: 2, service: 'stock:8003', status: '正常' },
  { warehouse: '上海仓', product: '无线鼠标-罗技M720', available: 8, locked: 5, service: 'stock:8004', status: '预警' },
  { warehouse: '广州仓', product: 'HDMI高清线-绿联2米', available: 120, locked: 10, service: 'stock:8003', status: '正常' },
  { warehouse: '北京仓', product: '铝合金笔记本支架', available: 0, locked: 0, service: 'stock:8004', status: '缺货' }
]

export const aiMock = {
  messages: [
    { role: 'user', content: '我的订单DD202505190001到哪了？' },
    {
      role: 'ai',
      content: '订单号：DD202505190001\n商品：机械键盘-Keychron K3 × 2\n金额：¥736.00\n物流状态：已发货\n当前位置：北京市顺义区集散中心\n预计送达：2026-05-20 14:00前'
    },
    { role: 'user', content: '从北京仓发货到上海浦东要多久？' },
    { role: 'ai', content: '通常需要 1-2 天，若遇到高峰期可能延迟。' }
  ],
  recommendations: [
    { title: '搭配推荐', content: '您浏览过机械键盘，推荐搭配Keychron K3专用掌托。' },
    { title: '库存预警', content: '无线鼠标-罗技M720库存仅剩8件，建议及时补货。' },
    { title: '热销趋势', content: 'HDMI高清线近7天销量增长120%，建议增加采购量至200条。' }
  ]
}

export const monitorMock = {
  cards: [
    { title: 'SPRINGBOOT 版本', value: '3.2.12', tone: 'primary' },
    { title: 'SPRINGCLOUD ALIBABA', value: '2023.0.1.2', tone: 'success' },
    { title: 'JDK 版本', value: '21 LTS', tone: 'primary' },
    { title: 'NACOS 服务数', value: '6', tone: 'success' },
    { title: 'SEATA 事务模式', value: 'AT模式', tone: 'warning' },
    { title: 'SENTINEL限流规则', value: '4条', tone: 'primary' }
  ],
  rows: [
    ['SpringBoot版本', '3.2.12', '基于 Spring Framework 6.1'],
    ['SpringCloud Alibaba版本', '2023.0.1.2', 'Nacos + Gateway + OpenFeign + Sentinel + Seata'],
    ['JDK版本', '21', 'LTS长期支持版本'],
    ['Vue版本', '3.5.x', 'Composition API + Vite构建'],
    ['Nacos服务数', '6', '5个业务服务 + 1个Gateway'],
    ['库存服务实例', '2', '端口8003、8004，轮询负载均衡'],
    ['Seata事务模式', 'AT模式', 'undo_log 自动回滚'],
    ['Sentinel限流规则', '4条', '库存扣减、订单创建等规则']
  ],
  knowledge: [
    'DDD服务拆分：按业务领域拆分微服务边界',
    'Nacos注册发现：服务注册、实例心跳、配置推送',
    'OpenFeign+Ribbon：声明式远程调用与负载均衡',
    'Gateway网关：统一鉴权、路由转发、限流入口',
    'Seata AT分布式事务：undo_log 回滚机制',
    'Sentinel限流降级：QPS限流、熔断策略',
    'DeepSeek AI接入：Prompt工程与业务问答'
  ]
}
