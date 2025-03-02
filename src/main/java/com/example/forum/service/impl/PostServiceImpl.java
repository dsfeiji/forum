package com.example.forum.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.forum.Dto.PostDto;
import com.example.forum.entity.Post;
import com.example.forum.entity.User;
import com.example.forum.mapper.PostMapper;
import com.example.forum.mapper.UserMapper;
import com.example.forum.service.IPostService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.forum.utils.JwtUtils;
import com.example.forum.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import io.jsonwebtoken.Claims;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-02-18
 */
@Service
@Slf4j
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements IPostService {
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PostMapper postMapper;

    @Override
    public Result<String> release(PostDto postDto, String token) {
        if (validatePostDto(postDto)) {
            return Result.error("请求参数不完整");
        }
        
        try {
            // 从 Token 获取邮箱
            String email = jwtUtils.getEmailFromToken(token);
            if (email == null) {
                return Result.error("Token无效");
            }

            // 通过邮箱查询用户
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, email));
            if (user == null) {
                return Result.error("用户不存在");
            }
            Integer userId = user.getUserId();
            
            Post post = new Post();
            BeanUtil.copyProperties(postDto, post);
            
            // 设置必要字段
            post.setUserId(userId);
            post.setCreateTime(LocalDateTime.now());
            post.setUpdateTime(LocalDateTime.now());
            post.setViewCount(0);
            post.setLikeCount(0);
            post.setCommentCount(0);
            post.setIsDeleted(false);
            
            // 检查标题长度
            if (post.getPostTitle().length() > 100) {
                return Result.error("标题长度不能超过100个字符");
            }
            
            // 检查是否存在重复文章
            if (checkDuplicateTitle(userId, post.getPostTitle())) {
                return Result.error("文章标题已存在");
            }
            
            // 使用 baseMapper 保存
            if (baseMapper.insert(post) > 0) {
                return Result.success("发布成功");
            }
            return Result.error("发布失败");
            
        } catch (Exception e) {
            log.error("发布文章失败: {}", e.getMessage());
            return Result.error("发布失败：" + e.getMessage());
        }
    }

    @Override
    public Result<String> delete(PostDto postDto) {
        if (postDto.getJwt() == null) {
            return Result.error("请先登录");
        }
        
        try {
            // 从 Token 获取邮箱
            String email = jwtUtils.getEmailFromToken(postDto.getJwt());
            if (email == null) {
                return Result.error("Token无效");
            }

            // 通过邮箱查询用户
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, email));
            if (user == null) {
                return Result.error("用户不存在");
            }
            Integer userId = user.getUserId();
            
            LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Post::getPostId, postDto.getPostId())
                    .eq(Post::getUserId, userId);
            
            if (baseMapper.delete(queryWrapper) > 0) {
                return Result.success("删除成功");
            }
            return Result.error("文章不存在或无权限删除");
        } catch (Exception e) {
            return Result.error("Token无效或已过期");
        }
    }

    @Override
    public Result<List<PostDto>> find(Post post) {
        try {
            LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
            
            // 构建搜索条件
            if (StrUtil.isNotBlank(post.getPostTitle())) {
                queryWrapper.like(Post::getPostTitle, post.getPostTitle());
            }
            if (StrUtil.isNotBlank(post.getPostText())) {
                queryWrapper.or().like(Post::getPostText, post.getPostText());
            }
            
            // 添加排序
            queryWrapper.orderByDesc(Post::getCreateTime);
            
            // 执行查询
            List<Post> posts = baseMapper.selectList(queryWrapper);
            List<PostDto> postDtos = posts.stream()
                    .map(this::convertToDto)
                    .toList();
            
            return Result.success(postDtos.toString());
        } catch (Exception e) {
            log.error("查询文章失败: {}", e.getMessage());
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    public Result<String> revise(PostDto postDto) {
        if (validatePostDto(postDto)) {
            return Result.error("请求参数不完整");
        }
        
        try {
            // 从 Token 获取邮箱
            String email = jwtUtils.getEmailFromToken(postDto.getJwt());
            if (email == null) {
                return Result.error("Token无效");
            }

            // 通过邮箱查询用户
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, email));
            if (user == null) {
                return Result.error("用户不存在");
            }
            Integer userId = user.getUserId();
            
            // 检查文章是否存在
            Post existingPost = getById(postDto.getPostId());
            if (existingPost == null) {
                return Result.error("文章不存在");
            }
            
            // 检查权限
            if (!existingPost.getUserId().equals(userId)) {
                return Result.error("无权修改此文章");
            }
            
            Post post = new Post();
            BeanUtil.copyProperties(postDto, post);
            post.setUpdateTime(LocalDateTime.now());
            
            updateById(post);
            return Result.success("更新成功");
        } catch (Exception e) {
            log.error("修改文章失败: {}", e.getMessage());
            return Result.error("修改失败：" + e.getMessage());
        }
    }

    @Override
    public Result<PostDto> getPostDetail(Integer postId) {
        try {
            Post post = getById(postId);
            if (post == null) {
                return Result.error("文章不存在");
            }
            
            // 更新浏览量
            post.setViewCount(post.getViewCount() + 1);
            updateById(post);
            
            return Result.success("成功",convertToDto(post));
        } catch (Exception e) {
            log.error("获取文章详情失败: {}", e.getMessage());
            return Result.error("获取文章详情失败");
        }
    }

    @Override
    public Result<List<PostDto>> getPostList(Integer pageNum, Integer pageSize) {
        try {
            Page<Post> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(Post::getCreateTime);
            
            Page<Post> postPage = page(page, queryWrapper);
            List<PostDto> postDtos = postPage.getRecords().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            
            return Result.success("成功",postDtos);
        } catch (Exception e) {
            log.error("获取文章列表失败: {}", e.getMessage());
            return Result.error("获取文章列表失败");
        }
    }

    @Override
    public Result<String> likePost(Integer postId, String token) {
        try {
            // 从 Token 获取邮箱
            String email = jwtUtils.getEmailFromToken(token);
            if (email == null) {
                return Result.error("Token无效");
            }

            // 通过邮箱查询用户
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, email));
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            Post post = getById(postId);
            if (post == null) {
                return Result.error("文章不存在");
            }
            
            // TODO: 实现点赞逻辑，需要考虑是否重复点赞
            post.setLikeCount(post.getLikeCount() + 1);
            updateById(post);
            
            return Result.success("点赞成功");
        } catch (Exception e) {
            log.error("点赞失败: {}", e.getMessage());
            return Result.error("点赞失败");
        }
    }

    @Override
    public Result<List<PostDto>> getPostMind(Integer pageNum, Integer pageSize ,Integer userId) {
        try {
            // 默认分页参数
            Page<Post> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
            
            // 添加用户ID过滤条件
            queryWrapper.eq(Post::getUserId, userId)
                       .orderByDesc(Post::getCreateTime);

            Page<Post> postPage = page(page, queryWrapper);
            List<PostDto> postDtos = postPage.getRecords().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            return Result.success("成功", postDtos);
        } catch (Exception e) {
            log.error("获取文章列表失败: {}", e.getMessage());
            return Result.error("获取文章列表失败");
        }
    }

    // 工具方法
    private boolean validatePostDto(PostDto postDto) {
        return postDto == null
                || !StrUtil.isNotBlank(postDto.getPostTitle())
                || !StrUtil.isNotBlank(postDto.getPostText());
    }

    private boolean checkDuplicateTitle(Integer userId, String title) {
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Post::getUserId, userId)
                .eq(Post::getPostTitle, title);
        return baseMapper.selectCount(queryWrapper) > 0;
    }

    private PostDto convertToDto(Post post) {
        if (post == null) {
            return null;
        }
        
        PostDto postDto = new PostDto();
        BeanUtil.copyProperties(post, postDto);
        
        // 获取作者信息
        User user = userMapper.selectById(post.getUserId());
        if (user != null) {
            postDto.setAuthorName(user.getUsername());
        }
        
        return postDto;
    }

    // 添加工具方法用于验证请求
    private Result<User> validateTokenAndGetUser(String token) {
        try {
            String email = jwtUtils.getEmailFromToken(token);
            if (email == null) {
                return Result.error("Token无效");
            }

            User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, email));
            if (user == null) {
                return Result.error("用户不存在");
            }

            return Result.success("验证成功", user);
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            return Result.error("Token验证失败");
        }
    }

    // 添加方法以检查权限
    private Result<String> checkPostPermission(Integer postId, Integer userId) {
        Post post = getById(postId);
        if (post == null) {
            return Result.error("文章不存在");
        }
        
        if (!post.getUserId().equals(userId)) {
            return Result.error("无权限操作此文章");
        }
        
        return Result.success("成功");
    }
}