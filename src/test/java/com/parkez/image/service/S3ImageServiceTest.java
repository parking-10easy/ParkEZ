package com.parkez.image.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.image.ImageService;
import com.parkez.image.S3ImageService;
import com.parkez.image.dto.request.ImageRequest;
import com.parkez.image.dto.response.ImageUrlResponse;
import com.parkez.image.enums.ImageTargetType;
import com.parkez.image.exception.ImageErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class S3ImageServiceTest {

    @InjectMocks
    private S3ImageService imageService;

    @Mock
    private AmazonS3Client s3Client;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(imageService, "bucket", "test-bucket");
    }

    @Test
    void 이미지_업로드_성공하면_URL_리스트_반환(){
        // given
        ImageRequest request = ImageRequest.builder()
                .targetType(ImageTargetType.USER_PROFILE)
                .targetId(1L)
                .build();

        MultipartFile mockFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "image-content".getBytes()
        );

        List<MultipartFile> files = List.of(mockFile);

        // when
        ImageUrlResponse urlResponse = imageService.upload(request, files);

        // then
        assertEquals(1, urlResponse.getUrls().size());
        assertTrue(urlResponse.getUrls().get(0).startsWith("https://test-bucket.s3."));
    }

    @Test
    void 이미지_삭제_성공하면_S3에서_객체_제거() {
        // given
        ImageRequest request = ImageRequest.builder()
                .targetType(ImageTargetType.USER_PROFILE)
                .targetId(1L)
                .build();

        String prefix = "USER_PROFILE/1/";

        S3ObjectSummary summary = new S3ObjectSummary();
        summary.setKey(prefix + "file.jpg");

        ListObjectsV2Result mockResult = new ListObjectsV2Result();
        mockResult.getObjectSummaries().add(summary);

        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenReturn(mockResult);

        // when
        imageService.delete(request);

        // then
        verify(s3Client).deleteObjects(any(DeleteObjectsRequest.class));
    }

    @Test
    void 삭제할_이미지가_없으면_IMAGE_NOT_FOUND_예외_발생() {
        // given
        ImageRequest request = ImageRequest.builder()
                .targetType(ImageTargetType.USER_PROFILE)
                .targetId(1L)
                .build();

        ListObjectsV2Result mockResult = new ListObjectsV2Result(); // 비어 있음

        //when
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenReturn(mockResult);

        ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                () -> imageService.delete(request));

        // then
        assertEquals(ImageErrorCode.IMAGE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void 이미지_업데이트_시_기존_이미지_삭제_후_업로드() {
        // given
        ImageRequest request = ImageRequest.builder()
                .targetType(ImageTargetType.USER_PROFILE)
                .targetId(1L)
                .build();

        MultipartFile file = new MockMultipartFile("file", "filename.png", "image/png", "test data".getBytes());
        List<MultipartFile> files = List.of(file);

        ImageService spyService = Mockito.spy(imageService);

        // when
        doReturn(ImageUrlResponse.builder()
                .urls(List.of("https://example.com/test.png"))
                .build())
                .when(spyService).upload(any(), any());

        doNothing().when(spyService).delete(any());

        ImageUrlResponse replaceUrls = spyService.replace(request, files);

        // then
        verify(spyService, times(1)).delete(eq(request));
        verify(spyService, times(1)).upload(eq(request), eq(files));
        assertEquals(1, replaceUrls.getUrls().size());

    }

    @Test
    void 이미지_업로드_중_IOException_발생시_IMAGE_UPLOAD_FAIL_예외_발생() throws IOException {
        // given
        ImageRequest request = ImageRequest.builder()
                .targetType(ImageTargetType.USER_PROFILE)
                .targetId(1L)
                .build();

        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.png");
        when(file.getContentType()).thenReturn("image/png");
        when(file.getSize()).thenReturn(123L);
        when(file.getInputStream()).thenThrow(new IOException("강제로 발생시킨 예외"));

        // when & then
        ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                () -> imageService.upload(request, List.of(file)));

        assertEquals(ImageErrorCode.IMAGE_UPLOAD_FAIL, exception.getErrorCode());
    }

    @Test
    void 이미지_업로드_중_AmazonServiceException_발생시_IMAGE_UPLOAD_FAIL_예외_발생() throws IOException {
        // given

        ImageRequest request = ImageRequest.builder()
                .targetType(ImageTargetType.USER_PROFILE)
                .targetId(1L)
                .build();

        MultipartFile file = new MockMultipartFile(
                "file",
                "test-image.png",
                "image/png",
                "image-content".getBytes()
        );

        List<MultipartFile> files = List.of(file);

        // when
        AmazonServiceException ase = new AmazonServiceException("S3 오류");
        ase.setStatusCode(500);
        ase.setErrorCode("InternalError");
        doThrow(ase).when(s3Client).putObject(any(PutObjectRequest.class));

        // then
        ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                () -> imageService.upload(request, files));

        assertEquals(ImageErrorCode.IMAGE_UPLOAD_FAIL, exception.getErrorCode());
    }

    @Test
    void 이미지_업로드_중_SdkClientException_발생시_IMAGE_UPLOAD_FAIL_예외_발생() throws IOException {
        // given
        ImageRequest request = ImageRequest.builder()
                .targetType(ImageTargetType.USER_PROFILE)
                .targetId(1L)
                .build();

        MultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "image-content".getBytes()
        );
        List<MultipartFile> files = List.of(file);

        doThrow(new SdkClientException("Sdk 예외")).when(s3Client).putObject(any(PutObjectRequest.class));

        // when & then
        ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                () -> imageService.upload(request, files));

        assertEquals(ImageErrorCode.IMAGE_UPLOAD_FAIL, exception.getErrorCode());
    }


    @Test
    void 파일_리스트가_null이면_IMAGE_IS_NULL_예외_발생(){
        // given
        ImageRequest request = ImageRequest.builder()
                .targetType(ImageTargetType.USER_PROFILE)
                .targetId(1L)
                .build();

        // when
        ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                () -> imageService.upload(request, null));

        // then
        assertEquals(ImageErrorCode.IMAGE_IS_NULL, exception.getErrorCode());
    }

    @Test
    void 허용되지_않은_파일_확장자일_경우_INVALID_EXTENSION_TYPE_예외_발생(){
        // given
        MultipartFile file = new MockMultipartFile("file", "filename.mmm", "application/octet-stream", "virus".getBytes());

        ImageRequest request = ImageRequest.builder()
                .targetType(ImageTargetType.USER_PROFILE)
                .targetId(1L)
                .build();

        List<MultipartFile> files = List.of(file);

        // when
        ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                () -> imageService.upload(request, files));

        //then
        assertEquals(ImageErrorCode.INVALID_EXTENSION_TYPE, exception.getErrorCode());
    }

    @Test
    void 파일_확장자가_없을_경우_INVALID_EXTENSION_TYPE_예외_발생(){

        // given
        MultipartFile file = new MockMultipartFile("file", "filename", "image/png", "data".getBytes());

        // when
        ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                () -> ReflectionTestUtils.invokeMethod(imageService, "extractFileExtension", file.getOriginalFilename()));

        // then
        assertEquals(ImageErrorCode.INVALID_EXTENSION_TYPE, exception.getErrorCode());

    }

    @Test
    void 파일명이_마침표로_끝날_경우_빈문자열_반환(){

        // given
        MultipartFile file = new MockMultipartFile("file", "filename.", "image/png", "data".getBytes());

        // when
        ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                () -> ReflectionTestUtils.invokeMethod(imageService, "extractFileExtension", file.getOriginalFilename()));

        // then
        assertEquals(ImageErrorCode.INVALID_EXTENSION_TYPE, exception.getErrorCode());

    }

    @Test
    void 파일명이_null이면_IMAGE_IS_NULL_예외_발생() {
        // given
        String fileName = null;

        // when & then
        ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                () -> {
                    ReflectionTestUtils.invokeMethod(imageService, "extractFileExtension", fileName);
                });

        assertEquals(ImageErrorCode.IMAGE_IS_NULL, exception.getErrorCode());
    }



}
