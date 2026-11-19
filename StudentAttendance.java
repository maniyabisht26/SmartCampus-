package attendance;

import database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StudentAttendance {

    public static void viewAttendance(int studentId) {

        String sql = "SELECT e.event_name, e.event_date, a.status "
                + "FROM attendance a "
                + "JOIN events e ON a.event_id = e.event_id "
                + "WHERE a.student_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            ResultSet rs = ps.executeQuery();

            System.out.println("\n===== My Attendance =====");

            boolean found = false;

            while (rs.next()) {
                found = true;

                System.out.println("Event: " + rs.getString("event_name"));
                System.out.println("Date: " + rs.getDate("event_date"));
                System.out.println("Status: " + rs.getString("status"));
                System.out.println("-------------------------");
            }

            if (!found) {
                System.out.println("No attendance records found.");
            }

        } catch (Exception e) {
            System.out.println("Unable to view attendance.");
            System.out.println(e.getMessage());
        }
    }
}