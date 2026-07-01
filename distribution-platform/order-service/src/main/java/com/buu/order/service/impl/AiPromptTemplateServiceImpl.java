package com.buu.order.service.impl;

import com.buu.order.dto.AiPromptTemplateResponse;
import com.buu.order.dto.RemoteProductDTO;
import com.buu.order.dto.RemoteStockDTO;
import com.buu.order.entity.OrderMain;
import com.buu.order.service.AiPromptTemplateService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * AI 客服 Prompt 模板服务实现
 * 模板内容严格基于当前已实现的 Controller 接口和实体字段。
 */
@Service
public class AiPromptTemplateServiceImpl implements AiPromptTemplateService {

    private static final String USER_MESSAGE_PLACEHOLDER = "{{用户输入内容}}";

    @Override
    public AiPromptTemplateResponse getPromptTemplate() {
        return new AiPromptTemplateResponse(
                buildIntentDefinitions(),
                buildIntentRecognitionTemplate(),
                buildGeneralCustomerPrompt("{userMessage}"),
                buildOrderLogisticsPrompt("{userMessage}", sampleOrder()),
                buildStockPrompt("{userMessage}", sampleProduct(), List.of(sampleStock()))
        );
    }

    @Override
    public String buildIntentRecognitionPrompt(String userMessage) {
        return buildIntentRecognitionTemplate().replace(USER_MESSAGE_PLACEHOLDER, nullToEmpty(userMessage));
    }

    @Override
    public String buildOrderLogisticsPrompt(String userMessage, OrderMain order) {
        return """
                【角色人设】
                你是优选电商的官方在线客服，称呼用户为「亲亲」，语气亲切、耐心、专业。

                【业务边界】
                仅基于下方订单与物流真实数据回复用户，禁止编造物流节点、赔付承诺和平台未提供的信息。

                【回复要求】
                1. 回复控制在150字以内
                2. 必须说明订单号、订单状态、物流状态、当前位置、预计送达时间
                3. 如果某个字段为空，用“暂未同步”表达，不要自行补全

                【订单与物流真实数据】
                订单号：%s
                商品：%s × %s
                订单状态：%s
                物流公司：%s
                物流单号：%s
                物流状态：%s
                当前地点：%s
                预计送达：%s

                【用户当前问题】
                %s
                """.formatted(
                nullToEmpty(order.getOrderNo()),
                nullToEmpty(order.getProductName()),
                value(order.getProductNum()),
                nullToEmpty(order.getOrderStatus()),
                nullToEmpty(order.getLogisticsCompany()),
                nullToEmpty(order.getLogisticsNo()),
                nullToEmpty(order.getLogisticsStatus()),
                nullToEmpty(order.getCurrentLocation()),
                value(order.getExpectArriveTime()),
                nullToEmpty(userMessage)
        );
    }

    @Override
    public String buildStockPrompt(String userMessage, RemoteProductDTO product, List<RemoteStockDTO> stocks) {
        String stockLines = stocks.stream()
                .map(stock -> "仓库ID：" + value(stock.getWarehouseId()) + "，可用库存：" + value(stock.getStockNum()))
                .collect(Collectors.joining("\n"));
        return """
                【角色人设】
                你是优选电商的官方在线客服，称呼用户为「亲亲」。

                【业务边界】
                仅基于下方商品和库存真实数据回复，禁止编造库存、到货时间和优惠活动。

                【回复要求】
                1. 回复控制在120字以内
                2. 库存不足时提醒用户尽快下单或关注补货
                3. 库存为0时明确说明当前缺货

                【商品与库存真实数据】
                商品ID：%s
                商品名称：%s
                商品分类：%s
                商品价格：%s
                库存明细：
                %s

                【用户当前问题】
                %s
                """.formatted(
                value(product.getProductId()),
                nullToEmpty(product.getProductName()),
                nullToEmpty(product.getCategory()),
                value(product.getPrice()),
                stockLines.isBlank() ? "暂未查询到库存记录" : stockLines,
                nullToEmpty(userMessage)
        );
    }

    @Override
    public String buildGeneralCustomerPrompt(String userMessage) {
        return """
                【角色人设】
                你是优选电商的官方在线客服，称呼用户为「亲亲」，语气亲切、耐心、专业，使用生活化口语表达。

                【业务范围】
                仅可解答订单物流、商品咨询、库存查询、活动规则四类问题。
                超出以上范围的问题，统一回复：「抱歉亲亲，我只能解答购物相关的问题哦~」

                【回复要求】
                1. 回复简洁易懂，控制在150字以内
                2. 涉及步骤时用数字序号分点说明
                3. 禁止编造平台规则、优惠活动、物流信息
                4. 禁止与用户争吵、禁止使用不礼貌用语

                【用户当前问题】
                %s
                """.formatted(nullToEmpty(userMessage));
    }

