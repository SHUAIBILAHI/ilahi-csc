package icc;
import java.sql.*;
import java.util.*;
public class Db {
  static String env(String k, String d) { String v = System.getenv(k); return v == null ? d : v; }
  static final String URL = env("DB_URL", "jdbc:mariadb://localhost:3306/ilahi"), USER = env("DB_USER", "root"), PASS = env("DB_PASS", "root");
  public static Connection get() throws SQLException { return DriverManager.getConnection(URL, USER, PASS); }
  public static String hash(String s) {
    try { return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(("icc$" + s).getBytes())); }
    catch (Exception e) { throw new RuntimeException(e); }
  }
  public static List<Map<String, Object>> q(String sql, Object... a) throws SQLException {
    try (Connection c = get(); PreparedStatement p = c.prepareStatement(sql)) {
      for (int i = 0; i < a.length; i++) p.setObject(i + 1, a[i]);
      ResultSet rs = p.executeQuery(); ResultSetMetaData m = rs.getMetaData(); List<Map<String, Object>> out = new ArrayList<>();
      while (rs.next()) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 1; i <= m.getColumnCount(); i++) { Object v = rs.getObject(i); if (v != null && !(v instanceof Number) && !(v instanceof String)) v = v.toString(); row.put(m.getColumnLabel(i), v); }
        out.add(row);
      }
      return out;
    }
  }
  public static int u(String sql, Object... a) throws SQLException {
    try (Connection c = get(); PreparedStatement p = c.prepareStatement(sql)) {
      for (int i = 0; i < a.length; i++) p.setObject(i + 1, a[i]);
      return p.executeUpdate();
    }
  }
}
