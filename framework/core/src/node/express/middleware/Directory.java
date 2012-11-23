package node.express.middleware;

import node.express.Express;
import node.express.Request;
import node.express.Response;

/**
 *
 */
public class Directory implements Express.Handler {
  private String path;

  public Directory(String path) {
    this.path = path;
  }

  public void exec(Request req, Response res, Express.Next next) {
    next.exec();
  }
}
