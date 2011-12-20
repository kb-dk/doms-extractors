package dk.statsbiblioteket.doms.radiotv.extractor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.Util;
import org.apache.log4j.Logger;

import dk.statsbiblioteket.doms.central.CentralWebservice;
import dk.statsbiblioteket.doms.central.CentralWebserviceService;
import dk.statsbiblioteket.doms.central.InvalidCredentialsException;
import dk.statsbiblioteket.doms.central.InvalidResourceException;
import dk.statsbiblioteket.doms.central.MethodFailedException;

public class DomsClient {

    private static Logger log = Logger.getLogger(DomsClient.class);

	private static final String datastream = "SHARD_METADATA";
    private static final QName CENTRAL_WEBSERVICE_SERVICE = new QName(
    		"http://central.doms.statsbiblioteket.dk/",
    		"CentralWebserviceService");
    /**
     * Reference to the active DOMS webservice client instance.
     */
    private static CentralWebservice domsAPI;

    private static DomsClient singletonDomsClient;
    
    protected DomsClient(String domsWSAPIEndpointUrlString, String userName, String password) {
    	log.info("Creating DOMS Client");
    	log.info("domsWSAPIEndpointUrlString, " + domsWSAPIEndpointUrlString);
    	log.info("userName: " + userName);
    	log.info("password: ****");
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
		if (singletonDomsClient == null) {
            log.info("Initializing DOMS client");
			singletonDomsClient = new DomsClient(domsWSAPIEndpointUrlString, userName, password);
		} else {
            log.debug("DOMS client already initialised");
        }
    }

    public static synchronized CentralWebservice getDOMSApiInstance(ServletConfig config) {
        if (domsAPI == null) {
            String endPoint = Util.getInitParameter(config, Constants.DOMS_ENDPOINT);
            String username = Util.getInitParameter(config, Constants.DOMS_USERNAME);
            String password = Util.getInitParameter(config, Constants.DOMS_PASSWORD);
            initializeSingleton(endPoint, username, password);
        }
        return domsAPI;
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

    public static CentralWebservice getDomsAPI() {
        return domsAPI;
    }
}
