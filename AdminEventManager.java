package event;

import database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Scanner;

public class AdminEventManager {

    public static void addEvent() {

        Scanner sc = new Scanner(System.in);

        System.out.println("\n===== Add New Event =====");

        System.out.print("Enter event name: ");
        String eventName = sc.nextLine();

        System.out.print("Enter description: ");
        String description = sc.nextLine();

        System.out.print("Enter event date (YYYY-MM-DD): ");
        String eventDate = sc.nextLine();

        System.out.print("Enter event time (HH:MM:SS): ");
        String eventTime = sc.nextLine();

        System.out.print("Enter venue: ");
        String venue = sc.nextLine();

        System.out.print("Enter total seats: ");
        int totalSeats = sc.nextInt();

        String sql = "INSERT INTO events "
                + "(event_name, description, event_date, event_time, venue, total_seats, available_seats) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, eventName);
            ps.setString(2, description);
            ps.setString(3, eventDate);
            ps.setString(4, eventTime);
            ps.setString(5, venue);
            ps.setInt(6, totalSeats);
            ps.setInt(7, totalSeats);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("\nEvent added successfully!");
            }

        } catch (Exception e) {
            System.out.println("\nFailed to add event.");
            System.out.println(e.getMessage());
        }
    }
}