package employee.managment.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;

public class attendence {
    private JFrame frame;
    private JTextField empIdField;
    private JComboBox<String> empIdDropdown;
    private JButton markAttendanceButton, viewAttendanceButton,back;
    private Connection conn;

    public attendence() {
        conn = createDatabaseConnection();

        frame = new JFrame("Attendance");
        empIdField = new JTextField();
        empIdDropdown = new JComboBox<>();
        markAttendanceButton = new JButton("Mark Attendance");
        viewAttendanceButton = new JButton("View Attendance");
        back = new JButton("Back");





        frame.setSize(550, 350);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        empIdField.setBounds(100, 80, 150, 30);
        empIdDropdown.setBounds(300, 80, 150, 30);
        markAttendanceButton.setBounds(100, 140, 150, 30);
        viewAttendanceButton.setBounds(300, 140, 150, 30);
        back.setBounds(235,270,80,20);



        frame.add(empIdField);
        frame.add(empIdDropdown);
        frame.add(markAttendanceButton);
        frame.add(viewAttendanceButton);
        frame.add(back);

        loadEmployeeIds();

        markAttendanceButton.addActionListener(e -> handleMarkAttendance());
        viewAttendanceButton.addActionListener(e -> handleViewAttendance());
        back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Main_class();
                frame.dispose();
            }
        });

        frame.setVisible(true);
    }

    private Connection createDatabaseConnection() {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/employee_man","root","root");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Database connection failed.");
            return null;
        }
    }

    private void loadEmployeeIds() {
        try {
            String query = "SELECT empID FROM employee";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                empIdDropdown.addItem(rs.getString("empID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getSelectedEmpId() {
        String empIdFromField = empIdField.getText().trim();
        String empIdFromDropdown = (String) empIdDropdown.getSelectedItem();
        return !empIdFromField.isEmpty() ? empIdFromField : empIdFromDropdown != null ? empIdFromDropdown : "";
    }

    private void handleMarkAttendance() {
        String empId = getSelectedEmpId();
        if (empId.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Enter or select Employee ID");
            return;
        }

        if (isAttendanceAlreadyMarked(empId)) {
            JOptionPane.showMessageDialog(frame, "Already attended for today.");
            return;
        }

        JFrame markFrame = new JFrame("Mark Attendance");
        markFrame.setSize(300, 200);
        markFrame.setLocationRelativeTo(null);
        markFrame.setLayout(new GridLayout(3, 1));
        markFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JLabel empIdLabel = new JLabel("Employee ID: " + empId);
        JLabel dateLabel = new JLabel("Date: " + LocalDate.now());
        JButton markButton = new JButton("Mark Your Attendance");

        markFrame.add(empIdLabel);
        markFrame.add(dateLabel);
        markFrame.add(markButton);

        markButton.addActionListener(e -> {
            markAttendanceInDatabase(empId);
            JOptionPane.showMessageDialog(markFrame, "Attendance marked for today!");
            markFrame.dispose();
        });

        markFrame.setVisible(true);
    }

    private boolean isAttendanceAlreadyMarked(String empId) {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.now();

        try {
            String query = "SELECT * FROM attendance WHERE emp_id = ? AND date = ? AND month = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, empId);
            pstmt.setDate(2, Date.valueOf(today));
            pstmt.setString(3, currentMonth.toString());
            ResultSet rs = pstmt.executeQuery();

            return rs.next(); // true if attendance for today is already marked
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Failed to check attendance status.");
            return false;
        }
    }

    private void markAttendanceInDatabase(String empId) {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.now();

        try {
            String insertQuery = "INSERT INTO attendance (emp_id, date, month) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insertQuery);
            pstmt.setString(1, empId);
            pstmt.setDate(2, Date.valueOf(today));
            pstmt.setString(3, currentMonth.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Failed to mark attendance.");
        }
    }

    private void handleViewAttendance() {
        String empId = getSelectedEmpId();
        if (empId.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Enter or select Employee ID");
            return;
        }

        JFrame viewFrame = new JFrame("View Attendance");
        viewFrame.setSize(500, 650);
        viewFrame.setLocationRelativeTo(null);
        viewFrame.setLayout(new BorderLayout());
        viewFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JLabel empIdLabel = new JLabel("Employee ID: " + empId);
        JLabel dateLabel = new JLabel("Date: " + LocalDate.now());
        JLabel totalDaysLabel = new JLabel("Total Days Worked: " + getTotalDaysWorked(empId));
        JPanel topPanel = new JPanel(new GridLayout(3, 1));
        topPanel.add(empIdLabel);
        topPanel.add(dateLabel);
        topPanel.add(totalDaysLabel);

        JTable attendanceTable = createAttendanceTable(empId);
        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        JButton closeButton = new JButton("Close");

        viewFrame.add(topPanel, BorderLayout.NORTH);
        viewFrame.add(scrollPane, BorderLayout.CENTER);
        viewFrame.add(closeButton, BorderLayout.SOUTH);

        closeButton.addActionListener(e -> viewFrame.dispose());

        viewFrame.setVisible(true);
    }

    private int getTotalDaysWorked(String empId) {
        YearMonth currentMonth = YearMonth.now();

        try {
            String query = "SELECT COUNT(*) AS days_worked FROM attendance WHERE emp_id = ? AND month = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, empId);
            pstmt.setString(2, currentMonth.toString());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("days_worked");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private JTable createAttendanceTable(String empId) {
        YearMonth currentMonth = YearMonth.now();
        String[][] data = new String[31][2];
        String[] columns = {"Date", "Attendance"};

        for (int i = 1; i <= currentMonth.lengthOfMonth(); i++) {
            LocalDate date = currentMonth.atDay(i);
            data[i - 1][0] = date.toString();

            try {
                String query = "SELECT * FROM attendance WHERE emp_id = ? AND date = ? AND month = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, empId);
                pstmt.setDate(2, Date.valueOf(date));
                pstmt.setString(3, currentMonth.toString());
                ResultSet rs = pstmt.executeQuery();

                //data[i - 1][1] = rs.next() ? "Present" : "Absent";   // print without color

                    if (rs.next()) {
                    data[i - 1][1] = "<html><font color='green'>Present</font></html>";
                    } else {
                    data[i - 1][1] = "<html><font color='red'>Absent</font></html>";
                    }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return new JTable(data, columns);
    }

    public static void main(String[] args) {
        new attendence();
    }
}
