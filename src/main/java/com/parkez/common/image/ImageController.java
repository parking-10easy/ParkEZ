package com.parkez.common.image;

import com.parkez.common.image.dto.ImageDeleteRequest;
import com.parkez.common.image.dto.ImageUploadRequest;
import com.parkez.common.response.Response;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
@Tag(name = "이미지 업로드 및 삭제 API", description = "AWS S3와 연동된 .")
public class ImageController {

    private final S3ImageService imageService;

    @PostMapping
    public Response<List<String>> upload(
            @RequestPart("request") ImageUploadRequest request,
            @RequestPart("images") List<MultipartFile> imageFiles) {

        List<String> urls = imageService.upload(request, imageFiles);
        return Response.of(urls);
    }

    @PutMapping
    public Response<List<String>> update(
            @RequestPart("request") ImageUploadRequest request,
            @RequestPart("images") List<MultipartFile> imageFiles
    ){
        List<String> updatedUrls = imageService.update(request, imageFiles);
        return Response.of(updatedUrls);
    }

    @DeleteMapping
    public Response<Void> deleteImages(
            @RequestBody ImageDeleteRequest request
    ) {
        imageService.delete(request);
        return Response.empty();
    }

}
