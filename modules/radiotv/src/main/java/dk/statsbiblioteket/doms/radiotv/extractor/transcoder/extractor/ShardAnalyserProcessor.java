package dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor;

import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.ProcessorChainElement;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.ProcessorException;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.ShardStructure;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.TranscodeRequest;
import dk.statsbiblioteket.doms.radiotv.extractor.updateidentifier.BroadcastExtractor;

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

    int gapToleranceSeconds = 2;


    @Override
    protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
        ShardStructure structure = new ShardStructure();
        request.setStructure(structure);
        findHolesAndOverlaps(request, config);
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
    SimpleDateFormat domsDateTime = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ssZ")      ;

    private void findHolesAndOverlaps(TranscodeRequest request, ServletConfig config) throws ProcessorException {

        List<TranscodeRequest.FileClip> clips = request.getClips();
        TranscodeRequest.FileClip clip1 = null;
        TranscodeRequest.FileClip clip2 = null;
        for (TranscodeRequest.FileClip clip: clips) {
            findStartStopTimes(clip, request);
            clip2 = clip1;
            clip1 = clip;
            if (clip2 != null) {
                 if ((clip2.getFileStartTime()-clip1.getFileEndTime()) > gapToleranceSeconds*1000L) {
                     ShardStructure.Hole hole = new ShardStructure.Hole();
                     hole.setFilePath1(clip1.getFilepath());
                     hole.setFilePath2(clip2.getFilepath());
                     hole.setHoleLength(clip2.getFileStartTime()-clip1.getFileEndTime());
                     request.getStructure().addHole(hole);
                 }  else
                     if ((clip1.getFileEndTime()-clip2.getFileStartTime()) > gapToleranceSeconds*1000L) {
                         ShardStructure.Overlap overlap = new ShardStructure.Overlap();
                         int overlapType;

                     }
            }
        }
    }



    /**
     * File names like:
     * mux1.1323327600-2011-12-08-08.00.00_1323331200-2011-12-08-09.00.00_dvb1-1.ts
     * dk4_399.250_S33-DK4_mpeg1_20111219045601_20111220045501_encoder2-2.mpeg
     *
     */
    Pattern muxPattern = Pattern.compile("mux.*[.]([0-9]*)-.*_([0-9]*)-.*_.*");
    Pattern bartPattern = Pattern.compile(".*([0-9]{14})_([0-9]{14}_.*)");
    SimpleDateFormat bartDateFormat = new SimpleDateFormat("yyyyMMddHHmmSS");
    private void findStartStopTimes(TranscodeRequest.FileClip clip, TranscodeRequest request) throws ProcessorException {
        String fileName = (new File(clip.getFilepath())).getName();
        Matcher muxMatcher = muxPattern.matcher(fileName);
        Matcher bartMatcher = bartPattern.matcher(fileName);
        try {
            if (muxMatcher.matches()) {
                clip.setFileStartTime(Long.parseLong(muxMatcher.group(1)));
                clip.setFileEndTime(Long.parseLong(muxMatcher.group(2)));
            } else if (bartMatcher.matches()) {
                clip.setFileStartTime(bartDateFormat.parse(bartMatcher.group(1)).getTime());
                clip.setFileEndTime(bartDateFormat.parse(bartMatcher.group(2)).getTime());
            } else {
                throw new ProcessorException("Could not parse filename '" + fileName + "'");
            }
        } catch (Exception e) {
            throw new ProcessorException(e);
        }
    }



}
