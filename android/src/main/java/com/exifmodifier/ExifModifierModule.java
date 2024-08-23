package com.exifmodifier;

import android.provider.MediaStore;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import androidx.exifinterface.media.ExifInterface;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.HashMap;

public class ExifModifierModule extends ReactContextBaseJavaModule {
    public ExifModifierModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "ExifModifier";
    }

    @ReactMethod
    public void saveImageWithUserComment(String base64ImageData, String userComment, Promise promise) {
        try {
            Map<String, String> properties = new HashMap<>();

            properties.put(ExifInterface.TAG_USER_COMMENT, userComment);
            saveImageWithProperties(base64ImageData, properties, promise);
        } catch (Exception e) {
            promise.reject("E_IMAGE_PROCESSING", e);
        }
    }

    @ReactMethod
    public void saveImageWithProperties(String base64ImageData, Map<String, String> properties, Promise promise) throws IOException {
        Context context = getReactApplicationContext();

        // Decode the base64 string to a bitmap
        byte[] decodedString = Base64.decode(base64ImageData, Base64.DEFAULT);
        Bitmap image = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        // Prepare ContentValues to create a new media file
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        // Insert the new file to the media store
        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            promise.reject("E_FILE_CREATION", "Failed to create new MediaStore entry.");
            return;
        }

        try (FileOutputStream fos = (FileOutputStream) context.getContentResolver().openOutputStream(uri)) {
            image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        }

        // Modify the EXIF data
        String filePath = getRealPathFromURI(context, uri);
        ExifInterface exifInterface = new ExifInterface(filePath);
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            exifInterface.setAttribute(entry.getKey(), entry.getValue());
        }
        exifInterface.saveAttributes();

        promise.resolve(uri.toString());
    }

    private String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;

        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

            cursor.moveToFirst();

            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
