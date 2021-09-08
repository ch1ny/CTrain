package Servlets.Service;

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
import java.sql.SQLException;

/**
 * @Author SDU德布罗煜
 * @Date 2021/8/24 13:32
 * @Description 未支付时主动取消订单
 * @Version 1.0
 */

@WebServlet("/service/dontWant")
public class DontWant extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        req.setCharacterEncoding("utf-8");
        PrintWriter pw = resp.getWriter();
        String tradeId = req.getParameter("ticket");
        Connection conn = SqlCon.getInstance();
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM Tickets WHERE ticket_id = ?");
            ps.setString(1, tradeId.substring(1));
            ps.execute();
            conn.commit();
            conn.close();
            pw.write("{\"code\": 0, \"data\":\"订单取消成功\"}");
        } catch (SQLException e) {
            try {
                conn.rollback();
                conn.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            pw.write("{\"code\": -2, \"data\":\"数据库错误\"}");
            e.printStackTrace();
        }
        pw.close();
    }
}
