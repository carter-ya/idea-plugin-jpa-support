package com.ifengxue.plugin.gui.annotation;

import java.beans.PropertyDescriptor;
import javax.swing.table.TableCellRenderer;

public interface RendererProvider {

  TableCellRenderer createRenderer(PropertyDescriptor pd);
}
