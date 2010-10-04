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
import dk.statsbiblioteket.util.Files;

import javax.servlet.ServletConfig;
import java.io.File;
import java.io.IOException;

/**
 * This element transcodes the single file (given by request.getPid()+"_first.ts") to a streamable mp4 and
 * copies it to the final directory.
 */
public class SecondClipper extends ProcessorChainElement {

    /**
     * Transcodes the file to a streamable mp4.
     * Pre-requisite: the request-pid refers to a file which is ready for transcoding
     * Side-effect: the final output directory is created if necessary.
     * @param request
     * @param config
     */
    @Override
    protected void processThis(TranscodeRequest request, ServletConfig config) {

        String tempDirName = config.getInitParameter(Constants.TEMP_DIR_INIT_PARAM);
        String finalDirName = config.getInitParameter(Constants.FINAL_DIR_INIT_PARAM);
        File tempDir = new File(tempDirName);
        File finalDir = new File(finalDirName);
        if (!finalDir.exists()) finalDir.mkdirs();
        File inputFile = new File(tempDir, request.getPid() + "_first.ts");
        File finalTempFile = new File(tempDir, request.getPid()+".mp4");
        File finalFinalFile = new File(finalDir, finalTempFile.getName());
        String command = "HandBrakeCLI -i " + inputFile.getAbsolutePath() +
                " -r 24 -e x264 -E faac --crop 0:0:0:0 --height 240 --vb 400 " +
                "-x subq=1:nob_adapt:bframes=1:threads=auto:keyint=1000 -o " +
                finalTempFile.getAbsolutePath();
        try {
            new ExternalJobRunner(command);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            Files.move(finalTempFile, finalFinalFile);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
