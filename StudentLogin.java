package student;

import database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class StudentLogin {

    public static void loginStudent() {

        Scanner sc = new Scanner(System.in);

        System.out.println("\n===== Student Login =====");

        System.out.print("Enter email: ");
        String email = sc.nextLine();

        System.out.print("Enter password: ");
        String password = sc.nextLine();

        String sql = "SELECT student_id, full_name, roll_number "
                + "FROM students "
                + "WHERE email = ? AND password_hash = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                System.out.println("\nLogin successful!");

                // Get student details from database
                int studentId = rs.getInt("student_id");
                String studentName = rs.getString("full_name");
                String rollNumber = rs.getString("roll_number");

                System.out.println("Welcome, " + studentName);
                System.out.println("Roll Number: " + rollNumber);

                // Open student dashboard
                StudentDashboard.showDashboard(studentId, studentName);

            } else {

                System.out.println("\nInvalid email or password.");
            }

        } catch (Exception e) {

            System.out.println("\nLogin failed!");
            System.out.println(e.getMessage());
        }
    }
}