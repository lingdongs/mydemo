package com.xxx.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.xxx.config.S3ConfigProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final AmazonS3 amazonS3;
    private final S3ConfigProperties s3ConfigProperties;

    public String initiateMultipartUpload(String objectKey) {
        String bucketName = s3ConfigProperties.getTemp().getQdztbucket();
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, objectKey);
        InitiateMultipartUploadResult result = amazonS3.initiateMultipartUpload(request);
        return result.getUploadId();
    }

    public PartETag uploadPart(String objectKey, String uploadId, int partNumber, long partSize, InputStream inputStream) {
        String bucketName = s3ConfigProperties.getTemp().getQdztbucket();
        UploadPartRequest uploadRequest = new UploadPartRequest()
                .withBucketName(bucketName)
                .withKey(objectKey)
                .withUploadId(uploadId)
                .withPartNumber(partNumber)
                .withPartSize(partSize)
                .withInputStream(inputStream);
        UploadPartResult result = amazonS3.uploadPart(uploadRequest);
        PartETag partETag = result.getPartETag();
        log.info("Successfully uploaded part {} for uploadId {}. ETag: {}", partNumber, uploadId, partETag.getETag());
        return partETag;
    }

    public void completeMultipartUpload(String objectKey, String uploadId, List<PartETag> partETags) {
        String bucketName = s3ConfigProperties.getTemp().getQdztbucket();
        log.info("Completing multipart upload for objectKey: {}, uploadId: {}, with parts: {}", objectKey, uploadId, partETags);
        CompleteMultipartUploadRequest completeRequest = new CompleteMultipartUploadRequest(
                bucketName,
                objectKey,
                uploadId,
                partETags
        );
        amazonS3.completeMultipartUpload(completeRequest);
    }

    public List<PartSummary> listParts(String objectKey, String uploadId) {
        String bucketName = s3ConfigProperties.getTemp().getQdztbucket();
        ListPartsRequest listPartsRequest = new ListPartsRequest(bucketName, objectKey, uploadId);
        return amazonS3.listParts(listPartsRequest).getParts();
    }

    public ObjectMetadata getObjectMetadata(String objectKey) {
        String bucketName = s3ConfigProperties.getTemp().getQdztbucket();
        return amazonS3.getObjectMetadata(bucketName, objectKey);
    }

    public S3ObjectInputStream getS3ObjectInputStream(String objectKey, long start, long end) {
        String bucketName = s3ConfigProperties.getTemp().getQdztbucket();
        GetObjectRequest rangeRequest = new GetObjectRequest(bucketName, objectKey).withRange(start, end);
        S3Object object = amazonS3.getObject(rangeRequest);
        return object.getObjectContent();
    }

    public List<String> listObjects(String prefix) {
        String bucketName = s3ConfigProperties.getTemp().getQdztbucket();
        ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucketName).withPrefix(prefix);
        ListObjectsV2Result result;
        result = amazonS3.listObjectsV2(req);
        return result.getObjectSummaries().stream()
                .map(S3ObjectSummary::getKey)
                .collect(Collectors.toList());
    }
}
