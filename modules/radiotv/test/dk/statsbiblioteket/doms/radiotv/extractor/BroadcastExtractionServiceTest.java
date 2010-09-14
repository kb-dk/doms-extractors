package dk.statsbiblioteket.doms.radiotv.extractor;

import java.util.HashMap;
import java.util.Map;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;

import junit.framework.TestCase;

public class BroadcastExtractionServiceTest extends TestCase {

	private final String baseUri = "http://localhost:9998/";
	private SelectorThread threadSelector; 

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		final Map<String, String> initParams = new HashMap<String, String>();
		initParams.put("com.sun.jersey.config.property.packages", "dk.statsbiblioteket.doms.radiotv.extractor");
		System.out.println("Starting grizzly...");
		threadSelector = GrizzlyWebContainerFactory.create(baseUri, initParams);
		System.out.println("Grizzly started.");
	}

	public void testDummyInterfaceProgramClipStatus() throws Exception {
		Client c = Client.create();
        WebResource r = c.resource(baseUri);
        String param = "DR";
        String responseMsg = r.path("/bes/statusofprogramclip?channel=DR").get(String.class);
        System.out.println("ResponseMsg: " + responseMsg);
		assertEquals("Request status for program clip. Channel: " + param, responseMsg);
	}

	public void testDummyInterfaceCreateProgramClip() throws Exception {
		Client c = Client.create();
        WebResource r = c.resource(baseUri);
        String responseMsg = r.path("/bes/createprogramclip").get(String.class);
		assertEquals("Request for generating program clip.", responseMsg);
	}

	public void testDummyInterfaceGetProgramClip() throws Exception {
		Client c = Client.create();
        WebResource r = c.resource(baseUri);
        String responseMsg = r.path("/bes/getprogramclip").get(String.class);
		assertEquals("Dette skal v�re en str�m af video...", responseMsg);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		System.out.println("Stopping grizzly...");
		threadSelector.stopEndpoint();
		System.out.println("Grizzly stopped.");
	}
}
