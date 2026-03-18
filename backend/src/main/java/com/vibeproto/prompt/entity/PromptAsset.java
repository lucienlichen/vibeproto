package com.vibeproto.prompt.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("prompt_asset")
public class PromptAsset {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String title;

    private String promptType;

    private String content;

    private Long relatedSourceVersionId;

    private Long relatedReleaseId;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
