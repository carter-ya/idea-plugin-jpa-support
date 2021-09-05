package com.ifengxue.plugin;

import com.ifengxue.plugin.state.AutoGeneratorSettingsState;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class TemplateManager {

    private static final Logger log = Logger.getInstance(TemplateManager.class);
    private static final TemplateManager INSTANCE = new TemplateManager();
    private static final List<String> SEARCH_PATH = new ArrayList<>();
    /**
     * only used for debug
     */
    public static Function<String, String> debugTemplateMapping;

    static {
        SEARCH_PATH.add("{module}/.idea/JPA Support/template");
        SEARCH_PATH.add("{project}/.idea/JPA Support/template");
        SEARCH_PATH.add("{user.home}/.JPA Support/template");
    }

    private TemplateManager() {
    }

    public static TemplateManager getInstance() {
        return INSTANCE;
    }

    public String loadTemplate(String templateId) {
        // debug
        if (debugTemplateMapping != null && debugTemplateMapping.apply(templateId) != null) {
            return debugTemplateMapping.apply(templateId);
        }

        AutoGeneratorSettingsState service = ServiceManager
            .getService(Holder.getProject(), AutoGeneratorSettingsState.class);
        VirtualFile projectDir = ProjectUtil.guessProjectDir(Holder.getProject());
        String projectPath = "";
        if (projectDir != null) {
            projectPath = StringUtils.trimToEmpty(projectDir.getCanonicalPath());
        }
        Module module = ModuleManager.getInstance(Holder.getProject())
            .findModuleByName(service.getModuleName());
        String modulePath = "";
        if (module != null) {
            VirtualFile moduleDir = ProjectUtil.guessModuleDir(module);
            if (moduleDir != null) {
                modulePath = StringUtils.trimToEmpty(moduleDir.getCanonicalPath());
            }
        }
        String userHome = System.getProperty("user.home");
        for (String path : SEARCH_PATH) {
            path = path.replace("{module}", modulePath)
                .replace("{project}", projectPath)
                .replace("{user.home}", userHome);
            path += "/" + templateId.replaceFirst("template/", "");
            try {
                Path realPath = Paths.get(path);
                if (Files.exists(realPath)) {
                    return IOUtils.toString(realPath.toUri(), StandardCharsets.UTF_8);
                }
            } catch (Exception e) {
                log.debug("Can't load template from " + path, e);
            }
        }
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(templateId)) {
            if (input == null) {
                log.warn("template " + templateId + " not exists.");
                return "";
            }
            byte[] buffer = new byte[input.available()];
            input.read(buffer);
            return new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("read template " + templateId + " error", e);
            return "";
        }
    }

}
