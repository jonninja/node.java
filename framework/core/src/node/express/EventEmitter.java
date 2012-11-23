package node.express;

/**
 * Interface for objects that emit events
 */
public interface EventEmitter {
  /**
   * Install an event listener
   * @param event the event name
   * @param listener the listener to receive the event
   */
  void on(String event, Listener listener);

  /**
   * Interface for the listener
   */
  interface Listener {
    /**
     * Called when an event fires
     */
    void event(Object data);
  }
}
