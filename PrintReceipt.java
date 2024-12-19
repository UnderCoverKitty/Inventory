import java.awt.*;
import java.awt.print.*;
import java.util.List;

public class PrintReceipt implements Printable {
    private String orderId;
    private String customerName;
    private String cashierName;
    private List<String[]> items; // Each item: {productName, price, qty, subtotal}
    private int totalPaid;

    public PrintReceipt(String orderId, String customerName, String cashierName, List<String[]> items, int totalPaid) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.cashierName = cashierName;
        this.items = items;
        this.totalPaid = totalPaid;
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Set paper size (58mm width)
        Paper paper = new Paper();
        double receiptWidth = 58 * 72 / 25.4; // 58mm width
        double receiptHeight = 300 * 72 / 25.4; // Dynamic height
        paper.setSize(receiptWidth, receiptHeight);
        paper.setImageableArea(5, 5, receiptWidth - 10, receiptHeight - 10);
        pageFormat.setPaper(paper);

        // Set monospaced font
        Font monoFont = new Font("Courier", Font.PLAIN, 10);
        g2d.setFont(monoFont);
        int lineHeight = 12;

        // Starting position
        int x = 10;
        int y = 20;

        // Header
        g2d.drawString("================================", x, y);
        y += lineHeight;
        g2d.drawString("       INVENTORY SYSTEM        ", x, y);
        y += lineHeight;
        g2d.drawString("================================", x, y);
        y += lineHeight;

        // Order and Customer Details
        g2d.drawString("Order ID: " + orderId, x, y);
        y += lineHeight;
        g2d.drawString("Cashier: " + cashierName, x, y);
        y += lineHeight;
        g2d.drawString("Customer: " + customerName, x, y);
        y += lineHeight;

        // Table Header
        g2d.drawString("--------------------------------", x, y);
        y += lineHeight;
        g2d.drawString(String.format("%-12s%6s%3s%8s", "Product", "Price", "Qty", "Subtotal"), x, y);
        y += lineHeight;
        g2d.drawString("--------------------------------", x, y);
        y += lineHeight;

        // Items
        for (String[] item : items) {
            g2d.drawString(String.format("%-12s%6s%3s%8s", item[0], item[1], item[2], item[3]), x, y);
            y += lineHeight;
        }

        // Total
        g2d.drawString("--------------------------------", x, y);
        y += lineHeight;
        g2d.drawString(String.format("Total Paid: %21s", totalPaid), x, y);
        y += lineHeight;
        g2d.drawString("--------------------------------", x, y);
        y += lineHeight;

        // Footer
        g2d.drawString("   Thank you for your purchase!   ", x, y);
        y += lineHeight;
        g2d.drawString("        Visit again!              ", x, y);

        return PAGE_EXISTS;
    }
}
