package com.parkez.common.image;

import com.parkez.common.image.dto.request.ImageDeleteRequest;
import com.parkez.common.image.dto.request.ImageUploadRequest;
import com.parkez.common.image.dto.response.ImageUrlResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {

    ImageUrlResponse upload(ImageUploadRequest request, List<MultipartFile> files);
    ImageUrlResponse update(ImageUploadRequest request, List<MultipartFile> files);
    void delete(ImageDeleteRequest request);
}
