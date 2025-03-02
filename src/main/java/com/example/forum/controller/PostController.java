package com.example.forum.controller;


import com.example.forum.Dto.PostDto;
import com.example.forum.entity.Post;
import com.example.forum.entity.User;
import com.example.forum.service.IPostService;
import com.example.forum.service.IUserService;
import com.example.forum.utils.JwtUtils;
import com.example.forum.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author DSfeiji
 * @since 2025-02-18
 */
@RestController
@RequestMapping("/post")
@Slf4j
public class PostController {
    @Autowired
    private IPostService postService;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private IUserService userService;

    @PostMapping("/release")
    public Result<String> release(@RequestBody PostDto postDto, HttpServletRequest request) {
        System.out.println(postDto);
        try {
            // 从请求头获取 Token
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return Result.error("未登录");
            }
            token = token.substring(7); // 去除 "Bearer " 前缀

            // 从 Token 获取邮箱
            String email = jwtUtils.getEmailFromToken(token);
            if (email == null) {
                return Result.error("Token无效");
            }

            // 通过邮箱查询用户ID
            User user = userService.getUserByEmail(email);
            System.out.println(user);
            if (user == null) {
                return Result.error("用户不存在");
            }
            Integer userId = user.getUserId();
            System.out.println(userId);

            // 设置 user_id 到 DTO
            postDto.setUserId(userId);
            Post post = new Post();
            post.setUserId(userId);
            // 调用 Service
            return postService.release(postDto, token);
        } catch (Exception e) {
            log.error("发布文章失败: {}", e.getMessage());
            return Result.error("发布失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public Result<String> delete(@RequestBody PostDto postDto, HttpServletRequest request) {
        try {
            // 从请求头获取 Token
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return Result.error("未登录");
            }
            token = token.substring(7); // 去除 "Bearer " 前缀

            // 设置 token 到 DTO
            postDto.setJwt(token);
            return postService.delete(postDto);
        } catch (Exception e) {
            log.error("删除文章失败: {}", e.getMessage());
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    @GetMapping("/find")
    public Result<List<PostDto>> find(Post post) {
        // 移除 @RequestBody，因为是 GET 请求
        return postService.find(post);
    }

    @PostMapping("/revise")
    public Result<String> revise(@RequestBody PostDto postDto, HttpServletRequest request) {
        try {
            // 从请求头获取 Token
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return Result.error("未登录");
            }
            token = token.substring(7); // 去除 "Bearer " 前缀

            // 设置 token 到 DTO
            postDto.setJwt(token);
            return postService.revise(postDto);
        } catch (Exception e) {
            log.error("修改文章失败: {}", e.getMessage());
            return Result.error("修改失败: " + e.getMessage());
        }
    }

    @GetMapping("/detail/{postId}")
    public Result<PostDto> getPostDetail(@PathVariable Integer postId) {
        return postService.getPostDetail(postId);
    }

    @GetMapping("/list")
    public Result<List<PostDto>> getPostList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return postService.getPostList(pageNum, pageSize);
    }

    @PostMapping("/like/{postId}")
    public Result<String> likePost(@PathVariable Integer postId, HttpServletRequest request) {
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
        
        return postService.likePost(postId, token);
    }

    @GetMapping("/getPostMind")
    public Result<List<PostDto>> getPostMind(@RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize , HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.error("未登录");
        }
        token = token.substring(7);

        String email = jwtUtils.getEmailFromToken(token);
        if (email == null) {
            return Result.error("Token无效");
        }

        // 通过邮箱查询用户
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return Result.error("用户不存在");
        }
        Integer userId = user.getUserId();
        return postService.getPostMind(pageNum,pageSize,userId);
    }

}
