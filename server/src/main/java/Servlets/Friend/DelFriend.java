package Servlets.Friend;

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
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

/**
 * @Author SDU德布罗煜
 * @Date 2021/8/10 21:58
 * @Description 移除同行旅客
 * @Version 1.0
 */

@WebServlet("/friend/del")
public class DelFriend extends HttpServlet {

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
                Connection conn = null;
                Claims c = JjwtUtil.parseJWT(jwt);
                try {
                    String userId = c.get("sub", String.class);
                    String friendId = req.getParameter("id");
                    int resultCode = 0;
                    if (userId.equals(friendId)) {
                        resultCode = -1;
                    } else {
                        conn = SqlCon.getInstance();
                        PreparedStatement ps = conn.prepareStatement("DELETE FROM Friends WHERE user_id=? AND friend_id=?");
                        ps.setString(1, userId);
                        ps.setString(2, friendId);
                        try {
                            ps.execute();
                        } catch (SQLIntegrityConstraintViolationException e1) {
                            resultCode = -2;
                        }
                        if (resultCode == 0) {
                            conn.commit();
                        }
                    }
                    pw.write("{\"code\": " + resultCode + ", \"data\":\"" + getResult(resultCode) + "\"}");
                }
                catch (SQLException e) {
                    if (conn != null) {
                        try {
                            conn.rollback();
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                    }
                } finally {
                    if (conn != null) {
                        try {
                            conn.close();
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                    }
                }
            } else if (jwtVer == -1) {
                pw.write("{\"code\":-3, \"data\":\"非法token\"}");
            } else {
                pw.write("{\"code\":-3, \"data\":\"token过期\"}");
            }
        }
        pw.close();
    }

    private String getResult(int code) {
        String result = null;
        switch (code) {
            case 0:
                result = "删除成功";
                break;
            case -1:
                result = "禁止删除自己";
                break;
            case -2:
                result = "删除失败，数据库发生错误";
        }
        return result;
    }

}
