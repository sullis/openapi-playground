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
import org.junit.jupiter.api.TestInfo;
import org.openapitools.codegen.ClientOptInput;
import org.openapitools.codegen.DefaultGenerator;
import org.openapitools.codegen.languages.JavaClientCodegen;

import static org.assertj.core.api.Assertions.assertThat;


public class JavaClientCodegenTest {
  private JavaClientCodegen codegen;

  @BeforeEach
  void beforeEachTest(TestInfo testInfo) {
    final String testClass = testInfo.getTestClass().get().getSimpleName();
    final String testMethod = testInfo.getTestMethod().get().getName();
    final File outputDir = new File("./target/" + testClass + "-" + testMethod + "-" + System.currentTimeMillis());
    outputDir.mkdirs();
    codegen = new JavaClientCodegen();
    codegen.setUseGzipFeature(true);
    codegen.setDoNotUseRx(true);
    codegen.setLibrary(JavaClientCodegen.APACHE);
    codegen.setDateLibrary("java8");
    codegen.setOutputDir(outputDir.toString());
  }

  @Test
  void generateCloudflareClient() {
    generateJavaClient("https://raw.githubusercontent.com/cloudflare/api-schemas/main/openapi.json");
  }

  private void generateJavaClient(final String url) {
    ParseOptions parseOpts = new ParseOptions();
    parseOpts.setResolveFully(true);
    OpenAPIParser parser = new OpenAPIParser();
    SwaggerParseResult result = parser.readLocation(url, Collections.emptyList(), parseOpts);
    OpenAPI openapi = result.getOpenAPI();

    assertThat(openapi).isNotNull();

    DefaultGenerator generator = new DefaultGenerator();

    codegen.setModelPackage("foobarxyz");
    codegen.setApiPackage("foobarxyz");
    codegen.setInvokerPackage("foobarxyz");

    ClientOptInput generatorInput = new ClientOptInput();
    generatorInput.openAPI(openapi).config(codegen);

    List<File> generatedFiles = generator.opts(generatorInput).generate();
    for (File f : generatedFiles) {
      System.out.println(f);
    }
  }
}
