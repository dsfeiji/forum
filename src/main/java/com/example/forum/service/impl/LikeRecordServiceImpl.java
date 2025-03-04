package com.example.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.forum.entity.LikeRecord;
import com.example.forum.entity.Post;
import com.example.forum.entity.User;
import com.example.forum.mapper.LikeRecordMapper;
import com.example.forum.mapper.PostMapper;
import com.example.forum.mapper.UserMapper;
import com.example.forum.service.ILikeRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.forum.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-03-04
 */
@Slf4j
@Service
public class LikeRecordServiceImpl extends ServiceImpl<LikeRecordMapper, LikeRecord> implements ILikeRecordService {

    @Autowired
    private PostMapper postMapper;
    
    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public Result<String> toggleLike(LikeRecord likeRecord) {
        try {
            // 检查文章是否存在
            Post post = postMapper.selectById(likeRecord.getPostId());
            if (post == null) {
                return Result.error("文章不存在");
            }
            
            // 检查是否已经点赞
            LambdaQueryWrapper<LikeRecord> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(LikeRecord::getUserId, likeRecord.getUserId())
                    .eq(LikeRecord::getPostId, likeRecord.getPostId());
            LikeRecord existingRecord = getOne(queryWrapper);
            
            // 如果已经点赞，则取消点赞
            if (existingRecord != null) {
                // 删除点赞记录
                if (remove(queryWrapper)) {
                    // 更新文章的点赞数
                    Integer currentLikeCount = post.getLikeCount();
                    if (currentLikeCount != null && currentLikeCount > 0) {
                        post.setLikeCount(currentLikeCount - 1);
                        postMapper.updateById(post);
                    }
                    return Result.success("取消点赞成功");
                }
                return Result.error("取消点赞失败");
            } 
            // 如果未点赞，则添加点赞
            else {
                // 设置点赞时间
                likeRecord.setCreateTime(LocalDateTime.now());
                
                // 保存点赞记录
                if (save(likeRecord)) {
                    // 更新文章的点赞数
                    Integer currentLikeCount = post.getLikeCount();
                    post.setLikeCount(currentLikeCount == null ? 1 : currentLikeCount + 1);
                    postMapper.updateById(post);
                    return Result.success("点赞成功");
                }
                return Result.error("点赞失败");
            }
        } catch (Exception e) {
            log.error("点赞操作失败: {}", e.getMessage());
            return Result.error("点赞操作失败：" + e.getMessage());
        }
    }

    // 其他方法保持不变
    @Override
    public Result<Boolean> checkUserLiked(Integer userId, Integer postId) {
        // 保持原有实现不变
        try {
            // 查找点赞记录
            LambdaQueryWrapper<LikeRecord> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(LikeRecord::getUserId, userId)
                    .eq(LikeRecord::getPostId, postId);
            LikeRecord existingRecord = getOne(queryWrapper);
            
            return Result.success("查询成功", existingRecord != null);
        } catch (Exception e) {
            log.error("查询点赞状态失败: {}", e.getMessage());
            return Result.error("查询点赞状态失败：" + e.getMessage());
        }
    }

    @Override
    public Result<List<Post>> getLikedPostsByUserId(Integer userId, Integer pageNum, Integer pageSize) {
        // 保持原有实现不变
        try {
            // 检查用户是否存在
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 分页查询用户点赞的文章ID
            Page<LikeRecord> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<LikeRecord> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(LikeRecord::getUserId, userId)
                    .orderByDesc(LikeRecord::getCreateTime);
            
            Page<LikeRecord> likeRecordPage = page(page, queryWrapper);
            List<LikeRecord> likeRecords = likeRecordPage.getRecords();
            
            // 如果没有点赞记录，返回空列表
            if (likeRecords.isEmpty()) {
                return Result.success("暂无点赞记录", new ArrayList<>());
            }
            
            // 获取文章详情
            List<Post> posts = new ArrayList<>();
            for (LikeRecord record : likeRecords) {
                Post post = postMapper.selectById(record.getPostId());
                if (post != null) {
                    // 获取作者信息
                    User author = userMapper.selectById(post.getUserId());
                    if (author != null) {
                        post.setUserId(author.getUserId());
                    }
                    posts.add(post);
                }
            }
            
            return Result.success("查询成功", posts);
        } catch (Exception e) {
            log.error("查询用户点赞文章失败: {}", e.getMessage());
            return Result.error("查询用户点赞文章失败：" + e.getMessage());
        }
    }

    @Override
    public Result<List<User>> getLikedUsersByPostId(Integer postId, Integer pageNum, Integer pageSize) {
        // 保持原有实现不变
        try {
            // 检查文章是否存在
            Post post = postMapper.selectById(postId);
            if (post == null) {
                return Result.error("文章不存在");
            }
            
            // 分页查询点赞该文章的用户ID
            Page<LikeRecord> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<LikeRecord> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(LikeRecord::getPostId, postId)
                    .orderByDesc(LikeRecord::getCreateTime);
            
            Page<LikeRecord> likeRecordPage = page(page, queryWrapper);
            List<LikeRecord> likeRecords = likeRecordPage.getRecords();
            
            // 如果没有点赞记录，返回空列表
            if (likeRecords.isEmpty()) {
                return Result.success("暂无点赞记录", new ArrayList<>());
            }
            
            // 获取用户详情
            List<User> users = new ArrayList<>();
            for (LikeRecord record : likeRecords) {
                User user = userMapper.selectById(record.getUserId());
                if (user != null) {
                    // 出于安全考虑，清除敏感信息
                    user.setPassword(null);
                    users.add(user);
                }
            }
            
            return Result.success("查询成功", users);
        } catch (Exception e) {
            log.error("查询文章点赞用户失败: {}", e.getMessage());
            return Result.error("查询文章点赞用户失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Integer> getPostLikeCount(Integer postId) {
        // 保持原有实现不变
        try {
            // 检查文章是否存在
            Post post = postMapper.selectById(postId);
            if (post == null) {
                return Result.error("文章不存在");
            }
            
            // 查询点赞数量
            LambdaQueryWrapper<LikeRecord> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(LikeRecord::getPostId, postId);
            int likeCount = (int) count(queryWrapper);
            
            return Result.success("查询成功", likeCount);
        } catch (Exception e) {
            log.error("查询文章点赞数失败: {}", e.getMessage());
            return Result.error("查询文章点赞数失败：" + e.getMessage());
        }
    }
}
