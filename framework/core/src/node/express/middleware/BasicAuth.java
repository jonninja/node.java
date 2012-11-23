package node.express.middleware;

import node.express.Express;
import node.express.Request;
import node.express.Response;

/**
 * Middleware that provides an implementation of basic authentication
 */
public class BasicAuth implements Express.Handler {
  private Validator validator;

  public interface Validator {
    boolean validate(String username, String password);
  }

  public BasicAuth(Validator validator) {
    this.validator = validator;
  }

  public void exec(Request req, Response res, Express.Next next) {
    next.exec();
  }
}
