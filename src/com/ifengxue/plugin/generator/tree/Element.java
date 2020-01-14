package com.ifengxue.plugin.generator.tree;

import com.ifengxue.plugin.generator.tree.visitor.Visitor;
import java.util.List;

public interface Element {

  String LINE_SEPARATOR = "\r\n";
  String SERIAL_VERSION_UID = "serialVersionUID";

  void setLineSeparator(String lineSeparator);

  /**
   * 设置缩进方式
   */
  void setIndent(Indent indent);

  /**
   * 设置父元素
   */
  void setParent(Element parent);

  /**
   * 获取当前元素的父元素
   *
   * @return 如果当前元素没有父元素则返回null
   */
  Element parent();

  /**
   * 从当前元素一直向上查找，返回匹配的元素
   */
  Element parents(String name);

  /**
   * 向当前节点添加子节点
   */
  void addChild(Element child);

  /**
   * 获取当前元素的直接子元素
   */
  List<Element> children();

  /**
   * 获取同级元素同名的第一个兄弟元素
   */
  Element firstSibling(String name);

  /**
   * 获取同级元素同名的所有兄弟元素
   */
  List<Element> siblings(String name);

  /**
   * 获取统计元素的所有兄弟元素
   */
  List<Element> siblings();

  /**
   * 获取当前元素名称
   */
  String name();

  /**
   * 生成源代码
   */
  String toJavaCode();

  void accept(Visitor visitor);

  enum Indent {
    TAB {
      @Override
      public String getIndent() {
        return "\t";
      }
    },
    TWO_TAB {
      @Override
      public String getIndent() {
        return TAB.getDoubleIndent();
      }
    },
    TWO_SPACE {
      @Override
      public String getIndent() {
        return "  ";
      }
    },
    FOUR_SPACE {
      @Override
      public String getIndent() {
        return TWO_SPACE.getDoubleIndent();
      }
    },
    EIGHT_SPACE {
      @Override
      public String getIndent() {
        return FOUR_SPACE.getDoubleIndent();
      }
    };

    public static Indent findByDTDDeclare(String dtdDeclare) {
      if ("tab".equals(dtdDeclare)) {
        return TAB;
      }
      if ("2tab".equals(dtdDeclare)) {
        return TWO_TAB;
      }
      if ("2space".equals(dtdDeclare)) {
        return TWO_SPACE;
      }
      if ("4space".equals(dtdDeclare)) {
        return FOUR_SPACE;
      }
      if ("8space".equals(dtdDeclare)) {
        return EIGHT_SPACE;
      }
      return FOUR_SPACE;
    }

    public abstract String getIndent();

    public String getDoubleIndent() {
      return getIndent() + getIndent();
    }
  }

  class KeyValuePair {

    private final String key;
    private final String value;

    private KeyValuePair(String key, String value) {
      this.key = key;
      this.value = value;
    }

    /**
     * value以普通形式生成，如 number,boolean...
     */
    public static KeyValuePair newKeyValuePair(String key, String value) {
      return new KeyValuePair(key, value);
    }

    /**
     * value以字符串形式生成
     */
    public static KeyValuePair newKeyAndStringValuePair(String key, String value) {
      return new KeyValuePair(key, "\"" + value + "\"");
    }

    public String getKey() {
      return key;
    }

    public String getValue() {
      return value;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      KeyValuePair that = (KeyValuePair) o;

      return key.equals(that.key) && value.equals(that.value);
    }

    @Override
    public int hashCode() {
      int result = key.hashCode();
      result = 31 * result + value.hashCode();
      return result;
    }
  }
}
