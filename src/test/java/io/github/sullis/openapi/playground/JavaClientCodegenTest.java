package io.github.sullis.openapi.playground;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openapitools.codegen.ClientOptInput;
import org.openapitools.codegen.DefaultGenerator;
import org.openapitools.codegen.languages.JavaClientCodegen;

import static org.assertj.core.api.Assertions.assertThat;


public class JavaClientCodegenTest {
  private static final List<String> CLIENT_LIBRARY_NAMES = List.of(
      JavaClientCodegen.NATIVE
  );

  private static final Map<String, String> OPENAPI_SPECS = Map.of(
    "cloudflare", "https://raw.githubusercontent.com/cloudflare/api-schemas/main/openapi.json"
  );

  static private Stream<Arguments> targets() {
    List<Arguments> argumentsList = new ArrayList<>();
    for (String openapiSpecName : OPENAPI_SPECS.keySet()) {
      final String openapiSpecUrl = OPENAPI_SPECS.get(openapiSpecName);
      for (String clientLibrary : CLIENT_LIBRARY_NAMES) {
        argumentsList.add(Arguments.of(clientLibrary, openapiSpecName, openapiSpecUrl));
      }
    }
    return argumentsList.stream();
  }

  @ParameterizedTest
  @MethodSource("targets")
  void generateJavaClient(final String codegenLibrary, final String openapiSpecName, final String url) {
    final String testClass = this.getClass().getSimpleName();
    final File outputDir = new File("./target/" + testClass + "-" + openapiSpecName + "-" + codegenLibrary + "-" + System.currentTimeMillis());
    outputDir.mkdirs();
    JavaClientCodegen codegen = new JavaClientCodegen();
    codegen.setDoNotUseRx(true);
    codegen.setLibrary(codegenLibrary);
    codegen.setDateLibrary("java8");
    codegen.setOutputDir(outputDir.toString());
    ParseOptions parseOpts = new ParseOptions();
    parseOpts.setResolveFully(true);
    OpenAPIParser parser = new OpenAPIParser();
    SwaggerParseResult result = parser.readLocation(url, Collections.emptyList(), parseOpts);
    OpenAPI openapi = result.getOpenAPI();

    assertThat(openapi).isNotNull();

    DefaultGenerator generator = new DefaultGenerator();

    final String basePackage = "codegen." + openapiSpecName.toLowerCase() + ".client";
    codegen.setModelPackage(basePackage + ".model");
    codegen.setApiPackage(basePackage + ".api");
    codegen.setInvokerPackage(basePackage);

    ClientOptInput generatorInput = new ClientOptInput();
    generatorInput.openAPI(openapi).config(codegen);

    List<File> generatedFiles = generator.opts(generatorInput).generate();
    for (File f : generatedFiles) {
      if (f.isFile() && f.getName().endsWith(".java")) {
        assertThat(f).isNotEmpty();
      }
    }
  }
}
