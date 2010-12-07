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
import dk.statsbiblioteket.doms.radiotv.extractor.ObjectStatus;
import dk.statsbiblioteket.doms.radiotv.extractor.ObjectStatusEnum;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import java.io.File;
import java.io.UnsupportedEncodingException;

public class StatusExtractor {

    private static Logger log = Logger.getLogger(StatusExtractor.class);

    private static double demuxWeight = 0.1;

    /**
     * Takes a doms url such as http://alhena:7980/fedora/objects/uuid%3A41ef709e-43bd-4843-9575-488ae105e5f2/datastreams/SHARD_METADATA/content
     * and returns the current status of this object or null if this program has not been requested before.
     * @param shardUrl
     * @return
     */
    public static ObjectStatus getStatus(String shardUrl, ServletConfig config) throws UnsupportedEncodingException, ProcessorException {
        String uuid = Util.getUuid(shardUrl);
        if (uuid == null) throw new IllegalArgumentException("Invalid url - no uuid found: '" + shardUrl + "'");
        TranscodeRequest request = new TranscodeRequest(uuid);
        //boolean isDone = Util.getIsDone(config, request);
        boolean isDone = (Util.getFinalFinalFile(request, config).exists()) || (Util.getFlashFile(request, config).exists() && !ClipStatus.getInstance().isKnown(uuid));
        if (isDone) {
            log.debug("Found already fully ready result for '" + uuid + "'");
            ObjectStatus status = new ObjectStatus();
            status.setStatus(ObjectStatusEnum.DONE);
            status.setStreamId(Util.getStreamId(request, config));
            status.setServiceUrl(Util.getInitParameter(config,Constants.WOWZA_URL));
            return status;
        } else if (ClipStatus.getInstance().isKnown(uuid)) {
            log.debug("Already started transcoding '" + uuid + "'");
            request = ClipStatus.getInstance().get(uuid);
            double completionPercentage = getCompletionPercentage(config, request);
            ObjectStatus status = new ObjectStatus();
            status.setServiceUrl(Util.getInitParameter(config, Constants.WOWZA_URL));
            completionPercentage = Math.min(completionPercentage, 99.5);
            status.setCompletionPercentage(completionPercentage);
            status.setPreviewIsComplete(request.isFlashIsDone());
            status.setStatus(ObjectStatusEnum.STARTED);
            File previewFile = Util.getFlashFile(request, config);
            if (previewFile.exists() && previewFile.length() > 0) {
                //status.setServiceUrl(config.getInitParameter(Constants.WOWZA_URL));
                status.setPreviewStreamId("flv:" + previewFile.getName());
            }
            if (completionPercentage == 0.0) {
                status.setPositionInQueue(Util.getQueuePosition(request, config));
            }
            return status;
        } else return null;
        //TODO look for error conditions - specifically files in the error directory
    }

    private static double getCompletionPercentage(ServletConfig config, TranscodeRequest request) throws ProcessorException {
        double completionPercentage = 0.0;
        File tempFinalFile = Util.getIntialFinalFile(request, config);
        File demuxFile = Util.getDemuxFile(request, config);
        if (tempFinalFile.exists()) {
            completionPercentage = 100*(demuxWeight + (1.0-demuxWeight)*tempFinalFile.length()/request.getFinalFileLengthBytes());
        } else if (demuxFile.exists()) {
            completionPercentage = 100*demuxWeight*Math.min( (long)1.0 , ((long)demuxFile.length())/(request.getDemuxedFileLengthBytes().longValue())  );
        }
        return completionPercentage;
    }

}
