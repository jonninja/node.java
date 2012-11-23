package node.express;

import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;

import java.util.*;

/**
 *
 */
public class Request {
  private Express app;
  private Map<String, Object> attributes = new HashMap<String, Object>();
  private HttpRequest request;
  private ReadOnlyMultiMap<String, String> queryParams;
  private String path;
  private Route route;
  private long startTime;
  private Map<String, String> params;

  public Request(Express app, MessageEvent e) {
    startTime = System.currentTimeMillis();
    request = (HttpRequest) e.getMessage();
    this.app = app;

    QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
    path = queryStringDecoder.getPath();
    queryParams = new ReadOnlyMultiMap<String, String>(queryStringDecoder.getParameters());
  }

  /**
   * Set the route for this request. Will cause parameters to be re-parsed based on the route
   * string
   */
  boolean checkRoute(Route route) {
    Map<String, String> params = route.match(path);
    if (params != null) {
      this.params = params;
      this.route = route;
      return true;
    } else {
      return false;
    }
  }

  public String path() {
    return path;
  }

  public String method() {
    return request.getMethod().getName();
  }

  public Map<String, String> params() {
    return params;
  }

  public Map<String, String> query() {
    return queryParams;
  }

  public Express app() {
    return app;
  }

  /**
   * Get the internal request object. Please use wisely.
   */
  public HttpRequest _request() {
    return request;
  }

  /**
   * Set an attribute on this request
   */
  public void attribute(String key, Object value) {

  }

  /**
   * Get an attribute from this request
   */
  public Object attribute(String key) {
    return null;
  }

  public Object body() {
    return attribute("body");
  }

  /**
   * Return the value of param 'key' when present. Lookup is performed in the following order:
   * - params
   * - body
   * - query
   */
  public Object param(String key) {
    Object result = null;
    if (params() != null) {
      result = params().get(key);
    }
    if (result == null && body() != null) {
      // check the body
    }
    if (result == null && queryParams != null) {
      result = queryParams.get(key);
    }
    return result;
  }

  /**
   * Get the case-insensitive request header.
   */
  public String get(String key) {
    return null;
  }

  public boolean accepts(String type) {
    return false;
  }

  public String accepts(String... types) {
    return null;
  }

  public long startTime() {
    return startTime;
  }

  /**
   * Check if the incoming request contains the 'Content-Type' header field,
   * and it matches the given mime type
   */
  public boolean is(String contentType) {
    return false;
  }

  public String ip() {
    return null;
  }

  /**
   * A basic read-only multi map for params, headers and query
   */
  public class ReadOnlyMultiMap<K,V> implements Map<K,V> {
    private Map<K, List<V>> src;

    public ReadOnlyMultiMap(Map<K, List<V>> src) {
      this.src = src;
    }

    public int size() {
      return src.keySet().size();
    }

    public boolean isEmpty() {
      return size() == 0;
    }

    public boolean containsKey(Object key) {
      return src.containsKey(key);
    }

    public boolean containsValue(Object value) {
      for (List<V> values : src.values()) {
        if (values.contains(value)) return true;
      }
      // not implemented
      return false;
    }

    public V get(Object key) {
      List<V> values = src.get(key);
      if (values != null && values.size() > 0) {
        return values.get(0);
      } else {
        return null;
      }
    }

    public V put(K key, V value) {
      return null; // not implemented
    }

    public V remove(Object key) {
      return null; // not implemented
    }

    public void putAll(Map<? extends K, ? extends V> m) {
      // not implemented
    }

    public void clear() {
      // not implemented
    }

    public Set<K> keySet() {
      return src.keySet();
    }

    public Collection<V> values() {
      List<V> all = new ArrayList<V>();
      for (List<V> vs : src.values()) {
        all.addAll(vs);
      }
      return all;
    }

    public Set<Entry<K, V>> entrySet() {
      Set<Entry<K,V>> result = new HashSet<Entry<K, V>>();
      for (Entry<K, List<V>> entry : src.entrySet()) {
        List<V> values = entry.getValue();
        if (values != null && values.size() > 0) {
          result.add(new AbstractMap.SimpleEntry<K, V>(entry.getKey(), values.get(0)));
        }
      }
      return result;
    }

    public List<V> getValues(K key) {
      return src.get(key);
    }
  }
}
