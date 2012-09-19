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
package dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor;

import dk.statsbiblioteket.doms.radiotv.extractor.Constants;
import dk.statsbiblioteket.doms.radiotv.extractor.ExternalJobRunner;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.*;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor.FlashTranscoderProcessor;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import java.io.File;

public class DigitvTranscoderMuxClipper extends ProcessorChainElement {

    private static Logger log = Logger.getLogger(DigitvTranscoderMuxClipper.class);

    @Override
    protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
             this.seamlessClip(request, config);
     }

    private void seamlessClip(TranscodeRequest request, ServletConfig config) throws ProcessorException {
        Long additionalStartOffset = (request.getUserAdditionalStartOffset() + Long.parseLong(Util.getInitParameter(config, Constants.START_OFFSET_DIGITV)))*ClipTypeEnum.MUX.getBitrate();
        Long additionalEndOffset =  (request.getUserAdditionalEndOffset() + Long.parseLong(Util.getInitParameter(config, Constants.END_OFFSET_DIGITV)))*ClipTypeEnum.MUX.getBitrate();
        log.debug("Additonal Start Offset for '" + request.getPid() + "' :" + additionalStartOffset + "bytes");
        log.debug("Additonal End Offset for '" + request.getPid() + "' :" + additionalEndOffset + "bytes");        
        Long blocksize = 1880L;
        Long offsetBytes = 0L;
        String processSubstitutionFileList = "";
        final int clipSize = request.getClips().size();
        int programNumber = 0;
        log.debug("Number of clips: " + clipSize);
        for (int iclip = 0; iclip < clipSize; iclip++ ) {
            TranscodeRequest.FileClip clip = request.getClips().get(iclip);
            //final long fileLength = new File(clip.getFilepath()).length();  //This line has the side-effect of automounting the media file
            Long clipLength = clip.getClipLength();
            if (iclip == 0) { // First clip
                programNumber = clip.getProgramId();
                if (clip.getStartOffsetBytes() == null) {
                    offsetBytes = additionalStartOffset;
                } else {
                    offsetBytes = clip.getStartOffsetBytes() + additionalStartOffset;
                }
                if (offsetBytes == null || offsetBytes < 0) offsetBytes = 0L;
                if (clipLength != null && clipSize == 1) {
                    Long totalLengthBytes = clipLength - additionalStartOffset + additionalEndOffset;   //Program contained within file
                    processSubstitutionFileList += " <(dd if=" + clip.getFilepath() + " bs="+blocksize + " skip=" + offsetBytes/blocksize
                            + " count=" + totalLengthBytes/blocksize + ") " ;
                } else {         //Otherwise always go to end of file
                    processSubstitutionFileList += " <(dd if=" + clip.getFilepath() + " bs="+blocksize + " skip=" + offsetBytes/blocksize + ") " ;
                }

            } else if (iclip == clipSize - 1 && clipSize != 1) {   //last clip in multiclip program
                String skipString = "";
                if (clip.getStartOffsetBytes() != null && clip.getStartOffsetBytes() != 0L) {
                    log.warn("Found non-zero offset outside first clip for '" + request.getPid() + "'\n" + request.getShard());
                    skipString = " skip=" + (additionalStartOffset + clip.getStartOffsetBytes())/blocksize + " ";
                }
                if (clipLength != null) {
                    clipLength += additionalEndOffset;
                    processSubstitutionFileList +=" <(dd if=" + clip.getFilepath() + " bs=" + blocksize + skipString +  " count=" + clipLength/blocksize + ") ";
                } else {

                    processSubstitutionFileList +=" <(dd if=" + clip.getFilepath() + " bs=" + blocksize + skipString  + ") ";
                }
                //processSubstitutionFileList +=" <(dd if=" + clip.getFilepath() + " bs=" + blocksize + skipString +  " count=" + clipLength/blocksize + ") ";
            } else {   //A file in the middle of a program so take the whole file
                String skipString = "";
                if (clip.getStartOffsetBytes() != null && clip.getStartOffsetBytes() != 0L) {
                    log.warn("Found non-zero offset outside first clip for '" + request.getPid() + "'\n" + request.getShard());
                    skipString = " skip=" + clip.getStartOffsetBytes()/blocksize + " ";
                }
                processSubstitutionFileList += " <(dd if=" + clip.getFilepath() + " bs=" + blocksize + skipString + ") ";
            }
        }

        //If we have pids for video and audio and correspondig fourcc's then specify a custom pmt here. Otherwise fall back to using --program
        // e.g. (from poorly documented vlc options) --ts-extra-pmt=1010:1010=111:video=h264,121:audio=mp4a,135:spu=dvbs
        String programSelector = " --program=" + programNumber + " ";
        if (request.getVideoFcc() != null && request.getAudioFcc() != null) {
            programSelector = " --program=1010 --sout-all --ts-extra-pmt=1010:1010=" + request.getVideoPid() + ":video=" + request.getVideoFcc()
                    + "," + request.getMinimumAudioPid() + ":audio=" + request.getAudioFcc();
           if (request.getDvbsubPid() != null) {
               programSelector += "," + request.getDvbsubPid() + ":spu=dvbs";
           }
        }
        log.debug("Program selector for " + request.getPid() + ": '" + programSelector + "'");
        String vb = Util.getInitParameter(config, Constants.DIGITV_VIDEO_BITRATE);
        String ab = Util.getInitParameter(config, Constants.DIGITV_AUDIO_BITRATE);
        String clipperCommand = "cat " + processSubstitutionFileList + " | "
                + " vlc - " + programSelector + " --demux=ts --intf dummy --play-and-exit --noaudio --novideo --sout " +
                "'#transcode{vcodec=mp2v,vb=" + vb + ",soverlay,deinterlace,keyint=16,strict-rc,vfilter=canvas{width=720,height=576,aspect=" + request.getDisplayAspectRatioString() + "}," +
                "fps=25,audio-sync,acodec=mp2a,ab=" + ab + ",channels=2,samplerate=48000,threads=0}" +
                ":std{access=file,mux=ps,dst=" + OutputFileUtil.getDigitvWorkOutputFile(request, config) + "}' ";
        //This alternative clipper command is a workaround to  https://sbprojects.statsbiblioteket.dk/jira/browse/BES-74
        String clipperCommandAlternative = "cat " + processSubstitutionFileList + " | "
                        + " vlc - " + programSelector + " --demux=ts --intf dummy --play-and-exit --noaudio --novideo --sout " +
                        "'#std{access=file,mux=ts,dst=-}'|vlc - --demux=ts --intf dummy --play-and-exit --noaudio --novideo --sout " +
                "'#transcode{vcodec=mp2v,vb=" + vb + ",soverlay,deinterlace,keyint=16,strict-rc,vfilter=canvas{width=720,height=576,aspect=" + request.getDisplayAspectRatioString() + "}," +
                "fps=25,audio-sync,acodec=mp2a,ab=" + ab + ",channels=2,samplerate=48000,threads=0}" +
                ":std{access=file,mux=ps,dst=" + OutputFileUtil.getDigitvWorkOutputFile(request, config) + "}' ";
        if (request.isAlternative()) {
            log.debug("Using alternative clipper command");
            clipperCommand = clipperCommandAlternative;
        }
        log.debug("clipperCommand: " + clipperCommand);
        try {
            long timeout = Util.getTranscodingTimeout(config, request);
            log.debug("Setting transcoding timeout for '" + request.getPid() + "' to " + timeout + "ms" );
            ExternalJobRunner.runClipperCommand(timeout, clipperCommand);
        } catch (ExternalProcessTimedOutException e) {
            File outputFile =  OutputFileUtil.getFlashVideoOutputFile(request, config);
            log.warn("Deleting '" + outputFile.getAbsolutePath() + "'");
            outputFile.delete();
            throw new ProcessorException(e);
        }
    }



}
