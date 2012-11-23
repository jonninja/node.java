package node.express.renderers;

import node.express.Renderer;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import java.io.StringWriter;
import java.util.Map;

/**
 * A renderer that uses the Velocity template system
 */
public class Velocity implements Renderer {
  private VelocityEngine ve = new VelocityEngine();

  public Velocity() {
    ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
    ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
  }

  public String render(String name, Map options) {
    VelocityContext context = new VelocityContext(options);

    Template template;
    try {
      template = ve.getTemplate(name);

      StringWriter sw = new StringWriter();
      template.merge(context, sw);
      return sw.toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }
}
