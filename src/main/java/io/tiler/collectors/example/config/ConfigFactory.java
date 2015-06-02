package io.tiler.collectors.example.config;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class ConfigFactory {
  public Config load(JsonObject config) {
    return new Config(
      config.getString("metricNamePrefix"),
      getMetrics(config));
  }

  private List<Metric> getMetrics(JsonObject config) {
    JsonArray metrics = config.getArray("metrics");
    ArrayList<Metric> loadedMetrics = new ArrayList<>();

    if (metrics == null) {
      return loadedMetrics;
    }

    metrics.forEach(serverObject -> {
      JsonObject metric = (JsonObject) serverObject;
      loadedMetrics.add(getServer(metric));
    });

    return loadedMetrics;
  }

  private Metric getServer(JsonObject metric) {
    return new Metric(
      metric.getString("name"),
      metric.getString("collectionInterval"),
      metric.getString("retentionPeriod"),
      getGroups(metric.getArray("groups")),
      getFields(metric.getArray("fields")));
  }

  private List<JsonObject> getGroups(JsonArray groups) {
    if (groups == null) {
      return new ArrayList<>();
    }

    ArrayList<JsonObject> loadedGroups = new ArrayList<>();
    groups.forEach(groupObject -> loadedGroups.add((JsonObject) groupObject));
    return loadedGroups;
  }

  private List<Field> getFields(JsonArray fields) {
    if (fields == null) {
      return new ArrayList<>();
    }

    ArrayList<Field> loadedFields = new ArrayList<>();

    fields.forEach(fieldObject -> {
      JsonObject field = (JsonObject) fieldObject;

      if (field == null) {
        return;
      }

      String type = field.getString("type");

      if (type == null) {
        return;
      }

      Field loadedField;

      switch (type) {
        case "randomInt": {
          loadedField = new RandomIntField(
            field.getString("name"),
            field.getInteger("min"),
            field.getInteger("max"));
          break;
        }
        default:
          return;
      }

      loadedFields.add(loadedField);
    });

    return loadedFields;
  }
}
