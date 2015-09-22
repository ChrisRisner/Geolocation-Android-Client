---
services:
platforms:
author: azure
---

# Geolocation - The Android Client
This is an Android client for the Geolocation service.  The client depends on a web service backend written in PHP which is [available here](https://github.com/WindowsAzure-Samples/Geolocation-PHP-Service).  Once the PHP site is up and running in Windows Azure Websites, the Android client will allow users to see their current position and any points tagged near them.  In addition, the user can upload new points with associated photos or videos..  This sample was built using Eclipse and the Android SDK.

Below you will find requirements and deployment instructions.

## Requirements
* Eclipse - This sample was built on Eclipse 3.7 though newer versions should work.  [Get Eclipse here](http://www.eclipse.org/downloads/).
* Android ADT - The ADT plugin for Eclipse was version 20 at build though newer versions should work.  [Get ADT here](http://developer.android.com/sdk/installing/installing-adt.html).
* Android SDK - The SDK was at version 20 at build and the app was compiled against API SDK version 15.  [Get the SDK here](http://developer.android.com/sdk/index.html).
* Windows Azure Account - Needed to run the PHP website.  [Sign up for a free trial](https://www.windowsazure.com/en-us/pricing/free-trial/).

## Additional Resources

#Specifying your site's subdomain.
Once you've set up your PHP backend with Windows Azure Websites, you will need to enter your site's subdomain into the source/src/com/msdpe/geodemo/misc/Constants.java file.  Replace all of the \<your-subdomain\> with the subdomain of the site you set up.

	public static final String kFindPOIUrl = "http://<Your Subdomain>.azurewebsites.net/api/Location/FindPointsOfInterestWithinRadius";
	public static final String kBlobSASUrl = "http://<Your Subdomain>.azurewebsites.net/api/blobsas/get?container=%s&blobname=%s";
	public static final String kAddPOIUrl = "http://<Your Subdomain>.azurewebsites.net/api/location/postpointofinterest/";

## Contact

For additional questions or feedback, please contact the [team](mailto:chrisner@microsoft.com).