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
import java.net.URI;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class ImageStorageService {
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private final Path uploadDirectory;
    private final String spacesBucket;
    private final String spacesPublicUrl;
    private final S3Client spacesClient;

    public ImageStorageService(
            @Value("${shopease.upload-dir:uploads/products}") String uploadDirectory,
            @Value("${SPACES_ENDPOINT:}") String spacesEndpoint,
            @Value("${SPACES_REGION:}") String spacesRegion,
            @Value("${SPACES_BUCKET:}") String spacesBucket,
            @Value("${SPACES_ACCESS_KEY:}") String spacesAccessKey,
            @Value("${SPACES_SECRET_KEY:}") String spacesSecretKey,
            @Value("${SPACES_PUBLIC_URL:}") String spacesPublicUrl) {
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
        this.spacesBucket = spacesBucket;
        this.spacesPublicUrl = stripTrailingSlash(spacesPublicUrl);
        this.spacesClient = allPresent(spacesEndpoint, spacesRegion, spacesBucket, spacesAccessKey, spacesSecretKey, spacesPublicUrl)
                ? S3Client.builder()
                    .endpointOverride(URI.create(spacesEndpoint))
                    .region(Region.of(spacesRegion))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(spacesAccessKey, spacesSecretKey)))
                    .build()
                : null;
    }

    public String storeProductImage(MultipartFile image) {
        if (image == null || image.isEmpty()) return null;
        if (image.getContentType() == null || !ALLOWED_TYPES.contains(image.getContentType().toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Please upload a JPG, PNG, WebP, or GIF image");
        }

        String originalName = StringUtils.cleanPath(image.getOriginalFilename() == null ? "image" : image.getOriginalFilename());
        String extension = StringUtils.getFilenameExtension(originalName);
        String fileName = UUID.randomUUID() + (extension == null ? "" : "." + extension.toLowerCase(Locale.ROOT));
        if (spacesClient != null) return storeInSpaces(image, fileName);
        try {
            Files.createDirectories(uploadDirectory);
            Files.copy(image.getInputStream(), uploadDirectory.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/products/" + fileName;
        } catch (IOException e) {
            throw new IllegalArgumentException("The product image could not be saved", e);
        }
    }

    private String storeInSpaces(MultipartFile image, String fileName) {
        String key = "products/" + fileName;
        try {
            spacesClient.putObject(PutObjectRequest.builder()
                            .bucket(spacesBucket)
                            .key(key)
                            .contentType(image.getContentType())
                            .acl("public-read")
                            .build(),
                    RequestBody.fromInputStream(image.getInputStream(), image.getSize()));
            return spacesPublicUrl + "/" + key;
        } catch (Exception e) {
            throw new IllegalArgumentException("The product image could not be uploaded", e);
        }
    }

    private static boolean allPresent(String... values) {
        for (String value : values) if (!StringUtils.hasText(value)) return false;
        return true;
    }

    private static String stripTrailingSlash(String value) {
        return value == null ? "" : value.replaceAll("/+$", "");
    }
}
