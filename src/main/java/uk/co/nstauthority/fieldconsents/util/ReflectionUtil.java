package uk.co.nstauthority.fieldconsents.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ReflectionUtil {

  private ReflectionUtil() {
    throw new IllegalStateException("Utility class");
  }

  public static List<Method> getAllMethods(Class<?> clazz) {
    if (Objects.isNull(clazz) || clazz.equals(Object.class)) {
      return Collections.emptyList();
    }

    var result = new ArrayList<>(getAllMethods(clazz.getSuperclass()));
    result.addAll(Arrays.stream(clazz.getDeclaredMethods()).toList());
    return result;
  }

  public static List<Field> getAllFields(Class<?> clazz) {
    if (Objects.isNull(clazz) || clazz.equals(Object.class)) {
      return Collections.emptyList();
    }

    var result = new ArrayList<>(getAllFields(clazz.getSuperclass()));
    result.addAll(Arrays.stream(clazz.getDeclaredFields()).toList());
    return result;
  }
}
