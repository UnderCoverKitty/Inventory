
import java.io.FileOutputStream;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.awt.*;
import java.io.File;
import javax.swing.*;
import java.awt.event.*;
import com.itextpdf.text.Rectangle;  // Add this specific import
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PrintReceipt {

    private String orderId;
    private String customerName;
    private String cashierName;
    private List<String[]> items;
    private int totalPaid;
    private File tempFile;

    public PrintReceipt(String orderId, String customerName, String cashierName, List<String[]> items, int totalPaid) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.cashierName = cashierName;
        this.items = items;
        this.totalPaid = totalPaid;
    }
    Connection con;
    PreparedStatement ps;
    ResultSet rs;

    public void showPreview() {
        JDialog previewDialog = new JDialog();
        previewDialog.setTitle("Receipt Preview");
        previewDialog.setSize(400, 600);
        previewDialog.setLocationRelativeTo(null);
        previewDialog.setLayout(new BorderLayout());
        previewDialog.setModal(true);

        // Create receipt content panel
        JPanel receiptPanel = new JPanel();
        receiptPanel.setLayout(new BoxLayout(receiptPanel, BoxLayout.Y_AXIS));
        receiptPanel.setBackground(java.awt.Color.WHITE);
        receiptPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add receipt content
        JTextArea textArea = new JTextArea();
        textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
        textArea.setEditable(false);
        textArea.setMargin(new java.awt.Insets(10, 10, 10, 10)); // Add some margin

        // Build the content
        StringBuilder content = new StringBuilder();
        content.append(String.format("%38s\n\n", "INVENTORY STORE"));
        content.append(String.format("%35s\n", "123 Business Street"));
        content.append(String.format("%35s\n", "City, State 12345"));
        content.append(String.format("%35s\n", "Tel: (555) 123-4567"));
        content.append(String.format("%35s\n", "www.inventorystore.com"));
        content.append("----------------------------------------\n");

        // Date and Time
        LocalDateTime now = LocalDateTime.now();
        content.append(String.format("Date: %s\n", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
        content.append(String.format("Time: %s\n", now.format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
        content.append(String.format("Receipt: %s\n", orderId));
        content.append(String.format("Cashier: %s\n", cashierName));
        content.append(String.format("Customer: %s\n", customerName));
        content.append("----------------------------------------\n");
        content.append("ITEM                 PRICE   QTY   AMOUNT\n");
        content.append("----------------------------------------\n");

        // Items
        for (String[] item : items) {
            String itemName = item[0].length() > 20
                    ? item[0].substring(0, 17) + "..."
                    : String.format("%-20s", item[0]);
            content.append(String.format("%s %6s %5s %8s\n",
                    itemName, item[1], item[2], item[3]));
        }

        content.append("----------------------------------------\n");
        content.append(String.format("%35s $%,d\n", "TOTAL AMOUNT:", totalPaid));
        content.append("----------------------------------------\n");
        content.append("\nPAYMENT INFORMATION\n");
        content.append("Payment Method: Cash\n");
        content.append(String.format("Amount Paid: $%,d\n", totalPaid));
        content.append("----------------------------------------\n\n");
        content.append(String.format("%35s\n", "Thank you for shopping!"));
        content.append(String.format("%35s\n", "Returns accepted within 7 days"));
        content.append(String.format("%35s\n\n", "with original receipt"));
        content.append(String.format("%35s\n\n", "@InventoryStore"));
        content.append(String.format("%35s\n", "*** End of Receipt ***"));

        // Set the content to the text area
        textArea.setText(content.toString());

        // Make sure text starts from top
        textArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(textArea);
        receiptPanel.add(scrollPane);

        // Create buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new java.awt.Color(240, 240, 240));

        JButton printButton = new JButton("Print");
        JButton cancelButton = new JButton("Cancel");

        // Style buttons
        printButton.setPreferredSize(new java.awt.Dimension(100, 35));
        cancelButton.setPreferredSize(new java.awt.Dimension(100, 35));

        printButton.setBackground(new java.awt.Color(51, 153, 255));
        printButton.setForeground(java.awt.Color.WHITE);
        printButton.setFocusPainted(false);
        cancelButton.setBackground(new java.awt.Color(128, 128, 128));
        cancelButton.setForeground(java.awt.Color.WHITE);
        cancelButton.setFocusPainted(false);

        printButton.addActionListener(e -> {
            try {
                tempFile = File.createTempFile("receipt_", ".pdf");
                tempFile.deleteOnExit();
                generatePDF(tempFile);
                updateDatabase();
                Desktop.getDesktop().print(tempFile);
                previewDialog.dispose();
                JOptionPane.showMessageDialog(null,
                        "Order processed and printed successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                showErrorDialog(previewDialog, "Error printing: " + ex.getMessage());
            }
        });

        cancelButton.addActionListener(e -> previewDialog.dispose());

        buttonPanel.add(printButton);
        buttonPanel.add(cancelButton);

        previewDialog.add(receiptPanel, BorderLayout.CENTER);
        previewDialog.add(buttonPanel, BorderLayout.SOUTH);
        previewDialog.setVisible(true);
    }

    private void updateDatabase() throws SQLException, ClassNotFoundException {
    Connection con = null;
    PreparedStatement ps = null;

    try {
        Class.forName("com.mysql.cj.jdbc.Driver");
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/inventorymanagement", "root", "");
        con.setAutoCommit(false);

        for (String[] item : items) {
            String checkQuery = "SELECT quantity FROM product WHERE name = ?";
            ps = con.prepareStatement(checkQuery);
            ps.setString(1, item[0]);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int currentQuantity = Integer.parseInt(rs.getString("quantity"));
                int requestedQuantity = Integer.parseInt(item[2]);

                if (currentQuantity >= requestedQuantity) {
                    String updateQuery = "UPDATE product SET quantity = quantity - ? WHERE name = ?";
                    ps = con.prepareStatement(updateQuery);
                    ps.setInt(1, requestedQuantity);
                    ps.setString(2, item[0]);

                    int updated = ps.executeUpdate();
                    if (updated != 1) {
                        con.rollback();
                        throw new SQLException("Failed to update quantity for product: " + item[0]);
                    }
                } else {
                    con.rollback();
                    throw new SQLException("Insufficient quantity for product: " + item[0] + ". Available: " + currentQuantity + ", Requested: " + requestedQuantity);
                }
            } else {
                con.rollback();
                throw new SQLException("Product not found: " + item[0]);
            }
        }

        String insertQuery = "INSERT INTO orderdetail (orderid, customerName, totalpaid, orderdate) VALUES (?, ?, ?, ?)";
        ps = con.prepareStatement(insertQuery);
        ps.setString(1, orderId);
        ps.setString(2, customerName);
        ps.setInt(3, totalPaid);
        ps.setString(4, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        int inserted = ps.executeUpdate();
        if (inserted != 1) {
            con.rollback();
            throw new SQLException("Failed to create order record");
        }

        con.commit();

    } catch (SQLException e) {
        if (con != null) {
            try {
                con.rollback();
            } catch (SQLException rollbackEx) {
                throw new SQLException("Error during rollback: " + rollbackEx.getMessage() + "\nOriginal error: " + e.getMessage());
            }
        }
        throw e;
    } finally {
        if (ps != null) ps.close();
        if (con != null) {
            con.setAutoCommit(true);
            con.close();
        }
    }
}

    private void showErrorDialog(JDialog parent, String message) {
        JOptionPane.showMessageDialog(parent,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    private void generatePDF(File file) {
        Rectangle pagesize = new Rectangle(226f, 842f);
        Document document = new Document(pagesize, 10f, 10f, 10f, 10f);

        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.COURIER, 12, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.COURIER, 8, com.itextpdf.text.Font.NORMAL);
            com.itextpdf.text.Font smallFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.COURIER, 7, com.itextpdf.text.Font.NORMAL);
            com.itextpdf.text.Font totalFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.COURIER, 10, com.itextpdf.text.Font.BOLD);

            // Store Name
            Paragraph storeName = new Paragraph("INVENTORY STORE", headerFont);
            storeName.setAlignment(Element.ALIGN_CENTER);
            document.add(storeName);

            // Store Info
            Paragraph storeInfo = new Paragraph(
                    "123 Business Street\n"
                    + "City, State 12345\n"
                    + "Tel: (555) 123-4567\n"
                    + "www.inventorystore.com\n", normalFont);
            storeInfo.setAlignment(Element.ALIGN_CENTER);
            document.add(storeInfo);

            document.add(new Paragraph("----------------------------------------", normalFont));

            // Receipt Details
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

            Paragraph details = new Paragraph();
            details.setFont(normalFont);
            details.add(String.format("Date: %s\n", now.format(dateFormatter)));
            details.add(String.format("Time: %s\n", now.format(timeFormatter)));
            details.add(String.format("Receipt: %s\n", orderId));
            details.add(String.format("Cashier: %s\n", cashierName));
            details.add(String.format("Customer: %s\n", customerName));
            document.add(details);

            document.add(new Paragraph("----------------------------------------", normalFont));

            // Items Header
            Paragraph itemHeader = new Paragraph();
            itemHeader.setFont(normalFont);
            itemHeader.add("ITEM                 PRICE   QTY   AMOUNT\n");
            document.add(itemHeader);
            document.add(new Paragraph("----------------------------------------", normalFont));

            // Items
            for (String[] item : items) {
                String itemLine = String.format("%-20s", item[0].length() > 20
                        ? item[0].substring(0, 17) + "..." : item[0]);
                String priceLine = String.format("%6s", item[1]);
                String qtyLine = String.format("%5s", item[2]);
                String amountLine = String.format("%8s", item[3]);

                Paragraph itemParagraph = new Paragraph(
                        itemLine + priceLine + qtyLine + amountLine, normalFont);
                document.add(itemParagraph);
            }

            document.add(new Paragraph("----------------------------------------", normalFont));

            // Total
            Paragraph totalLine = new Paragraph();
            totalLine.setFont(totalFont);
            totalLine.add(String.format("TOTAL AMOUNT: $%,d\n", totalPaid));
            totalLine.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalLine);

            document.add(new Paragraph("----------------------------------------", normalFont));

            // Payment Info
            Paragraph paymentInfo = new Paragraph("PAYMENT INFORMATION\n", normalFont);
            paymentInfo.add(String.format("Payment Method: Cash\n"));
            paymentInfo.add(String.format("Amount Paid: $%,d\n", totalPaid));
            document.add(paymentInfo);

            document.add(new Paragraph("----------------------------------------", normalFont));

            // Footer
            Paragraph footer = new Paragraph();
            footer.setFont(smallFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.add("Thank you for shopping with us!\n\n");
            footer.add("Returns accepted within 7 days\n");
            footer.add("with original receipt\n\n");
            footer.add("Follow us on social media\n");
            footer.add("@InventoryStore\n\n");
            footer.add(String.format("*** End of Receipt ***\n"));
            document.add(footer);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (document != null && document.isOpen()) {
                document.close();
            }
        }
    }
}
