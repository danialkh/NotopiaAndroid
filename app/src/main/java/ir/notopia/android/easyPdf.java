package ir.notopia.android;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;

import androidx.core.content.FileProvider;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.FontSelector;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ir.notopia.android.database.AppRepository;
import ir.notopia.android.database.entity.AttachEntity;
import ir.notopia.android.database.entity.ScanEntity;

/**
 * Created by hackro on 24/11/15.
 */
public class easyPdf {

    private Activity activity;

    private String directory = "/Notopia/Pdf/";

    private String PdfFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + directory;

    public easyPdf(Activity activity) {

        this.activity = activity;
        activity.getApplicationContext().getExternalFilesDir("notopia");
        CreateDirectory();

    }

//    private int CREATE_FILE = 1;
//
//    private void createFile() {
//        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT).apply
//    }


    // Request code

//
//    private fun createFile(pickerInitialUri: Uri) {
//        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
//            addCategory(Intent.CATEGORY_OPENABLE)
//            type = "Type of file"
//            putExtra(Intent.EXTRA_TITLE, "Name of File")
//
//            // Optionally, specify a URI for the directory that should be  opened in
//
//            // the system file picker before your app creates the document.
//            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
//        }
//        startActivityForResult(intent, CREATE_FILE)
//    }


    private void CreateDirectory() {

        File dir = new File(PdfFolder);
        if (!dir.exists()) {
            try {
                dir.mkdirs();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public Boolean write(String fname, ArrayList<ScanEntity> mScans) {

        try {
            String fpath = fname + ".pdf";
            File file = new File(PdfFolder,fpath);

            if (!file.exists()) {
                file.createNewFile();
            }

            // use tahoma for english
            BaseFont baseFont = BaseFont.createFont("assets/fonts/Tahoma.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font tahomaFont = new Font(baseFont, 14, Font.NORMAL, BaseColor.BLACK);

            // use B yekan for farsi
            BaseFont baseFont2 = BaseFont.createFont("assets/fonts/B_Yekan.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font englishFont = new Font(baseFont2, 14, Font.NORMAL, BaseColor.BLACK);


            // use tahoma for english
            Font tahomaFontTitle = new Font(baseFont, 18, Font.BOLD, BaseColor.BLACK);

            // use B yekan for farsi
            Font englishFontTitle = new Font(baseFont2, 18, Font.BOLD, BaseColor.BLACK);





            Document document = new Document();

            PdfWriter pdfWriter = PdfWriter.getInstance(document,
                    new FileOutputStream(file.getAbsoluteFile()));
            document.open();

            FontSelector fontSelector_text = new FontSelector();
            fontSelector_text.addFont(tahomaFont);
            fontSelector_text.addFont(englishFont);



            FontSelector fontSelector_title = new FontSelector();
            fontSelector_title.addFont(tahomaFontTitle);
            fontSelector_title.addFont(englishFontTitle);



            for(int i = 0;i < mScans.size();i++) {


                PdfPTable table1 = new PdfPTable(1);
                table1.setWidthPercentage(100);
                table1.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);

                PdfPCell pdfCell_Title = new PdfPCell();
                pdfCell_Title.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
                pdfCell_Title.setBorder(0);
                pdfCell_Title.setPhrase(fontSelector_title.process(mScans.get(i).getTitle()));
                pdfCell_Title.setLeading(28, 0);




                PdfPCell pdfCell_Text = new PdfPCell();
                pdfCell_Text.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
                pdfCell_Text.setBorder(0);
                pdfCell_Text.setPhrase(fontSelector_text.process(mScans.get(i).getText()));
                pdfCell_Text.setLeading(28, 0);

                //attachment icons

                String attachText = "فایل های پیوست : ";


                AppRepository mRepository = AppRepository.getInstance(activity);

                List<AttachEntity> mAttachs = mRepository.getAllAttachsFromNoton(mScans.get(i));

                for(int a = 0;a < mAttachs.size();a++){

                    switch (mAttachs.get(a).getType()) {
                        case "image/jpeg":
                            attachText += " تصویر ";
                            break;
                        case "audio/mpeg":
                            attachText += " صوت ";
                            break;
                        case "video/mp4":
                            attachText += " ویدیو ";
                            break;
                        default:
                            attachText += " سند ";
                            break;
                    }

                    if((a + 1) != mAttachs.size()) {
                        attachText += "،";
                    }
                }


                if(mAttachs.size() == 0){
                    attachText += "ندارد";
                }


                PdfPCell pdfCell_attach = new PdfPCell();
                pdfCell_attach.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
                pdfCell_attach.setBorder(0);
                pdfCell_attach.setPhrase(fontSelector_text.process(attachText));
                pdfCell_attach.setLeading(28, 0);



                LineSeparator lineSeparator = new LineSeparator();
                PdfPCell pdfCell_line = new PdfPCell();
                pdfCell_line.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
                pdfCell_line.setBorder(0);
                pdfCell_line.addElement(lineSeparator);
                pdfCell_line.setPadding(10f);


                table1.addCell(pdfCell_Title);
                table1.addCell(pdfCell_Text);
                table1.addCell(pdfCell_attach);
                table1.addCell(pdfCell_line);



                File imgFile = new File(mScans.get(i).getImage());

                if(imgFile.exists()) {

                    Uri uri = FileProvider.getUriForFile(activity, activity.getApplicationContext().getPackageName() + ".myprovider", imgFile);

                    InputStream in = activity.getContentResolver().openInputStream(uri);

                    Bitmap bmp = BitmapFactory.decodeStream(in);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();

                    bmp.compress(Bitmap.CompressFormat.JPEG, 50, stream);

                    byte[] byteArray = stream.toByteArray();
                    // PdfImage img = new PdfImage(arg0, arg1, arg2)

                    // Converting byte array into image Image img =
                    Image img = Image.getInstance(byteArray); // img.scalePercent(50);
                    img.setAlignment(Image.TEXTWRAP);
//                    img.scaleAbsolute(200f, 50f);
                    //img.setAbsolutePosition(imageStartX, imageStartY); // Adding Image


                    PdfPCell cellImage = new PdfPCell(img, true);
                    cellImage.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
                    cellImage.setBorder(0);


                    table1.addCell(cellImage); //add image cell in table
                }



                document.add(table1);

            }

            // columnText usage

//            ColumnText ct = new ColumnText(pdfWriter.getDirectContent());
//            ct.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
//            ct.setSimpleColumn(0, 0, 500, 800, 24, Element.ALIGN_RIGHT);
//
//
//
//            String matn = "این متن English نیست";
//
//            Chunk chunk = new Chunk(matn, tahomaFont);
//            ct.addElement(chunk);
//            ct.go();




            document.close();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }


}