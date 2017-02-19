package com.verint.textanalytics.common.security;

import com.verint.textanalytics.common.configuration.ConfigurationManager;
import com.verint.textanalytics.common.exceptions.AWTServiceErrorCode;
import com.verint.textanalytics.common.exceptions.AWTServiceException;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.jose4j.base64url.internal.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by TBaum on 6/23/2016.
 */
public class AWTService {

	@Autowired
	private ConfigurationManager configurationManager;

	private Logger logger = LogManager.getLogger(this.getClass());

	private final String iso8601DateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	private final int randomStringLength = 32;
	private final String utf8 = "UTF-8";
	private String apiKeyId;
	private String apiKey;
	private final String verintAuthId = "Vrnt-1-HMAC-SHA256";
	private RandomString random = new RandomString(randomStringLength);
	private Mac sha256HMAC;
	private final Lock lock = new ReentrantLock();

	/**
	 * JWTService Constructor.
	 */
	public AWTService() {

	}

	/**
	 * init the class.
	 */
	public void initialize() {
		try {

			logger.info("Initialize AWT Service.");

			this.apiKey = configurationManager.getApplicationConfiguration().getAwtApiKey();
			this.apiKeyId = configurationManager.getApplicationConfiguration().getAwtApiKeyId();

			byte[] secretkey = apiKey.getBytes(StandardCharsets.US_ASCII);

			sha256HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secretKeySpec = new SecretKeySpec(secretkey, "HmacSHA256");
			sha256HMAC.init(secretKeySpec);
		} catch (Exception ex) {
			logger.error("Failed tp initiaize AWT token generation data.", ex);
		}
	}

	/**
	 * creates a signed AET token.
	 *
	 * @param httpVerb - The HTTP verb used in the request (i.e. "GET", "POST","PUT")
	 * @param httpPath - The path part of the un-decoded HTTP Request-URI, up-to but not including the query string.
	 * @return header (key value) pair to add to the request
	 * @throws Exception - AWTServiceException
	 */
	public Pair<String, String> getAWTHeader(String httpVerb, String httpPath) {

		lock.lock();

		try {
			String salt = this.getSalt();

			//IssuedAt = ISO-8601-Format( now )
			String issuedAt = getISO8601StringFromDate(new DateTime(DateTimeZone.UTC));

			String stringToSign = String.format("%s\n%s\n%s\n%s\n%s\n", salt, httpVerb, httpPath, issuedAt, "");
			byte[] hashValue = sha256HMAC.doFinal(stringToSign.getBytes(StandardCharsets.US_ASCII));

			// URL Encode sig
			String hashValueStr = Base64.encodeBase64String(hashValue);
			String signature = URLEncoder.encode(base64ForUrlEncode(hashValueStr));

			String authHeaderValue = String.format("%s salt=%s,iat=%s,kid=%s,sig=%s", verintAuthId, salt, issuedAt, apiKeyId, signature);

			return new Pair<>("Authorization", authHeaderValue);

		} catch (Exception e) {
			logger.error(String.format("AWT Header creation failure. for HTTP verb %s and path %s", httpVerb, httpPath), e);
			throw new AWTServiceException(e, AWTServiceErrorCode.CreateHeaderTokenError);
		} finally {
			lock.unlock();
		}

	}

	private String getSalt() throws UnsupportedEncodingException {
		String randomString = random.nextString();
		return toBase64UrlEncode(randomString);
	}

	private String toBase64UrlEncode(String input) throws UnsupportedEncodingException {
		byte[] encodedBytes = Base64.encodeBase64(input.getBytes(StandardCharsets.US_ASCII));

		String forUrlEncode = base64ForUrlEncode(new String(encodedBytes, StandardCharsets.US_ASCII));
		String result =  URLEncoder.encode(forUrlEncode);
		return result;
	}

	private String base64ForUrlEncode(String stringForUrlEncode) {
		stringForUrlEncode = stringForUrlEncode.substring(0, stringForUrlEncode.indexOf('=')); // Remove any trailing '='s
		stringForUrlEncode = stringForUrlEncode.replace('+', '-'); // 62nd char of encoding
		stringForUrlEncode = stringForUrlEncode.replace('/', '_'); // 63rd char of encoding
		return stringForUrlEncode;
	}

	/**
	 *
	 * @param date date
	 * @return iso8601DateTimeFormat
	 */
	public String getISO8601StringFromDate(DateTime date) {
		return date.toString(iso8601DateTimeFormat);
	}

}
