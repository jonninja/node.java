package node.express;

import java.util.List;
import java.util.Map;

/**
 * Defines a body
 */
public interface Body {
  /**
   * Get a string value
   */
  String getString(String key);

  /**
   * Get an integer value by key
   */
  Integer getInteger(String key);

  /**
   * Get a string value by index
   */
  String getString(int index);

  /**
   * Get an integer value by index
   */
  Integer getInteger(int index);

  Map map();

  List list();
}
