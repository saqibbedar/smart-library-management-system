// DBConnection.java
import java.sql.*;

public class DBConnection {
    public static void main(String[] args) {

        String path = "./SLMS-DB.accdb";
        String url = "jdbc:ucanaccess://" + path;

        try {
            Connection conn = DriverManager.getConnection(url);
            System.out.println("Connected to MS Access Database!");
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
