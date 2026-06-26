USE distribution_db;

SELECT * FROM order_main ORDER BY order_id DESC;
SELECT * FROM payment ORDER BY pay_id DESC;
SELECT * FROM stock WHERE product_id = 1 AND warehouse_id = 1;