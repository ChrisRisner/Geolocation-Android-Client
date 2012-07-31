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
package com.msdpe.geodemo.misc;

public class Constants {

	public static final String kFindPOIUrl = "http://<Your Subdomain>.azurewebsites.net/api/Location/FindPointsOfInterestWithinRadius";
	public static final String kBlobSASUrl = "http://<Your Subdomain>.azurewebsites.net/api/blobsas/get?container=%s&blobname=%s";
	public static final String kAddPOIUrl = "http://<Your Subdomain>.azurewebsites.net/api/location/postpointofinterest/";
	
	public static final String kContainerName = "test";
}
