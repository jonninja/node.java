package node.express;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Some utilities for working with mime types
 */
public class MimeTypes {
  private static Map<String, String> extMimeMap = new HashMap<String, String>() {{
    put("png", "image/png");
    put("jpg", "image/jpeg");
    put("txt", "text/plain");
    put("html", "text/html");
    put("json", "application/json");
    put("gif", "image/gif");
    put("css", "text/css");
    put("js", "application/javascript");
  }};

  public static String mimeTypeFromExt(String ext) {
    return extMimeMap.get(ext);
  }

  /**
   * Get a mime type from a file
   */
  public static String mimeTypeFromFile(File file) {
    int i = file.getPath().lastIndexOf(".");
    if (i >= 0) {
      String ext = file.getPath().substring(i + 1);
      return mimeTypeFromExt(ext);
    }
    return null;
  }
}
