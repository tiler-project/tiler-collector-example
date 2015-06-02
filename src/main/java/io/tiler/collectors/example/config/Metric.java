package io.tiler.collectors.example.config;

import io.tiler.core.time.TimePeriodParser;
import org.vertx.java.core.json.JsonObject;

import java.util.List;

public class Metric {
  private final String name;
  private final long collectionIntervalInMilliseconds;
  private final long retentionPeriodInMicroseconds;
  private final List<JsonObject> groups;
  private final List<Field> fields;

  public Metric(String name, String collectionInterval, String retentionPeriod, List<JsonObject> groups, List<Field> fields) {
    if (collectionInterval == null) {
      collectionInterval = "10s";
    }

    if (retentionPeriod == null) {
      retentionPeriod = "10m";
    }

    this.name = name;
    this.collectionIntervalInMilliseconds = TimePeriodParser.parseTimePeriodToMilliseconds(collectionInterval);
    this.retentionPeriodInMicroseconds = TimePeriodParser.parseTimePeriodToMicroseconds(retentionPeriod);
    this.groups = groups;
    this.fields = fields;
  }

  public String name() {
    return name;
  }

  public long collectionIntervalInMilliseconds() {
    return collectionIntervalInMilliseconds;
  }

  public long retentionPeriodInMicroseconds() {
    return retentionPeriodInMicroseconds;
  }

  public List<JsonObject> groups() {
    return groups;
  }

  public List<Field> fields() {
    return fields;
  }
}
