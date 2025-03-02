package com.example.forum.service;

import cn.hutool.system.UserInfo;
import com.example.forum.Dto.UserDto;
import com.example.forum.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.forum.utils.Result;
import com.example.forum.vo.UserVo;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-02-18
 */
public interface IUserService extends IService<User> {
    String sendVerificationCode(User user);
    Result<String> register(UserDto userDto);
    Result<UserVo> login(User user);
    Result<String> user(User user);
    boolean validateUserCredentials(String email, String password);
    User getUserByEmail(String email);
}
