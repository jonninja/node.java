package node.express;

import java.util.Map;

/**
 * Interface that renderers must implement
 */
public interface Renderer {
  /**
   * Render a template.
   * @param name the path to the template to render
   * @param context context for the renderer, including extra data for the template, in the
   *                locals attribute
   * @return the rendered string
   */
  String render(String name, Map context);
}
