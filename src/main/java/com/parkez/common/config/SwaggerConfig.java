package com.parkez.common.config;

import java.util.Collections;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;

@Configuration
@OpenAPIDefinition(
	info = @Info(
		title = "주차10조",
		description = "최종 프로젝트",
		version = "1.0.0"
	)
)
@RequiredArgsConstructor
public class SwaggerConfig {

	@Bean
	public GroupedOpenApi publicApi() {
		String[] packages = {"com.parkez"};
		return GroupedOpenApi.builder()
			.group("default")
			.packagesToScan(packages)
			.build();
	}

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
			.components(new Components()
				.addSecuritySchemes("JWT", new SecurityScheme()
					.type(SecurityScheme.Type.HTTP)
					.scheme("bearer")
					.bearerFormat("JWT")
					.in(SecurityScheme.In.HEADER)
					.name("Authorization")
				)
			)
			.security(Collections.singletonList(new SecurityRequirement().addList("JWT")));
	}
}
