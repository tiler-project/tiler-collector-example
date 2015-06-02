package io.tiler.collectors.example.config;

import java.util.List;

public class Config {
  private final String metricNamePrefix;
  private final List<Metric> metrics;

  public Config(String metricNamePrefix, List<Metric> metrics) {
    if (metricNamePrefix == null) {
      metricNamePrefix = "examples.";
    }

    this.metricNamePrefix = metricNamePrefix;
    this.metrics = metrics;
  }

  public String metricNamePrefix() {
    return metricNamePrefix;
  }

  public List<Metric> metrics() {
    return metrics;
  }

  public String getFullMetricName(String metricName) {
    return metricNamePrefix() + metricName;
  }
}
