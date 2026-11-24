
package com.unla.ghsicilianotfi.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI epicentroGourmetOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Epicentro Gourmet - API REST")
                        .version("Hito 3 - v1")
                        .description(
                                "Documentacion y pruebas de los endpoints REST "
                                        + "del Trabajo Practico de Orientacion a Objetos II."
                        ));
    }
}
