package com.example.login123.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysUserDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;


    private String username;

    private String password;

    private String nickname;

    private String phone;

    private String avatar;

    private String role;

    private String status;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
