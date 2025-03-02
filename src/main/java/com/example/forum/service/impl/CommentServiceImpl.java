package com.example.forum.service.impl;

import com.example.forum.entity.Comment;
import com.example.forum.mapper.CommentMapper;
import com.example.forum.service.ICommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author DSfeiji
 * @since 2025-02-18
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

}
