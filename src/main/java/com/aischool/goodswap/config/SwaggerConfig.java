package com.aischool.goodswap.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// JWT 사용시 아래의 폼으로 변경해야함
@Configuration
public class SwaggerConfig {
  @Bean
  public OpenAPI openAPI() {
    String jwt = "JWT";
    SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt);
    Components components = new Components().addSecuritySchemes(jwt, new SecurityScheme()
      .name(jwt)
      .type(SecurityScheme.Type.HTTP)
      .scheme("bearer")
      .bearerFormat("JWT")
    );
    return new OpenAPI()
      .components(new Components())
      .info(apiInfo())
      .addSecurityItem(securityRequirement)
      .components(components);
  }
  private Info apiInfo() {
    return new Info()
      .title("GoodSwap") // API의 제목
      .description("GoodSwap의 Swagger UI입니다") // API에 대한 설명
      .version("2.0.0"); // API의 버전
  }
}