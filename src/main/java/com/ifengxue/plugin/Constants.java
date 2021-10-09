package com.ifengxue.plugin;

public interface Constants {

  String PLUGIN_NAME = "JPA Support";
  String NAME = "JPASupport";
  String GROUP_ID = NAME;
  String JPA_ENTITY_TEMPLATE_ID = "template/JpaEntity.vm";
  String JPA_REPOSITORY_TEMPLATE_ID = "template/JpaRepository.vm";
  String CONTROLLER_TEMPLATE_ID = "template/Controller.vm";
  String JPA_SERVICE_TEMPLATE_ID = "template/Service.vm";
  String MYBATIS_PLUS_SERVICE_TEMPLATE_ID = "template/Service-MybatisPlus.vm";
  String TK_MYBATIS_SERVICE_TEMPLATE_ID = "template/Service-TkMybatis.vm";
  String MAPPER_XML_TEMPLATE_ID = "template/MapperXml.vm";
  String SAVE_VO_TEMPLATE_ID = "template/SaveVO.vm";
  String UPDATE_VO_TEMPLATE_ID = "template/UpdateVO.vm";
  String QUERY_VO_TEMPLATE_ID = "template/QueryVO.vm";
  String DTO_TEMPLATE_ID = "template/DTO.vm";

  String MYBATIS_GENERATOR_CONFIG_TEMPLATE_ID = "template/MybatisGenerateConfig.vm";

  // For Test

  String IN_TEST_ENVIRONMENT = "InTestEnvironment";
}
