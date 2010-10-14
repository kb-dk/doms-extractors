package dk.statsbiblioteket.doms.radiotv.extractor;

import javax.servlet.ServletConfig;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("/bes")
public class BroadcastExtractionService {

    public final boolean dummyService=true;

    @Context ServletConfig config;

    @GET @Path("/getobjectstatus")
    @Produces(MediaType.APPLICATION_XML)
    public ObjectStatus getObjectStatus(@QueryParam("programpid") String programPid) {
        if (dummyService) {
            return getDummyObjectStatus(programPid);
        } else {
            readContextParameters();
            ObjectStatus status = new ObjectStatus();
            status.setStatus(ObjectStatusEnum.STARTING);
            status.setCompletionPercentage(0.0);
            return status;
        }
    }

    /**
     * This is a dummy method which reads initParams (for testing purposes)
     * @return
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String init() {
        readContextParameters();
        return "";
    }

    private void readContextParameters() {
        String tempdir = config.getInitParameter(Constants.TEMP_DIR_INIT_PARAM);
        Transcoder.tempdir = tempdir;
        String finaldir = config.getInitParameter(Constants.FINAL_DIR_INIT_PARAM);
        Transcoder.finaldir = finaldir;
    }


    static int dummyState = 0;
    private ObjectStatus getDummyObjectStatus(String pid) {
        ObjectStatus status = new ObjectStatus();
        if (dummyState%3 == 0) {
            status.setStatus(ObjectStatusEnum.STARTING);
        } else if (dummyState%3 == 1) {
            status.setStatus(ObjectStatusEnum.STARTED);
            status.setCompletionPercentage(50.0);
        } else if (dummyState%3 == 2) {
            status.setStatus(ObjectStatusEnum.DONE);
            status.setServiceUrl("rtmp://130.225.24.62/vod");
            status.setStreamId("mp4:drhd_2009-11-11_17-55-00.mp4");
        }
        dummyState++;
        return status;
    }


}
