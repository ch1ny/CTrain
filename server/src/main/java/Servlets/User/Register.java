package Servlets.User;

import Utils.Md5Util;
import Utils.SqlCon;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author SDU德布罗煜
 * @Date 2021/7/9 18:24
 * @Description 用户注册
 * @Version 1.0
 */

@WebServlet("/user/register")
public class Register extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html; charset=UTF-8");
        req.setCharacterEncoding("utf-8");
        Connection conn = null;
        try {
            String id = req.getParameter("id").toUpperCase(Locale.ROOT);
            String email = req.getParameter("email");
            String name = req.getParameter("name");
            String captcha = req.getParameter("captcha");
            HttpSession session = req.getSession();
            if (session.getAttribute("registerCaptcha") == null || !session.getAttribute("registerCaptcha").equals(captcha + email)) {
                PrintWriter pw = resp.getWriter();
                pw.write("{\"code\":-3, \"result\":\"验证码错误或已超时\"}");
                pw.close();
                return;
            }
            int resultCode = verify(id);
            if (resultCode == 0) {
                String psw = Md5Util.SaltMd5(id, req.getParameter("psw"));
                conn = SqlCon.getInstance();
                PreparedStatement ps = conn.prepareStatement("INSERT INTO Users (user_id, user_email, user_name, user_password) VALUES (?,?,?,?)");
                ps.setString(1, id);
                ps.setString(2, email);
                ps.setString(3, name);
                ps.setString(4, psw);
                try {
                    ps.execute();
                } catch (SQLIntegrityConstraintViolationException e1) {
                    resultCode = -2;
                }
                if (resultCode == 0) {
                    ps = conn.prepareStatement("INSERT INTO Friends (user_id, friend_id, friend_name) VALUES (?,?,?)");
                    ps.setString(1, id);
                    ps.setString(2, id);
                    ps.setString(3, name);
                    ps.execute();
                    conn.commit();
                }
            }
            PrintWriter pw = resp.getWriter();
            String json = "{\"code\":" + resultCode + ",\"result\":\"" + getResult(resultCode) + "\"}";
            pw.write(json);
            pw.close();
        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
        } catch (SQLException e) {
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
                result = "注册成功";
                break;
            case -1:
                result = "请输入有效的身份证号码";
                break;
            case -2:
                result = "该身份证或电子邮箱已被注册";
        }
        return result;
    }

}
