package io.mosip.kernel.keymanagerservice.test.util;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertThat;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import io.mosip.kernel.core.keymanager.exception.KeystoreProcessingException;
import io.mosip.kernel.core.keymanager.model.CertificateEntry;
import io.mosip.kernel.core.keymanager.spi.ECKeyStore;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.keymanager.hsm.util.CertificateUtility;
import io.mosip.kernel.keymanagerservice.constant.KeymanagerConstant;
import io.mosip.kernel.keymanagerservice.repository.KeyAliasRepository;
import io.mosip.kernel.keymanagerservice.repository.KeyPolicyRepository;
import io.mosip.kernel.keymanagerservice.repository.KeyStoreRepository;
import io.mosip.kernel.keymanagerservice.test.KeymanagerTestBootApplication;
import io.mosip.kernel.keymanagerservice.util.KeymanagerUtil;

@SpringBootTest(classes = { KeymanagerTestBootApplication.class })
@RunWith(SpringRunner.class)
public class KeymanagerUtilTest {

	@MockBean
	private ECKeyStore keyStore;

	@MockBean
	private KeyAliasRepository keyAliasRepository;

	@MockBean
	private KeyPolicyRepository keyPolicyRepository;

	@MockBean
	private KeyStoreRepository keyStoreRepository;

	@Autowired
	private KeymanagerUtil keymanagerUtil;

	private KeyPair keyPairMaster;

	private KeyPair keyPair;

	private X509Certificate[] chain;

	@Before
	public void setupKey() throws NoSuchAlgorithmException {
		BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KeymanagerConstant.RSA);
		keyGen.initialize(2048);
		keyPairMaster = keyGen.generateKeyPair();
		keyPair = keyGen.generateKeyPair();
		X509Certificate x509Certificate = CertificateUtility.generateX509Certificate(keyPair.getPrivate(), keyPair.getPublic(), "mosip", "mosip", "mosip",
				"india", LocalDateTime.of(2010, 1, 1, 12, 00), LocalDateTime.of(2011, 1, 1, 12, 00), "SHA256withRSA", "BC");
		chain = new X509Certificate[1];
		chain[0] = x509Certificate;
	}

	@Test
	public void encryptdecryptPrivateKeyTest() {
		byte[] key = keymanagerUtil.encryptKey(keyPair.getPrivate(), keyPairMaster.getPublic());
		assertThat(key, isA(byte[].class));
		assertThat(keymanagerUtil.decryptKey(key, keyPairMaster.getPrivate(), keyPairMaster.getPublic()), isA(byte[].class));
	}

	@Test(expected = KeystoreProcessingException.class)
	public void isCertificateValidExceptionTest() {
		CertificateEntry<X509Certificate, PrivateKey> certificateEntry = new CertificateEntry<X509Certificate, PrivateKey>(
				chain, keyPair.getPrivate());
		keymanagerUtil.isCertificateValid(certificateEntry, DateUtils.parseUTCToDate("2019-05-01T12:00:00.00Z"));
	}

}
