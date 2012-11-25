package node;

import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Implements the Node.js Crypto API
 */
public class Crypto {
  public static class Hash {
    private MessageDigest md;

    public Hash(String type) {
      try {
        md = MessageDigest.getInstance(type);
      } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException(e);
      }
    }

    public Hash update(String data) {
      update(data, "UTF-8");
      return this;
    }

    public Hash update(String data, String encoding) {
      try {
        md.update(data.getBytes(encoding));
        return this;
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
      }
    }

    public Hash update(byte[] data) {
      md.update(data);
      return this;
    }

    public byte[] digest() {
      return md.digest();
    }

    public String digest(String encoding) {
      byte[] data = md.digest();
      String result = null;
      if (encoding.equals("hex")) {
        BigInteger bi = new BigInteger(1, data);
        result = String.format("%0" + (data.length << 1) + "X", bi);
      } else if (encoding.equals("base64")) {
        result = Base64.encodeBase64String(data);
      }
      return result;
    }
  }

  public static Hash createHash(String algorithm) {
    return new Hash(algorithm);
  }

  /**
   * Encrypt string data with an encryption algorithm with specified encoding
   * @param data the source string
   * @param algorithm the encryption algorithm to use
   * @param encoding the encoding to use
   */
  public static String encrypt(String data, String algorithm, String encoding) {
    return createHash(algorithm).update(data).digest(encoding);
  }

  /**
   * Encoding data with a specified encoding
   * @param src the source string
   * @param encoding the encoding (hex or base64 are supported)
   */
  public static String encode(String src, String encoding) {
    byte[] data = src.getBytes();
    if (encoding.equals("hex")) {
      BigInteger bi = new BigInteger(1, data);
      return String.format("%0" + (data.length << 1) + "X", bi);
    } else if (encoding.equals("base64")) {
      return Base64.encodeBase64String(data);
    } else {
      throw new IllegalArgumentException();
    }
  }

  /**
   * Encode a source string to base64
   */
  public static String base64(String src) {
    return Base64.encodeBase64String(src.getBytes());
  }
}
