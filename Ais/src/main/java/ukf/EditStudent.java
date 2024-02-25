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

@WebServlet("/EditStudent")
public class EditStudent extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String JDBC_URL = "jdbc:mysql://localhost/Ais";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "";

    public EditStudent() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);

            int studentID = Integer.parseInt(request.getParameter("studentID"));

            String studentQuery = "SELECT * FROM Users WHERE UserID = ?";
            pstmt = conn.prepareStatement(studentQuery);
            pstmt.setInt(1, studentID);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                String studentName = rs.getString("UserName");
                String studentEmail = rs.getString("Email");

                out.println("<h1>Edit Student Data:</h1>");
                out.println("<form method='post' action='EditStudent'>");
                out.println("Name: <input type='text' name='name' value='" + studentName + "'><br>");
                out.println("Email: <input type='text' name='email' value='" + studentEmail + "'><br>");
                out.println("<input type='hidden' name='studentID' value='" + studentID + "'>");
                out.println("<input type='submit' value='Save Changes'>");
                out.println("</form>");
            } else {
                out.println("<h1>Student not found.</h1>");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null)
                    rs.close();
            } catch (Exception e) {
            }
            try {
                if (pstmt != null)
                    pstmt.close();
            } catch (Exception e) {
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (Exception e) {
            }
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);

            int studentID = Integer.parseInt(request.getParameter("studentID"));
            String studentName = request.getParameter("name");
            String studentEmail = request.getParameter("email");

            String updateQuery = "UPDATE Users SET UserName = ?, Email = ? WHERE UserID = ?";
            pstmt = conn.prepareStatement(updateQuery);
            pstmt.setString(1, studentName);
            pstmt.setString(2, studentEmail);
            pstmt.setInt(3, studentID);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                response.sendRedirect("Main"); 
            } else {
                out.println("<h1>Failed to update student data.</h1>");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null)
                    pstmt.close();
            } catch (Exception e) {
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (Exception e) {
            }
        }
    }
}
