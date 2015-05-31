package io.tiler.collectors.example.config;

import io.tiler.time.TimePeriodParser;

public class Config {
  private final long collectionIntervalInMilliseconds;
  private final String metricNamePrefix;

  public Config(String collectionInterval, String metricNamePrefix) {
    if (collectionInterval == null) {
      collectionInterval = "10s";
    }

    if (metricNamePrefix == null) {
      metricNamePrefix = "examples.";
    }

    this.collectionIntervalInMilliseconds = TimePeriodParser.parseTimePeriodToMilliseconds(collectionInterval);
    this.metricNamePrefix = metricNamePrefix;
  }

  public long collectionIntervalInMilliseconds() {
    return collectionIntervalInMilliseconds;
  }

  public String metricNamePrefix() {
    return metricNamePrefix;
  }

  public String getFullMetricName(String metricName) {
    return metricNamePrefix() + metricName;
  }
}
