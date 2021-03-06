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

import static com.openshift.client.utils.FileUtils.createRandomTempFile;
import static com.openshift.client.utils.Samples.GET_DOMAINS;
import static com.openshift.client.utils.UrlEndsWithMatcher.urlEndsWith;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.openshift.client.IHttpClient;
import com.openshift.client.IOpenShiftConnection;
import com.openshift.client.IOpenShiftSSHKey;
import com.openshift.client.ISSHPublicKey;
import com.openshift.client.IUser;
import com.openshift.client.OpenShiftConnectionFactory;
import com.openshift.client.OpenShiftSSHKeyException;
import com.openshift.client.OpenShiftUnknonwSSHKeyTypeException;
import com.openshift.client.SSHKeyPair;
import com.openshift.client.SSHKeyType;
import com.openshift.client.SSHPublicKey;
import com.openshift.client.utils.SSHKeyTestUtils;
import com.openshift.client.utils.SSHKeyTestUtils.SSHPublicKeyAssertion;
import com.openshift.client.utils.Samples;
import com.openshift.internal.client.httpclient.HttpClientException;

/**
 * @author Andre Dietisheim
 */
public class SSHKeyTest {

	private IHttpClient mockClient;
	private IUser user;
	private RestService service;

	@Before
	public void setUp() throws SocketTimeoutException, HttpClientException, Throwable {
		mockClient = mock(IHttpClient.class);
		when(mockClient.get(urlEndsWith("/broker/rest/api")))
				.thenReturn(Samples.GET_API.getContentAsString());
		when(mockClient.get(urlEndsWith("/user")))
				.thenReturn(Samples.GET_USER_JSON.getContentAsString());
		when(mockClient.get(urlEndsWith("/domains")))
				.thenReturn(GET_DOMAINS.getContentAsString());
		this.service = new RestService("http://mock", "clientId", mockClient);
		final IOpenShiftConnection connection = 
				new OpenShiftConnectionFactory().getConnection(service,"foo@redhat.com", "bar");
		this.user = connection.getUser();
	}

	@Test
	public void shouldCreatePublicKey() throws Exception {
		// pre-conditions
		String publicKeyPath = createRandomTempFile().getAbsolutePath();
		String privateKeyPath = createRandomTempFile().getAbsolutePath();
		// operation
		SSHKeyPair sshKey = SSHKeyPair.create(SSHKeyTestUtils.DEFAULT_PASSPHRASE, privateKeyPath, publicKeyPath);
		// verification
		String publicKey = sshKey.getPublicKey();
		assertNotNull(sshKey.getKeyType());
		String keyType = sshKey.getKeyType().getTypeId();
		assertNotNull(publicKey);
		assertThat(publicKey)
				// no identifier
				.doesNotContain(SSHKeyTestUtils.SSH_RSA)
				// no comment
				.doesNotContain(" ");
		assertEquals(SSHKeyType.SSH_RSA.getTypeId(), keyType);
	}

	@Test
	public void shouldLoadKeyPairRsa() throws Exception {
		// pre-condition
		String publicKeyPath = createRandomTempFile().getAbsolutePath();
		String privateKeyPath = createRandomTempFile().getAbsolutePath();
		SSHKeyPair.create(SSHKeyTestUtils.DEFAULT_PASSPHRASE, privateKeyPath, publicKeyPath);
		// operation
		SSHKeyPair sshKey = SSHKeyPair.load(privateKeyPath, publicKeyPath);
		// verification
		String publicKey = sshKey.getPublicKey();
		assertNotNull(sshKey.getKeyType());
		String keyType = sshKey.getKeyType().getTypeId();
		assertNotNull(publicKey);
		assertThat(publicKey)
				// no identifier
				.doesNotContain(SSHKeyTestUtils.SSH_RSA)
				// no comment
				.doesNotContain(" ");
		assertEquals(SSHKeyType.SSH_RSA.getTypeId(), keyType);
	}

	@Test
	public void shouldLoadKeyPairDsa() throws Exception {
		// pre-conditions
		String publicKeyPath = createRandomTempFile().getAbsolutePath();
		String privateKeyPath = createRandomTempFile().getAbsolutePath();
		SSHKeyTestUtils.createDsaKeyPair(publicKeyPath, privateKeyPath);
		// operation
		SSHKeyPair sshKey = SSHKeyPair.load(privateKeyPath, publicKeyPath);
		// verification
		String publicKey = sshKey.getPublicKey();
		assertNotNull(sshKey.getKeyType());
		String keyType = sshKey.getKeyType().getTypeId();
		assertNotNull(publicKey);
		assertThat(publicKey)
				// no identifier
				.doesNotContain(SSHKeyTestUtils.SSH_DSA)
				// no comment
				.doesNotContain(" ");
		assertEquals(SSHKeyType.SSH_DSA.getTypeId(), keyType);
	}

