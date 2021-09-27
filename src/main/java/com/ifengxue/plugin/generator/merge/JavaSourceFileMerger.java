package com.ifengxue.plugin.generator.merge;

import static java.util.stream.Collectors.toMap;

import com.ifengxue.plugin.Holder;
import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.generator.config.GeneratorConfig;
import com.intellij.lang.jvm.annotation.JvmAnnotationAttribute;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiJvmModifiersOwner;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiReferenceList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class JavaSourceFileMerger implements SourceFileMerger {

    private final Logger log = Logger.getInstance(JavaSourceFileMerger.class);

    @Override
    public boolean tryMerge(GeneratorConfig generatorConfig, Table table,
        PsiFile originalFile, PsiFile newFile) {
        merge(originalFile, newFile);
        return true;
    }

    @Override
    public boolean isMergeSupported() {
        return true;
    }

    private void merge(PsiFile originalFile, PsiFile psiFile) {
        PsiClass[] originalPsiClasses = ((PsiJavaFile) originalFile).getClasses();
        PsiClass[] psiClasses = ((PsiJavaFile) psiFile).getClasses();
        PsiClass originalTopClass = originalPsiClasses[0];
        PsiClass psiTopClass = psiClasses[0];

        // merge annotations
        mergeAnnotations(originalTopClass, psiTopClass);

        // merge implements
        try {
            Map<String, PsiClassType> nameToClassType = Arrays
                .stream(originalTopClass.getImplementsListTypes())
                .collect(toMap(psiClassType -> Objects.requireNonNull(psiClassType.resolve())
                        .getQualifiedName(),
                    Function.identity()));
            for (PsiClassType implementsListType : psiTopClass.getImplementsListTypes()) {
                PsiClass resolvePsiClass = implementsListType.resolve();
                if (resolvePsiClass != null && !nameToClassType
                    .containsKey(resolvePsiClass.getQualifiedName())) {
                    PsiJavaCodeReferenceElement implementsReference = ServiceManager
                        .getService(Holder.getOrDefaultProject(), PsiElementFactory.class)
                        .createReferenceFromText(
                            Objects.requireNonNull(resolvePsiClass.getQualifiedName()),
                            originalTopClass);
                    PsiReferenceList implementsList = originalTopClass.getImplementsList();
                    if (implementsList != null) {
                        implementsList.add(implementsReference);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Can't merge implements", e);
        }

        // merge fields
        Map<String, PsiField> nameToField = Arrays.stream(originalTopClass.getFields())
            .collect(toMap(PsiField::getName, Function.identity()));
        for (PsiField field : psiTopClass.getFields()) {
            if (!nameToField.containsKey(field.getName())) {
                mergeFieldAndTryKeepOrder(field, psiTopClass, originalTopClass);
            } else {
                PsiField originalField = nameToField.get(field.getName());
                mergeAnnotations(originalField, field);
            }
        }

        // merge methods
        Map<String, PsiMethod> nameToMethod = Arrays.stream(originalTopClass.getMethods())
            .collect(toMap(PsiMethod::getName, Function.identity()));
        for (PsiMethod method : psiTopClass.getMethods()) {
            if (!nameToMethod.containsKey(method.getName())) {
                originalTopClass.add(method);
            }
        }
    }

    /**
     * merge source annotations to original
     *
     * @param original original
     * @param source target
     */
    private void mergeAnnotations(PsiJvmModifiersOwner original, PsiJvmModifiersOwner source) {
        Map<String, PsiAnnotation> nameToAnnotation = Arrays
            .stream(original.getAnnotations())
            .collect(toMap(PsiAnnotation::getQualifiedName, Function.identity()));
        for (PsiAnnotation annotation : source.getAnnotations()) {
            if (!nameToAnnotation.containsKey(annotation.getQualifiedName())) {
                PsiModifierList modifierList = original.getModifierList();
                if (modifierList != null && annotation.getQualifiedName() != null) {
                    PsiAnnotation psiAnnotation = modifierList
                        .addAnnotation(annotation.getQualifiedName());
                    for (JvmAnnotationAttribute attribute : annotation.getAttributes()) {
                        psiAnnotation.setDeclaredAttributeValue(attribute.getAttributeName(),
                            annotation.findAttributeValue(attribute.getAttributeName()));
                    }
                }
            }
        }
    }

    /**
     * merge field to target class, and try keep order
     */
    private void mergeFieldAndTryKeepOrder(PsiField field, PsiClass originalClass,
        PsiClass targetClass) {
        PsiField precursorField = null;
        boolean precursorIsFound = false;
        for (PsiField originalClassField : originalClass.getFields()) {
            if (originalClassField.getName().equals(field.getName())) {
                precursorIsFound = true;
                break;
            } else {
                precursorField = originalClassField;
            }
        }
        PsiField[] targetClassFields = targetClass.getFields();
        if (precursorIsFound && precursorField != null) {
            for (PsiField targetClassField : targetClassFields) {
                if (targetClassField.getName().equals(precursorField.getName())) {
                    precursorField = targetClassField;
                    break;
                }
            }
        }

        if (!precursorIsFound) {
            if (targetClassFields.length == 0) {
                PsiMethod[] methods = targetClass.getMethods();
                if (methods.length == 0) {
                    targetClass.add(field);
                } else {
                    targetClass.addBefore(field, methods[0]);
                }
            } else {
                PsiField firstNotStaticField = null;
                for (PsiField targetClassField : targetClassFields) {
                    if (targetClassField.getModifierList() == null
                        || !targetClassField.getModifierList()
                        .hasModifierProperty(PsiModifier.STATIC)) {
                        firstNotStaticField = targetClassField;
                        break;
                    }
                }
                if (firstNotStaticField == null) {
                    firstNotStaticField = targetClassFields[targetClassFields.length - 1];
                }
                targetClass.addBefore(field, firstNotStaticField);
            }
        } else {
            targetClass.addAfter(field, precursorField);
        }
    }
}
