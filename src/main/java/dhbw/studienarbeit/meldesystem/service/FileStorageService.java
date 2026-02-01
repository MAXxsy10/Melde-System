package dhbw.studienarbeit.meldesystem.service;


import dhbw.studienarbeit.meldesystem.exceptions.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${app.file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${app.file.max-size:5242880}") // 5MB default
    private long maxFileSize;

    public String storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Cannot store empty file");
        }

        if (file.getSize() > maxFileSize) {
            throw new BadRequestException("File size exceeds maximum allowed size");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Only image files are allowed");
        }

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";

            String filename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(filename);

            // Copy file to upload directory
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("File stored successfully: {}", filename);
            return "/uploads/" + filename; // Return relative URL

        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new RuntimeException("Failed to store file", e);
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl != null && fileUrl.startsWith("/uploads/")) {
                String filename = fileUrl.replace("/uploads/", "");
                Path filePath = Paths.get(uploadDir).resolve(filename);
                Files.deleteIfExists(filePath);
                log.info("File deleted: {}", filename);
            }
        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileUrl, e);
        }
    }
}

