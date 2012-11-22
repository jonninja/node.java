package node.express.middleware;

import node.express.Express;
import node.express.Request;
import node.express.Response;

/**
 *
 */
public class Static implements Express.Handler {
  private String basePath;

  public Static(String basePath) {
    this.basePath = basePath;
  }

  public void exec(Request req, Response res, Express.Next next) {

  }
}
