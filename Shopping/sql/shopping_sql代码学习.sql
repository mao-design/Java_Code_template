create database if not exists sme_mall
    default character set utf8mb4
    collate utf8mb4_unicode_ci;

use sme_mall;

# 商品
create table product_spu
(
    id          bigint       not null auto_increment comment '数据库主键',
    spu_id      bigint       not null comment 'spu业务id，雪花算法生成',
    name        varchar(200) not null comment '商品名称',
    subtitle    varchar(500) comment '商品副标题',
    status      tinyint      not null default 1 comment '1上架 0下架',
    create_time datetime     not null default
                                          current_timestamp,
    update_time datetime     not null default
                                          current_timestamp
        on update current_timestamp,
    primary key (id),
#     UNIQUE KEY 既是索引，也强制数据唯一性约束，KEY 仅用于加速查询（索引）
    unique key uk_spu_id (spu_id),
    key idx_status_id (status, id)

) engine = InnoDB
  default charset = utf8mb4
    comment = '商品spu表';

# SPU 是“商品是什么”，SKU 是“这个商品具体买哪一种规格”

# 商品对应的规格，比如大小，颜色，型号
create table product_sku
(
    id          bigint         not null auto_increment comment '数据库主键',
    sku_id      bigint         not null comment 'sku业务id，雪花算法生成',
    spu_id      bigint         not null comment 'spu业务id',
    sku_name    varchar(200)   not null comment 'sku名称',
#     decimal(10, 2)表示数字一共可以存储 10 位数字，其中小数点后面保留 2 位
    price       decimal(10, 2) not null comment '价格',
    stock       int            not null default 0 comment '库存',
    status      tinyint        not null default 1 comment '1上架 0下架',
    version     int            not null default 0 comment '版本',
    create_time datetime       not null default current_timestamp,
    update_time datetime       not null default current_timestamp
        on update current_timestamp,
    primary key (id),
    unique key uk_sku_id (sku_id),
    key idx_spu_status (spu_id, status)
) engine = InnoDB
  default charset = utf8mb4
    comment = '商品sku表';

# 订单
create table orders
(
    id                bigint         not null auto_increment comment '数据库主键',
    orders_id         bigint         not null comment '订单业务id，雪花算法',
    user_id           bigint         not null comment '用户业务id',
    total_amout       decimal(12, 2) not null comment '总金额',
    pay_amout         decimal(12, 2) not null comment '支付金额',
    status            varchar(32)    not null comment '状态',
    receiver_name     varchar(64)    not null comment '买家名称',
    receiver_phone    varchar(32)    not null comment '买家号码',
    receiver_address  varchar(500)   not null comment '买家地址',
    payment_id        bigint                  default null comment '业务id',
    pain_time         datetime                default null,
    logistics_company varchar(100)            default null comment '物流公司',
    tracking_no       varchar(100)            default null comment '物流单号',
    shippe_time       datetime                default null comment '发货时间',
    complete_time     datetime                default null comment '完成时间',
    cancelle_time     datetime                default null comment '取消时间',
    create_time       datetime       not null default current_timestamp,
    update_time       datetime       not null default current_timestamp
        on update current_timestamp,
    primary key (id),
    unique key uk_order_id (orders_id),
    key idx_user_created (
                          user_id,
                          status,
                          create_time
        ),
    key idx_status_create (
                           status,
                           create_time
        )
) engine = InnoDB
  default charset = utf8mb4
    comment = '订单主表';

# 订单对应的商品信息
create table order_item
(
    id            bigint         not null auto_increment comment '数据库主键',
    order_item_id bigint         not null comment '订单明细业务id',
    order_id      bigint         not null comment '订单业务id',
    sku_id        bigint         not null comment 'sku业务id',
    product_name  varchar(200)   not null comment '商品名称快照',
    sku_name      varchar(200)   not null comment 'sku名称快照',
    unit_price    decimal(10, 2) not null comment '成交单价',
    quantity      int            not null comment '购买数量',
    subtotal      decimal(12, 2) not null comment '小计',
    create_time   datetime       not null default current_timestamp,
    primary key (id),
    unique key uk_order_item_id (
                                 order_item_id
        ),
    key id_order_id (
                     order_id
        ),
    key idx_sku_id (
                    sku_id
        )

) engine = InnoDB
  default charset = utf8mb4
    comment = '订单明细表';

# 支付记录
#
create table payment_record
(
    id               bigint         not null auto_increment comment '数据库主键',
    payment_id       bigint         not null comment '支付业务id，雪花算法',
    order_id         bigint         not null comment '订单业务id',
    third_payment_no varchar(100)            default null comment '第三方支付流水号',
    channel          varchar(32)    not null comment 'ALIPAY、WECHAT、MOCK',
    amount           decimal(12, 3) not null comment '数量',
    status           varchar(32)    not null,
    create_time      datetime       not null default current_timestamp,
    update_time      datetime       not null default current_timestamp
        on update current_timestamp,
    primary key (id),
    unique key uk_payment_id (
                              payment_id
        ),
    unique key uk_third_payment_no (
                                    third_payment_no
        ),
    key idx_order_id (
                      order_id
        ),
    key idx_stauts_create (
                           status,
                           create_time
        )
) engine = InnoDB
  default charset = utf8mb4
    comment = '支付流水表';

# 售后服务
create table after_sale
(
    id            bigint         not null auto_increment comment '数据库主键',
    after_sale_id bigint         not null comment '售后业务id',
    order_id      bigint         not null comment '订单业务id',
    user_id       bigint         not null comment '用户业务id',
    type          varchar(32)    not null comment '类型',
    reason        varchar(500)   not null comment '原因',
    refund_amount decimal(12, 2) not null comment '退款金额',
    status        varchar(32)    not null,
    create_time   datetime       not null default current_timestamp,
    update_time   datetime       not null default current_timestamp
        on update current_timestamp,
    primary key (id),
    unique key un_after_sale_id (
                                 after_sale_id
        ),
    key idx_order_id (
                      order_id
        ),
    key idx_user_create (
                         user_id,
                         create_time
        ),
    key idx_user_status_create (
                                user_id,
                                status,
                                create_time
        ),
    key idx_status_create (
                           status,
                           create_time
        )
) engine = InnoDB
  default charset = utf8mb4
    comment = '售后表';

/*
表结构关系
SPU
 ↓
SKU
 ↓
订单明细
 ↑
订单
 ├── 支付
 │    └── 退款
 │
 ├── 售后
 │    └── 退款
 │
 └── 操作日志
*/



