package ukf;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/EditTeacher")
public class EditTeacher extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String JDBC_URL = "jdbc:mysql://localhost/Ais";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "";

    public EditTeacher() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String role = (String) session.getAttribute("role");

        if (role != null && role.equals("admin")) {
            String teacherName = request.getParameter("teacherName");
            if (teacherName != null && !teacherName.isEmpty()) {
                displayEditTeacherForm(request, response, teacherName);
            } else {
                response.sendRedirect("Main");
            }
        } else {
            response.sendRedirect("Login");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String role = (String) session.getAttribute("role");

        if (role != null && role.equals("admin")) {
            String teacherName = request.getParameter("teacherName");
            if (teacherName != null && !teacherName.isEmpty()) {
                if (request.getParameter("updateSubjects") != null) { 
                    String[] assignedSubjects = request.getParameterValues("assignedSubjects[]");
                    if (assignedSubjects != null) {
                        updateTeacherSubjects(request, response, teacherName, assignedSubjects);
                    } else {
                        response.sendRedirect("EditTeacher?teacherName=" + teacherName);
                    }
                } else {
                    updateTeacherData(request, response, teacherName);
                }
            } else {
                response.sendRedirect("Main");
            }
        } else {
            response.sendRedirect("Login"); 
        }
    }


    private void displayEditTeacherForm(HttpServletRequest request, HttpServletResponse response, String teacherName)
            throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);

            String teacherInfoQuery = "SELECT * FROM Users WHERE UserName = ?";
            pstmt = conn.prepareStatement(teacherInfoQuery);
            pstmt.setString(1, teacherName);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                String currentTeacherName = rs.getString("UserName");
                String currentTeacherEmail = rs.getString("Email");

                out.println("<a href='Main'>Go to Main Page</a>");
                out.println("<h1>Edit Teacher: " + currentTeacherName + "</h1>");
                out.println("<form method='post' action='EditTeacher'>");
                out.println("Name: <input type='text' name='newTeacherName' value='" + currentTeacherName + "'><br>");
                out.println("Email: <input type='text' name='newTeacherEmail' value='" + currentTeacherEmail + "'><br>");
                out.println("<input type='hidden' name='teacherName' value='" + currentTeacherName + "'>");
                out.println("<input type='submit' value='Update'>");
                out.println("</form>");

                String availableSubjectsQuery = "SELECT * FROM Subjects";
                pstmt = conn.prepareStatement(availableSubjectsQuery);
                rs = pstmt.executeQuery();

                List<String> assignedSubjects = new ArrayList<>();
                List<String> availableSubjectOptions = new ArrayList<>();

                String assignedSubjectsQuery = "SELECT SubjectName FROM Subjects s " +
                        "INNER JOIN Teachers t ON s.SubjectID = t.SubjectID " +
                        "INNER JOIN Users u ON t.TeacherID = u.UserID " +
                        "WHERE u.UserName = ?";
                pstmt = conn.prepareStatement(assignedSubjectsQuery);
                pstmt.setString(1, currentTeacherName);
                ResultSet assignedSubjectsResultSet = pstmt.executeQuery();
                while (assignedSubjectsResultSet.next()) {
                    assignedSubjects.add(assignedSubjectsResultSet.getString("SubjectName"));
                }

                while (rs.next()) {
                    String subjectName = rs.getString("SubjectName");
                    if (assignedSubjects.contains(subjectName)) {
                        availableSubjectOptions.add("<option value='" + subjectName + "' selected>" + subjectName + "</option>");
                    } else {
                        availableSubjectOptions.add("<option value='" + subjectName + "'>" + subjectName + "</option>");
                    }
                }

                out.println("<h2>Assign Subjects:</h2>");
                out.println("<form method='post' action='EditTeacher'>");
                out.println("<input type='hidden' name='teacherName' value='" + currentTeacherName + "'>");
                out.println("<select multiple name='assignedSubjects[]' size='5'>");
                for (String option : availableSubjectOptions) {
                    out.println(option);
                }
                out.println("</select><br>");
                out.println("<input type='submit' name='updateSubjects' value='Assign Subjects'>"); 
                out.println("</form>");

            } else {
                out.println("<p>Teacher not found.</p>");
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

    private void updateTeacherData(HttpServletRequest request, HttpServletResponse response, String teacherName)
            throws ServletException, IOException {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);

            String newTeacherName = request.getParameter("newTeacherName");
            String newTeacherEmail = request.getParameter("newTeacherEmail");

            String updateTeacherQuery = "UPDATE Users SET UserName = ?, Email = ? WHERE UserName = ?";
            pstmt = conn.prepareStatement(updateTeacherQuery);
            pstmt.setString(1, newTeacherName);
            pstmt.setString(2, newTeacherEmail);
            pstmt.setString(3, teacherName);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                String[] assignedSubjects = request.getParameterValues("assignedSubjects[]");
                if (assignedSubjects != null) {
                    updateTeacherSubjects(request, response, newTeacherName, assignedSubjects);
                } else {
                    response.sendRedirect("EditTeacher?teacherName=" + newTeacherName);
                }
            } else {
                response.sendRedirect("Main");
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
    private void updateTeacherSubjects(HttpServletRequest request, HttpServletResponse response, String teacherName, String[] assignedSubjects)
            throws ServletException, IOException {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);

            // 1. Видаліть всі записи про вчителя з таблиці Teachers, які відповідають даному вчителю
            String deleteAssignmentsQuery = "DELETE FROM Teachers WHERE TeacherID = (SELECT UserID FROM Users WHERE UserName = ?)";
            pstmt = conn.prepareStatement(deleteAssignmentsQuery);
            pstmt.setString(1, teacherName);
            pstmt.executeUpdate();

            // 2. Вставте нові записи для обраних предметів
            String assignSubjectsQuery = "INSERT INTO Teachers (TeacherID, SubjectID) VALUES ((SELECT UserID FROM Users WHERE UserName = ?), (SELECT SubjectID FROM Subjects WHERE SubjectName = ?))";
            pstmt = conn.prepareStatement(assignSubjectsQuery);
            pstmt.setString(1, teacherName);
            for (String subject : assignedSubjects) {
                pstmt.setString(2, subject);
                pstmt.executeUpdate();
            }

            response.sendRedirect("EditTeacher?teacherName=" + teacherName);
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
