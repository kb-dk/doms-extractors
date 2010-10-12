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

import javax.servlet.ServletConfig;
import java.io.File;
import java.io.IOException;

public class DemuxerProcessor extends ProcessorChainElement {

    /**
     * Takes the list of clips in the clips field of the request and
     * creates a single transport-stream file from them. This method
     * blocks during the demuxing process.
     * Side-effect: the temporary directory is created if it doesn't already exist.
     * @param request Pre-condition is that the field "clips" in the
     */
    @Override
    protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {

        String outputDir = config.getInitParameter(Constants.TEMP_DIR_INIT_PARAM);
        String fileName = request.getPid() + "_first.ts";
        File outputDirFile = new File(outputDir);
        outputDirFile.mkdirs();
        File outputFile = new File(outputDir, fileName);
        // TODO work out what to do if this file already exists

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
