package node.express.middleware;

import node.express.EventEmitter;
import node.express.Express;
import node.express.Request;
import node.express.Response;

/**
 * Adds the `X-Response-Time` header displaying the response
 * duration in milliseconds.
 */
public class ResponseTime implements Express.Handler {
  private final String headerName;
  private final String format;

  public ResponseTime() {
    headerName = "X-Response-Time";
    format = "%dms";
  }

  public ResponseTime(String headerName, String format) {
    this.headerName = headerName;
    this.format = format;
  }

  public void exec(Request req, final Response res, Express.Next next) {
    final long start = System.currentTimeMillis();
    res.on("header", new EventEmitter.Listener() {
      public void event(Object data) {
        long time = System.currentTimeMillis() - start;
        res.header(headerName, String.format(format, time));
      }
    });
    next.exec();
  }
}
