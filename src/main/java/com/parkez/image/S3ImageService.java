package com.parkez.image;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.image.dto.request.ImageRequest;
import com.parkez.image.dto.response.ImageUrlResponse;
import com.parkez.image.enums.AllowedExtension;
import com.parkez.image.enums.ImageTargetType;
import com.parkez.image.exception.ImageErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3ImageService implements ImageService {

    private final AmazonS3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Override
    public ImageUrlResponse upload(ImageRequest request, List<MultipartFile> files) {

        if(files == null || files.isEmpty()) {
            throw new ParkingEasyException(ImageErrorCode.IMAGE_IS_NULL);
        }

        List<String> imageUrls = new ArrayList<>();
        ImageTargetType targetType = request.getTargetType();

        for(MultipartFile file : files) {

            if(!isValidExtension(file.getOriginalFilename())) {
                throw new ParkingEasyException(ImageErrorCode.INVALID_EXTENSION_TYPE);
            }

            LocalDateTime now = LocalDateTime.now();
            String fileName = createS3Key(targetType.name(), request.getTargetId(), file, now);

            uploadToS3(file, fileName);

            imageUrls.add(getPublicUrl(fileName));

        }

        return ImageUrlResponse.of(imageUrls);
    }

    @Override
    public ImageUrlResponse replace(ImageRequest request, List<MultipartFile> files) {

        delete(request);

        return upload(request, files);
    }

    @Override
    public void delete(ImageRequest request) {
        ImageTargetType targetType = request.getTargetType();

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

        ObjectMetadata metadata = createMetadata(file);

        try{
            // S3에 파일 업로드 요청 생성
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, fileName, file.getInputStream(), metadata);

            // S3에 파일 업로드
            s3Client.putObject(putObjectRequest);

        } catch (AmazonServiceException e) {
            log.error("[S3 Upload Error] AWS 응답 오류 - Status: {}, Code: {}, Message: {}", e.getStatusCode(), e.getErrorCode(), e.getMessage(), e);
            throw new ParkingEasyException(ImageErrorCode.IMAGE_UPLOAD_FAIL);

        } catch (SdkClientException e) {
            log.error("[S3 Upload Error] 클라이언트 설정 오류 - {}", e.getMessage(), e);
            throw new ParkingEasyException(ImageErrorCode.IMAGE_UPLOAD_FAIL);

        } catch (IOException e) {
            log.error("[IOException] 파일 스트림 처리 중 실패: {}", e.getMessage(), e);
            throw new ParkingEasyException(ImageErrorCode.IMAGE_UPLOAD_FAIL);
        }

    }

    private ObjectMetadata createMetadata(MultipartFile file) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        return metadata;
    }


    private String createS3Key(String targetType, Long targetId, MultipartFile file, LocalDateTime now) {

        String uuidFileName = String.format("%s_%s_%s", UUID.randomUUID(), file.getOriginalFilename(), now);
        return String.format("%s/%d/%s", targetType, targetId, uuidFileName);
    }


    private String getPublicUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, s3Client.getRegionName(), key);
    }

    private boolean isValidExtension(String fileName) {
        String extension = extractFileExtension(fileName);
        return AllowedExtension.contains(extension);
    }

    private String extractFileExtension(String fileName){

        if(fileName==null) {
            throw new ParkingEasyException(ImageErrorCode.IMAGE_IS_NULL);
        }

        int index = fileName.lastIndexOf('.');

        if (index == -1 || index == fileName.length() - 1) {
            throw new ParkingEasyException(ImageErrorCode.INVALID_EXTENSION_TYPE);
        }
        return fileName.substring(index + 1).toLowerCase();

    }


}
