/* $Id$
 * $Revision$
 * $Date$
 * $Author$
 *
 * The Netarchive Suite - Software to harvest and preserve websites
 * Copyright 2004-2009 Det Kongelige Bibliotek and Statsbiblioteket, Denmark
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *   USA
 */
package dk.statsbiblioteket.doms.radiotv.extractor.transcoder;

import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import java.io.IOException;

import dk.statsbiblioteket.doms.radiotv.extractor.ExternalJobRunner;
import dk.statsbiblioteket.doms.radiotv.extractor.Constants;

public class FlashTranscoderProcessor extends ProcessorChainElement {

    private static Logger log = Logger.getLogger(FlashTranscoderProcessor.class);


    @Override
    protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
        switch (request.getClipType()) {
            case MUX:
                (new MuxFlashClipper()).processThis(request, config);
                break;
            case MPEG1:
                (new MpegTranscoderProcessor()).processThis(request, config);
                break;
            case MPEG2:
                (new MpegTranscoderProcessor()).processThis(request, config);
                break;
            case WAV:
                (new WavTranscoderProcessor()).processThis(request, config);
        }
        Util.unlockRequest(request);
    }

    protected static String getFfmpegCommandLine(TranscodeRequest request, ServletConfig config) {
        String line = "ffmpeg -i - " + config.getInitParameter(Constants.FFMPEG_PARAMS)
                + " -b " + config.getInitParameter(Constants.VIDEO_BITRATE) + "000"
                + " -ab " + config.getInitParameter(Constants.AUDIO_BITRATE) + "000"
                + " " + getFfmpegAspectRatio(request, config)
                + " " + " -vpre "  + config.getInitParameter(Constants.X264_PRESET)
                + " " + Util.getFlashFile(request, config);
        return line;
    }


    protected static String getFfmpegAspectRatio(TranscodeRequest request, ServletConfig config) {
        Double aspectRatio = request.getDisplayAspectRatio();
        String ffmpegResolution;
        Long height = Long.parseLong(config.getInitParameter(Constants.PICTURE_HEIGHT));
        if (aspectRatio != null) {
            long width = Math.round(aspectRatio*height);
            if (width%2 == 1) width += 1;
            ffmpegResolution = " -s " + width + "x" + height;
        } else {
            ffmpegResolution = " -s 320x240";
        }
        return ffmpegResolution;
    }

    static int getHeight(TranscodeRequest request, ServletConfig config) {
        Long height = Long.parseLong(config.getInitParameter(Constants.PICTURE_HEIGHT));
        return height.intValue();
   }

     static int getWidth(TranscodeRequest request, ServletConfig config) {
       Double aspectRatio = request.getDisplayAspectRatio();
        Long height = Long.parseLong(config.getInitParameter(Constants.PICTURE_HEIGHT));
        if (aspectRatio != null) {
            long width = Math.round(aspectRatio*height);
            if (width%2 == 1) width += 1;
            return (int) width;
        } else {
            return 320;
        }
   }

    protected static void runClipperCommand(String clipperCommand) throws ProcessorException {
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

}
