package attendance;

import database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Scanner;

public class AttendanceManager {

    // GUI-based attendance method
    public static boolean markAttendance(
            int studentId,
            int eventId,
            String status) throws Exception {

        if (studentId <= 0 || eventId <= 0) {
            throw new Exception("Invalid student or event ID.");
        }

        if (!status.equalsIgnoreCase("Present") &&
            !status.equalsIgnoreCase("Absent")) {

            throw new Exception("Status must be Present or Absent.");
        }

        String sql = "INSERT INTO attendance "
                + "(student_id, event_id, status) "
                + "VALUES (?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, eventId);
            ps.setString(3, status);

            return ps.executeUpdate() > 0;
        }
    }

    // Original console-based attendance method
    public static void markAttendance() {

        Scanner sc = new Scanner(System.in);

        System.out.println("\n===== Mark Attendance =====");

        try {

            System.out.print("Enter student ID: ");
            int studentId = Integer.parseInt(sc.nextLine());

            System.out.print("Enter event ID: ");
            int eventId = Integer.parseInt(sc.nextLine());

            System.out.print("Enter attendance status (Present/Absent): ");
            String status = sc.nextLine();

            if (markAttendance(studentId, eventId, status)) {

                System.out.println("\nAttendance marked successfully!");

            }

        } catch (Exception e) {

            System.out.println("\nFailed to mark attendance.");
            System.out.println(e.getMessage());
        }
    }
}
