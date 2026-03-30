package com.ifengxue.plugin;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;

public class TemplateManagerTest {

  @Test
  public void mapperXmlTemplateFallsBackToDefaultWhenUseKotlinExtension() {
    AtomicReference<String> requestedTemplateId = new AtomicReference<>();
    TemplateManager.debugTemplateMapping = templateId -> {
      requestedTemplateId.set(templateId);
      return "mock-template";
    };
    try {
      String template = TemplateManager.getInstance().loadTemplate("kt", Constants.MAPPER_XML_TEMPLATE_ID);
      assertNotNull(template);
      assertFalse(template.isEmpty());
      assertEquals("template/MapperXml.vm", requestedTemplateId.get());
    } finally {
      TemplateManager.debugTemplateMapping = null;
    }
  }
}
