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
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import java.io.File;
import java.io.IOException;

/**
 * This element transcodes the single file (given by request.getPid()+"_first.ts") to a streamable mp4 and
 * copies it to the final directory.
 */
public class TranscoderProcessor extends ProcessorChainElement {

    private static Logger log = Logger.getLogger(TranscoderProcessor.class);

    /**
     * Transcodes the file to a streamable mp4.
     * Pre-requisite: the request-pid refers to a file which is ready for transcoding
     * Side-effect: the final output directory is created if necessary.
     * @param request
     * @param config
     */
    @Override
    protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {

        //TODO move to Util class
        String tempDirName = config.getInitParameter(Constants.TEMP_DIR_INIT_PARAM);
        String finalDirName = config.getInitParameter(Constants.FINAL_DIR_INIT_PARAM);
        File tempDir = new File(tempDirName);
        File finalDir = new File(finalDirName);
        if (!finalDir.exists()) finalDir.mkdirs();
        File inputFile = new File(tempDir, Util.getDemuxFilename(request));
        File finalTempFile = new File(tempDir, Util.getFinalFilename(request));
        File finalFinalFile = new File(finalDir, finalTempFile.getName());
        String command = "HandBrakeCLI -i " + inputFile.getAbsolutePath() +
                " " + config.getInitParameter(Constants.HANDBRAKE_PARAMETERS) + " " +
                " --vb " + config.getInitParameter(Constants.VIDEO_BITRATE) + " " +
                " --ab " + config.getInitParameter(Constants.AUDIO_BITRATE) + " " +                
                " " + config.getInitParameter(Constants.X264_PARAMETERS) + " -o " +               
                finalTempFile.getAbsolutePath();
        log.info("Executing '" + command + "'");
        try {
            ExternalJobRunner runner = new ExternalJobRunner(command);
            log.info("Command '" + command + " returned with exit code '" + runner.getExitValue() + "'");
            if (runner.getExitValue() != 0) {
                log.debug("Standard out:\n" + runner.getOutput());
                log.debug("Standard err:\n" + runner.getError());
            }
        } catch (IOException e) {
            throw new ProcessorException(e);
        } catch (InterruptedException e) {
            throw new ProcessorException(e);
        }
        try {
            Files.delete(inputFile);
        } catch (IOException e) {
            throw new ProcessorException(e);
        }
       try {
            log.info("Deploying final output to '" + finalFinalFile.getAbsolutePath() + "'");
            Files.move(finalTempFile, finalFinalFile);
        } catch (Exception e) {
            throw new ProcessorException(e);
        }
    }
}
