/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package com.openshift.client.fakes;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;

import com.openshift.client.OpenShiftException;
import com.openshift.client.utils.OpenShiftTestConfiguration;
import com.openshift.internal.client.httpclient.FormUrlEncodedMediaType;
import com.openshift.internal.client.httpclient.HttpClientException;
import com.openshift.internal.client.httpclient.UrlConnectionHttpClient;

/**
 * @author Andre Dietisheim
 */
public class HttpClientFake extends UrlConnectionHttpClient {

	public HttpClientFake(String mediaType, String version) throws FileNotFoundException, IOException, OpenShiftException {
		this(new OpenShiftTestConfiguration(), mediaType, version);
	}
	
	@Override
	protected String write(String data, String requestMethod, URL url)
			throws SocketTimeoutException, HttpClientException {
		return data;
	}

	protected HttpClientFake(OpenShiftTestConfiguration configuration, String mediaType, String version) {
		super(configuration.getRhlogin(),
				configuration.getPassword(),
				configuration.getClientId(),
				false,
				new FormUrlEncodedMediaType(),
				mediaType,
				version);
	}

}
