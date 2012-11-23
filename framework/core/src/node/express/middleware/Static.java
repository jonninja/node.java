package node.express.middleware;

import node.express.Express;
import node.express.Request;
import node.express.Response;

import java.io.File;

/**
 * Middleware to serve static files
 */
public class Static implements Express.Handler {
  private String basePath;
  private long maxAge = -1;

  public Static(String basePath) {
    if (basePath.startsWith("/")) {
      basePath = basePath.substring(1);
    }
    if (basePath.endsWith("/")) {
      basePath = basePath.substring(0, basePath.length() - 1);
    }
    this.basePath = basePath;
  }

  /**
   * Set the cache control header for assets served
   *
   * @param age the length of time to cache, in ms
   */
  public Static maxAge(long age) {
    this.maxAge = age;
    return this;
  }

  public void exec(Request req, Response res, Express.Next next) {
    String path = (String) req.param("wildcard");
    if (!path.startsWith("/")) {
      path = "/" + path;
    }
    if (path.endsWith("/")) {
      path = path + "index.html";
    }

    File srcFile = new File(basePath + path);
    if (!srcFile.exists()) {
      res.send(404);
    } else {
      res.sendFile(srcFile);
    }
  }
}
