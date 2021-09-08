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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author SDU德布罗煜
 * @Date 2021/9/5 10:34
 * @Description 移除订单记录
 * @Version 1.0
 */

@WebServlet("/service/deleteTicket")
public class DeleteTicket extends HttpServlet {
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
                String ticket = req.getParameter("ticket").substring(1);
                try {
                    PreparedStatement ps = conn.prepareStatement("SELECT off_date FROM Tickets WHERE ticket_id = ? AND user_id = ? ORDER BY off_date");
                    ps.setString(1, ticket);
                    ps.setString(2, userId);
                    ps.execute();
                    ResultSet rs = ps.getResultSet();
                    if (rs.next()) {
                        String offDate = rs.getString("off_date");
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        if (offDate.compareTo(sdf.format(new Date())) < 0) {
                            ps = conn.prepareStatement("DELETE FROM Tickets WHERE ticket_id = ?");
                            ps.setString(1, ticket);
                            ps.execute();
                            conn.commit();
                            pw.write("{\"code\": 0, \"data\":\"移除成功\"}");
                        } else {
                            pw.write("{\"code\": -2, \"data\":\"该订单暂时不可移除！\"}");
                        }
                    } else {
                        pw.write("{\"code\":-2, \"data\":\"该订单不存在！\"}");
                    }
                } catch (SQLException e) {
                    try {
                        conn.rollback();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                    e.printStackTrace();
                    pw.write("{\"code\": -3, \"data\":\"数据库错误\"}");
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
}
