package com.parkez.image;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.image.config.S3Properties;
import com.parkez.image.dto.request.ImageRequest;
import com.parkez.image.dto.response.ImageUrlResponse;
import com.parkez.image.enums.AllowedExtension;
import com.parkez.image.enums.ImageTargetType;
import com.parkez.image.exception.ImageErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@EnableConfigurationProperties(S3Properties.class)
public class S3ImageService implements ImageService {

    private final S3Client s3Client;

    private final String region;

    private final String bucket;

    public S3ImageService(S3Client s3Client, S3Properties s3Properties) {
        this.s3Client = s3Client;
        this.region = s3Properties.getRegion();
        this.bucket = s3Properties.getS3Bucket();
    }
    @Override
    public ImageUrlResponse upload(ImageRequest request, List<MultipartFile> files) {

        if(files == null || files.isEmpty()) {
            throw new ParkingEasyException(ImageErrorCode.IMAGE_IS_NULL);
        }

        List<String> imageUrls = new ArrayList<>();
        ImageTargetType targetType = request.getTargetType();

        for(MultipartFile file : files) {

            String fileExtension = extractFileExtension(file.getOriginalFilename());

            if(!AllowedExtension.contains(fileExtension)){
                throw new ParkingEasyException(ImageErrorCode.INVALID_EXTENSION_TYPE);
            }

            LocalDateTime now = LocalDateTime.now();
            String fileName = createS3Key(targetType.name(), request.getTargetId(), now);

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

        ListObjectsV2Request listRequest = ListObjectsV2Request.builder().bucket(bucket).prefix(folderPrefix).build();


        ListObjectsV2Response response = s3Client.listObjectsV2(listRequest);

        if (response.contents().isEmpty()) {
            throw new ParkingEasyException(ImageErrorCode.IMAGE_NOT_FOUND);
        }

        // 삭제할 객체 리스트 구성
        List<ObjectIdentifier> objectToDelete = response.contents().stream()
                .map(s3Object -> ObjectIdentifier.builder().key(s3Object.key())
                        .build()).toList();

        // S3 객체 삭제 요청
        DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                .bucket(bucket)
                .delete(Delete.builder().objects(objectToDelete).build())
                .build();

        s3Client.deleteObjects(deleteRequest);

    }


    private void uploadToS3(MultipartFile file, String fileName) {

        try{
            // S3에 파일 업로드 요청 생성
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            // S3에 파일 업로드
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (SdkClientException e) {
            log.error("[S3 Upload Error] 클라이언트 설정 오류 - {}", e.getMessage(), e);
            throw new ParkingEasyException(ImageErrorCode.IMAGE_UPLOAD_FAIL);

        } catch (S3Exception e) {
            log.error("[S3 Upload Error] S3 업로드 실패 - {}", e.getMessage(), e);
            throw new ParkingEasyException(ImageErrorCode.IMAGE_UPLOAD_FAIL);

        } catch (IOException e) {
            log.error("[IOException] 파일 스트림 처리 중 실패: {}", e.getMessage(), e);
            throw new ParkingEasyException(ImageErrorCode.IMAGE_UPLOAD_FAIL);
        }

    }

    private String createS3Key(String targetType, Long targetId, LocalDateTime now) {

        String uuidFileName = String.format("%s_%s", UUID.randomUUID(), now);
        return String.format("%s/%d/%s", targetType, targetId, uuidFileName);
    }


    private String getPublicUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
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
