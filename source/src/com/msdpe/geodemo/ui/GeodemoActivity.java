// ---------------------------------------------------------------------------------- 
// Microsoft Developer & Platform Evangelism 
//  
// Copyright (c) Microsoft Corporation. All rights reserved. 
//  
// THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND,  
// EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES  
// OF MERCHANTABILITY AND/OR FITNESS FOR A PARTICULAR PURPOSE. 
// ---------------------------------------------------------------------------------- 
// The example companies, organizations, products, domain names, 
// e-mail addresses, logos, people, places, and events depicted 
// herein are fictitious.  No association with any real company, 
// organization, product, domain name, email address, logo, person, 
// places, or events is intended or should be inferred. 
// ---------------------------------------------------------------------------------- 
package com.msdpe.geodemo.ui;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.msdpe.geodemo.R;
import com.msdpe.geodemo.mapping.GeoItemizedOverlay;
import com.msdpe.geodemo.misc.Constants;
import com.msdpe.geodemo.misc.GeoDemoApplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class GeodemoActivity extends MapActivity {

	private String TAG = "GeodemoActivity";

	private static final int kCREATEPOIREQUESTCODE = 0123;

	private MapView mMapMain;
	private TextView mLblLatitudeValue;
	private TextView mLblLongitudeValue;
	private List<Overlay> mMapOverlays;
	private Drawable mDrawable;
	private GeoItemizedOverlay mItemizedOverlay;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Get UI controls
		mLblLatitudeValue = (TextView) findViewById(R.id.lblLatitudeValue);
		mLblLongitudeValue = (TextView) findViewById(R.id.lblLongitudeValue);
		mMapMain = (MapView) findViewById(R.id.mapMain);

		mMapMain.setBuiltInZoomControls(true);

		mMapOverlays = mMapMain.getOverlays();
		mDrawable = this.getResources().getDrawable(R.drawable.androidmarker);
		mItemizedOverlay = new GeoItemizedOverlay(mDrawable, this);

		mMapOverlays.add(mItemizedOverlay);

		MyLocationOverlay myLocationOverlay = new MyLocationOverlay(this,
				mMapMain);
		mMapMain.getOverlays().add(myLocationOverlay);
		myLocationOverlay.enableMyLocation();

		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates
		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				// Called when a new location is found by the network location
				// provider.
				Log.i(TAG, "onLocationChanged");
				makeUseOfNewLocation(location);

				// Set our current location on the app object so we can access
				// it later
				GeoDemoApplication app = (GeoDemoApplication) getApplication();
				app.setCurrentLocation(location);
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				Log.i(TAG, "onStatusChanged");
			}

			public void onProviderEnabled(String provider) {
				Log.i(TAG, "onProviderEnabled");
			}

			public void onProviderDisabled(String provider) {
				Log.i(TAG, "onProviderDisabled");
			}
		};

		// Register the listener with the Location Manager to receive location
		// updates
		boolean couldPollNetworkProvider = true;
		boolean couldPollGPSProvider = true;
		try {
			locationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		} catch (Exception ex) {
			couldPollNetworkProvider = false;
		}
		try {
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		} catch (Exception ex) {
			couldPollGPSProvider = false;
		}
		if (!couldPollGPSProvider && !couldPollNetworkProvider)
			Toast.makeText(this, "Couldn't get any location provider",Toast.LENGTH_LONG).show();
		else if (!couldPollGPSProvider)
			Toast.makeText(this, "Couldn't get GPS provider",Toast.LENGTH_LONG).show();
		else if (!couldPollNetworkProvider)
			Toast.makeText(this, "Couldn't get network provider",Toast.LENGTH_LONG).show();
	}

	protected void makeUseOfNewLocation(Location location) {
		// Set our text views to the new long and lat
		mLblLatitudeValue.setText(String.valueOf(location.getLatitude()));
		mLblLongitudeValue.setText(String.valueOf(location.getLongitude()));

		GeoPoint point = coordinatesToGeoPoint(new double[] {
				location.getLatitude(), location.getLongitude() });

		CenterLocation(point);

		// Get Data from server
		loadPointsFromServer(location);
		mMapMain.invalidate();
	}

	private void loadPointsFromServer(Location location) {

		try {
			String fetchUrl = Constants.kFindPOIUrl + "?latitude="
					+ location.getLatitude() + "&longitude="
					+ location.getLongitude() + "&radiusInMeters=1000";
			URL url = new URL(fetchUrl);
			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();
			try {
				InputStream in = new BufferedInputStream(
						urlConnection.getInputStream());

				BufferedReader r = new BufferedReader(new InputStreamReader(in));
				StringBuilder stringBuilderResult = new StringBuilder();
				String line;
				while ((line = r.readLine()) != null) {
					stringBuilderResult.append(line);
				}
				Log.w(TAG, stringBuilderResult.toString());

				JSONArray jsonArray = new JSONArray(
						stringBuilderResult.toString());
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					Log.i(TAG, "Obj: " + jsonObject.toString());
					Double latitude = jsonObject.getDouble("Latitude");
					Double longitude = jsonObject.getDouble("Longitude");
					String description = jsonObject.getString("Description");
					String itemUrl = jsonObject.getString("Url");
					// The item URL comes back with quotes at the beginning,
					// so we strip them out
					itemUrl = itemUrl.replace("\"", "");

					// Create a new geo point with this information and add it
					// to the overlay
					GeoPoint point = coordinatesToGeoPoint(new double[] {
							latitude, longitude });
					OverlayItem overlayitem = new OverlayItem(point,
							description, itemUrl);
					mItemizedOverlay.addOverlay(overlayitem);
				}
			} catch (Exception ex) {
				Log.e(TAG, "Error getting data from server: " + ex.getMessage());
			} finally {
				urlConnection.disconnect();
			}
		} catch (Exception ex) {
			Log.e(TAG, "Error creating connection: " + ex.getMessage());

		}
	}

	private void CenterLocation(GeoPoint centerGeoPoint) {
		mMapMain.getController().animateTo(centerGeoPoint);
	};

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	// Create a geo point from coordinates
	public static GeoPoint coordinatesToGeoPoint(double[] coords) {
		if (coords.length > 2) {
			return null;
		}
		if (coords[0] == Double.NaN || coords[1] == Double.NaN) {
			return null;
		}
		final int latitude = (int) (coords[0] * 1E6);
		final int longitude = (int) (coords[1] * 1E6);
		return new GeoPoint(latitude, longitude);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case (R.id.menu_map):
			return true;
		case (R.id.menu_addpoi):
			Intent addPOIIntent = new Intent(getApplicationContext(),
					AddPointOfInterestActivity.class);
			addPOIIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivityForResult(addPOIIntent, kCREATEPOIREQUESTCODE);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/***
	 * Handler for any activites returning from a call to startActivityForResult
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == kCREATEPOIREQUESTCODE) {
			// Check to see if a POI was created
			if (resultCode == AddPointOfInterestActivity.kRESULTCODECREATED) {
				Toast.makeText(getApplicationContext(), "New POI Created!",
						Toast.LENGTH_SHORT).show();
				GeoDemoApplication app = (GeoDemoApplication) getApplication();
				loadPointsFromServer(app.getCurrentLocation());

			} else {
				Toast.makeText(getApplicationContext(), "Nothing done",
						Toast.LENGTH_SHORT).show();
			}
		} else
			super.onActivityResult(requestCode, resultCode, data);
	}
}