package fastjdbc;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

public final class BeanUtil {

  /**
   * 实例化对象
   *
   * @param clazz class 对象
   * @param <T> 泛型
   */
  public static <T> T instantiate(Class<T> clazz) {
    T dest;
    try {
      dest = clazz.newInstance();
    } catch (InstantiationException e) {
      throw new RuntimeException(clazz.getName() + " may be abstract class", e);
    } catch (IllegalAccessException e) {
      try {
        Constructor<T> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        dest = constructor.newInstance();
      } catch (NoSuchMethodException e1) {
        throw new RuntimeException(clazz.getName() + " have none parameter constructor", e1);
      } catch (ReflectiveOperationException e1) {
        throw new RuntimeException(clazz.getName() + " instance error.", e1);
      }
    }
    return dest;
  }

  public static PropertyDescriptor[] findPropertyDescriptors(Object obj) {
    obj = Objects.requireNonNull(obj, "obj can't be null");
    return findPropertyDescriptors(obj.getClass());
  }

  public static PropertyDescriptor[] findPropertyDescriptors(Class<?> clazz) {
    clazz = Objects.requireNonNull(clazz, "clazz can't be null");
    try {
      BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
      return beanInfo.getPropertyDescriptors();
    } catch (IntrospectionException e) {
      throw new RuntimeException(e);
    }
  }

  public static PropertyDescriptor findPropertyDescriptor(Class<?> clazz, String property) {
    clazz = Objects.requireNonNull(clazz, "clazz can't be null");
    property = Objects.requireNonNull(property, "property can not be null");
    PropertyDescriptor[] properties = findPropertyDescriptors(clazz);
    for (PropertyDescriptor propertyDescriptor : properties) {
      String name = propertyDescriptor.getName();
      if (name.equals(property)) {
        return propertyDescriptor;
      }
    }
    throw new RuntimeException(new NoSuchFieldException("field " + property + " not found."));
  }

  /**
   * 复制<code>orig</code>属性到<code>destClazz</code>
   *
   * @param orig 原始对象
   * @param destClazz 目标对象类型
   * @param <T> 目标对象泛型
   */
  public static <T> T copyProperties(Object orig, Class<T> destClazz) {
    orig = Objects.requireNonNull(orig, "orig can't be null");
    destClazz = Objects.requireNonNull(destClazz, "destClazz can not be null");
    if (destClazz.isInterface()) {
      throw new RuntimeException(destClazz.getName() + " is interface.");
    }
    return copyProperties(orig, instantiate(destClazz));
  }

  /**
   * 复制<code>orig</code>属性到<code>dest</code>
   *
   * @param orig 原始对象
   * @param dest 目标对象
   * @param <T> 目标对象泛型
   */
  public static <T> T copyProperties(Object orig, T dest) {
    orig = Objects.requireNonNull(orig, "orig can't be null");
    dest = Objects.requireNonNull(dest, "dest can't be null");
    return copyProperties(orig, dest, (String[]) null);
  }

  /**
   * 复制<code>orig</code>属性到<code>destClazz</code>，忽略<code>ignoreProperties</code>指定的属性
   *
   * @param orig 原始对象
   * @param destClazz 目标对象类型
   * @param ignoreProperties 忽略的属性
   * @param <T> 目标对象泛型
   */
  public static <T> T copyProperties(Object orig, Class<T> destClazz, String... ignoreProperties) {
    return copyProperties(orig, instantiate(destClazz), ignoreProperties);
  }

