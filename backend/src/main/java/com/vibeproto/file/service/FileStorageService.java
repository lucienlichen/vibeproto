package com.vibeproto.file.service;

import com.vibeproto.common.config.properties.StorageProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final StorageProperties storageProperties;

    public FileStorageService(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    public String store(MultipartFile file, String relativeDir, String filename) throws IOException {
        Path directory = resolve(relativeDir);
        Files.createDirectories(directory);
        Path target = directory.resolve(filename);
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return normalize(relativeDir + "/" + filename);
    }

    public String storeText(String content, String relativeDir, String filename) throws IOException {
        Path directory = resolve(relativeDir);
        Files.createDirectories(directory);
        Path target = directory.resolve(filename);
        Files.writeString(target, content);
        return normalize(relativeDir + "/" + filename);
    }

    public String storeFile(Path sourceFile, String relativeDir, String filename) throws IOException {
        Path directory = resolve(relativeDir);
        Files.createDirectories(directory);
        Path target = directory.resolve(filename);
        Files.copy(sourceFile, target, StandardCopyOption.REPLACE_EXISTING);
        return normalize(relativeDir + "/" + filename);
    }

    public String uniqueFilename(String originalFilename, String fallbackExtension) {
        String extension = StringUtils.getFilenameExtension(originalFilename);
        String suffix = StringUtils.hasText(extension) ? "." + extension : fallbackExtension;
        return UUID.randomUUID().toString().replace("-", "") + suffix;
    }

    private Path resolve(String relativeDir) {
        return Path.of(storageProperties.rootPath()).resolve(relativeDir).normalize();
    }

    private String normalize(String path) {
        return path.replace("\\", "/");
    }
}
