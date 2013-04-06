package com.hmi.smartphotosharing.local;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.gson.Gson;
import com.hmi.smartphotosharing.Login;
import com.hmi.smartphotosharing.NavBarFragmentActivity;
import com.hmi.smartphotosharing.PhotoDetailActivity;
import com.hmi.smartphotosharing.R;
import com.hmi.smartphotosharing.groups.GroupDetailActivity;
import com.hmi.smartphotosharing.json.Group;
import com.hmi.smartphotosharing.json.GroupListResponse;
import com.hmi.smartphotosharing.json.OnDownloadListener;
import com.hmi.smartphotosharing.json.Photo;
import com.hmi.smartphotosharing.json.PhotoListResponse;
import com.hmi.smartphotosharing.json.PostData;
import com.hmi.smartphotosharing.json.PostRequest;
import com.hmi.smartphotosharing.util.HelpDialog;
import com.hmi.smartphotosharing.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class MapActivity extends NavBarFragmentActivity implements LocationListener, LocationSource, OnCameraChangeListener, OnDownloadListener {

    private GoogleMap googleMap;
    private Marker lastClicked;
    
    private Marker longClickMarker;
    
    private long lastChange;
    private LatLng lastPos;
    
    private List<Polygon> polyList;
    private List<Long> markerIds;
    
    public static final String TYPE_GROUP = "GROUP";
    public static final String TYPE_PHOTO = "PHOTO";
    public static final String TYPE_POINT = "POINT";
        
    private static long MAP_TIME_THRESHOLD = 5000;
    private static int MAP_ZOOM_THRESHOLD = 14;
    private static int MAP_DISTANCE_THRESHOLD = 500;

    private static final int CODE_PHOTOS = 0;
    private static final int CODE_GROUPS = 1;
    private static final int CODE_POINT = 2;
    
    private static final int CODE_FILTER = 0;
    
	private OnLocationChangedListener mListener;
	private LocationManager mLocationManager;

	private ImageLoader imageLoader;
	
	// The first time the camera centers on the user's location it should zoom to street level
	// Afterwards, it should only center on the position without zooming
	private boolean firstZoomCamera;
	
	// Filter settings
	private boolean filterShowGroupBorders;
	private int filterType, filterDate;
	
	public static final String KEY_LAT = "lat";
	public static final String KEY_LON = "lon";
	public static final String KEY_THUMB = "thumb";
	public static final String KEY_IID = "iid";
	
	private double startLat, startLon;
	private String startThumb;
	private long startIid;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState,R.layout.local_map);
        
        polyList = new ArrayList<Polygon>();
        markerIds = new ArrayList<Long>();
    	firstZoomCamera = true;
        
        // Show selection in nav bar
        ImageView local = (ImageView) findViewById(R.id.local);
        Util.setSelectedBackground(getApplicationContext(), local);

        // Get intent
        Intent intent = getIntent();
        startLat = intent.getDoubleExtra(KEY_LAT, 0);
        startLon = intent.getDoubleExtra(KEY_LON, 0);
        startThumb = intent.getStringExtra(KEY_THUMB);
        startIid = intent.getLongExtra(KEY_IID, 0);        
        
        // ImageLoader
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(this));
       
        loadPreferences();

        SharedPreferences settings = getSharedPreferences(HelpDialog.DIALOG_PREFS, MODE_PRIVATE);
        boolean hide = settings.getBoolean(HelpDialog.DIALOG_LOCAL, false);
        String s = getResources().getString(R.string.dialog_local);
        if (!hide)
        	Util.createSimpleDialog(this,s,HelpDialog.DIALOG_LOCAL);
    }
    
	private void loadPreferences() {
	
	    SharedPreferences settings = getSharedPreferences(MapFilterActivity.FILTER_PREFS, MODE_PRIVATE);
	    filterType = settings.getInt(MapFilterActivity.TYPE, 0);
	    filterDate = settings.getInt(MapFilterActivity.DATE_VALUE, (int) (System.currentTimeMillis() / 1000));
	    filterShowGroupBorders = settings.getBoolean(MapFilterActivity.BORDERS, true);
	}
	
    private void setUpMapIfNeeded() {
        
    	// Do a null check to confirm that we have not already instantiated the map.
        if (googleMap == null) {
            googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googlemap))
                                .getMap();
            
            // Check if we were successful in obtaining the map.
            if (googleMap != null) {
            	            	
            	googleMap.setMyLocationEnabled(true);
                // One time fix to set the camera when the map is done loading
                googleMap.setOnCameraChangeListener(this);
                // Set the infowindow adapter
                googleMap.setInfoWindowAdapter(new MyInfoWindowAdapter(imageLoader, getLayoutInflater()));
                googleMap.setOnInfoWindowClickListener(new MyInfoWindowClickListener(this));
                googleMap.setOnMarkerClickListener(new MyMarkerClickListener());
                googleMap.setOnMapLongClickListener(new MyOnMapClickListener(this));
                
                // Set the source of location updates to this activity
                googleMap.setLocationSource(this);
                
                Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                
                if (location != null && firstZoomCamera) {
                	firstZoomCamera = false;
                	CameraPosition camPos = new CameraPosition.Builder()
                	   .target(new LatLng(location.getLatitude(), location.getLongitude()))
                	   .zoom(MAP_ZOOM_THRESHOLD)
                	   .build();
                	googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPos));
                	loadData();
                }
            } else {
            	// If the user does not have Play Services installed, show this notice and a download link
            	// The default message by Google is broken, so display our own message
            	RelativeLayout l = (RelativeLayout)findViewById(R.id.googlemapcontainer);
            	l.setVisibility(RelativeLayout.GONE);
            	TextView t = (TextView)findViewById(R.id.error_google_play_missing);
            	Button b = (Button)findViewById(R.id.button_google_play_missing);
            	t.setVisibility(TextView.VISIBLE);
            	b.setVisibility(Button.VISIBLE);
            }
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();

        // Check if the GPS setting is currently enabled on the device.
        // This verification should be done during onStart() because the system calls this method
        // when the user returns to the activity, which ensures the desired location provider is
        // enabled each time the activity resumes from the stopped state.
        mLocationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        if(mLocationManager != null) {
        	boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        	if(gpsEnabled) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 10F, this);
            	mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000L, 10F, this);
            } else {
            	Util.createGpsDisabledAlert(this);
            }
        }
    }
    
    @Override
    public void onPause() {
        if(mLocationManager != null) {
            mLocationManager.removeUpdates(this);
        }

        super.onPause();
    }
    
	@Override
    public void onResume() {
      super.onResume();

      setUpMapIfNeeded();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    	if (requestCode == CODE_FILTER && resultCode == RESULT_OK) {
    		googleMap.clear();
    		markerIds.clear();
        	loadPreferences();
    		loadData();
    	}
    	
    }
	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		super.onCreateOptionsMenu(menu);
	    return true;
	}	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
        switch (item.getItemId()) {

	        default:
	        	return super.onOptionsItemSelected(item);
        }
    }	

	@Override
	public void onCameraChange(CameraPosition pos) {
		
        long now = System.currentTimeMillis(); 
        if (lastChange == 0 || lastPos == null) {
        	lastChange = now;
        	lastPos = pos.target;
        } else {
        	
        	// If it is at least X milliseconds since the last change and
        	// the zoom level is greater than Y, request new photo locations
        	if (pos.zoom >= MAP_ZOOM_THRESHOLD) {
	        	if (now - lastChange >= MAP_TIME_THRESHOLD){
	        		
	        		// Check if the difference in distance from last camera pos is greater than X meters
	        		lastChange = now;
	        		lastPos = pos.target;
	        		//Log.d("SmarthPhotoSharing", "z: " + pos.zoom + " / d: " );
	        		
	        		loadData();
	        	}
        	} else {
        		clearMap();
        	}
        }		
	}
	
	/*
	public void onClickListMode(View view) {
    	Intent intent = new Intent(this,LocalPhotoActivity.class);
    	startActivity(intent);
	}
	
	public void onClickMapMode(View view) {
		// do nothing
	}*/
	
	private void clearMap() {
		googleMap.clear();
		markerIds.clear();		
	}

	public void onClickPlayServices(View view) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.gms"));
		startActivity(browserIntent);
	}
	
	public void onClickFilter(View view) {
    	Intent intent = new Intent(this,MapFilterActivity.class);
    	startActivityForResult(intent, CODE_FILTER);
	}
	
    public void onSearchClick(View view) {

        // Getting reference to EditText to get the user input location
        EditText etLocation = (EditText) findViewById(R.id.et_location);

        // Getting user input location
        String location = etLocation.getText().toString();

        if(location!=null && !location.equals("")) {
            new GeocoderTask().execute(location);
        }
    }
    
	private class MyInfoWindowClickListener implements OnInfoWindowClickListener {

		private Context c;
		
		public MyInfoWindowClickListener(Context c) {
			this.c = c;
		}
		
		@Override
		public void onInfoWindowClick(Marker marker) {
			
			String[] msg = marker.getSnippet().split(",");
			
			if (msg[0].equals(TYPE_GROUP)) {
		    	Intent intent = new Intent(c,GroupDetailActivity.class);
		    	intent.putExtra(GroupDetailActivity.KEY_ID, Long.parseLong(msg[1]));
		    	startActivity(intent);
				
			} else if (msg[0].equals(TYPE_POINT)) {
		    	Intent intent = new Intent(c,LocalGroupsActivity.class);
		    	intent.putExtra(LocalGroupsActivity.LAT, longClickMarker.getPosition().latitude);
		    	intent.putExtra(LocalGroupsActivity.LON, longClickMarker.getPosition().longitude);
		    	startActivity(intent);
				
			} else {
		    	Intent intent = new Intent(c,PhotoDetailActivity.class);
		    	intent.putExtra(PhotoDetailActivity.KEY_ID, Long.parseLong(msg[1]));
		    	startActivity(intent);
			}
			
		}
	}

	private class MyMarkerClickListener implements OnMarkerClickListener {

		@Override
		public boolean onMarkerClick(Marker marker) {
			// Close the InfoWindow if the user clicked it before
			if (lastClicked != null && lastClicked.equals(marker)) {
				lastClicked = null;
				marker.hideInfoWindow();
				return true;
			} else {
				lastClicked = marker;
				return false;
			}
		}
	}
	
	
	private class MyOnMapClickListener implements OnMapLongClickListener {

		private Context c;
		
		public MyOnMapClickListener(Context c) {
			this.c = c;
		}
		@Override
		public void onMapLongClick(LatLng point) {
			
			// Add a marker on the clicked point
			if (longClickMarker != null) {
				longClickMarker.remove();
			}
			
			MarkerOptions options = new MarkerOptions()
        	.position(point)
        	.title("Loading data")
        	.snippet(TYPE_POINT+","+"Loading...")
        	.icon(BitmapDescriptorFactory.fromResource(R.drawable.pushpin));
			longClickMarker = googleMap.addMarker(options);
			
			// Get data about this point
			SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
			String hash = settings.getString(Login.SESSION_HASH, null);
			
	    	// Get group info
			String url = Util.getUrl(getApplicationContext(),R.string.local_http_point);
			
	        HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();
	        try {
	        	
				map.put("sid",new StringBody(hash));
				
				map.put("lat", new StringBody(Double.toString(point.latitude)));
				map.put("lon", new StringBody(Double.toString(point.longitude)));
				
			} catch (UnsupportedEncodingException e) {
				Log.e("Map activity", e.getMessage());
			}
	        PostData pr = new PostData(url,map);
	        new PostRequest(c, CODE_POINT, false).execute(pr);
			
		}

	}
			
	private void loadData() {
		
		VisibleRegion bounds = googleMap.getProjection().getVisibleRegion();
				
		SharedPreferences settings = getSharedPreferences(Login.SESSION_PREFS, MODE_PRIVATE);
		String hash = settings.getString(Login.SESSION_HASH, null);
		
    	// Get group info
		String url = Util.getUrl(this,R.string.local_http);
		//Log.d("Maps url", detailUrl);
		//new FetchJSON(this).execute(detailUrl);
		
        HashMap<String,ContentBody> map = new HashMap<String,ContentBody>();
        try {
        	
			map.put("sid",new StringBody(hash));
			
			map.put("lat1", new StringBody(Double.toString(bounds.farLeft.latitude)));
			map.put("lon1", new StringBody(Double.toString(bounds.farLeft.longitude)));
			
			map.put("lat2", new StringBody(Double.toString(bounds.nearRight.latitude)));
			map.put("lon2", new StringBody(Double.toString(bounds.nearRight.longitude)));
			
			map.put("filtertype", new StringBody(Integer.toString(filterType)));
			map.put("filterdate", new StringBody(Integer.toString(filterDate)));
			
		} catch (UnsupportedEncodingException e) {
			Log.e("Map activity", e.getMessage());
		}
        PostData pr = new PostData(url,map);
        new PostRequest(this, CODE_PHOTOS, false).execute(pr);

        if (filterShowGroupBorders) {
	        HashMap<String,ContentBody> map2 = new HashMap<String,ContentBody>();
	        try {
	        	
				map2.put("sid",new StringBody(hash));
				
				map2.put("lat1", new StringBody(Double.toString(bounds.farLeft.latitude)));
				map2.put("lon1", new StringBody(Double.toString(bounds.farLeft.longitude)));
				
				map2.put("lat2", new StringBody(Double.toString(bounds.nearRight.latitude)));
				map2.put("lon2", new StringBody(Double.toString(bounds.nearRight.longitude)));
				
			} catch (UnsupportedEncodingException e) {
				Log.e("Map activity", e.getMessage());
			}
			url = Util.getUrl(this,R.string.local_http_groups);
	        pr = new PostData(url,map2);
	        new PostRequest(this, CODE_GROUPS, false).execute(pr);   
        }
	}
	
	@Override
	public void parseJson(String json, int code) {

		switch (code) {
			case CODE_GROUPS:
				parseGroups(json);
				break;
				
			case CODE_PHOTOS:
				parsePhotos(json);
				break;

			case CODE_POINT:
				parsePoint(json);
				break;	
			default:
		}
		
	}

	private void parsePoint(String json) {
		Gson gson = new Gson();
		GroupListResponse response = gson.fromJson(json, GroupListResponse.class);
		//Log.d("GroupsMap", json);
		
		if (response.getStatus() == Util.STATUS_OK) {
			// Put markers on the map
			List<Group> list = response.getObject();
			
			String txt = "";
			if (list != null && list.size() > 0) {
				txt = list.size() + " groups on this location. Click for info.";
			} else {
				txt = "No groups on this location.";
			}
			longClickMarker.setSnippet(TYPE_POINT + "," + txt);
			longClickMarker.showInfoWindow();
		} else {
			Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
		}
		
	}


	private void parseGroups(String json) {
		Gson gson = new Gson();
		GroupListResponse response = gson.fromJson(json, GroupListResponse.class);
		//Log.d("GroupsMap", json);
		
		if (response.getStatus() == Util.STATUS_OK) {
			// Put markers on the map
			List<Group> list = response.getObject();
			
			if (list != null && list.size() > 0) {
				
				for (Polygon p : polyList) {
					p.remove();
				}
				polyList.clear();
				
				for(Group g : list) {
					Double lat1 = Double.parseDouble(g.latstart);
					Double lon1 = Double.parseDouble(g.longstart);
					
					Double lat2 = Double.parseDouble(g.latend);
					Double lon2 = Double.parseDouble(g.longend);

	                LatLng ne = new LatLng(lat1,lon2);
	                LatLng nw = new LatLng(lat1,lon1);
	                LatLng sw = new LatLng(lat2,lon1);
	                LatLng se = new LatLng(lat2,lon2);

					MarkerOptions markerOptions = new MarkerOptions()
	                	.position(new LatLng(lat1,lon1))
	                	.title(Util.getThumbUrl(g))
	                	.snippet(TYPE_GROUP + "," + g.gid + "," + g.name)
	                	.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_group));
	                	//.icon(BitmapDescriptorFactory.defaultMarker(Util.getHue(Integer.parseInt(g.gid))));

	                googleMap.addMarker(markerOptions);
	                
	                Polygon p = googleMap.addPolygon(new PolygonOptions()
		    		    .add(ne, nw, sw, se) // 4 corners, ccw
		    		    .strokeWidth(3)
		    		    .strokeColor(Util.getColor(Integer.parseInt(g.gid))));
	                polyList.add(p);
				}
			}
			
		} else {
			Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
		}
		
	}


	private void parsePhotos(String json) {
		Gson gson = new Gson();
		PhotoListResponse response = gson.fromJson(json, PhotoListResponse.class);
		//Log.d("JSON parse", json);
		
		if (response.getStatus() == Util.STATUS_OK) {
			// Put markers on the map
			List<Photo> list = response.getObject();
			
			if (list != null && list.size() > 0) {
				
				for(Photo p : list) {
					long iid = p.getId();
					if ((startIid == 0 || startIid != iid) && !markerIds.contains(iid)) {
						markerIds.add(iid);
						Double lat = Double.parseDouble(p.latitude);
						Double lon = Double.parseDouble(p.longitude);
						
						MarkerOptions markerOptions = new MarkerOptions()
		                	.position(new LatLng(lat,lon))
		                	.title(Util.getThumbUrl(p))
		                	.snippet(TYPE_PHOTO + "," + p.iid)
		                	.icon(BitmapDescriptorFactory.defaultMarker(Util.getHue(Integer.parseInt(p.gid))));
	
		                googleMap.addMarker(markerOptions);
					}
				}
			}
			
		} else {
			Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
		}
		
	}


	// Methods to make this activity the source of location updates for the google map
	@Override
	public void activate(OnLocationChangedListener listener) {
		mListener = listener;		
	}


	@Override
	public void deactivate() {
		mListener = null;		
	}


	@Override
	public void onLocationChanged(Location location) {
		
	    if(mListener != null) {
	        mListener.onLocationChanged( location );

	        //Move the camera to the user's location on location update
	        // Only zoom if it is the first update after starting the activity
	        if (firstZoomCamera) {
	        	
	        	firstZoomCamera = false;
	        	
	        	if (startIid != 0) {

	        		LatLng startPos = new LatLng(startLat, startLon);
	        		
		        	CameraPosition camPos = new CameraPosition.Builder()
		        	   .target(startPos)
		        	   .zoom(MAP_ZOOM_THRESHOLD)
		        	   .build();
		        	
					MarkerOptions markerOptions = new MarkerOptions()
	                	.position(startPos)
	                	.title(startThumb)
	                	.snippet(TYPE_PHOTO + "," + startIid)
	                	.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_photo));

	                googleMap.addMarker(markerOptions);
		        	googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPos));
		        } else {
		        	CameraPosition camPos = new CameraPosition.Builder()
		        	   .target(new LatLng(location.getLatitude(), location.getLongitude()))
		        	   .zoom(MAP_ZOOM_THRESHOLD)
		        	   .build();
		        	googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPos));
                	loadData();
		        }
	        } else {
	        	/*
	        	googleMap.animateCamera(CameraUpdateFactory
	        		.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));*/
	        }
	    }
	}


	@Override
	public void onProviderDisabled(String arg0) {}


	@Override
	public void onProviderEnabled(String arg0) {}


	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}

	private class GeocoderTask extends AsyncTask<String, Void, List<Address>>{

        @Override
        protected List<Address> doInBackground(String... locationName) {
            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(getBaseContext());
            List<Address> addresses = null;

            try {
                // Getting a maximum of 3 Address that matches the input text
                addresses = geocoder.getFromLocationName(locationName[0], 3);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {

            if(addresses==null || addresses.size()==0){
                Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
            }

            // Clears all the existing markers on the map
            googleMap.clear();

            // Adding Markers on Google Map for each matching address
            for(int i=0;i<addresses.size();i++){

                Address address = (Address) addresses.get(i);

                // Creating an instance of GeoPoint, to display in Google Map
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                String addressText = String.format("%s, %s",
                address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                address.getCountryName());

                /*MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(addressText);

                googleMap.addMarker(markerOptions);*/

                // Locate the first location
                if(i==0)
                	googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        }
    }
}
