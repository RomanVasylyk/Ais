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


@WebServlet("/EditSubject")
public class EditSubjectServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String JDBC_URL = "jdbc:mysql://localhost/Ais";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "";

    public EditSubjectServlet() {
        super();
    }


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int subjectID = Integer.parseInt(request.getParameter("subjectID"));
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);

            String subjectQuery = "SELECT * FROM Subjects WHERE SubjectID = ?";
            pstmt = conn.prepareStatement(subjectQuery);
            pstmt.setInt(1, subjectID);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                String subjectName = rs.getString("SubjectName");

                PrintWriter out = response.getWriter();
                out.println("<html><head><title>Edit Subject</title></head><body>");
                out.println("<h1>Edit Subject</h1>");
                out.println("<form method='post' action='EditSubject'>");
                out.println("<input type='hidden' name='subjectID' value='" + subjectID + "'>");
                out.println("Subject Name: <input type='text' name='subjectName' value='" + subjectName + "'><br>");
                out.println("<input type='submit' value='Save Changes'>");
                out.println("</form>");
                out.println("</body></html>");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
    }


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int subjectID = Integer.parseInt(request.getParameter("subjectID"));
        String newSubjectName = request.getParameter("subjectName");

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);

            String updateQuery = "UPDATE Subjects SET SubjectName = ? WHERE SubjectID = ?";
            pstmt = conn.prepareStatement(updateQuery);
            pstmt.setString(1, newSubjectName);
            pstmt.setInt(2, subjectID);
            pstmt.executeUpdate();


            response.sendRedirect("Main"); 
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
    }

}
