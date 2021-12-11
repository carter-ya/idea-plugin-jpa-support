package com.ifengxue.plugin.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValueProvider.Result;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.SlowOperations;
import com.intellij.util.containers.ConcurrentFactoryMap;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Carter
 */
public class JavaLibraryUtils {

    public static boolean hasLibraryClass(Module module, String classFqn) {
        return findClass(module, classFqn) != null;
    }

    public static boolean hasLibraryClass(Project project, String classFqn) {
        return findClass(project, classFqn) != null;
    }

    @Nullable
    public static PsiClass findClass(Module module, String classFqn) {
        return SlowOperations.allowSlowOperations(() -> getLibraryClassMap(module).get(classFqn));
    }

    @Nullable
    public static PsiClass findClass(Project project, String classFqn) {
        return SlowOperations.allowSlowOperations(() -> getLibraryClassMap(project).get(classFqn));
    }

    private static Map<String, PsiClass> getLibraryClassMap(@NotNull Project project) {
        if (DumbService.isDumb(project)) {
            return Collections.emptyMap();
        }
        return CachedValuesManager.getManager(project).getCachedValue(project, () -> {
            ConcurrentMap<String, PsiClass> map = ConcurrentFactoryMap.createMap((classFqn) ->
                JavaPsiFacade.getInstance(project)
                    .findClass(classFqn, GlobalSearchScope.allScope(project)));
            return Result.createSingleDependency(map, ProjectRootManager.getInstance(project));
        });
    }

    private static Map<String, PsiClass> getLibraryClassMap(@NotNull Module module) {
        if (DumbService.isDumb(module.getProject())) {
            return Collections.emptyMap();
        }
        return CachedValuesManager.getManager(module.getProject()).getCachedValue(module, () -> {
            ConcurrentMap<String, PsiClass> map = ConcurrentFactoryMap.createMap(classFqn ->
                JavaPsiFacade.getInstance(module.getProject()).findClass(classFqn,
                    GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)));
            return Result.createSingleDependency(map,
                ProjectRootManager.getInstance(module.getProject()));
        });
    }
}
