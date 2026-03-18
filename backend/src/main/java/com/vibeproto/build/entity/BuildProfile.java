package com.vibeproto.build.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("build_profile")
public class BuildProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String profileName;

    private String nodeVersion;

    private String installCommand;

    private String buildCommand;

    private String outputDir;

    private String envJson;

    private Integer isDefault;

    private Integer enabled;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
