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
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableMap;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Date;
import java.util.Locale;

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
      saveImageAndModifyProperties(base64ImageData, properties, promise);
    } catch (Exception e) {
      promise.reject("E_IMAGE_PROCESSING", e);
    }
  }

  @ReactMethod
  public void saveImageWithProperties(String base64ImageData, ReadableMap properties, Promise promise) {
    try {
      Map<String, String> mappedProperties = new HashMap<>();

      if (
        properties.hasKey("GPSLatitude") &&
        properties.hasKey("GPSLongitude") &&
        properties.hasKey("GPSAltitude")
      ) {
        double latitude = Double.parseDouble(properties.getString("GPSLatitude"));
        double longitude = Double.parseDouble(properties.getString("GPSLongitude"));
        double altitude = Double.parseDouble(properties.getString("GPSAltitude"));

        mappedProperties.put(ExifInterface.TAG_GPS_LATITUDE, convertToDMS(latitude));
        mappedProperties.put(ExifInterface.TAG_GPS_LATITUDE_REF, latitude >= 0 ? "N" : "S");
        mappedProperties.put(ExifInterface.TAG_GPS_LONGITUDE, convertToDMS(longitude));
        mappedProperties.put(ExifInterface.TAG_GPS_LONGITUDE_REF, longitude >= 0 ? "E" : "W");
        mappedProperties.put(ExifInterface.TAG_GPS_ALTITUDE, convertToRational(altitude));
        mappedProperties.put(ExifInterface.TAG_GPS_ALTITUDE_REF, altitude >= 0 ? "0" : "1");
      }

      if (properties.hasKey("UserComment")) {
        mappedProperties.put(ExifInterface.TAG_USER_COMMENT, properties.getString("UserComment"));
      }

      if (properties.hasKey("DateTime")) {
        mappedProperties.put(ExifInterface.TAG_DATETIME, properties.getString("DateTime"));
      }

      if (properties.hasKey("DateTimeOriginal")) {
        mappedProperties.put(ExifInterface.TAG_DATETIME_ORIGINAL, properties.getString("DateTimeOriginal"));
      }

      if (properties.hasKey("DateTimeDigitized")) {
        mappedProperties.put(ExifInterface.TAG_DATETIME_DIGITIZED, properties.getString("DateTimeDigitized"));
      }

      saveImageAndModifyProperties(base64ImageData, mappedProperties, promise);
    } catch (Exception e) {
      promise.reject("E_IMAGE_PROCESSING", e);
    }
  }

  private void saveImageAndModifyProperties(String base64ImageData, Map<String, String> properties, Promise promise) throws IOException {
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
      String[] proj = {MediaStore.Images.Media.DATA};
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

  private String convertToRational(double value) {
    return (int) value + "/1";
  }

  private String convertToDMS(double coordinate) {
    coordinate = Math.abs(coordinate);
    int degrees = (int) coordinate;
    coordinate = (coordinate - degrees) * 60;
    int minutes = (int) coordinate;
    coordinate = (coordinate - minutes) * 60;
    int seconds = (int) (coordinate * 1000);

    return degrees + "/1," + minutes + "/1," + seconds + "/1000";
  }
}

