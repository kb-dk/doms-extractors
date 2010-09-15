package dk.statsbiblioteket.doms.radiotv.extractor;

import java.util.HashMap;
import java.util.Map;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
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
        WebResource r = c.resource(baseUri).path("/bes/statusofprogramclip").queryParam("channel", "DR");
        String responseMsg = r.get(String.class);
        System.out.println("ResponseMsg: " + responseMsg);
		assertNotNull(responseMsg);
        assertNotSame("", responseMsg);
        assertTrue(responseMsg.contains("DR"));
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

    public void testGetObjectStatus() throws Exception {
        ClientConfig cc =new DefaultClientConfig();
        cc.getClasses().add(JsonBodyWriter.class);
        Client c = Client.create(cc);
        WebResource r = c.resource(baseUri).path("/bes/getobjectstatus").queryParam("programpid", "12345");
        ObjectStatus status = r.get(ObjectStatus.class);
        assertTrue(status.getUrl().contains("URL"));
    }


	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		System.out.println("Stopping grizzly...");
		threadSelector.stopEndpoint();
		System.out.println("Grizzly stopped.");
	}
}
