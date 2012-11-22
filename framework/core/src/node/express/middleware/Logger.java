package node.express.middleware;

import node.express.Express;
import node.express.Request;
import node.express.Response;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

/**
 *
 */
public class Logger implements Express.Handler {
  private java.util.logging.Logger logger = java.util.logging.Logger.getLogger("express");

  private Formatter formatter;

  public Logger() {
    formatter = new DefaultFormatter();
  }

  public interface Formatter {
    String format(Request req, Response res);
  }

  private class DefaultFormatter implements Formatter {
    public String format(Request req, Response res) {
      long time = System.currentTimeMillis() - req.startTime();
      return req.method() + " " + req.path() + " " + res.status() + " " + time + "ms";
    }
  }

  public void exec(final Request req, final Response response, Express.Next next) {
    final ChannelFutureListener end = response.end;
    response.end = new ChannelFutureListener() {
      public void operationComplete(ChannelFuture future) throws Exception {
        response.end = end;
        response.end.operationComplete(future);
        logger.info(formatter.format(req, response));
      }
    };
    next.exec();
  }
}
