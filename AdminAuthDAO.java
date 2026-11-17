package ui;

import database.DatabaseConnection;

import java.sql.*;
import java.security.SecureRandom;
import java.security.MessageDigest;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import java.util.Base64;

public class AdminAuthDAO {

    private static final int ITERATIONS = 120000;
    private static final int KEY_LENGTH = 256;

    // Password hashing
    private static String hashPassword(String password) throws Exception {

        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                ITERATIONS,
                KEY_LENGTH
        );

        byte[] hash = SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .getEncoded();

        return ITERATIONS + ":"
                + Base64.getEncoder().encodeToString(salt) + ":"
                + Base64.getEncoder().encodeToString(hash);
    }

    // Verify password
    private static boolean verifyPassword(
            String password, String storedHash) throws Exception {

        String[] parts = storedHash.split(":");

        int iterations = Integer.parseInt(parts[0]);

        byte[] salt = Base64.getDecoder().decode(parts[1]);
        byte[] originalHash = Base64.getDecoder().decode(parts[2]);

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                iterations,
                originalHash.length * 8
        );

        byte[] newHash = SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .getEncoded();

        return MessageDigest.isEqual(originalHash, newHash);
    }

    // Admin Registration
    public static boolean registerAdmin(
            String fullName,
            String username,
            String password) throws Exception {

        String checkSQL =
                "SELECT admin_id FROM admins WHERE username = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement check = con.prepareStatement(checkSQL)) {

            check.setString(1, username);

            ResultSet rs = check.executeQuery();

            if (rs.next()) {
                return false;
            }

            String sql = "INSERT INTO admins "
                    + "(full_name, username, password_hash) "
                    + "VALUES (?, ?, ?)";

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, fullName);
                ps.setString(2, username);
                ps.setString(3, hashPassword(password));

                return ps.executeUpdate() > 0;
            }
        }
    }

    // Admin Login
    public static String loginAdmin(
            String username,
            String password) throws Exception {

        String sql = "SELECT full_name, password_hash "
                + "FROM admins WHERE username = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    String storedHash = rs.getString("password_hash");

                    if (verifyPassword(password, storedHash)) {
                        return rs.getString("full_name");
                    }
                }
            }
        }

        return null;
    }
}