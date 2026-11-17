package ui;

import javax.swing.*;
import java.awt.*;

public class AdminSignupFrame extends JFrame {

    private JTextField nameField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    public AdminSignupFrame() {

        setTitle("SmartCampus - Admin Sign Up");
        setSize(450, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 15));

        panel.setBorder(
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        );

        panel.add(new JLabel("ADMIN REGISTRATION"));

        panel.add(new JLabel(""));

        panel.add(new JLabel("Full Name:"));
        nameField = new JTextField();
        panel.add(nameField);

        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        panel.add(new JLabel("Confirm Password:"));
        confirmPasswordField = new JPasswordField();
        panel.add(confirmPasswordField);

        JButton signupButton = new JButton("Create Account");
        JButton backButton = new JButton("Back to Login");

        panel.add(backButton);
        panel.add(signupButton);

        signupButton.addActionListener(e -> registerAdmin());

        backButton.addActionListener(e -> {

            dispose();

            new AdminLoginFrame().setVisible(true);
        });

        add(panel);

        setVisible(true);
    }

    private void registerAdmin() {

        String name = nameField.getText().trim();
        String username = usernameField.getText().trim();

        String password =
                new String(passwordField.getPassword());

        String confirmPassword =
                new String(confirmPasswordField.getPassword());

        if (name.isEmpty() || username.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please fill all fields."
            );

            return;
        }

        if (password.length() < 6) {

            JOptionPane.showMessageDialog(
                    this,
                    "Password must contain at least 6 characters."
            );

            return;
        }

        if (!password.equals(confirmPassword)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Passwords do not match."
            );

            return;
        }

        try {

            boolean success = AdminAuthDAO.registerAdmin(
                    name,
                    username,
                    password
            );

            if (success) {

                JOptionPane.showMessageDialog(
                        this,
                        "Admin Account Created Successfully!"
                );

                dispose();

                new AdminLoginFrame().setVisible(true);

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Username already exists."
                );
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Registration Failed: " + e.getMessage()
            );
        }
    }
}