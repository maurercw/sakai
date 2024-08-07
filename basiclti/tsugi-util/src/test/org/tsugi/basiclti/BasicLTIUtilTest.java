package org.tsugi.basiclti;
import static org.junit.Assert.*;
import static org.tsugi.basiclti.BasicLTIConstants.LTI_MESSAGE_TYPE;
import static org.tsugi.basiclti.BasicLTIConstants.LTI_MESSAGE_TYPE_BASICLTILAUNCHREQUEST;
import static org.tsugi.basiclti.BasicLTIConstants.LTI_VERSION;
import static org.tsugi.basiclti.BasicLTIConstants.LTI_VERSION_1;

import net.oauth.OAuth;
import net.oauth.OAuthMessage;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.text.StringEscapeUtils;

@Slf4j
public class BasicLTIUtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testGetRealPath() {
		String fixed = BasicLTIUtil.getRealPath("http://localhost/path/blah/", "https://right.com");
		assertEquals("https://right.com/path/blah/",fixed);
		fixed = BasicLTIUtil.getRealPath("https://localhost/path/blah/", "https://right.com");
		assertEquals("https://right.com/path/blah/",fixed);
		fixed = BasicLTIUtil.getRealPath("https://localhost/path/blah/", "http://right.com");
		assertEquals("http://right.com/path/blah/",fixed);

