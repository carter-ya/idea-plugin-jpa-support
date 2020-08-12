package com.ifengxue.plugin.generator.source;

import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.generator.config.GeneratorConfig;

/**
 * 源码解析器
 */
public interface SourceParser {

  String parse(GeneratorConfig config, Table table);

  default String parse(GeneratorConfig config, Table table, String template) {
    throw new UnsupportedOperationException();
  }
}
