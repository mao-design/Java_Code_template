package com.example.login.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.login.model.dto.RegisterDTO;
import com.example.login.model.entity.SysUserDO;
import com.example.login.model.vo.LoginTokenVO;

public interface UserService extends IService<SysUserDO> {
    LoginTokenVO login(String username, String password);

    void register(RegisterDTO registerDTO);

    LoginTokenVO refresh(String oldRefreshToken);

    void logout(String refreshToken);
}
