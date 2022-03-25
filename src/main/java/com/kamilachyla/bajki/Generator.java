package com.kamilachyla.bajki;

import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.AreaBreakType;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;


/**
 * Generator generates pdf using:
 *  <ul>
 *      <li>path resolver given to {@link Main#Main(Path)} constructor</li>
 *      <li>Meta instance with book metadata and pages given to {@link Main#run()} method</li>
 *      <li>output filename supplier given to {@link Main#run()} method</li>
 *  </ul>
 *
 */
public class Generator {

    public static final DeviceRgb BLUE = new DeviceRgb(35, 206, 235);
    public static final DeviceRgb YELLOW = new DeviceRgb(255, 218, 185);
    private static final String BOOK_SERIES_NAME = "Bajeczki dla Eweczki";
    public static final int TITLE_FONT_SIZE = 24;
    public static final int AUTHOR_FONT_SIZE = 18;
    public static final int LINE_MARGIN_TOP = 10;
    public static final int LINE_MARGIN_BOTTOM = 10;
    public static final int FOOTER_MARGIN_TOP = 10;
    public static final int TITLE_MARGIN_BOTTOM = 50;
    final int PAGE_FONT_SIZE = 18;
    public static final PageSize PAGE_SIZE = PageSize.A5;

    private static Logger logger = LoggerFactory.getLogger("Generator");
    private final Function<String, Path> pathResolver;
    /** FontData keeps font alisa and .ttf file path for use with @{link {@link FontProgramFactory#registerFont(String, String)}}*/
    static record FontData(String alias, String path){}
    private static PdfFont myBoldFont = registerFont(new FontData("chamfont", "src/main/resources/ChamsBold.ttf"));

    /**
     * Creates generator which resolves filenames found during generation using given pathResolver
     * @param pathResolver returns Path from a string
     * */
    public Generator(Function<String, Path> pathResolver) {
        this.pathResolver = pathResolver;
    }

    private static PdfFont registerFont(FontData data) {
        FontProgramFactory.registerFont(data.path(), data.alias());
        logger.info("Registering font {}", data);
        try {
            return PdfFontFactory.createRegisteredFont(data.alias(), PdfEncodings.CP1250);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    /**
     * Generates a .pdf file  from @{link Meta} meta parameter. Name of result file is provided by @{param outputFileNameSupplier}
     * @param  book data to render in a form of pdf
     * @param outputFileName  name of output .pdf file */
    public  Optional<Path> generate(Book book, String outputFileName) throws FileNotFoundException {
        logger.info("Generate {}", book.title());
        final var filename = pathResolver.apply(outputFileName)
                .toAbsolutePath().normalize().toString();
        var writer = new PdfWriter(filename);
        var pdf = new PdfDocument(writer);
        Document doc = new Document(pdf, PAGE_SIZE);
        
        createTitlePage(doc, book);
        for (BookPage p : book.pages()) {
            generateNextPage(doc, p);
        }
        
        pdf.close();
        return Optional.of(Path.of(filename));

    }

    private void generateNextPage(Document doc, BookPage p) {
        logger.info("generateNextPage {}", p.number());

        doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));

        doc.add(new Paragraph(
                new Text(p.text())
                        .setFontSize(PAGE_FONT_SIZE)
                        .setFont(myBoldFont))
        );

        SolidLine line = new SolidLine(0);
        LineSeparator ls = new LineSeparator(line);
        ls.setWidth(UnitValue.createPercentValue(100));
        ls.setMarginTop(LINE_MARGIN_TOP);
        ls.setMarginBottom(LINE_MARGIN_BOTTOM);

        doc.add(ls);

        try {
            doc.add(getImage(doc, p.imagePath()));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        addFooter(doc, Optional.of(String.valueOf(p.number())));
    }

    private void addFooter(Document doc, Optional<String> text) {
        Table table = new Table(text.isPresent() ? 2 : 1);
        table.setWidth(UnitValue.createPercentValue(100));
        table.addCell(new Div().add(
                    new Paragraph(new Text(BOOK_SERIES_NAME)))
                .setTextAlignment(TextAlignment.CENTER)
                .setBackgroundColor(BLUE));
        text.ifPresent(t -> {
            table.addCell(new Div().add(
                            new Paragraph(new Text(t))).setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(YELLOW));
        });
        table.setBackgroundColor(BLUE);
        removeBorders(table);
        table.setMarginTop(FOOTER_MARGIN_TOP);

        doc.add(table);
    }

    private void removeBorders(Table table) {
        for (int i = 0; i < table.getNumberOfRows(); i++) {
            for (int j = 0; j < table.getNumberOfColumns(); j++) {
                table.getCell(i, j).setBorder(Border.NO_BORDER);
            }
        }
    }



    private void createTitlePage(Document doc, Book book) {
        logger.info("Generating title page for {}", book.title());
        doc.add(new Paragraph(
                new Text(book.author())
                        .setFontSize(AUTHOR_FONT_SIZE)
                        .setFont(myBoldFont)));


        doc.add(new Paragraph(book.title())
                .setFontSize(TITLE_FONT_SIZE).setFont(myBoldFont)
                .setFontColor(new DeviceRgb(30, 50, 200))
                .setMarginBottom(TITLE_MARGIN_BOTTOM));


        try {
            doc.add(getImage(doc, book.titleImagePath()));
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        addFooter(doc, Optional.empty());
        logger.info("createTitlePage for {} complete.", book.title());
    }

    private Image getImage(Document doc, String imagePath) throws MalformedURLException {
        final var filename = pathResolver.apply(imagePath).normalize().toString();
        logger.info("Resolved path for {}: {}", imagePath, filename);
        final var width = doc.getPageEffectiveArea(PAGE_SIZE).getWidth();
        return new Image(ImageDataFactory.create(filename)).setWidth(width);
    }
}
