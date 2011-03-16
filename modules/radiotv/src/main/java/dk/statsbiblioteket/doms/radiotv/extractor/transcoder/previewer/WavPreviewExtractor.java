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
package dk.statsbiblioteket.doms.radiotv.extractor.transcoder.previewer;

import dk.statsbiblioteket.doms.radiotv.extractor.Constants;
import dk.statsbiblioteket.doms.radiotv.extractor.ExternalJobRunner;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.*;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor.WavTranscoderProcessor;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import java.io.File;

public class WavPreviewExtractor extends ProcessorChainElement {

    private static Logger log = Logger.getLogger(WavPreviewExtractor.class);


    @Override
    protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
        log.debug("Beginning preview processing of '" + request.getPid() + "'");
        TranscodeRequest.FileClip longestClip = request.getLongestClip();
        log.debug("Longest clip for '" + request.getPid() + "' is '" + longestClip + "'");
        int previewLengthSeconds = Integer.parseInt(Util.getInitParameter(config, Constants.PREVIEW_LENGTH));
        Long clipLength = longestClip.getClipLength();
        Long clipOffset = longestClip.getStartOffsetBytes();
        Long fileLength = new File(longestClip.getFilepath()).length();
        long clipStartPosition = 0L;
        if (clipLength == null && clipOffset == null) {
            clipStartPosition = fileLength/2;
        } else if (clipLength != null && clipOffset != null) {
            clipStartPosition = clipOffset + clipLength/2;
        } else if (clipLength == null && clipOffset != null) {
            clipStartPosition = (clipOffset + (fileLength-clipOffset)/2);
        } else if (clipLength != null && clipOffset == null) {
            clipStartPosition = clipLength/2;
        }
        long bitrate = request.getClipType().getBitrate();

        String command = "sox " + longestClip.getFilepath() + " -t wav - trim " + clipStartPosition/bitrate + ".0 " + previewLengthSeconds + ".0 |" + WavTranscoderProcessor.getLameCommand(request, config);

        ExternalJobRunner.runClipperCommand(command);
    }
}
