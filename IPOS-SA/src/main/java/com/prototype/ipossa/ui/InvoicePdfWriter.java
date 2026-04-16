package com.prototype.ipossa.ui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


//invoice pdf creator with invoice details
public class InvoicePdfWriter {

    public static class LineItem {
        public final String itemId, description;
        public final int quantity;
        public final double unitCost;
        public LineItem(String id, String d, int q, double c) {
            this.itemId = id; this.description = d; this.quantity = q; this.unitCost = c;
        }
        public double lineTotal() { return quantity * unitCost; }
    }

    public static void write(File out,
                             String invoiceId, String orderId, String orderDate,
                             String merchantName, String merchantAddress,
                             List<LineItem> items, double total) throws Exception {

        // Build the page content stream
        StringBuilder cs = new StringBuilder();
        // Header
        cs.append("BT\n");
        cs.append("/F2 22 Tf\n");          // Helvetica-Bold 22
        cs.append("72 770 Td\n");
        cs.append("(InfoPharma Ltd) Tj\n");
        cs.append("/F1 10 Tf\n");
        cs.append("0 -16 Td\n");
        cs.append("(Wholesale Pharmaceutical Supplier) Tj\n");
        cs.append("ET\n");

        // Invoice title
        cs.append("BT\n/F2 18 Tf\n400 770 Td\n(INVOICE) Tj\nET\n");

        // Invoice metadata block
        cs.append("BT\n/F1 10 Tf\n400 745 Td\n(Invoice #: ").append(esc(invoiceId)).append(") Tj\n");
        cs.append("0 -14 Td\n(Order #:   ").append(esc(orderId)).append(") Tj\n");
        cs.append("0 -14 Td\n(Date:      ").append(esc(orderDate)).append(") Tj\nET\n");

        // Bill-to
        cs.append("BT\n/F2 11 Tf\n72 700 Td\n(BILL TO) Tj\n");
        cs.append("/F1 11 Tf\n0 -16 Td\n(").append(esc(merchantName)).append(") Tj\n");
        // Address can be multi-line and  split on commas and newlines
        String[] addrLines = (merchantAddress == null ? "" : merchantAddress).split("[,\\n]");
        for (String l : addrLines) {
            String t = l.trim();
            if (t.isEmpty()) continue;
            cs.append("0 -13 Td\n(").append(esc(t)).append(") Tj\n");
        }
        cs.append("ET\n");

        // Items table header
        float yTop = 600;
        cs.append("0.85 0.85 0.85 rg\n");
        cs.append("72 ").append(yTop).append(" 468 20 re f\n");
        cs.append("0 0 0 rg\n");
        cs.append("BT\n/F2 10 Tf\n");
        cs.append("78 ").append(yTop + 6).append(" Td\n(Item ID) Tj\n");
        cs.append("80 0 Td\n(Description) Tj\n");
        cs.append("210 0 Td\n(Qty) Tj\n");
        cs.append("40 0 Td\n(Unit GBP) Tj\n");
        cs.append("60 0 Td\n(Line GBP) Tj\n");
        cs.append("ET\n");

        // Items rows
        float y = yTop - 16;
        for (LineItem it : items) {
            cs.append("BT\n/F1 10 Tf\n");
            cs.append("78 ").append(y).append(" Td\n(").append(esc(it.itemId)).append(") Tj\n");
            cs.append("80 0 Td\n(").append(esc(trunc(it.description, 30))).append(") Tj\n");
            cs.append("210 0 Td\n(").append(it.quantity).append(") Tj\n");
            cs.append("40 0 Td\n(").append(String.format("%.2f", it.unitCost)).append(") Tj\n");
            cs.append("60 0 Td\n(").append(String.format("%.2f", it.lineTotal())).append(") Tj\n");
            cs.append("ET\n");
            y -= 14;
            if (y < 120) break; // simple single-page guard
        }

        // Total
        y -= 6;
        cs.append("0 0 0 RG\n0.5 w\n");
        cs.append("72 ").append(y).append(" m 540 ").append(y).append(" l S\n");
        y -= 18;
        cs.append("BT\n/F2 12 Tf\n");
        cs.append("380 ").append(y).append(" Td\n(TOTAL DUE: GBP ").append(String.format("%.2f", total)).append(") Tj\nET\n");

        // Footer
        cs.append("BT\n/F1 9 Tf\n72 80 Td\n(Payment terms: net 30 days from invoice date.) Tj\n");
        cs.append("0 -12 Td\n(Please reference the invoice number when making payment.) Tj\nET\n");

        byte[] csBytes = cs.toString().getBytes(StandardCharsets.ISO_8859_1);

        // Build PDF objects
        List<byte[]> objects = new ArrayList<>();
        objects.add(bytes("<< /Type /Catalog /Pages 2 0 R >>"));
        objects.add(bytes("<< /Type /Pages /Kids [3 0 R] /Count 1 >>"));
        objects.add(bytes("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] " +
                "/Resources << /Font << /F1 5 0 R /F2 6 0 R >> >> /Contents 4 0 R >>"));
        // Stream object
        ByteArrayOutputStream streamObj = new ByteArrayOutputStream();
        streamObj.writeBytes(("<< /Length " + csBytes.length + " >>\nstream\n").getBytes(StandardCharsets.ISO_8859_1));
        streamObj.writeBytes(csBytes);
        streamObj.writeBytes("\nendstream".getBytes(StandardCharsets.ISO_8859_1));
        objects.add(streamObj.toByteArray());
        objects.add(bytes("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>"));
        objects.add(bytes("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>"));

        // Write the PDF
        try (OutputStream os = new FileOutputStream(out)) {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            buf.writeBytes("%PDF-1.4\n%\u00E2\u00E3\u00CF\u00D3\n".getBytes(StandardCharsets.ISO_8859_1));
            int[] offsets = new int[objects.size() + 1];
            for (int i = 0; i < objects.size(); i++) {
                offsets[i + 1] = buf.size();
                buf.writeBytes(((i + 1) + " 0 obj\n").getBytes(StandardCharsets.ISO_8859_1));
                buf.writeBytes(objects.get(i));
                buf.writeBytes("\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));
            }
            int xrefOffset = buf.size();
            buf.writeBytes(("xref\n0 " + (objects.size() + 1) + "\n").getBytes(StandardCharsets.ISO_8859_1));
            buf.writeBytes("0000000000 65535 f \n".getBytes(StandardCharsets.ISO_8859_1));
            for (int i = 1; i <= objects.size(); i++) {
                buf.writeBytes(String.format("%010d 00000 n \n", offsets[i]).getBytes(StandardCharsets.ISO_8859_1));
            }
            buf.writeBytes(("trailer\n<< /Size " + (objects.size() + 1) + " /Root 1 0 R >>\n" +
                    "startxref\n" + xrefOffset + "\n%%EOF\n").getBytes(StandardCharsets.ISO_8859_1));
            os.write(buf.toByteArray());
        }
    }

    private static byte[] bytes(String s) { return s.getBytes(StandardCharsets.ISO_8859_1); }
    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
    private static String trunc(String s, int n) {
        if (s == null) return "";
        return s.length() <= n ? s : s.substring(0, n - 1) + "…";
    }
}
