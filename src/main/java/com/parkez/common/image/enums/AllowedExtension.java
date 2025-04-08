package com.parkez.common.image.enums;

import lombok.Getter;

@Getter
public enum AllowedExtension {
    JPG("jpg"),
    JPEG("jpeg"),
    PNG("png");

    private final String extension;

    AllowedExtension(String extension) {
        this.extension = extension;
    }

    public static boolean isAllowedExtension(String extension) {
        for (AllowedExtension allowedExtension : AllowedExtension.values()) {
            if(allowedExtension.getExtension().equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

}
