package com.parkez.image;

import com.parkez.image.dto.request.ImageRequest;
import com.parkez.image.dto.response.ImageUrlResponse;
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
@Tag(name = "이미지 업로드/수정/삭제 API", description = "AWS S3와 연동된 이미지 업로드/수정/삭제 기능입니다.")
public class ImageController {

    private final ImageService imageService;

    @PostMapping
    @Operation(summary = "이미지 업로드", description = "targetType과 targetId로 구분된 폴더에 s3 객체가 생성됩니다.")
    public Response<ImageUrlResponse> upload(
            @RequestPart("request") ImageRequest request,
            @RequestPart("images") List<MultipartFile> imageFiles) {

        return Response.of(imageService.upload(request, imageFiles));

    }

    @PutMapping
    @Operation(summary = "이미지 수정", description = "기존 객체를 지우고 새로운 객체로 업데이트합니다.")
    public Response<ImageUrlResponse> update(
            @RequestPart("request") ImageRequest request,
            @RequestPart("images") List<MultipartFile> imageFiles
    ){
        ;
        return Response.of(imageService.replace(request, imageFiles));
    }

    @DeleteMapping
    @Operation(summary = "이미지 삭제", description = "s3 객체를 삭제합니다.")
    public Response<Void> deleteImages(
            @RequestBody ImageRequest request
    ) {
        imageService.delete(request);
        return Response.empty();
    }

}
