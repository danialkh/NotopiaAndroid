package ir.notopia.android;


import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipManager {

    private static int BUFFER_SIZE = 2048;
    private static String TAG  = "zipGenerator";

    public static void zip(String[] files, String zipFile) throws IOException {
        BufferedInputStream origin = null;
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
        try {
            byte data[] = new byte[BUFFER_SIZE];

            for (int i = 0; i < files.length; i++) {
                FileInputStream fi = new FileInputStream(files[i]);
                origin = new BufferedInputStream(fi, BUFFER_SIZE);
                try {
                    ZipEntry entry = new ZipEntry(files[i].substring(files[i].lastIndexOf("/") + 1));
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                        out.write(data, 0, count);
                    }
                } finally {
                    origin.close();
                }
            }
        } finally {
            out.close();
        }
    }

    public static void unzip(String zipFile, String location) throws IOException {
        try {
            File f = new File(location);
            if (!f.isDirectory()) {
                f.mkdirs();
            }
            ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFile));
            try {
                ZipEntry ze = null;
                while ((ze = zin.getNextEntry()) != null) {
                    String path = location + ze.getName();

                    if (ze.isDirectory()) {
                        File unzipFile = new File(path);
                        if (!unzipFile.isDirectory()) {
                            unzipFile.mkdirs();
                        }
                    } else {

                        String DocumentPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "";

                        String directoryAttach = "/Notopia/attachment/";
                        String directoryDoc = "/Notopia/Scans/";

                        String attachFolder = DocumentPath + directoryAttach;
                        String scanFolder = DocumentPath + directoryDoc;


                        Log.d(TAG,"unzip:" + "filePath: " + path);

                        String newPath;
                        if(path.contains("attack-")){
                            String[] splitPath = path.split("attack-");
                            newPath = attachFolder + "attack-" + splitPath[1];

                            File makeFolderAttach = new File(attachFolder);
                            if (!makeFolderAttach.isDirectory()) {
                                makeFolderAttach.mkdirs();
                            }


                        }
                        else if(path.contains("DOC-")){
                            String[] splitPath = path.split("DOC-");
                            newPath = scanFolder + "DOC-" + splitPath[1];

                            File makeFolderScans = new File(scanFolder);
                            if (!makeFolderScans.isDirectory()) {
                                makeFolderScans.mkdirs();
                            }

                        }
                        else {
                            // for json file just unzip in import folder and no change is needed
                            newPath = path;
                        }

                        Log.d(TAG,"unzip:" + "newfilePath: " + newPath);


                        File searchFile =new File(newPath);
                        if(searchFile.exists()) {
                            searchFile.delete();
                        }

                        FileOutputStream fout = new FileOutputStream(newPath, false);
                        try {

                            BufferedOutputStream bufout = new BufferedOutputStream(fout);
                            byte[] buffer = new byte[BUFFER_SIZE];
                            int read = 0;
                            while ((read = zin.read(buffer)) != -1) {
                                bufout.write(buffer, 0, read);
                            }

                            zin.closeEntry();
                            bufout.close();

                        } finally {
                            fout.close();
                        }
                    }
                }
            } finally {
                zin.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Unzip exception", e);
        }
    }

}