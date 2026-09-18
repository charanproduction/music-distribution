package com.digital.service;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.digital.entity.User;
import com.digital.entity.UserRightsConsent;
import com.digital.util.ConsentTemplateLoader;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.lowagie.text.alignment.VerticalAlignment;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState;

import com.itextpdf.layout.Document;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsentPDFGenerator {
	private final ConsentTemplateLoader templateLoader;

	public byte[] generateRightsPDF(User user) {
	    try {
	        String template = Files.readString(Paths.get("src/main/resources/templates/legal/rights-consent-template.html"));
	        String body = Files.readString(Paths.get("src/main/resources/templates/legal/rights-consent-body.txt"));

	        String html = template
	                .replace("{{DATE}}", LocalDate.now().toString())
	                .replace("{{CHANNEL_NAME}}", user.getChannelName())
	                .replace("{{OWNER_NAME}}", user.getFullName())
	                .replace("{{EMAIL}}", user.getEmail())
	                .replace("{{BODY}}", body.replace("\n", "<br/>"));

	        ByteArrayOutputStream baos = new ByteArrayOutputStream();

	        ITextRenderer renderer = new ITextRenderer();
	        renderer.setDocumentFromString(html);
	        renderer.layout();
	        renderer.createPDF(baos);

	        return baos.toByteArray();

	    } catch (Exception e) {
	        throw new RuntimeException("Error generating Consent PDF", e);
	    }
	}
	
	public byte[] generateApprovedConsentPdf(UserRightsConsent consent) throws Exception {

	    String html = templateLoader.loadTemplate(consent.getUser(), consent.getFullName());

	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    PdfDocument pdfDoc = new PdfDocument(new PdfWriter(baos));

	    ConverterProperties props = new ConverterProperties();
	    HtmlConverter.convertToPdf(html, pdfDoc, props);

	    // ---- Watermark ----
	    PdfCanvas canvas = new PdfCanvas(pdfDoc.getFirstPage());
	    canvas.saveState()
	          .setExtGState(new PdfExtGState().setFillOpacity(0.12f))
	          .setColor(ColorConstants.GREEN, true)
	          .beginText()
	          .setFontAndSize(PdfFontFactory.createFont(), 90)
	          .setTextMatrix(100, 400)   // position
	          .showText("APPROVED")
	          .endText()
	          .restoreState();

	    // ---- Footer signature ----
	    Document doc = new Document(pdfDoc);

	    Paragraph signatureBlock = new Paragraph(
	            "Approved by: " + consent.getApprovedBy() + "\n" +
	            "Approved On: " + consent.getApprovedOn()
	    )
	        .setTextAlignment(TextAlignment.RIGHT)
	        .setFontSize(10);

	    // Fixed position at bottom-right of the last page
	    int lastPage = pdfDoc.getNumberOfPages();
	    signatureBlock.setFixedPosition(lastPage, 36, 20, 520);
	    doc.add(signatureBlock);

	    doc.close();
	    return baos.toByteArray();
	}

	
	
}

