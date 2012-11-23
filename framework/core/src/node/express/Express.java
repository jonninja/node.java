package node.express;

import node.express.middleware.BodyParser;
import node.express.middleware.Directory;
import node.express.middleware.Logger;
import node.express.middleware.Static;
import node.express.renderers.Freemarker;
import node.express.renderers.Velocity;
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
 * An express app
 */
public class Express {
  private ServerBootstrap bootstrap;
  private Map<String, Object> settings = new HashMap<String, Object>();
  private Map<String, List<Route>> handlerMap = new HashMap<String, List<Route>>();
  private Map<String, Renderer> renderers = new HashMap<String, Renderer>();
  private Map<String, Object> locals = new HashMap<String, Object>();

  private List<Route> middleware = new ArrayList<Route>();

  public interface Next {
    void exec();
  }

  /**
   * Interface to implement for all request handlers.
   */
  public interface Handler {
    void exec(Request req, Response res, Express.Next next);
  }

  /**
   * An implementation of a request handler that calls a method on an object
   */
  public static class ObjectHandler implements Handler {
    private Method method;
    private Object obj;
    private boolean hasNext;

    /**
     * Construct the object handler for a static method. The method must have a signature of
     * (Request, Response) or (Request, Response, Next)
     * @param clazz the class
     * @param methodName the name of the method
     */
    public ObjectHandler(Class clazz, String methodName) {
      try {
        method = clazz.getMethod(methodName, Request.class, Response.class);
        hasNext = false;
      } catch (NoSuchMethodException e) {
        try {
          method = obj.getClass().getMethod(methodName, Request.class, Response.class, Next.class);
        } catch (NoSuchMethodException e1) {
          throw new RuntimeException("Method not found");
        }
      }
    }

    /**
     * Construct an object handler for an object method. The method must have a signature of
     * (Request, Response) or (Request, Response, Next)
     * @param obj the object
     * @param methodName the name of the method to invoke.
     */
    public ObjectHandler(Object obj, String methodName) {
      this(obj.getClass(), methodName);
      this.obj = obj;
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

  /**
   * Construct an Express application
   */
  public Express() {
    bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
        Executors.newCachedThreadPool(),
        Executors.newCachedThreadPool()
    ));
    bootstrap.setPipelineFactory(new PipelineFactory());

    settings.put("views", "views/");

    // register some default rendering engines
    renderers.put("vm", new Velocity());
    renderers.put("ftl", new Freemarker());

    locals.put("settings", settings);
  }

  /**
   * Assigns setting name to value
   */
  public void set(String name, Object value) {
    settings.put(name, value);
  }

  /**
   * Get setting name value
   */
  public Object get(String name) {
    return settings.get(name);
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

  /**
   * Register a rendering engine
   * @param ext the extension
   * @param renderer the renderer to use
   */
  public void engine(String ext, Renderer renderer) {
    renderers.put(ext, renderer);
  }

  /**
   * Render a view with a callback responding with the rendered string. This is the app-level variant of
   * Response.render(), and otherwise behaves the same way.
   * @param view the name of the view to render
   * @param context the context, which should include data used by the template engine
   * @return the rendered text
   */
  public String render(String view, Map context) {
    int i = view.lastIndexOf(".");
    String ext;
    if (i >= 0) {
      ext = view.substring(i + 1);
    } else {
      ext = (String) settings.get("view engine");
      if (ext == null) {
        throw new IllegalArgumentException("No default view set for view without extension");
      }
      view = view + "." + ext;
    }
    Renderer renderer = renderers.get(ext);
    if (renderer == null) {
      throw new IllegalArgumentException("No renderer for ext: " + ext);
    }

    Map mergedContext = new HashMap();
    mergedContext.putAll(locals);
    if (context != null) {
      mergedContext.putAll(context);
    }
    return renderer.render(settings.get("views") + view, mergedContext);
  }

  /**
   * Application local variables are provided to all templates rendered within the application.
   * This is useful for providing helper functions to templates, as well as app-level data.
   *
   * By default 'settings' is included in the locals, so renderers will have access to any settings
   * in the application. So, app.set("title", "My app") is accessible at settings.title
   */
  public Map<String, Object> locals() {
    return locals;
  }

  /**
   * Batch add a number of locals
   */
  public void locals(Map<String, Object> locals) {
    locals.putAll(locals);
  }

  /**
   * Install global middleware. The middleware will be called for all requests
   */
  public void use(Handler middleware) {
    Route route = new Route("all", "*", new Handler[] { middleware });
    this.middleware.add(route);
  }

  /**
   * Install middleware for the given path specification. Middleware will be called
   * only for requests that match this path.
   * @param path the path specification
   * @param middleware the middleware
   */
  public void use(String path, Handler middleware) {
    Route route = new Route("all", path, new Handler[] {middleware});
  }

  /**
   * Convenience method for getting the static file middleware. All files below
   * the given root path will be made available
   * @param path the root path for the files
   */
  public Handler staticFiles(String path) {
    return new Static(path);
  }

  /**
   * Convenience method for creating the logger middleware. For more options,
   * create the Logger middleware directly
   */
  public Handler logger() {
    return new Logger();
  }

  /**
   * Convenience method for creating the BodyParser middleware. The BodyParser automatically
   * parses JSON and UrlEncoded content and makes it avaialble via Request.body().
   */
  public Handler bodyParser() {
    return new BodyParser();
  }

  public Handler directory(String path) {
    return new Directory(path);
  }

  public void listen(int port) {
    bootstrap.bind(new InetSocketAddress(port));
  }

  /**
   * Install a GET handler
   * @param path the path spec
   * @param handler the handler
   */
  public void get(String path, Handler... handler) {
    addRoute("get", path, handler);
  }

  /**
   * Install a POST handler
   * @param path the path spec
   * @param handler the handler
   */
  public void post(String path, Handler... handler) {
    addRoute("post", path, handler);
  }

  /**
   * Install a DELETE handler
   * @param path the path spec
   * @param handler the handler
   */
  public void del(String path, Handler... handler) {
    addRoute("delete", path, handler);
  }

  /**
   * Install a PUT handler
   * @param path the path spec
   * @param handler the handler
   */
  public void put(String path, Handler... handler) {
    addRoute("put", path, handler);
  }

  /**
   * Install a handler for all HTTP methods
   * @param path the path spec
   * @param handler the handler
   */
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

  /**
   * Our core request handler that receives messages from Netty and passes them
   * along to handlers
   */
  private class RequestHandler extends SimpleChannelUpstreamHandler {
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
      HttpRequest request = (HttpRequest) e.getMessage();
      HttpMethod method = request.getMethod();

      final Request req = new Request(Express.this, e);
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

  /**
   * Intialization of the Netty pipeline
   */
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
