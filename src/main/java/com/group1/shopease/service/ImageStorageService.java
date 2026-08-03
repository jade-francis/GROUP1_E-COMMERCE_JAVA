package com.group1.shopease.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class ImageStorageService {
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private final Path uploadDirectory;

    public ImageStorageService(@Value("${shopease.upload-dir:uploads/products}") String uploadDirectory) {
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    public String storeProductImage(MultipartFile image) {
        if (image == null || image.isEmpty()) return null;
        if (image.getContentType() == null || !ALLOWED_TYPES.contains(image.getContentType().toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Please upload a JPG, PNG, WebP, or GIF image");
        }

        String originalName = StringUtils.cleanPath(image.getOriginalFilename() == null ? "image" : image.getOriginalFilename());
        String extension = StringUtils.getFilenameExtension(originalName);
        String fileName = UUID.randomUUID() + (extension == null ? "" : "." + extension.toLowerCase(Locale.ROOT));
        try {
            Files.createDirectories(uploadDirectory);
            Files.copy(image.getInputStream(), uploadDirectory.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/products/" + fileName;
        } catch (IOException e) {
            throw new IllegalArgumentException("The product image could not be saved", e);
        }
    }
}
