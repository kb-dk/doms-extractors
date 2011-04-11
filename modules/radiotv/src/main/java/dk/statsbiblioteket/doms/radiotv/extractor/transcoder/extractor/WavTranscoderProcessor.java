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
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import java.util.List;
import java.io.File;

public class WavTranscoderProcessor extends ProcessorChainElement {

    private static Logger log = Logger.getLogger(WavTranscoderProcessor.class);

    @Override
    protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
        String command;
        command = getMultiClipCommand(request, config);
        //MuxFlashClipper.symlinkToRootDir(config, OutputFileUtil.getMP3AudioOutputFile(request, config));                
        try {
            long timeout = Math.round(Double.parseDouble(Util.getInitParameter(config, Constants.TRANSCODING_TIMEOUT_FACTOR))*request.getTotalLengthSeconds()*1000L);
            log.debug("Setting transcoding timeout for '" + request.getPid() + "' to " + timeout + "ms" );
            ExternalJobRunner.runClipperCommand(timeout, command);
        } catch (ExternalProcessTimedOutException e) {
            log.warn("Deleting '" + getOutputFile(request, config).getAbsolutePath() + "'");
            getOutputFile(request, config).delete();
        }
    }

    public static String getLameCommand(TranscodeRequest request, ServletConfig config) {
        String outputFileName = getOutputFile(request, config).getAbsolutePath();
        return "lame -b "  + Util.getAudioBitrate(config) + " - " + outputFileName;
    }

    public static File getOutputFile(TranscodeRequest request, ServletConfig config) {
       switch (request.getServiceType()) {
            case BROADCAST_EXTRACTION:
                return  OutputFileUtil.getMP3AudioOutputFile(request, config);
            case PREVIEW_GENERATION:
                return OutputFileUtil.getMP3AudioPreviewOutputFile(request, config);
            case THUMBNAIL_GENERATION:
                return null;
            case PREVIEW_THUMBNAIL_GENERATION:
                return null;
        }
        return null;
    }

    private String getMultiClipCommand(TranscodeRequest request, ServletConfig config) {
        String files = "";
        long start = 0L;
        long length = 0L;
        List<TranscodeRequest.FileClip> clips = request.getClips();
        long bitrate = request.getClipType().getBitrate();
        for (int i=0; i<clips.size(); i++) {
            TranscodeRequest.FileClip clip = clips.get(i);
            files += " " + clip.getFilepath() + " ";
            if (clip.getClipLength() != null) {
                length += clip.getClipLength()/bitrate;
            } else if (clip.getStartOffsetBytes() != null) {
                length += (((new File(clip.getFilepath())).length() - clip.getStartOffsetBytes()))/bitrate;
            } else {
                length += (new File(clip.getFilepath())).length()/bitrate;
            }
            if (i == 0 && clip.getStartOffsetBytes() != null) {
                start = clip.getStartOffsetBytes()/bitrate;
            }
        }
        String command = "sox " + files + " -t wav - trim " + start + ".0 " + length + ".0 |" + getLameCommand(request, config); 
        return command;
    }

}
