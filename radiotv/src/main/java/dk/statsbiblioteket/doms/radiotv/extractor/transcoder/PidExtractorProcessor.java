/* File:        $Id$
 * Revision:    $Revision$
 * Author:      $Author$
 * Date:        $Date$
 *
 * Copyright 2004-2009 Det Kongelige Bibliotek and Statsbiblioteket, Denmark
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package dk.statsbiblioteket.doms.radiotv.extractor.transcoder;

import dk.statsbiblioteket.doms.radiotv.extractor.ExternalJobRunner;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PidExtractorProcessor extends ProcessorChainElement {

    private static Logger log = Logger.getLogger(PidExtractorProcessor.class);
    private static final long clipSize = 100000000L;


    /**
     * For digitv broadcasts, detects and sets audio, video and dvbsub pids in the
     * request.
     * @param request
     * @param config
     * @throws ProcessorException
     */
    @Override
    protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
        Long blocksize = 1880L;
        Long blockcount = clipSize/blocksize;
        String filename = null;
        Integer program;
        Long offset = null;
        if (request.getClips().size() == 1) {
            TranscodeRequest.FileClip clip = request.getClips().get(0);
            program = clip.getProgramId();
            filename = clip.getFilepath();
            offset = (new File(filename)).length()/2L;
        } else {
            TranscodeRequest.FileClip clip = request.getClips().get(1);
            offset=0L;
            program = clip.getProgramId();
            filename = clip.getFilepath();
        }
        String command = null;
        switch(request.getClipType()) {
            case MUX:
                command = "dd if=" + filename + " "
                        + "bs=" + blocksize + " "
                        + "count=" + blockcount + " "
                        + "skip=" + offset/blocksize + " "
                        + "|ffmpeg -i - ";
                break;
            case MPEG1:
                return;
            case MPEG2:
                return;
            case WAV:
                return;
        }
        extractPidsFromFfmpeg(request, command);
        if (request.getAudioPids().isEmpty() || request.getVideoPid() == null || request.getDvbsubPid() == null) {
            if (request.getClipType() == ClipTypeEnum.MUX) {
                command = "ffmpeg -i " + filename;
                extractPidsFromFfmpeg(request, command);
            }
        }
        if (request.getAudioPids().isEmpty() || request.getVideoPid() == null || request.getDvbsubPid() == null) {
            if (request.getClipType() == ClipTypeEnum.MUX) {
                command = " vlc -vv  --no-audio --no-video --intf dummy --run-time 5  --play-and-exit " + filename;
                extractPidsFromVlc(request, command);
            }
        }
    }

    private void extractPidsFromVlc(TranscodeRequest request, String command) throws ProcessorException {
          log.info("Executing '" + command + "'");
        ExternalJobRunner runner;
        try {
            runner = new ExternalJobRunner(new String[]{"bash", "-c", command});
            log.debug("Command '" + command + "' returned with output '" + runner.getError());
        } catch (IOException e) {
            throw new ProcessorException(e);
        } catch (InterruptedException e) {
            throw new ProcessorException(e);
        } catch (ExternalProcessTimedOutException e) {
            throw new ProcessorException(e);
        }
        Pattern thisProgramPattern = Pattern.compile(".*program number="+request.getClips().get(0).getProgramId()+".*");
        Pattern programPattern = Pattern.compile(".*program number=.*");
        Pattern dvbsubPattern = Pattern.compile(".*pid=([0-9]*).*fcc=dvbs.*");
        Pattern videoPattern = Pattern.compile(".*pid=([0-9]*).*((fcc=mpgv)|(fcc=h264)).*");
        Pattern audioPattern1 = Pattern.compile(".*pid=([0-9]*).*((fcc=mp4a)|(fcc=mpga)).*");
        String[] commandOutput = runner.getError().split("\\n");
        boolean foundProgram = false;
        for (String line:commandOutput) {
            log.debug("Checking line '" + line + "'");
            if (foundProgram && programPattern.matcher(line).matches()) {
                log.debug("Found next program section, returning");
                return;
            }
            if (thisProgramPattern.matcher(line).matches()) {
                log.debug("Found requested program");
                foundProgram = true;
            }
            if (foundProgram) {
                Matcher dvbsubMatcher = dvbsubPattern.matcher(line);
                if (dvbsubMatcher.matches()){
                    request.setDvbsubPid(dvbsubMatcher.group(1));
                    log.info("Setting pid for dvbsub '" + dvbsubMatcher.group(1) + "'");
                }
                Matcher videoMatcher = videoPattern.matcher(line);
                if (videoMatcher.matches()) {
                    request.setVideoPid(videoMatcher.group(1));
                    log.info("Setting pid for video '" + videoMatcher.group(1) + "'");
                    if (line.contains("mpgv")) {
                        request.setVideoFcc("mpgv");
                    } else if (line.contains("h264")) {
                        request.setVideoFcc("h264");
                    }
                    log.debug("Identified video fourcc for " + request.getPid() + ": " + request.getVideoFcc());
                }
                Matcher audioMatcher = audioPattern1.matcher(line);
                if (audioMatcher.matches()) {
                    request.addAudioPid(audioMatcher.group(1));
                    log.info("Setting pid for audio '" + audioMatcher.group(1) + "'");
                    if (line.contains("mp4a")) {
                        request.setAudioFcc("mp4a");
                    } else if (line.contains("mpga")) {
                        request.setAudioFcc("mpga");
                    }
                    log.debug("Identified audio fourcc for " + request.getPid() + ": " + request.getAudioFcc());
                }
            }
        }
    }

    private void extractPidsFromFfmpeg(TranscodeRequest request, String command) throws ProcessorException {
        log.info("Executing '" + command + "'");
        ExternalJobRunner runner;
        try {
            runner = new ExternalJobRunner(new String[]{"bash", "-c", command});
            log.debug("Command '" + command + "' returned with output '" + runner.getError());
        } catch (IOException e) {
            throw new ProcessorException(e);
        } catch (InterruptedException e) {
            throw new ProcessorException(e);
        } catch (ExternalProcessTimedOutException e) {
            throw new ProcessorException(e);            
        }
        Pattern thisProgramPattern = Pattern.compile(".*Program\\s"+request.getClips().get(0).getProgramId()+".*");
        Pattern programPattern = Pattern.compile(".*Program.*");
        Pattern dvbsubPattern = Pattern.compile(".*Stream.*\\[(0x[0-9a-f]*)\\].*dvb.*sub.*");
        Pattern videoPattern = Pattern.compile(".*Stream.*\\[(0x[0-9a-f]*)\\].*Video.*DAR\\s(([0-9]*):([0-9]*)).*");
        Pattern audioPattern1 = Pattern.compile(".*Stream.*\\[(0x[0-9a-f]*)\\].*Audio.*");
        Pattern audioPattern2 = Pattern.compile(".*Stream.*\\[(0x[0-9a-f]*)\\].*0x0011.*");
        String[] commandOutput = runner.getError().split("\\n");
        boolean foundProgram = false;
        for (String line:commandOutput) {
            log.debug("Checking line '" + line + "'");
            if (foundProgram && programPattern.matcher(line).matches()) {
                log.debug("Found next program section, returning");
                return;
            }
            if (thisProgramPattern.matcher(line).matches()) {
                log.debug("Found requested program");
                foundProgram = true;
            }
            if (foundProgram) {
                Matcher dvbsubMatcher = dvbsubPattern.matcher(line);
                if (dvbsubMatcher.matches()){
                    request.setDvbsubPid(dvbsubMatcher.group(1));
                    log.info("Setting pid for dvbsub '" + dvbsubMatcher.group(1) + "'");
                }
                Matcher videoMatcher = videoPattern.matcher(line);
                if (videoMatcher.matches()) {
                    request.setVideoPid(videoMatcher.group(1));
                    log.info("Setting pid for video '" + videoMatcher.group(1) + "'");
                     if (line.contains("mpeg2video")) {
                        request.setVideoFcc("mpgv");
                    } else if (line.contains("h264")) {
                        request.setVideoFcc("h264");
                    }
                    log.debug("Identified video fourcc for " + request.getPid() + ": " + request.getVideoFcc());
                    request.setDisplayAspectRatioString(videoMatcher.group(2));
                    log.debug("Identified aspect ratio '" + request.getDisplayAspectRatioString() + "'");
                }
                Matcher audioMatcher = audioPattern1.matcher(line);
                if (audioMatcher.matches()) {
                    request.addAudioPid(audioMatcher.group(1));
                    log.info("Setting pid for audio '" + audioMatcher.group(1) + "'");
                     if (line.contains("aac_latm")) {
                        request.setAudioFcc("mp4a");
                    } else if (line.contains("mp2")) {
                        request.setAudioFcc("mpga");
                    }
                    log.debug("Identified audio fourcc for " + request.getPid() + ": " + request.getAudioFcc());
                }
                audioMatcher = audioPattern2.matcher(line);
                if (audioMatcher.matches()) {
                    request.addAudioPid(audioMatcher.group(1));
                    log.info("Setting pid for audio '" + audioMatcher.group(1) + "'");
                    request.setAudioFcc("mp4a");
                    log.debug("Identified audio fourcc for " + request.getPid() + ": " + request.getAudioFcc());
                }
            }
        }
    }
}