		// Test folks sending in URL with extra stuff...
		fixed = BasicLTIUtil.getRealPath("https://localhost/path/blah/", "https://right.com/path/blah");
		assertEquals("https://right.com/path/blah/",fixed);
	}

	@Test
	public void testGetRealPathRequest() {
		HttpServletRequest req = Mockito.mock(HttpServletRequest.class);

		Mockito.when(req.getRequestURL()).thenReturn(new StringBuffer("http://localhost/path/blah/"));
		String fixed = BasicLTIUtil.getRealPath(req, "https://right.com");
		assertEquals("https://right.com/path/blah/", fixed);

		Mockito.when(req.getRequestURL()).thenReturn(new StringBuffer("https://localhost/path/blah/"));
		fixed = BasicLTIUtil.getRealPath(req, "https://right.com");
		assertEquals("https://right.com/path/blah/", fixed);

		Mockito.when(req.getRequestURL()).thenReturn(new StringBuffer("https://localhost/path/blah/"));
		fixed = BasicLTIUtil.getRealPath(req, "http://right.com");
		assertEquals("http://right.com/path/blah/", fixed);

		Mockito.when(req.getRequestURL()).thenReturn(new StringBuffer("http://localhost/path/blah/"));
		fixed = BasicLTIUtil.getRealPath(req, "https://right.com/path/blah");
		assertEquals("https://right.com/path/blah/", fixed);
	}

	@Test
	public void testOAuthGET()
	{
		String url = BasicLTIUtil.getOAuthURL("GET", "http://www.dr-chuck.com/page1.htm?x=1", "12345", "secret");
		log.debug("url={}", url);
		assertTrue(url.contains("oauth_consumer_key"));
		assertTrue(url.contains("oauth_signature="));
		assertTrue(url.contains("www.dr-chuck.com"));

		url = BasicLTIUtil.getOAuthURL("GET", null, null, null);
		log.debug("url={}", url);
		assertNull(url);

		/*
		// Comment this test out int trunk as it requires the network to be up...
		HttpURLConnection connection = BasicLTIUtil.sendOAuthURL("GET", "http://www.dr-chuck.com/dump.php?x=1", "12345", "secret");
		try { int responseCode = connection.getResponseCode();
			log.debug("Responsecode={}", responseCode);
		} catch(Exception e) { }
		String data = BasicLTIUtil.readHttpResponse(connection);
		log.debug("data={}", data);
		// End of test with network required
		*/
	}

	@Test
	public void iso8601Text() throws Exception {
		String x = BasicLTIUtil.getISO8601();
		assertTrue(x.contains("Z"));
		assertTrue(x.contains("T"));
		String target = "2017-08-20:10:00:00";
		String iso8601 = "2017-01-20T10:00:00Z";

		DateFormat df = new SimpleDateFormat("yyyy-mm-dd:hh:mm:ss");
				df.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date result = df.parse(target);
		String y = BasicLTIUtil.getISO8601(result);
		assertEquals(y,iso8601);

		// Now lets parse some iso dates
		result = BasicLTIUtil.parseIMS8601(iso8601);
		String z = BasicLTIUtil.getISO8601(result);
		assertEquals(z, iso8601);

		result = BasicLTIUtil.parseIMS8601(null);
		assertNull(result);

		String[] variations = {
			"2017-01-20T10:00:00+0000",
			"2017-01-20T11:00:00+0100",
			"2017-01-20T09:00:00-0100",
			"2017-01-20T10:00:00+00:00",
			"2017-01-20T11:00:00+01:00",
			"2017-01-20T09:00:00-01:00",
			"2017-01-20T10:00:00+00",
			"2017-01-20T11:00:00+01",
			"2017-01-20T09:00:00-01",
			"2017-01-20T05:00:00 EST",
		};

		for(int i=0; i<variations.length; i++) {
			result = BasicLTIUtil.parseIMS8601(variations[i]);
			z = BasicLTIUtil.getISO8601(result);
			assertEquals(z, iso8601);
		}
	}

	@Test
	public void isRequest() {
		HttpServletRequest req = Mockito.mock(HttpServletRequest.class);

		//Nothing set = bad request
		boolean isReq = BasicLTIUtil.isRequest(req);
		assertFalse(isReq);

		//Set both things = good request
		Mockito.when(req.getParameter(LTI_MESSAGE_TYPE)).thenReturn(LTI_MESSAGE_TYPE_BASICLTILAUNCHREQUEST);
		Mockito.when(req.getParameter(LTI_VERSION)).thenReturn(LTI_VERSION_1);
		isReq = BasicLTIUtil.isRequest(req);
		assertTrue(isReq);

		//Bad message type = bad request
		Mockito.when(req.getParameter(LTI_MESSAGE_TYPE)).thenReturn("foobar");
		isReq = BasicLTIUtil.isRequest(req);
		assertFalse(isReq);

		//Bad version = bad request
		Mockito.when(req.getParameter(LTI_MESSAGE_TYPE)).thenReturn(LTI_MESSAGE_TYPE_BASICLTILAUNCHREQUEST);
		Mockito.when(req.getParameter(LTI_VERSION)).thenReturn("taco");
		isReq = BasicLTIUtil.isRequest(req);
		assertFalse(isReq);
	}

	@Test
	public void validateMessageIncorrectConsumerKey() {
		Map<String, String[]> paramMap = new HashMap<>();
		paramMap.put(OAuth.OAUTH_CONSUMER_KEY, new String[] {"zuul"});

		HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
		Mockito.when(req.getParameterMap()).thenReturn(paramMap);

		String url = "https://www.sakailms.org/";
		String secret = "shhh";
		String key = "zuul2";

		Object obj = BasicLTIUtil.validateMessage(req, url, secret, key);
		assertEquals("Incorrect consumer key zuul", obj);
	}

	@Test
	public void validateMessageNoBaseString() {
		Map<String, String[]> paramMap = new HashMap<>();
		paramMap.put(OAuth.OAUTH_CONSUMER_KEY, new String[] {"zuul"});


		HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
		Mockito.when(req.getParameterMap()).thenReturn(paramMap);

		String url = "https://www.sakailms.org/";
		String secret = "shhh";
		String key = "zuul";

		Object obj = BasicLTIUtil.validateMessage(req, url, secret, key);
		assertEquals("Unable to find base string", obj);
	}

	@Test
	public void validateGoodMessage() {
		Map<String, String> paramMap = new HashMap<>();

		String url = "https://www.sakailms.org/";
		String secret = "shhh";
		String key = "zuul";

		Map<String, String> signedParams_pre = BasicLTIUtil.signProperties(paramMap, url, OAuthMessage.POST, key, secret,
				"guid", "desc", "tool_url",
				"name", "email", null);

		//Need to convert from a <String, String> to a <String, String[]> so I can mock the HttpServletRequest
		Map<String, String[]> signedParams = signedParams_pre.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getKey,
						e -> new String[]{e.getValue()}));

		HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
		Mockito.when(req.getParameterMap()).thenReturn(signedParams);
		Mockito.when(req.getMethod()).thenReturn(OAuthMessage.POST);

		Object obj = BasicLTIUtil.validateMessage(req, url, secret, key);
		assertEquals(Boolean.TRUE, obj);
	}

	@Test
	public void validateDescriptorBad() throws Exception {
		String valid = BasicLTIUtil.validateDescriptor(null);
		assertNull(valid);

		valid = BasicLTIUtil.validateDescriptor("garbage");
		assertNull(valid);

		//Validate a descriptor that has no launch urls
		InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("descriptor_no_launch.xml");
		String fileContents = IOUtils.toString(resourceAsStream, "UTF-8");
		valid = BasicLTIUtil.validateDescriptor(fileContents);
		assertNull(valid);

		resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("descriptor_empty_base_tag.xml");
		fileContents = IOUtils.toString(resourceAsStream, "UTF-8");
		valid = BasicLTIUtil.validateDescriptor(fileContents);
		assertNull(valid);
	}

	@Test
	public void validateDescriptor() throws Exception {
		//Validate a descriptor with a secure launch
		InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("descriptor_secure.xml");
		String fileContents = IOUtils.toString(resourceAsStream, "UTF-8");
		String valid = BasicLTIUtil.validateDescriptor(fileContents);
		assertEquals("secure url to the basiclti launch URL", valid);

		//Validate a descriptor with a regular launch
		resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("descriptor.xml");
		fileContents = IOUtils.toString(resourceAsStream, "UTF-8");
		valid = BasicLTIUtil.validateDescriptor(fileContents);
		assertEquals("url to the basiclti launch URL", valid);
	}

	@Test(expected = IllegalArgumentException.class)
	public void adaptToCustomPropertyNameNull() {
		BasicLTIUtil.adaptToCustomPropertyName(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void adaptToCustomPropertyNameEmpty() {
		BasicLTIUtil.adaptToCustomPropertyName("");
	}

	@Test
	public void adaptToCustomPropertyName() {
		String newPropName = BasicLTIUtil.adaptToCustomPropertyName("foobar");
		assertEquals("custom_foobar", newPropName);
	}

	@Test
	public void parseDescriptorNulls() throws Exception {
		boolean parsed = BasicLTIUtil.parseDescriptor(new HashMap(), null, null);
		assertFalse(parsed);

		InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("descriptor_empty.xml");
		String descriptor = IOUtils.toString(resourceAsStream, "UTF-8");

		parsed = BasicLTIUtil.parseDescriptor(new HashMap(), new HashMap(), descriptor);
		assertFalse(parsed);
	}

	@Test
	public void parseDescriptorNoLaunch() throws Exception {
		InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("descriptor_no_launch.xml");
		String descriptor = IOUtils.toString(resourceAsStream, "UTF-8");

		Map<String, String> launchMap = new HashMap<>();
		Map<String, String> postPropMap = new HashMap<>();

		boolean parsed = BasicLTIUtil.parseDescriptor(launchMap, postPropMap, descriptor);
		assertFalse(parsed);
	}

	@Test
	public void parseDescriptor() throws Exception {
		InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("descriptor.xml");
		String descriptor = IOUtils.toString(resourceAsStream, "UTF-8");

		Map<String, String> launchMap = new HashMap<>();
		Map<String, String> postPropMap = new HashMap<>();

		boolean parsed = BasicLTIUtil.parseDescriptor(launchMap, postPropMap, descriptor);
		assertTrue(parsed);
	}

	@Test
	public void prepareForExport() throws Exception {
		String forExport = BasicLTIUtil.prepareForExport(null);
		assertNull(forExport);

		InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("descriptor_secure.xml");
		String descriptor = IOUtils.toString(resourceAsStream, "UTF-8");

		assertTrue(descriptor.contains("x-secure"));

		forExport = BasicLTIUtil.prepareForExport(descriptor);
		assertFalse(forExport.contains("x-secure"));

		resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("descriptor_empty.xml");
		descriptor = IOUtils.toString(resourceAsStream, "UTF-8");

		forExport = BasicLTIUtil.prepareForExport(descriptor);
		assertNull(forExport);
	}

	@Test
	public void mapKeyName() {
		String keyName = BasicLTIUtil.mapKeyName("Vendor:Chapter");
		assertEquals("vendor_chapter", keyName);

		keyName = BasicLTIUtil.mapKeyName("ASDF:1234&asdf.1qaz");
		assertEquals("asdf_1234_asdf_1qaz", keyName);

		keyName = BasicLTIUtil.mapKeyName("foobar");
		assertEquals("foobar", keyName);

		keyName = BasicLTIUtil.mapKeyName("El Ni·ño");
		assertEquals("el_ni_ño", keyName);

		keyName = BasicLTIUtil.mapKeyName(null);
		assertNull(keyName);

		keyName = BasicLTIUtil.mapKeyName("");
		assertNull(keyName);
	}

	@Test
	public void toNull() {
		String result = BasicLTIUtil.toNull(null);
		assertNull(result);

		result = BasicLTIUtil.toNull("");
		assertNull(result);

		result = BasicLTIUtil.toNull(" ");
		assertNull(result);

		result = BasicLTIUtil.toNull("   ");
		assertNull(result);

		result = BasicLTIUtil.toNull("foobar");
		assertNotNull(result);
		assertEquals("foobar", result);
	}

	@Test
	public void setProperty() {
		Map<String, String> theMap = new HashMap<>();

		BasicLTIUtil.setProperty(theMap, "foo", "bar");
		assertEquals(1, theMap.size());
		assertTrue(theMap.containsKey("foo"));
		assertEquals("bar", theMap.get("foo"));

		BasicLTIUtil.setProperty(theMap, "foo", "qwerty");
		assertEquals(1, theMap.size());
		assertTrue(theMap.containsKey("foo"));
		assertEquals("qwerty", theMap.get("foo"));

		BasicLTIUtil.setProperty(theMap, "foo", "");
		assertEquals(1, theMap.size());
		assertTrue(theMap.containsKey("foo"));
		assertEquals("qwerty", theMap.get("foo"));

		BasicLTIUtil.setProperty(theMap, "lms", "sakai");
		assertEquals(2, theMap.size());
		assertTrue(theMap.containsKey("lms"));
		assertEquals("sakai", theMap.get("lms"));

		BasicLTIUtil.setProperty(theMap, "", "?");
		assertEquals(3, theMap.size());
		assertTrue(theMap.containsKey(""));
		assertEquals("?", theMap.get(""));

		BasicLTIUtil.setProperty(theMap, "asdf", null);
		assertEquals(3, theMap.size());
		assertFalse(theMap.containsKey("asdf"));
		assertNull(theMap.get("asdf"));

		BasicLTIUtil.setProperty(theMap, null, "no boom");
		assertEquals(4, theMap.size());
		assertTrue(theMap.containsKey(null));
		assertEquals("no boom", theMap.get(null));
	}

	@Test
	public void htmlspecialchars() {
		String input = "<hi & \"bye' = ><hi & \"bye' = >";
		String result = BasicLTIUtil.htmlspecialchars(input);
		assertEquals("&lt;hi &amp; &quot;bye' = &gt;&lt;hi &amp; &quot;bye' = &gt;", result);
		String roundtrip = StringEscapeUtils.unescapeHtml4(result);
		assertEquals(input, roundtrip);

		input = "one line\r\ntwo line\r\n";
		result = BasicLTIUtil.htmlspecialchars(input);
		assertEquals("one line&#13;&#10;two line&#13;&#10;", result);
		roundtrip = StringEscapeUtils.unescapeHtml4(result);
		assertEquals(input, roundtrip);

		result = BasicLTIUtil.htmlspecialchars("nothing to see here");
		assertEquals("nothing to see here", result);

		result = BasicLTIUtil.htmlspecialchars(null);
		assertNull(result);
	}

	@Test
	public void convertToMap() {
		Properties props = new Properties();
		props.setProperty("foo", "bar");
		props.setProperty("lms", "Sakai");
		props.setProperty("", "something");

		Map<String, String> results = BasicLTIUtil.convertToMap(props);
		assertNotNull(results);
		assertEquals(3, results.size());
		assertEquals("bar", results.get("foo"));
		assertEquals("Sakai", results.get("lms"));
		assertEquals("something", results.get(""));
		assertNull(results.get("taco"));
	}

	@Test
	public void isBlank() {
		assertTrue(BasicLTIUtil.isBlank(null));
		assertTrue(BasicLTIUtil.isBlank(""));
		assertTrue(BasicLTIUtil.isBlank(" "));
		assertFalse(BasicLTIUtil.isBlank("bob"));
		assertFalse(BasicLTIUtil.isBlank(" bob "));
	}

	@Test
	public void isNotBlank() {
		assertFalse(BasicLTIUtil.isNotBlank(null));
		assertFalse(BasicLTIUtil.isNotBlank(""));
		assertFalse(BasicLTIUtil.isNotBlank(" "));
		assertTrue(BasicLTIUtil.isNotBlank("bob"));
		assertTrue(BasicLTIUtil.isNotBlank(" bob "));
	}

	@Test
	public void equals() {
		assertTrue(BasicLTIUtil.equals(null, null));
		assertFalse(BasicLTIUtil.equals(null, "abc"));
		assertFalse(BasicLTIUtil.equals("abc", null));
		assertTrue(BasicLTIUtil.equals("abc", "abc"));
		assertFalse(BasicLTIUtil.equals("abc", "ABC"));
	}

	@Test
	public void equalsIgnoreCase() {
		assertTrue(BasicLTIUtil.equalsIgnoreCase(null, null));
		assertFalse(BasicLTIUtil.equalsIgnoreCase(null, "abc"));
		assertFalse(BasicLTIUtil.equalsIgnoreCase("abc", null));
		assertTrue(BasicLTIUtil.equalsIgnoreCase("abc", "abc"));
		assertTrue(BasicLTIUtil.equalsIgnoreCase("abc", "ABC"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void postLaunchHtmlEmptyMap() {
		Map<String, String> props = new HashMap<>();
		String endpoint = "";
		String launchtext = "";
		Map<String, String> extra = new HashMap<>();
		BasicLTIUtil.postLaunchHTML(props, endpoint, launchtext, true, extra);
	}

	@Test(expected = IllegalArgumentException.class)
	public void postLaunchHtmlEmptyEndpoint() {
		Map<String, String> props = new HashMap<>();
		props.put("key", "value");
		String endpoint = null;
		String launchtext = "";
		Map<String, String> extra = new HashMap<>();
		BasicLTIUtil.postLaunchHTML(props, endpoint, launchtext, true, extra);
	}

	@Test
	public void postLaunchHtml() {
		Map<String, String> props = new HashMap<>();
		props.put("key", "value");
		props.put("null", null);
		String endpoint = "";
		String launchtext = "";
		Map<String, String> extra = new HashMap<>();
		extra.put("foo", "bar");
		extra.put("BaseString", "base string");
		String html = BasicLTIUtil.postLaunchHTML(props, endpoint, launchtext, false, extra);
		assertNotNull(html);

		html = BasicLTIUtil.postLaunchHTML(props, endpoint, launchtext, true, extra);
		assertNotNull(html);
	}

	@Test
	public void checkProperties() {
		Map<String, String> props = new HashMap<>();
		boolean checkedProperties = BasicLTIUtil.checkProperties(props, "https://www.sakailms.org/", "POST",
				"key", "secret");
		assertFalse(checkedProperties);

		String url = "https://www.sakailms.org/";
		String key = "key";
		String secret = "secret";
		Map<String, String> signedParams = BasicLTIUtil.signProperties(props, url, OAuthMessage.POST, key, secret,
				"guid", "desc", "tool_url",
				"name", "email", null);
		checkedProperties = BasicLTIUtil.checkProperties(signedParams, url, OAuthMessage.POST,
				key, secret);
		assertTrue(checkedProperties);
	}

	@Test
	public void signProperties() {
		Map<String, String> props = new HashMap<>();
		Map<String, String> extra = new HashMap<>();

		String url = "https://www.sakailms.org/";
		String key = "key";
		String secret = "secret";
		Map<String, String> signedParams = BasicLTIUtil.signProperties(props, url, OAuthMessage.POST, key, secret,
				"guid", "desc", "tool_url",
				"name", "email", extra);
		assertNotNull(signedParams);

		signedParams = BasicLTIUtil.signProperties(props, url, OAuthMessage.GET, null, secret,
				"guid", "desc", "tool_url",
				"name", "email", extra);
		assertNotNull(signedParams);
	}

	@Test
	public void testMergeCSV() {
		String retval = BasicLTIUtil.mergeCSV("1,2,3", null, "4");
		assertEquals(retval, "1,2,3,4");
		retval = BasicLTIUtil.mergeCSV("1,2", "3", "4");
		assertEquals(retval, "1,2,3,4");
		retval = BasicLTIUtil.mergeCSV("1,2", "1,2,3,4", "4");
		assertEquals(retval, "1,2,3,4");
		retval = BasicLTIUtil.mergeCSV("1,2", "1,2,3,4", null);
		assertEquals(retval, "1,2,3,4");
		retval = BasicLTIUtil.mergeCSV("1,2", "2,1,3,4", null);
		assertEquals(retval, "1,2,3,4");
	}

	@Test
	public void testShiftJVMDateToTimeZone() {
		TimeZone tz = TimeZone.getTimeZone("America/New_York");
		assertEquals(tz.getRawOffset(), -18000000);
		TimeZone tzla = TimeZone.getTimeZone("America/Los_Angeles");
		assertEquals(tzla.getRawOffset(), -28800000);
		TimeZone tzutc = TimeZone.getTimeZone("UTC");
		assertEquals(tzutc.getRawOffset(), 0);

		// If The JVM is Eastern and the due date is 3PM, when we send a UTC
		// time to a server that is in Mountain and we want it to display as
		// 3PM on the Mountain server, it will be sent as a two hour later UTC
		// Since we don't know the JVM timezone we shift a JVM date into Eastern
		// and Mountain and compare those values
		// The result is that both systems show the due date as 3PM server local time.
		// When you are a multi-tenant LTI tool with tenants in many time zones, and the LTI
		// tool sets a due date and then sends it to the upstream LMS - when the students
		// think it is due at 3PM - it needs to be shown as 2PM on both sysytems.
		// It does mean that late points are best computed in the controlling LMS, not the
		// LTI/SakaiPlus system
		Date dueDate = new Date("Wed Jan 18 15:00:00 2023");
		Date eastDate = BasicLTIUtil.shiftJVMDateToTimeZone(dueDate, "US/Eastern");
		Date mountainDate = BasicLTIUtil.shiftJVMDateToTimeZone(dueDate, "America/Denver");
		assertEquals(eastDate.getTime(), mountainDate.getTime()+2*60*60*1000);
	}


	@Test
	public void testEscapeNewlinesAndCarriageReturns() {
		String input = "This is a line with newlines.\r\nHere is another line.\rAnd another one.\n";
		String expected = "This is a line with newlines.\\r\\nHere is another line.\\rAnd another one.\\n";
		String actual = BasicLTIUtil.escapeSpecialCharacters(input);
		assertEquals(expected, actual);
	}

	@Test
	public void testEscapeTabsAndBackslash() {
		String input = "This \tis \ta \tstring \twith \t\ttabs \\and backslashes \\";
		String expected = "This \\tis \\ta \\tstring \\twith \\t\\ttabs \\\\and backslashes \\\\";
		String actual = BasicLTIUtil.escapeSpecialCharacters(input);
		assertEquals(expected, actual);
	}
}
