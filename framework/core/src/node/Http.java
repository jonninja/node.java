package node;

/**
 * The HTTP interfaces in Node are designed to support many features of the protocol which have been
 * traditionally difficult to use. In particular, large, possibly chunk-encoded, messages. The interface
 * is careful to never buffer entire requests or responses--the user is able to stream data.
 */
public class Http {
  public static class Request {

  }

  public static class Response {

  }

  public static class Server {
    public Server listen(int port) {
      return this;
    }
    public Server listen(int port, String hostName) {

      return this;
    }

    public void close() {

    }
  }

  public static Server createServer() {
    return new Server();
  }
}
