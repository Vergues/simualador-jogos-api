package com.vergues.simuladorjogosapi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "API de Resultados do Brasileirão",
        version = "v1", description = "Consulta de partidas simuladas e recebimento de atualizações."))
public class OpenApiConfiguration { }
