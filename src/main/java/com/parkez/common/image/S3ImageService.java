package com.parkez.common.image;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.image.dto.request.ImageDeleteRequest;
import com.parkez.common.image.dto.request.ImageUploadRequest;
import com.parkez.common.image.dto.response.ImageUrlResponse;
import com.parkez.common.image.enums.AllowedExtension;
import com.parkez.common.image.enums.ImageTargetType;
import com.parkez.common.image.exception.ImageErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class S3ImageService implements ImageService {

    private final AmazonS3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Override
    public ImageUrlResponse upload(ImageUploadRequest request, List<MultipartFile> files) {

        if(files == null || files.isEmpty()) {
            throw new ParkingEasyException(ImageErrorCode.IMAGE_IS_NULL);
        }

        List<String> imageUrls = new ArrayList<>();
        ImageTargetType targetType = ImageTargetType.of(request.getTargetType());

        for(MultipartFile file : files) {

            if(!isAllowedExtension(file)) {
                throw new ParkingEasyException(ImageErrorCode.INVALID_EXTENSION_TYPE);
            }

            long currentTime = System.currentTimeMillis();
            String fileName = createS3Key(targetType.name(), request.getTargetId(), file, currentTime);

            uploadToS3(file, fileName);

            imageUrls.add(getPublicUrl(fileName));

        }

        return ImageUrlResponse.of(imageUrls);
    }

    @Override
    public ImageUrlResponse update(ImageUploadRequest request, List<MultipartFile> files) {

        ImageDeleteRequest deleteRequest = new ImageDeleteRequest(
                request.getTargetType(),
                request.getTargetId()
        );

        delete(deleteRequest);

        return upload(request, files);
    }

    @Override
    public void delete(ImageDeleteRequest request) {
        ImageTargetType targetType = ImageTargetType.of(request.getTargetType());

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

    private String createS3Key(String targetType, Long targetId, MultipartFile file, long currentTime) {

        String uuidFileName = UUID.randomUUID() + "_" + file.getOriginalFilename() + "_" + currentTime;
        return String.format("%s/%d/%s", targetType, targetId, uuidFileName);
    }


    private String getPublicUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, s3Client.getRegionName(), key);
    }

    private boolean isAllowedExtension(MultipartFile file){
        String extension = getFileExtension(file);
        return AllowedExtension.isAllowedExtension(extension);
    }

    private String getFileExtension(MultipartFile file){
        String originalFilename = file.getOriginalFilename();
        if(originalFilename!=null){

            int index = originalFilename.lastIndexOf('.');
            if (index == -1 || index == originalFilename.length() - 1) {
                return ""; // 확장자가 없거나, .으로 끝나는 경우
            }
            return originalFilename.substring(index + 1).toLowerCase();

        } throw new ParkingEasyException(ImageErrorCode.IMAGE_IS_NULL);

    }


}
