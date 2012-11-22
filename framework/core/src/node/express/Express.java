package node.express;

import node.express.middleware.BodyParser;
import node.express.middleware.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Executors;

/**
 *
 */
public class Express {
  private ServerBootstrap bootstrap;
  private Map<String, List<Route>> handlerMap = new HashMap<String, List<Route>>();

  private List<Route> middleware = new ArrayList<Route>();

  public interface Next {
    void exec();
  }

  public interface Handler {
    void exec(Request req, Response res, Express.Next next);
  }

  public static class ObjectHandler implements Handler {
    private Method method;
    private Object obj;
    private boolean hasNext;

    public ObjectHandler(Object obj, String methodName) {
      this.obj = obj;
      try {
        method = obj.getClass().getMethod(methodName, Request.class, Response.class);
        hasNext = false;
      } catch (NoSuchMethodException e) {
        try {
          method = obj.getClass().getMethod(methodName, Request.class, Response.class, Next.class);
        } catch (NoSuchMethodException e1) {
          throw new RuntimeException("Method not found");
        }
      }
    }

    public void exec(Request req, Response res, Next next) {
      try {
        if (hasNext) {
          method.invoke(obj, req, res, next);
        } else {
          method.invoke(obj, req, res);
        }
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public Express() {
    bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
        Executors.newCachedThreadPool(),
        Executors.newCachedThreadPool()
    ));
    bootstrap.setPipelineFactory(new PipelineFactory());
  }

  /**
   * Assigns setting name to value
   */
  public void set(String name, Object value) {

  }

  /**
   * Get setting name value
   */
  public Object get(String name) {
    return null;
  }

  /**
   * Set setting to true
   */
  public void enable(String name) {

  }

  /**
   * Set setting to false
   */
  public void disable(String name) {

  }

  /**
   * Check if setting is enabled
   */
  public boolean enabled(String name) {
    return false;
  }

  public void use(Handler callback) {
    Route route = new Route("all", "*", new Handler[] { callback });
    this.middleware.add(route);
  }

  public void use(String path, Handler middleware) {

  }

  public Handler staticFiles(String path) {
    return null;
  }

  public Handler logger() {
    return new Logger();
  }

  public Handler bodyParser() {
    return new BodyParser();
  }

  public void listen(int port) {
    bootstrap.bind(new InetSocketAddress(port));
  }

  public void get(String path, Handler... handler) {
    addRoute("get", path, handler);
  }
  public void post(String path, Handler... handler) {
    addRoute("post", path, handler);
  }
  public void del(String path, Handler... handler) {
    addRoute("delete", path, handler);
  }
  public void put(String path, Handler... handler) {
    addRoute("put", path, handler);
  }
  public void all(String path, Handler... handler) {
    addRoute("*", path, handler);
  }

  /**
   * Add a route handler
   * @param verb the http verb (PUT, POST, etc.)
   * @param path the path
   * @param handlers a list of handlers
   */
  private void addRoute(String verb, String path, Handler[] handlers) {
    Route impl = new Route(verb, path, handlers);

    List<Route> routes = handlerMap.get(verb);
    if (routes == null) {
      routes = new ArrayList<Route>();
      handlerMap.put(verb, routes);
    }
    routes.add(impl);
  }

  private class NoOpNextHandler implements Next {
    public void exec() {
      // if we got here, we didn't handle the request, so send
    }
  }

  /**
   * A 'next' handler that works through a list of routes
   */
  private class RouteListHandler implements Next {
    private Request request;
    private Response response;
    private List<Route> routes;
    private Next next;

    private RouteListHandler(Request request, Response response, List<Route> routes, Next next) {
      this.request = request;
      this.response = response;
      this.routes = routes;
      this.next = next;
    }

    public void exec() {
      // if we're out of handlers, just execute next chain
      if (routes == null || routes.size() == 0) {
        next.exec();
        return;
      }

      // now we should find the first handler that matches
      int index = 0;
      for (Route route : routes) {
        if (request.checkRoute(route)) {
          Next nextNext;
          List<Route> remaining = routes.subList(index + 1, routes.size());
          if (!remaining.isEmpty()) {
            nextNext = new RouteListHandler(request, response, routes.subList(1, routes.size()), next);
          } else {
            nextNext = next;
          }
          new CallbackListNextHandler(request, response, route, Arrays.asList(route.callbacks), nextNext).exec();
          return;
        }
        index++;
      }

    }
  }

  /**
   * Next implementation that cycles through a list of handlers
   */
  private class CallbackListNextHandler implements Next {
    private final Request request;
    private final Response response;
    private Route route;
    private Next next;
    private List<Handler> handlers;

    private CallbackListNextHandler(Request request, Response response, Route route,
                                    List<Handler> handlers, Next next) {
      this.request = request;
      this.response = response;
      this.route = route;
      this.next = next;
      this.handlers = handlers;
    }

    public void exec() {
      Next nextNext = null;
      if (handlers.size() > 1) {
        nextNext = new CallbackListNextHandler(request, response, route, handlers.subList(1, handlers.size()), next);
      } else {
        nextNext = next;
      }
      Handler handler = handlers.get(0);
      handler.exec(request, response, nextNext);
    }
  }

  private class RequestHandler extends SimpleChannelUpstreamHandler {
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
      HttpRequest request = (HttpRequest) e.getMessage();
      HttpMethod method = request.getMethod();

      final Request req = new Request(e);
      final Response res = new Response(req, e);

      List<Route> handlers = handlerMap.get(method.getName().toLowerCase());

      try {
        new RouteListHandler(req, res, middleware,
            new RouteListHandler(req, res, handlers,
                new Next() {
                  public void exec() {
                    // if we got here, we didn't handle it, so return a 404
                    res.send(404);
                  }
                })).exec();
      } catch (Throwable t) {
        t.printStackTrace();
        res.send(500);
      }
    }
  }

  private class PipelineFactory implements ChannelPipelineFactory {
    public ChannelPipeline getPipeline() throws Exception {
      ChannelPipeline pipeline = Channels.pipeline();

      pipeline.addLast("decoder", new HttpRequestDecoder());
      pipeline.addLast("aggregator", new HttpChunkAggregator(1048576));
      pipeline.addLast("encoder", new HttpResponseEncoder());
      pipeline.addLast("handler", new RequestHandler());
      pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
      return pipeline;
    }
  }
}
