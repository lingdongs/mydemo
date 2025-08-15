package com.xxx.controller;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.xxx.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/video")
@RequiredArgsConstructor
public class VideoController {

    private final S3Service s3Service;

    @GetMapping("/list")
    public ResponseEntity<List<Map<String, String>>> listVideos() {
        List<String> objectKeys = s3Service.listObjects("zyxfapp/");
        List<Map<String, String>> videoInfos = objectKeys.stream()
                .map(objectKey -> {
                    Map<String, String> videoInfo = new HashMap<>();
                    videoInfo.put("objectKey", objectKey);
                    String[] parts = objectKey.split("/");
                    String lastPart = parts[parts.length - 1];
                    int firstHyphenIndex = lastPart.indexOf('-');
                    String fileName = (firstHyphenIndex != -1) ? lastPart.substring(firstHyphenIndex + 1) : lastPart;
                    videoInfo.put("fileName", fileName);
                    return videoInfo;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(videoInfos);
    }

    @GetMapping("/stream")
    public ResponseEntity<StreamingResponseBody> streamVideo(@RequestParam String objectKey, @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader) throws IOException {
        ObjectMetadata objectMetadata = s3Service.getObjectMetadata(objectKey);
        long contentLength = objectMetadata.getContentLength();
        String contentType = objectMetadata.getContentType();

        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT_RANGES, "bytes");
        headers.add(HttpHeaders.CONTENT_TYPE, contentType);

        if (rangeHeader != null) {
            String[] ranges = rangeHeader.substring("bytes=".length()).split("-");
            long rangeStart = Long.parseLong(ranges[0]);
            long rangeEnd = ranges.length > 1 && !ranges[1].isEmpty() ? Long.parseLong(ranges[1]) : contentLength - 1;

            if (rangeEnd >= contentLength) {
                rangeEnd = contentLength - 1;
            }

            long requestedLength = rangeEnd - rangeStart + 1;
            headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(requestedLength));
            headers.add(HttpHeaders.CONTENT_RANGE, "bytes " + rangeStart + "-" + rangeEnd + "/" + contentLength);

            final long finalRangeStart = rangeStart;
            final long finalRangeEnd = rangeEnd;
            StreamingResponseBody responseBody = outputStream -> {
                try (S3ObjectInputStream s3is = s3Service.getS3ObjectInputStream(objectKey, finalRangeStart, finalRangeEnd)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = s3is.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.flush();
                }
            };
            return new ResponseEntity<>(responseBody, headers, HttpStatus.PARTIAL_CONTENT);
        } else {
            headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength));
            StreamingResponseBody responseBody = outputStream -> {
                try (S3ObjectInputStream s3is = s3Service.getS3ObjectInputStream(objectKey, 0, contentLength - 1)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = s3is.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.flush();
                }
            };
            return new ResponseEntity<>(responseBody, headers, HttpStatus.OK);
        }
    }
}
