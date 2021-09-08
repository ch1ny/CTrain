package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @Author SDU德布罗煜
 * @Date 2021/8/7 19:37
 * @Description JDBC工具类
 * @Version 1.0
 */

public class SqlCon {

    // 此处MyConfig.java未上传，内部仅配置部分项目口令，可自行修改
    static final String DB_URL = MyConfig.DB_URL;
    static final String USER = MyConfig.DB_USER;
    static final String PSW = MyConfig.DB_PSW;

    public static Connection getInstance() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, USER, PSW);
            conn.setAutoCommit(false);
            return conn;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
