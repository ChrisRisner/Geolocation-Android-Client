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
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import com.msdpe.geodemo.R;
import com.msdpe.geodemo.misc.Constants;
import com.msdpe.geodemo.misc.GeoDemoApplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AddPointOfInterestActivity extends Activity {

	public final static int kRESULTCODECREATED = 2345;
	public final static int kRESULTCODENOTCREATED = 3456;
	private final static int GallaryRequestCode = 1234;

	private static final String TAG = "AddPointOfInterestActivity";

	private Activity mActivity;
	private Button mBtnGetSAS, mBtnSavePOI, mBtnSelectImage;
	private TextView mLblSASDetails;
	private ImageView mImgSelectedImage;
	private ProgressDialog mProgressDialog;

	private String _blobImagePostString = null;
	private Uri mImageUrl;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_poi);

		mActivity = this;
		// Get UI Controls
		mBtnGetSAS = (Button) findViewById(R.id.btnGetSAS);
		mBtnSavePOI = (Button) findViewById(R.id.btnSavePOI);
		mBtnSelectImage = (Button) findViewById(R.id.btnSelectImage);
		mImgSelectedImage = (ImageView) findViewById(R.id.imgSelectedImage);
		mLblSASDetails = (TextView) findViewById(R.id.lblSASDetails);

		mBtnGetSAS.setEnabled(false);
		mBtnSavePOI.setEnabled(false);

		// Image select handler
		mBtnSelectImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectImage();
			}
		});

		// Get SAS Handler
		mBtnGetSAS.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Start progress dialog and start async task
				mProgressDialog = new ProgressDialog(mActivity);
				mProgressDialog.setMessage("Requesting SAS URL");
				mProgressDialog.show();
				new GetSASTask().execute();
			}
		});

		// Save POI handler
		mBtnSavePOI.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Start progress dialog and start async task
				mProgressDialog = new ProgressDialog(mActivity);
				mProgressDialog.setMessage("Uploading Point of Interest");
				mProgressDialog.show();
				new PostPointOfInterestTask().execute();
			}
		});
	}

	// Fire off intent to select image from gallary
	protected void selectImage() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(intent, GallaryRequestCode);

	}

	// Result handler for any intents started with startActivityForResult
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		try {
			Log.i(TAG, "onActivityResult called code=" + requestCode
					+ "  resultCode: " + resultCode);
			//handle result from gallary select
			if (requestCode == AddPointOfInterestActivity.GallaryRequestCode) {

				Uri currImageURI = data.getData();
				Log.w(TAG, "URI: " + currImageURI.toString());
				this.mImageUrl = currImageURI;
				//Set the image view's image by using imageUri
				mImgSelectedImage.setImageURI(currImageURI);

				mBtnGetSAS.setEnabled(true);
				mBtnSavePOI.setEnabled(false);
			}
		} catch (Exception ex) {
			Log.e(TAG, "Error in onActivityResult: " + ex.getMessage());
		}
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
			Intent geoActivity = new Intent(getApplicationContext(),
					GeodemoActivity.class);
			geoActivity.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(geoActivity);
			return true;
		case (R.id.menu_addpoi):
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	//Fetches a SAS URL from the server
	protected void getSas() {
		try {
			String fetchUrl = String.format(Constants.kBlobSASUrl, Constants.kContainerName,
					System.currentTimeMillis());
			Log.i(TAG, "FetchURL: " + fetchUrl);
			URL url = new URL(fetchUrl);
			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();

			InputStream in = new BufferedInputStream(
					urlConnection.getInputStream());

			BufferedReader r = new BufferedReader(new InputStreamReader(in));
			StringBuilder total = new StringBuilder();
			String line;
			while ((line = r.readLine()) != null) {
				total.append(line);
			}
			Log.w(TAG, total.toString());
			_blobImagePostString = total.toString();

		} catch (Exception ex) {
			Log.e(TAG, "Error:" + ex.getMessage());
		}

	}

	//Handles posting a point of interest including the image to the server
	private String postPointOfInterestToServer() {
		try {
			// Make sure we have an image selected
			if (this.mImageUrl == null) {
				return "FAIL-IMAGE";
			}
			// Make sure we have a location
			GeoDemoApplication app = (GeoDemoApplication) getApplication();
			if (app.getCurrentLocation() == null) {
				return "FAIL-LOCATION";
			}

			Cursor cursor = getContentResolver().query(this.mImageUrl, null,
					null, null, null);
			cursor.moveToFirst();

			int index = cursor
					.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
			String absoluteFilePath = cursor.getString(index);
			Log.i(TAG, "FilePath: " + absoluteFilePath);
			FileInputStream fis = new FileInputStream(absoluteFilePath);

			int bytesRead = 0;
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] b = new byte[1024];
			while ((bytesRead = fis.read(b)) != -1) {
				bos.write(b, 0, bytesRead);
			}
			byte[] bytes = bos.toByteArray();

			// //////////////////////////////////////////////////////////////////////////////
			// Post our byte array to the server

			URL url = new URL(_blobImagePostString.replace("\"", ""));
			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();
			urlConnection.setDoOutput(true);
			urlConnection.setRequestMethod("PUT");
			urlConnection.addRequestProperty("Content-Type", "image/jpeg");
			urlConnection.setRequestProperty("Content-Length", ""
					+ bytes.length);
			Log.i(TAG, "Length: " + bytes.length);

			// Write image data to server
			DataOutputStream wr = new DataOutputStream(
					urlConnection.getOutputStream());
			wr.write(bytes);
			wr.flush();
			wr.close();

			int response = urlConnection.getResponseCode();
			Log.w(TAG, "Response: " + response);
			Log.w(TAG, "Resonse Message: " + urlConnection.getResponseMessage());

			if (response == 201
					&& urlConnection.getResponseMessage().equals("Created")) {
				// We've posted succesfully, let's build the JSON data
				Location currentLocation = app.getCurrentLocation();
				JSONObject jsonUrl = new JSONObject();
				try {
					jsonUrl.put("Description", absoluteFilePath
							.substring(absoluteFilePath.lastIndexOf("/") + 1));
					UUID uuid = UUID.randomUUID();
					jsonUrl.put("Id", uuid.toString());
					jsonUrl.put("Latitude", currentLocation.getLatitude());
					jsonUrl.put("Longitude", currentLocation.getLongitude());
					jsonUrl.put("Type", 1);
					jsonUrl.put("Url",
							this._blobImagePostString.split("\\?")[0]);
				} catch (JSONException e) {
					Log.e(TAG, "Exception building JSON: " + e.getMessage());
					e.printStackTrace();
				}
				Log.i(TAG, "JSON: " + jsonUrl.toString());

				HttpURLConnection newPOIUrlConnection = null;

				URL newPOIUrl = new URL(Constants.kAddPOIUrl);
				newPOIUrlConnection = (HttpURLConnection) newPOIUrl
						.openConnection();
				newPOIUrlConnection.setDoOutput(true);
				newPOIUrlConnection.setRequestMethod("POST");
				newPOIUrlConnection.addRequestProperty("Content-Type",
						"application/json");
				newPOIUrlConnection.setRequestProperty(//
						"Content-Length",
						""
								+ Integer.toString(jsonUrl.toString()
										.getBytes().length));
				//byte[] newPoiBytes = jsonUrl.toString().getBytes("UTF-8");

				// Write json data to server
				DataOutputStream newPoiWR = new DataOutputStream(
						newPOIUrlConnection.getOutputStream());
				newPoiWR.writeBytes(jsonUrl.toString());
				newPoiWR.flush();
				newPoiWR.close();

				int newPoiResponse = urlConnection.getResponseCode();
				Log.w(TAG, "Response 2: " + newPoiResponse);
				Log.w(TAG, "Resonse Message 2: "
								+ newPOIUrlConnection.getResponseMessage());
				return newPOIUrlConnection.getResponseMessage();				
			}
			// End of post of byte array to server
		} catch (Exception ex) {
			Log.e(TAG, "Error in image upload: " + ex.getMessage());
		}
		return "BIGFAIL";
	}

	private class PostPointOfInterestTask extends
			AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			return postPointOfInterestToServer();

		}

		/***
		 * Handles results of trying to post a POI.
		 * This occurs on the UI thread
		 */
		@Override
		protected void onPostExecute(String result) {
			if (result.equals("Created")) {
				setResult(AddPointOfInterestActivity.kRESULTCODECREATED);
				finish();
			} else if (result.equals("FAIL-IMAGE"))
				Toast.makeText(
						getApplicationContext(),
						"You must select an image from the gallery prior to uploading a POI.",
						Toast.LENGTH_LONG).show();
			else if (result.equals("FAIL-LOCATION"))
				Toast.makeText(getApplicationContext(),
						"You must set a location prior to uploading a POI.",
						Toast.LENGTH_LONG).show();
			else
				Toast.makeText(mActivity, "POI upload FAILED",
						Toast.LENGTH_SHORT).show();
			mProgressDialog.dismiss();

		}
	}

	private class GetSASTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			getSas();
			return _blobImagePostString;
		}

		/***
		 * Handles results of getting SAS.
		 * This happens on the UI Thread
		 */
		protected void onPostExecute(String result) {
			mLblSASDetails.setText(result);
			mProgressDialog.dismiss();
			mBtnGetSAS.setEnabled(false);
			mBtnSavePOI.setEnabled(true);
		}
	}
}
