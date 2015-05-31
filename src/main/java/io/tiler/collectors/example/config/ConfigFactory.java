package io.tiler.collectors.example.config;

import org.vertx.java.core.json.JsonObject;

public class ConfigFactory {
  public Config load(JsonObject config) {
    return new Config(
      config.getString("collectionInterval"),
      config.getString("metricNamePrefix"));
  }
}
