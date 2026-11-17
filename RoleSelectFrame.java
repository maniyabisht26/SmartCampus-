package ui;

import javax.swing.*;
import java.awt.*;

public class RoleSelectFrame extends JFrame {

    public RoleSelectFrame() {

        setTitle("SmartCampus");
        setSize(450, 350);

        setLocationRelativeTo(null);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(5, 1, 15, 15));

        panel.setBorder(
                BorderFactory.createEmptyBorder(30, 50, 30, 50)
        );

        JLabel heading = new JLabel(
                "SMARTCAMPUS",
                JLabel.CENTER
        );

        heading.setFont(
                new Font("Arial", Font.BOLD, 28)
        );

        JButton studentButton = new JButton("Student Login");

        JButton adminButton = new JButton("Admin Login");

        JButton exitButton = new JButton("Exit");

        studentButton.addActionListener(e -> {

            dispose();

            new LoginFrame();
        });

        adminButton.addActionListener(e -> {

            dispose();

            new AdminLoginFrame();
        });

        exitButton.addActionListener(e -> System.exit(0));

        panel.add(heading);
        panel.add(studentButton);
        panel.add(adminButton);
        panel.add(exitButton);

        add(panel);

        setVisible(true);
    }
}