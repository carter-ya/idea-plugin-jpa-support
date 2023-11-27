- [English](README.md)
- [中文文档](README_zh.md)

# Plugin description

<!-- Plugin description -->
- Generate entity/repository class for JPA/Lombok/Spring Data JPA.
- Generate entity/repository class for JPA/Lombok/Spring Data JPA.
- Support internationalization. Currently supported languages are <a href="https://github.com/carter-ya/idea-plugin-jpa-support/blob/master/README.md">English</a>, <a href="https://github.com/carter-ya/idea-plugin-jpa-support/blob/master/README_zh.md">简体中文</a>.
- Support all databases.
- Support generate source code from Database plugin
- Support custom generate code template
   - Available template directory
      - Module template directory: `{module}/.idea/JPA Support/template`
      - Project template directory: `{project}/.idea/JPA Support/template`
      - Home template directory: `{user.home}/.JPA Support/template`
   - Available templates
      - Controller.vm
      - DTO.vm
      - JpaEntity.vm
      - JpaRepository.vm
      - MapperXml.vm
      - MybatisGenerateConfig.vm
      - QueryVO.vm
      - SaveVO.vm
      - Service.vm
      - Service-MybatisPlus.vm
      - Service-TkMybatis.vm
      - UpdateVO.vm
- Support custom db type mapping
- Mac: Command + N or Windows: Alt + Insert and select <b>Jpa Entities</b>
<!-- Plugin description end -->

# Guide

## Active Plugin

1. Way 1 `Generate JPA Entities` <br>
   ![Way 1](doc/package_right_click.png)

2. Way 2 -> Edit area right click `Generate JPA Entities` <br>

![Way 2](doc/editor_right_click.png)

3. `Database` right click<br>

![Database右击](doc/database_right_click_generate.png)

> IDEA Ultimate only

4. `Hot key`

- Mac: `Command + N`
- Windows: `Alt + Insert`

## Usage

1. Step 1<br>

![Step 1](doc/set_database_connection.png)

2. Step 2<br>

![Step 2](doc/generate_setting.png)

3. Step 3<br>

![Step 3](doc/select_and_generate.png)

## Other settings

1. Custom template<br>

![Custom template](doc/template.png)

2. Custom type mapping<br>

![Custom type mapping](doc/type_mapping.png)

## Related articles

1. [Use Tutorial Video YouTube](https://www.youtube.com/watch?v=CynidTePOys)

## Sponsor

<a href="https://jb.gg/OpenSource" alt="JetBrains" target="_blank"><img src="doc/jetbrains.png" width="100" alt="JetBrains" /></a>
