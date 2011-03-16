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
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor.FlashTranscoderProcessor;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.snapshotter.MpegSnapshotGeneratorProcessor;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MpegPreviewProcessor extends ProcessorChainElement {

    private static Logger log = Logger.getLogger(MpegPreviewProcessor.class);


    @Override
    protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
        log.debug("Beginning preview processing of '" + request.getPid() + "'");
        Long blocksize = 1880L;
        TranscodeRequest.FileClip longestClip = request.getLongestClip();
        log.debug("Longest clip for '" + request.getPid() + "' is '" + longestClip + "'");
        int previewLengthSeconds = Integer.parseInt(Util.getInitParameter(config, Constants.PREVIEW_LENGTH));
        Long previewLengthBytes = ClipTypeEnum.getType(request).getBitrate() * previewLengthSeconds;


        TranscodeRequest.SnapshotPosition previewSnapshot = new TranscodeRequest.SnapshotPosition();
        previewSnapshot.setFilepath(longestClip.getFilepath());
        previewSnapshot.setProgramId(longestClip.getProgramId());
        List<TranscodeRequest.SnapshotPosition> pos = new ArrayList<TranscodeRequest.SnapshotPosition>();
        pos.add(previewSnapshot);
        request.setSnapshotPositions(pos);
        MpegSnapshotGeneratorProcessor snapshotter = new MpegSnapshotGeneratorProcessor();
        snapshotter.setLabel(MpegSnapshotGeneratorProcessor.PREVIEW_LABEL);
        this.setChildElement(snapshotter);

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
        previewSnapshot.setBytePosition(clipStartPosition);
        String command = "dd if=" + longestClip.getFilepath() + " bs=" + blocksize + " skip=" + clipStartPosition/blocksize + " count=" + previewLengthBytes/blocksize + "| "
                + FlashTranscoderProcessor.getFfmpegCommandLine(request, config);
        ExternalJobRunner.runClipperCommand(command);
        request.setServiceType(ServiceTypeEnum.PREVIEW_GENERATION);
    }
}
