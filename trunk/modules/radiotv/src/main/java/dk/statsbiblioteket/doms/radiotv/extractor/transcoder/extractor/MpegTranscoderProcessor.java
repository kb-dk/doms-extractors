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
import java.util.List;
import java.io.File;

public class MpegTranscoderProcessor extends ProcessorChainElement {

    private static Logger log = Logger.getLogger(MpegTranscoderProcessor.class);


    @Override
    protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
        mpegClip(request, config);
    }


    private void mpegClip(TranscodeRequest request, ServletConfig config) throws ProcessorException {
        Long additionalStartOffset = Long.parseLong(Util.getInitParameter(config, Constants.START_OFFSET_BART))*request.getClipType().getBitrate();
        Long additionalEndOffset =  Long.parseLong(Util.getInitParameter(config, Constants.END_OFFSET_BART))*request.getClipType().getBitrate();
        log.debug("Additonal Start Offset for '" + request.getPid() + "' :" + additionalStartOffset + "bytes");
        log.debug("Additonal End Offset for '" + request.getPid() + "' :" + additionalEndOffset + "bytes");
        long blocksize = 1880L;
        final int clipSize = request.getClips().size();
        String command;
        if (clipSize > 1) {
            command = getMultiClipCommand(request, config, additionalStartOffset, additionalEndOffset);
        } else {
            TranscodeRequest.FileClip clip = request.getClips().get(0);
            String start = "";
            if (clip.getStartOffsetBytes() != null && clip.getStartOffsetBytes() != 0L) {
                start = "skip=" + Math.max((additionalStartOffset + clip.getStartOffsetBytes())/blocksize, 0L);
            }
            String length = "";
            if (clip.getClipLength() != null && clip.getClipLength() != 0L) length = "count=" + (additionalEndOffset + clip.getClipLength())/blocksize;
            command = "dd if=" + clip.getFilepath() + " bs=" + blocksize + " " + start + " " + length + "| "
                    + FlashTranscoderProcessor.getFfmpegCommandLine(request, config);
        }
        String outputFile = null;
        switch (request.getServiceType()) {
            case PREVIEW_GENERATION:
                outputFile = OutputFileUtil.getFlashVideoPreviewOutputFile(request, config).getAbsolutePath();
                break;
            case BROADCAST_EXTRACTION:
                outputFile = OutputFileUtil.getFlashVideoOutputFile(request, config).getAbsolutePath();
                break;
        }
        //MuxFlashClipper.symlinkToRootDir(config, new File(outputFile));
        try {
            long timeout = Math.round(Double.parseDouble(Util.getInitParameter(config, Constants.TRANSCODING_TIMEOUT_FACTOR))*request.getTotalLengthSeconds()*1000L);
            log.debug("Setting transcoding timeout for '" + request.getPid() + "' to " + timeout + "ms" );
            ExternalJobRunner.runClipperCommand(timeout, command);
        } catch (ExternalProcessTimedOutException e) {
            log.warn("Deleting '" + outputFile + "'");
            (new File(outputFile)).delete();
            throw new ProcessorException(e);
        }
    }

    private String getMultiClipCommand(TranscodeRequest request, ServletConfig config, Long additionalStartOffset, Long additionalEndOffset) {
        String files = "cat ";
        long blocksize = 1880L;
        List<TranscodeRequest.FileClip> clips = request.getClips();
        for (int i=0; i<clips.size(); i++) {
            TranscodeRequest.FileClip clip = clips.get(i);
            files += " <(dd if="+clip.getFilepath() + " bs=" + blocksize;
            if (clip.getStartOffsetBytes() != null) {
                if (i != 0) {
                    files += " skip=" + clip.getStartOffsetBytes()/blocksize;
                } else {
                    files += " skip=" + Math.max(0, (additionalStartOffset + clip.getStartOffsetBytes())/blocksize);
                }
            }
            if (clip.getClipLength() != null) {
                if (i != clips.size() -1 && i != 0) {
                    files += " count=" + clip.getClipLength()/blocksize;
                } else if (i == 0) {
                    //Don't specify count at all. Just go to end of file.
                    //log.warn("Unusual to have cliplength set in first clip of multiclip program\n" + request.getShard() );
                    files += " count=" + (-additionalStartOffset + clip.getClipLength())/blocksize;
                } else {
                    files += " count=" + (additionalEndOffset + clip.getClipLength())/blocksize;
                }
            }
            files += ") ";
        }
        String command = files + " | "  + FlashTranscoderProcessor.getFfmpegCommandLine(request, config);
        return command;
    }


}