	@Test
	public void shouldLoadPublicKeyRsa() throws Exception {
		// pre-conditions
		String publicKeyPath = createRandomTempFile().getAbsolutePath();
		String privateKeyPath = createRandomTempFile().getAbsolutePath();
		SSHKeyPair.create(SSHKeyTestUtils.DEFAULT_PASSPHRASE, privateKeyPath, publicKeyPath);
		ISSHPublicKey sshKey = new SSHPublicKey(new File(publicKeyPath));
		String publicKey = sshKey.getPublicKey();
		assertNotNull(sshKey.getKeyType());
		String keyType = sshKey.getKeyType().getTypeId();
		assertNotNull(publicKey);
		assertThat(publicKey)
				// no identifier
				.doesNotContain(SSHKeyTestUtils.SSH_RSA)
				// no comment
				.doesNotContain(" ");
		// operation
		SSHKeyPair keyPair = SSHKeyPair.load(privateKeyPath, publicKeyPath);
		assertEquals(publicKey, keyPair.getPublicKey());
		assertEquals(SSHKeyType.SSH_RSA.getTypeId(), keyType);
	}

	@Test
	public void shouldLoadPublicKeyDsa() throws Exception {
		// pre-conditions
		String publicKeyPath = createRandomTempFile().getAbsolutePath();
		String privateKeyPath = createRandomTempFile().getAbsolutePath();
		SSHKeyTestUtils.createDsaKeyPair(publicKeyPath, privateKeyPath);
		ISSHPublicKey sshKey = new SSHPublicKey(publicKeyPath);
		String publicKey = sshKey.getPublicKey();
		assertNotNull(sshKey.getKeyType());
		String keyType = sshKey.getKeyType().getTypeId();
		assertNotNull(publicKey);
		assertThat(publicKey)
		// no identifier
		.doesNotContain(SSHKeyTestUtils.SSH_DSA)
		// no comment
		.doesNotContain(" ");
		
		// operation
		SSHKeyPair keyPair = SSHKeyPair.load(privateKeyPath, publicKeyPath);
		assertEquals(publicKey, keyPair.getPublicKey());
		assertEquals(SSHKeyType.SSH_DSA.getTypeId(), keyType);
	}

	@Test
	public void shouldGetKeyTypeByTypeId() throws OpenShiftUnknonwSSHKeyTypeException {
		assertTrue(SSHKeyType.SSH_DSA == SSHKeyType.getByTypeId(SSHKeyTestUtils.SSH_DSA));
		assertTrue(SSHKeyType.SSH_RSA == SSHKeyType.getByTypeId(SSHKeyTestUtils.SSH_RSA));
	}

	@Test(expected = OpenShiftUnknonwSSHKeyTypeException.class)
	public void getKeyTypeByTypeIdReturnsNullIfNoMatchingType() throws OpenShiftUnknonwSSHKeyTypeException {
		SSHKeyType.getByTypeId("dummy");
	}

	@Test
	public void shouldReturn2SSHKeys() throws HttpClientException, Throwable {
		// pre-conditions
		when(mockClient.get(urlEndsWith("/user/keys")))
				.thenReturn(Samples.GET_USER_KEYS_2KEYS.getContentAsString());
		// operation
		List<IOpenShiftSSHKey> sshKeys = user.getSSHKeys();
		// verifications
		assertThat(sshKeys).hasSize(2);
		assertThat(new SSHPublicKeyAssertion(sshKeys.get(0)))
				.hasName("default").hasPublicKey("ABBA").isType(SSHKeyTestUtils.SSH_RSA);
		assertThat(new SSHPublicKeyAssertion(sshKeys.get(1)))
				.hasName("default2").hasPublicKey("AABB").isType(SSHKeyTestUtils.SSH_DSA);
	}

	@Test
	public void shouldAddAndUpdateKey() throws SocketTimeoutException, HttpClientException, Throwable {
		// pre-conditions
		when(mockClient.post(anyMapOf(String.class, Object.class), urlEndsWith("/user/keys")))
				.thenReturn(Samples.PUT_BBCC_DSA_USER_KEYS_SOMEKEY.getContentAsString());
		when(mockClient.get(urlEndsWith("/user/keys")))
				.thenReturn(Samples.GET_USER_KEYS_NONE.getContentAsString());
		String publicKeyPath = createRandomTempFile().getAbsolutePath();
		String privateKeyPath = createRandomTempFile().getAbsolutePath();
		SSHKeyTestUtils.createDsaKeyPair(publicKeyPath, privateKeyPath);
		SSHPublicKey publicKey = new SSHPublicKey(publicKeyPath);

		String keyName = "somekey";
		// operation
		user.putSSHKey(keyName, publicKey);

		// verifications
		List<IOpenShiftSSHKey> keys = user.getSSHKeys();
		assertThat(keys).hasSize(1);
		assertThat(new SSHPublicKeyAssertion(keys.get(0)))
				.hasName(keyName)
				.hasPublicKey("BBCC")
				.isType(SSHKeyTestUtils.SSH_DSA);
	}

