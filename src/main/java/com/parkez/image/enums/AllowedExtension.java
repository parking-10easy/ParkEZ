package com.parkez.image.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum AllowedExtension {
    JPG("jpg"),
    JPEG("jpeg"),
    PNG("png");

    private final String description;

    AllowedExtension(String description) {
        this.description = description;
    }

    public static boolean contains(String extension) {
        return Arrays.stream(values())
                .anyMatch(e -> e.description.equalsIgnoreCase(extension));
    }

}
