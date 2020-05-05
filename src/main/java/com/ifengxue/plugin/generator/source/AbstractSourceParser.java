package com.ifengxue.plugin.generator.source;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Objects;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public abstract class AbstractSourceParser implements SourceParser, VelocityEngineAware {

  protected VelocityEngine velocityEngine;
  protected String encoding;

  protected String evaluate(VelocityContext ctx, String templateName) {
    StringWriter writer = new StringWriter();
    try (InputStream input = getClass().getClassLoader().getResourceAsStream(templateName)) {
      Objects.requireNonNull(input, "Resource " + templateName + " not exists.");
      byte[] buffer = new byte[input.available()];
      input.read(buffer);
      velocityEngine.evaluate(ctx, writer, getClass().getName(), new String(buffer, encoding));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return writer.toString();
  }

  @Override
  public void setVelocityEngine(VelocityEngine ve, String encoding) {
    this.velocityEngine = ve;
    this.encoding = encoding;
  }
}