    private String buildIntentRecognitionTemplate() {
        return """
                你是电商系统的需求分析师，严格基于下方提供的【微服务清单】【核心建表字段】【Controller层用户侧接口文档】，生成C端AI客服的语义提取结果。

                【硬性约束】
                1. 只基于提供的材料推导，严禁新增任何文档里没有的接口、字段和业务能力
                2. 一个用户侧查询接口对应一个核心意图，意图命名和接口功能对齐
                3. 槽位字段名必须和Controller接口的入参名称完全一致，必填/可选属性和接口保持统一
                4. 剔除鉴权类公共参数（如token、userId），只保留用户对话中会提到的业务参数
                5. 必须补充other兜底意图，覆盖所有接口外的无关问题

                【可识别意图列表】
                1. order_logistics：订单物流查询，对应接口 /api/order/remote-detail/{orderNo}，必填槽位 orderNo
                2. goods_stock：商品库存查询，对应接口 /api/stock/product/{productId}，必填槽位 productId
                3. product_consult：商品详情咨询，对应接口 /api/product/{productId}，必填槽位 productId
                4. other：无法识别或超出当前 Controller 能力范围的问题

                【核心建表字段】
                order_main：order_no、product_name、product_num、order_status、logistics_company、logistics_no、logistics_status、current_location、expect_arrive_time
                product：product_id、product_name、category、price、sku_code、description、status、safe_stock
                stock：product_id、warehouse_id、stock_num

                【Controller层用户侧接口文档】
                接口名称：订单跨服务详情查询
                接口路径：/api/order/remote-detail/{orderNo}
                请求方式：GET
                入参列表：
                - orderNo：String，必填，订单编号

                接口名称：商品详情查询
                接口路径：/api/product/{productId}
                请求方式：GET
                入参列表：
                - productId：Long，必填，商品ID

                接口名称：商品库存查询
                接口路径：/api/stock/product/{productId}
                请求方式：GET
                入参列表：
                - productId：Long，必填，商品ID

                【输出格式】
                严格输出纯JSON，无任何多余字符，字段定义如下：
                {
                  "intent": "order_logistics/goods_stock/product_consult/other",
                  "slots": {
                    "orderNo": "提取到的订单号，未提取到则为null",
                    "productId": "提取到的商品ID，未提取到则为null"
                  },
                  "is_complete": true,
                  "reply_tip": "参数完整为null；缺失则返回亲切的追问话术；无关问题返回「抱歉亲亲，我只能解答购物相关的问题哦~」"
                }

                用户提问：{{用户输入内容}}
                """;
    }

    private List<AiPromptTemplateResponse.IntentDefinition> buildIntentDefinitions() {
        return List.of(
                new AiPromptTemplateResponse.IntentDefinition(
                        "order_logistics",
                        "订单物流查询",
                        "/api/order/remote-detail/{orderNo}",
                        "orderNo",
                        "用户询问订单发货状态、物流轨迹、当前位置或预计送达时间"
                ),
                new AiPromptTemplateResponse.IntentDefinition(
                        "goods_stock",
                        "商品库存查询",
                        "/api/stock/product/{productId}",
                        "productId",
                        "用户询问某个商品是否有货或库存数量"
                ),
                new AiPromptTemplateResponse.IntentDefinition(
                        "product_consult",
                        "商品详情咨询",
                        "/api/product/{productId}",
                        "productId",
                        "用户询问某个商品价格、分类、规格或描述"
                ),
                new AiPromptTemplateResponse.IntentDefinition(
                        "other",
                        "兜底意图",
                        "无对应业务接口",
                        "无",
                        "当前 Controller 能力范围外的问题"
                )
        );
    }

    private OrderMain sampleOrder() {
        OrderMain order = new OrderMain();
        order.setOrderNo("{orderNo}");
        order.setProductName("{productName}");
        order.setProductNum(1);
        order.setOrderStatus("{orderStatus}");
        order.setLogisticsCompany("{logisticsCompany}");
        order.setLogisticsNo("{logisticsNo}");
        order.setLogisticsStatus("{logisticsStatus}");
        order.setCurrentLocation("{currentLocation}");
        return order;
    }

    private RemoteProductDTO sampleProduct() {
        RemoteProductDTO product = new RemoteProductDTO();
        product.setProductId(1L);
        product.setProductName("{productName}");
        product.setCategory("{category}");
        return product;
    }

    private RemoteStockDTO sampleStock() {
        RemoteStockDTO stock = new RemoteStockDTO();
        stock.setWarehouseId(1L);
        stock.setStockNum(50);
        return stock;
    }

    private String nullToEmpty(String value) {
        return Objects.toString(value, "暂未同步");
    }

    private String value(Object value) {
        return Objects.toString(value, "暂未同步");
    }
}
