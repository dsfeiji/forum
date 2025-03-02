package com.example.forum.controller;


import com.example.forum.Dto.UserDto;
import com.example.forum.entity.User;
import com.example.forum.service.IUserService;
import com.example.forum.utils.Result;
import com.example.forum.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author DSfeiji
 * @since 2025-02-18
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    IUserService userService;
    @PostMapping("/sendVerificationCode")
    public Result<String> sendVerificationCode(@RequestBody User user) {
        System.out.println(user);
        String msg = userService.sendVerificationCode(user);
        return  Result.success(msg);
    }

    @PostMapping("/register")
    public Result<String> register (@RequestBody UserDto userDto) {
        return userService.register(userDto);
    }

    @PostMapping("/login")
    public Result<UserVo> login(@RequestBody User user) {
        return userService.login(user);
    }

    @PostMapping("/user")
    public Result<String> user(@RequestBody User user) {
        return userService.user(user);
    }

}
