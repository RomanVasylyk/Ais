package ukf;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@WebServlet("/RegisterForExam")
public class RegisterForExam extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static final String JDBC_URL = "jdbc:mysql://localhost/Ais";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "";
       
   
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Integer studentID = (Integer) session.getAttribute("userID");
        int examID = Integer.parseInt(request.getParameter("examID"));

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
                String sql = "INSERT INTO StudentRegistrations (StudentID, ExamID) VALUES (?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, studentID);
                    pstmt.setInt(2, examID);
                    pstmt.executeUpdate();
                }
            }
            response.sendRedirect("Main"); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
