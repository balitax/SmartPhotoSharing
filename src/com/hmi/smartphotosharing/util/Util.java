package com.hmi.smartphotosharing.util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.json.Group;
import com.hmi.smartphotosharing.json.News;
import com.hmi.smartphotosharing.json.Photo;
import com.hmi.smartphotosharing.json.Subscription;
import com.hmi.smartphotosharing.json.User;

public class Util {

	private static String APP_NAME = "Picalilly";
	
	// JSON response statuses
	public static final int STATUS_OK 			= 200;
	public static final int STATUS_LOGIN 		= 304;
	public static final int STATUS_DENIED 		= 403;
	public static final int STATUS_404 			= 404;
	public static final int STATUS_ERROR 		= 500;
	
	// Nav bar actions
    public static final int ACTION_ARCHIVE 		= 1;
    public static final int ACTION_CAMERA 		= 2;
    public static final int ACTION_SETTINGS 	= 3;
    public static final int ACTION_FAVOURITE 	= 4;
    public static final int ACTION_MAP 			= 5;
    public static final int ACTION_FRIENDS		= 6;
    
    // Google services API key
    public static final String API_KEY = "AIzaSyCKN-AGNHA7ZYTPQ_-IXZUHFGT8UlXlZig";
    // Google static maps
    public static final String API_KEY_MAPS ="0LgN0zWElNFx2cMBe0vH1UtWShWq1VlUPUeUb9w";
    
    public static final String SERVER 			= "http://picalilly.student.utwente.nl/";
    public static final String API 				= SERVER + "api.php?";
    public static final String THUMB_URL 		= SERVER + "thumb.php?src=";
    public static final String THUMB_SIZE 		= "100";
    public static final String THUMB_CONFIG 	= "&w=" + THUMB_SIZE + "&h=" + THUMB_SIZE;
    
    public static final String IMG_DB 			= SERVER + "imgdb/";
    public static final String USER_DB 			= SERVER + "profilepicdb/";
    public static final String GROUP_DB 		= SERVER + "logodb/";
    public static final String REGISTER_URL 	= SERVER + "signup.php";
    
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    
    public static final String DUMMY_PHOTO = SERVER + "thumb.php?src=logodb/default.jpg&w=100&h=100";

	public static String URL_MESSAGE = "com.hmi.smartphotosharing.URL_MESSAGE";
	
	public static int[] colors = new int[]{Color.BLUE, Color.CYAN, 
											Color.GREEN, Color.MAGENTA, 
											Color.RED, Color.YELLOW};
	
	public static float[] hue = new float[]{
										BitmapDescriptorFactory.HUE_BLUE, BitmapDescriptorFactory.HUE_CYAN,
										BitmapDescriptorFactory.HUE_GREEN, BitmapDescriptorFactory.HUE_MAGENTA,
										BitmapDescriptorFactory.HUE_RED, BitmapDescriptorFactory.HUE_YELLOW};
	
	public static String getThumbUrl(String db, String file) {
		return THUMB_URL + db + file + THUMB_CONFIG;
	}
	
	public static String getThumbUrl(Photo p) {
		return THUMB_URL + IMG_DB + p.name + THUMB_CONFIG;
	}
	
	public static String getThumbUrl(User u) {
		return THUMB_URL + USER_DB + u.picture + THUMB_CONFIG;
	}
	
	public static String getThumbUrl(Group g) {
		return THUMB_URL + GROUP_DB + g.logo + THUMB_CONFIG;
	}

	public static String getThumbUrl(Subscription subscription) {
		return THUMB_URL + USER_DB + subscription.picture + THUMB_CONFIG;
	}
	
    public static String getUrl(Context c, int resource) {
    	return API + c.getResources().getString(resource);
    }
    
    
    public static int getColor(int i) {
    	return colors[i%colors.length];
    }
    
