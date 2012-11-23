import node.express.Express;
import node.express.Request;
import node.express.Response;

/**
 * Sample application to demonstrate page rendering
 */
public class Render {
  public static void main(String[] args) {
    Express app = new Express();
    app.set("view engine", "vm");
    app.set("title", "Render Test App");
    app.get("/velocity/:name", new Express.Handler() {
      public void exec(final Request req, Response res, Express.Next next) {
        res.render("render", req.params());
      }
    });

    app.get("/freemarker/:name", new Express.Handler() {
      public void exec(Request req, Response res, Express.Next next) {
        res.render("render.ftl", req.params());
      }
    });
    app.listen(3000);
  }
}
