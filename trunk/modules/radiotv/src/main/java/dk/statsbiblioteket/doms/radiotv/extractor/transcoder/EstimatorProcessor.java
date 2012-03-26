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
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;

public class EstimatorProcessor extends ProcessorChainElement {

    private static Logger log = Logger.getLogger(EstimatorProcessor.class);

    /**
     * Takes the list of clips in the clips field of the request and
     * estimates the intermediate (demuxed) and final (transcoded) file
     * sizes
     * @param request
     * @param config
     * @throws ProcessorException
     */
    @Override
    protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
        Integer audioBitrate = Integer.parseInt(Util.getInitParameter(config, Constants.AUDIO_BITRATE));
        Integer videoBitrate = Integer.parseInt(Util.getInitParameter(config, Constants.VIDEO_BITRATE));
        //The above rates are kilobit/second
        //TODO unfortunately these values cannot be trusted for flash video
        Long finalFileSizeBytes = request.getTotalLengthSeconds()*(audioBitrate + videoBitrate)*1000L/8L;
        request.setFinalFileLengthBytes(finalFileSizeBytes);
        int demuxedBitrate = 0;        
        if (request.getClipType().equals(ClipTypeEnum.MUX)) {
            if (request.getClips().get(0).getProgramId() == 2030) {
                demuxedBitrate = 4974;
            } else {
                demuxedBitrate = 3766;
            }
        }
        Long demuxedFileSize = request.getTotalLengthSeconds()*demuxedBitrate*1000L/8L;
        request.setDemuxedFileLengthBytes(demuxedFileSize);
        log.debug("Estimated filesizes for '" + request.getPid() + "' :" + demuxedFileSize + "   " + finalFileSizeBytes);
    }




}
