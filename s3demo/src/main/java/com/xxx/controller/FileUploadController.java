package com.xxx.controller;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.PartSummary;
import com.xxx.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class FileUploadController {

    private final S3Service s3Service;

    @PostMapping("/upload/initiate")
    public ResponseEntity<Map<String, String>> initiateMultipartUpload(@RequestParam String fileName) {
        try {
            // 1. Hardcode business type
            String businessType = "zyxfapp";

            // 2. Generate date path
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            // 3. Generate unique file name to avoid collision
            String uniqueFileName = UUID.randomUUID().toString() + "-" + fileName;

            // 4. Combine to create the final object key
            String objectKey = String.join("/", businessType, datePath, uniqueFileName);

            // 5. Initiate upload and get uploadId
            String uploadId = s3Service.initiateMultipartUpload(objectKey);

            // 6. Return both uploadId and the unique objectKey to the client
            Map<String, String> response = new HashMap<>();
            response.put("uploadId", uploadId);
            response.put("objectKey", objectKey);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to initiate multipart upload for fileName: {}", fileName, e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/upload/part")
    public ResponseEntity<PartETag> uploadPart(@RequestParam("file") MultipartFile file,
                                               @RequestParam String objectKey,
                                               @RequestParam String uploadId,
                                               @RequestParam int partNumber) {
        try {
            PartETag partETag = s3Service.uploadPart(objectKey, uploadId, partNumber, file.getSize(), file.getInputStream());
            return ResponseEntity.ok(partETag);
        } catch (IOException e) {
            log.error("Failed to upload part for objectKey: {}, uploadId: {}", objectKey, uploadId, e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/upload/complete")
    public ResponseEntity<String> completeMultipartUpload(@RequestParam String objectKey,
                                                          @RequestParam String uploadId,
                                                          @RequestBody List<Map<String, Object>> parts) {
        try {
            List<PartETag> partETags = parts.stream()
                    .map(part -> new PartETag((Integer) part.get("partNumber"), (String) part.get("eTag")))
                    .sorted(Comparator.comparingInt(PartETag::getPartNumber))
                    .collect(Collectors.toList());
            s3Service.completeMultipartUpload(objectKey, uploadId, partETags);
            return ResponseEntity.ok("File uploaded successfully.");
        } catch (Exception e) {
            log.error("Failed to complete multipart upload for objectKey: {}, uploadId: {}. Received parts: {}", objectKey, uploadId, parts, e);
            return ResponseEntity.status(500).body("Failed to complete multipart upload: " + e.getMessage());
        }
    }

    @GetMapping("/upload/parts")
    public ResponseEntity<List<PartSummary>> listParts(@RequestParam String objectKey, @RequestParam String uploadId) {
        try {
            List<PartSummary> parts = s3Service.listParts(objectKey, uploadId);
            return ResponseEntity.ok(parts);
        } catch (AmazonS3Exception e) {
            if ("NoSuchUpload".equals(e.getErrorCode())) {
                log.warn("No such upload found for uploadId: {}", uploadId);
                return ResponseEntity.status(404).body(null);
            }
            log.error("Error listing parts for uploadId {}: {}", uploadId, e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }
}
