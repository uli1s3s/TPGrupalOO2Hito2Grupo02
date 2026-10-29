package com.unla.ghsicilianotfi.services.implementation;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.unla.ghsicilianotfi.entities.Cajero;
import com.unla.ghsicilianotfi.entities.Cocinero;
import com.unla.ghsicilianotfi.entities.LiquidacionDeHaber;
import com.unla.ghsicilianotfi.entities.Personal;
import com.unla.ghsicilianotfi.services.IReciboSueldoPDFService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service("reciboSueldoPDFService")
public class ReciboSueldoPDFService implements IReciboSueldoPDFService {

    private static final DeviceRgb COLOR_PRIMARIO = new DeviceRgb(33, 37, 41);
    private static final DeviceRgb COLOR_FONDO_TH = new DeviceRgb(241, 243, 245);
    private static final DeviceRgb COLOR_BORDE = new DeviceRgb(206, 212, 218);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public byte[] generarReciboPDF(LiquidacionDeHaber liquidacion) {
        if (liquidacion == null) {
            throw new IllegalArgumentException("La liquidación no puede ser nula");
        }

        Personal personal = liquidacion.getPersonal();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            pdfDoc.setDefaultPageSize(PageSize.A4);
            Document document = new Document(pdfDoc);
            document.setMargins(30, 35, 30, 35);

            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

            // 1. ENCABEZADO INSTITUCIONAL
            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{65, 35}));
            headerTable.setWidth(UnitValue.createPercentValue(100));
            headerTable.setBorder(Border.NO_BORDER);

            Cell leftHeader = new Cell().setBorder(Border.NO_BORDER);
            leftHeader.add(new Paragraph("EPICENTRO GOURMET S.A.")
                    .setBold().setFontSize(16).setFontColor(COLOR_PRIMARIO).setMargin(0));
            leftHeader.add(new Paragraph("Recibo de Liquidación de Haberes (Ley N° 20.744)")
                    .setFontSize(10).setItalic().setFontColor(ColorConstants.GRAY).setMarginTop(2));
            leftHeader.add(new Paragraph("CUIT: 30-71234567-8 | Av. 29 de Septiembre 3901, Lanús")
                    .setFontSize(8).setFontColor(ColorConstants.DARK_GRAY));

            String descPeriodo = liquidacion.getPeriodo();
            try {
                java.time.YearMonth ym = java.time.YearMonth.parse(liquidacion.getPeriodo().trim());
                String mes = ym.getMonth().getDisplayName(java.time.format.TextStyle.FULL, new java.util.Locale("es", "AR"));
                descPeriodo = "Liquidación de " + mes.substring(0, 1).toUpperCase() + mes.substring(1) + " " + ym.getYear();
            } catch (Exception ignored) {
            }

            Cell rightHeader = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
            rightHeader.add(new Paragraph("RECIBO N°: " + String.format("%06d", liquidacion.getIdLiquidacion()))
                    .setBold().setFontSize(11).setFontColor(COLOR_PRIMARIO).setMargin(0));
            rightHeader.add(new Paragraph(descPeriodo)
                    .setBold().setFontSize(10).setMarginTop(2));
            rightHeader.add(new Paragraph("Emisión: " + (liquidacion.getFechaLiquidacion() != null
                    ? liquidacion.getFechaLiquidacion().format(DATE_FORMATTER) : "N/D"))
                    .setFontSize(9).setFontColor(ColorConstants.DARK_GRAY));

            headerTable.addCell(leftHeader);
            headerTable.addCell(rightHeader);
            document.add(headerTable);

            document.add(new Paragraph().setMarginBottom(10));

            // 2. DATOS DEL EMPLEADO Y FESTIVAL
            Table empTable = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}));
            empTable.setWidth(UnitValue.createPercentValue(100));
            empTable.setBorder(new SolidBorder(COLOR_BORDE, 1));

            String cargo = "Empleado";
            if (personal instanceof Cocinero cocinero) {
                cargo = "Cocinero (" + (cocinero.getCategoria() != null ? cocinero.getCategoria().getNombre() : "General") + ")";
            } else if (personal instanceof Cajero cajero) {
                cargo = "Cajero (Turno " + cajero.getTurno() + ")";
            }

            String unidad = (liquidacion.getUnidadVenta() != null)
                    ? liquidacion.getUnidadVenta().getNombreComercial()
                    : "No asignada";

            String festival = (liquidacion.getFestival() != null)
                    ? liquidacion.getFestival().getNombreLimpio()
                    : "Festival General";

            agregarCeldaDato(empTable, "Apellido y Nombre:", personal.getApellido() + ", " + personal.getNombre(), 2);
            agregarCeldaDato(empTable, "DNI:", personal.getDni(), 1);
            agregarCeldaDato(empTable, "Fecha Ingreso:", (personal.getFechaIngreso() != null
                    ? personal.getFechaIngreso().format(DATE_FORMATTER) : "N/D"), 1);

            agregarCeldaDato(empTable, "Cargo / Rol:", cargo, 2);
            agregarCeldaDato(empTable, "Puesto / Unidad:", unidad, 2);

            agregarCeldaDato(empTable, "Festival:", festival, 4);

            document.add(empTable);
            document.add(new Paragraph().setMarginBottom(15));

            // 3. DETALLE DE HABERES
            Table conceptTable = new Table(UnitValue.createPercentArray(new float[]{45, 20, 15, 20}));
            conceptTable.setWidth(UnitValue.createPercentValue(100));
            conceptTable.setBorder(new SolidBorder(COLOR_BORDE, 1));

            conceptTable.addHeaderCell(crearCeldaHeader("Concepto"));
            conceptTable.addHeaderCell(crearCeldaHeader("Detalle / Base"));
            conceptTable.addHeaderCell(crearCeldaHeader("Factor / %"));
            conceptTable.addHeaderCell(crearCeldaHeader("Importe ($)"));

            // Básico
            conceptTable.addCell(crearCeldaTexto("Sueldo Básico"));
            conceptTable.addCell(crearCeldaTexto("Mensual asignado"));
            conceptTable.addCell(crearCeldaNumero("100%"));
            conceptTable.addCell(crearCeldaNumero(currencyFormat.format(liquidacion.getSueldoBase())));

            // Adicional
            conceptTable.addCell(crearCeldaTexto("Adicional remunerativo"));
            conceptTable.addCell(crearCeldaTexto(liquidacion.getDetalleAdicional() != null ? liquidacion.getDetalleAdicional() : "Sin adicional"));
            conceptTable.addCell(crearCeldaNumero("-"));
            conceptTable.addCell(crearCeldaNumero(currencyFormat.format(liquidacion.getAdicional())));

            // Ajuste por días
            conceptTable.addCell(crearCeldaTexto("Ajuste por días trabajados"));
            conceptTable.addCell(crearCeldaTexto(liquidacion.getDiasTrabajados() + " días computados"));
            conceptTable.addCell(crearCeldaNumero(liquidacion.getPorcentajeAplicado() + "%"));
            conceptTable.addCell(crearCeldaNumero(currencyFormat.format(liquidacion.getTotalACobrar())));

            document.add(conceptTable);
            document.add(new Paragraph().setMarginBottom(15));

            // 4. TOTAL NETO A COBRAR
            Table totalTable = new Table(UnitValue.createPercentArray(new float[]{60, 40}));
            totalTable.setWidth(UnitValue.createPercentValue(100));
            totalTable.setBorder(new SolidBorder(COLOR_BORDE, 1));

            Cell labelTotal = new Cell().setBackgroundColor(COLOR_FONDO_TH).setBorder(Border.NO_BORDER);
            labelTotal.add(new Paragraph("TOTAL NETO A PERCIBIR:")
                    .setBold().setFontSize(12).setTextAlignment(TextAlignment.RIGHT).setMargin(8));

            Cell valorTotal = new Cell().setBackgroundColor(COLOR_FONDO_TH).setBorder(Border.NO_BORDER);
            valorTotal.add(new Paragraph(currencyFormat.format(liquidacion.getTotalACobrar()))
                    .setBold().setFontSize(14).setFontColor(COLOR_PRIMARIO).setTextAlignment(TextAlignment.RIGHT).setMargin(8));

            totalTable.addCell(labelTotal);
            totalTable.addCell(valorTotal);
            document.add(totalTable);

            document.add(new Paragraph().setMarginBottom(35));

            // 5. SECCIÓN DE FIRMAS
            Paragraph legal = new Paragraph("Recibí de conformidad el importe neto de esta liquidación y copia del presente recibo.")
                    .setFontSize(8).setItalic().setFontColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER);
            document.add(legal);
            document.add(new Paragraph().setMarginBottom(40));

            Table firmasTable = new Table(UnitValue.createPercentArray(new float[]{45, 10, 45}));
            firmasTable.setWidth(UnitValue.createPercentValue(100));
            firmasTable.setBorder(Border.NO_BORDER);

            Cell firmaEmpleador = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER);
            firmaEmpleador.add(new Paragraph("____________________________________").setFontSize(9));
            firmaEmpleador.add(new Paragraph("Firma del Empleador / Responsable").setBold().setFontSize(9));

            Cell separador = new Cell().setBorder(Border.NO_BORDER);

            Cell firmaEmpleado = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER);
            firmaEmpleado.add(new Paragraph("____________________________________").setFontSize(9));
            firmaEmpleado.add(new Paragraph("Firma del Empleado (Conformidad)").setBold().setFontSize(9));

            firmasTable.addCell(firmaEmpleador);
            firmasTable.addCell(separador);
            firmasTable.addCell(firmaEmpleado);
            document.add(firmasTable);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar el recibo de sueldo en PDF", e);
        }
    }

    private void agregarCeldaDato(Table table, String etiqueta, String valor, int colspan) {
        Cell cell = new Cell(1, colspan);
        cell.setBorder(new SolidBorder(COLOR_BORDE, 0.5f));
        cell.setPadding(5);
        cell.add(new Paragraph(etiqueta).setFontSize(8).setBold().setFontColor(ColorConstants.DARK_GRAY).setMargin(0));
        cell.add(new Paragraph(valor != null ? valor : "-").setFontSize(9).setMargin(0));
        table.addCell(cell);
    }

    private Cell crearCeldaHeader(String texto) {
        return new Cell()
                .setBackgroundColor(COLOR_FONDO_TH)
                .setBorder(new SolidBorder(COLOR_BORDE, 0.5f))
                .setPadding(6)
                .setTextAlignment(TextAlignment.CENTER)
                .add(new Paragraph(texto).setBold().setFontSize(9).setFontColor(COLOR_PRIMARIO).setMargin(0));
    }

    private Cell crearCeldaTexto(String texto) {
        return new Cell()
                .setBorder(new SolidBorder(COLOR_BORDE, 0.5f))
                .setPadding(5)
                .add(new Paragraph(texto).setFontSize(9).setMargin(0));
    }

    private Cell crearCeldaNumero(String texto) {
        return new Cell()
                .setBorder(new SolidBorder(COLOR_BORDE, 0.5f))
                .setPadding(5)
                .setTextAlignment(TextAlignment.RIGHT)
                .add(new Paragraph(texto).setFontSize(9).setMargin(0));
    }
}

