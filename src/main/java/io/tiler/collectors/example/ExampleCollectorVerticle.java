package io.tiler.collectors.example;

import io.tiler.BaseCollectorVerticle;
import io.tiler.collectors.example.config.Config;
import io.tiler.collectors.example.config.ConfigFactory;
import io.tiler.json.JsonArrayIterable;
import org.simondean.vertx.async.Async;
import org.simondean.vertx.async.DefaultAsyncResult;
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

    final boolean[] isRunning = {true};

    collect(aVoid -> isRunning[0] = false);

    vertx.setPeriodic(config.collectionIntervalInMilliseconds(), aLong -> {
      if (isRunning[0]) {
        logger.info("Collection aborted as previous run still executing");
        return;
      }

      isRunning[0] = true;

      collect(aVoid -> isRunning[0] = false);
    });

    logger.info("ExampleCollectorVerticle started");
  }

  private void collect(AsyncResultHandler<Void> handler) {
    logger.info("Collection started");
    Async.waterfall()
      .<HashMap<String, JsonObject>>task(taskHandler -> getExistingMetrics(taskHandler))
      .<JsonArray>task((existingMetrics, taskHandler) -> getMetrics(existingMetrics, taskHandler))
      .<Void>task((metrics, taskHandler) -> {
        saveMetrics(metrics);
        taskHandler.handle(DefaultAsyncResult.succeed());
      })
      .run(result -> {
        if (result.failed()) {
          handler.handle(DefaultAsyncResult.fail(result));
          return;
        }

        logger.info("Collection finished");
        handler.handle(null);
      });
  }

  private void getExistingMetrics(AsyncResultHandler<HashMap<String, JsonObject>> handler) {
    List<String> metricNames = getMetricNames();

    getExistingMetrics(metricNames, result -> {
      if (result.failed()) {
        handler.handle(DefaultAsyncResult.fail(result));
        return;
      }

      JsonArray existingMetrics = result.result();
      HashMap<String, JsonObject> existingMetricMap = new HashMap<>();

      for (JsonObject metric : new JsonArrayIterable<JsonObject>(existingMetrics)) {
        existingMetricMap.put(metric.getString("name"), metric);
      }

      handler.handle(DefaultAsyncResult.succeed(existingMetricMap));
    });
  }

  private List<String> getMetricNames() {
    return Arrays.asList(config.getFullMetricName("1"));
  }

  private void getMetrics(HashMap<String, JsonObject> existingMetricMap, AsyncResultHandler<JsonArray> handler) {
    long timeInMicroseconds = currentTimeInMicroseconds();

    JsonArray metrics = new JsonArray()
      .addObject(getMetric1(timeInMicroseconds, existingMetricMap));

    handler.handle(DefaultAsyncResult.succeed(metrics));
  }

  private JsonObject getMetric1(long timeInMicroseconds, HashMap<String, JsonObject> existingMetricMap) {
    String metricName = config.getFullMetricName("1");
    JsonObject metric = existingMetricMap.get(metricName);

    if (metric == null) {
      metric = new JsonObject()
        .putString("name", metricName)
        .putArray("points", new JsonArray());
    }

    final JsonObject finalMetric = metric;

    Arrays.asList("one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten").forEach(name -> {
      finalMetric.getArray("points")
        .addObject(new JsonObject()
          .putNumber("time", timeInMicroseconds)
          .putString("name", name)
          .putNumber("value", random.nextInt(101)));
    });

    return metric;
  }
}
