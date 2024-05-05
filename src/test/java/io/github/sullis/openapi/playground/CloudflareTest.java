package io.github.sullis.openapi.playground;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.io.File;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.codegen.DefaultGenerator;
import org.openapitools.codegen.languages.JavaClientCodegen;

import static org.assertj.core.api.Assertions.assertThat;


public class CloudflareTest {
  private JavaClientCodegen codegen;

  @BeforeEach
  void beforeEachTest() {
    codegen = new JavaClientCodegen();
    codegen.setUseGzipFeature(true);
    codegen.setDoNotUseRx(true);
    // codegen.setLibrary("foobar");
    codegen.setDateLibrary("java8");
  }

  @Test
  void generateJavaClient() {
    final String url = "https://raw.githubusercontent.com/cloudflare/api-schemas/main/openapi.yaml";
    ParseOptions parseOpts = new ParseOptions();
    parseOpts.setResolveFully(true);
    OpenAPIParser parser = new OpenAPIParser();
    SwaggerParseResult result = parser.readLocation(url, Collections.emptyList(), parseOpts);
    assertThat(result.getMessages()).isEmpty();

    DefaultGenerator generator = new DefaultGenerator();
    List<File> generatedFiles = generator.generate();
    for (File f : generatedFiles) {
      System.out.println(f);
    }
  }
}
