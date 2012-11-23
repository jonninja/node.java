package node.express.renderers;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import node.express.Renderer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * A Freemarker renderer for Node.Java
 */
public class Freemarker implements Renderer {
  private Configuration fm;

  public Freemarker() {
    fm = new Configuration();
    fm.setClassForTemplateLoading(Freemarker.class, "/");
    fm.setObjectWrapper(new DefaultObjectWrapper());
  }

  public String render(String name, Map context) {
    try {
      Template template = fm.getTemplate(name);

      StringWriter writer = new StringWriter();
      template.process(context, writer);
      writer.flush();
      return writer.toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (TemplateException e) {
      throw new RuntimeException(e);
    }
  }
}
