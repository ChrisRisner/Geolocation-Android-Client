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
package com.msdpe.geodemo.mapping;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class GeoItemizedOverlay extends ItemizedOverlay<OverlayItem> {

	private final String TAG = "GeoItemizedOverlay";
	private Context mContext;

	public GeoItemizedOverlay(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		this.mContext = context;
		this.populate();
	}

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

	public void addOverlay(OverlayItem overlay) {
		mOverlays.add(overlay);
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}

	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		Log.i(TAG, "item tapped: " + p.toString());
		return super.onTap(p, mapView);
	}

	/***
	 * When an overlay item is tapped, pop up an alert with it's Title and
	 * snippet
	 */
	@Override
	protected boolean onTap(int index) {
		Log.i(TAG, "Index of tapped item: " + index);
		OverlayItem tappedItem = mOverlays.get(index);
		Log.i(TAG, "Title of tapped item: " + tappedItem.getTitle());
		Log.i(TAG, "shippet of tapped item: " + tappedItem.getSnippet());

		// Bulid the alert dialog and show it
		AlertDialog dialog = new AlertDialog.Builder(this.mContext).create();
		dialog.setTitle(tappedItem.getTitle());
		dialog.setMessage(tappedItem.getSnippet());
		final String url = tappedItem.getSnippet();

		//When the user clicks view image, open a browser with the image URL
		dialog.setButton("View Image", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Intent webIntent = new Intent(Intent.ACTION_VIEW);
				webIntent.setData(Uri.parse(url));
				mContext.startActivity(webIntent);
			}
		});
		dialog.setButton2("Close", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}
		});

		dialog.show();
		
		return super.onTap(index);
	}
}
