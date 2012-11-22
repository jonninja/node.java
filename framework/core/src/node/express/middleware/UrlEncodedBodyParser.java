package node.express.middleware;

import node.express.Body;
import node.express.Express;
import node.express.Request;
import node.express.Response;
import org.jboss.netty.handler.codec.http.multipart.Attribute;
import org.jboss.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import org.jboss.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.jboss.netty.handler.codec.http.multipart.InterfaceHttpData;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Body parser for URL encoded data
 */
public class UrlEncodedBodyParser implements Express.Handler {
  public void exec(Request req, Response res, Express.Next next) {
    if (req.body() == null) {
      try {
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), req._request());
        List<InterfaceHttpData> data = decoder.getBodyHttpDatas();
        if (data.size() > 0) {
          req.attribute("body", new PostBody(decoder));
        }
      } catch (Throwable e) {
        // ignored
      }
    }
    next.exec();
  }

  private class PostBody implements Body {
    private HttpPostRequestDecoder decoder;

    private PostBody(HttpPostRequestDecoder decoder) {
      this.decoder = decoder;
    }

    private Attribute getAttribute(String key) {
      try {
        InterfaceHttpData data = decoder.getBodyHttpData(key);
        if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
          return (Attribute) data;
        }
        return null;
      } catch (HttpPostRequestDecoder.NotEnoughDataDecoderException e) {
        throw new RuntimeException(e);
      }
    }

    public String getString(String key) {
      try {
        Attribute attribute = getAttribute(key);
        return attribute != null ? attribute.getValue() : null;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    public Integer getInteger(String key) {
      try {
        Attribute attribute = getAttribute(key);
        return attribute != null ? new Integer(attribute.getValue()) : null;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    private Attribute getAttribute(int index) {
      try {
        InterfaceHttpData data = decoder.getBodyHttpDatas().get(index);
        if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
          return (Attribute) data;
        }
        return null;
      } catch (HttpPostRequestDecoder.NotEnoughDataDecoderException e) {
        throw new RuntimeException(e);
      }
    }


    public String getString(int index) {
      try {
        Attribute attribute = getAttribute(index);
        return attribute != null ? attribute.getValue() : null;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    public Integer getInteger(int index) {
      try {
        Attribute attribute = getAttribute(index);
        return attribute != null ? new Integer(attribute.getValue()) : null;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    public Map map() {
      return null;
    }

    public List list() {
      return null;
    }
  }
}
