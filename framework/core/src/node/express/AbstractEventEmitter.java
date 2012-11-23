package node.express;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract implementation of an event emitter
 */
public class AbstractEventEmitter implements EventEmitter {
  private Map<String, List<Listener>> listeners = new HashMap<String, List<Listener>>();

  public synchronized void on(String event, Listener listener) {
    List<Listener> list = listeners.get(event);
    if (list == null) {
      list = new ArrayList<Listener>(1);
      listeners.put(event, list);
    }
    list.add(listener);
  }

  /**
   * Emit an event
   * @param event the event name
   * @param data the event data
   */
  public synchronized void emit(String event, Object data) {
    List<Listener> list = listeners.get(event);
    if (list != null) {
      for (Listener listener : list) {
        listener.event(data);
      }
    }
  }
}
