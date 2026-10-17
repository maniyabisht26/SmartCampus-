
package ui;

import database.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class RegistrationFrame extends JFrame {

    private JTextField rollField;
    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField departmentField;
    private JTextField yearField;
    private JPasswordField passwordField;

    public RegistrationFrame() {
        setTitle("SmartCampus - Student Registration");
        setSize(500, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 12, 7, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel heading = new JLabel("SMARTCAMPUS");
        heading.setFont(new Font("Arial", Font.BOLD, 26));
        heading.setForeground(new Color(25, 48, 90));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(heading, gbc);

        JLabel subtitle = new JLabel("Create your student account");
        gbc.gridy++;
        panel.add(subtitle, gbc);

        gbc.gridwidth = 1;

        rollField = addField(panel, gbc, "Roll Number:", 2);
        nameField = addField(panel, gbc, "Full Name:", 3);
        emailField = addField(panel, gbc, "Email:", 4);

        passwordField = new JPasswordField(18);
        addRow(panel, gbc, "Password:", passwordField, 5);

        phoneField = addField(panel, gbc, "Phone:", 6);
        departmentField = addField(panel, gbc, "Department:", 7);
        yearField = addField(panel, gbc, "Year of Study:", 8);

        JButton registerButton = new JButton("Create Account");
        registerButton.setBackground(new Color(35, 76, 145));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);

        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        panel.add(registerButton, gbc);

        JButton loginButton = new JButton("Already registered? Login");
        gbc.gridy = 10;
        panel.add(loginButton, gbc);

        registerButton.addActionListener(e -> registerStudent());

        loginButton.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        add(new JScrollPane(panel));
        setVisible(true);
    }

    private JTextField addField(
            JPanel panel, GridBagConstraints gbc,
            String label, int row) {

        JTextField field = new JTextField(18);
        addRow(panel, gbc, label, field, row);
        return field;
    }

    private void addRow(
            JPanel panel, GridBagConstraints gbc,
            String label, JComponent field, int row) {

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void registerStudent() {

        String roll = rollField.getText().trim();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(
                passwordField.getPassword());
        String phone = phoneField.getText().trim();
        String department = departmentField.getText().trim();
        String yearText = yearField.getText().trim();

        if (roll.isEmpty() || name.isEmpty()
                || email.isEmpty() || password.isEmpty()
                || phone.isEmpty() || department.isEmpty()
                || yearText.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this, "Please fill in every field.");
            return;
        }

        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            JOptionPane.showMessageDialog(
                    this, "Please enter a valid email address.");
            return;
        }

        if (!phone.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(
                    this, "Enter a valid 10-digit phone number.");
            return;
        }

        int year;

        try {
            year = Integer.parseInt(yearText);

            if (year < 1 || year > 6) {
                JOptionPane.showMessageDialog(
                        this, "Enter a valid year of study.");
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this, "Year of study must be a number.");
            return;
        }

        String sql = "INSERT INTO students "
                + "(roll_number, full_name, email, "
                + "password_hash, phone, department, year_of_study) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con =
                     DatabaseConnection.getConnection()) {

            if (con == null) {
                JOptionPane.showMessageDialog(
                        this, "Database connection failed.");
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, roll);
                ps.setString(2, name);
                ps.setString(3, email);
                ps.setString(4, password);
                ps.setString(5, phone);
                ps.setString(6, department);
                ps.setInt(7, year);

                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int studentId = keys.getInt(1);

                        JOptionPane.showMessageDialog(
                                this,
                                "Registration successful! Welcome, "
                                        + name + ".");

                        dispose();

                        // Open the student's dashboard.
                        // The student can now view events,
                        // book events and check their bookings.
                        new StudentDashboardFrame(
                                studentId, name);
                    }
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Registration failed. Check whether "
                            + "the roll number or email is "
                            + "already registered.\n"
                            + ex.getMessage());
        }
    }
}
