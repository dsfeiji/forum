package com.example.forum.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.forum.Dto.UserDto;
import com.example.forum.entity.User;
import com.example.forum.mapper.UserMapper;
import com.example.forum.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.forum.utils.EmailUtils;
import com.example.forum.utils.JwtUtils;
import com.example.forum.utils.Result;
import com.example.forum.vo.UserVo;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-02-18
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtils jwtUtils;

    @Resource
    EmailUtils emailUtils;
    private static final Log log = LogFactory.get();

    @Override
    public Result<String> sendVerificationCode(User user) {
        // 生成验证码

        // 将验证码存储到内存中
        String email = user.getEmail();
        // 发送验证码邮件
        return emailUtils.sendVerificationCode(email);
    }


    @Override
    public Result<String> register(UserDto userDto) {
        if (EmailUtils.validateVerificationCode(userDto.getEmail(), userDto.getVerificationCode())) {
            // 查询数据库中是否已存在该邮箱
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getEmail, userDto.getEmail());
            User existingUser = getOne(queryWrapper);

            // 如果邮箱已存在，返回注册失败
            if (existingUser != null) {
                return Result.error("注册失败，邮箱已存在");
            }

            // 如果邮箱不存在，创建新用户
            User userInfo = new User();
            userInfo.setEmail(userDto.getEmail());
            userInfo.setPassword(userDto.getPassword());
            Random ra = new Random();
            //生成5位随机数
            int randomNumber = 10000 + ra.nextInt(90000);
            String name ="用户"+randomNumber;
            userInfo.setUsername(name);
            userDto.setUsername(name);
            boolean flag = save(userInfo);

            // 返回注册结果
            return flag ? Result.success("注册成功") : Result.error("注册失败");
        } else {
            return Result.error("注册失败，验证码错误");
        }
    }

    @Override
    public Result<UserVo> login(User user) {
        System.out.println(user);
        String email = user.getEmail();
        String password = user.getPassword();
        // 判断是否查询到用户
        if (validateUserCredentials(email, password)) {
            // 获取完整的用户信息
            User completeUser = getUserByEmail(email);
            UserVo userInfoVo = new UserVo();
            // 使用 Hutool 的 BeanUtil.copyProperties 进行对象拷贝
            BeanUtil.copyProperties(completeUser, userInfoVo); // 将完整的用户信息拷贝到 userInfoVo
            log.info("登录成功");
            return Result.success(jwtUtils.generateToken(completeUser.getEmail(), completeUser.getUserId()), userInfoVo);
        }
        log.info("登录失败");
        return Result.error("登录失败，邮箱或密码错误");
    }

    public boolean validateUserCredentials(String email, String password) {
        // 使用 LambdaQueryWrapper 构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, email) // 查询邮箱匹配的记录
                .eq(User::getPassword, password); // 查询密码匹配的记录
        log.info(email,password);
        // 查询数据库
        User users = this.getOne(queryWrapper);
        if (users == null) {
            return false;
        }else {
            return true;
        }
    }
    /**
     * 更新用户信息
     */
    @Override
    public Result<String> updateUserInfo(User user, String token) {
        try {
            // 验证token
            String email = jwtUtils.getEmailFromToken(token);
            if (email == null) {
                return Result.error("Token无效");
            }
            
            // 获取当前登录用户
            User currentUser = getUserByEmail(email);
            if (currentUser == null) {
                return Result.error("用户不存在");
            }
            
            // 验证用户权限（确保只能修改自己的信息）
            if (!currentUser.getUserId().equals(user.getUserId())) {
                return Result.error("无权修改他人信息");
            }
            
            // 创建要更新的用户对象
            User updateUser = new User();
            updateUser.setUserId(user.getUserId());
            
            // 检查是否有要更新的字段
            boolean hasUpdates = false;
            
            // 更新用户名（如果提供了新的用户名）
            if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
                updateUser.setUsername(user.getUsername());
                hasUpdates = true;
            }
            
            // 更新生日（如果提供了新的生日）
            if (user.getBirthday() != null) {
                updateUser.setBirthday(user.getBirthday());
                hasUpdates = true;
            }
            
            // 如果没有要更新的字段，返回错误
            if (!hasUpdates) {
                return Result.error("没有要更新的信息");
            }
            
            // 执行更新操作
            boolean success = updateById(updateUser);
            if (success) {
                return Result.success("修改成功");
            } else {
                return Result.error("修改失败");
            }
        } catch (Exception e) {
            log.error("修改用户信息失败: {}", e.getMessage());
            return Result.error("修改用户信息失败：" + e.getMessage());
        }
    }
    @Override
    public Result<String> user(User user,String token) {
        // 检查用户是否存在
        User existingUser = getById(user.getUserId());
        if (existingUser == null) {
            return Result.error("用户不存在");
        }
        
        // 使用正确的字段名 user_id
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", user.getUserId())
                .set("username", user.getUsername());
                
        // 如果有其他字段需要更新，可以在这里添加
        if (user.getBirthday() != null) {
            updateWrapper.set("birthday", user.getBirthday());
        }
        
        int rows = userMapper.update(null, updateWrapper);
        if (rows > 0) {
            return Result.success("修改成功");
        } else {
            return Result.error("修改失败");
        }
    }
    @Override
    public User getUserByEmail(String email) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, email);
        return baseMapper.selectOne(queryWrapper);
    }
}

