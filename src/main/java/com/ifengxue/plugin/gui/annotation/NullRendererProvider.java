package com.ifengxue.plugin.gui.annotation;

import java.beans.PropertyDescriptor;
import javax.swing.table.TableCellRenderer;

public class NullRendererProvider implements RendererProvider {

  @Override
  public TableCellRenderer createRenderer(PropertyDescriptor pd) {
    return null;
  }
}
