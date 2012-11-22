import com.fasterxml.jackson.databind.ObjectMapper;
import node.express.Express;
import node.express.Request;
import node.express.Response;

import java.io.File;

/**
 *
 */
public class SampleApp {
  private static final ObjectMapper json = new ObjectMapper();
  public static void main(String[] args) {
    Express express = new Express();

    express.use(express.logger());
    express.use(express.bodyParser());

//    express.get("/home", new Express.Handler() {
//      public void exec(Request req, Response res, Express.Next next) {
//        res.send(json.createObjectNode().put("name", "Jon Nichols").put("age", 38));
//      }
//    });

//    express.put("*", new Express.Handler() {
//      public void exec(Request req, Response res, Express.Next next) {
//        req.body();
//        res.send(200);
//      }
//    });

    Fun fun = new Fun();
    express.get("/", new Express.ObjectHandler(fun, "handleStuff"));

    express.listen(3000);
  }

  public static class Fun {
    public void handleStuff(Request req, Response res) {
      res.sendFile(new File("/Users/jnichols/another.png"));
    }
  }
}
