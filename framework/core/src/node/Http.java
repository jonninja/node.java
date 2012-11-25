package node;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import node.express.Response;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * An extremely simple API for making HTTP client requests. The API is designed to support 98% of use cases,
 * maintaining a simple, clear API.
 *
 * A very basic request to get json from an endpoint is as follows
 * JsonNode userInfo = Http.get("http://www.facebook.com/508850041").json();
 *
 * Requests will not be initiated until the client requests something of the response.
 */
public class Http {
  private static HttpClient client;
  private static ObjectMapper mapper = new ObjectMapper();

  static {
    client = new DefaultHttpClient();
  }

  public static class Request {
    private HttpRequestBase request;
    private HttpResponse response;

    private List<NameValuePair> formParameters; // stores form parameters

    public Request(HttpRequestBase request) {
      this.request = request;
    }

    /**
     * Set a request header. This must be called before any call to retrieve data from a response
     * @param key the header key
     * @param value the header value
     */
    public Request header(String key, String value) {
      request.setHeader(key, value);
      return this;
    }

    /**
     * Get a response header
     */
    public String header(String key) {
      connect();
      Header[] headers = response.getHeaders(key);
      if (headers != null && headers.length > 0) {
        return headers[0].getValue();
      }
      return null;
    }

    /**
     * Add values to the query string
     */
    public Request query(String key, Object value) {
      try {
        URIBuilder builder = new URIBuilder(request.getURI());
        builder.setParameter(key, value.toString());
        request.setURI(builder.build());
        return this;
      } catch (URISyntaxException e) {
        throw new HttpException(e);
      }
    }

    public Request type(String contentType) {
      return header("Content-Type", contentType);
    }

    public String type() {
      return header("Content-Type");
    }

    public int status() {
      connect();
      return response.getStatusLine().getStatusCode();
    }

    public boolean success() {
      int status = status();
      return (status >= 200 && status <= 399);
    }

    /**
     * Get the body of the request as json.
     */
    public JsonNode json() {
      connect();
      try {
        String content = EntityUtils.toString(response.getEntity());
        return mapper.readTree(content);
      } catch (IOException e) {
        throw new HttpException(e);
      }
    }

    /**
     * Set the body of the request to json. Sets the content type appropriately. This will only work with
     * request types that allow a body.
     */
    public Request json(JsonNode body) throws HttpException {
      try {
        type("application/json");
        StringEntity entity = new StringEntity(body.toString());
        ((HttpEntityEnclosingRequestBase)request).setEntity(entity);
        return this;
      } catch (Throwable e) {
        throw new HttpException(e);
      }
    }

    /**
     * Get the body of the request as text
     */
    public String text() {
      try {
        return EntityUtils.toString(response.getEntity());
      } catch (IOException e) {
        throw new HttpException(e);
      }
    }

    /**
     * Get the stream of the body
     */
    public InputStream body() {
      try {
        connect();
        return response.getEntity().getContent();
      } catch (IOException e) {
        throw new HttpException(e);
      }
    }

    /**
     * Add form parameters. Since this call returns the Request object itself, it's fairly easy to chain calls
     * Request.post("http://service.com/upload").form("name","Some Name").form("age", 38)
     *
     * @param key the key of the form parameter
     * @param value the value of the form parameter
     */
    public Request form(String key, Object value) {
      if (formParameters == null) {
        formParameters = new ArrayList<NameValuePair>();
      }
      formParameters.add(new BasicNameValuePair(key, value.toString()));
      return this;
    }

    /**
     * Pipe the data from a request to an output stream
     */
    public void pipe(OutputStream outputStream) throws HttpException {
      InputStream input = null;
      try {
        connect();
        input = response.getEntity().getContent();
        IOUtils.copy(input, outputStream);
      } catch (IOException e) {
        throw new HttpException(e);
      } finally {
        try {
          if (input != null) input.close();
        } catch (IOException e) { /* ignored */ }
      }
    }

    /**
     * Pipe the data from a request to a file
     */
    public void pipe(File file) {
      InputStream input = null;
      try {
        connect();
        input = response.getEntity().getContent();
        FileUtils.copyInputStreamToFile(input, file);
      } catch (IOException e) {
        throw new HttpException(e);
      } finally {
        try {
          if (input != null) input.close();
        } catch (IOException e) { /* ignored */ }
      }
    }

    /**
     * Pipe the data from a request to an Express response
     */
    public void pipe(Response response) {

    }

    /**
     * Pipe the content of this request into another request. Typically, this request would be a GET,
     * and the passed request would be a PUT or POST
     */
    public void pipe(Request request) {

    }

    /**
     * Initiate the connection
     */
    public Request connect() throws HttpException {
      try {
        if (response == null) {
          if (formParameters != null) {
            HttpEntity entity = ((HttpEntityEnclosingRequestBase) request).getEntity();
            if (entity != null) {
              throw new HttpException("Multiple entities are not allowed. Perhaps you have set a body and form parameters?");
            }
            ((HttpEntityEnclosingRequestBase) request).setEntity(new UrlEncodedFormEntity(formParameters));
          }
          response = client.execute(request);
        }
        return this;
      } catch (IOException e) {
        throw new HttpException(e);
      }
    }

    /**
     * Close the request. This is only required if the content of the response is not consumed. Calling
     * json() or text() (or any of the pipe methods) will consume the response.
     */
    public void close() {
      try {
        if (response != null) {
          EntityUtils.consume(response.getEntity());
        }
      } catch (IOException e) {
        throw new HttpException(e);
      }
    }
  }

  /**
   * Create a GET request.
   * @param url the url to connect to
   */
  public static Request get(String url) {
    HttpGet get = new HttpGet(url);
    return new Request(get);
  }

  /**
   * Create a PUT request.
   */
  public static Request put(String url) {
    return new Request(new HttpPut(url));
  }

  /**
   * Create a POST request
   */
  public static Request post(String url) {
    return new Request(new HttpPost(url));
  }

  /**
   * Exception thrown by most Http methods
   */
  public static class HttpException extends RuntimeException {
    public HttpException(Throwable cause) {
      super(cause);
    }
    public HttpException(String message) {
      super(message);
    }
  }
}
