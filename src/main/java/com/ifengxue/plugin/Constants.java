package com.ifengxue.plugin;

import com.ifengxue.plugin.generator.source.AbstractSourceParser;
import com.ifengxue.plugin.generator.source.EntitySourceParserV2;
import com.ifengxue.plugin.generator.source.JpaRepositorySourceParser;
import com.ifengxue.plugin.generator.source.JpaServiceSourceParser;
import com.ifengxue.plugin.util.MapFactory;
import java.util.Map;

public interface Constants {

    String PLUGIN_NAME = "Jpa Support";
    String NAME = "JpaSupport";
    String GROUP_ID = NAME;
    String TEMPLATE_ID_PREFIX = "template/";
    String JPA_ENTITY_TEMPLATE_ID = TEMPLATE_ID_PREFIX + "JpaEntity.vm";
    String JPA_REPOSITORY_TEMPLATE_ID = TEMPLATE_ID_PREFIX + "JpaRepository.vm";
    String JPA_SERVICE_TEMPLATE_ID = TEMPLATE_ID_PREFIX + "JpaService.vm";
    Map<String, Class<? extends AbstractSourceParser>> BUILTIN_TEMPLATE_IDS = MapFactory.of(
        JPA_ENTITY_TEMPLATE_ID, EntitySourceParserV2.class,
        JPA_REPOSITORY_TEMPLATE_ID, JpaRepositorySourceParser.class,
        JPA_SERVICE_TEMPLATE_ID, JpaServiceSourceParser.class);

}
