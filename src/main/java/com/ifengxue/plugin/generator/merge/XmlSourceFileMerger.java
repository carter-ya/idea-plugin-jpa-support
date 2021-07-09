package com.ifengxue.plugin.generator.merge;

import com.ifengxue.plugin.entity.Table;
import com.ifengxue.plugin.generator.config.GeneratorConfig;
import com.ifengxue.plugin.generator.merge.bridge.MybatisContext;
import com.ifengxue.plugin.generator.source.MybatisGeneratorConfigSourceParser;
import com.ifengxue.plugin.util.VelocityUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiFile;
import fastjdbc.BeanUtil;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.velocity.app.VelocityEngine;
import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

public class XmlSourceFileMerger implements SourceFileMerger {

    private final Logger log = Logger.getInstance(getClass());

    @Override
    public boolean tryMerge(GeneratorConfig generatorConfig, Table table, PsiFile originalFile,
        PsiFile newFile) {
        List<String> warnings = new ArrayList<>();
        VelocityEngine velocityEngine = VelocityUtil.getInstance();
        MybatisGeneratorConfigSourceParser sourceParser = new MybatisGeneratorConfigSourceParser();
        sourceParser.setVelocityEngine(velocityEngine, StandardCharsets.UTF_8.name());
        String configXml = sourceParser.parse(generatorConfig, table);

        ConfigurationParser cp = new ConfigurationParser(warnings);
        try {
            Configuration configuration = cp.parseConfiguration(
                new ByteArrayInputStream(configXml.getBytes(StandardCharsets.UTF_8)));
            decorateConfiguration(configuration, table);

            MyBatisGenerator generator = new MyBatisGenerator(configuration,
                new DefaultShellCallback(false), warnings);
            generator.generate(null, null, null, false);
            List<GeneratedXmlFile> generatedXmlFiles = generator.getGeneratedXmlFiles();
            if (generatedXmlFiles.isEmpty()) {
                return false;
            }
            String formattedContent = generatedXmlFiles.get(0).getFormattedContent();
            System.out.println(formattedContent);
            return true;
        } catch (Exception e) {
            log.error("Try merge xml failed", e);
            return false;
        }
    }

    private void decorateConfiguration(Configuration configuration, Table table) {
        List<Context> contexts = configuration.getContexts();
        List<Context> newContexts = new ArrayList<>();
        for (Context context : contexts) {
            MybatisContext mybatisContext = new MybatisContext(context.getDefaultModelType(),
                table);
            BeanUtil.copyProperties(context, mybatisContext);
            context.getTableConfigurations().forEach(mybatisContext::addTableConfiguration);
            newContexts.add(mybatisContext);
        }
        contexts.clear();
        contexts.addAll(newContexts);
    }

    @Override
    public boolean isMergeSupported() {
        return true;
    }
}
