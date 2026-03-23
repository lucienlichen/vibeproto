package com.vibeproto.source.service;

import com.vibeproto.source.dto.GitImportRequest;
import com.vibeproto.source.dto.HtmlCreateRequest;
import com.vibeproto.source.vo.SourceVersionVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface SourceVersionService {

    SourceVersionVO uploadZip(Long projectId, String remark, MultipartFile file, Long operatorId) throws IOException;

    SourceVersionVO createFromHtml(HtmlCreateRequest request, Long operatorId) throws IOException;

    SourceVersionVO createFromGit(GitImportRequest request, Long operatorId);

    List<SourceVersionVO> listByProjectId(Long projectId);

    void delete(Long id);

    Path getDownloadFile(Long id);
}
