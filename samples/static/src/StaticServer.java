import node.express.Express;

/**
 * Example of serving a directory of static files
 */
public class StaticServer {
  public static void main(String[] args) {
    Express app = new Express();
    app.use(app.staticFiles("/public/"));
    app.listen(3000);
  }
}
