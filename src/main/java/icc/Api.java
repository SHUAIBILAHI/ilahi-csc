package icc;
import com.google.gson.Gson;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.file.*;
import java.time.Year;
import java.util.*;
import static icc.Db.*;

@WebServlet("/api/*") @MultipartConfig(maxFileSize = 2 * 1024 * 1024)
public class Api extends HttpServlet {
  static final Gson G = new Gson();
  static final String APP_SQL = "SELECT a.id,a.app_no,s.name service,u.name customer,u.mobile,a.details,a.file_name,a.status,a.remarks,a.created_at FROM applications a JOIN services s ON s.id=a.service_id JOIN users u ON u.id=a.user_id ";

  @Override protected void service(HttpServletRequest r, HttpServletResponse rs) throws IOException {
    rs.setContentType("application/json;charset=UTF-8"); r.setCharacterEncoding("UTF-8");
    Object o;
    try { o = route(r.getMethod() + " " + (r.getPathInfo() == null ? "" : r.getPathInfo()), r, r.getSession()); }
    catch (Exception e) { rs.setStatus(400); o = Map.of("error", String.valueOf(e.getMessage())); }
    rs.getWriter().write(G.toJson(o));
  }

  Object route(String m, HttpServletRequest r, HttpSession s) throws Exception {
    Long uid = (Long) s.getAttribute("uid"); String role = (String) s.getAttribute("role");
    switch (m) {
      case "GET /me": return uid == null ? Map.of() : Map.of("id", uid, "name", s.getAttribute("name"), "role", role);
      case "GET /services": return q("SELECT * FROM services WHERE active=1");
      case "POST /logout": s.invalidate(); return Map.of("ok", true);
      case "POST /register": {
        String mob = r.getParameter("mobile"), pw = r.getParameter("password"), nm = r.getParameter("name");
        if (nm == null || nm.isBlank()) throw new Exception("Naam daalein");
        if (mob == null || !mob.matches("\\d{10}")) throw new Exception("10 digit mobile number daalein");
        if (pw == null || pw.length() < 6) throw new Exception("Password kam se kam 6 akshar ka ho");
        if (!q("SELECT id FROM users WHERE mobile=?", mob).isEmpty()) throw new Exception("Ye mobile pehle se registered hai");
        u("INSERT INTO users(name,mobile,email,pass) VALUES(?,?,?,?)", nm, mob, r.getParameter("email"), hash(pw));
        return Map.of("ok", true);
      }
      case "POST /login": {
        var l = q("SELECT * FROM users WHERE mobile=? AND pass=? AND status='ACTIVE'", r.getParameter("mobile"), hash(String.valueOf(r.getParameter("password"))));
        if (l.isEmpty()) throw new Exception("Mobile/username ya password galat hai");
        var x = l.get(0); s.setAttribute("uid", ((Number) x.get("id")).longValue()); s.setAttribute("name", x.get("name")); s.setAttribute("role", x.get("role"));
        return Map.of("ok", true);
      }
      case "POST /apply": {
        need(uid, role, "USER");
        Part f = r.getPart("file"); String fn = null;
        if (f != null && f.getSize() > 0) {
          String orig = Paths.get(f.getSubmittedFileName()).getFileName().toString().replaceAll("[^A-Za-z0-9._-]", "_");
          if (!orig.toLowerCase().matches(".*\\.(jpg|jpeg|png|pdf)$")) throw new Exception("Sirf JPG, PNG ya PDF allowed hai");
          fn = UUID.randomUUID() + "_" + orig; Path dir = Path.of("uploads").toAbsolutePath(); Files.createDirectories(dir); f.write(dir.resolve(fn).toString());
        }
        String no = "ILC-" + Year.now() + "-" + (100000 + new Random().nextInt(900000));
        u("INSERT INTO applications(app_no,user_id,service_id,details,file_name) VALUES(?,?,?,?,?)", no, uid, Long.parseLong(r.getParameter("serviceId")), r.getParameter("details"), fn);
        return Map.of("appNo", no);
      }
      case "GET /myapps": need(uid, role, "USER"); return q(APP_SQL + "WHERE a.user_id=? ORDER BY a.id DESC", uid);
      case "GET /track": need(uid, role, "USER"); return q(APP_SQL + "WHERE a.app_no=? AND a.user_id=?", r.getParameter("no"), uid);
      case "GET /admin/stats": need(uid, role, "ADMIN");
        return q("SELECT COUNT(*) total,COALESCE(SUM(status='PENDING'),0) pending,COALESCE(SUM(status='APPROVED'),0) approved,COALESCE(SUM(status='REJECTED'),0) rejected,(SELECT COUNT(*) FROM users WHERE role='USER') users FROM applications").get(0);
      case "GET /admin/apps": need(uid, role, "ADMIN"); return q(APP_SQL + "ORDER BY a.id DESC LIMIT 200");
      case "POST /admin/status": {
        need(uid, role, "ADMIN"); String st = r.getParameter("status");
        if (!List.of("PENDING", "IN_PROGRESS", "APPROVED", "REJECTED").contains(st)) throw new Exception("Galat status");
        u("UPDATE applications SET status=?,remarks=? WHERE id=?", st, r.getParameter("remarks"), Long.parseLong(r.getParameter("id")));
        return Map.of("ok", true);
      }
      default: throw new Exception("Not found");
    }
  }
  void need(Long uid, String role, String want) throws Exception { if (uid == null || !want.equals(role)) throw new Exception("Pehle login karein"); }
}
