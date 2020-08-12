package com.ifengxue.plugin.state;

import com.ifengxue.plugin.Constants;
import com.ifengxue.plugin.component.Settings;
import com.ifengxue.plugin.component.TemplateItem;
import com.ifengxue.plugin.generator.source.EntitySourceParserV2;
import com.ifengxue.plugin.generator.source.JpaRepositorySourceParser;
import com.ifengxue.plugin.gui.SourceCodeViewerDialog;
import com.ifengxue.plugin.i18n.LocaleContextHolder;
import com.ifengxue.plugin.util.TestTemplateHelper;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.util.xmlb.annotations.Transient;
import java.awt.event.ItemEvent;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import lombok.Data;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
public class SettingsConfigurable implements SearchableConfigurable {

    private static final Logger log = Logger.getInstance(SettingsConfigurable.class);
    @Transient
    private Settings settings;
    private SettingsState settingsState;

    @NotNull
    @Override
    public String getId() {
        return Constants.NAME;
    }

    @Nls(capitalization = Capitalization.Title)
    @Override
    public String getDisplayName() {
        return getId();
    }

    @Override
    public void reset() {
        settingsState = ServiceManager.getService(SettingsState.class);

        JTabbedPane tabbedPane = settings.getTabbedPane();
        tabbedPane.setTitleAt(0, LocaleContextHolder.format("source_code_template_tip"));
        String templateId = Constants.JPA_ENTITY_TEMPLATE_ID;
        settings.getCbxSelectCodeTemplate().removeAllItems();
        settings.getCbxSelectCodeTemplate().addItem(new TemplateItem()
            .setId(templateId)
            .setName("JpaEntity.vm")
            .setTemplate(settingsState.loadTemplate(templateId))
            .setSourceParseClass(EntitySourceParserV2.class)
        );
        templateId = Constants.JPA_REPOSITORY_TEMPLATE_ID;
        settings.getCbxSelectCodeTemplate().addItem(new TemplateItem()
            .setId(templateId)
            .setName("JpaRepository.vm")
            .setTemplate(settingsState.loadTemplate(templateId))
            .setSourceParseClass(JpaRepositorySourceParser.class)
        );
        settings.getCbxSelectCodeTemplate().addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                TemplateItem item = (TemplateItem) event.getItem();
                settings.getTxtSourceCode().setText(item.getTemplate());
            }
        });
        settings.getCbxSelectCodeTemplate().setSelectedIndex(0);
        settings.getTxtSourceCode().setText(settings.getCbxSelectCodeTemplate().getItemAt(0).getTemplate());
        settings.getBtnResetTemplate().addActionListener(event -> {
            TemplateItem item = settings.getCbxSelectCodeTemplate()
                .getItemAt(settings.getCbxSelectCodeTemplate().getSelectedIndex());
            item.setTemplate(settingsState.forceLoadTemplate(item.getId()));
            settings.getTxtSourceCode().setText(item.getTemplate());
        });
        settings.getBtnTestTemplate().addActionListener(event -> {
            SourceCodeViewerDialog dialog = new SourceCodeViewerDialog(ProjectManager.getInstance().getDefaultProject(),
                false);
            dialog.setSourceCode(TestTemplateHelper.evaluateToString(settings.getCbxSelectCodeTemplate()
                    .getItemAt(settings.getCbxSelectCodeTemplate().getSelectedIndex()).getSourceParseClass(),
                settings.getTxtSourceCode().getText()));
            dialog.show();
        });
        settings.getTxtSourceCode().addDocumentListener(new DocumentAdapter() {
            @Override
            public void documentChanged(DocumentEvent e) {
                TemplateItem item = settings.getCbxSelectCodeTemplate()
                    .getItemAt(settings.getCbxSelectCodeTemplate().getSelectedIndex());
                item.setTemplate(e.getDocument().getText());
            }
        });
        settings.setData(settingsState);
    }

    @Override
    public void disposeUIResources() {
        settings = null;
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        settings = new Settings();
        return settings.getRootComponent();
    }

    @Override
    public boolean isModified() {
        return settings.isModified(settingsState);
    }

    @Override
    public void apply() {
        settings.getData(settingsState);
    }

}
