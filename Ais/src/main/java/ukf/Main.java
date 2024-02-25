package ukf;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;



@WebServlet("/Main")
public class Main extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static final String JDBC_URL = "jdbc:mysql://localhost/Ais";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "";

    public Main() {
        super();
    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String role = (String) session.getAttribute("role");
        int teacherID = (Integer) session.getAttribute("userID");
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        String action = request.getParameter("action");

        if ("logout".equals(action)) {
            session.invalidate();
            response.sendRedirect("Login");
        } else {
        if (role != null && role.equals("admin")) {
            String deleteTeacherName = request.getParameter("deleteTeacher");
            if (deleteTeacherName != null && !deleteTeacherName.isEmpty()) {
                deleteTeacher(request, response, deleteTeacherName);
            } else {
                displayAdminDashboard(request, response);
            }
        }
        else if (role != null && role.equals("teacher")) {
            List<String> teacherSubjects = getTeacherSubjects(teacherID);

            out.println("<h1>Create Exam:</h1>");
            out.println("<form action='CreateExam' method='post'>");
            out.println("<label for='subjectSelect'>Select Subject:</label>");
            out.println("<select name='subjectID' id='subjectSelect' required>");
            
            for (String subject : teacherSubjects) {
                out.println("<option value='" + subject + "'>" + subject + "</option>");
            }
            
            out.println("</select><br>");
            out.println("<label for='examDate'>Exam Date:</label>");
            out.println("<input type='date' name='examDate' id='examDate' required><br>");
            out.println("<input type='submit' value='Create Exam'>");
            out.println("</form>");

            displayTeacherExams(request, response, teacherID);
            displayStudentGradingForm(request, response, teacherID);

        }
        else if (role.equals("student")) {
            displayStudentForm(request, response);
            displayRegisteredExams(request, response, teacherID);

        }
        out.println("<a href='Main?action=logout'>Logout</a>");

        }


    }


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	private void displayAdminDashboard(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
            
            String deleteSubjectID = request.getParameter("deleteSubjectID");
            if (deleteSubjectID != null && !deleteSubjectID.isEmpty()) {
                int subjectIDToDelete = Integer.parseInt(deleteSubjectID);
                String deleteSubjectQuery = "DELETE FROM Subjects WHERE SubjectID = ?";
                pstmt = conn.prepareStatement(deleteSubjectQuery);
                pstmt.setInt(1, subjectIDToDelete);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    out.println("<p>Subject with ID " + subjectIDToDelete + " has been deleted successfully.</p>");
                } else {
                    out.println("<p>Failed to delete subject with ID " + subjectIDToDelete + ".</p>");
                }
            }
            String deleteStudentID = request.getParameter("deleteStudentID");
            if (deleteStudentID != null && !deleteStudentID.isEmpty()) {
                int studentIDToDelete = Integer.parseInt(deleteStudentID);
                
                String deleteUserRolesQuery = "DELETE FROM UserRoles WHERE UserID = ?";
                pstmt = conn.prepareStatement(deleteUserRolesQuery);
                pstmt.setInt(1, studentIDToDelete);
                pstmt.executeUpdate();

                String deleteStudentQuery = "DELETE FROM Users WHERE UserID = ?";
                pstmt = conn.prepareStatement(deleteStudentQuery);
                pstmt.setInt(1, studentIDToDelete);
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    out.println("<p>Student with ID " + studentIDToDelete + " has been deleted successfully.</p>");
                } else {
                    out.println("<p>Failed to delete student with ID " + studentIDToDelete + ".</p>");
                }
            }

            String teachersAndSubjectsQuery = "SELECT u.UserName AS TeacherName, GROUP_CONCAT(s.SubjectName SEPARATOR ', ') AS Subjects FROM Users u " +
                    "LEFT JOIN Teachers t ON u.UserID = t.TeacherID " +
                    "LEFT JOIN Subjects s ON t.SubjectID = s.SubjectID " +
                    "INNER JOIN UserRoles ur ON u.UserID = ur.UserID " +
                    "INNER JOIN Roles r ON ur.RoleID = r.RoleID " +
                    "WHERE r.RoleName = 'teacher' " +
                    "GROUP BY TeacherName";

            pstmt = conn.prepareStatement(teachersAndSubjectsQuery);
            rs = pstmt.executeQuery();

            out.println("<h1>List of teachers and their subjects:</h1>");
            out.println("<ul>");
            while (rs.next()) {
                String teacherName = rs.getString("TeacherName");
                String subjects = rs.getString("Subjects");

                if (subjects != null) {
                    out.println("<li>" + teacherName + " - " + subjects +
                            " <a href='EditTeacher?teacherName=" + teacherName + "'>Edit</a>" +
                            " <a href='?deleteTeacher=" + teacherName + "'>Delete</a></li>");
                } else {
                    out.println("<li>" + teacherName + " - No subjects assigned<a href='EditTeacher?teacherName=" + teacherName + "'>Edit</a>" +
                            " <a href='?deleteTeacher=" + teacherName + "'>Delete</a></li>");
                }
            }
            out.println("</ul>");


            String studentQuery = "SELECT u.UserID, u.UserName FROM Users u " +
                "INNER JOIN UserRoles ur ON u.UserID = ur.UserID " +
                "INNER JOIN Roles r ON ur.RoleID = r.RoleID " +
                "WHERE r.RoleName = 'student'";
            pstmt = conn.prepareStatement(studentQuery);
            rs = pstmt.executeQuery();

            out.println("<h1>List of students:</h1>");
            out.println("<ul>");
            while (rs.next()) {
                int studentID = rs.getInt("UserID");
                String studentName = rs.getString("UserName");
                out.println("<li>" + studentName +
                        " <a href='EditStudent?studentID=" + studentID + "'>Edit</a>" +
                        " <a href='?deleteStudentID=" + studentID + "'>Delete</a></li>");
            }
            out.println("</ul>");


            String subjectQuery = "SELECT SubjectID, SubjectName FROM Subjects";
            pstmt = conn.prepareStatement(subjectQuery);
            rs = pstmt.executeQuery();

            out.println("<h1>List of subjects:</h1>");
            out.println("<ul>");
            while (rs.next()) {
                int subjectID = rs.getInt("SubjectID");
                String subjectName = rs.getString("SubjectName");
                out.println("<li>" + subjectName +
                        " <a href='EditSubject?subjectID=" + subjectID + "'>Edit</a>" +
                        " <a href='?deleteSubjectID=" + subjectID + "'>Delete</a></li>");
            }
            out.println("</ul>");
            
            

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
    }
	private void deleteTeacher(HttpServletRequest request, HttpServletResponse response, String teacherName) throws ServletException, IOException {
	    Connection conn = null;
	    PreparedStatement pstmt = null;

	    try {
	        Class.forName("com.mysql.cj.jdbc.Driver");
	        conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);

	        String findTeacherIDQuery = "SELECT UserID FROM Users WHERE UserName = ?";
	        pstmt = conn.prepareStatement(findTeacherIDQuery);
	        pstmt.setString(1, teacherName);
	        ResultSet rs = pstmt.executeQuery();
	        int teacherID = 0;
	        if (rs.next()) {
	            teacherID = rs.getInt("UserID");
	        }

	        if (teacherID != 0) {
	            String deleteUserRolesQuery = "DELETE FROM UserRoles WHERE UserID = ?";
	            pstmt = conn.prepareStatement(deleteUserRolesQuery);
	            pstmt.setInt(1, teacherID);
	            pstmt.executeUpdate();

	            String deleteTeacherSubjectsQuery = "DELETE FROM Teachers WHERE TeacherID = ?";
	            pstmt = conn.prepareStatement(deleteTeacherSubjectsQuery);
	            pstmt.setInt(1, teacherID);
	            pstmt.executeUpdate();

	            String deleteTeacherQuery = "DELETE FROM Users WHERE UserID = ?";
	            pstmt = conn.prepareStatement(deleteTeacherQuery);
	            pstmt.setInt(1, teacherID);
	            int rowsAffected = pstmt.executeUpdate();

	            if (rowsAffected > 0) {
	                response.sendRedirect("Main");
	            } else {
	                response.sendRedirect("Main?error=deleteFailed");
	            }
	        } else {
	            response.sendRedirect("Main?error=teacherNotFound");
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.sendRedirect("Main?error=deleteFailed");
	    } finally {
	        try {
	            if (pstmt != null) pstmt.close();
	        } catch (Exception e) {}
	        try {
	            if (conn != null) conn.close();
	        } catch (Exception e) {}
	    }
	}
	private List<String> getTeacherSubjects(int teacherID) {
	    List<String> subjects = new ArrayList<>();
	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;

	    try {
	        Class.forName("com.mysql.cj.jdbc.Driver");
	        conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);

	        String subjectsQuery = "SELECT s.SubjectName FROM Subjects s " +
	                               "INNER JOIN Teachers t ON s.SubjectID = t.SubjectID " +
	                               "WHERE t.TeacherID = ?";
	        pstmt = conn.prepareStatement(subjectsQuery);
	        pstmt.setInt(1, teacherID);
	        rs = pstmt.executeQuery();

	        while (rs.next()) {
	            subjects.add(rs.getString("SubjectName"));
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        try { if (rs != null) rs.close(); } catch (Exception e) {}
	        try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
	        try { if (conn != null) conn.close(); } catch (Exception e) {}
	    }

	    return subjects;
	}
	private void displayTeacherExams(HttpServletRequest request, HttpServletResponse response, int teacherID) throws ServletException, IOException {
	    response.setContentType("text/html");
	    PrintWriter out = response.getWriter();

	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;

	    try {
	        Class.forName("com.mysql.cj.jdbc.Driver");
	        conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);

	        String teacherExamsQuery = "SELECT e.ExamID, s.SubjectName, e.ExamDate FROM Exams e " +
	                                   "INNER JOIN Subjects s ON e.SubjectID = s.SubjectID " +
	                                   "WHERE e.TeacherID = ?";
	        pstmt = conn.prepareStatement(teacherExamsQuery);
	        pstmt.setInt(1, teacherID);
	        rs = pstmt.executeQuery();

	        out.println("<h1>Your Exams:</h1>");
	        out.println("<table border='1'>");
	        out.println("<tr><th>Exam ID</th><th>Subject</th><th>Exam Date</th></tr>");
	        
	        while (rs.next()) {
	            int examID = rs.getInt("ExamID");
	            String subjectName = rs.getString("SubjectName");
	            java.sql.Date examDate = rs.getDate("ExamDate");

	            out.println("<tr>");
	            out.println("<td>" + examID + "</td>");
	            out.println("<td>" + subjectName + "</td>");
	            out.println("<td>" + examDate + "</td>");
	            out.println("</tr>");
	        }
	        
	        out.println("</table>");
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        try { if (rs != null) rs.close(); } catch (Exception e) {}
	        try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
	        try { if (conn != null) conn.close(); } catch (Exception e) {}
	    }
	}
	private void displayStudentForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    response.setContentType("text/html");
	    PrintWriter out = response.getWriter();
	    HttpSession session = request.getSession();
        int studentID = (Integer) session.getAttribute("userID");
	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;

	    try {
	        Class.forName("com.mysql.cj.jdbc.Driver");
	        conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);

	        String query = "SELECT s.SubjectID, s.SubjectName, t.TeacherID, u.UserName AS TeacherName FROM Subjects s " +
	                       "JOIN Teachers t ON s.SubjectID = t.SubjectID " +
	                       "JOIN Users u ON t.TeacherID = u.UserID";

	        pstmt = conn.prepareStatement(query);
	        rs = pstmt.executeQuery();

	        out.println("<h1>Select Subject and Teacher:</h1>");
	        out.println("<form action='StudentAction' method='post'>"); 
	        out.println("<label for='subjectSelect'>Select Subject:</label>");
	        out.println("<select name='subjectID' id='subjectSelect' required>");
	        
	        while (rs.next()) {
	            int subjectID = rs.getInt("SubjectID");
	            String subjectName = rs.getString("SubjectName");
	            int teacherID = rs.getInt("TeacherID");
	            String teacherName = rs.getString("TeacherName");

	            out.println("<option value='" + subjectID + "_" + teacherID + "'>" + subjectName + " - " + teacherName + "</option>");
	        }
	        
	        out.println("</select><br>");
	        out.println("<input type='submit' value='Submit'>");
	        out.println("</form>");
	        
	        String examQuery = "SELECT e.ExamID, e.SubjectID, s.SubjectName, e.ExamDate FROM Exams e " +
                    "JOIN Subjects s ON e.SubjectID = s.SubjectID " +
                    "JOIN StudentSubjects ss ON ss.SubjectID = e.SubjectID " +
                    "WHERE ss.StudentID = ?";

		 pstmt = conn.prepareStatement(examQuery);
		 pstmt.setInt(1, studentID);
		 ResultSet examRs = pstmt.executeQuery();
		
		 out.println("<h1>Register for Exam:</h1>");
		 out.println("<form action='RegisterForExam' method='post'>");
		 out.println("<label for='examSelect'>Select Exam:</label>");
		 out.println("<select name='examID' id='examSelect' required>");
		 
		 while (examRs.next()) {
		     int examID = examRs.getInt("ExamID");
		     String subjectName = examRs.getString("SubjectName");
		     Date examDate = examRs.getDate("ExamDate");
		     out.println("<option value='" + examID + "'>" + subjectName + " - " + examDate + "</option>");
		 }
		 
		 out.println("</select><br>");
		 out.println("<input type='submit' value='Register'>");
		 out.println("</form>");
		 
		 
		 String gradesQuery = "SELECT g.GradeValue, s.SubjectName FROM Grades g " +
                 "JOIN Exams e ON g.ExamID = e.ExamID " +
                 "JOIN Subjects s ON e.SubjectID = s.SubjectID " +
                 "WHERE g.StudentID = ?";
		pstmt = conn.prepareStatement(gradesQuery);
		pstmt.setInt(1, studentID);
		rs = pstmt.executeQuery();
		
		List<BigDecimal> grades = new ArrayList<>();
		out.println("<h1>Your Grades:</h1>");
		out.println("<table border='1'>");
		out.println("<tr><th>Subject</th><th>Grade</th></tr>");
		
		while (rs.next()) {
		BigDecimal gradeValue = rs.getBigDecimal("GradeValue");
		String subjectName = rs.getString("SubjectName");
		grades.add(gradeValue);
		
		out.println("<tr>");
		out.println("<td>" + subjectName + "</td>");
		out.println("<td>" + gradeValue + "</td>");
		out.println("</tr>");
		}
		out.println("</table>");
		
		BigDecimal average = grades.stream()
		                      .reduce(BigDecimal.ZERO, BigDecimal::add)
		                      .divide(new BigDecimal(grades.size()), 2, RoundingMode.HALF_UP);
		out.println("<p>Average Grade: " + average + "</p>");

		 
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        try { if (rs != null) rs.close(); } catch (Exception e) {}
	        try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
	        try { if (conn != null) conn.close(); } catch (Exception e) {}
	    }
	}
	private void displayRegisteredExams(HttpServletRequest request, HttpServletResponse response, int studentID) throws ServletException, IOException {
		response.setContentType("text/html");
	    PrintWriter out = response.getWriter();
	    HttpSession session = request.getSession();
	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;

	    try {
	        Class.forName("com.mysql.cj.jdbc.Driver");
	        conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);

	    String registeredExamsQuery = "SELECT sr.RegistrationID, e.ExamID, s.SubjectName, e.ExamDate " +
	                                  "FROM StudentRegistrations sr " +
	                                  "JOIN Exams e ON sr.ExamID = e.ExamID " +
	                                  "JOIN Subjects s ON e.SubjectID = s.SubjectID " +
	                                  "WHERE sr.StudentID = ? AND sr.IsCancelled = FALSE";
	    pstmt = conn.prepareStatement(registeredExamsQuery);
	        pstmt.setInt(1, studentID);
	         rs = pstmt.executeQuery();

	         out = response.getWriter();
	        out.println("<h1>Your Registered Exams:</h1>");
	        out.println("<table border='1'>");
	        out.println("<tr><th>Subject</th><th>Exam Date</th><th>Action</th></tr>");

	        while (rs.next()) {
	            int registrationID = rs.getInt("RegistrationID");
	            String subjectName = rs.getString("SubjectName");
	            Date examDate = rs.getDate("ExamDate");
	            out.println("<tr>");
	            out.println("<td>" + subjectName + "</td>");
	            out.println("<td>" + examDate + "</td>");
	            out.println("<td><form action='CancelRegistration' method='post'>");
	            out.println("<input type='hidden' name='registrationID' value='" + registrationID + "'/>");
	            out.println("<input type='submit' value='Cancel'/>");
	            out.println("</form></td>");
	            out.println("</tr>");
	        }
	        out.println("</table>");
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        try { if (rs != null) rs.close(); } catch (Exception e) {}
	        try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
	        try { if (conn != null) conn.close(); } catch (Exception e) {}
	    }
	}
	private void displayStudentGradingForm(HttpServletRequest request, HttpServletResponse response, int teacherID) throws ServletException, IOException {
	    response.setContentType("text/html");
	    PrintWriter out = response.getWriter();

	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;

	    try {
	        Class.forName("com.mysql.cj.jdbc.Driver");
	        conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);

	        String studentQuery = "SELECT sr.RegistrationID, u.UserID, u.UserName, s.SubjectName, sr.ExamID, g.GradeValue " +
	                              "FROM StudentRegistrations sr " +
	                              "JOIN Users u ON sr.StudentID = u.UserID " +
	                              "JOIN Exams e ON sr.ExamID = e.ExamID " +
	                              "JOIN Subjects s ON e.SubjectID = s.SubjectID " +
	                              "LEFT JOIN Grades g ON sr.StudentID = g.StudentID AND sr.ExamID = g.ExamID " +
	                              "WHERE e.TeacherID = ? AND sr.IsCancelled = FALSE";

	        pstmt = conn.prepareStatement(studentQuery);
	        pstmt.setInt(1, teacherID);
	        rs = pstmt.executeQuery();

	        out.println("<h1>Assign Grades:</h1>");
	        out.println("<form action='AssignGrade' method='post'>");
	        out.println("<table border='1'>");
	        out.println("<tr><th>Student</th><th>Subject</th><th>Grade</th></tr>");

	        while (rs.next()) {
	            int registrationID = rs.getInt("RegistrationID");
	            String studentName = rs.getString("UserName");
	            String subjectName = rs.getString("SubjectName");
	            String gradeValue = rs.getString("GradeValue");

	            out.println("<tr>");
	            out.println("<td>" + studentName + "</td>");
	            out.println("<td>" + subjectName + "</td>");
	            out.println("<td><input type='text' name='grade_" + registrationID + "' size='5' value='" + (gradeValue != null ? gradeValue : "") + "'></td>");
	            out.println("</tr>");
	        }
	        out.println("</table>");
	        out.println("<input type='submit' value='Submit Grades'>");
	        out.println("</form>");
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        try { if (rs != null) rs.close(); } catch (Exception e) {}
	        try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
	        try { if (conn != null) conn.close(); } catch (Exception e) {}
	    }
	}




}
