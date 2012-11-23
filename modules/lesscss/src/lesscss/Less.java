package lesscss;

import node.express.Express;
import node.express.Request;
import node.express.Response;
import org.lesscss.LessCompiler;
import org.lesscss.LessException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Middleware that compiles .less files into css
 */
public class Less implements Express.Handler {
  private Map<String, File> cache = new HashMap<String, File>();
  private String basePath;

  public Less() {
  }

  public Less basePath(String basePath) {
    if (basePath.startsWith("/")) {
      basePath = basePath.substring(1);
    }
    if (basePath.endsWith("/")) {
      basePath = basePath.substring(0, basePath.length() - 1);
    }
    this.basePath = basePath;
    return this;
  }

  public void exec(Request req, Response res, Express.Next next) {
    try {
      String path = (String) req.param("wildcard");
      File cssFile = null;
      if (path.endsWith(".less")) {
        if (!path.startsWith("/")) {
          path = "/" + path;
        }

        File srcFile = new File(basePath + path);

        if (!srcFile.exists()) {
          res.send(404);
        } else {
          cssFile = cache.get(path);
          if (cssFile != null && cssFile.exists()) {
            // check to see if the srcFile has been changed, and force recompile
            if (cssFile.lastModified() < srcFile.lastModified()) {
              cssFile = null;
            }
          }
          if (cssFile == null) {
            cssFile = File.createTempFile("LessCss", ".css");
            LessCompiler lessCompiler = new LessCompiler();
            lessCompiler.compile(srcFile, cssFile);
            cache.put(path, cssFile);
          }
        }
      }
      if (cssFile != null) {
        res.type("text/css");
        res.sendFile(cssFile);
      } else {
        next.exec();
      }
    } catch (LessException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
