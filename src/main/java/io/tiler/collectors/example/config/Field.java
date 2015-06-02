package io.tiler.collectors.example.config;

public class Field {
  private final String name;

  public Field(String name) {
    if (name == null) {
      name = "value";
    }

    this.name = name;
  }

  public String name() {
    return name;
  }
}
