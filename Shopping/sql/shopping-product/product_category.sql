use sme_mall;

CREATE TABLE product_category
(
    id          BIGINT PRIMARY KEY COMMENT '分类ID',

    parent_id   BIGINT  DEFAULT 0 COMMENT '父分类ID',

    name        VARCHAR(64) NOT NULL COMMENT '分类名称',

    level       INT         NOT NULL COMMENT '分类层级',

    sort        INT     DEFAULT 0 COMMENT '排序',

    status      TINYINT DEFAULT 1 COMMENT '状态 1启用 0禁用',

    icon        VARCHAR(255) COMMENT '分类图标',

    create_time DATETIME    NOT NULL COMMENT '创建时间',

    update_time DATETIME    NOT NULL COMMENT '更新时间',

    deleted     TINYINT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT '商品分类表';
