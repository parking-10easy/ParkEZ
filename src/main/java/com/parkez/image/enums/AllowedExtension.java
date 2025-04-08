package com.parkez.image.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum AllowedExtension {
    JPG("jpg"),
    JPEG("jpeg"),
    PNG("png");

    private final String description;

    public static boolean contains(String extension) {
        return Arrays.stream(values())
                .anyMatch(e -> e.description.equalsIgnoreCase(extension));
    }

}
