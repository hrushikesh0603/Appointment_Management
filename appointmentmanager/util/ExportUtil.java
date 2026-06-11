package com.appointmentmanager.util;

import com.appointmentmanager.model.Appointment;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ExportUtil {

    private ExportUtil() {
    }

    public static void exportToCSV(List<Appointment> appointments, String filePath) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            
            String[] header = {"ID", "Client Name", "Contact Number", "Email", "Date", "Time",
                               "Service Type", "Assigned Staff", "Remarks"};
            writer.writeNext(header);

for (Appointment apt : appointments) {
                String[] row = {
                        String.valueOf(apt.getId()),
                        apt.getClientName(),
                        apt.getContactNumber(),
                        apt.getClientEmail() != null ? apt.getClientEmail() : "",
                        DateTimeUtil.formatDate(apt.getAppointmentDate()),
                        DateTimeUtil.formatTime(apt.getAppointmentTime()),
                        apt.getServiceType(),
                        apt.getAssignedStaff(),
                        apt.getRemarks() != null ? apt.getRemarks() : ""
                };
                writer.writeNext(row);
            }
        }
    }

    public static void exportToPDF(List<Appointment> appointments, String filePath) throws IOException {
        try (PdfWriter pdfWriter = new PdfWriter(filePath);
             PdfDocument pdfDoc = new PdfDocument(pdfWriter);
             Document document = new Document(pdfDoc)) {

pdfDoc.setDefaultPageSize(com.itextpdf.kernel.geom.PageSize.A4.rotate());

Paragraph title = new Paragraph("Appointment Management System - Report")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10);
            document.add(title);

Paragraph subtitle = new Paragraph("Generated on: " +
                    DateTimeUtil.formatDateForDisplay(java.time.LocalDate.now()))
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(subtitle);

float[] columnWidths = {30f, 95f, 75f, 95f, 75f, 55f, 75f, 90f, 85f};
            Table table = new Table(UnitValue.createPointArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

String[] headers = {"ID", "Client Name", "Contact", "Email", "Date", "Time",
                                "Service", "Staff", "Remarks"};
            for (String header : headers) {
                Cell cell = new Cell()
                        .add(new Paragraph(header).setBold().setFontSize(9))
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(TextAlignment.CENTER);
                table.addHeaderCell(cell);
            }

for (int i = 0; i < appointments.size(); i++) {
                Appointment apt = appointments.get(i);
                com.itextpdf.kernel.colors.Color bgColor = (i % 2 == 0) ?
                        ColorConstants.WHITE :
                        new com.itextpdf.kernel.colors.DeviceRgb(240, 240, 240);

                addCell(table, String.valueOf(apt.getId()), bgColor);
                addCell(table, apt.getClientName(), bgColor);
                addCell(table, apt.getContactNumber(), bgColor);
                addCell(table, apt.getClientEmail() != null ? apt.getClientEmail() : "", bgColor);
                addCell(table, DateTimeUtil.formatDate(apt.getAppointmentDate()), bgColor);
                addCell(table, DateTimeUtil.formatTime(apt.getAppointmentTime()), bgColor);
                addCell(table, apt.getServiceType(), bgColor);
                addCell(table, apt.getAssignedStaff(), bgColor);
                addCell(table, apt.getRemarks() != null ? apt.getRemarks() : "", bgColor);
            }

            document.add(table);

Paragraph footer = new Paragraph("Total Appointments: " + appointments.size())
                    .setFontSize(10)
                    .setMarginTop(15)
                    .setTextAlignment(TextAlignment.RIGHT);
            document.add(footer);
        }
    }

    private static void addCell(Table table, String content,
                                com.itextpdf.kernel.colors.Color bgColor) {
        Cell cell = new Cell()
                .add(new Paragraph(content).setFontSize(8))
                .setBackgroundColor(bgColor)
                .setTextAlignment(TextAlignment.LEFT);
        table.addCell(cell);
    }
}