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

import javax.servlet.ServletConfig;
import java.util.List;
import java.io.File;

public class WavTranscoderProcessor extends ProcessorChainElement {
    @Override
    protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
        final int clipSize = request.getClips().size();
        String command;
        /*if (clipSize > 1) {
            command = getMultiClipCommand(request, config);
        } else {
            long bitrate = request.getClipType().getBitrate();
            TranscodeRequest.FileClip clip = request.getClips().get(0);
            String start = "";
            if (clip.getStartOffsetBytes() != null && clip.getStartOffsetBytes() != 0L) start = " -ss " + clip.getStartOffsetBytes()/bitrate;
            String length = "";
            if (clip.getClipLength() != null && clip.getClipLength() != 0L) length = " -t " + clip.getClipLength()/bitrate;
            command = "ffmpeg -i " + clip.getFilepath()
                    + start + length + " -f wav - |" + getLameCommand(request, config);

        }*/
        command = getMultiClipCommand(request, config);
        FlashTranscoderProcessor.runClipperCommand(command);
    }

    private String getLameCommand(TranscodeRequest request, ServletConfig config) {
        return "lame -b "  + config.getInitParameter(Constants.AUDIO_BITRATE) + " - " + Util.getMp3File(request, config);
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
