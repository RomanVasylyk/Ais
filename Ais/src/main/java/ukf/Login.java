package ukf;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@WebServlet("/Login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static final String JDBC_URL = "jdbc:mysql://localhost/Ais";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "";

    public Login() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Login</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; text-align: center; margin: 20px; }");
        out.println("h2 { color: #333; }");
        out.println("form { width: 300px; margin: 0 auto; padding: 20px; background-color: #f7f7f7; border-radius: 5px; box-shadow: 0px 0px 10px 0px #aaa; }");
        out.println("label { display: block; margin-bottom: 10px; }");
        out.println("input[type='text'], input[type='password'] { width: 100%; padding: 10px; margin-bottom: 20px; border: 1px solid #ccc; border-radius: 5px; }");
        out.println("input[type='submit'] { background-color: #333; color: #fff; padding: 10px 20px; border: none; border-radius: 5px; cursor: pointer; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h2>Login</h2>");
        out.println("<form method='post' action='Login'>");
        out.println("<label for='username'>Username:</label>");
        out.println("<input type='text' id='username' name='username'><br>");
        out.println("<label for='password'>Password:</label>");
        out.println("<input type='password' id='password' name='password'><br>");
        out.println("<input type='submit' value='Login'>");
        out.println("</form>");
        out.println("</body>");
        out.println("</html>");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (authenticateUser(username, password, request)) {
            response.sendRedirect("Main");
        } else {
        
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Authentication Error</title>");
            out.println("<style>");
            out.println("body { font-family: Arial, sans-serif; text-align: center; margin: 20px; }");
            out.println("h2 { color: #333; }");
            out.println("</style>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h2>Authentication Error</h2>");
            out.println("<p>Invalid username or password. Please try again.</p>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    private boolean authenticateUser(String username, String password, HttpServletRequest request) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
        	Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);

            String sql = "SELECT UserID, Password FROM Users WHERE UserName = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("Password");

                if (storedPassword.equals(password)) { 
                    int userId = rs.getInt("UserID");

                    sql = "SELECT RoleName FROM Roles INNER JOIN UserRoles ON Roles.RoleID = UserRoles.RoleID WHERE UserRoles.UserID = ?";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, userId);
                    ResultSet rsRole = pstmt.executeQuery();

                    if (rsRole.next()) {
                        String roleName = rsRole.getString("RoleName");

                        HttpSession session = request.getSession();
                        session.setAttribute("userID", userId);
                        session.setAttribute("role", roleName);

                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {};
            try { if (pstmt != null) pstmt.close(); } catch (Exception e) {};
            try { if (conn != null) conn.close(); } catch (Exception e) {};
        }
        return false;
    }
}
