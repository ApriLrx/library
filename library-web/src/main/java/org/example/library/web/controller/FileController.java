package org.example.library.web.controller;

import org.example.library.common.Result;
import org.example.library.domain.user.model.Role;
import org.example.library.infra.auth.RequireRole;
import org.example.library.infra.storage.FileStorageService;
import org.example.library.web.vo.UploadVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 文件上传接口。 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /** 上传书籍封面（ADMIN），返回可访问的 URL。 */
    @RequireRole(Role.ADMIN)
    @PostMapping("/cover")
    public Result<UploadVO> uploadCover(@RequestParam("file") MultipartFile file) {
        return Result.success(new UploadVO(fileStorageService.storeCover(file)));
    }
}
