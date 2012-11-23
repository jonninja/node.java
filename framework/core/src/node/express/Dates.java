package node.express;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Helpers to work with HTTP dates
 */
public class Dates {
  private static String format = "EEE, dd MMM yyyy HH:mm:ss zzz";

  public static String formatHttpDate(Date date) {
    SimpleDateFormat dateFormat = new SimpleDateFormat(format);
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    return dateFormat.format(date);
  }

  public static Date parseHttpDate(String dateString) throws ParseException {
    SimpleDateFormat dateFormat = new SimpleDateFormat(format);
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    return dateFormat.parse(dateString);
  }
}
