package com.parkez.image.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws")
@Getter
@RequiredArgsConstructor
public class S3Properties {

    private final String region;
    private final String accessKey;
    private final String secretKey;
    private final String s3Bucket;


}