package Servlets.Service;

import Utils.JjwtUtil;
import Utils.SqlCon;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * @Author SDU德布罗煜
 * @Date 2021/8/16 11:46
 * @Description 改签
 * @Version 1.0
 */

@WebServlet("/service/reboot")
public class Reboot extends HttpServlet {
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
                Connection conn = SqlCon.getInstance();
                String userId = JjwtUtil.parseJWT(jwt).getSubject();
                String ticket = req.getParameter("ticket");
                int days = Integer.parseInt(req.getParameter("days"));
                try {
                    PreparedStatement ps = conn.prepareStatement("SELECT passenger_id, owner, A.train_id, off_date, from_station, to_station, A.carriage_id, A.price, seat_type FROM Tickets A INNER JOIN Seats B ON A.train_id = B.train_id AND A.carriage_id = B.carriage_id AND A.seat_id = B.seat_id WHERE ticket_id = ? AND user_id = ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    ps.setString(1, ticket.substring(1));
                    ps.setString(2, userId);
                    ps.execute();
                    ResultSet rs = ps.getResultSet();
                    List<Map> indents = new ArrayList<>();
                    if (rs.next()) {
                        rs.beforeFirst();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Calendar calendar = Calendar.getInstance();
                        while (rs.next()) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("id", rs.getString("passenger_id"));
                            map.put("owner", rs.getString("owner"));
                            map.put("train", rs.getString("train_id"));
                            map.put("from", rs.getString("from_station"));
                            map.put("to", rs.getString("to_station"));
                            map.put("carriage", rs.getString("carriage_id"));
                            map.put("price", rs.getFloat("price"));
                            map.put("seat", rs.getString("seat_type"));
                            calendar.setTime(sdf.parse(rs.getString("off_date")));
                            calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + days);
                            map.put("date", sdf.format(calendar.getTime()));
                            indents.add(map);
                        }
                        ps = conn.prepareStatement("DELETE FROM Tickets WHERE ticket_id = ? AND user_id = ?");
                        ps.setString(1, ticket.substring(1));
                        ps.setString(2, userId);
                        ps.execute();
                        for (int i = 0; i < indents.size(); i++) {
                            Map indent = indents.get(i);
                            ps = conn.prepareStatement("SELECT arrive_time, stop_time, day_num - 1 days FROM TrainStation WHERE train_id = ? AND station_name = ?");
                            ps.setString(1, String.valueOf(indent.get("train")));
                            ps.setString(2, String.valueOf(indent.get("from")));
                            ps.execute();
                            rs = ps.getResultSet();
                            if (rs.next()) {
                                String arriveTime = rs.getString("arrive_time");
                                int stopTime = rs.getInt("stop_time");
                                int hour = Integer.parseInt(arriveTime.split(":")[0]);
                                int minute = Integer.parseInt(arriveTime.split(":")[1]);
                                minute += stopTime;
                                if (minute >= 60) {
                                    hour += 1;
                                    if (hour >= 24) {
                                        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 1);
                                    }
                                }
                                calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - rs.getInt("days"));
                                ps = conn.prepareStatement(purchaseSQL, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                                ps.setString(1, String.valueOf(indent.get("train")));
                                ps.setString(2, String.valueOf(indent.get("from")));
                                ps.setString(3, String.valueOf(indent.get("to")));
                                ps.setString(4, String.valueOf(indent.get("seat")));
                                ps.setString(5, String.valueOf(indent.get("train")));
                                ps.setString(6, String.valueOf(indent.get("train")));
                                ps.setString(7, String.valueOf(indent.get("train")));
                                ps.setString(8, String.valueOf(indent.get("date")));
                                ps.execute();
                                rs = ps.getResultSet();
                                rs.last();
                                int sNum = rs.getRow();
                                if (sNum < 1) {
                                    conn.rollback();
                                    pw.write("{\"code\":-3, \"data\":\"无余票\"}");
                                    return;
                                }
                                Random r = new Random();
                                rs.absolute(r.nextInt(sNum - 1));
                                rs.next();
                                try {
                                    ps = conn.prepareStatement("INSERT INTO Tickets (ticket_id, user_id, passenger_id, owner, train_id, off_date, from_station, to_station, carriage_id, seat_id, price, purchase_time, purchased) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
                                    ps.setString(1, ticket.substring(1));
                                    ps.setString(2, userId);
                                    ps.setString(4, String.valueOf(indent.get("owner")));
                                    ps.setString(5, String.valueOf(indent.get("train")));
                                    ps.setString(6, String.valueOf(indent.get("date")));
                                    ps.setString(7, String.valueOf(indent.get("from")));
                                    ps.setString(8, String.valueOf(indent.get("to")));
                                    ps.setFloat(11, (Float) indent.get("price"));
                                    Date now = new Date();
                                    sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    ps.setString(12, sdf.format(now));
                                    ps.setInt(13, 1);
                                    ps.setString(3, String.valueOf(indent.get("id")));
                                    ps.setInt(9, rs.getInt("carriage_id"));
                                    ps.setString(10, rs.getString("seat_id"));
                                    ps.execute();
                                } catch (SQLIntegrityConstraintViolationException e) {
                                    conn.rollback();
                                    pw.write("{\"code\":-3, \"data\":\"请不要重复购买\"}");
                                }
                            }
                        }
                        conn.commit();
                        pw.write("{\"code\":0, \"data\":\"改签成功\"}");
                    } else {
                        pw.write("{\"code\": -2, \"data\":\"没有该条订单信息\"}");
                    }
                } catch (SQLException e) {
                    try {
                        conn.rollback();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                    e.printStackTrace();
                    pw.write("{\"code\": -2, \"data\":\"数据库错误\"}");
                } catch (ParseException e) {
                    try {
                        conn.rollback();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                    e.printStackTrace();
                    pw.write("{\"code\":-2, \"data\":\"请按照yyyy-MM-dd格式输入日期\"}");
                }
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else if (jwtVer == -1) {
                pw.write("{\"code\":-1, \"data\":\"非法token\"}");
            } else {
                pw.write("{\"code\":-1, \"data\":\"token过期\"}");
            }
        }
        pw.close();
    }

    private String purchaseSQL = "SELECT A.carriage_id, A.seat_id\n" +
            "FROM (\n" +
            "\tSELECT carriage_id, seat_id, from_index, to_index\n" +
            "    FROM Seats A\n" +
            "    INNER JOIN (\n" +
            "        SELECT A.stop_index from_index, B.stop_index to_index, A.train_id\n" +
            "        FROM TrainStation A\n" +
            "        INNER JOIN TrainStation B\n" +
            "        ON A.train_id = B.train_id\n" +
            "        WHERE A.train_id = ? AND A.station_name = ? AND B.station_name = ?\n" +
            "    ) B\n" +
            "    ON A.train_id = B.train_id\n" +
            "    WHERE seat_type = ?\n" +
            ") A\n" +
            "LEFT OUTER JOIN (\n" +
            "    SELECT A.stop_index from_index, B.stop_index to_index, carriage_id, seat_id\n" +
            "    FROM (\n" +
            "        SELECT station_name, stop_index\n" +
            "        FROM TrainStation\n" +
            "        WHERE train_id = ?\n" +
            "    ) A\n" +
            "    INNER JOIN (\n" +
            "        SELECT station_name, stop_index\n" +
            "        FROM TrainStation\n" +
            "        WHERE train_id = ?\n" +
            "    ) B\n" +
            "    INNER JOIN (\n" +
            "        SELECT from_station, to_station, carriage_id, seat_id\n" +
            "        FROM Tickets\n" +
            "        WHERE train_id = ? AND off_date = ?\n" +
            "        FOR UPDATE\n" +
            "    ) C\n" +
            "    ON A.station_name = C.from_station AND B.station_name = C.to_station\n" +
            ") B\n" +
            "ON A.carriage_id = B.carriage_id AND A.seat_id = B.seat_id\n" +
            "WHERE B.seat_id IS NULL OR A.from_index >= B.to_index OR A.to_index <= B.from_index";
}
