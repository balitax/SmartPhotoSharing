package com.hmi.smartphotosharing;

/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.    
*/
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

public class DrawableManager {
   private final Map<String, Bitmap> bitmaps;

   private Context context;
   
   public DrawableManager(Context context) {
       bitmaps = new HashMap<String, Bitmap>();
       this.context = context;
   }

   public Bitmap fetchDrawable(String urlString, ImageView imageView) {
	   if (bitmaps.containsKey(urlString)) {
           return bitmaps.get(urlString);
       }
	   
       Log.d(this.getClass().getSimpleName(), "image url:" + urlString);
       try {
    	   BufferedHttpEntity be = getHttpEntity(urlString);
           Bitmap b = getBitmapFromStream(be, imageView);
           if (b != null) {
        	   bitmaps.put(urlString, b);
               Log.d(this.getClass().getSimpleName(), "got a thumbnail drawable: " + b.getHeight() + ", " + b.getWidth());
           } else {
        	   //b = BitmapFactory.decodeStream(is);
        	   Log.w(this.getClass().getSimpleName(), "could not get thumbnail");
           }

           return b;
       } catch (ConnectTimeoutException e) {
    	   Log.e(this.getClass().getSimpleName(), "Connection timed out", e);
    	   return null;
       } catch (MalformedURLException e) {
           Log.e(this.getClass().getSimpleName(), "FetchDrawable failed", e);
           return null;
       } catch (IOException e) {
           Log.e(this.getClass().getSimpleName(), "FetchDrawable failed", e);
           return null;
       }
   }

   public void fetchDrawableOnThread(final String urlString, final ImageView imageView) {
	   
       if (bitmaps.containsKey(urlString)) {
           imageView.setImageBitmap(bitmaps.get(urlString));
           Log.i("DrawableManager", "Image loaded from cache");
           return;
       } else {
    	   imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_launcher));
       }

       final Handler handler = new Handler() {
           @Override
           public void handleMessage(Message message) {
        	   Bitmap b = (Bitmap) message.obj;
               if (b != null) imageView.setImageBitmap(b);
           }
       };

       Thread thread = new Thread() {
           @Override
           public void run() {
               Bitmap b = fetchDrawable(urlString, imageView);
               Message message = handler.obtainMessage(1, b);
               handler.sendMessage(message);
           }
       };
       thread.start();
   }

   private BufferedHttpEntity getHttpEntity(String urlString) throws MalformedURLException, IOException {
       DefaultHttpClient httpClient = new DefaultHttpClient();
       HttpGet request = new HttpGet(urlString);
       HttpResponse response = httpClient.execute(request);
       
       return new BufferedHttpEntity(response.getEntity());

   }
   
	/**
	 * Prescales the image to fit the view.
	 * @throws IOException 
	 */
	private Bitmap getBitmapFromStream(BufferedHttpEntity be, ImageView mImageView) throws IOException {
		
		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
		int targetW = mImageView.getWidth();
		int targetH = mImageView.getHeight();

		/* Get the size of the image */
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		//BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		InputStream is = be.getContent();
		BitmapFactory.decodeStream(is, null, bmOptions);
		is.close();
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
		is = be.getContent();
		Bitmap b = BitmapFactory.decodeStream(is, null, bmOptions);
		is.close();
		return b;
		
	}
}
