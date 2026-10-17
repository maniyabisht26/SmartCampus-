package student;

import attendance.StudentAttendance;

import java.util.Scanner;

public class StudentDashboard {

    public static void showDashboard(int studentId, String studentName) {

        Scanner sc = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\n================================");
            System.out.println("       STUDENT DASHBOARD");
            System.out.println("================================");
            System.out.println("Welcome, " + studentName);

            System.out.println("1. View Events");
            System.out.println("2. Book an Event");
            System.out.println("3. View My Bookings");
            System.out.println("4. Check Attendance");
            System.out.println("5. Check Certificate Eligibility");
            System.out.println("6. Logout");

            System.out.print("Enter your choice: ");
            choice = sc.nextInt();

            switch (choice) {

                case 1:
                    System.out.println("Event viewing module is coming soon.");
                    break;

                case 2:
                    System.out.println("Event booking module is coming soon.");
                    break;

                case 3:
                    System.out.println("My Bookings module is coming soon.");
                    break;

                case 4:
                    StudentAttendance.viewAttendance(studentId);
                    break;

                case 5:
                    System.out.println("Certificate eligibility module is coming soon.");
                    break;

                case 6:
                    System.out.println("Logged out successfully!");
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }

        } while (choice != 6);
    }
}