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

import dk.statsbiblioteket.doms.radiotv.extractor.Constants;

import javax.servlet.ServletConfig;

import org.apache.log4j.Logger;

public class FlashEstimatorProcessor extends ProcessorChainElement {

     private static Logger log = Logger.getLogger(FlashEstimatorProcessor.class);

    @Override
    protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
        ClipTypeEnum clipType = request.getClipType();
        Long finalFileSizeBytes;
        if (clipType.equals(ClipTypeEnum.MPEG1) || clipType.equals(ClipTypeEnum.MPEG2) || clipType.equals(ClipTypeEnum.MUX)) {
            Integer audioBitrate = Integer.parseInt(Util.getInitParameter(config, Constants.AUDIO_BITRATE));
            Integer videoBitrate = Integer.parseInt(Util.getInitParameter(config, Constants.VIDEO_BITRATE));
            //The above rates are kilobit/second
            finalFileSizeBytes = request.getTotalLengthSeconds()*(audioBitrate + videoBitrate)*1000L/8L;
        } else {
            Integer audioBitrate = Integer.parseInt(Util.getInitParameter(config, Constants.AUDIO_BITRATE));
            Integer videoBitrate = 0;
            //The above rates are kilobit/second
            finalFileSizeBytes = request.getTotalLengthSeconds()*(audioBitrate + videoBitrate)*1000L/8L;
        }
        request.setFinalFileLengthBytes(finalFileSizeBytes);
        log.debug("Estimated filesize for '" + request.getPid() + "' :" + finalFileSizeBytes);
    }
}
