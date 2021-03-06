package node.express;

import com.fasterxml.jackson.databind.JsonNode;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.handler.stream.ChunkedFile;
import org.jboss.netty.util.CharsetUtil;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * The HTTP response object
 */
public class Response extends AbstractEventEmitter {
  public static final String JSON_MIME = "application/json";
  public static final String HTML_MIME = "text/html";
  private HttpResponse response;
  private MessageEvent e;
  private Request request;
  private Map locals = new HashMap();

  /**
   * Handles the end of the response stream. Typically closes the stream.
   */
  public ChannelFutureListener end;

  public Response(Request request, MessageEvent e) {
    this.request = request;
    response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
    this.e = e;

    end = new ChannelFutureListener() {
      public void operationComplete(ChannelFuture future) throws Exception {
        ChannelFutureListener.CLOSE.operationComplete(future);
        emit("end", Response.this);
      }
    };
  }

  public void set(String key, String value) {
    response.setHeader(key, value);
  }

  public String get(String key) {
    return response.getHeader(key);
  }

  public String header(String key) {
    return get(key);
  }

  public void header(String key, String value) {
    set(key, value);
  }

  public void cookie(String name, String value) {

  }

  public void clearCookie(String name) {

  }

  public void render(String view) {
    render(view, this.locals);
  }

  public void render(String view, Map data) {
    String rendered = request.app().render(view, data);
    send(rendered);
  }

  public void redirect(int code, String url) {
    response.setStatus(HttpResponseStatus.valueOf(code));
    response.setHeader("Location", url);
  }

  public void redirect(String url) {
    redirect(302, url);
  }

  public int status() {
    return response.getStatus().getCode();
  }

  public void status(int status) {
    response.setStatus(HttpResponseStatus.valueOf(status));
  }

  public void send(int statusCode) {
    response.setStatus(HttpResponseStatus.valueOf(statusCode));
    write();
  }

  public void send(int statusCode, String message) {
    response.setStatus(new HttpResponseStatus(statusCode, message));
    write();
  }

  /**
   * Send json data
   */
  public void send(JsonNode json) {
    setIfEmpty(HttpHeaders.Names.CONTENT_TYPE, JSON_MIME);
    setResponseText(json.toString());
    write();
  }

  public void json(JsonNode json) {
    send(json);
  }

  public void json(int status, JsonNode json) {
    send(status, json);
  }

  public void type(String type) {
    response.setHeader(HttpHeaders.Names.CONTENT_TYPE, type);
  }

  public void send(int statusCode, JsonNode json) {
    response.setStatus(HttpResponseStatus.valueOf(statusCode));
    setResponseText(json.toString());
    write();
  }

  public void send(String string) {
    setIfEmpty(HttpHeaders.Names.CONTENT_TYPE, HTML_MIME);
    setResponseText(string);
    write();
  }

  private void setIfEmpty(String key, Object value) {
    if (response.getHeader(key) == null) {
      response.setHeader(key, value);
    }
  }

  private void write() {
    response.setHeader(HttpHeaders.Names.DATE, Dates.formatHttpDate(new Date()));
    ChannelFuture future = writeResponse();
    future.addListener(end);
  }

  private void setResponseText(String text) {
    ChannelBuffer content = ChannelBuffers.copiedBuffer(text, CharsetUtil.UTF_8);
    response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, content.writerIndex());
    response.setContent(content);
  }

  public void sendFile(File file) {
    try {
      if (!file.exists()) {
        send(404);
        return;
      }
      long size = file.length();

      String ifModifiedString = request.header(HttpHeaders.Names.IF_MODIFIED_SINCE);
      if (ifModifiedString != null) {
        try {
          Date ifModifiedDate = Dates.parseHttpDate(ifModifiedString);
          if (ifModifiedDate.getTime() >= file.lastModified()) {
            send(304);
            return;
          }
        } catch (ParseException e1) {
          // if can't parse, just ignore
        }
      }

      setIfEmpty(HttpHeaders.Names.CONTENT_LENGTH, size);
      setIfEmpty(HttpHeaders.Names.DATE, Dates.formatHttpDate(new Date()));
      setIfEmpty(HttpHeaders.Names.LAST_MODIFIED, Dates.formatHttpDate(new Date(file.lastModified())));

      String contentType = MimeTypes.mimeTypeFromFile(file);
      if (contentType == null) {
        contentType = "application/octet-stream";
      }
      setIfEmpty(HttpHeaders.Names.CONTENT_TYPE, contentType);

      writeResponse();
      Channel channel = e.getChannel();
      ChannelFuture future = channel.write(new ChunkedFile(file));
      future.addListener(end);
    } catch (IOException e1) {
      throw new RuntimeException(e1);
    }
  }

  private ChannelFuture writeResponse() {
    Channel channel = e.getChannel();
    emit("header", this);
    return channel.write(response);
  }

  /**
   * Get access to the underlying netty channel for this response
   */
  public Channel channel() {
    return e.getChannel();
  }
}
