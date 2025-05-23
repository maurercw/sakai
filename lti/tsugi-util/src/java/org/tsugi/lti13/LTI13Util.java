/*
 * $URL$
 * $Id$
 *
 * Copyright (c) 2013- Charles R. Severance
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.tsugi.lti13;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;
import java.util.Base64;
import java.util.Enumeration;
import java.util.Properties;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.tsugi.lti.LTIConstants;
import org.tsugi.lti.LTIUtil;

import org.tsugi.lti13.objects.LaunchJWT;
import org.tsugi.lti13.objects.LTI11Transition;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LTI13Util {

	public static Map<String, String> generateKeys()
			throws java.security.NoSuchAlgorithmException {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(2048);

		KeyPair kp = keyGen.genKeyPair();
		byte[] publicKey = kp.getPublic().getEncoded();
		byte[] privateKey = kp.getPrivate().getEncoded();

		String publicRSA = "-----BEGIN PUBLIC KEY-----\n"
				+ base64Encode(privateKey)
				+ "\n-----END PUBLIC KEY-----\n";
		String privateRSA = "-----BEGIN PRIVATE KEY-----\n"
				+ base64Encode(privateKey)
				+ "\n-----END PRIVATE KEY-----\n";

		// If we need a pem style for these keys
		// String pemBase64 = javax.xml.bind.DatatypeConverter.printBase64Binary(publicKey);
		Map<String, String> returnMap = new TreeMap<>();
		returnMap.put("platform_public", publicRSA);
		returnMap.put("platform_private", privateRSA);

		// Do it again for the tool
		keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(2048);

		kp = keyGen.genKeyPair();
		publicKey = kp.getPublic().getEncoded();
		privateKey = kp.getPrivate().getEncoded();

		publicRSA = "-----BEGIN RSA PUBLIC KEY-----\n"
				+ base64Encode(privateKey)
				+ "\n-----END RSA PUBLIC KEY-----\n";
		privateRSA = "-----BEGIN RSA PRIVATE KEY-----\n"
				+ base64Encode(privateKey)
				+ "\n-----END RSA PRIVATE KEY-----\n";

		returnMap.put("tool_public", publicRSA);
		returnMap.put("tool_private", privateRSA);

		return returnMap;

	}

	public static String base64Encode(byte[] input) {
		Base64.Encoder encoder = Base64.getEncoder();
		String retval = encoder.encodeToString(input);
		return retval;
	}

	public static String stripPKCS8(String input) {
		if (input == null) {
			return input;
		}
		if (!input.startsWith("-----BEGIN")) {
			return input;
		}
		String[] lines = input.split("\n");
		String retval = "";
		for (String line : lines) {
			if (line.startsWith("----")) {
				continue;
			}
			retval = retval + line.trim();
		}
		return retval;
	}

	public static KeyPair generateKeyPair() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(2048);
			KeyPair kp = keyGen.genKeyPair();
			return kp;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * The B64 Format is like the PEM format but no headers and all one line - it
	 * is like the SSH format
	 */
	public static String getKeyB64(Key key) {
		byte[] encodeArray = key.getEncoded();
		String publicRSA = base64Encode(encodeArray);
		return publicRSA;
	}

	public static String getPublicB64(KeyPair kp) {
		return getKeyB64(kp.getPublic());
	}


	public static String getPublicEncoded(KeyPair kp) {
		return getPublicEncoded(kp.getPublic());
	}

	public static String getPublicEncoded(Key key) {
		return getPublicEncoded(getKeyB64(key));
	}

	public static String getPublicEncoded(String keyStr) {
		// Don't double convert
		if ( keyStr.startsWith("-----BEGIN ") ) return keyStr;

		String publicRSA = "-----BEGIN PUBLIC KEY-----\n"
				+ breakKeyIntoLines(keyStr)
				+ "\n-----END PUBLIC KEY-----\n";
		return publicRSA;
	}

	public static String getPrivateB64(KeyPair kp) {
		return getKeyB64(kp.getPrivate());
	}

	public static String getPrivateB64(Key key) {
		byte[] encodeArray = key.getEncoded();
		String privateRSA = base64Encode(encodeArray);
		return privateRSA;
	}

	public static String getPrivateEncoded(KeyPair kp) {
		return getPrivateEncoded(kp.getPrivate());
	}

	public static String getPrivateEncoded(Key key) {
		return getPrivateEncoded(getPrivateB64(key));
	}

	public static String getPrivateEncoded(String keyStr) {
		// Don't double convert
		if ( keyStr.startsWith("-----BEGIN ") ) return keyStr;

		String privateRSA = "-----BEGIN PRIVATE KEY-----\n"
				+ breakKeyIntoLines(keyStr)
				+ "\n-----END PRIVATE KEY-----\n";
		return privateRSA;
	}

	public static String breakKeyIntoLines(String rawkey) {
		int len = 65;
		StringBuilder ret = new StringBuilder();

		String trimmed = rawkey.trim();
		for (int i = 0; i < trimmed.length(); i += len) {
			int end = i + len;
			if (ret.length() > 0) {
				ret.append("\n");
			}
			if (end > trimmed.length()) {
				end = trimmed.length();
			}
			ret.append(trimmed.substring(i, end));
		}
		return ret.toString();
	}

	public static Key string2PrivateKey(String keyString) {
		if ( keyString == null ) return null;
		try {
			KeyFactory kf = KeyFactory.getInstance("RSA");

			keyString = stripPKCS8(keyString);
			PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(keyString.getBytes()));
			return (Key) kf.generatePrivate(keySpecPKCS8);
		} catch (IllegalArgumentException | InvalidKeySpecException e) {
			return null;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public static Key string2PublicKey(String keyString) {
		if ( keyString == null ) return null;
		try {
			KeyFactory kf = KeyFactory.getInstance("RSA");

			keyString = stripPKCS8(keyString);
			X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(keyString));
			return (Key) kf.generatePublic(keySpecX509);
		} catch (IllegalArgumentException | InvalidKeySpecException e) {
			return null;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public static KeyPair strings2KeyPair(String pubKeyStr, String privKeyStr) {
		if ( pubKeyStr == null || privKeyStr == null) return null;
		String pubEncoded = LTI13Util.getPublicEncoded(pubKeyStr);
        String privEncoded = LTI13Util.getPrivateEncoded(privKeyStr);
		if ( pubEncoded == null || privEncoded == null) return null;

		PublicKey pubKey = (PublicKey) LTI13Util.string2PublicKey(pubEncoded);
        PrivateKey privKey = (PrivateKey) LTI13Util.string2PrivateKey(privEncoded);
		if ( pubKey == null || privKey == null) return null;
		KeyPair retval = new KeyPair(pubKey, privKey);
		return retval;
	}

	public static String serializeKeyPair(KeyPair kp)
	{
		String pub = LTI13Util.getPublicEncoded(kp);
		String priv = LTI13Util.getPrivateEncoded(kp);
		String concat = pub + "::" + priv;
		return concat;
	}

	public static KeyPair deSerializeKeyPair(String ser)
	{
		if ( ser == null ) return null;
		String [] pieces = ser.split("::");
		if ( pieces.length != 2 ) return null;
		return strings2KeyPair(pieces[0], pieces[1]);
	}

	// https://www.imsglobal.org/spec/lti/v1p3/migr#lti-1-1-migration-claim
	/*
		sign=base64(hmac_sha256(utf8bytes('179248902&689302&https://lmsvendor.com&PM48OJSfGDTAzAo&1551290856&172we8671fd8z'), utf8bytes('my-lti11-secret')))

		{
			"nonce": "172we8671fd8z",
			"iat": 1551290796,
			"exp": 1551290856,
			"iss": "https://lmsvendor.com",
			"aud": "PM48OJSfGDTAzAo",
			"sub": "3",
			"https://purl.imsglobal.org/spec/lti/claim/deployment_id": "689302",
			"https://purl.imsglobal.org/spec/lti/claim/lti1p1": {
				"user_id": "34212",
				"oauth_consumer_key": "179248902",
				"oauth_consumer_key_sign": "lWd54kFo5qU7xshAna6v8BwoBm6tmUjc6GTax6+12ps="
			}
		}

	 */

	/**
	 * Compute the base string for a Launch JWT
	 *
	 * See: https://www.imsglobal.org/spec/lti/v1p3/migr#lti-1-1-migration-claim
	 *
	 * @param lj The Launch JSON Web Token with the LTI 1.1 transition data
	 *
	 * @return string This is null if the base string cannot be computed
	 */
	public static String getLTI11TransitionBase(LaunchJWT lj) {

		String nonce =  lj.nonce;
		Long expires = lj.expires;
		String issuer = lj.issuer;
		String client_id = lj.audience;
		String subject = lj.subject;
		String deployment_id = lj.deployment_id;
		if ( nonce == null || issuer == null || expires == null ||
				client_id == null || subject == null || deployment_id == null) return null;

		if ( lj.lti11_transition == null ) return null;
		LTI11Transition lti11_transition = lj.lti11_transition;
		String user_id = lti11_transition.user_id;
		String oauth_consumer_key = lti11_transition.oauth_consumer_key;
		if ( user_id == null || oauth_consumer_key == null ) return null;

		String base = oauth_consumer_key + "&" + deployment_id + "&" + issuer + "&" +
			client_id + "&" + expires + "&" + nonce;

		return base;
	}

	/**
	 * Compute the OAuth signature for an LTI 1.3 Launch JWT
	 *
	 * See: https://www.imsglobal.org/spec/lti/v1p3/migr#lti-1-1-migration-claim
	 *
	 * @param lj The Launch JSON Web Token with the LTI 11 transition data
	 * @param secret The OAuth secret
	 *
	 * @return string This is null if the signature cannot be computed
	 */
	public static String signLTI11Transition(LaunchJWT lj, String secret) {

		if ( secret == null ) return null;

		String base = getLTI11TransitionBase(lj);
		if ( base == null ) return null;

		String signature = compute_HMAC_SHA256(base, secret);
		return signature;
	}

	/**
	 * Check the OAuth signature for an LTI 1.3 Launch JWT
	 *
	 * See: https://www.imsglobal.org/spec/lti/v1p3/migr#lti-1-1-migration-claim
	 *
	 * @param lj The Launch JSON Web Token with the LTI 11 transition data
	 * @param key The OAuth key
	 * @param secret The OAuth secret
	 *
	 * @return true if the signature matches, false if the JWT
	 * the signature does not match or the JWT data is malformed.
	 */
	public static boolean checkLTI11Transition(LaunchJWT lj, String key, String secret) {

		if ( key == null ) return false;
		if ( secret == null ) return false;

		LTI11Transition lti11_transition = lj.lti11_transition;
		if ( lti11_transition == null ) return false;
		String oauth_consumer_key_sign = lti11_transition.oauth_consumer_key_sign;
		if ( oauth_consumer_key_sign == null ) return false;
		String oauth_consumer_key = lti11_transition.oauth_consumer_key;
		if ( oauth_consumer_key == null ) return false;

		if ( !oauth_consumer_key.equals(key) ) return false;

		String base = getLTI11TransitionBase(lj);
		if ( base == null ) return false;

		String signature = compute_HMAC_SHA256(base, secret);
		return oauth_consumer_key_sign.equals(signature);
	}

	public static String sha256(String input) {

		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			// return Base64.getEncoder().encodeToString((md.digest(convertme));
			// md.update(input.getBytes());
			// byte[] output = Base64.encode(md.digest());
			String hash = base64Encode(md.digest(input.getBytes()));
			return hash;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Compute the HMAC256 of a string (part of LTI 1.1 Transition)
	 *
	 * See: https://www.imsglobal.org/spec/lti/v1p3/migr#lti-1-1-migration-claim
	 *
	 * Based on:
	 * https://www.jokecamp.com/blog/examples-of-creating-base64-hashes-using-hmac-sha256-in-different-languages/#php
	 * https://stackoverflow.com/questions/7124735/hmac-sha256-algorithm-for-signature-calculation
	 *
	 * @param object $message The message to sign
	 * @param string $secret The secret used to sign the message
	 *
	 * @return string The signed message
	 */
	public static String compute_HMAC_SHA256(String message, String secret)
	{

		try {
			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
			sha256_HMAC.init(secret_key);

			String hash = base64Encode(sha256_HMAC.doFinal(message.getBytes()));
			return hash;
		}
		catch (Exception e){
			return null;
		}
	}

	/**
	 * Get the scores URL from a lineItem Url
	 *
	 * Moodle gives us lineItem URLs with query parameters.
	 */
	public static String getScoreUrlForLineItem(String lineItemUrl)
	{
		if ( lineItemUrl == null ) return lineItemUrl;
		int pos = lineItemUrl.indexOf("?");
		if ( pos < 0 ) return lineItemUrl + "/scores";
		return lineItemUrl.substring(0,pos) + "/scores?" + lineItemUrl.substring(pos+1);
	}

	/*
		HTTP/1.1 400 OK
		Content-Type: application/json;charset=UTF-8
		Cache-Control: no-store
		Pragma: no-cache

		{
			"error" : "invalid_scope"
		}
	*/

	public static void return400(HttpServletResponse response, String error) {
		return4XX(response, error, null, HttpServletResponse.SC_BAD_REQUEST);
	}

	public static void return400(HttpServletResponse response, String error, String detail) {
		return4XX(response, error, detail, HttpServletResponse.SC_BAD_REQUEST);
	}

	public static void return403(HttpServletResponse response, String error) {
		return4XX(response, error, null, HttpServletResponse.SC_FORBIDDEN);
	}

	public static void return403(HttpServletResponse response, String error, String detail) {
		return4XX(response, error, detail, HttpServletResponse.SC_FORBIDDEN);
	}

	public static void return404(HttpServletResponse response, String error) {
		return4XX(response, error, null, HttpServletResponse.SC_NOT_FOUND);
	}

	public static void return404(HttpServletResponse response, String error, String detail) {
		return4XX(response, error, detail, HttpServletResponse.SC_NOT_FOUND);
	}

	public static void return4XX(HttpServletResponse response, String error, String detail, int httpStatus) {
		response.setContentType("application/json;charset=UTF-8");
		response.setHeader("Cache-Control", "no-store");
		response.setStatus(httpStatus);
		if ( detail != null ) response.setHeader("X-Tsugi-LTI13-Error-Detail", detail);
		JSONObject job = new JSONObject();
		job.put("error", error);
		String retval = org.tsugi.jackson.JacksonUtil.toString(job);
		try {
			log.debug("httpStatus={}; retval={}", httpStatus, retval);
			PrintWriter out = response.getWriter();
			out.println(retval);
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			log.error(e.getMessage(), e);
		}
	}

    public static void substituteCustom(Properties custom, Properties substitutes) {
        if (custom == null || substitutes == null) return;
        custom.stringPropertyNames().forEach(key -> {
            String value = custom.getProperty(key);

            // Allow both User.id and $User.id
            if (value.startsWith("$") && value.length() > 1) {
                value = value.substring(1);
            }

            String substituteValue = substitutes.getProperty(value);
            if (StringUtils.isNotBlank(substituteValue)) {
                setProperty(custom, key, substituteValue);
            }
        });
    }

    // Place the custom values into the launch
    public static void addCustomToLaunch(Properties ltiProps, Properties custom) {
        for (String key : custom.stringPropertyNames()) {
            if (key.startsWith("lti_launch_")) {
                // sakai internal substitution
                setProperty(ltiProps,
                        LTIUtil.mapKeyName(StringUtils.substringAfter(key, "lti_launch_")),
                        custom.getProperty(key));
            } else {
                // sets a custom property
                setProperty(ltiProps,
                        LTIConstants.CUSTOM_PREFIX + LTIUtil.mapKeyName(key),
                        custom.getProperty(key));
            }
        }
    }

	@SuppressWarnings("deprecation")
    public static void setProperty(Properties props, String key, String value) {
        LTIUtil.setProperty(props, key, value);
    }

    /**
     * timeStamp - Add a time-stamp to the beginning of a string
     */
    public static String timeStamp(String token) {
        long now = (new java.util.Date()).getTime()/1000;
        String retval = now + ":" + token;
        return retval;
    }

    /**
     * timeStampCheck - Check a time-stamp at the beginning of a string
     */
    public static boolean timeStampCheck(String token, int seconds) {
        String [] pieces = token.split(":");
        if ( pieces.length < 2 ) return false;

        long now = (new java.util.Date()).getTime()/1000;
        long token_now = LTIUtil.toLong(pieces[0]);
        long delta = now - token_now;
        boolean retval = (delta >= 0) && (delta <= seconds);
        return retval;
    }

    /**
     * timeStampSign - Take a one-time token and add a timestamp
     *
     * timestamp:token:signature
     */
    public static String timeStampSign(String token, String secret) {
        String timeStamped = timeStamp(token);
        String base_string = timeStamped + ":" + secret;
        String signature = sha256(base_string);
        String retval = timeStamped + ":" + signature;
        return retval;
    }

    /**
     * timeStampCheckSign - check to see if a token has not expired and is signed
     */
    public static boolean timeStampCheckSign(String token, String secret, int seconds) {
        if ( ! timeStampCheck(token, seconds) ) return false;
        String [] pieces = token.split(":");
        if ( pieces.length != 3 ) return false;
        String token_signature = pieces[2];
        String base_string = pieces[0] + ":" + pieces[1] + ":" + secret;
        String signature = sha256(base_string);
        if ( ! signature.equals(token_signature) ) return false;
        return true;
    }

    public static String getStringNull(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        return null;
    }

}
