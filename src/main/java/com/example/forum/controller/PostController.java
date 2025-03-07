package com.example.forum.controller;


import com.example.forum.Dto.PostDto;
import com.example.forum.Dto.PostPublishDto;
import com.example.forum.entity.Post;
import com.example.forum.entity.User;
import com.example.forum.service.IPostService;
import com.example.forum.service.IUserService;
import com.example.forum.utils.JwtUtils;
import com.example.forum.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

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

    // 图片存储的基础路径
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    // 访问图片的基础URL
    @Value("${file.access-url}")
    private String accessUrl;

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

    @PostMapping("/upload")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                return Result.error("请选择要上传的图片");
            }

            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("只能上传图片文件");
            }

            // 获取文件扩展名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 生成唯一文件名
            String newFilename = UUID.randomUUID() + extension;

            // 确保目录存在
            File uploadPath = new File(uploadDir);
            if (!uploadPath.exists()) {
                uploadPath.mkdirs();
            }

            // 保存文件
            Path filePath = Paths.get(uploadDir, newFilename);
            Files.write(filePath, file.getBytes());
            // 验证文件是否成功保存
            File savedFile = new File(uploadDir, newFilename);
            if (!savedFile.exists()) {
                log.error("图片保存失败，文件不存在: {}", savedFile.getAbsolutePath());
                return Result.error("图片保存失败");
            }
            // 返回图片访问URL - 修改为正确的URL格式
            String imageUrl = accessUrl + "/" + newFilename;
            log.info("图片上传成功: {}, 物理路径: {}", imageUrl, savedFile.getAbsolutePath());
            log.info("图片上传成功: {}", imageUrl);

            return Result.success("图片上传成功", imageUrl);
        } catch (IOException e) {
            log.error("图片上传失败: {}", e.getMessage());
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传文章封面图片
     */
    @PostMapping("/uploadCover")
    public Result<String> uploadCoverImage(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            // 验证用户登录状态
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
    
            // 检查文件是否为空
            if (file.isEmpty()) {
                return Result.error("请选择要上传的图片");
            }
    
            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("只能上传图片文件");
            }
    
            // 检查文件大小
            if (file.getSize() > 5 * 1024 * 1024) { // 5MB
                return Result.error("图片大小不能超过5MB");
            }
    
            // 获取文件扩展名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
    
            // 生成唯一文件名
            String newFilename = UUID.randomUUID() + extension;
    
            // 确保目录存在
            File uploadPath = new File(uploadDir);
            if (!uploadPath.exists()) {
                uploadPath.mkdirs();
            }
    
            // 保存文件
            Path filePath = Paths.get(uploadDir, newFilename);
            Files.write(filePath, file.getBytes());
    
            // 返回图片访问URL
            String imageUrl = accessUrl + "/" + newFilename;
            log.info("图片上传成功: {}", imageUrl);
    
            return Result.success("图片上传成功", imageUrl);
        } catch (IOException e) {
            log.error("图片上传失败: {}", e.getMessage());
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }
    /**
     * 上传文章内容图片
     */
    @PostMapping("/uploadContent")
    public Result<String> uploadContentImage(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            // 验证用户登录状态
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
    
            // 检查文件是否为空
            if (file.isEmpty()) {
                return Result.error("请选择要上传的图片");
            }
    
            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("只能上传图片文件");
            }
    
            // 检查文件大小
            if (file.getSize() > 10 * 1024 * 1024) { // 10MB
                return Result.error("图片大小不能超过10MB");
            }
    
            // 获取文件扩展名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
    
            // 生成唯一文件名
            String newFilename = UUID.randomUUID() + extension;
    
            // 确保目录存在
            File uploadPath = new File(uploadDir);
            if (!uploadPath.exists()) {
                uploadPath.mkdirs();
            }
    
            // 保存文件
            Path filePath = Paths.get(uploadDir, newFilename);
            Files.write(filePath, file.getBytes());
    
            // 返回图片访问URL
            String imageUrl = accessUrl + "/" + newFilename;
            log.info("内容图片上传成功: {}", imageUrl);
    
            return Result.success("图片上传成功", imageUrl);
        } catch (IOException e) {
            log.error("图片上传失败: {}", e.getMessage());
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 文章发布时的图片上传处理
     * 返回包含文章标题、内容、内容图片和封面图片路径集合的数据结构
     */
    @PostMapping("/publishWithImages")
    public Result<PostPublishDto> publishWithImages(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "contentImages", required = false) MultipartFile[] contentImages,
            @RequestParam(value = "coverImages", required = false) MultipartFile[] coverImages,
            HttpServletRequest request) {
        
        try {
            // 验证用户登录状态
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
            
            // 创建返回的DTO对象
            PostPublishDto publishDto = new PostPublishDto();
            publishDto.setPostTitle(title);
            publishDto.setPostText(content);
            
            // 处理内容图片上传
            List<String> contentImageUrls = new ArrayList<>();
            if (contentImages != null && contentImages.length > 0) {
                for (MultipartFile file : contentImages) {
                    // 检查文件是否为空
                    if (file.isEmpty()) {
                        continue;
                    }
                    
                    // 检查文件类型
                    String contentType = file.getContentType();
                    if (contentType == null || !contentType.startsWith("image/")) {
                        continue;
                    }
                    
                    // 检查文件大小
                    if (file.getSize() > 10 * 1024 * 1024) { // 10MB
                        continue;
                    }
                    
                    // 获取文件扩展名
                    String originalFilename = file.getOriginalFilename();
                    String extension = "";
                    if (originalFilename != null && originalFilename.contains(".")) {
                        extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    }
                    
                    // 生成唯一文件名
                    String newFilename = UUID.randomUUID() + extension;
                    
                    // 确保目录存在
                    File uploadPath = new File(uploadDir);
                    if (!uploadPath.exists()) {
                        uploadPath.mkdirs();
                    }
                    
                    // 保存文件
                    Path filePath = Paths.get(uploadDir, newFilename);
                    Files.write(filePath, file.getBytes());
                    
                    // 添加到URL列表
                    String imageUrl = accessUrl + "/" + newFilename;
                    contentImageUrls.add(imageUrl);
                    log.info("内容图片上传成功: {}", imageUrl);
                }
            }
            publishDto.setContentImages(contentImageUrls);
            
            // 处理封面图片上传
            List<String> coverImageUrls = new ArrayList<>();
            if (coverImages != null && coverImages.length > 0) {
                for (MultipartFile file : coverImages) {
                    // 检查文件是否为空
                    if (file.isEmpty()) {
                        continue;
                    }
                    
                    // 检查文件类型
                    String contentType = file.getContentType();
                    if (contentType == null || !contentType.startsWith("image/")) {
                        continue;
                    }
                    
                    // 检查文件大小
                    if (file.getSize() > 5 * 1024 * 1024) { // 5MB
                        continue;
                    }
                    
                    // 获取文件扩展名
                    String originalFilename = file.getOriginalFilename();
                    String extension = "";
                    if (originalFilename != null && originalFilename.contains(".")) {
                        extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    }
                    
                    // 生成唯一文件名
                    String newFilename = UUID.randomUUID() + extension;
                    
                    // 确保目录存在
                    File uploadPath = new File(uploadDir);
                    if (!uploadPath.exists()) {
                        uploadPath.mkdirs();
                    }
                    
                    // 保存文件
                    Path filePath = Paths.get(uploadDir, newFilename);
                    Files.write(filePath, file.getBytes());
                    
                    // 添加到URL列表
                    String imageUrl = accessUrl + "/" + newFilename;
                    coverImageUrls.add(imageUrl);
                    log.info("封面图片上传成功: {}", imageUrl);
                }
            }
            publishDto.setCoverImages(coverImageUrls);
            
            return Result.success("图片上传成功", publishDto);
            
        } catch (IOException e) {
            log.error("图片上传失败: {}", e.getMessage());
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }
}
