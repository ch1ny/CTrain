package Servlets.Admin;

import Utils.JjwtUtil;
import Utils.SqlCon;
import io.jsonwebtoken.Claims;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @Author SDU德布罗煜
 * @Date 2021/8/12 17:12
 * @Description 根据列车车号/类型随机生成座位表
 * @Version 1.0
 */

@WebServlet("/admin/setSeats")
public class GenerateSeats extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        req.setCharacterEncoding("utf-8");
        PrintWriter pw = resp.getWriter();
        String jwt = req.getHeader("Authorization");
        if (jwt == null || jwt.equals("Bearer null")) {
            pw.write("{\"code\": -1, \"data\":\"用户未登录\"}");
        } else {
            int jwtVer = JjwtUtil.vertify(jwt);
            if (jwtVer == 0) {
                Claims c = JjwtUtil.parseJWT(jwt);
                Connection conn = null;
                try {
                    String userId = c.getSubject();
                    conn = SqlCon.getInstance();
                    PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM Admins WHERE user_id = ?");
                    ps.setString(1, userId);
                    ps.execute();
                    ResultSet rs = ps.getResultSet();
                    if (rs.next()) {
                        String train_id = req.getParameter("train");
                        ps = conn.prepareStatement("SELECT train_type FROM Trains WHERE train_id = ?");
                        ps.setString(1, train_id);
                        ps.execute();
                        rs = ps.getResultSet();
                        if (rs.next()) {
                            ps = conn.prepareStatement("SELECT 1 FROM Seats WHERE train_id = ?");
                            ps.setString(1, train_id);
                            ps.execute();
                            if (ps.getResultSet().next()) {
                                pw.write("{\"code\":-3, \"data\":\"该列车已生成座位\"}");
                            } else {
                                String type = rs.getString("train_type");
                                switch (type) {
                                    case "高铁":
                                        setGSeats(train_id, conn);
                                        break;
                                    case "动车":
                                        setDSeats(train_id, conn);
                                        break;
                                    default:
                                        setSeats(train_id, conn);
                                }
                                conn.commit();
                                pw.write("{\"code\":0, \"data\":\"座位生成成功\"}");
                            }
                        } else {
                            pw.write("{\"code\":-3, \"data\":\"该列车不存在\"}");
                        }
                    } else {
                        pw.write("{\"code\":-2, \"data\":\"权限不足\"}");
                    }
                    if (conn != null) {
                        conn.close();
                    }
                } catch (SQLException throwables) {
                    try {
                        conn.rollback();
                        conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    pw.write("{\"code\":-3, \"data\":\"数据库写入失败\"}");
                }
            } else if (jwtVer == -1) {
                pw.write("{\"code\":-1, \"data\":\"非法token\"}");
            } else {
                pw.write("{\"code\":-1, \"data\":\"token过期\"}");
            }
        }
        pw.close();
    }

    private void setGSeats(String train_id, Connection conn) throws SQLException {
        PreparedStatement ps = null;
        ps = conn.prepareStatement("INSERT INTO Seats (train_id, carriage_id, seat_id, seat_type, price) VALUES (?,?,?,?,?)");
        ps.setString(1, train_id);
        int sw = (int) (Math.random() * 2);
        for (int i = 0; i < sw; i++) { // 商务座
            ps.setInt(2, i + 1);
            ps.setString(4, "商务座");
            ps.setFloat(5, 1.47f);
            for (int j = 0; j < 10; j++) {
                String seat = String.valueOf(j + 1);
                if (j < 9) {
                    seat = "0" + seat;
                }
                for (int k = 0; k < 3; k++) {
                    switch (k) {
                        case 0:
                            ps.setString(3, seat + "A");
                            break;
                        case 1:
                            ps.setString(3, seat + "C");
                            break;
                        case 2:
                            ps.setString(3, seat + "F");
                    }
                    ps.execute();
                }
            }
        }
        int td = (int) (Math.random() * 2) + sw;
        for (int i = 0; i < td; i++) { // 特等座
            ps.setInt(2, i + 1 + sw);
            ps.setString(4, "特等座");
            ps.setFloat(5, 1.02f);
            for (int j = 0; j < 13; j++) {
                String seat = String.valueOf(j + 1);
                if (j < 9) {
                    seat = "0" + seat;
                }
                for (int k = 0; k < 3; k++) {
                    switch (k) {
                        case 0:
                            ps.setString(3, seat + "A");
                            break;
                        case 1:
                            ps.setString(3, seat + "C");
                            break;
                        case 2:
                            ps.setString(3, seat + "F");
                    }
                    ps.execute();
                }
            }
        }
        int yd = (int) (Math.random() * 4) + sw + td + 1;
        for (int i = 0; i < yd; i++) { // 一等座
            ps.setInt(2, i + 1 + sw + td);
            ps.setString(4, "一等座");
            ps.setFloat(5, 0.8f);
            for (int j = 0; j < 15; j++) {
                String seat = String.valueOf(j + 1);
                if (j < 9) {
                    seat = "0" + seat;
                }
                for (int k = 0; k < 4; k++) {
                    switch (k) {
                        case 0:
                            ps.setString(3, seat + "A");
                            break;
                        case 1:
                            ps.setString(3, seat + "C");
                            break;
                        case 2:
                            ps.setString(3, seat + "D");
                            break;
                        case 3:
                            ps.setString(3, seat + "F");
                    }
                    ps.execute();
                }
            }
        }
        int ed = yd + 1;
        for (int i = 0; i < ed; i++) { // 二等座
            ps.setInt(2, i + 1 + sw + td + yd);
            ps.setString(4, "二等座");
            ps.setFloat(5, 0.45f);
            for (int j = 0; j < 20; j++) {
                String seat = String.valueOf(j + 1);
                if (j < 9) {
                    seat = "0" + seat;
                }
                for (int k = 0; k < 5; k++) {
                    switch (k) {
                        case 0:
                            ps.setString(3, seat + "A");
                            break;
                        case 1:
                            ps.setString(3, seat + "B");
                            break;
                        case 2:
                            ps.setString(3, seat + "C");
                            break;
                        case 3:
                            ps.setString(3, seat + "D");
                            break;
                        case 4:
                            ps.setString(3, seat + "F");
                    }
                    ps.execute();
                }
            }
        }
    }

    private void setDSeats(String train_id, Connection conn) throws SQLException {
        PreparedStatement ps = null;
        ps = conn.prepareStatement("INSERT INTO Seats (train_id, carriage_id, seat_id, seat_type, price) VALUES (?,?,?,?,?)");
        ps.setString(1, train_id);
        int sw = (int) (Math.random() * 2);
        for (int i = 0; i < sw; i++) { // 商务座
            ps.setInt(2, i + 1);
            ps.setString(4, "商务座");
            ps.setFloat(5, 1.42f);
            for (int j = 0; j < 10; j++) {
                String seat = String.valueOf(j + 1);
                if (j < 9) {
                    seat = "0" + seat;
                }
                for (int k = 0; k < 3; k++) {
                    switch (k) {
                        case 0:
                            ps.setString(3, seat + "A");
                            break;
                        case 1:
                            ps.setString(3, seat + "C");
                            break;
                        case 2:
                            ps.setString(3, seat + "F");
                    }
                    ps.execute();
                }
            }
        }
        int td = (int) (Math.random() * 2) + sw;
        for (int i = 0; i < td; i++) { // 特等座
            ps.setInt(2, i + 1 + sw);
            ps.setString(4, "特等座");
            ps.setFloat(5, 0.99f);
            for (int j = 0; j < 13; j++) {
                String seat = String.valueOf(j + 1);
                if (j < 9) {
                    seat = "0" + seat;
                }
                for (int k = 0; k < 3; k++) {
                    switch (k) {
                        case 0:
                            ps.setString(3, seat + "A");
                            break;
                        case 1:
                            ps.setString(3, seat + "C");
                            break;
                        case 2:
                            ps.setString(3, seat + "F");
                    }
                    ps.execute();
                }
            }
        }
        int yd = (int) (Math.random() * 4) + sw + td + 1;
        for (int i = 0; i < yd; i++) { // 一等座
            ps.setInt(2, i + 1 + sw + td);
            ps.setString(4, "一等座");
            ps.setFloat(5, 0.72f);
            for (int j = 0; j < 15; j++) {
                String seat = String.valueOf(j + 1);
                if (j < 9) {
                    seat = "0" + seat;
                }
                for (int k = 0; k < 4; k++) {
                    switch (k) {
                        case 0:
                            ps.setString(3, seat + "A");
                            break;
                        case 1:
                            ps.setString(3, seat + "C");
                            break;
                        case 2:
                            ps.setString(3, seat + "D");
                            break;
                        case 3:
                            ps.setString(3, seat + "F");
                    }
                    ps.execute();
                }
            }
        }
        int ed = yd + 1;
        for (int i = 0; i < ed; i++) { // 二等座
            ps.setInt(2, i + 1 + sw + td + yd);
            ps.setString(4, "二等座");
            ps.setFloat(5, 0.44f);
            for (int j = 0; j < 20; j++) {
                String seat = String.valueOf(j + 1);
                if (j < 9) {
                    seat = "0" + seat;
                }
                for (int k = 0; k < 5; k++) {
                    switch (k) {
                        case 0:
                            ps.setString(3, seat + "A");
                            break;
                        case 1:
                            ps.setString(3, seat + "B");
                            break;
                        case 2:
                            ps.setString(3, seat + "C");
                            break;
                        case 3:
                            ps.setString(3, seat + "D");
                            break;
                        case 4:
                            ps.setString(3, seat + "F");
                    }
                    ps.execute();
                }
            }
        }
    }

    private void setSeats(String train_id, Connection conn) throws SQLException {
        PreparedStatement ps = null;
        ps = conn.prepareStatement("INSERT INTO Seats (train_id, carriage_id, seat_id, seat_type, price) VALUES (?,?,?,?,?)");
        ps.setString(1, train_id);
        float more = 0.00f;
        switch (train_id.substring(0,1)) {
            case "T": case "Z":
                more = 0.02f;
                break;
            case "K":
                more = 0.01f;
                break;
            default:
                more = 0.00f;
        }
        int crgNum = 0, r;
        r = (int) (Math.random() * 2) + 3;
        for (int i = 0; i < r; i++) {
            ps.setInt(2, i + crgNum + 1);
            ps.setString(4, "硬座");
            ps.setFloat(5, 0.11f + more);
            for (int j = 0; j < 25; j++) {
                String seat = String.valueOf(j + 1);
                if (j < 9) {
                    seat = "0" + seat;
                }
                for (int k = 0; k < 5; k++) {
                    switch (k) {
                        case 0:
                            ps.setString(3, seat + "A");
                            break;
                        case 1:
                            ps.setString(3, seat + "B");
                            break;
                        case 2:
                            ps.setString(3, seat + "C");
                            break;
                        case 3:
                            ps.setString(3, seat + "D");
                            break;
                        case 4:
                            ps.setString(3, seat + "F");
                    }
                    ps.execute();
                }
            }
        }
        crgNum += r;
        r = (int) (Math.random() * 2) + 3;
        for (int i = 0; i < r; i++) {
            ps.setInt(2, i + crgNum + 1);
            ps.setString(4, "软座");
            ps.setFloat(5, 0.16f + more);
            for (int j = 0; j < 25; j++) {
                String seat = String.valueOf(j + 1);
                if (j < 9) {
                    seat = "0" + seat;
                }
                for (int k = 0; k < 4; k++) {
                    switch (k) {
                        case 0:
                            ps.setString(3, seat + "A");
                            break;
                        case 1:
                            ps.setString(3, seat + "C");
                            break;
                        case 2:
                            ps.setString(3, seat + "D");
                            break;
                        case 3:
                            ps.setString(3, seat + "F");
                    }
                    ps.execute();
                }
            }
        }
        crgNum += r;
        r = (int) (Math.random() * 2) + 3;
        for (int i = 0; i < r; i++) {
            ps.setInt(2, i + crgNum + 1);
            ps.setString(4, "硬卧");
            for (int j = 0; j < 20; j++) {
                String seat = String.valueOf(j + 1);
                if (j < 9) {
                    seat = "0" + seat;
                }
                for (int k = 0; k < 3; k++) {
                    switch (k) {
                        case 0:
                            ps.setString(3, seat + "上");
                            ps.setFloat(5, 0.19f + more);
                            break;
                        case 1:
                            ps.setString(3, seat + "中");
                            ps.setFloat(5, 0.2f + more);
                            break;
                        case 2:
                            ps.setString(3, seat + "下");
                            ps.setFloat(5, 0.21f + more);
                    }
                    ps.execute();
                }
            }
        }
        crgNum += r;
        r = (int) (Math.random() * 2) + 3;
        for (int i = 0; i < r; i++) {
            ps.setInt(2, i + crgNum + 1);
            ps.setString(4, "软卧");
            for (int j = 0; j < 20; j++) {
                String seat = String.valueOf(j + 1);
                if (j < 9) {
                    seat = "0" + seat;
                }
                for (int k = 0; k < 2; k++) {
                    switch (k) {
                        case 0:
                            ps.setString(3, seat + "上");
                            ps.setFloat(5, 0.31f + more);
                            break;
                        case 1:
                            ps.setString(3, seat + "下");
                            ps.setFloat(5, 0.32f + more);
                    }
                    ps.execute();
                }
            }
        }
    }

}
