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

/**
 * Servlet implementation class CancelRegistration
 */
@WebServlet("/CancelRegistration")
public class CancelRegistration extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static final String JDBC_URL = "jdbc:mysql://localhost/Ais";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CancelRegistration() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int registrationID = Integer.parseInt(request.getParameter("registrationID"));

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
                String sql = "UPDATE StudentRegistrations SET IsCancelled = TRUE WHERE RegistrationID = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, registrationID);
                    pstmt.executeUpdate();
                }
            }
            response.sendRedirect("Main"); // Redirect back to the main page
        } catch (Exception e) {
            e.printStackTrace();
            // Handle error, possibly redirect to an error page
        }
    }

}
