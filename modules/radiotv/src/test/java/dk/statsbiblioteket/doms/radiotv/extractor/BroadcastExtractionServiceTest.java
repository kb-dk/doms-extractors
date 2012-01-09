package dk.statsbiblioteket.doms.radiotv.extractor;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor.ExtractionStatus;

public class BroadcastExtractionServiceTest extends GrizzlyTestCase {

	@Override
    protected void setUp() throws Exception {
            super.setUp();
    }



    public void testGetObjectStatus() throws Exception {
        ClientConfig cc =new DefaultClientConfig();
        Client c = Client.create(cc);
        WebResource r = c.resource(baseUri).path("/bes/getobjectstatus").queryParam("programpid", "12345");
        ExtractionStatus status = r.get(ExtractionStatus.class);
        assertTrue(status.getStatus().equals(ObjectStatusEnum.STARTING));
    }

    public void testParameterSet() {
         ClientConfig cc =new DefaultClientConfig();
        Client c = Client.create(cc);
        WebResource r = c.resource(baseUri).path("/bes");
        String output = r.get(String.class);
        assertTrue(Transcoder.tempdir.equals("tempdir"));
        assertTrue(Transcoder.finaldir.equals("clips"));
    }

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

	}
}
