
package lokalite.android.app;

//import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
//import java.io.InputStreamReader;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;	 
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import android.util.Log;

//import android.util.Log;

/**
 * 
 * @author Alexandra Boughton, Kim Nguyen, Zainab Aljaroudi, Jonathan Tsui
 *
 * @param <T>
 */

/**
 * HTTPConnect: Makes a connection to the server and parses JSON string into an object
 *
 */
public class HTTPConnect <T> {

	//Debug
	private static String TAG = "HTTPConnect";


	
	/**
	 * Connects to webserver and a Input strema containing
	 * the JSON string.
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static InputStream loadManySerialized(String url) throws IOException {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpContext localContext = new BasicHttpContext();
		HttpResponse response;
		InputStream instream = null;
		// Prepare a request object
		HttpGet httpget;
		url = url.replace(" ", "%20");
		httpget = new HttpGet(url);
		
		// Execute the request
		try {
			
			response = httpclient.execute(httpget, localContext);
			// Get hold of the response entity
			HttpEntity entity = response.getEntity();
			// If the response does not enclose an entity, 
			//there is no need to worry about connection release
			if (entity != null) {
				// A Simple Response Read
				instream = entity.getContent();
				//result = convertStreamToString(instream);

				// Closing the input stream will trigger connection release
				//instream.close();
			}
		} catch (ClientProtocolException e) {
			Log.i(TAG, "EXCEPTION: Caught a ClientProtocolException in loadManySerialize");
			e.printStackTrace();
		} 

		return instream;
	}
	
	
	/**
	 * Serializing a JSON string to an java object given an input stream
	 * @param <T>
	 * @param JSONString
	 * @param o
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static <T> T getObject(InputStream JSONString, Class<T> o) throws JsonParseException, JsonMappingException, IOException{
        ObjectMapper mapper = new ObjectMapper(); 
        T t = null;
        t = mapper.readValue(JSONString, o);
		return t;
		
	}
	
	
	/**
	 * Serializing a JSON string to an java object given an string containing
	 * @param <T>
	 * @param JSONString
	 * @param o
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static <T> T getObject(String JSONString, Class<T> o) throws JsonParseException, JsonMappingException, IOException{
        //String JSONString = null;
        ObjectMapper mapper = new ObjectMapper(); 
        T t = null;
        t = mapper.readValue(JSONString, o);
		return t;
	}
	
	
	/**
	 * Returns an array list of objects from a input stream containing a JSON String
	 * @param <T>
	 * @param JSONString
	 * @param o
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static <T> T getMultipleObjects(InputStream JSONString, TypeReference<T> o) throws JsonParseException, JsonMappingException, IOException{
        T t = null;
        ObjectMapper mapper = new ObjectMapper();
        t = mapper.readValue(JSONString, o);	
		return t;
	}
	
	/**
	 * Returns an array list of objects given a JSON string.
	 * @param <T>
	 * @param JSONString
	 * @param o
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static <T> T getMultipleObjects(String JSONString, TypeReference<T> o) throws JsonParseException, JsonMappingException, IOException{
        T t = null;
        ObjectMapper mapper = new ObjectMapper();
        t = mapper.readValue(JSONString, o);	
		return t;
	}
}
