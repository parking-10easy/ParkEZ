package com.parkez.image.service;


import com.parkez.common.exception.ParkingEasyException;
import com.parkez.image.config.S3Properties;
import com.parkez.image.dto.request.ImageRequest;
import com.parkez.image.dto.response.ImageUrlResponse;
import com.parkez.image.enums.ImageTargetType;
import com.parkez.image.exception.ImageErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class S3ImageServiceTest {

    private S3ImageService imageService;

    @Mock
    private S3Client s3Client;

    @BeforeEach
    void setUp() {
        S3Properties s3Properties = new S3Properties(
                "ap-northeast-2",
                "dummy-access-key",
                "dummy-secret-key",
                "test-bucket"
        );

        imageService = new S3ImageService(s3Client, s3Properties);
    }

    @Nested
    class imageUpload {
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
        void 이미지_업로드_시_파일_개수_5개_초과하면_EXCEED_IMAGE_UPLOAD_LIMIT_예외_발생(){
            ImageRequest request = ImageRequest.builder()
                    .targetType(ImageTargetType.USER_PROFILE)
                    .targetId(1L)
                    .build();

            List<MultipartFile> files = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                MultipartFile mockFile = new MockMultipartFile(
                        "file" + i,
                        "test-image-" + i + ".jpg",
                        "image/jpeg",
                        ("image-content-" + i).getBytes()
                );
                files.add(mockFile);
            }

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> imageService.upload(request, files));

            assertEquals(ImageErrorCode.EXCEED_IMAGE_UPLOAD_LIMIT, exception.getErrorCode());
        }

        @Test
        void 파일명이_null이면_FILENAME_IS_NULL_예외_발생() {
            // given
            MultipartFile file = mock(MultipartFile.class);
            when(file.getOriginalFilename()).thenReturn(null);

            ImageRequest request = ImageRequest.builder()
                    .targetType(ImageTargetType.USER_PROFILE)
                    .targetId(1L)
                    .build();

            List<MultipartFile> files = List.of(file);

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> imageService.upload(request, files));

            assertEquals(ImageErrorCode.FILENAME_IS_NULL, exception.getErrorCode());
        }

    }

    @Nested
    class imageReplace {
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
    }

    @Nested
    class imageDelete {
        @Test
        void 이미지_삭제_성공하면_S3에서_객체_제거() {
            // given
            ImageRequest request = ImageRequest.builder()
                    .targetType(ImageTargetType.USER_PROFILE)
                    .targetId(1L)
                    .build();

            List<S3Object> mockObjects = List.of(
                    S3Object.builder().key("USER_PROFILE/1/file.jpg").build()
            );

            ListObjectsV2Response mockResult = ListObjectsV2Response.builder()
                    .contents(mockObjects)
                    .build();

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

            ListObjectsV2Response mockResult = ListObjectsV2Response.builder()
                    .contents(Collections.emptyList())
                    .build();
            //when
            when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                    .thenReturn(mockResult);

            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> imageService.delete(request));

            // then
            assertEquals(ImageErrorCode.IMAGE_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Nested
    class uploadToS3 {

        @Test
        void 이미지_업로드_중_IOException_발생시_IMAGE_UPLOAD_FAIL_예외_발생() throws IOException {
            // given
            ImageRequest request = ImageRequest.builder()
                    .targetType(ImageTargetType.USER_PROFILE)
                    .targetId(1L)
                    .build();

            MultipartFile file = mock(MultipartFile.class);
            when(file.getOriginalFilename()).thenReturn("test.png");
            when(file.getSize()).thenReturn(123L);
            when(file.getInputStream()).thenThrow(new IOException("강제로 발생시킨 예외"));

            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> imageService.upload(request, List.of(file)));

            assertEquals(ImageErrorCode.IMAGE_UPLOAD_FAIL, exception.getErrorCode());
        }

        @Test
        void 이미지_업로드_중_S3Exception_발생시_IMAGE_UPLOAD_FAIL_예외_발생() throws IOException {
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

            doThrow(S3Exception.builder().message("S3 연결 오류").build())
                    .when(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));


            // when & then
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

            doThrow(SdkClientException.builder().message("Sdk 에러").build())
                    .when(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));


            // when & then
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> imageService.upload(request, files));

            assertEquals(ImageErrorCode.IMAGE_UPLOAD_FAIL, exception.getErrorCode());
        }

    }


    @Nested
    class extractFileExtension{
        @Test
        void 파일명이_마침표로_끝날_경우_INVALID_EXTENSION_TYPE_예외_발생(){

            // given
            MultipartFile file = new MockMultipartFile("file", "filename.", "image/png", "data".getBytes());

            // when
            ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                    () -> ReflectionTestUtils.invokeMethod(imageService, "extractFileExtension", file.getOriginalFilename()));

            // then
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
    }



    @Nested
    class extractContentType{
        @Test
        void contentType_추출중_IOException_발생시_IMAGE_UPLOAD_FAIL_예외_발생(){
            // given
            ImageRequest request = ImageRequest.builder()
                    .targetType(ImageTargetType.USER_PROFILE)
                    .targetId(1L)
                    .build();

            MultipartFile file = mock(MultipartFile.class);
            when(file.getOriginalFilename()).thenReturn("image.png");

            // Files.probeContentType(path) mocking
            try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
                mockedFiles.when(() -> Files.probeContentType(any(Path.class)))
                        .thenThrow(new IOException("테스트용 예외"));

                // when & then
                ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                        () -> imageService.upload(request, List.of(file)));

                assertEquals(ImageErrorCode.IMAGE_UPLOAD_FAIL, exception.getErrorCode());
            }
        }
    }


}
