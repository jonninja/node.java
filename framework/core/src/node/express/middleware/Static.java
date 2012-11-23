package node.express.middleware;

import node.express.Express;
import node.express.Request;
import node.express.Response;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 *
 */
public class Static implements Express.Handler {
  private String basePath;

  public Static(String basePath) {
    if (basePath.startsWith("/")) {
      basePath = basePath.substring(1);
    }
    if (basePath.endsWith("/")) {
      basePath = basePath.substring(0, basePath.length() - 1);
    }
    this.basePath = basePath;
  }

  public void exec(Request req, Response res, Express.Next next) {
    String path = (String) req.param("wildcard");

    if (!path.startsWith("/")) {
      path = "/" + path;
    }

    if (path.endsWith("/")) {
      path = path + "index.html";
    }

    URL url = Static.class.getClassLoader().getResource(basePath + path);
    if (url == null) {
      res.send(404);
    } else {
      if (url.getProtocol().equals("file")) {
        try {
          File file = new File(url.toURI());
          res.sendFile(file);
        } catch (URISyntaxException e) {
          throw new RuntimeException(e);
        }
      } else {

      }
    }

  }
}
