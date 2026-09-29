use sme_mall;

# 商品主表
CREATE TABLE product
(
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',

    product_no VARCHAR(64) NOT NULL COMMENT '商品编号',

    name VARCHAR(128) NOT NULL COMMENT '商品名称',

    category_id BIGINT NOT NULL COMMENT '商品分类ID',

    brand_id BIGINT COMMENT '品牌ID',

    description TEXT COMMENT '商品详情',

    cover_url VARCHAR(512) COMMENT '商品主图',

    status TINYINT NOT NULL DEFAULT 0 COMMENT '
    商品状态:
    0 草稿
    1 上架
    2 下架
    ',

    create_time DATETIME NOT NULL,

    update_time DATETIME NOT NULL

)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


# 商品sku表
# 商品表 SPU 1 —— N SKU表
# SKU 表存“这个商品有哪些具体规格可卖”
# 比如颜色、尺码、容量、价格、库存、条码、规格图。
CREATE TABLE product_sku
(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    product_id BIGINT NOT NULL COMMENT '商品ID',

    sku_code VARCHAR(64) NOT NULL COMMENT 'SKU编码',

    sku_name VARCHAR(128) COMMENT 'SKU名称',

    spec_json JSON COMMENT '规格',

    sale_price DECIMAL(10,2)
        COMMENT '销售价格',

    stock INT DEFAULT 0
        COMMENT '库存',

    status TINYINT DEFAULT 1,

    create_time DATETIME,

    update_time DATETIME,


    UNIQUE KEY uk_sku_code(sku_code)

)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*
商品表：
id=1，name=某品牌T恤

SKU表：
sku_id=101，product_id=1，规格=红色 M，价格=59，库存=10
sku_id=102，product_id=1，规格=红色 L，价格=59，库存=5
sku_id=103，product_id=1，规格=蓝色 M，价格=62，库存=0
*/

# 商品图片表
CREATE TABLE product_image
(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    product_id BIGINT NOT NULL,

    url VARCHAR(512)
        COMMENT '图片地址',

    sort INT DEFAULT 0,

    type TINYINT DEFAULT 1
        COMMENT '
    1 主图
    2 详情图
    ',

    create_time DATETIME

)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



