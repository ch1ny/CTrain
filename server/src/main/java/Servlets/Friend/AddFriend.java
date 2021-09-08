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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author SDU德布罗煜
 * @Date 2021/8/8 10:23
 * @Description 添加同行旅客
 * @Version 1.0
 */

@WebServlet("/friend/add")
public class AddFriend extends HttpServlet {

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
                    String id = req.getParameter("id");
                    String name = req.getParameter("name");
                    int resultCode = verify(id);
                    if (resultCode == 0) {
                        conn = SqlCon.getInstance();
                        PreparedStatement ps = conn.prepareStatement("INSERT INTO Friends (user_id, friend_id, friend_name) VALUES (?,?,?)");
                        ps.setString(1, userId);
                        ps.setString(2, id);
                        ps.setString(3, name);
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
                pw.write("{\"code\":-1, \"data\":\"非法token\"}");
            } else {
                pw.write("{\"code\":-1, \"data\":\"token过期\"}");
            }
        }
        pw.close();
    }

    private int verify(String id) {
        if (id.length() == 18) {
            String former = id.substring(0, 17);
            if (isNumeric(former)) {
                String[] verifyCode = { "1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2" };
                int[] weights = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2 };
                int sum = 0;
                for (int i = 0; i < 17; i++) {
                    sum = sum + (former.charAt(i) - 48) * weights[i];
                }
                int modValue = sum % 11;
                String strVerifyCode = verifyCode[modValue];
                if (strVerifyCode.equals(id.substring(17))) {
                    return 0;
                }
            }
        }
        return -1;
    }

    private boolean isNumeric(String strnum) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(strnum);
        if (isNum.matches()) {
            return true;
        } else {
            return false;
        }
    }

    private String getResult(int code) {
        String result = null;
        switch (code) {
            case 0:
                result = "添加成功";
                break;
            case -1:
                result = "请输入有效的身份证号码";
                break;
            case -2:
                result = "请勿重复添加";
        }
        return result;
    }
}
