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
package com.openshift.internal.client;

import static com.openshift.client.utils.UrlEndsWithMatcher.urlEndsWith;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.openshift.client.IHttpClient;
import com.openshift.client.IOpenShiftConnection;
import com.openshift.client.OpenShiftConnectionFactory;
import com.openshift.client.cartridge.IEmbeddableCartridge;
import com.openshift.client.cartridge.IStandaloneCartridge;
import com.openshift.client.utils.Samples;

/**
 * @author Xavier Coulon
 * @author Andre Dietisheim
 */
public class APIResourceTest {

	private IHttpClient mockClient;

	private IOpenShiftConnection connection;

	@Before
	public void setup() throws Throwable {
		mockClient = mock(IHttpClient.class);
		when(mockClient.get(urlEndsWith("/broker/rest/api")))
				.thenReturn(Samples.GET_API.getContentAsString());
		when(mockClient.get(urlEndsWith("/cartridges")))
				.thenReturn(Samples.GET_CARTRIDGES.getContentAsString());
		connection = new OpenShiftConnectionFactory()
				.getConnection(new RestService("http://mock", "clientId", mockClient), "foo@redhat.com", "bar");
	}

	@Test
	public void shouldLoadListOfStandaloneCartridges() throws Throwable {
		// pre-conditions
		// operation
		final List<IStandaloneCartridge> cartridges = connection.getStandaloneCartridges();
		// verifications
		assertThat(cartridges)
				.hasSize(15)
				.onProperty("name")
					.contains("nodejs-0.6", "jbossas-7", "jbosseap-6.0", "jbossews-1.0", "jbossews-2.0")
					.excludes("mongodb-2.2", "mysql-5.1", "switchyard-0.6");
	}

	@Test
	public void shouldListEmbeddableCartridges() throws Throwable {
		// pre-conditions
		// operation
		final List<IEmbeddableCartridge> cartridges = connection.getEmbeddableCartridges();
		// verifications
		assertThat(cartridges)
				.hasSize(11)
				.onProperty("name")
				.excludes("nodejs-0.6", "jbossas-7", "jbosseap-6.0", "jbossews-1.0", "jbossews-2.0")
				.contains("mongodb-2.2", "mysql-5.1", "switchyard-0.6");
	}
}
