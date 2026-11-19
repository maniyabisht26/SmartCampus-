package certificate;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.*;
import java.sql.*;
import java.util.UUID;

import database.DatabaseConnection;

public class CertificateFrame extends JFrame {

    private final int studentId;
    private final boolean adminMode;

    private JTable eventTable;
    private DefaultTableModel tableModel;
    private JComboBox<EventItem> eventCombo;

    // STUDENT CONSTRUCTOR
    public CertificateFrame(int studentId) {
        this.studentId = studentId;
        this.adminMode = false;

        setTitle("SmartCampus - My Certificates");
        setSize(850, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        buildStudentInterface();
        loadEligibleEvents();

        setVisible(true);
    }

    // ADMIN CONSTRUCTOR
    public CertificateFrame() {
        this.studentId = -1;
        this.adminMode = true;

        setTitle("SmartCampus - Admin Certificate Management");
        setSize(950, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        buildAdminInterface();
        loadEvents();

        setVisible(true);
    }

    // EVENT MODEL
    private static class EventItem {

        int id;
        String name;

        EventItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return id + " - " + name;
        }
    }

    // ================= STUDENT INTERFACE =================

    private void buildStudentInterface() {

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        );

        JLabel heading = new JLabel("MY CERTIFICATE ELIGIBILITY");
        heading.setFont(new Font("Arial", Font.BOLD, 24));
        heading.setForeground(new Color(25, 48, 90));

        mainPanel.add(heading, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new String[]{
                    "Event ID",
                    "Event Name",
                    "Event Date",
                    "Attendance",
                    "Certificate Code"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        eventTable = new JTable(tableModel);
        eventTable.setRowHeight(28);

        mainPanel.add(
                new JScrollPane(eventTable),
                BorderLayout.CENTER
        );

        JButton refreshButton = new JButton("Refresh");
        JButton generateButton = new JButton("Generate Certificate");

        refreshButton.addActionListener(e -> loadEligibleEvents());
        generateButton.addActionListener(e -> generateStudentCertificate());

        JPanel buttons = new JPanel();
        buttons.add(refreshButton);
        buttons.add(generateButton);

        mainPanel.add(buttons, BorderLayout.SOUTH);

        add(mainPanel);
    }

    // LOAD STUDENT'S ELIGIBLE EVENTS
    private void loadEligibleEvents() {

        tableModel.setRowCount(0);

        String sql =
                "SELECT e.event_id, e.event_name, e.event_date, "
                + "a.status, c.certificate_code "
                + "FROM attendance a "
                + "JOIN events e ON a.event_id = e.event_id "
                + "LEFT JOIN certificates c "
                + "ON c.student_id = a.student_id "
                + "AND c.event_id = a.event_id "
                + "WHERE a.student_id = ? "
                + "AND LOWER(a.status) = 'present'";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    tableModel.addRow(new Object[]{
                        rs.getInt("event_id"),
                        rs.getString("event_name"),
                        rs.getDate("event_date"),
                        rs.getString("status"),
                        rs.getString("certificate_code")
                    });
                }
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "No certificates available yet. "
                        + "Attend an event to become eligible."
                );
            }

        } catch (Exception ex) {

            showError("Error loading eligible events", ex);
        }
    }

    // GENERATE STUDENT CERTIFICATE
    private void generateStudentCertificate() {

        int row = eventTable.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(
                    this, "Please select an event."
            );
            return;
        }

        int eventId = Integer.parseInt(
                tableModel.getValueAt(row, 0).toString()
        );

        String sql =
                "SELECT s.full_name, e.event_name, e.event_date "
                + "FROM attendance a "
                + "JOIN students s ON a.student_id = s.student_id "
                + "JOIN events e ON a.event_id = e.event_id "
                + "WHERE a.student_id = ? "
                + "AND a.event_id = ? "
                + "AND LOWER(a.status) = 'present'";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, eventId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    String code = issueCertificate(
                            studentId, eventId
                    ).code;

                    String name = rs.getString("full_name");
                    String event = rs.getString("event_name");
                    String date = String.valueOf(
                            rs.getDate("event_date")
                    );

                    showCertificate(name, event, date, code);

                    loadEligibleEvents();

                } else {

                    JOptionPane.showMessageDialog(
                            this,
                            "You are not eligible for this certificate."
                    );
                }
            }

        } catch (Exception ex) {

            showError("Certificate generation failed", ex);
        }
    }

    // ================= ADMIN INTERFACE =================

    private void buildAdminInterface() {

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));

        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        );

        JLabel heading = new JLabel("CERTIFICATE MANAGEMENT");
        heading.setFont(new Font("Arial", Font.BOLD, 24));
        heading.setForeground(new Color(25, 48, 90));

        eventCombo = new JComboBox<>();

        JButton loadButton = new JButton("Load Students");
        JButton refreshButton = new JButton("Refresh Events");
        JButton generateButton = new JButton("Generate Selected");
        JButton generateAllButton = new JButton("Generate All Present");

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        topPanel.add(new JLabel("Select Event:"));
        topPanel.add(eventCombo);
        topPanel.add(loadButton);
        topPanel.add(refreshButton);

        tableModel = new DefaultTableModel(
                new String[]{
                    "Student ID",
                    "Student Name",
                    "Attendance",
                    "Certificate Code"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        eventTable = new JTable(tableModel);
        eventTable.setRowHeight(28);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        centerPanel.add(topPanel, BorderLayout.NORTH);
        centerPanel.add(
                new JScrollPane(eventTable),
                BorderLayout.CENTER
        );

        JPanel bottomPanel = new JPanel();

        bottomPanel.add(generateButton);
        bottomPanel.add(generateAllButton);

        mainPanel.add(heading, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        loadButton.addActionListener(e -> loadAdminStudents());

        refreshButton.addActionListener(e -> loadEvents());

        generateButton.addActionListener(e -> generateSelectedCertificate());

        generateAllButton.addActionListener(e -> generateAllCertificates());
    }

    // LOAD EVENTS
    private void loadEvents() {

        eventCombo.removeAllItems();

        String sql =
                "SELECT event_id, event_name FROM events";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                eventCombo.addItem(
                        new EventItem(
                                rs.getInt("event_id"),
                                rs.getString("event_name")
                        )
                );
            }

        } catch (Exception ex) {

            showError("Unable to load events", ex);
        }
    }

    // LOAD STUDENTS AND ATTENDANCE FOR SELECTED EVENT
    private void loadAdminStudents() {

        EventItem selected =
                (EventItem) eventCombo.getSelectedItem();

        if (selected == null) {
            JOptionPane.showMessageDialog(
                    this, "Please select an event."
            );
            return;
        }

        tableModel.setRowCount(0);

        String sql =
                "SELECT s.student_id, s.full_name, a.status, "
                + "c.certificate_code "
                + "FROM attendance a "
                + "JOIN students s ON a.student_id = s.student_id "
                + "LEFT JOIN certificates c "
                + "ON c.student_id = a.student_id "
                + "AND c.event_id = a.event_id "
                + "WHERE a.event_id = ? "
                + "ORDER BY s.full_name";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, selected.id);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    tableModel.addRow(new Object[]{
                        rs.getInt("student_id"),
                        rs.getString("full_name"),
                        rs.getString("status"),
                        rs.getString("certificate_code")
                    });
                }
            }

            if (tableModel.getRowCount() == 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "No attendance records found for this event."
                );
            }

        } catch (Exception ex) {

            showError("Unable to load students", ex);
        }
    }

    // GENERATE CERTIFICATE FOR SELECTED STUDENT
    private void generateSelectedCertificate() {

        int row = eventTable.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(
                    this, "Please select a student."
            );
            return;
        }

        String status = String.valueOf(
                tableModel.getValueAt(row, 2)
        );

        if (!"Present".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Only students marked Present are eligible."
            );
            return;
        }

        int studentId = Integer.parseInt(
                tableModel.getValueAt(row, 0).toString()
        );

        String studentName =
                tableModel.getValueAt(row, 1).toString();

        EventItem selected =
                (EventItem) eventCombo.getSelectedItem();

        if (selected == null) return;

        try {

            IssueResult result = issueCertificate(
                    studentId, selected.id
            );

            String date = getEventDate(selected.id);

            showCertificate(
                    studentName,
                    selected.name,
                    date,
                    result.code
            );

            loadAdminStudents();

            JOptionPane.showMessageDialog(
                    this,
                    result.created
                            ? "Certificate issued successfully!"
                            : "Certificate already existed. Showing existing certificate."
            );

        } catch (Exception ex) {

            showError("Certificate generation failed", ex);
        }
    }

    // GENERATE CERTIFICATES FOR ALL PRESENT STUDENTS
    private void generateAllCertificates() {

        EventItem selected =
                (EventItem) eventCombo.getSelectedItem();

        if (selected == null) {
            JOptionPane.showMessageDialog(
                    this, "Please select an event."
            );
            return;
        }

        String sql =
                "SELECT student_id FROM attendance "
                + "WHERE event_id = ? "
                + "AND LOWER(status) = 'present'";

        int created = 0;
        int existing = 0;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, selected.id);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    int id = rs.getInt("student_id");

                    IssueResult result = issueCertificate(
                            id, selected.id
                    );

                    if (result.created) {
                        created++;
                    } else {
                        existing++;
                    }
                }
            }

            loadAdminStudents();

            JOptionPane.showMessageDialog(
                    this,
                    "Certificate generation completed!\n\n"
                    + "New certificates: " + created
                    + "\nAlready issued: " + existing
            );

        } catch (Exception ex) {

            showError("Unable to generate certificates", ex);
        }
    }

    // ================= DATABASE CERTIFICATE OPERATIONS =================

    private static class IssueResult {

        String code;
        boolean created;

        IssueResult(String code, boolean created) {
            this.code = code;
            this.created = created;
        }
    }

    // INSERT CERTIFICATE OR RETURN EXISTING CERTIFICATE
    private IssueResult issueCertificate(
            int studentId, int eventId) throws SQLException {

        try (Connection con = DatabaseConnection.getConnection()) {

            if (con == null) {
                throw new SQLException("Database connection failed.");
            }

            String checkSql =
                    "SELECT certificate_code FROM certificates "
                    + "WHERE student_id = ? AND event_id = ?";

            try (PreparedStatement check =
                    con.prepareStatement(checkSql)) {

                check.setInt(1, studentId);
                check.setInt(2, eventId);

                try (ResultSet rs = check.executeQuery()) {

                    if (rs.next()) {
                        return new IssueResult(
                                rs.getString("certificate_code"),
                                false
                        );
                    }
                }
            }

            String code = "SC-" +
                    UUID.randomUUID().toString()
                    .substring(0, 8).toUpperCase();

            String insertSql =
                    "INSERT INTO certificates "
                    + "(student_id, event_id, certificate_code) "
                    + "VALUES (?, ?, ?)";

            try (PreparedStatement insert =
                    con.prepareStatement(insertSql)) {

                insert.setInt(1, studentId);
                insert.setInt(2, eventId);
                insert.setString(3, code);

                insert.executeUpdate();
            }

            return new IssueResult(code, true);
        }
    }

    // GET EVENT DATE
    private String getEventDate(int eventId) throws SQLException {

        String sql =
                "SELECT event_date FROM events WHERE event_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, eventId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return String.valueOf(rs.getDate("event_date"));
                }
            }
        }

        return "N/A";
    }

    // ================= CERTIFICATE DISPLAY =================

    private void showCertificate(
            String studentName,
            String eventName,
            String eventDate,
            String certificateCode) {

        JFrame certificateWindow = new JFrame(
                "SmartCampus - Certificate"
        );

        certificateWindow.setSize(950, 650);
        certificateWindow.setLocationRelativeTo(this);
        certificateWindow.setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        JPanel certificatePanel = new JPanel();

        certificatePanel.setLayout(
                new BoxLayout(certificatePanel, BoxLayout.Y_AXIS)
        );

        certificatePanel.setPreferredSize(
                new Dimension(850, 550)
        );

        certificatePanel.setBackground(Color.WHITE);

        certificatePanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(25, 48, 90), 8
                        ),
                        BorderFactory.createEmptyBorder(
                                35, 35, 35, 35
                        )
                )
        );

        JLabel college = new JLabel("SMARTCAMPUS");

        college.setFont(
                new Font("Arial", Font.BOLD, 25)
        );

        college.setForeground(new Color(25, 48, 90));
        college.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel(
                "CERTIFICATE OF PARTICIPATION"
        );

        title.setFont(
                new Font("Serif", Font.BOLD, 30)
        );

        title.setForeground(new Color(25, 48, 90));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel text1 = new JLabel(
                "This certificate is proudly presented to"
        );

        text1.setFont(new Font("Serif", Font.PLAIN, 19));
        text1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel name = new JLabel(studentName);

        name.setFont(
                new Font("Serif", Font.BOLD, 34)
        );

        name.setForeground(new Color(25, 48, 90));
        name.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel text2 = new JLabel(
                "for successfully attending"
        );

        text2.setFont(new Font("Serif", Font.PLAIN, 19));
        text2.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel event = new JLabel(eventName);

        event.setFont(
                new Font("Serif", Font.BOLD, 25)
        );

        event.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel date = new JLabel(
                "Event Date: " + eventDate
        );

        date.setFont(new Font("Arial", Font.PLAIN, 17));
        date.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel code = new JLabel(
                "Certificate ID: " + certificateCode
        );

        code.setFont(new Font("Arial", Font.PLAIN, 13));
        code.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel footer = new JLabel(
                "We appreciate your participation!"
        );

        footer.setFont(new Font("Serif", Font.ITALIC, 19));
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);

        certificatePanel.add(Box.createVerticalGlue());
        certificatePanel.add(college);
        certificatePanel.add(Box.createVerticalStrut(25));
        certificatePanel.add(title);
        certificatePanel.add(Box.createVerticalStrut(35));
        certificatePanel.add(text1);
        certificatePanel.add(Box.createVerticalStrut(15));
        certificatePanel.add(name);
        certificatePanel.add(Box.createVerticalStrut(20));
        certificatePanel.add(text2);
        certificatePanel.add(Box.createVerticalStrut(10));
        certificatePanel.add(event);
        certificatePanel.add(Box.createVerticalStrut(20));
        certificatePanel.add(date);
        certificatePanel.add(Box.createVerticalStrut(15));
        certificatePanel.add(code);
        certificatePanel.add(Box.createVerticalStrut(25));
        certificatePanel.add(footer);
        certificatePanel.add(Box.createVerticalGlue());

        JButton printButton = new JButton("Print / Save as PDF");

        printButton.addActionListener(
                e -> printCertificate(certificatePanel)
        );

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(printButton);

        certificateWindow.add(
                certificatePanel,
                BorderLayout.CENTER
        );

        certificateWindow.add(
                buttonPanel,
                BorderLayout.SOUTH
        );

        certificateWindow.setVisible(true);
    }

    // PRINT CERTIFICATE
    private void printCertificate(JPanel panel) {

        PrinterJob job = PrinterJob.getPrinterJob();

        job.setJobName("SmartCampus Certificate");

        job.setPrintable(new Printable() {

            @Override
            public int print(
                    Graphics graphics,
                    PageFormat pageFormat,
                    int pageIndex) {

                if (pageIndex > 0) {
                    return NO_SUCH_PAGE;
                }

                Graphics2D g2d = (Graphics2D) graphics;

                g2d.translate(
                        pageFormat.getImageableX(),
                        pageFormat.getImageableY()
                );

                double scaleX =
                        pageFormat.getImageableWidth()
                        / panel.getWidth();

                double scaleY =
                        pageFormat.getImageableHeight()
                        / panel.getHeight();

                double scale = Math.min(scaleX, scaleY);

                g2d.scale(scale, scale);

                panel.printAll(g2d);

                return PAGE_EXISTS;
            }
        });

        if (job.printDialog()) {

            try {

                job.print();

                JOptionPane.showMessageDialog(
                        this,
                        "Certificate printed successfully!"
                );

            } catch (PrinterException ex) {

                showError("Printing failed", ex);
            }
        }
    }

    // COMMON ERROR MESSAGE
    private void showError(String message, Exception ex) {

        JOptionPane.showMessageDialog(
                this,
                message + ":\n" + ex.getMessage(),
                "SmartCampus Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}