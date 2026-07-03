-- 修复 Docker MySQL 初始化时未按 utf8mb4 读取 SQL 导致的中文乱码。
-- 典型现象：机械键盘 变成 æœºæ¢°é”®ç›˜。
-- 本脚本只转换包含常见 mojibake 字符的文本字段，已经正常的中文不会被重复转换。

SET NAMES utf8mb4;
SET @mojibake_pattern = 'æ|å|ç|é|è|ä|œ|¼|¢|ƒ|…|™|”|“|—|š|ž|±|¥|‰|¤';

UPDATE user_db.sys_user
SET nickname = CASE WHEN nickname REGEXP @mojibake_pattern THEN CONVERT(BINARY CONVERT(nickname USING latin1) USING utf8mb4) ELSE nickname END,
    role = CASE WHEN role REGEXP @mojibake_pattern THEN CONVERT(BINARY CONVERT(role USING latin1) USING utf8mb4) ELSE role END;

UPDATE auth_db.auth_account
SET nickname = CASE WHEN nickname REGEXP @mojibake_pattern THEN CONVERT(BINARY CONVERT(nickname USING latin1) USING utf8mb4) ELSE nickname END,
    role = CASE WHEN role REGEXP @mojibake_pattern THEN CONVERT(BINARY CONVERT(role USING latin1) USING utf8mb4) ELSE role END;

UPDATE product_db.product
SET product_name = CASE WHEN product_name REGEXP @mojibake_pattern THEN CONVERT(BINARY CONVERT(product_name USING latin1) USING utf8mb4) ELSE product_name END,
    category = CASE WHEN category REGEXP @mojibake_pattern THEN CONVERT(BINARY CONVERT(category USING latin1) USING utf8mb4) ELSE category END,
    description = CASE WHEN description REGEXP @mojibake_pattern THEN CONVERT(BINARY CONVERT(description USING latin1) USING utf8mb4) ELSE description END;

UPDATE stock_db.warehouse
SET warehouse_name = CASE WHEN warehouse_name REGEXP @mojibake_pattern THEN CONVERT(BINARY CONVERT(warehouse_name USING latin1) USING utf8mb4) ELSE warehouse_name END,
    warehouse_address = CASE WHEN warehouse_address REGEXP @mojibake_pattern THEN CONVERT(BINARY CONVERT(warehouse_address USING latin1) USING utf8mb4) ELSE warehouse_address END;

UPDATE order_db.order_main
SET product_name = CASE WHEN product_name REGEXP @mojibake_pattern THEN CONVERT(BINARY CONVERT(product_name USING latin1) USING utf8mb4) ELSE product_name END,
    order_status = CASE WHEN order_status REGEXP @mojibake_pattern THEN CONVERT(BINARY CONVERT(order_status USING latin1) USING utf8mb4) ELSE order_status END,
    receiver_name = CASE WHEN receiver_name REGEXP @mojibake_pattern THEN CONVERT(BINARY CONVERT(receiver_name USING latin1) USING utf8mb4) ELSE receiver_name END,
    receiver_address = CASE WHEN receiver_address REGEXP @mojibake_pattern THEN CONVERT(BINARY CONVERT(receiver_address USING latin1) USING utf8mb4) ELSE receiver_address END,
    logistics_company = CASE WHEN logistics_company REGEXP @mojibake_pattern THEN CONVERT(BINARY CONVERT(logistics_company USING latin1) USING utf8mb4) ELSE logistics_company END,
    logistics_status = CASE WHEN logistics_status REGEXP @mojibake_pattern THEN CONVERT(BINARY CONVERT(logistics_status USING latin1) USING utf8mb4) ELSE logistics_status END,
    current_location = CASE WHEN current_location REGEXP @mojibake_pattern THEN CONVERT(BINARY CONVERT(current_location USING latin1) USING utf8mb4) ELSE current_location END;

UPDATE order_db.order_item
SET product_name = CASE WHEN product_name REGEXP @mojibake_pattern THEN CONVERT(BINARY CONVERT(product_name USING latin1) USING utf8mb4) ELSE product_name END;

UPDATE order_db.ai_chat_record
SET question = CASE WHEN question REGEXP @mojibake_pattern THEN CONVERT(BINARY CONVERT(question USING latin1) USING utf8mb4) ELSE question END,
    answer = CASE WHEN answer REGEXP @mojibake_pattern THEN CONVERT(BINARY CONVERT(answer USING latin1) USING utf8mb4) ELSE answer END;

UPDATE order_db.seata_transaction_record
SET transaction_status = CASE WHEN transaction_status REGEXP @mojibake_pattern THEN CONVERT(BINARY CONVERT(transaction_status USING latin1) USING utf8mb4) ELSE transaction_status END,
    stock_branch_status = CASE WHEN stock_branch_status REGEXP @mojibake_pattern THEN CONVERT(BINARY CONVERT(stock_branch_status USING latin1) USING utf8mb4) ELSE stock_branch_status END,
    pay_branch_status = CASE WHEN pay_branch_status REGEXP @mojibake_pattern THEN CONVERT(BINARY CONVERT(pay_branch_status USING latin1) USING utf8mb4) ELSE pay_branch_status END,
    failure_reason = CASE WHEN failure_reason REGEXP @mojibake_pattern THEN CONVERT(BINARY CONVERT(failure_reason USING latin1) USING utf8mb4) ELSE failure_reason END;

UPDATE pay_db.payment
SET pay_method = CASE WHEN pay_method REGEXP @mojibake_pattern THEN CONVERT(BINARY CONVERT(pay_method USING latin1) USING utf8mb4) ELSE pay_method END,
    pay_status = CASE WHEN pay_status REGEXP @mojibake_pattern THEN CONVERT(BINARY CONVERT(pay_status USING latin1) USING utf8mb4) ELSE pay_status END,
    callback_content = CASE WHEN callback_content REGEXP @mojibake_pattern THEN CONVERT(BINARY CONVERT(callback_content USING latin1) USING utf8mb4) ELSE callback_content END;
