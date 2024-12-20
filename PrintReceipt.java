import java.awt.*;
import java.awt.print.*;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PrintReceipt implements Printable {
    private String orderId;
    private String customerName;
    private String cashierName;
    private List<String[]> items; // Each item: {productName, price, qty, subtotal}
    private int totalPaid;
    private final SimpleDateFormat dateFormat;
    private final Date currentDate;

    public PrintReceipt(String orderId, String customerName, String cashierName, List<String[]> items, int totalPaid) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.cashierName = cashierName;
        this.items = items;
        this.totalPaid = totalPaid;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.currentDate = new Date();
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Set paper size (80mm width thermal printer standard)
        Paper paper = new Paper();
        double width = 80 * 72 / 25.4; // 80mm converted to points
        double height = 297 * 72 / 25.4; // A4 height (can be adjusted)
        paper.setSize(width, height);
        paper.setImageableArea(10, 10, width - 20, height - 20);
        pageFormat.setPaper(paper);

        // Set font
        Font headerFont = new Font("Courier New", Font.BOLD, 12);
        Font regularFont = new Font("Courier New", Font.PLAIN, 10);
        int lineHeight = 15;
        int startX = 10;
        int startY = 20;
        int currentY = startY;

        // Header
        g2d.setFont(headerFont);
        String separator = "-------------------------------------------------";
        
        // Center align header text
        FontMetrics metrics = g2d.getFontMetrics(headerFont);
        int headerWidth = metrics.stringWidth("Inventory Management Sysasstem");
        int centerX = (int)((width - headerWidth) / 2);

        g2d.drawString(separator, startX, currentY);
        currentY += lineHeight;
        g2d.drawString("Inventory Management System", centerX, currentY);
        currentY += lineHeight;
        
        // Switch to regular font for details
        g2d.setFont(regularFont);
        
        // Order details
        g2d.drawString("Date: " + dateFormat.format(currentDate), startX, currentY);
        currentY += lineHeight;
        g2d.drawString("Order ID: " + orderId, startX, currentY);
        currentY += lineHeight;
        g2d.drawString("Cashier: " + cashierName, startX, currentY);
        currentY += lineHeight;
        g2d.drawString("Customer: " + customerName, startX, currentY);
        currentY += lineHeight;

        // Items header
        g2d.drawString(separator, startX, currentY);
        currentY += lineHeight;
        g2d.drawString(String.format("%-20s %-8s %-6s %-10s", 
            "Product", "Price", "Qty", "Subtotal"), startX, currentY);
        currentY += lineHeight;
        g2d.drawString(separator, startX, currentY);
        currentY += lineHeight;

        // Items
        for (String[] item : items) {
            String productName = truncateString(item[0], 20);
            g2d.drawString(String.format("%-20s %-8s %-6s %-10s",
                productName,
                item[1],
                item[2],
                item[3]), startX, currentY);
            currentY += lineHeight;
        }

        // Footer
        g2d.drawString(separator, startX, currentY);
        currentY += lineHeight;
        g2d.drawString(String.format("Total Paid: %-28d", totalPaid), startX, currentY);
        currentY += lineHeight;
        g2d.drawString(separator, startX, currentY);
        currentY += lineHeight;

        // Center align footer messages
        g2d.setFont(headerFont);
        String thankYouMsg = "Thank you for your purchase!";
        String visitAgainMsg = "noVisit again!";
        int thankYouWidth = metrics.stringWidth(thankYouMsg);
        int visitAgainWidth = metrics.stringWidth(visitAgainMsg);
        
        g2d.drawString(thankYouMsg, (int)((width - thankYouWidth) / 2), currentY);
        currentY += lineHeight;
        g2d.drawString(visitAgainMsg, (int)((width - visitAgainWidth) / 2), currentY);

        return PAGE_EXISTS;
    }

    private String truncateString(String str, int length) {
        if (str == null) return "";
        if (str.length() <= length) return str;
        return str.substring(0, length - 3) + "...";
    }

    // Helper method to create a paper with custom size
    private PageFormat getPageFormat() {
        PageFormat pageFormat = new PageFormat();
        Paper paper = new Paper();
        
        double width = 80 * 72 / 25.4; // 80mm
        double height = 297 * 72 / 25.4; // A4 height
        
        paper.setSize(width, height);
        paper.setImageableArea(10, 10, width - 20, height - 20);
        
        pageFormat.setPaper(paper);
        pageFormat.setOrientation(PageFormat.PORTRAIT);
        
        return pageFormat;
    }
}