package node.express.middleware;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import node.express.Body;
import node.express.Express;
import node.express.Request;
import node.express.Response;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.util.CharsetUtil;

import java.util.List;
import java.util.Map;

/**
 * Middleware that parses a body as Json
 */
public class JsonBodyParser implements Express.Handler {
  private ObjectMapper json = new ObjectMapper();

  public void exec(Request req, Response res, Express.Next next) {
    if (req.body() == null) {
      try {
        ChannelBuffer content = req._request().getContent();
        if (content.readable()) {
          String jsonString = content.toString(CharsetUtil.UTF_8);
          JsonNode node = json.readTree(jsonString);
          req.attribute("body", new JsonBody(node));
        }
      } catch (Throwable e) {
        // any exceptions, and we don't bother
      }
    }
    next.exec();
  }

  public static class JsonBody implements Body {
    private JsonNode node;

    public JsonBody(JsonNode node) {
      this.node = node;
    }

    public String getString(String key) {
      JsonNode field = node.get(key);
      return field != null ? field.asText() : null;
    }

    public Integer getInteger(String key) {
      JsonNode field = node.get(key);
      return field != null ? field.asInt() : null;
    }

    public Map map() {
      return null;
    }

    public List list() {
      return null;
    }

    public String getString(int index) {
      JsonNode field = node.get(index);
      return field != null ? field.asText() : null;
    }

    public Integer getInteger(int index) {
      JsonNode field = node.get(index);
      return field != null ? field.asInt() : null;
    }
  }
}
