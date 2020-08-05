package com.ifengxue.plugin.generator.source;

import com.ifengxue.plugin.state.SettingsState;
import com.intellij.openapi.components.ServiceManager;
import java.io.StringWriter;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public abstract class AbstractSourceParser implements SourceParser, VelocityEngineAware {

  protected VelocityEngine velocityEngine;
  protected String encoding;

  protected String evaluate(VelocityContext ctx, String templateName) {
    SettingsState settingsState = ServiceManager.getService(SettingsState.class);
    try {
      StringWriter writer = new StringWriter();
      velocityEngine.evaluate(ctx, writer, getClass().getName(), settingsState.loadTemplate(templateName));
      return writer.toString();
    } catch (Exception e) {
      throw new EvaluateSourceCodeException(e);
    }
  }

  @Override
  public void setVelocityEngine(VelocityEngine ve, String encoding) {
    this.velocityEngine = ve;
    this.encoding = encoding;
  }
}
