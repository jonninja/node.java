import lesscss.Less;
import node.Configuration;
import node.express.Express;

/**
 * Example of serving a directory of static files
 */
public class StaticServer {
  public static void main(String[] args) {
    Configuration.load("settings.json");

    Express app = new Express();
    app.use(app.logger());
    app.use(new Less().basePath("public/"));
    app.use(app.staticFiles("public/"));
    app.listen(3000);
  }
}