	@Test
	public void shouldUpdateKeyTypeAndPublicKey() throws SocketTimeoutException, HttpClientException, Throwable {
		// pre-conditions
		String keyName = "somekey";
		String keyUrl = service.getServiceUrl() + "user/keys/" + keyName;
		String newPublicKey = "BBCC";

		when(mockClient.get(urlEndsWith("/user/keys")))
				.thenReturn(Samples.GET_USER_KEYS_1KEY.getContentAsString());
		when(mockClient.put(anyMapOf(String.class, Object.class), urlEndsWith(keyUrl)))
				.thenReturn(Samples.PUT_BBCC_DSA_USER_KEYS_SOMEKEY.getContentAsString());

		// operation
		List<IOpenShiftSSHKey> keys = user.getSSHKeys();
		assertThat(keys).hasSize(1);
		IOpenShiftSSHKey key = keys.get(0);
		
		// verification
		assertThat(key.getKeyType()).isEqualTo(SSHKeyType.SSH_RSA);
		
		// operation
		key.setKeyType(SSHKeyType.SSH_DSA, newPublicKey);

		// verification
		assertThat(key.getKeyType()).isEqualTo(SSHKeyType.SSH_DSA);
		assertThat(key.getPublicKey()).isEqualTo(newPublicKey);
		HashMap<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put("type", SSHKeyTestUtils.SSH_DSA);
		parameterMap.put("content", key.getPublicKey());
		verify(mockClient).put(parameterMap, new URL(keyUrl));
	}

	@Test
	public void shouldUpdatePublicKey() throws SocketTimeoutException, HttpClientException, Throwable {
		// pre-conditions
		String keyName = "somekey";
		String keyUrl = service.getServiceUrl() + "user/keys/" + keyName;
		String newPublicKey = "BBCC";
		when(mockClient.get(urlEndsWith("/user/keys")))
				.thenReturn(Samples.GET_USER_KEYS_1KEY.getContentAsString());
		when(mockClient.put(anyMapOf(String.class, Object.class), urlEndsWith(keyUrl)))
				.thenReturn(Samples.PUT_BBCC_DSA_USER_KEYS_SOMEKEY.getContentAsString());

		// operation
		List<IOpenShiftSSHKey> keys = user.getSSHKeys();
		assertThat(keys).hasSize(1);
		IOpenShiftSSHKey key = keys.get(0);
		assertThat(key.getKeyType()).isEqualTo(SSHKeyType.SSH_RSA);
		assertThat(key.getPublicKey()).isNotEqualTo(newPublicKey);
		key.setPublicKey(newPublicKey);

		// verification
		assertThat(key.getKeyType()).isEqualTo(SSHKeyType.SSH_DSA);
		assertThat(key.getPublicKey()).isEqualTo(newPublicKey);
		HashMap<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put("type", SSHKeyTestUtils.SSH_RSA);
		parameterMap.put("content", newPublicKey);
		verify(mockClient).put(parameterMap, new URL(keyUrl));
	}

	@Test(expected = OpenShiftSSHKeyException.class)
	public void shouldNotAddKeyWithExistingName() throws SocketTimeoutException, HttpClientException, Throwable {
		// pre-conditions
		when(mockClient.get(urlEndsWith("/user/keys")))
				.thenReturn(Samples.GET_USER_KEYS_1KEY.getContentAsString());
		String publicKeyPath = createRandomTempFile().getAbsolutePath();
		String privateKeyPath = createRandomTempFile().getAbsolutePath();
		SSHKeyTestUtils.createDsaKeyPair(publicKeyPath, privateKeyPath);
		SSHPublicKey publicKey = new SSHPublicKey(publicKeyPath);

		// operation
		assertThat(user.getSSHKeys()).hasSize(1);
		String existingKeyName = user.getSSHKeys().get(0).getName();
		user.putSSHKey(existingKeyName, publicKey);
	}

	@Test(expected = OpenShiftSSHKeyException.class)
	public void shouldNotAddKeyTwice() throws SocketTimeoutException, HttpClientException, Throwable {
		// pre-conditions
		String keyName = "somekey";
		String keyUrl = service.getServiceUrl() + "user/keys/" + keyName;
		when(mockClient.get(urlEndsWith("/user/keys")))
				.thenReturn(Samples.GET_USER_KEYS_1KEY.getContentAsString());
		when(mockClient.put(anyMapOf(String.class, Object.class), urlEndsWith(keyUrl)))
				.thenReturn(Samples.PUT_BBCC_DSA_USER_KEYS_SOMEKEY.getContentAsString());
		String publicKeyPath = createRandomTempFile().getAbsolutePath();
		String privateKeyPath = createRandomTempFile().getAbsolutePath();
		SSHKeyTestUtils.createDsaKeyPair(publicKeyPath, privateKeyPath);
		SSHPublicKey publicKey = new SSHPublicKey(publicKeyPath);

		// operation
		assertThat(user.getSSHKeys()).onProperty("name").contains(keyName);
		user.putSSHKey(keyName, publicKey); // throws
	}

}
