package io.tiler.collectors.example.config;

public class RandomIntField extends Field {
  private final int min;
  private final int max;

  public RandomIntField(String name, Integer min, Integer max) {
    super(name);

    if (min == null) {
      min = 0;
    }

    if (max == null) {
      max = 0;
    }

    this.min = min;
    this.max = max;
  }

  public int min() {
    return min;
  }

  public int max() {
    return max;
  }
}
