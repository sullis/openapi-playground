package io.github.sullis.openapi.playground;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
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

  @Test
  void hubspotApi() throws Exception {
    String url = "https://api.hubspot.com/api-catalog-public/v1/apis";
    HttpClient client = HttpClient.newBuilder().build();
    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().header("Accept", "application/json").build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertThat(response.statusCode()).isEqualTo(200);
    String body = response.body();
    assertThat(body).startsWith("{");
    ObjectMapper mapper = new ObjectMapper();
    ObjectReader reader = mapper.reader();
    HubspotApiCatalog catalog = reader.readValue(body, HubspotApiCatalog.class);
    assertThat(catalog.results()).isNotEmpty();
    for (HubspotApiInfo info : catalog.results()) {
      System.out.println(info.name() + " " + info.features());
//        generateJavaClient("native", info.name(), url);
    }
  }
}