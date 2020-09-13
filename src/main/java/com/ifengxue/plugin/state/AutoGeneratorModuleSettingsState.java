package com.ifengxue.plugin.state;

import com.intellij.openapi.components.RoamingType;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import lombok.Data;

@Data
@State(name = "AutoGeneratorSettingsState", storages = {
    @Storage(value = StateConstants.APPLICATION_STATE_NAME, roamingType = RoamingType.PER_OS),
})
public class AutoGeneratorModuleSettingsState {

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
