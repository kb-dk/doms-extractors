package dk.statsbiblioteket.doms.radiotv.extractor;

import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.*;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor.ExtractionStatus;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor.ExtractionStatusExtractor;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor.FlashEstimatorProcessor;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor.FlashTranscoderProcessor;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.previewer.IdentifyLongestClipProcessor;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.previewer.PreviewGeneratorDispatcherProcessor;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.previewer.PreviewerStatus;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.previewer.PreviewerStatusExtractor;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.snapshotter.SnapshotGeneratorDispatcherProcessor;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.snapshotter.SnapshotPositionFinderProcessor;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.snapshotter.SnapshotStatus;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.snapshotter.SnapshotStatusExtractor;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

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

    private static final Logger log = Logger.getLogger(BroadcastExtractionService.class);

    public final boolean dummyService=false;

    @Context ServletConfig config;

    @GET @Path("/getobjectstatus")
    @Produces(MediaType.APPLICATION_XML)
    public ExtractionStatus getObjectStatus(@QueryParam("programpid") String programPid) throws ProcessorException, UnsupportedEncodingException {
        log.debug("Received transcoding request for program '" + programPid + "'");
        if (dummyService) {
            log.warn("Returning dummy status for program '" + programPid + "'");
            return getDummyObjectStatus(programPid);
        } else {
            return getRealObjectStatus(programPid);
        }
    }

    /**@GET @Path("/getsnapshotstatus")
    @Produces(MediaType.APPLICATION_XML) */
    public SnapshotStatus getSnapshotStatus(String programPid) throws ProcessorException, UnsupportedEncodingException {
        log.info("Received snapshot request for '" + programPid + "'");
        SnapshotStatus status = SnapshotStatusExtractor.getStatus(programPid, config);
        if (status != null) {
            return status;
        } else {
            String uuid = Util.getUuid(programPid);
            TranscodeRequest request = new TranscodeRequest(uuid);
            request.setServiceType(ServiceTypeEnum.THUMBNAIL_GENERATION);
            OutputFileUtil.getAndCreateOutputDir(request, config);
            RequestRegistry.getInstance().register(request);
            ProcessorChainElement fetcher = new ShardFetcherProcessor();
            ProcessorChainElement parser = new ShardParserProcessor();
            ProcessorChainElement snapshotFinder = new SnapshotPositionFinderProcessor();
            ProcessorChainElement dispatcher = new SnapshotGeneratorDispatcherProcessor();
            fetcher.setChildElement(parser);
            parser.setChildElement(snapshotFinder);
            snapshotFinder.setChildElement(dispatcher);
            ProcessorChainThread thread = ProcessorChainThread.getIterativeProcessorChainThread(fetcher, request, config);
            ProcessorChainThreadPool.addProcessorChainThread(thread);
            status = new SnapshotStatus();
            status.setStatus(ObjectStatusEnum.STARTING);
            return status;
        }
    }

    @GET @Path("/getsnapshotstatus")
    @Produces(MediaType.APPLICATION_XML)
    public List<SnapshotStatus> getSnapshotStatus(@QueryParam("programpid") List<String> pids) throws ProcessorException, UnsupportedEncodingException {
       List<SnapshotStatus> result = new ArrayList<SnapshotStatus>();
        for (String pid: pids) {
            result.add(getSnapshotStatus(pid));
        }
        return result;
    }

    @GET @Path("/getpreviewstatus")
    @Produces(MediaType.APPLICATION_XML)
    public PreviewerStatus getPreviewStatus(@QueryParam("programpid") String programPid) throws ProcessorException, UnsupportedEncodingException {
        log.info("Received preview request for '" + programPid + "'");        
        PreviewerStatus status = PreviewerStatusExtractor.getStatus(programPid, config);
        if (status != null) {
            return status;
        } else {
            String uuid = Util.getUuid(programPid);
            TranscodeRequest request = new TranscodeRequest(uuid);
            request.setServiceType(ServiceTypeEnum.PREVIEW_GENERATION);
            OutputFileUtil.getAndCreateOutputDir(request, config);
            RequestRegistry.getInstance().register(request);
            ProcessorChainElement fetcher = new ShardFetcherProcessor();
            ProcessorChainElement parser = new ShardParserProcessor();
            ProcessorChainElement aspecter = new AspectRatioDetectorProcessor();
            ProcessorChainElement pider = new PidExtractorProcessor();
            ProcessorChainElement longer = new IdentifyLongestClipProcessor();
            ProcessorChainElement dispatcher = new PreviewGeneratorDispatcherProcessor();
            fetcher.setChildElement(parser);
            parser.setChildElement(aspecter);
            aspecter.setChildElement(pider);
            pider.setChildElement(longer);
            longer.setChildElement(dispatcher);
            ProcessorChainThread thread = ProcessorChainThread.getIterativeProcessorChainThread(fetcher, request, config);
            ProcessorChainThreadPool.addProcessorChainThread(thread);
            status = new PreviewerStatus();
            status.setStatus(ObjectStatusEnum.STARTING);
            return status;
        }
    }
    

    private ExtractionStatus getRealObjectStatus(String programPid) throws ProcessorException, UnsupportedEncodingException {
        ExtractionStatus status = ExtractionStatusExtractor.getStatus(programPid, config);
        if (status != null) {
            return status;
        } else {
            String uuid = Util.getUuid(programPid);
            TranscodeRequest request = new TranscodeRequest(uuid);
            request.setServiceType(ServiceTypeEnum.BROADCAST_EXTRACTION);
            OutputFileUtil.getAndCreateOutputDir(request, config);
            RequestRegistry.getInstance().register(request);
            ProcessorChainElement transcoder = new FlashTranscoderProcessor();
            ProcessorChainElement estimator = new FlashEstimatorProcessor();
            ProcessorChainElement aspecter = new AspectRatioDetectorProcessor();
            ProcessorChainElement pider = new PidExtractorProcessor();
            ProcessorChainElement parser = new ShardParserProcessor();
            ProcessorChainElement fetcher = new ShardFetcherProcessor();
            transcoder.setParentElement(estimator);
            estimator.setParentElement(pider);
            pider.setParentElement(aspecter);
            aspecter.setParentElement(parser);
            parser.setParentElement(fetcher);
            ProcessorChainThread thread = ProcessorChainThread.getRecursiveProcessorChainThread(transcoder, request, config);
            //thread.start();
            ProcessorChainThreadPool.addProcessorChainThread(thread);
        }
        status = new ExtractionStatus();
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
        String tempdir = Util.getInitParameter(config, Constants.TEMP_DIR_INIT_PARAM);
        Transcoder.tempdir = tempdir;
        String finaldir = Util.getInitParameter(config, Constants.FINAL_DIR_INIT_PARAM);
        Transcoder.finaldir = finaldir;
    }


    static int dummyState = 0;
    private ExtractionStatus getDummyObjectStatus(String pid) {
        ExtractionStatus status = new ExtractionStatus();
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
