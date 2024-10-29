
package employee.managment.system;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class payroll {
    private JFrame frame;
    private JComboBox<String> empIdDropdown;
    private JTextArea payrollDisplay;
    private Connection conn;
    private JButton back;

    public payroll() {
        conn = createDatabaseConnection();

        frame = new JFrame("Payroll Calculation");
        frame.setSize(500, 600);
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        empIdDropdown = new JComboBox<>();
        payrollDisplay = new JTextArea();
        payrollDisplay.setEditable(false);
        back = new JButton("Back");

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Select Employee ID:"));
        topPanel.add(empIdDropdown);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(payrollDisplay), BorderLayout.CENTER);
        frame.add(back,BorderLayout.SOUTH);

        empIdDropdown.addItem("Select EMP ID");  // Set default option
        loadEmployeeIds();
        empIdDropdown.addActionListener(e -> displayPayroll());
        back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Main_class();
                frame.dispose();
            }
        });





        frame.setLocationRelativeTo(null); // Center the JFrame
        frame.setVisible(true);
    }

    private Connection createDatabaseConnection() {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/employee_man", "root", "root");
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

    private void displayPayroll() {
        String empId = (String) empIdDropdown.getSelectedItem();
        if (empId == null || empId.isEmpty() || empId.equals("Select EMP ID")) {
            JOptionPane.showMessageDialog(frame, "Please select a valid Employee ID.");
            return;
        }

        try {
            String query = "SELECT name, salary FROM employee WHERE empID = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, empId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String empName = rs.getString("name");
                double ctc = rs.getDouble("salary");

                calculateAndDisplayPayroll(empName, ctc);
            } else {
                JOptionPane.showMessageDialog(frame, "Employee not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void calculateAndDisplayPayroll(String empName, double ctc) {
        double annualBonus = 100000;
        double monthlyCtc = (ctc - annualBonus) / 12;
        double baseSalary = 0.4 * monthlyCtc;

        // Allowances
        double hra = 0.4 * baseSalary;
        double lta = 0.1 * baseSalary;
        double telephoneAllowance = 500;

        // Deductions
        double epfEmployeeContribution = 0.12 * baseSalary;
        double epfEmployerContribution = 0.12 * baseSalary;
        double gratuity = 0.08 * baseSalary;
        double professionalTax = 300;

        // Other Allowances and Overall Salary
        double otherAllowances = monthlyCtc - (baseSalary + epfEmployerContribution + gratuity + professionalTax);
        double overallSalary = baseSalary + otherAllowances;

        // Tax calculation
        double taxableAmount = overallSalary - hra - lta - telephoneAllowance;
        double tax = 0.08 * taxableAmount;

        // Final Salary
        double finalSalary = overallSalary - tax - epfEmployeeContribution;

        // Display Results
        String payrollDetails = String.format(
                "Employee Name: %s\n" +
                        "CTC: %.2f\n\n" +
                        "Calculations:\n" +
                        "-----------------------------------\n" +
                        "Fixed Bonus (Year-end): %.2f\n" +
                        "Monthly CTC: %.2f\n" +
                        "Base Salary: %.2f\n\n" +
                        "Allowances:\n" +
                        "HRA: %.2f\n" +
                        "LTA: %.2f\n" +
                        "Telephone Allowance: %.2f\n\n" +
                        "Deductions:\n" +
                        "EPF (Employee Contribution): %.2f\n" +
                        "EPF (Employer Contribution): %.2f\n" +
                        "Gratuity: %.2f\n" +
                        "Professional Tax: %.2f\n\n" +
                        "Other Allowances: %.2f\n" +
                        "Overall Salary: %.2f\n" +
                        "Tax: %.2f\n\n" +
                        "Final Salary: %.2f",
                empName, ctc,
                annualBonus, monthlyCtc, baseSalary,
                hra, lta, telephoneAllowance,
                epfEmployeeContribution, epfEmployerContribution, gratuity, professionalTax,
                otherAllowances, overallSalary, tax, finalSalary
        );

        payrollDisplay.setText(payrollDetails);
    }

    public static void main(String[] args) {
        new payroll();
    }
}
