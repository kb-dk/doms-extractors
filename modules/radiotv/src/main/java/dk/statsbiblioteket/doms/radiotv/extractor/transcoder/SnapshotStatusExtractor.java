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

import dk.statsbiblioteket.doms.radiotv.extractor.ObjectStatusEnum;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import java.io.UnsupportedEncodingException;

public class SnapshotStatusExtractor {

    private static Logger log = Logger.getLogger(SnapshotStatusExtractor.class);


    public static SnapshotStatus getStatus(String shardUrl, ServletConfig config) throws UnsupportedEncodingException {
        String uuid = Util.getUuid(shardUrl);
        if (uuid == null) throw new IllegalArgumentException("Invalid url - no uuid found: '" + shardUrl + "'");
        TranscodeRequest request = new TranscodeRequest(uuid);
        request.setServiceType(ServiceTypeEnum.THUMBNAIL_GENERATION);
        boolean isDone = (Util.getSnapshotFilenames(config, request).length != 0 && !ClipStatus.getInstance().isKnown(request));
        if (isDone) {
            log.debug("Found snapshots for '" + uuid + "'");
            SnapshotStatus status = new SnapshotStatus();
            String[] snapshots = Util.getSnapshotFilenames(config, request);
            String[] thumbnails = Util.getSnapshotThumbnailFilenames(config, request);
            status.setStatus(ObjectStatusEnum.DONE);
            status.setSnapshotFilename(snapshots);
            status.setThumbnailFilename(thumbnails);
            return status;
        } else if (ClipStatus.getInstance().isKnown(request)) {
            log.debug("Already doing snapshots for '" + uuid + "'");
            SnapshotStatus status = new SnapshotStatus();
            status.setStatus(ObjectStatusEnum.STARTED);
            return status;
        } else return null;
    }

}
