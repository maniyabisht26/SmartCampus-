
package ui;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import database.DatabaseConnection;

public class LoginFrame extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;

    public LoginFrame() {

        setTitle("SmartCampus - Student Login");
        setSize(450, 380);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(Color.WHITE);

        JLabel heading = new JLabel("SMARTCAMPUS");
        heading.setFont(new Font("Arial", Font.BOLD, 25));
        heading.setBounds(105, 25, 250, 35);
        panel.add(heading);

        JLabel subtitle = new JLabel("Student Login");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitle.setBounds(165, 65, 150, 25);
        panel.add(subtitle);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(55, 115, 100, 25);
        panel.add(emailLabel);

        emailField = new JTextField();
        emailField.setBounds(145, 115, 230, 30);
        panel.add(emailField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(55, 165, 100, 25);
        panel.add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(145, 165, 230, 30);
        panel.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(145, 220, 230, 35);
        panel.add(loginButton);

        loginButton.addActionListener(e -> loginStudent());

        JLabel footer = new JLabel("College Event Management System");
        footer.setFont(new Font("Arial", Font.ITALIC, 12));
        footer.setBounds(105, 285, 260, 25);
        panel.add(footer);

        add(panel);
        setVisible(true);
        JButton registerButton = new JButton("Create New Account");

registerButton.setBounds(145, 265, 230, 35);
panel.add(registerButton);

registerButton.addActionListener(e -> {
    dispose();
    new RegistrationFrame();
});
    }

    private void loginStudent() {

        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this, "Please enter email and password.");
            return;
        }

        String sql = "SELECT student_id, full_name, roll_number "
                + "FROM students "
                + "WHERE email = ? AND password_hash = ?";

        try (Connection con = DatabaseConnection.getConnection()) {

            if (con == null) {
                JOptionPane.showMessageDialog(
                        this, "Could not connect to the database.");
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, email);
                ps.setString(2, password);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {

                        int studentId = rs.getInt("student_id");
                        String studentName = rs.getString("full_name");

                        JOptionPane.showMessageDialog(
                                this, "Welcome, " + studentName + "!");

                        dispose();

                        // Dashboard window will be connected here.
                        new StudentDashboardFrame(
                                studentId, studentName);

                    } else {
                        JOptionPane.showMessageDialog(
                                this, "Invalid email or password.");
                    }
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this, "Login failed: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}
