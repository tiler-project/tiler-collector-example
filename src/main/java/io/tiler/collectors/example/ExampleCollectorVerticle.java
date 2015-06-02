package io.tiler.collectors.example;

import io.tiler.core.BaseCollectorVerticle;
import io.tiler.collectors.example.config.Config;
import io.tiler.collectors.example.config.ConfigFactory;
import io.tiler.collectors.example.config.Metric;
import io.tiler.collectors.example.config.RandomIntField;
import io.tiler.core.json.JsonArrayIterable;
import org.simondean.vertx.async.Async;
import org.simondean.vertx.async.DefaultAsyncResult;
import org.simondean.vertx.async.ObjectWrapper;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;

import java.util.*;

public class ExampleCollectorVerticle extends BaseCollectorVerticle {
  private Logger logger;
  private Config config;
  private Random random;

  public void start() {
    logger = container.logger();
    config = new ConfigFactory().load(container.config());
    random = new Random();

    Async.iterable(config.metrics())
      .each((metricConfig, handler) -> {
        String metricName = config.getFullMetricName(metricConfig.name());
        logger.info("Configuring collection for metric '" + metricName + "'");
        final ObjectWrapper<Boolean> isRunning = new ObjectWrapper<>(true);

        vertx.runOnContext(aVoid -> collect(metricName, metricConfig, result -> isRunning.setObject(false)));

        vertx.setPeriodic(metricConfig.collectionIntervalInMilliseconds(), timerID -> {
          if (isRunning.getObject()) {
            logger.warn("Collection aborted as previous run still executing");
            return;
          }

          isRunning.setObject(true);

          collect(metricName, metricConfig, result -> isRunning.setObject(false));
        });
      })
      .run(vertx, result -> logger.info("ExampleCollectorVerticle started"));
  }

  private void collect(String metricName, Metric metricConfig, AsyncResultHandler<Void> handler) {
    long timeInMicroseconds = currentTimeInMicroseconds();
    logger.info("Collection started for metric '" + metricName + "'");

    Async.waterfall()
      .<JsonObject>task(taskHandler -> getExistingMetric(timeInMicroseconds, metricConfig, metricName, taskHandler))
      .<JsonObject>task((existingMetric, taskHandler) -> getMetric(timeInMicroseconds, metricConfig, metricName, existingMetric, taskHandler))
      .<Void>task((metric, taskHandler) -> {
        saveMetrics(new JsonArray().addObject(metric));
        taskHandler.handle(DefaultAsyncResult.succeed());
      })
      .run(result -> {
        if (result.failed()) {
          logger.error("Collection failed");
          handler.handle(DefaultAsyncResult.fail(result));
          return;
        }

        logger.info("Collection finished");
        handler.handle(DefaultAsyncResult.succeed());
      });
  }

  private void getExistingMetric(long timeInMicroseconds, Metric metricConfig, String metricName, AsyncResultHandler<JsonObject> handler) {
    getExistingMetrics(Arrays.asList(metricName), result -> {
      if (result.failed()) {
        handler.handle(DefaultAsyncResult.fail(result));
        return;
      }

      JsonArray existingMetrics = result.result();

      for (JsonObject metric : new JsonArrayIterable<JsonObject>(existingMetrics)) {
        if (metric.getString("name").equals(metricName)) {
          applyRetentionPeriod(timeInMicroseconds, metric, metricConfig);
          handler.handle(DefaultAsyncResult.succeed(metric));
          return;
        }
      }

      handler.handle(DefaultAsyncResult.succeed(null));
    });
  }

  private void applyRetentionPeriod(long timeInMicroseconds, JsonObject metric, Metric metricConfig) {
    long retainFromTimeInMicroseconds = timeInMicroseconds - metricConfig.retentionPeriodInMicroseconds();

    for (Iterator<JsonObject> iterator = new JsonArrayIterable<JsonObject>(metric.getArray("points")).iterator(); iterator.hasNext();) {
      JsonObject point = iterator.next();

      if (point.getLong("time") < retainFromTimeInMicroseconds) {
        iterator.remove();
      }
    }
  }

  private void getMetric(long timeInMicroseconds, Metric metricConfig, String metricName, JsonObject metric, AsyncResultHandler<JsonObject> handler) {
    if (metric == null) {
      metric = new JsonObject()
        .putString("name", metricName)
        .putArray("points", new JsonArray());
    }

    final JsonArray points = metric.getArray("points");

    metricConfig.groups().forEach(group -> {
      JsonObject point = group.copy()
        .putNumber("time", timeInMicroseconds);

      metricConfig.fields().forEach(fieldConfig -> {
        if (fieldConfig instanceof RandomIntField) {
          RandomIntField randomIntFieldConfig = (RandomIntField) fieldConfig;
          point.putNumber(fieldConfig.name(), generateRandomInt(randomIntFieldConfig.min(), randomIntFieldConfig.max()));
        } else {
          logger.error("Unsupported field config type '" + fieldConfig.getClass().getName() + "'");
        }
      });

      points.addObject(point);
    });

    handler.handle(DefaultAsyncResult.succeed(metric));
  }

  private int generateRandomInt(int min, int max) {
    return random.nextInt(max - min + 1) + min;
  }
}
