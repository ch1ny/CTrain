package Servlets.Service;

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
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author SDU德布罗煜
 * @Date 2021/8/14 20:41
 * @Description 获取余票数量及价格
 * @Version 1.0
 */

@WebServlet("/service/getTickets")
public class GetTickets extends HttpServlet {
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
                String train = req.getParameter("train");
                String from = req.getParameter("from");
                String to = req.getParameter("to");
                String date = req.getParameter("date");
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(sdf.parse(date));
                    PreparedStatement ps = conn.prepareStatement("SELECT day_num - 1 days FROM TrainStation WHERE train_id = ? AND station_name = ?");
                    ps.setString(1, train);
                    ps.setString(2, from);
                    ps.execute();
                    ResultSet rs = ps.getResultSet();
                    if (rs.next()) {
                        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - rs.getInt("days"));
                        ps = conn.prepareStatement(GetTicketsNumSQL);
                        ps.setString(1, train);
                        ps.setString(2, from);
                        ps.setString(3, to);
                        ps.setString(4, train);
                        ps.setString(5, train);
                        ps.setString(6, train);
                        ps.setString(7, sdf.format(calendar.getTime()));
                        ps.execute();
                        rs = ps.getResultSet();
                        List<Map> tickets = new ArrayList<>();
                        while (rs.next()) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("type", rs.getString("seat_type"));
                            map.put("num", rs.getInt("num"));
                            NumberFormat nf = NumberFormat.getInstance();
                            nf.setMaximumFractionDigits(2);
                            nf.setMinimumFractionDigits(2);
                            map.put("price", nf.format(getDistance(conn, train, from, to) * getPrice(conn, rs.getString("seat_type"))));
                            tickets.add(map);
                        }
                        pw.write("{\"code\":0, \"data\":" + JSON.toJSONString(tickets) + "}");
                    }
                } catch (SQLException throwables) {
                    pw.write("{\"code\":-2, \"data\":\"数据库错误\"}");
                } catch (ParseException e) {
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

    private int getDistance(Connection conn, String train, String from, String to) throws SQLException {
        int distance = 0;
        PreparedStatement ps = conn.prepareStatement("SELECT A.distance - B.distance distance FROM (SELECT distance FROM TrainStation WHERE train_id = ? AND station_name = ?) A, (SELECT distance FROM TrainStation WHERE train_id = ? AND station_name = ?) B");
        ps.setString(1, train);
        ps.setString(2, to);
        ps.setString(3, train);
        ps.setString(4, from);
        ps.execute();
        ResultSet rs = ps.getResultSet();
        if (rs.next()) {
            distance = rs.getInt("distance");
        }
        switch (distance / 100) {
            case 0: case 1:
                distance *= 1;
                break;
            case 2: case 3: case 4:
                distance = (int) (200 + (distance - 200) * 0.9);
                break;
            case 5: case 6: case 7: case 8: case 9:
                distance = (int) (470 + (distance - 500) * 0.8);
                break;
            case 10: case 11: case 12: case 13: case 14:
                distance = (int) (870 + (distance - 1000) * 0.7);
                break;
            case 15: case 16: case 17: case 18: case 19:
                distance = (int) (1220 + (distance - 1500) * 0.6);
                break;
            default:
                distance = (int) (1520 + (distance - 2000) * 0.5);
        }
        return distance;
    }

    private float getPrice(Connection conn, String seat) throws SQLException {
        float price = 0;
        PreparedStatement ps = conn.prepareStatement("SELECT DISTINCT price FROM Seats WHERE seat_type = ?");
        ps.setString(1, seat);
        ps.execute();
        ResultSet rs = ps.getResultSet();
        if (rs.next()) {
            price = rs.getFloat("price");
        }
        return price;
    }

    private String GetTicketsNumSQL = "SELECT seat_type, COUNT(*) num\n" +
            "FROM (\n" +
            "\tSELECT seat_type, carriage_id, seat_id, from_index, to_index\n" +
            "    FROM Seats A\n" +
            "    INNER JOIN (\n" +
            "        SELECT A.stop_index from_index, B.stop_index to_index, A.train_id\n" +
            "        FROM TrainStation A\n" +
            "        INNER JOIN TrainStation B\n" +
            "        ON A.train_id = B.train_id\n" +
            "        WHERE A.train_id = ? AND A.station_name = ? AND B.station_name = ?\n" +
            "    ) B\n" +
            "    ON A.train_id = B.train_id\n" +
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
            "    ) C\n" +
            "    ON A.station_name = C.from_station AND B.station_name = C.to_station\n" +
            ") B\n" +
            "ON A.carriage_id = B.carriage_id AND A.seat_id = B.seat_id\n" +
            "WHERE B.seat_id IS NULL OR A.from_index >= B.to_index OR A.to_index <= B.from_index\n" +
            "GROUP BY A.seat_type";

}
