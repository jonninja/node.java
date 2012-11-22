package node;

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
      }
      return result;
    }
  }

  public static Hash createHash(String algorithm) {
    return new Hash(algorithm);
  }
}
