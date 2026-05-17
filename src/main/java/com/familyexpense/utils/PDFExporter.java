package com.familyexpense.utils;

import com.familyexpense.database.DatabaseManager;
import com.familyexpense.models.Budget;
import com.familyexpense.models.Expense;
import com.familyexpense.models.FamilyMember;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PDFExporter {

    public static void exportMonthlyReport(int month, int year, File outputFile) throws IOException {
        DatabaseManager db = DatabaseManager.getInstance();

        String monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        List<Expense> expenses = db.getExpensesByMonth(month, year);
        double totalExpense = db.getTotalExpenseForMonth(month, year);
        Budget budget = db.getBudgetForMonth(month, year);
        Map<String, Double> categoryTotals = db.getCategoryTotalsForMonth(month, year);
        Map<String, Double> memberTotals = db.getMemberTotalsForMonth(month, year);
        String familyName = db.getFamilyName();

        try (PDDocument doc = new PDDocument()) {
            // ---- Page 1: Summary ----
            PDPage page1 = new PDPage(PDRectangle.A4);
            doc.addPage(page1);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page1)) {
                float pageWidth = PDRectangle.A4.getWidth();
                float pageHeight = PDRectangle.A4.getHeight();
                float margin = 50;
                float y = pageHeight - margin;

                // Header background
                cs.setNonStrokingColor(new Color(67, 97, 238));
                cs.addRect(0, pageHeight - 100, pageWidth, 100);
                cs.fill();

                // Title text
                cs.setNonStrokingColor(Color.WHITE);
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 22);
                cs.newLineAtOffset(margin, pageHeight - 45);
                cs.showText(familyName + " Family - Monthly Report");
                cs.endText();

                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 14);
                cs.newLineAtOffset(margin, pageHeight - 70);
                cs.showText(monthName + " " + year + "  |  Ghar Ka Kharcha");
                cs.endText();

                y = pageHeight - 130;

                // Summary Box
                cs.setNonStrokingColor(new Color(240, 245, 255));
                cs.addRect(margin, y - 100, pageWidth - 2 * margin, 100);
                cs.fill();
                cs.setStrokingColor(new Color(67, 97, 238));
                cs.setLineWidth(1.5f);
                cs.addRect(margin, y - 100, pageWidth - 2 * margin, 100);
                cs.stroke();

                cs.setNonStrokingColor(Color.BLACK);
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 13);
                cs.newLineAtOffset(margin + 15, y - 25);
                cs.showText("MONTHLY SUMMARY");
                cs.endText();

                double income = budget != null ? budget.getTotalIncome() : 0;
                double savings = income - totalExpense;

                drawLabelValue(cs, margin + 15, y - 50, "Total Kharcha (Expenses):", CurrencyFormatter.format(totalExpense), true, new Color(220, 50, 50));
                drawLabelValue(cs, margin + 15, y - 68, "Total Amdani (Income):", CurrencyFormatter.format(income), false, Color.BLACK);
                drawLabelValue(cs, margin + 15, y - 86, "Bachat (Savings):", CurrencyFormatter.format(savings), true, savings >= 0 ? new Color(0, 150, 80) : new Color(220, 50, 50));

                y -= 120;

                // Budget info
                if (budget != null) {
                    cs.setNonStrokingColor(Color.BLACK);
                    cs.beginText();
                    cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 13);
                    cs.newLineAtOffset(margin, y - 15);
                    cs.showText("BUDGET STATUS");
                    cs.endText();

                    double pct = budget.getMonthlyBudget() > 0 ? (totalExpense / budget.getMonthlyBudget()) * 100 : 0;
                    drawLabelValue(cs, margin, y - 33, "Monthly Budget:", CurrencyFormatter.format(budget.getMonthlyBudget()), false, Color.BLACK);
                    drawLabelValue(cs, margin, y - 51, "Spent:", String.format("%.1f%%", pct), false, pct > 80 ? new Color(220, 50, 50) : new Color(0, 150, 80));
                    y -= 80;
                }

                // Category breakdown
                cs.setNonStrokingColor(Color.BLACK);
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 13);
                cs.newLineAtOffset(margin, y - 15);
                cs.showText("CATEGORY WISE KHARCHA");
                cs.endText();

                y -= 35;
                for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                    drawLabelValue(cs, margin + 10, y, entry.getKey() + ":", CurrencyFormatter.format(entry.getValue()), false, Color.BLACK);
                    y -= 18;
                    if (y < 100) break;
                }

                y -= 15;
                // Member breakdown
                cs.setNonStrokingColor(Color.BLACK);
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 13);
                cs.newLineAtOffset(margin, y);
                cs.showText("MEMBER WISE KHARCHA");
                cs.endText();

                y -= 20;
                for (Map.Entry<String, Double> entry : memberTotals.entrySet()) {
                    drawLabelValue(cs, margin + 10, y, entry.getKey() + ":", CurrencyFormatter.format(entry.getValue()), false, Color.BLACK);
                    y -= 18;
                    if (y < 80) break;
                }

                // Footer
                cs.setNonStrokingColor(new Color(150, 150, 150));
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 9);
                cs.newLineAtOffset(margin, 30);
                cs.showText("Generated by Ghar Ka Kharcha - Family Expense Tracker  |  " + LocalDate.now().toString());
                cs.endText();
            }

            // ---- Page 2: Expense Details ----
            if (!expenses.isEmpty()) {
                PDPage page2 = new PDPage(PDRectangle.A4);
                doc.addPage(page2);

                try (PDPageContentStream cs = new PDPageContentStream(doc, page2)) {
                    float pageWidth = PDRectangle.A4.getWidth();
                    float pageHeight = PDRectangle.A4.getHeight();
                    float margin = 50;
                    float y = pageHeight - margin;

                    cs.setNonStrokingColor(new Color(67, 97, 238));
                    cs.addRect(0, pageHeight - 60, pageWidth, 60);
                    cs.fill();

                    cs.setNonStrokingColor(Color.WHITE);
                    cs.beginText();
                    cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                    cs.newLineAtOffset(margin, pageHeight - 38);
                    cs.showText("EXPENSE DETAILS - " + monthName.toUpperCase() + " " + year);
                    cs.endText();

                    y = pageHeight - 80;

                    // Table header
                    cs.setNonStrokingColor(new Color(67, 97, 238));
                    cs.addRect(margin, y - 20, pageWidth - 2 * margin, 20);
                    cs.fill();

                    cs.setNonStrokingColor(Color.WHITE);
                    cs.beginText();
                    cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 9);
                    cs.newLineAtOffset(margin + 5, y - 14);
                    cs.showText("DATE");
                    cs.newLineAtOffset(60, 0);
                    cs.showText("MEMBER");
                    cs.newLineAtOffset(80, 0);
                    cs.showText("CATEGORY");
                    cs.newLineAtOffset(90, 0);
                    cs.showText("NOTE");
                    cs.newLineAtOffset(130, 0);
                    cs.showText("AMOUNT");
                    cs.endText();

                    y -= 20;

                    boolean alternate = false;
                    for (Expense exp : expenses) {
                        if (y < 60) {
                            // Add new page if needed
                            break;
                        }
                        if (alternate) {
                            cs.setNonStrokingColor(new Color(240, 245, 255));
                            cs.addRect(margin, y - 16, pageWidth - 2 * margin, 16);
                            cs.fill();
                        }
                        alternate = !alternate;

                        cs.setNonStrokingColor(Color.BLACK);
                        cs.beginText();
                        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 8);
                        cs.newLineAtOffset(margin + 5, y - 11);
                        cs.showText(exp.getDate() != null ? exp.getDate() : "");
                        cs.newLineAtOffset(60, 0);
                        String mName = exp.getMemberName() != null ? exp.getMemberName().replace("👨", "").replace("👩", "").replace("👦", "").replace("👧", "").trim() : "";
                        cs.showText(truncate(mName, 12));
                        cs.newLineAtOffset(80, 0);
                        cs.showText(truncate(exp.getCategory() != null ? exp.getCategory() : "", 14));
                        cs.newLineAtOffset(90, 0);
                        cs.showText(truncate(exp.getNote() != null ? exp.getNote() : "", 20));
                        cs.newLineAtOffset(130, 0);
                        cs.showText(CurrencyFormatter.format(exp.getAmount()));
                        cs.endText();

                        y -= 16;
                    }

                    // Footer
                    cs.setNonStrokingColor(new Color(150, 150, 150));
                    cs.beginText();
                    cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 9);
                    cs.newLineAtOffset(margin, 30);
                    cs.showText("Generated by Ghar Ka Kharcha - Family Expense Tracker  |  " + LocalDate.now().toString());
                    cs.endText();
                }
            }

            doc.save(outputFile);
        }
    }

    private static void drawLabelValue(PDPageContentStream cs, float x, float y,
                                        String label, String value, boolean bold, Color valueColor) throws IOException {
        cs.setNonStrokingColor(Color.BLACK);
        cs.beginText();
        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
        cs.newLineAtOffset(x, y);
        cs.showText(label);
        cs.endText();

        cs.setNonStrokingColor(valueColor);
        cs.beginText();
        if (bold) {
            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
        } else {
            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
        }
        cs.newLineAtOffset(x + 200, y);
        cs.showText(value);
        cs.endText();
    }

    private static String truncate(String text, int maxLen) {
        if (text == null) return "";
        // Remove emojis (non-ASCII) for PDF compatibility
        String cleaned = text.replaceAll("[^\\x00-\\x7F]", "").trim();
        if (cleaned.length() > maxLen) {
            return cleaned.substring(0, maxLen - 2) + "..";
        }
        return cleaned;
    }
}
