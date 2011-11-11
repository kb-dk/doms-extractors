package dk.statsbiblioteket.doms.radiotv.extractor.transcoder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.apache.log4j.Logger;
import org.junit.Before;

import dk.statsbiblioteket.doms.central.CentralWebservice;
import dk.statsbiblioteket.doms.central.CentralWebserviceService;
import dk.statsbiblioteket.doms.central.InvalidCredentialsException;
import dk.statsbiblioteket.doms.central.InvalidResourceException;
import dk.statsbiblioteket.doms.central.MethodFailedException;
import dk.statsbiblioteket.doms.radiotv.extractor.Constants;

public class DomsClient {

    private static Logger log = Logger.getLogger(DomsClient.class);

	private static final String datastream = "SHARD_METADATA";
    private static final QName CENTRAL_WEBSERVICE_SERVICE = new QName(
    		"http://central.doms.statsbiblioteket.dk/",
    		"CentralWebserviceService");
    /**
     * Reference to the active DOMS webservice client instance.
     */
    private CentralWebservice domsAPI;

    private static DomsClient singletonDomsClient;
    
    protected DomsClient(String domsWSAPIEndpointUrlString, String userName, String password) {
    	log.debug("Creating DOMS Client");
    	log.debug("domsWSAPIEndpointUrlString, " + domsWSAPIEndpointUrlString);
    	log.debug("userName: " + userName);
    	log.debug("password: " + password);
    	URL domsWSAPIEndpoint;
		try {
			domsWSAPIEndpoint = new URL(domsWSAPIEndpointUrlString);
		} catch (MalformedURLException e) {
			throw new RuntimeException("URL to DOMS not configured correctly. Was: " + domsWSAPIEndpointUrlString, e);
		}
    	domsAPI = new CentralWebserviceService(domsWSAPIEndpoint, CENTRAL_WEBSERVICE_SERVICE).getCentralWebservicePort();
    	Map<String, Object>  domsAPILogin = ((BindingProvider) domsAPI).getRequestContext();
    	domsAPILogin.put(BindingProvider.USERNAME_PROPERTY, userName);
    	domsAPILogin.put(BindingProvider.PASSWORD_PROPERTY, password);
    }

    public static synchronized void initializeSingleton(String domsWSAPIEndpointUrlString, String userName, String password) {
    	log.debug("Initializing DOMS client");
		if (singletonDomsClient == null) {
			singletonDomsClient = new DomsClient(domsWSAPIEndpointUrlString, userName, password);
		}
    }
    
    public static synchronized DomsClient getSingletonDomsClient() {
		if (singletonDomsClient == null) {
			throw new RuntimeException("Singleton not initialized before attempted use.");
		}
		return singletonDomsClient;
	}
    
    public String getShard(String pid) {
		try {
			//if (!pid.startsWith("uuid:")) {
				
				//throw new IllegalArgumentException("The pid is expected to start with \"uuid:\"");
			//}
			return domsAPI.getDatastreamContents("uuid:" + pid, datastream);
		} catch (InvalidCredentialsException e) {
			throw new RuntimeException("Wrong credentials in property file.", e);
		} catch (InvalidResourceException e) {
			throw new RuntimeException("Wrong resource in property file.", e);
		} catch (MethodFailedException e) {
			throw new RuntimeException("Something went wrong.", e);
		}
    }

}
