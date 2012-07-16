package com.hmi.smartphotosharing.camera;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.SharePhotoActivity;

/**
 * This class handles the Camera page.
 * It creates an intent to let a different app handle the photo capturing.
 * The resulting picture can be stored in the gallery, shared with friends or deleted.
 * @author Edwin
 *
 */
public class CameraActivity extends Activity {


	private static final int ACTION_TAKE_PHOTO = 1;

	private static final String BITMAP_STORAGE_KEY = "viewbitmap";
	private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";
	private ImageView mImageView;
	private Bitmap mImageBitmap;

	private String mCurrentPhotoPath;

	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";

	private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);

		// Find the ImageView from the xml file
		mImageView = (ImageView) findViewById(R.id.imageView1);
		mImageBitmap = null;

		// Disable the button if the Intent is not available
		Button picBtn = (Button) findViewById(R.id.btnIntend);
		setBtnListenerOrDisable( 
				picBtn, 
				mTakePicOnClickListener,
				MediaStore.ACTION_IMAGE_CAPTURE
		);
		
		// Check the Android version and decide which album path settings to use
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
		} else {
			mAlbumStorageDirFactory = new BaseAlbumDirFactory();
		}
    }

	
	/**
	 * Method that handles the result of the Intent.
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (requestCode == ACTION_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
			handleCameraPhoto();
		}

	}

	/**
	 * Some lifecycle callbacks so that the image can survive orientation change
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(BITMAP_STORAGE_KEY, mImageBitmap);
		outState.putBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY, (mImageBitmap != null) );
		super.onSaveInstanceState(outState);
	}

	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		mImageBitmap = savedInstanceState.getParcelable(BITMAP_STORAGE_KEY);
		mImageView.setImageBitmap(mImageBitmap);
		mImageView.setVisibility(
				savedInstanceState.getBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY) ? 
						ImageView.VISIBLE : ImageView.INVISIBLE
		);
	}
	
	/**
	 * OnClickListener that responds to the Take Photo button being clicked.
	 * Dispatches the capturing of the photo to another app.
	 */
	Button.OnClickListener mTakePicOnClickListener = 
		new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				dispatchTakePictureIntent(ACTION_TAKE_PHOTO);
			}
	};
	
	/**
	 * OnClickListener that responds to the Share button being clicked.
	 * Simply sends the current picture to the share page.
	 */

	public void onShareClick(View v) {
		// Send the intent to the class that can handle incoming photos
		Intent intent = new Intent(this,SharePhotoActivity.class);
		intent.setType("image/jpeg");

		// Add the Uri of the current photo as extra value
		intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(mCurrentPhotoPath));
		
		// Create and start the chooser
		startActivity(intent);
	}

	
	/**
	 * Binds the bitmap image to the View and adds it to the gallery.
	 */
	private void handleCameraPhoto() {

		if (mCurrentPhotoPath != null) {
						
			setPic();
			galleryAddPic();
			//mCurrentPhotoPath = null;
		}

	}	
	
	/**
	 *  Photo album for this application 
	 */
	private String getAlbumName() {
		return getString(R.string.album_name);
	}
	
	/**
	 * Tries to create the album directory
	 * @return The created directory or null when it cannot create the directory.
	 */
	private File getAlbumDir() {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			
			storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());

			if (storageDir != null) {
				if (! storageDir.mkdirs()) {
					if (! storageDir.exists()){
						Log.d("CameraSample", "failed to create directory");
						return null;
					}
				}
			}
			
		} else {
			Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
		}
		
		return storageDir;
	}

	/**
	 * Sets the filename and actually creates the file.
	 * @return
	 * @throws IOException
	 */
	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
		return imageF;
	}

	/**
	 * Creates the file and sets the path for the photo
	 * @return The File where the image is stored.
	 * @throws IOException Thrown when writing is not possible.
	 */
	private File setUpPhotoFile() throws IOException {
		
		File f = createImageFile();
		mCurrentPhotoPath = f.getAbsolutePath();
		
		return f;
	}

	/**
	 * Prescales the image to fit the view.
	 */
	private void setPic() {

		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
		int targetW = mImageView.getWidth();
		int targetH = mImageView.getHeight();

		/* Get the size of the image */
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;
		
		/* Figure out which way needs to be reduced less */
		int scaleFactor = 1;
		if ((targetW > 0) || (targetH > 0)) {
			scaleFactor = Math.min(photoW/targetW, photoH/targetH);	
		}

		/* Set bitmap options to scale the image decode target */
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */
		Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		
		/* Associate the Bitmap to the ImageView */
		mImageView.setImageBitmap(bitmap);
		mImageView.setVisibility(View.VISIBLE);
	}

	/**
	 * Method for adding the current photo to the gallery.
	 * Creates an Intent to rescan the media gallery.
	 */
	private void galleryAddPic() {
		    Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
			File f = new File(mCurrentPhotoPath);
		    Uri contentUri = Uri.fromFile(f);
		    mediaScanIntent.setData(contentUri);
		    sendBroadcast(mediaScanIntent);
	}

	/**
	 * This method handles the actual taking of a picture.
	 * It creates an Intent to capture an image, sets up the resulting file path
	 * and calls the method for handling the Intent result.
	 * @param actionCode
	 */
	private void dispatchTakePictureIntent(int actionCode) {

		// Create a new intent to let another app capture a picture
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		// Prepare the file path
		switch(actionCode) {
			case ACTION_TAKE_PHOTO:
				File f = null;
				
				try {
					f = setUpPhotoFile();
					mCurrentPhotoPath = f.getAbsolutePath();
					takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
				} catch (IOException e) {
					e.printStackTrace();
					f = null;
					mCurrentPhotoPath = null;
				}
				break;
	
			default:
				break;			
		}

		// Handle the result of the Intent
		startActivityForResult(takePictureIntent, actionCode);
	}
	
	/**
	 * Indicates whether the specified action can be used as an intent. This
	 * method queries the package manager for installed packages that can
	 * respond to an intent with the specified action. If no suitable package is
	 * found, this method returns false.
	 * http://android-developers.blogspot.com/2009/01/can-i-use-this-intent.html
	 *
	 * @param context The application's environment.
	 * @param action The Intent action to check for availability.
	 *
	 * @return True if an Intent with the specified action can be sent and
	 *         responded to, false otherwise.
	 */
	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list =
			packageManager.queryIntentActivities(intent,
					PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	/**
	 * Disables the button when the Intent for taking a picture is not available (no camera on the device).
	 * @param btn The button that was clicked
	 * @param onClickListener The listener that handles the button click
	 * @param intentName Name of the Intent
	 */
	private void setBtnListenerOrDisable( 
			Button btn, 
			Button.OnClickListener onClickListener,
			String intentName) {
		if (isIntentAvailable(this, intentName)) {
			btn.setOnClickListener(onClickListener);        	
		} else {
			btn.setText( 
				getText(R.string.cannot).toString() + " " + btn.getText());
			btn.setClickable(false);
		}
	}
}