package com.parkez.common.image;

import com.parkez.common.image.dto.ImageDeleteRequest;
import com.parkez.common.image.dto.ImageUploadRequest;
import com.parkez.common.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
@Tag(name = "이미지 업로드 및 삭제 API", description = "AWS S3와 연동된 이미지 업로드/수정/삭제 기능입니다.")
public class ImageController {

    private final ImageService imageService;

    @PostMapping
    @Operation(summary = "이미지 업로드", description = "targetType과 targetId로 구분된 폴더에 s3 객체가 생성됩니다.")
    public Response<List<String>> upload(
            @RequestPart("request") ImageUploadRequest request,
            @RequestPart("images") List<MultipartFile> imageFiles) {

        List<String> urls = imageService.upload(request, imageFiles);
        return Response.of(urls);
    }

    @PutMapping
    @Operation(summary = "이미지 수정", description = "기존 객체를 지우고 새로운 객체로 업데이트합니다.")
    public Response<List<String>> update(
            @RequestPart("request") ImageUploadRequest request,
            @RequestPart("images") List<MultipartFile> imageFiles
    ){
        List<String> updatedUrls = imageService.update(request, imageFiles);
        return Response.of(updatedUrls);
    }

    @DeleteMapping
    @Operation(summary = "이미지 삭제", description = "s3 객체를 삭제합니다.")
    public Response<Void> deleteImages(
            @RequestBody ImageDeleteRequest request
    ) {
        imageService.delete(request);
        return Response.empty();
    }

}
