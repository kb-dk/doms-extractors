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

import dk.statsbiblioteket.doms.radiotv.extractor.Constants;
import dk.statsbiblioteket.doms.radiotv.extractor.ExternalJobRunner;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import java.io.File;
import java.io.IOException;

public class DemuxerProcessor extends ProcessorChainElement {

    private static final int previewHeight = 240;

    private static Logger log = Logger.getLogger(DemuxerProcessor.class);

    /**
     * Takes the list of clips in the clips field of the request and
     * creates a single transport-stream file from them. This method
     * blocks during the demuxing process.
     * Side-effect: the temporary directory is created if it doesn't already exist.
     * @param request Pre-condition is that the field "clips" in the
     */
    @Override
    protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {

        createTempDirectory(request, config);

        switch (request.getClipType()) {
            case MUX:
                if (config.getInitParameter(Constants.DEMUXER_ALGORITHM).equals("seamless")) {
                    seamlessClip(request, config);
                } else {
                    log.warn("Using naive clipping. This is deprecated.");
                    naiveClip(request, config);
                }
                break;
             case MPEG1:
                 mpegClip(request, config);
                 break;
             case MPEG2:
                 mpegClip(request, config);
                 break;
             case WAV:
                 throw new ProcessorException("WAV clipping not yet implemented");
        }


        if ("true".equals(config.getInitParameter(Constants.RELEASE_AFTER_DEMUX))) {
            Util.unlockRequest(request);
        }
    }

    private void createTempDirectory(TranscodeRequest request, ServletConfig config) throws ProcessorException {
        String outputDir = config.getInitParameter(Constants.TEMP_DIR_INIT_PARAM);
        File outputDirFile = new File(outputDir);
        if (outputDirFile.mkdirs()) {
            log.info("Created directory '" + outputDirFile.getAbsolutePath() + "'");
        }
        File outputFile = Util.getDemuxFile(request, config);
        if (outputFile.exists()) {
            throw new ProcessorException("Output file '" + outputFile.getAbsolutePath() + "' already exists. This shouldn't happen");
        }
    }

    private void mpegClip(TranscodeRequest request, ServletConfig config) throws ProcessorException {
        File outputFile = Util.getPreviewFile(request, config);
        long blocksize = 1880L;
        final int clipSize = request.getClips().size();
        if (clipSize > 1) throw new ProcessorException("Haven't implemented mpeg mulitclipping yet");
        TranscodeRequest.FileClip clip = request.getClips().get(0);
        String start = "";
        if (clip.getStartOffsetBytes() != null && clip.getStartOffsetBytes() != 0L) start = "skip=" + clip.getStartOffsetBytes()/blocksize;
        String length = "";
        if (clip.getClipLength() != null && clip.getClipLength() != 0L) length = "count=" + clip.getClipLength()/blocksize;
        String command = "dd if=" + clip.getFilepath() + " bs=" + blocksize + " " + start + " " + length + "|ffmpeg -i - "
                + " -ar 44100 -f flv -vcodec flv -y "
                + " " + getFfmpegAspectRatio(request) + " "
                + " " + "-b 200000 -ab 96000 -g 160 -cmp dct -subcmp dct -mbd 2 -flags +aic+cbp+mv0+mv4 -trellis 1 -ac 1 -deinterlace " + " "
                + outputFile.getAbsolutePath();
        runClipperCommand(command);
    }



