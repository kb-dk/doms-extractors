package dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor;

import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.ProcessorChainElement;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.ProcessorException;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.ShardStructure;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.TranscodeRequest;
import dk.statsbiblioteket.doms.radiotv.extractor.updateidentifier.BroadcastExtractor;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class ShardAnalyserProcessor extends ProcessorChainElement{

    private static Logger log = Logger.getLogger(ShardAnalyserProcessor.class);

    int gapToleranceSeconds = 2;


    @Override
    protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
        ShardStructure structure = new ShardStructure();
        request.setStructure(structure);
        findHolesAndOverlaps(request, config);
        findMissingStart(request, config);
        findMissingEnd(request, config);
        if (request.getStructure().isNonTrivial()) {
            log.debug(request.getStructure());
        }
    }

    /**
         * There are four overlap types:
         *  file1 |-----------------------------------------|
         *  file2                                |------------------------------------|
         *  type0                 [------------------------------------]
         *  type1                      [-------------]
         *  type2                                  [-----]
         *  type3                                        [----------------]
         */
    SimpleDateFormat domsDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")      ;

    private void findHolesAndOverlaps(TranscodeRequest request, ServletConfig config) throws ProcessorException {

        List<TranscodeRequest.FileClip> clips = request.getClips();
        TranscodeRequest.FileClip clip1 = null;
        TranscodeRequest.FileClip clip2 = null;
        for (TranscodeRequest.FileClip clip: clips) {
            findStartStopTimes(clip, request);
            clip1 = clip2;
            clip2 = clip;
            if (clip1 != null) {
                final long holeLength = clip2.getFileStartTime() - clip1.getFileEndTime();
                log.debug("Hole length = " + clip2.getFileStartTime() + " - " + clip1.getFileEndTime() + " = " + holeLength);
                final long overlapLength = 0 - holeLength;
                String filename1 = (new File(clip1.getFilepath())).getName();
                String filename2 = (new File(clip2.getFilepath())).getName();
                if (holeLength > gapToleranceSeconds*1000L) {
                     ShardStructure.Hole hole = new ShardStructure.Hole();
                     hole.setFilePath1(filename1);
                     hole.setFilePath2(filename2);
                     hole.setHoleLength(holeLength/1000);
                     request.getStructure().addHole(hole);
                 }  else
                     if (overlapLength > gapToleranceSeconds*1000L) {
                         ShardStructure.Overlap overlap = new ShardStructure.Overlap();
                         overlap.setFilePath1(filename1);
                         overlap.setFilePath2(filename2);
                         int overlapType = 0;
                         overlap.setOverlapLength(overlapLength/1000);
                         if (request.getProgramStartTime()<clip2.getFileStartTime() && request.getProgramEndTime()>clip1.getFileEndTime()) {
                             overlapType = 0;
                         } else if (request.getProgramStartTime()<clip2.getFileStartTime() && request.getProgramEndTime()<clip1.getFileEndTime() ) {
                             overlapType = 1;
                         } else if (request.getProgramStartTime()>clip2.getFileStartTime() && request.getProgramEndTime()<clip1.getFileEndTime()) {
                             overlapType = 2;
                         } else if (request.getProgramStartTime()>clip2.getFileStartTime() && request.getProgramEndTime()>clip1.getFileEndTime()) {
                             overlapType = 3;
                         }
                         overlap.setOverlapType(overlapType);
                         request.getStructure().addOverlap(overlap);
                     }
            }
        }
    }



    /**
     * File names like:
     * mux1.1323327600-2011-12-08-08.00.00_1323331200-2011-12-08-09.00.00_dvb1-1.ts
     * dk4_399.250_S33-DK4_mpeg1_20111219045601_20111220045501_encoder2-2.mpeg
     * drp2_103.000_DR-P2_pcm_20060331075502_20060401075002.wav
     *
     */
    Pattern muxPattern = Pattern.compile("mux.*[.]([0-9]*)-.*_([0-9]*)-.*_.*");
    Pattern bartPattern = Pattern.compile(".*([0-9]{14})_([0-9]{14}).*");
    SimpleDateFormat bartDateFormat = new SimpleDateFormat("yyyyMMddHHmmSS");
    private void findStartStopTimes(TranscodeRequest.FileClip clip, TranscodeRequest request) throws ProcessorException {
        String fileName = (new File(clip.getFilepath())).getName();
        Matcher muxMatcher = muxPattern.matcher(fileName);
        Matcher bartMatcher = bartPattern.matcher(fileName);
        try {
            if (muxMatcher.matches()) {
                clip.setFileStartTime(Long.parseLong(muxMatcher.group(1))*1000L);
                clip.setFileEndTime(Long.parseLong(muxMatcher.group(2))*1000L);
            } else if (bartMatcher.matches()) {
                final String startTimeElement = bartMatcher.group(1);
                final long startTime = bartDateFormat.parse(startTimeElement).getTime();
                log.debug("Setting file start time to '" + startTime + "' (" + startTimeElement + ")");
                clip.setFileStartTime(startTime);
                final String endTimeElement = bartMatcher.group(2);
                final long endTime = bartDateFormat.parse(endTimeElement).getTime();
                log.debug("Setting file end time to '" + endTime + "' (" + endTimeElement + ")");
                clip.setFileEndTime(endTime);
            } else {
                throw new ProcessorException("Could not parse filename '" + fileName + "'");
            }
        } catch (Exception e) {
            throw new ProcessorException(e);
        }
    }

    private void findMissingStart(TranscodeRequest request, ServletConfig config) {
        List<TranscodeRequest.FileClip> clips = request.getClips();
        int nclips = clips.size();
        TranscodeRequest.FileClip first = clips.get(0);
        final long startGap = first.getFileStartTime() - request.getProgramStartTime();
        log.debug("Start gap = " + first.getFileStartTime() + " - " + request.getProgramStartTime() +  " = " + startGap);
        if (startGap > gapToleranceSeconds*1000L) {
            ShardStructure.MissingStart start = new ShardStructure.MissingStart();
            start.setMissingSeconds((int) startGap/1000);
            request.getStructure().setMissingStart(start);
        }
    }

    private void findMissingEnd(TranscodeRequest request, ServletConfig config) {
        List<TranscodeRequest.FileClip> clips = request.getClips();
        int nclips = clips.size();
        TranscodeRequest.FileClip last = clips.get(nclips - 1);
        final long endGap = request.getProgramEndTime() - last.getFileEndTime();
        if (endGap > gapToleranceSeconds*1000L) {
            ShardStructure.MissingEnd end = new ShardStructure.MissingEnd();
            end.setMissingSeconds((int) endGap / 1000);
            request.getStructure().setMissingEnd(end);
        }
    }

}
