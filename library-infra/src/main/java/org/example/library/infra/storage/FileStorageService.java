package org.example.library.infra.storage;

import org.example.library.common.BusinessException;
import org.example.library.common.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** 本地文件存储：把上传的封面图片落盘到可配置目录，返回对外访问 URL。 */
@Component
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private static final long MAX_SIZE = 5 * 1024 * 1024;

    private static final Set<String> ALLOWED_EXT =
            new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "gif", "webp"));

    private final Path coversDir;

    public FileStorageService(@Value("${library.upload.dir:./uploads}") String uploadDir) {
        this.coversDir = Paths.get(uploadDir, "covers").toAbsolutePath().normalize();
    }

    /** 保存封面图片，返回形如 /uploads/covers/xxx.jpg 的访问路径。 */
    public String storeCover(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请选择要上传的图片");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "图片大小不能超过 5MB");
        }
        String ext = resolveExt(file.getOriginalFilename());
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "仅支持 jpg/jpeg/png/gif/webp 格式");
        }

        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        try {
            Files.createDirectories(coversDir);
            Path target = coversDir.resolve(filename).normalize();
            if (!target.startsWith(coversDir)) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "非法文件名");
            }
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("封面保存失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "封面保存失败");
        }
        return "/uploads/covers/" + filename;
    }

    private String resolveExt(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int dot = originalFilename.lastIndexOf('.');
        if (dot < 0 || dot == originalFilename.length() - 1) {
            return "";
        }
        return originalFilename.substring(dot + 1).toLowerCase();
    }
}