    /**
     * Seamless clipping works by concatenating all the files first and then cutting the concatenated
     * stream before demuxing. It is assumed that the startOffset is zero in every file except the
     * first and that the length is equal to the file length minus startOffset for the first clip and
     * then zero for every clip but the last. These constraints are not checked.
     * @param request
     */
    private void seamlessClip(TranscodeRequest request, ServletConfig config) throws ProcessorException {
        File outputFile = Util.getDemuxFile(request, config);
        Long offsetBytes = 0L;
        Long totalLengthBytes = 0L;
        String fileList = "";
        final int clipSize = request.getClips().size();
        int programNumber = 0;
        for (int iclip = 0; iclip < clipSize; iclip++ ) {
            TranscodeRequest.FileClip clip = request.getClips().get(iclip);
            final long fileLength = new File(clip.getFilepath()).length();  //This line has the side-effect of automounting the media file
            Long clipLength = clip.getClipLength();
            fileList += " " + clip.getFilepath() + " ";
            if (iclip == 0) {
                programNumber = clip.getProgramId();
                offsetBytes = clip.getStartOffsetBytes();
                if (offsetBytes == null) offsetBytes = 0L;
                if (clipLength != null && clipSize == 1) {
                    totalLengthBytes = clipLength;   //Program contained within file
                } else {         //Otherwise always go to end of file
                    totalLengthBytes = fileLength - offsetBytes;
                }
            } else if (iclip == clipSize - 1 && clipSize != 1) {   //last clip in multiclip program
                if (clipLength != null) {
                    totalLengthBytes += clip.getClipLength();
                } else {
                    totalLengthBytes += fileLength;
                }
            } else {   //A file in the middle of a program so take the whole file
                totalLengthBytes += fileLength;
            }
        }
        Long blocksize = 1880L;
        /*       String clipperCommand = "cat " + fileList + " | dd bs=" + blocksize
   + " skip=" + offsetBytes/blocksize + " count=" + totalLengthBytes/blocksize
   + " | vlc - --program=" + programNumber + " --demux=ts --intf dummy --play-and-exit --noaudio --novideo " +
   "--sout '#std{access=file,mux=ts,dst=" + outputFile.getAbsolutePath() + "}'";*/

        String ffmpegResolution = getFfmpegAspectRatio(request);
        String clipperCommand = "cat " + fileList + " | dd bs=" + blocksize
                + " skip=" + offsetBytes/blocksize + " count=" + totalLengthBytes/blocksize
                + " | vlc - --program=" + programNumber + " --demux=ts --intf dummy --play-and-exit --noaudio --novideo "
                + "--sout '#duplicate{dst=std{access=file, mux=ts, dst=-},dst=std{access=file,mux=ts,dst=" + outputFile.getAbsolutePath() + "}}' |"
                + "ffmpeg -i - -b 200 " + ffmpegResolution + " -ar 44100 " + Util.getPreviewFile(request, config);


        runClipperCommand(clipperCommand);
    }

    private void runClipperCommand(String clipperCommand) throws ProcessorException {
        log.info("Executing '" + clipperCommand + "'");
        try {
            ExternalJobRunner runner = new ExternalJobRunner(new String[]{"bash", "-c", clipperCommand});
            log.info("Command '" + clipperCommand + "' returned with exit value '" + runner.getExitValue() + "'");
            if (runner.getExitValue() != 0) {
                log.debug("Standard out:\n" + runner.getOutput());
                log.debug("Standard err:\n" + runner.getError());
            }
        } catch (IOException e) {
            throw new ProcessorException(e);
        } catch (InterruptedException e) {
            throw new ProcessorException(e);
        }
    }

    private String getFfmpegAspectRatio(TranscodeRequest request) {
        Double aspectRatio = request.getDisplayAspectRatio();
        String ffmpegResolution = null;
        if (aspectRatio != null) {
            long width = Math.round(aspectRatio*previewHeight);
            if (width%2 == 1) width += 1;
            ffmpegResolution = " -s " + width + "x" + previewHeight;
        } else {
            ffmpegResolution = " -s 320x240";
        }
        return ffmpegResolution;
    }

    private void naiveClip(TranscodeRequest request, ServletConfig config) throws ProcessorException {
        File outputFile = Util.getDemuxFile(request, config);
        boolean firstClip = true;
        for (TranscodeRequest.FileClip clip: request.getClips()) {
            String command = getClipperCommand(clip, firstClip, outputFile.getAbsolutePath());
            firstClip = false;
            try {
                ExternalJobRunner runner = new ExternalJobRunner(new String[]{"bash", "-c", command});
            } catch (IOException e) {
                throw new ProcessorException(e);
            } catch (InterruptedException e) {
                throw new ProcessorException(e);
             }
        }
    }

    private String getClipperCommand(TranscodeRequest.FileClip clip, boolean firstClip, String fileName) {

        // dd section
        Long blocksize = 1880L;
        String command = "dd if=" + clip.getFilepath() + " bs=" + blocksize;
        Long startOffsetBytes = clip.getStartOffsetBytes();
        if (startOffsetBytes != null) command += " skip=" + startOffsetBytes/blocksize;
        Long clipLengthBytes = clip.getClipLength();
        if (clipLengthBytes != null) command += " count=" + clipLengthBytes/blocksize;

        //vlc section
        command += " | vlc - --program=" + clip.getProgramId() + " --demux=ts --intf dummy --play-and-exit --noaudio " +
                "--novideo";
        if (!firstClip) command += " --sout-file-append";
        command +=  " --sout '#std{access=file,mux=ts,dst=" + fileName + "}'";
        return command;
    }

}
