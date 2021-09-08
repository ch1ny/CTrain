package Servlets.Service;

import Utils.JjwtUtil;
import Utils.SnowFlakeUtil;
import Utils.SqlCon;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

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
 * @Date 2021/8/15 17:15
 * @Description 生成订单
 * @Version 1.0
 */

@WebServlet("/service/purchase")
public class Purchase extends HttpServlet {
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
                String indentStr = req.getParameter("indents");
                JSONArray indents = JSON.parseArray(indentStr);
                String ticket = String.valueOf(SnowFlakeUtil.getInstance().getNextId());
                try {
                    Date now = new Date();
                    for (int i = 0; i < indents.size(); i++) {
                        JSONObject indent = indents.getJSONObject(i);
                        String train = indent.getString("train");
                        String from = indent.getString("from");
                        String to = indent.getString("to");
                        String date = indent.getString("date");
                        String seat = indent.getString("seat");
                        String passengersStr = indent.getString("passengers");
                        float price = indent.getFloat("price");
                        JSONArray passengers = JSON.parseArray(passengersStr);
                        int pNum = passengers.size();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(sdf.parse(date));
                        if (i == 0) {
                            Calendar today = Calendar.getInstance();
                            Calendar limit = Calendar.getInstance();
                            limit.setTime(sdf.parse(date));
                            if (limit.before(today)) {
                                pw.write("{\"code\":-3, \"data\":\"无法购买已出行的车票\"}");
                                pw.close();
                                conn.close();
                                return;
                            }
                            limit.set(Calendar.DATE, limit.get(Calendar.DATE) - 15);
                            if (limit.after(today)) {
                                pw.write("{\"code\":-3, \"data\":\"无法购买15天后的车票\"}");
                                pw.close();
                                conn.close();
                                return;
                            }
                        }
                        PreparedStatement ps = conn.prepareStatement("SELECT arrive_time, stop_time, day_num - 1 days FROM TrainStation WHERE train_id = ? AND station_name = ?");
                        ps.setString(1, train);
                        ps.setString(2, from);
                        ps.execute();
                        ResultSet rs = ps.getResultSet();
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
                            ps.setString(1, train);
                            ps.setString(2, from);
                            ps.setString(3, to);
                            ps.setString(4, seat);
                            ps.setString(5, train);
                            ps.setString(6, train);
                            ps.setString(7, train);
                            ps.setString(8, sdf.format(calendar.getTime()));
                            ps.execute();
                            rs = ps.getResultSet();
                            rs.last();
                            int sNum = rs.getRow();
                            if (sNum < pNum) {
                                conn.rollback();
                                pw.write("{\"code\":-3, \"data\":\"无余票\"}");
                                return;
                            }
                            Random r = new Random();
                            rs.absolute(r.nextInt(sNum - pNum));
                            try {
                                ps = conn.prepareStatement("INSERT INTO Tickets (ticket_id, user_id, passenger_id, owner, train_id, off_date, from_station, to_station, carriage_id, seat_id, price, purchase_time, purchased) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
                                ps.setString(1, ticket);
                                ps.setString(2, userId);
                                ps.setString(5, train);
                                ps.setString(6, sdf.format(calendar.getTime()));
                                ps.setString(7, from);
                                ps.setString(8, to);
                                ps.setFloat(11, price);
                                ps.setInt(13, 0);
                                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                ps.setString(12, sdf.format(now));
                                for (int j = 0; j < pNum; j++) {
                                    rs.next();
                                    JSONObject json = passengers.getJSONObject(j);
                                    ps.setString(3, json.getString("id"));
                                    ps.setString(4, json.getString("name"));
                                    ps.setInt(9, rs.getInt("carriage_id"));
                                    ps.setString(10, rs.getString("seat_id"));
                                    ps.execute();
                                }
                                String eventSQL = "CREATE EVENT IF NOT EXISTS remove_outtime_ticket_" + ticket +  " ON SCHEDULE AT CURRENT_TIMESTAMP + INTERVAL 30 MINUTE DO DELETE FROM Tickets WHERE ticket_id = ? AND purchased = 0";
                                ps = conn.prepareStatement(eventSQL);
                                ps.setString(1, ticket);
                                ps.execute();
                            } catch (SQLIntegrityConstraintViolationException e) {
                                conn.rollback();
                                pw.write("{\"code\":-3, \"data\":\"请不要重复购买\"}");
                            }
                        }
                    }
                    conn.commit();
                    pw.write("{\"code\":0, \"data\":\"购票成功\"}");
                } catch (SQLException e) {
                    e.printStackTrace();
                    try {
                        conn.rollback();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                    pw.write("{\"code\":-2, \"data\":\"数据库错误\"}");
                } catch (ParseException e) {
                    e.printStackTrace();
                    try {
                        conn.rollback();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                    pw.write("{\"code\":-2, \"data\":\"请按照yyyy-MM-dd格式输入日期\"}");
                }
                try {
                    conn.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
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
