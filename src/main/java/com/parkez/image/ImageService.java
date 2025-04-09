package com.parkez.image;

import com.parkez.image.dto.request.ImageRequest;
import com.parkez.image.dto.response.ImageUrlResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {

    ImageUrlResponse upload(ImageRequest request, List<MultipartFile> files);
    ImageUrlResponse replace(ImageRequest request, List<MultipartFile> files);
    void delete(ImageRequest request);
}
