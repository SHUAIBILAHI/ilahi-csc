package icc;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebListener;
@WebListener
public class Init implements ServletContextListener {
  public void contextInitialized(ServletContextEvent e) {
    try {
      Db.u("CREATE TABLE IF NOT EXISTS users(id BIGINT AUTO_INCREMENT PRIMARY KEY,name VARCHAR(100),mobile VARCHAR(20) UNIQUE,email VARCHAR(120),pass VARCHAR(64),role VARCHAR(10) DEFAULT 'USER',status VARCHAR(10) DEFAULT 'ACTIVE',created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
      Db.u("CREATE TABLE IF NOT EXISTS services(id BIGINT AUTO_INCREMENT PRIMARY KEY,category VARCHAR(50),name VARCHAR(100),description VARCHAR(255),active TINYINT DEFAULT 1)");
      Db.u("CREATE TABLE IF NOT EXISTS applications(id BIGINT AUTO_INCREMENT PRIMARY KEY,app_no VARCHAR(30) UNIQUE,user_id BIGINT,service_id BIGINT,details TEXT,file_name VARCHAR(255),status VARCHAR(15) DEFAULT 'PENDING',remarks VARCHAR(255),created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,FOREIGN KEY(user_id) REFERENCES users(id),FOREIGN KEY(service_id) REFERENCES services(id))");
      if (Db.q("SELECT id FROM users WHERE role='ADMIN'").isEmpty())
        Db.u("INSERT INTO users(name,mobile,email,pass,role) VALUES('Admin','admin','ilahicomputer20@gmail.com',?,'ADMIN')", Db.hash("admin123"));
      if (Db.q("SELECT id FROM services").isEmpty()) {
        String[][] s = {{"Aadhaar","Aadhaar Correction","Naam, DOB, address sudhaar"},{"Aadhaar","Aadhaar Download","e-Aadhaar download"},{"Aadhaar","Aadhaar Linking","PAN / Mobile se link"},{"Aadhaar","Address Update","Pata badlein"},{"Aadhaar","Mobile Number Update","Mobile number update"},{"PAN","PAN Card Apply","Naya PAN / correction"},{"Passport","Passport Service","Apply / Track"},{"Driving","Driving Licence","Apply / Renew"},{"Ration","Ration Card Services","Apply / Update"},{"Certificate","Income Certificate","Aay praman patra"},{"Certificate","Caste Certificate","Jati praman patra"}};
        for (String[] x : s) Db.u("INSERT INTO services(category,name,description) VALUES(?,?,?)", x[0], x[1], x[2]);
      }
    } catch (Exception ex) { throw new RuntimeException("DB init failed: " + ex.getMessage(), ex); }
  }
}
