package Servlets.User;

import Utils.JjwtUtil;
import Utils.SqlCon;
import com.alibaba.fastjson.JSON;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author SDU德布罗煜
 * @Date 2021/8/15 14:47
 * @Description 我的订单
 * @Version 1.0
 */

@WebServlet("/user/myIndents")
public class GetMyIndents extends HttpServlet {
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
                try {
                    PreparedStatement ps = conn.prepareStatement("SELECT ticket_id, purchase_time, passenger_id, owner, A.train_id, off_date, from_station, to_station, A.price, A.carriage_id, A.seat_id, purchased, B.arrive_time bat, B.stop_time bst, B.day_num bdn, C.arrive_time cat, C.day_num cdn, seat_type FROM Tickets A INNER JOIN TrainStation B INNER JOIN TrainStation C INNER JOIN Seats D ON A.train_id = B.train_id AND A.from_station = B.station_name AND A.train_id = C.train_id AND A.to_station = C.station_name AND A.train_id = D.train_id AND A.carriage_id = D.carriage_id AND A.seat_id = D.seat_id WHERE A.user_id = ? ORDER BY purchase_time DESC, off_date ASC, bat DESC");
                    String userId = JjwtUtil.parseJWT(jwt).getSubject();
                    ps.setString(1, userId);
                    ps.execute();
                    ResultSet rs = ps.getResultSet();
                    List<Map> indents = new ArrayList<>();
                    while (rs.next()) {
                        Map<String, Object> indent = new HashMap<>();
                        indent.put("ticket", "T" + rs.getString("ticket_id"));
                        indent.put("id", rs.getString("passenger_id"));
                        indent.put("name", rs.getString("owner"));
                        indent.put("passenger", rs.getString("passenger_id"));
                        indent.put("train", rs.getString("train_id"));
                        indent.put("purchaseTime", rs.getString("purchase_time"));
                        indent.put("purchased", rs.getInt("purchased"));
                        int days = rs.getInt("bdn") - 1;
                        String arrive = rs.getString("bat");
                        int hour = Integer.parseInt(arrive.split(":")[0]);
                        int minute = Integer.parseInt(arrive.split(":")[1]);
                        minute += rs.getInt("bst");
                        if (minute >= 60) {
                            minute -= 60;
                            hour += 1;
                            if (hour >= 24) {
                                hour -= 24;
                                days++;
                            }
                        }
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(sdf.parse(rs.getString("off_date")));
                        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + days);
                        indent.put("startTime", sdf.format(calendar.getTime()) + " " + String.format("%02d", hour) + ":" + String.format("%02d", minute));
                        calendar.setTime(sdf.parse(rs.getString("off_date")));
                        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + rs.getInt("cdn") - 1);
                        indent.put("endTime", sdf.format(calendar.getTime()) + " " + rs.getString("cat"));
                        indent.put("from", rs.getString("from_station"));
                        indent.put("to", rs.getString("to_station"));
                        indent.put("carriage", rs.getInt("carriage_id"));
                        indent.put("seat", rs.getString("seat_id"));
                        indent.put("type", rs.getString("seat_type"));
                        indent.put("price", rs.getFloat("price"));
                        indents.add(indent);
                    }
                    pw.write("{\"code\":0, \"data\":" + JSON.toJSON(indents) + "}");
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                    pw.write("{\"code\":-2, \"data\":\"数据库错误\"}");
                } catch (ParseException e) {
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
}
