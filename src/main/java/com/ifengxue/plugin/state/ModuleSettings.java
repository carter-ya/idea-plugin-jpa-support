package com.ifengxue.plugin.state;

import lombok.Data;

@Data
public class ModuleSettings {

    /**
     * 实体包名
     */
    private String entityPackageName = "";
    /**
     * 实体包parent路径<br>
     * 如: /path/to/maven/module/src/main/java
     */
    private String entityParentDirectory = "";
    /**
     * repository包名
     */
    private String repositoryPackageName = "";
    /**
     * repository包名<br>
     * 如:/path/to/maven/module/src/main/java
     */
    private String repositoryParentDirectory = "";
}
