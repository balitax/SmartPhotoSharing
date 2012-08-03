package com.hmi.smartphotosharing.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;

public class Util {

	// JSON response statuses
	public static final int STATUS_OK = 200;
	public static final int STATUS_LOGIN = 304;
	public static final int STATUS_DENIED = 403;
	public static final int STATUS_404 = 404;
	public static final int STATUS_ERROR = 500;
	
	// Nav bar actions
    public static final int ACTION_ARCHIVE = 1;
    public static final int ACTION_CAMERA = 2;
    public static final int ACTION_SETTINGS = 3;
    public static final int ACTION_FAVOURITE = 4;

    public static final String API_KEY = "AIzaSyCKN-AGNHA7ZYTPQ_-IXZUHFGT8UlXlZig";
    public static final String API_KEY_MAPS ="0LgN0zWElNFx2cMBe0vH1UtWShWq1VlUPUeUb9w";
    
    public static final String API = "http://sps.juursema.com/api.php?";
    public static final String USER_DB = "http://sps.juursema.com/profilepicdb/";
    public static final String GROUP_DB = "http://sps.juursema.com/logodb/";
    public static final String REGISTER_URL = "http://sps.juursema.com/signup.php";
    
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    
    public static String getUrl(Context c, int resource) {
    	return API + c.getResources().getString(resource);
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
	
	public static void createSimpleDialog(Context c, String s) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(c);
		builder.setMessage(s)
		     .setCancelable(false)
		     .setNeutralButton("Ok", null);
		AlertDialog alert = builder.create();
		alert.show();
		
	}
    
	private static void showGpsOptions(Context c){
		Intent gpsOptionsIntent = new Intent(
				android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		c.startActivity(gpsOptionsIntent);
	}
}
