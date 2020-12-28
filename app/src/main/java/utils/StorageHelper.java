package utils;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import app.XplocityApplication;

public class StorageHelper {

    public static Uri saveImage(Bitmap image) {
        File imagesFolder = new File(XplocityApplication.getAppContext().getCacheDir(), "images");
        Uri uri = null;
        try {
            imagesFolder.mkdirs();
            File file = new File(imagesFolder, "shared_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(XplocityApplication.getAppContext(), "com.xplocity.fileprovider", file);

        } catch (IOException e) {
            Log.d("SHARE_IMG", "IOException while trying to write file for sharing: " + e.getMessage());
        }
        return uri;
    }

}
