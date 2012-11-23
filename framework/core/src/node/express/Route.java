package node.express;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A route definition
 */
public class Route {
  String method;
  String path;
  Express.Handler[] callbacks;
  List<String> keys;
  Pattern pattern;

  public Route(String method, String path, Express.Handler[] callbacks) {
    this.method = method;
    this.path = path;
    this.callbacks = callbacks;

    pattern = Pattern.compile(pathRegexp(path, true, true), Pattern.CASE_INSENSITIVE);
  }

  private class Key {
    String value;
    boolean optional;
  }

  public String path() {
    return path;
  }

  /**
   * Does this route's signature match the path
   */
  public Map<String, String> match(String path) {
    Matcher matcher = pattern.matcher(path);

    Map<String, String> result = null;
    if (matcher.matches()) {
      result = new HashMap<String, String>();
      int count = matcher.groupCount();
      for (int i = 1; i <= count; i++) {
        String value = matcher.group(i);
        if (keys.size() >= i) {
          String key = keys.get(i - 1);
          result.put(key, value);
        } else {
          result.put("wildcard", value);
        }
      }
    }
    return result;
  }

  public String pathRegexp(String path, boolean sensitive, boolean strict) {
    keys = new ArrayList<String>();
    path = strict ? path : "/?";

    StringBuffer sb = new StringBuffer();
    Pattern pattern = Pattern.compile("(/)?(\\.)?:(\\w+)(?:(\\(.*?\\)))?(\\?)?(\\*)?");
    Matcher matcher = pattern.matcher(path);
    while (matcher.find()) {
      String slash = matcher.group(1);
      String format = matcher.group(2);
      String key = matcher.group(3);
      String capture = matcher.group(4);
      String optional = matcher.group(5);
      String star = matcher.group(6);

      Key k = new Key();
      k.value = key;
      k.optional = optional != null;
      keys.add(key);

      if (slash == null) slash = "";
      String replacement = "";
      if (optional == null) replacement += slash;
      replacement += "(?:";
      if (optional != null) replacement += slash;
      if (format != null) replacement += format;

      if (capture != null) {
        replacement += capture;
      } else if (format != null) {
        replacement += "([^/.]+?)";
      } else {
        replacement += "([^/]+?)";
      }
      replacement += ")";
      if (optional != null) replacement += optional;

      if (star != null) replacement += "(/*)?";

      matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(sb);
    String result = sb.toString();
    result = result.replaceAll("([/.])", "\\/");
    result = result.replaceAll("\\*", "(.*)");
    result = "^" + result + "$";
    return result;
  }

  public static void main(String[] args) {
    Route route = new Route("get", "*", null);
    Map<String, String> params = route.match("/project/123/jon");
    for (Map.Entry<String, String> entry : params.entrySet()) {
      System.out.println(entry.getKey() + "=" + entry.getValue());
    }
  }
}
