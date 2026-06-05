package com.example.soundscape_app.mapper.song;

import com.example.soundscape_app.dto.response.song.CommentResponse;
import com.example.soundscape_app.entity.song.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "replyCount", expression = "java(comment.getReplies() != null ? comment.getReplies().size() : 0)")
    CommentResponse toResponse(Comment comment);

}