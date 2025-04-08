package com.parkez.common.image;

import com.parkez.common.image.dto.ImageDeleteRequest;
import com.parkez.common.image.dto.ImageUploadRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {

    List<String> upload(ImageUploadRequest request, List<MultipartFile> files);
    List<String> update(ImageUploadRequest request, List<MultipartFile> files);
    void delete(ImageDeleteRequest request);
}
