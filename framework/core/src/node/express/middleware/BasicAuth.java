package node.express.middleware;

import node.express.Express;
import node.express.Request;
import node.express.Response;
import org.apache.commons.codec.binary.Base64;

/**
 * Middleware that provides an implementation of basic authentication
 */
public class BasicAuth implements Express.Handler {
  private Validator validator;
  private String realm;
  private String username;
  private String password;

  /**
   * Callback for apps to provide validation logic.
   */
  public interface Validator {
    /**
     * Validate a provided username and password. Return true for valid credentials.
     */
    boolean validate(String username, String password);
  }

  public BasicAuth(Validator validator, String realm) {
    this.validator = validator;
    this.realm = realm;
  }

  /**
   * Construct the authenticator. Requests must match the given username and password
   */
  public BasicAuth(String username, String password, String realm) {
    this.username = username;
    this.password = password;
    this.realm = realm;
  }

  private void unauthorized(Response res) {
    res.header("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
    res.send(401, "Unauthorized");
  }

  public void exec(Request req, Response res, Express.Next next) {
    String authorization = req.header("authorization");
    if (authorization == null) {
      unauthorized(res);
      return;
    }

    String[] parts = authorization.split(" ");
    String scheme = parts[0];
    String[] credentials = new String(Base64.decodeBase64(parts[1])).split(":");
    String username = credentials[0];
    String password = credentials[1];

    if (!"Basic".equals(scheme)) {
      res.send(400);
      return;
    }

    boolean valid = false;
    if (validator != null) {
      valid = validator.validate(username, password);
    } else if (this.username != null && this.password != null) {
      valid = (this.username.equals(username) && this.password.equals(password));
    }
    if (valid) {
      req.attribute("user", username);
      req.attribute("remoteUser", username);
      next.exec();
    } else {
      unauthorized(res);
    }

  }
}
