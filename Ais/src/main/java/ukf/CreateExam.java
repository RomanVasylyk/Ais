package ukf;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

@WebServlet("/CreateExam")
public class CreateExam extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String JDBC_URL = "jdbc:mysql://localhost/Ais";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "";

    public CreateExam() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String role = (String) session.getAttribute("role");
        int teacherID = (Integer) session.getAttribute("userID");
        
        if (role != null && role.equals("teacher")) {
            String subjectID = request.getParameter("subjectID");
            String examDate = request.getParameter("examDate");
            
            boolean isValidData = validateData(subjectID, examDate);

            if (isValidData) {
                boolean isExamSaved = saveExam(subjectID, examDate, teacherID);

                if (isExamSaved) {
                    response.sendRedirect("Main");
                } else {
                    PrintWriter out = response.getWriter();
                    out.println("<p>Failed to save the exam. Please try again.</p>");
                }
            } else {
                PrintWriter out = response.getWriter();
                out.println("<p>Invalid data. Please check your input.</p>");
            }
        }
    }

    private boolean validateData(String subjectID, String examDate) {
        return true; 
    }

    private boolean saveExam(String subjectID, String examDate, int teacherID) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);

            String selectSubjectIDQuery = "SELECT SubjectID FROM Subjects WHERE SubjectName = ?";
            pstmt = conn.prepareStatement(selectSubjectIDQuery);
            pstmt.setString(1, subjectID);
            resultSet = pstmt.executeQuery();

            int actualSubjectID = 0;
            if (resultSet.next()) {
                actualSubjectID = resultSet.getInt("SubjectID");
            }

            if (actualSubjectID != 0) {
                String insertExamQuery = "INSERT INTO Exams (SubjectID, TeacherID, ExamDate) VALUES (?, ?, ?)";
                pstmt = conn.prepareStatement(insertExamQuery);
                pstmt.setInt(1, actualSubjectID);
                pstmt.setInt(2, teacherID);
                pstmt.setDate(3, new java.sql.Date(new Date().getTime()));

                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            } else {
                return false;
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (resultSet != null)
                    resultSet.close();
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
}