  /**
   * 复制<code>orig</code>属性到<code>dest</code>，忽略<code>ignoreProperties</code>指定的属性
   *
   * @param orig 原始对象
   * @param dest 目标对象
   * @param ignoreProperties 忽略的属性
   * @param <T> 目标对象泛型
   */
  public static <T> T copyProperties(Object orig, T dest, String... ignoreProperties) {
    orig = Objects.requireNonNull(orig, "orig can't be null");
    dest = Objects.requireNonNull(dest, "dest can't be null");
    PropertyDescriptor[] origProperties = findPropertyDescriptors(orig);
    PropertyDescriptor[] destProperties = findPropertyDescriptors(dest);
    Map<String, PropertyDescriptor> nameKeyedPropertyMap = Arrays.stream(destProperties)
        .collect(Collectors.toMap(PropertyDescriptor::getName, property -> property));
    Set<String> ignorePropertySet = wrapToSet(ignoreProperties);
    for (PropertyDescriptor origProperty : origProperties) {
      String name = origProperty.getName();
      if (ignorePropertySet.contains(name)) {
        continue;
      }
      PropertyDescriptor destProperty = nameKeyedPropertyMap.get(name);
      if (destProperty == null) {
        continue;
      }
      Method writeMethod = destProperty.getWriteMethod();
      if (writeMethod == null) {
        continue;
      }
      try {
        writeMethod.invoke(dest, origProperty.getReadMethod().invoke(orig));
      } catch (ReflectiveOperationException e) {
        throw new RuntimeException("copy property " + name + " fail.", e);
      } catch (IllegalArgumentException e) {
        throw new RuntimeException(
            "copy property " + name + " fail, " + "can't convert " + origProperty.getPropertyType().getName() + " to "
                + destProperty.getPropertyType().getName() + ".", e);
      }
    }
    return dest;
  }

  /**
   * 转换对象为map
   *
   * @param orig 对象
   * @param ignoreProperties 忽略的属性
   */
  public static Map<String, Object> toMap(Object orig, String... ignoreProperties) {
    PropertyDescriptor[] origProperties = findPropertyDescriptors(orig);
    Set<String> ignorePropertySet = wrapToSet(ignoreProperties);
    Map<String, Object> origMap = new HashMap<>(origProperties.length - ignorePropertySet.size());
    for (PropertyDescriptor origProperty : origProperties) {
      String name = origProperty.getName();
      if (ignorePropertySet.contains(name)) {
        continue;
      }
      try {
        origMap.put(name, origProperty.getReadMethod().invoke(orig));
      } catch (ReflectiveOperationException e) {
        throw new RuntimeException("read property " + name + " fail.", e);
      }
    }
    return origMap;
  }

  /**
   * 转换对象为 string map
   *
   * @param orig 对象
   * @param ignoreProperties 忽略的属性
   */
  public static Map<String, String> toStringMap(Object orig, String... ignoreProperties) {
    Map<String, Object> objectMap = toMap(orig, ignoreProperties);
    Map<String, String> stringMap = new HashMap<>(objectMap.size());
    objectMap.forEach((key, value) -> stringMap.put(key, value == null ? null : value.toString()));
    return stringMap;
  }

  /**
   * 查找类的所有字段，包含父类的字段，不包括Object中的字段
   */
  public static List<Field> findFields(Class<?> clazz) {
    Class<?> superClass = clazz;
    List<Field> fields = new LinkedList<>();
    while (superClass != Object.class) {
      Field[] fs = superClass.getDeclaredFields();
      for (Field field : fs) {
        if (!field.isAccessible()) {
          field.setAccessible(true);
        }
        fields.add(field);
      }
      superClass = superClass.getSuperclass();
    }
    return fields;
  }

  /**
   * 查找类的所有字段，包含父类的字段，不包括Object中的字段，顺序是源码的顺序，父类字段在前，子类字段在后
   */
  public static List<Field> findOrderedFields(Class<?> clazz) {
    Class<?> superClass = clazz;
    List<Field> fields = new LinkedList<>();
    Stack<Class<?>> classStack = new Stack<>();
    while (superClass != Object.class) {
      classStack.push(superClass);
      superClass = superClass.getSuperclass();
    }
    while (!classStack.isEmpty()) {
      for (Field field : classStack.pop().getDeclaredFields()) {
        if (!field.isAccessible()) {
          field.setAccessible(true);
        }
        fields.add(field);
      }
    }
    return fields;
  }

  /**
   * 从类中查找指定的方法，包含父类的方法
   *
   * @param methodName 方法名称
   */
  public static Method findMethod(String methodName, Class<?> clazz, Class<?>... parameterTypes) {
    Class<?> superClass = clazz;
    while (superClass != null) {
      try {
        Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
        if (!method.isAccessible()) {
          method.setAccessible(true);
        }
        return method;
      } catch (NoSuchMethodException e) {
        // skip
      }
      superClass = superClass.getSuperclass();
    }
    return null;
  }

  private static Set<String> wrapToSet(String[] array) {
    if (array == null || array.length == 0) {
      return Collections.emptySet();
    } else {
      return new HashSet<>(Arrays.asList(array));
    }
  }

}