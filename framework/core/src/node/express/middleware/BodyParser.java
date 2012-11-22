package node.express.middleware;

import node.express.Express;
import node.express.Request;
import node.express.Response;

/**
 *
 */
public class BodyParser implements Express.Handler {
  private JsonBodyParser json;
  private UrlEncodedBodyParser urlEncoded;

  public BodyParser() {
    json = new JsonBodyParser();
    urlEncoded = new UrlEncodedBodyParser();
  }

  public void exec(final Request req, final Response res, final Express.Next next) {
    json.exec(req, res, new Express.Next() {
      public void exec() {
        // try the url encoded body parser
        urlEncoded.exec(req, res, next);
      }
    });
  }
}
