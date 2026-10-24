package com.concesionaria.venta.venta.service;

import com.concesionaria.common.security.UsuarioActual;
import com.concesionaria.venta.cliente.ClienteClient;
import com.concesionaria.venta.cliente.dto.ClienteRemoto;
import com.concesionaria.venta.config.VentasProperties;
import com.concesionaria.venta.tasacion.TasacionTradeIn;
import com.concesionaria.venta.vehiculo.VehiculoClient;
import com.concesionaria.venta.vehiculo.dto.VehiculoRemoto;
import com.concesionaria.venta.venta.Venta;
import com.concesionaria.venta.venta.enums.EstadoVenta;
import com.concesionaria.venta.venta.enums.FormaPago;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * RF-27: contrato / comprobante de la venta en PDF. Los datos del cliente y del vehículo se
 * consultan al momento de generarlo (por si se corrigió algo después de la venta).
 */
@Service
@RequiredArgsConstructor
public class ComprobantePdfService {

    private static final Font TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    private static final Font SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    private static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font NEGRITA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font CHICA = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.DARK_GRAY);
    private static final Font ANULADA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.RED);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color GRIS = new Color(235, 235, 235);

    private final VentaService ventaService;
    private final ClienteClient clienteClient;
    private final VehiculoClient vehiculoClient;
    private final VentasProperties propiedades;

    public record Comprobante(String nombreArchivo, byte[] contenido) {
    }

    public Comprobante generar(Long ventaId, UsuarioActual usuario) {
        Venta venta = ventaService.buscarParaComprobante(ventaId, usuario);
        ClienteRemoto cliente = clienteClient.obtener(venta.getClienteId());
        VehiculoRemoto vehiculo = vehiculoClient.obtener(venta.getVehiculoId());

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(doc, salida);
        doc.addTitle("Comprobante de venta " + venta.getNumeroComprobante());
        doc.open();

        encabezado(doc, venta);
        seccion(doc, "Comprador");
        tabla(doc,
                "Nombre", cliente.nombreCompleto(),
                "Documento", cliente.tipoDocumento() + " " + cliente.numeroDocumento(),
                "Domicilio", cliente.direccion() != null ? cliente.direccion().toString() : "-",
                "Email / Teléfono", valor(cliente.email()) + " / " + valor(cliente.telefono()));

        seccion(doc, "Vehículo");
        tabla(doc,
                "Marca y modelo", vehiculo.marca() + " " + vehiculo.modelo(),
                "Año / Color", vehiculo.anio() + " / " + vehiculo.color(),
                "VIN", vehiculo.vin(),
                "Condición / Km", vehiculo.condicion() + " / " + vehiculo.kilometraje() + " km");

        TasacionTradeIn tasacion = venta.getTasacion();
        if (tasacion != null) {
            seccion(doc, "Vehículo entregado como parte de pago");
            tabla(doc,
                    "Modelo / Año", tasacion.getModeloDescripcion() + " " + tasacion.getAnio(),
                    "VIN", tasacion.getVin(),
                    "Km", tasacion.getKilometraje() + " km",
                    "Valor tasado", pesos(tasacion.getValorTasado()));
        }

        seccion(doc, "Detalle de la operación");
        PdfPTable montos = tablaBase();
        fila(montos, "Precio del vehículo", pesos(venta.getPrecioVehiculo()), false);
        if (venta.getDescuento().signum() > 0) {
            fila(montos, "Descuento", "- " + pesos(venta.getDescuento()), false);
        }
        fila(montos, "Precio final", pesos(venta.getPrecioFinal()), true);
        if (venta.getValorTradeIn().signum() > 0) {
            fila(montos, "Trade-in", "- " + pesos(venta.getValorTradeIn()), false);
        }
        if (venta.getMontoAnticipoReserva().signum() > 0) {
            fila(montos, "Seña de la reserva", "- " + pesos(venta.getMontoAnticipoReserva()), false);
        }
        fila(montos, "Saldo a pagar", pesos(venta.getSaldoAPagar()), true);
        doc.add(montos);

        seccion(doc, "Forma de pago");
        if (venta.getFormaPago() == FormaPago.FINANCIADA) {
            BigDecimal entrega = venta.getSaldoAPagar().subtract(venta.getMontoFinanciado());
            tabla(doc,
                    "Forma de pago", "Financiada - plan " + venta.getPlanNombre(),
                    "Entrega en efectivo", pesos(entrega),
                    "Monto financiado", pesos(venta.getMontoFinanciado()),
                    "Cuotas", venta.getCantidadCuotas() + " cuotas de " + pesos(venta.getValorCuota()));
        } else {
            tabla(doc, "Forma de pago", "Contado", "Importe", pesos(venta.getSaldoAPagar()));
        }

        Paragraph vendedor = new Paragraph("Vendedor: " + venta.getVendedorNombre(), NORMAL);
        vendedor.setSpacingBefore(14);
        doc.add(vendedor);

        firmas(doc);
        Paragraph pie = new Paragraph("Documento generado el " + java.time.LocalDateTime.now().format(FECHA)
                + ". El vehículo se entrega en el estado en que se encuentra, revisado por el comprador.", CHICA);
        pie.setSpacingBefore(20);
        doc.add(pie);
        doc.close();

        return new Comprobante("comprobante-" + venta.getNumeroComprobante() + ".pdf", salida.toByteArray());
    }

    private void encabezado(Document doc, Venta venta) {
        var empresa = propiedades.empresa();
        doc.add(new Paragraph(empresa.nombre(), TITULO));
        doc.add(new Paragraph("CUIT " + empresa.cuit() + " - " + empresa.direccion() + " - Tel. " + empresa.telefono(), CHICA));

        Paragraph titulo = new Paragraph("COMPROBANTE DE VENTA N° " + venta.getNumeroComprobante(), SUBTITULO);
        titulo.setSpacingBefore(16);
        doc.add(titulo);
        doc.add(new Paragraph("Fecha: " + venta.getFechaVenta().format(FECHA), NORMAL));
        if (venta.getEstado() == EstadoVenta.ANULADA) {
            Paragraph anulada = new Paragraph("ANULADA el " + venta.getFechaAnulacion().format(FECHA)
                    + ": " + venta.getMotivoAnulacion(), ANULADA);
            anulada.setSpacingBefore(6);
            doc.add(anulada);
        }
    }

    private static void seccion(Document doc, String titulo) {
        Paragraph p = new Paragraph(titulo, SUBTITULO);
        p.setSpacingBefore(14);
        p.setSpacingAfter(4);
        doc.add(p);
    }

    /** Pares etiqueta / valor. */
    private static void tabla(Document doc, String... pares) {
        PdfPTable tabla = tablaBase();
        for (int i = 0; i < pares.length; i += 2) {
            fila(tabla, pares[i], pares[i + 1], false);
        }
        doc.add(tabla);
    }

    private static PdfPTable tablaBase() {
        PdfPTable tabla = new PdfPTable(new float[]{35, 65});
        tabla.setWidthPercentage(100);
        return tabla;
    }

    private static void fila(PdfPTable tabla, String etiqueta, String valor, boolean destacar) {
        PdfPCell izq = new PdfPCell(new Phrase(etiqueta, NEGRITA));
        izq.setBackgroundColor(GRIS);
        izq.setPadding(5);
        PdfPCell der = new PdfPCell(new Phrase(valor, destacar ? NEGRITA : NORMAL));
        der.setPadding(5);
        if (destacar) {
            der.setHorizontalAlignment(Element.ALIGN_LEFT);
        }
        tabla.addCell(izq);
        tabla.addCell(der);
    }

    private static void firmas(Document doc) {
        PdfPTable firmas = new PdfPTable(2);
        firmas.setWidthPercentage(100);
        firmas.setSpacingBefore(50);
        for (String quien : new String[]{"Firma del comprador", "Firma y sello de la concesionaria"}) {
            PdfPCell celda = new PdfPCell();
            celda.setBorder(PdfPCell.NO_BORDER);
            celda.setHorizontalAlignment(Element.ALIGN_CENTER);
            Paragraph p = new Paragraph();
            p.add(new Chunk("______________________________\n", NORMAL));
            p.add(new Chunk(quien, CHICA));
            p.setAlignment(Element.ALIGN_CENTER);
            celda.addElement(p);
            firmas.addCell(celda);
        }
        doc.add(firmas);
    }

    private static String pesos(BigDecimal monto) {
        NumberFormat formato = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"));
        return formato.format(monto);
    }

    private static String valor(String texto) {
        return texto != null ? texto : "-";
    }
}