    public static float getHue(int i) {
    	return hue[i%hue.length];
    }
	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	public static boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private static boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}

	/**
	 * This method shows a pop-up to the user where he is asked
	 * to enable his GPS receiver. If the user confirms, he is
	 * directed to the GPS settings screen.
	 * @param c The application context
	 */
	public static void createGpsDisabledAlert(final Context c){
		AlertDialog.Builder builder = new AlertDialog.Builder(c);
		builder.setMessage("Your GPS is disabled! Would you like to enable it?")
		     .setCancelable(false)
		     .setPositiveButton("Enable GPS",
		          new DialogInterface.OnClickListener(){
		          public void onClick(DialogInterface dialog, int id){
		               showGpsOptions(c);
		          }
		     });
		     builder.setNegativeButton("Do nothing",
		          new DialogInterface.OnClickListener(){
		          public void onClick(DialogInterface dialog, int id){
		               dialog.cancel();
		          }
		     });
		AlertDialog alert = builder.create();
		alert.show();
	}  
	
	/**
	 * Creates a simple dialog with an OK button for the given string message.
	 * @param c The application context
	 * @param s The message that should be displayed
	 */
	public static void createSimpleDialog(Context c, String s) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(c);
		builder.setMessage(s)
		     .setCancelable(false)
		     .setNeutralButton("Ok", null);
		AlertDialog alert = builder.create();
		alert.show();
		
	}

	
	/**
	 * Method to redirect the user to the GPS settings screen.
	 * @param c The application context
	 */
	private static void showGpsOptions(Context c){
		Intent gpsOptionsIntent = new Intent(
				android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		c.startActivity(gpsOptionsIntent);
	}
	
	/**
	 * Sets the background of a view to a light green color.
	 * @param c The application context
	 */
	public static void setSelectedBackground(Context c, View v) {
		v.setBackgroundColor(c.getResources().getColor(R.color.button_hilight));
	}
	
	/** Create a file Uri for saving an image or video */
	public static Uri getOutputMediaFileUri(int type){
	      return Uri.fromFile(getOutputMediaFile(type));
	}
	
	public static File getOutputMediaFile(int type){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.

	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), APP_NAME);
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d(APP_NAME, "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "IMG_"+ timeStamp + ".jpg");
	    } else if(type == MEDIA_TYPE_VIDEO) {
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "VID_"+ timeStamp + ".mp4");
	    } else {
	        return null;
	    }

	    return mediaFile;
	}

	public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight, int rotation) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(path, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    Bitmap tmp = BitmapFactory.decodeFile(path, options);
	    
	    Matrix mat = new Matrix();
	    if (rotation != 0)
	    	mat.postRotate(rotation);
	    
        return Bitmap.createBitmap(tmp, 0, 0, tmp.getWidth(), tmp.getHeight(), mat, true);
	}
	
	public static int getRotationDegrees(String path) {

	    // Calculate rotation
	    int exifOrientation = 0;
	    
	    try {
			ExifInterface exif = new ExifInterface(path);
			exifOrientation = Integer.parseInt(exif.getAttribute(ExifInterface.TAG_ORIENTATION));

		} catch (IOException e) {
			e.printStackTrace();
		}
		
	    int rotation = 0;
	    switch (exifOrientation) {
		    case (ExifInterface.ORIENTATION_ROTATE_90):
		    	rotation = 90;
		    	break;
		    case (ExifInterface.ORIENTATION_ROTATE_180):
		    	rotation = 180;
		    	break;
		    case (ExifInterface.ORIENTATION_ROTATE_270):
		    	rotation = 270;
		    	break;
		    default:	
	    }

	    return rotation;
	}
	
	public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	        if (width > height) {
	            inSampleSize = Math.round((float)height / (float)reqHeight);
	        } else {
	            inSampleSize = Math.round((float)width / (float)reqWidth);
	        }
	    }
	    return inSampleSize;
	}
	
	public static String getRealPathFromURI(Context mContext, Uri contentUri) {
	    String[] proj = { MediaStore.Images.Media.DATA };
	    CursorLoader loader = new CursorLoader(mContext, contentUri, proj, null, null, null);
	    Cursor cursor = loader.loadInBackground();
	    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	    cursor.moveToFirst();
	    return cursor.getString(column_index);
	}


	public static void showSubHeader(TextView title, TextView subtitle) {

        subtitle.setVisibility(TextView.VISIBLE);
        LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.RIGHT_OF, R.id.app_icon);
        title.setLayoutParams(lp);
		
	}


}
