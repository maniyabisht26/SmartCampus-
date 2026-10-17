
package ui;

import javax.swing.*;
import java.awt.*;

public class StudentDashboardFrame extends JFrame {

    private final int studentId;

    public StudentDashboardFrame(int studentId, String studentName) {
        this.studentId = studentId;

        setTitle("SmartCampus - Student Dashboard");
        setSize(600, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
        panel.setBorder(
                BorderFactory.createEmptyBorder(25, 50, 25, 50));
        panel.setBackground(Color.WHITE);

        JLabel heading = new JLabel(
                "Welcome, " + studentName, SwingConstants.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 24));
        heading.setForeground(new Color(25, 48, 90));

        JButton viewEventsButton = new JButton("View Events");
        JButton bookEventButton = new JButton("Book an Event");
        JButton myBookingsButton = new JButton("My Bookings");
        JButton attendanceButton = new JButton("Check Attendance");
        JButton certificateButton =
                new JButton("Certificate Eligibility");
        JButton logoutButton = new JButton("Logout");

        panel.add(heading);
        panel.add(viewEventsButton);
        panel.add(bookEventButton);
        panel.add(myBookingsButton);
        panel.add(attendanceButton);
        panel.add(certificateButton);
        panel.add(logoutButton);

        viewEventsButton.addActionListener(
                e -> new EventsFrame(studentId));

        bookEventButton.addActionListener(
                e -> new EventsFrame(studentId));

        myBookingsButton.addActionListener(
                e -> new MyBookingsFrame(studentId));

        attendanceButton.addActionListener(e ->
                JOptionPane.showMessageDialog(
                        this,
                        "Attendance screen will be connected next."));

        certificateButton.addActionListener(e ->
                JOptionPane.showMessageDialog(
                        this,
                        "Certificate eligibility will be connected next."));

        logoutButton.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        add(panel);
        setVisible(true);
    }
}
