package com.ifengxue.plugin.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValueProvider.Result;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.containers.ConcurrentFactoryMap;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

/**
 * @author Carter
 */
public class JavaLibraryUtils {

    public static boolean hasLibraryClass(@Nonnull Module module, String classFqn) {
        return getLibraryClassMap(module).getOrDefault(classFqn, false);
    }

    public static boolean hasLibraryClass(@Nonnull Project project, String classFqn) {
        return getLibraryClassMap(project).getOrDefault(classFqn, false);
    }

    private static Map<String, Boolean> getLibraryClassMap(@NotNull Project project) {
        if (DumbService.isDumb(project)) {
            return Collections.emptyMap();
        }
        return CachedValuesManager.getManager(project).getCachedValue(project, () -> {
            ConcurrentMap<String, Boolean> map = ConcurrentFactoryMap.createMap((classFqn) ->
                JavaPsiFacade.getInstance(project)
                    .findClass(classFqn, GlobalSearchScope.allScope(project)) != null);
            return Result.createSingleDependency(map, ProjectRootManager.getInstance(project));
        });
    }

    private static Map<String, Boolean> getLibraryClassMap(@NotNull Module module) {
        if (DumbService.isDumb(module.getProject())) {
            return Collections.emptyMap();
        }
        return CachedValuesManager.getManager(module.getProject()).getCachedValue(module, () -> {
            ConcurrentMap<String, Boolean> map = ConcurrentFactoryMap.createMap(
                classFqn -> JavaPsiFacade.getInstance(module.getProject()).findClass(classFqn,
                    GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)) != null);
            return Result.createSingleDependency(map,
                ProjectRootManager.getInstance(module.getProject()));
        });
    }
}
