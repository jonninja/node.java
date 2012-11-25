package node;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A very simple configuration system, based on JSON files. Multiple configuration files can be
 * loaded, which subsequent loads being merged with previous data. When there are conflicts, the
 * value is replaced, allowing for hierarchical settings configurations, where a base configuration
 * can be extended and customized.
 */
public class Configuration {
  private static ObjectMapper json = new ObjectMapper();
  private static Map root = new HashMap();

  /**
   * Load one or more settings files. Each of the paths will be loaded, with each one merged into
   * the master
   */
  public static void load(String... paths) {
    try {
      for (String path : paths) {
        Map data = json.readValue(new File(path), Map.class);
        merge(root, data);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void merge(Map src, Map data) {
    Set<Map.Entry> entries = data.entrySet();
    for (Map.Entry entry : entries) {
      Object srcValue = src.get(entry.getKey());
      Object value = entry.getValue();
      if (srcValue == null) {
        src.put(entry.getKey(), value);
      } else {
        if (srcValue instanceof Map && value instanceof Map) {
          merge((Map)srcValue, (Map) value);
        } else {
          src.put(entry.getKey(), value);
        }
      }
    }
  }

  public static Object get(String path) {
    String[] components = path.split("\\.");
    Object value = root;
    for (String component : components) {
      if (value != null && value instanceof Map) {
        value = ((Map) value).get(component);
      } else {
        return null;
      }
    }
    return value;
  }

  public static String string(String path) {
    return (String) get(path);
  }

  public static Map map(String path) {
    return (Map) get(path);
  }

  public static Integer Int(String path) {
    return (Integer) get(path);
  }

  public static int Int(String path, int defaultValue) {
    Integer result = Int(path);
    return result != null ? result : defaultValue;
  }
}
