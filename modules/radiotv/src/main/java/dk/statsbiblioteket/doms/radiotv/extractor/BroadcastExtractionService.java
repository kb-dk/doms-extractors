package dk.statsbiblioteket.doms.radiotv.extractor;

import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.*;

import javax.servlet.ServletConfig;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;

/**
 * On receiving a request for a given shard url:
 * i) Parse the uuid out of the url.
 * ia) Look for the finished result
 * ii) Look for an in-progress transcoding by looking up the uuid in the in-memory map
 * iii) If it exists, calculate the progress and return a status for it
 * iv) If it does not exist, look for any files in the temp or failed dirs. If these are found, return a
 * failed status.
 * v) Otherwise, register the transcoding in the map, create the processor chain, and start it.
 *
 */
@Path("/bes")
public class BroadcastExtractionService {

    public final boolean dummyService=false;

    @Context ServletConfig config;

    @GET @Path("/getobjectstatus")
    @Produces(MediaType.APPLICATION_XML)
    public ObjectStatus getObjectStatus(@QueryParam("programpid") String programPid) throws ProcessorException, UnsupportedEncodingException {
        if (dummyService) {
            return getDummyObjectStatus(programPid);
        } else {
            return getRealObjectStatus(programPid);
        }
    }

    private ObjectStatus getRealObjectStatus(String programPid) throws ProcessorException, UnsupportedEncodingException {
        ObjectStatus status = StatusExtractor.getStatus(programPid, config);
        if (status != null) {
            return status;
        } else {
            String uuid = Util.getUuid(programPid);
            TranscodeRequest request = new TranscodeRequest(uuid);
            ClipStatus.getInstance().register(request);
            ProcessorChainElement transcoder = new TranscoderProcessor();
            ProcessorChainElement demuxer = new DemuxerProcessor();
            ProcessorChainElement estimator = new EstimatorProcessor();
            ProcessorChainElement parser = new ShardParserProcessor();
            ProcessorChainElement fetcher = new ShardFetcherProcessor();
            transcoder.setParentElement(demuxer);
            demuxer.setParentElement(estimator);
            estimator.setParentElement(parser);
            parser.setParentElement(fetcher);
            ProcessorChainThread thread = new ProcessorChainThread(transcoder, request, config);
            thread.start();
        }
        status = new ObjectStatus();
        status.setStatus(ObjectStatusEnum.STARTING);
        return status;
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
