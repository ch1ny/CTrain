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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author SDU德布罗煜
 * @Date 2021/8/14 9:47
 * @Description 搜索换乘路线
 * @Version 1.0
 */

@WebServlet("/service/searchTransfer")
public class SearchTransfer extends HttpServlet {
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
                    PreparedStatement ps = conn.prepareStatement(SearchTransferSQL);
                    String start = req.getParameter("start");
                    String dest = req.getParameter("dest");
                    ps.setString(1, start);
                    ps.setString(2, dest);
                    ps.execute();
                    ResultSet rs = ps.getResultSet();
                    List<Map> routes = new ArrayList<>();
                    while (rs.next()) {
                        Map<String, Object> map = new HashMap<>();
                        ps = conn.prepareStatement("SELECT TrainStation.station_name, stop_index, day_num, arrive_time, stop_time, distance, location FROM TrainStation INNER JOIN Stations ON TrainStation.station_name = Stations.station_name WHERE train_id = ? ORDER BY stop_index ASC");
                        ps.setString(1, rs.getString("from_id"));
                        ps.execute();
                        ResultSet _rs = ps.getResultSet();
                        List<Map> fromRoutes = new ArrayList<>();
                        boolean access = false;
                        int days = 0;
                        String startTime = null, endTime = null;
                        int fromStartDay = 1, toStartDay = 1;
                        while (_rs.next()) {
                            Map<String, Object> station = new HashMap<>();
                            if (_rs.getString("location").equals(start)) {
                                access = true;
                                fromStartDay = _rs.getInt("day_num");
                                startTime = _rs.getString("arrive_time");
                                int hour = Integer.parseInt(startTime.split(":")[0]);
                                int minute = Integer.parseInt(startTime.split(":")[1]);
                                minute += _rs.getInt("stop_time");
                                if (minute >= 60) {
                                    minute -= 60;
                                    hour += 1;
                                    if (hour >= 24) {
                                        hour -= 24;
                                        fromStartDay++;
                                    }
                                }
                                startTime = String.format("%02d", hour) + ":" + String.format("%02d", minute);
                            }
                            station.put("station", _rs.getString("station_name"));
                            station.put("index", _rs.getInt("stop_index"));
                            station.put("days", _rs.getInt("day_num"));
                            station.put("arrive", _rs.getString("arrive_time"));
                            station.put("stop", _rs.getInt("stop_time"));
                            station.put("distance", _rs.getInt("distance"));
                            station.put("access", access);
                            if (_rs.getString("station_name").equals(rs.getString("off_station"))) {
                                access = false;
                                days = _rs.getInt("day_num") - fromStartDay;
                                map.put("offTime", _rs.getString("arrive_time"));
                                map.put("fromDays", days);
                            }
                            fromRoutes.add(station);
                        }
                        ps.setString(1, rs.getString("to_id"));
                        ps.execute();
                        _rs = ps.getResultSet();
                        List<Map> toRoutes = new ArrayList<>();
                        while (_rs.next()) {
                            Map<String, Object> station = new HashMap<>();
                            if (_rs.getString("station_name").equals(rs.getString("on_station"))) {
                                access = true;
                                toStartDay = _rs.getInt("day_num");
                                int hour = Integer.parseInt(_rs.getString("arrive_time").split(":")[0]);
                                int minute = Integer.parseInt(_rs.getString("arrive_time").split(":")[1]);
                                minute += _rs.getInt("stop_time");
                                if (minute >= 60) {
                                    minute -= 60;
                                    hour += 1;
                                    if (hour >= 24) {
                                        hour -= 24;
                                        toStartDay++;
                                    }
                                }
                                if ((String.format("%02d", hour) + ":" + String.format("%02d", minute)).compareTo(map.get("offTime").toString()) <= 0) {
                                    days++;
                                }
                                map.put("onTime", String.format("%02d", hour) + ":" + String.format("%02d", minute));
                            }
                            station.put("station", _rs.getString("station_name"));
                            station.put("index", _rs.getInt("stop_index"));
                            station.put("days", _rs.getInt("day_num"));
                            station.put("arrive", _rs.getString("arrive_time"));
                            station.put("stop", _rs.getInt("stop_time"));
                            station.put("distance", _rs.getInt("distance"));
                            station.put("access", access);
                            if (_rs.getString("location").equals(dest)) {
                                access = false;
                                endTime = _rs.getString("arrive_time");
                                days += (_rs.getInt("day_num") - toStartDay);
                            }
                            toRoutes.add(station);
                        }
                        map.put("fromStation", rs.getString("from_station"));
                        map.put("fromTrain", rs.getString("from_id"));
                        map.put("toStation", rs.getString("to_station"));
                        map.put("toTrain", rs.getString("to_id"));
                        map.put("offStation", rs.getString("off_station"));
                        map.put("onStation", rs.getString("on_station"));
                        map.put("startTime", startTime);
                        map.put("endTime", endTime);
                        map.put("fromRoutes", fromRoutes);
                        map.put("toRoutes", toRoutes);
                        map.put("days", days);
                        routes.add(map);
                    }
                    pw.write("{\"code\":0, \"data\":" + JSON.toJSONString(routes) + "}");
                } catch (SQLException throwables) {
                    pw.write("{\"code\":-2, \"data\":\"数据库错误\"}");
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


    private String SearchTransferSQL = "SELECT A.location, A.train_id AS from_id, A.station_name AS off_station, B.train_id AS to_id, B.station_name AS on_station, from_station, to_station\n" +
            "FROM (\n" +
            "    SELECT location, A.station_name, B.train_id, from_station\n" +
            "    FROM Stations A\n" +
            "    INNER JOIN (\n" +
            "   \t\tSELECT station_name, A.train_id, from_station\n" +
            "        FROM TrainStation A\n" +
            "        INNER JOIN (\n" +
            "        \tSELECT train_id, stop_index, A.station_name AS from_station\n" +
            "            FROM TrainStation A\n" +
            "            WHERE EXISTS (\n" +
            "            \tSELECT 1\n" +
            "\t            FROM Stations B\n" +
            "                WHERE A.station_name = B.station_name AND B.location = ?\n" +
            "\t\t\t)\n" +
            "\t\t) B\n" +
            "        ON A.train_id = B.train_id\n" +
            "        WHERE A.stop_index > B.stop_index\n" +
            "    ) B\n" +
            "    ON A.station_name = B.station_name\n" +
            ") A\n" +
            "INNER JOIN (\n" +
            "    SELECT location, A.station_name, B.train_id, to_station\n" +
            "    FROM Stations A\n" +
            "    INNER JOIN (\n" +
            "   \t\tSELECT station_name, A.train_id, to_station\n" +
            "        FROM TrainStation A\n" +
            "        INNER JOIN (\n" +
            "        \tSELECT train_id, stop_index, A.station_name AS to_station\n" +
            "            FROM TrainStation A\n" +
            "            WHERE EXISTS (\n" +
            "            \tSELECT 1\n" +
            "\t            FROM Stations B\n" +
            "                WHERE A.station_name = B.station_name AND B.location = ?\n" +
            "\t\t\t)\n" +
            "\t\t) B\n" +
            "        ON A.train_id = B.train_id\n" +
            "        WHERE A.stop_index < B.stop_index\n" +
            "    ) B\n" +
            "    ON A.station_name = B.station_name\n" +
            ") B\n" +
            "ON A.location = B.location";
}
