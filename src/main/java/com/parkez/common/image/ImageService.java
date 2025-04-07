package com.parkez.common.image;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.image.dto.ImageDeleteRequest;
import com.parkez.common.image.dto.ImageUploadRequest;
import com.parkez.common.image.enums.imageTargetType;
import com.parkez.common.image.exception.ImageErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final AmazonS3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public List<String> upload(ImageUploadRequest request, List<MultipartFile> files) {

        List<String> imageUrls = new ArrayList<>();
        imageTargetType targetType = imageTargetType.fromString(request.getTargetType());

        for(MultipartFile file : files) {
            String fileName = createS3Key(targetType.name(), request.getTargetId(), file);

            uploadToS3(file, fileName);

            imageUrls.add(getPublicUrl(fileName));

        }

        return imageUrls;
    }

    public List<String> update(ImageUploadRequest request, List<MultipartFile> files) {

        ImageDeleteRequest deleteRequest = new ImageDeleteRequest(
                request.getTargetType(),
                request.getTargetId()
        );

        delete(deleteRequest);

        return upload(request, files);
    }


    public void delete(ImageDeleteRequest request) {
        imageTargetType targetType = imageTargetType.fromString(request.getTargetType());

        String folderPrefix = String.format("%s/%d/", targetType.name(), request.getTargetId());

        ListObjectsV2Request listRequest = new ListObjectsV2Request()
                .withBucketName(bucket)
                .withPrefix(folderPrefix);


        ListObjectsV2Result result = s3Client.listObjectsV2(listRequest);
        List<S3ObjectSummary> objectSummaries = result.getObjectSummaries();

        if (objectSummaries.isEmpty()) {
            throw new ParkingEasyException(ImageErrorCode.IMAGE_NOT_FOUND);
        }

        // 삭제할 객체 키 리스트 구성
        List<DeleteObjectsRequest.KeyVersion> keysToDelete = objectSummaries.stream()
                .map(obj -> new DeleteObjectsRequest.KeyVersion(obj.getKey()))
                .collect(Collectors.toList());

        // S3 객체 삭제 요청
        DeleteObjectsRequest deleteRequest = new DeleteObjectsRequest(bucket)
                .withKeys(keysToDelete);

        s3Client.deleteObjects(deleteRequest);

    }


    private void uploadToS3(MultipartFile file, String fileName) {
        try{
            // 메타데이터 설정
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            // S3에 파일 업로드 요청 생성
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, fileName, file.getInputStream(), metadata);

            // S3에 파일 업로드
            s3Client.putObject(putObjectRequest);

        } catch (IOException e) {
            throw new ParkingEasyException(ImageErrorCode.IMAGE_UPLOAD_FAIL);
        }
    }

    private String createS3Key(String targetType, Long targetId, MultipartFile file) {
        long currentTime = System.currentTimeMillis();
        String uuidFileName = UUID.randomUUID() + "_" + file.getOriginalFilename() + "_" + currentTime;
        return String.format("%s/%d/%s", targetType, targetId, uuidFileName);
    }


    private String getPublicUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, s3Client.getRegionName(), key);
    }


}
