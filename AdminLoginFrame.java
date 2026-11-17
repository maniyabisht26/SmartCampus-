package ui;

import javax.swing.*;
import java.awt.*;

public class AdminLoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public AdminLoginFrame() {

        setTitle("SmartCampus - Admin Login");
        setSize(450, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 20));

        panel.setBorder(
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        );

        panel.add(new JLabel("ADMIN LOGIN"));

        panel.add(new JLabel(""));

        panel.add(new JLabel("Username:"));

        usernameField = new JTextField();
        panel.add(usernameField);

        panel.add(new JLabel("Password:"));

        passwordField = new JPasswordField();
        panel.add(passwordField);

        JButton loginButton = new JButton("Login");

        JButton signupButton = new JButton("Sign Up");

        JButton backButton = new JButton("Back");

        panel.add(loginButton);
        panel.add(signupButton);

        panel.add(backButton);

        loginButton.addActionListener(e -> loginAdmin());

        signupButton.addActionListener(e -> {

            dispose();

            new AdminSignupFrame();
        });

        backButton.addActionListener(e -> {

            dispose();

            new RoleSelectFrame().setVisible(true);
        });

        add(panel);

        setVisible(true);
    }

    private void loginAdmin() {

        String username = usernameField.getText().trim();

        String password =
                new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter username and password."
            );

            return;
        }

        try {

            String adminName =
                    AdminAuthDAO.loginAdmin(username, password);

            if (adminName != null) {

                JOptionPane.showMessageDialog(
                        this,
                        "Welcome, " + adminName + "!"
                );

                dispose();

                new AdminDashboardFrame(adminName);

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Invalid username or password."
                );
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Login Failed: " + e.getMessage()
            );
        }
    }
}