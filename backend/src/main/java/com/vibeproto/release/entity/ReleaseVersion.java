package com.vibeproto.release.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("release_version")
public class ReleaseVersion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long sourceVersionId;

    private Long buildTaskId;

    private String versionNo;

    private String releasePath;

    private String previewUrl;

    private Integer isCurrent;

    private Long releasedBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
