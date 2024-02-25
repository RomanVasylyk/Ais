package ukf;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/AssignGrade")
public class AssignGrade extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static final String JDBC_URL = "jdbc:mysql://localhost/Ais";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
                Enumeration<String> parameterNames = request.getParameterNames();

                while (parameterNames.hasMoreElements()) {
                    String paramName = parameterNames.nextElement();
                    if (paramName.startsWith("grade_")) {
                        int registrationID = Integer.parseInt(paramName.substring(6));
                        String gradeValue = request.getParameter(paramName);

                        
                        int studentID = 0;
                        int examID = 0;
                        String fetchIdsSql = "SELECT StudentID, ExamID FROM StudentRegistrations WHERE RegistrationID = ?";
                        try (PreparedStatement pstmtFetch = conn.prepareStatement(fetchIdsSql)) {
                            pstmtFetch.setInt(1, registrationID);
                            ResultSet rs = pstmtFetch.executeQuery();
                            if (rs.next()) {
                                studentID = rs.getInt("StudentID");
                                examID = rs.getInt("ExamID");
                            }
                        }

                        if (studentID != 0 && examID != 0) {
                            String sql = "INSERT INTO Grades (StudentID, ExamID, GradeValue) VALUES (?, ?, ?)";
                            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                                pstmt.setInt(1, studentID);
                                pstmt.setInt(2, examID);
                                pstmt.setBigDecimal(3, new BigDecimal(gradeValue));
                                pstmt.executeUpdate();
                            }
                        }
                    }
                }
            }
            response.sendRedirect("Main"); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	
}
