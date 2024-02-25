package ukf;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

@WebServlet("/StudentAction")
public class StudentAction extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String JDBC_URL = "jdbc:mysql://localhost/Ais";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "";

    public StudentAction() {
        super();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Integer studentID = (Integer) session.getAttribute("userID");
        String selectedValue = request.getParameter("subjectID");

        if (studentID != null && selectedValue != null && !selectedValue.isEmpty()) {
            String[] parts = selectedValue.split("_");
            int subjectID = Integer.parseInt(parts[0]);
            int teacherID = Integer.parseInt(parts[1]);

            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
                    String sql = "INSERT INTO StudentSubjects (StudentID, SubjectID, TeacherID) VALUES (?, ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setInt(1, studentID);
                        pstmt.setInt(2, subjectID);
                        pstmt.setInt(3, teacherID);
                        pstmt.executeUpdate();
                    }
                }
                response.sendRedirect("Main"); 
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
        }
    }
}
