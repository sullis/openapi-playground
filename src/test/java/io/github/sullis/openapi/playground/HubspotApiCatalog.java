package io.github.sullis.openapi.playground;

import java.util.List;
import java.util.Map;

public record HubspotApiCatalog(List<HubspotApiInfo> results) {
}

record HubspotApiInfo(String name, Map<String, Map<String, String>> features) {

}