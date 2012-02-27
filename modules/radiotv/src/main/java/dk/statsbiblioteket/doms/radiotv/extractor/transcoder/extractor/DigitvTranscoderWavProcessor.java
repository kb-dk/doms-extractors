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

public class DigitvTranscoderWavProcessor extends ProcessorChainElement {

    private static Logger log = Logger.getLogger(DigitvTranscoderWavProcessor.class);

    @Override
    protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
    	File outputFile = OutputFileUtil.getDigitvWorkOutputFile(request, config);
        String clipCommand = getClipCommand(request, config);
        String ab = Util.getInitParameter(config, Constants.DIGITV_AUDIO_BITRATE);
		String transcodeCommand = "ffmpeg -f s16le -i - -ab " + ab + "k " + outputFile.getAbsolutePath();
        String command = clipCommand + " | " + transcodeCommand;
        try {
            long timeout = Math.round(Double.parseDouble(Util.getInitParameter(config, Constants.TRANSCODING_TIMEOUT_FACTOR))*request.getTotalLengthSeconds()*1000L);
            log.debug("Setting transcoding timeout for '" + request.getPid() + "' to " + timeout + "ms" );
            ExternalJobRunner.runClipperCommand(timeout, command);
        } catch (ExternalProcessTimedOutException e) {
            log.warn("Deleting '" + outputFile + "'");
            outputFile.delete();
        }
    }

    private String getClipCommand(TranscodeRequest request, ServletConfig config) {
        String command = "cat ";
        List<TranscodeRequest.FileClip> clips = request.getClips();
        long bitrate = request.getClipType().getBitrate();
        Long additionalStartOffsetSeconds = (request.getUserAdditionalStartOffset() + Long.parseLong(Util.getInitParameter(config, Constants.START_OFFSET_RADIO)));
        Long additionalEndOffsetSeconds =  (request.getUserAdditionalEndOffset() + Long.parseLong(Util.getInitParameter(config, Constants.END_OFFSET_RADIO)));
        log.debug("Additonal Start Offset for '" + request.getPid() + "' :" + additionalStartOffsetSeconds + "seconds");
        log.debug("Additonal End Offset for '" + request.getPid() + "' :" + additionalEndOffsetSeconds + "seconds");
        for (int i=0; i<clips.size(); i++) {
            TranscodeRequest.FileClip clip = clips.get(i);
            String soxTranscodeParameters = Util.getInitParameter(config, Constants.SOX_TRANSCODE_PARAM);
            command += " <(sox " + clip.getFilepath() + " " + soxTranscodeParameters + " - ";
            if ((clip.getStartOffsetBytes() != null && clip.getStartOffsetBytes() != 0) || clip.getClipLength() != null) {
                String trimFilter = " trim ";
                if (clip.getStartOffsetBytes() != null && clip.getStartOffsetBytes() != 0) {
                    long startOffsetSeconds = clip.getStartOffsetBytes()/bitrate;
                    if (i == 0) {
                        startOffsetSeconds = Math.max(0, startOffsetSeconds + additionalStartOffsetSeconds);
                    }
                    trimFilter += startOffsetSeconds + ".0 ";
                } else {
                    trimFilter += " 0.0 ";
                }
                if (clip.getClipLength() != null) {
                    long clipLengthSeconds = clip.getClipLength()/bitrate;
                    if ( i==0 ) {
                        clipLengthSeconds = clipLengthSeconds - additionalStartOffsetSeconds;
                    }
                    if ( i == clips.size() -1 ) {
                        clipLengthSeconds += additionalEndOffsetSeconds;
                    }
                    trimFilter += clipLengthSeconds + ".0 ";
                }
                command += trimFilter;
            }
            command += " ) ";
        }
        return command;
    }
}
