package com.example.forum.controller;


import com.example.forum.Dto.UserDto;
import com.example.forum.entity.User;
import com.example.forum.service.IUserService;
import com.example.forum.utils.JwtUtils;
import com.example.forum.utils.Result;
import com.example.forum.vo.UserVo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 *  前端控制器
 * </p>
 *1
 * @author DSfeiji
 * @since 2025-02-18
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    IUserService userService;
    @Autowired
    JwtUtils jwtUtils;
    
    @PostMapping("/sendVerificationCode")
    public Result<String> sendVerificationCode(@RequestBody User user) {
        System.out.println(user);
        return userService.sendVerificationCode(user);
    }

    @PostMapping("/register")
    public Result<String> register (@RequestBody UserDto userDto) {
        return userService.register(userDto);
    }

    @PostMapping("/login")
    public Result<UserVo> login(@RequestBody User user) {
        return userService.login(user);
    }

    /**
     * 修改用户信息
     */
    @PostMapping("/updateUser")
    public Result<String> updateUser(@RequestBody User user, HttpServletRequest request) {
        try {
            // 从请求头获取 Token
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return Result.error("未登录");
            }
            token = token.substring(7);

            // 从 Token 获取邮箱
            String email = jwtUtils.getEmailFromToken(token);
            if (email == null) {
                return Result.error("Token无效");
            }

            // 通过邮箱查询用户
            User currentUser = userService.getUserByEmail(email);
            if (currentUser == null) {
                return Result.error("用户不存在");
            }

            // 设置用户ID，确保修改的是当前登录用户的信息
            user.setUserId(currentUser.getUserId());
            
            // 调用服务层方法更新用户信息
            return userService.updateUserInfo(user, token);
        } catch (Exception e) {
            log.error("修改用户信息失败: {}", e.getMessage());
            return Result.error("修改失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<User> getUserInfo(HttpServletRequest request) {
        try {
            // 从请求头获取 Token
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return Result.error("未登录");
            }
            token = token.substring(7);

            // 从 Token 获取邮箱
            String email = jwtUtils.getEmailFromToken(token);
            if (email == null) {
                return Result.error("Token无效");
            }

            // 通过邮箱查询用户
            User user = userService.getUserByEmail(email);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 出于安全考虑，清除密码信息
            user.setPassword(null);
            
            return Result.success("获取成功", user);
        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage());
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }
}
