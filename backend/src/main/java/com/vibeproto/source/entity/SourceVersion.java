package com.vibeproto.source.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("source_version")
public class SourceVersion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String versionNo;

    private String sourceType;

    private String sourceName;

    private String filePath;

    private String gitUrl;

    private String gitBranch;

    private String commitHash;

    private String htmlContentPath;

    private String remark;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
