package dk.statsbiblioteket.doms.radiotv.extractor;

import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.*;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor.DigitvEstimatorProcessor;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor.DigitvExtractionStatus;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor.DigitvFileMoverTranscoderProcessor;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor.DigitvTranscoderDispatcherProcessor;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor.ExtractionStatus;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor.DigitvExtractionStatusExtractor;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor.ExtractionStatusExtractor;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor.FlashEstimatorProcessor;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor.FlashTranscoderProcessor;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor.DigitvJobLinkEmailProcessor;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor.ShardAnalyserProcessor;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor.ShardEnricherProcessor;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor.ShardFixerProcessor;
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
import javax.servlet.ServletContext;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    public static final String besVersion = "2.0.2";

    public final boolean dummyService=false;

    @Context 
    ServletConfig config;
    @Context 
    UriInfo uriInfo;


    @GET @Path("/getobjectstatus")
    @Produces(MediaType.APPLICATION_XML)
    public ExtractionStatus getObjectStatus(@QueryParam("programpid") String programPid, @QueryParam("title") String title, @QueryParam("channel") String channel, @QueryParam("date") long startTime) throws ProcessorException, UnsupportedEncodingException {
        logIncomingRequest("transcoding", programPid, title, channel, startTime);
        if (dummyService) {
            log.warn("Returning dummy status for program '" + programPid + "'");
            return getDummyObjectStatus(programPid);
        } else {
            return getRealObjectStatusIteratively(programPid);
        }
    }

    @GET @Path("/forcetranscode")
    @Produces(MediaType.APPLICATION_XML)
    public ExtractionStatus forceTranscode(@QueryParam("programpid") String programPid, @QueryParam("title") String title, @QueryParam("channel") String channel, @QueryParam("date") long startTime) throws ProcessorException, UnsupportedEncodingException {
        logIncomingRequest("forced transcoding", programPid, title, channel, startTime);
        ExtractionStatus status = ExtractionStatusExtractor.getStatus(programPid, config);
        if (status != null && status.getStatus() == ObjectStatusEnum.DONE) {
            TranscodeRequest request = new TranscodeRequest(Util.getUuid(programPid));
            request.setServiceType(ServiceTypeEnum.BROADCAST_EXTRACTION);
            File outputFile = OutputFileUtil.getExistingMediaOutputFile(request, config);
            boolean deleted = outputFile.delete();
            log.info("Deleting '" + outputFile.getAbsolutePath() + "'");
            if (deleted) {
                log.debug("Deleted '" + outputFile.getAbsolutePath() + "'");
            } else {
                log.warn("Failed to delete '" + outputFile.getAbsolutePath() + "'");
            }
        }
        return getObjectStatus(programPid, title,  channel, startTime);
    }


    private static void logIncomingRequest(String requestType, String programPid, String title, String channel, long startTime) {
        log.info("Received " + requestType + " request for program '" + programPid + "'");
        if (title != null || channel != null) {
            log.info("This corresponds to '" + title + "' on  '" + channel + "'");
        }
        if (startTime != 0L) {
            Date startDate = new Date();
            startDate.setTime(startTime);
            log.info("Start time is '" + startDate + "'");
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

    @GET @Path("/forcesnapshot")
    @Produces(MediaType.APPLICATION_XML)
    public List<SnapshotStatus> forceSnapshot(@QueryParam("programpid") List<String> pids) throws ProcessorException, UnsupportedEncodingException {
        List<SnapshotStatus> result = new ArrayList<SnapshotStatus>();
        for (String pid: pids) {
            result.add(forceSnapshot(pid));
        }
        return result;
    }

    private SnapshotStatus forceSnapshot(String pid) throws ProcessorException, UnsupportedEncodingException {
        log.info("Received forcing snapshot request for '" + pid + "'");
        SnapshotStatus status = SnapshotStatusExtractor.getStatus(pid, config);
        if (status != null && status.getStatus() == ObjectStatusEnum.DONE) {
            TranscodeRequest request = new TranscodeRequest(Util.getUuid(pid));
            request.setServiceType(ServiceTypeEnum.THUMBNAIL_GENERATION);
            File[] files = OutputFileUtil.getAllSnapshotFiles(config, request);
            for (File file: files) {
                log.info("Deleting '" + file.getAbsolutePath() + "'");
                if (file.delete()) {
                    log.debug("Deleted '" + file.getAbsolutePath() + "'");
                }
            }
        }
        return getSnapshotStatus(pid);
    }


    @GET @Path("/getpreviewstatus")
    @Produces(MediaType.APPLICATION_XML)
    public PreviewerStatus getPreviewStatus(@QueryParam("programpid") String programPid, @QueryParam("title") String title, @QueryParam("channel") String channel, @QueryParam("date") long startTime) throws ProcessorException, UnsupportedEncodingException {
        logIncomingRequest("preview", programPid, title, channel, startTime);
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

    @GET @Path("/forcepreview")
    @Produces(MediaType.APPLICATION_XML)
    public PreviewerStatus forcePreview(@QueryParam("programpid") String programPid, @QueryParam("title") String title, @QueryParam("channel") String channel, @QueryParam("date") long startTime) throws ProcessorException, UnsupportedEncodingException {
        logIncomingRequest("Force preview", programPid, title, channel, startTime);
        PreviewerStatus status = PreviewerStatusExtractor.getStatus(programPid, config);
        if (status != null && status.getStatus() == ObjectStatusEnum.DONE) {
            String uuid = Util.getUuid(programPid);
            TranscodeRequest request = new TranscodeRequest(uuid);
            request.setServiceType(ServiceTypeEnum.PREVIEW_GENERATION);
            File file = OutputFileUtil.getExistingMediaOutputFile(request, config);
            log.info("Deleting '" + file.getAbsolutePath() + "'");
            if (file.delete()) {
                log.debug("Deleted '" + file.getAbsolutePath() + "'");
            } else {
                log.warn("Failed to delete '" + file.getAbsolutePath() + "'");
            }
        }
        return getPreviewStatus(programPid, title, channel, startTime);
    }

    @GET @Path("/digitv_transcode")
    @Produces(MediaType.APPLICATION_XML)
    public DigitvExtractionStatus startDigitvTranscoding(
    		@QueryParam("programpid") String programPid, 
    		@QueryParam("title") String title, 
    		@QueryParam("channel") String channel, 
    		@QueryParam("date") long startTime, 
    		@QueryParam("additional_start_offset") long additionalStartOffset, 
    		@QueryParam("additional_end_offset") long additionalEndOffset, 
    		@QueryParam("filename_prefix") String filenamePrefix,
    		@DefaultValue("true") @QueryParam("send_email") String sendEmailParam) throws ProcessorException, UnsupportedEncodingException { 
    	logIncomingRequest("digitv_transcode", programPid, title, channel, startTime);
    	String domsProgramPid = Util.getUuid(programPid);
    	String filenamePrefixURLDecoded = URLDecoder.decode(filenamePrefix, "UTF-8");
    	boolean sendEmail = ((sendEmailParam == null) || (sendEmailParam.equalsIgnoreCase("true")));
    	log.info("BES version: " + besVersion);
    	log.info("Transcode request set filename: " + filenamePrefixURLDecoded);
    	log.info("Transcode request set user defined additional start offset: " + additionalStartOffset);
    	log.info("Transcode request set user defined additional end offset: " + additionalEndOffset);
    	log.info("Transcode request set service type: " + ServiceTypeEnum.DIGITV_BROADCAST_EXTRACTION);
    	log.info("Transcode request set doms program pid: " + domsProgramPid);
    	log.info("Transcode request set send email: " + sendEmail);
    	TranscodeRequest request = new TranscodeRequest(domsProgramPid, additionalStartOffset, additionalEndOffset, filenamePrefixURLDecoded);
    	DigitvExtractionStatus status = DigitvExtractionStatusExtractor.getStatus(request, config);
    	if (status != null) {
    		return status;
    	} else {
    		log.info("Starting transcoding of program: " + programPid);
    		RequestRegistry.getInstance().register(request);
    		ProcessorChainElement sendJobEmail = new DigitvJobLinkEmailProcessor(uriInfo.getRequestUri().toString(), sendEmail);
    		ProcessorChainElement programPidExtracter = new ShardPidFromProgramPidFetcherProcessor();
    		ProcessorChainElement fetcher = new ShardFetcherProcessor();
    		ProcessorChainElement parser = new ShardParserProcessor();
            ProcessorChainElement aspecter = new AspectRatioDetectorProcessor();
    		ProcessorChainElement pider = new PidExtractorProcessor();
    		ProcessorChainElement estimator = new DigitvEstimatorProcessor();
    		ProcessorChainElement transcoder = new DigitvTranscoderDispatcherProcessor();
    		ProcessorChainElement mover = new DigitvFileMoverTranscoderProcessor();
    		sendJobEmail.setChildElement(programPidExtracter);
    		programPidExtracter.setChildElement(fetcher);
    		fetcher.setChildElement(parser);
    		parser.setChildElement(aspecter);
            aspecter.setChildElement(pider);
    		pider.setChildElement(estimator);
    		estimator.setChildElement(transcoder);
    		transcoder.setChildElement(mover);
    		ProcessorChainThread thread = ProcessorChainThread.getIterativeProcessorChainThread(sendJobEmail, request, config);
    		ProcessorChainThreadPool.addProcessorChainThread(thread);

    	}
    	status = new DigitvExtractionStatus();
    	status.setStatus(ObjectStatusEnum.STARTING);

        return status;
    }


    @GET @Path("/analyse")
    @Produces(MediaType.TEXT_PLAIN)
    public String analyseShard(@QueryParam("programpid") String programPid, @QueryParam("title") String title, @QueryParam("channel") String channel, @QueryParam("date") long startTime) throws ProcessorException {
        logIncomingRequest("Shard Analysis",programPid, title, channel, startTime);
        String uuid = null;
        try {
            uuid = Util.getUuid(programPid);
        } catch (UnsupportedEncodingException e) {
            throw new ProcessorException(e);
        }
        TranscodeRequest request = new TranscodeRequest(uuid);
        request.setServiceType(ServiceTypeEnum.SHARD_ANALYSIS);
        RequestRegistry.getInstance().register(request);
        ProcessorChainElement fetcher = new ShardFetcherProcessor();
        ProcessorChainElement parser = new ShardParserProcessor();
        ProcessorChainElement pbcorer = new PBCoreParserProcessor();
        ProcessorChainElement analyser = new ShardAnalyserProcessor();
        ProcessorChainElement enricher = new ShardEnricherProcessor();
        fetcher.setChildElement(parser);
        parser.setChildElement(pbcorer);
        pbcorer.setChildElement(analyser);
        analyser.setChildElement(enricher);
        ProcessorChainThread thread = ProcessorChainThread.getIterativeProcessorChainThread(fetcher, request, config);
        ProcessorChainThreadPool.addProcessorChainThread(thread);
        return "Queued Shard Analysis of '" + uuid + "'";
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

    private ExtractionStatus getRealObjectStatusIteratively(String programPid) throws ProcessorException, UnsupportedEncodingException {
         ExtractionStatus status = ExtractionStatusExtractor.getStatus(programPid, config);
        if (status != null) {
            return status;
        } else {
            String uuid = Util.getUuid(programPid);
            TranscodeRequest request = new TranscodeRequest(uuid);
            request.setServiceType(ServiceTypeEnum.BROADCAST_EXTRACTION);
            OutputFileUtil.getAndCreateOutputDir(request, config);
            RequestRegistry.getInstance().register(request);
            ProcessorChainElement fetcher = new ShardFetcherProcessor();
            ProcessorChainElement parser = new ShardParserProcessor();
            ProcessorChainElement pbcorer = new PBCoreParserProcessor();
            ProcessorChainElement analyser = new ShardAnalyserProcessor();
            ProcessorChainElement enricher = new ShardEnricherProcessor();
            ProcessorChainElement fixer = new ShardFixerProcessor();
            ProcessorChainElement pider = new PidExtractorProcessor();
            ProcessorChainElement aspecter = new AspectRatioDetectorProcessor();
            ProcessorChainElement estimator = new FlashEstimatorProcessor();
            ProcessorChainElement transcoder = new FlashTranscoderProcessor();
            fetcher.setChildElement(parser);
            parser.setChildElement(pbcorer);
            pbcorer.setChildElement(analyser);
            analyser.setChildElement(enricher);
            enricher.setChildElement(fixer);
            fixer.setChildElement(pider);
            pider.setChildElement(aspecter);
            aspecter.setChildElement(estimator);
            estimator.setChildElement(transcoder);
            ProcessorChainThread thread = ProcessorChainThread.getIterativeProcessorChainThread(fetcher, request, config);
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
