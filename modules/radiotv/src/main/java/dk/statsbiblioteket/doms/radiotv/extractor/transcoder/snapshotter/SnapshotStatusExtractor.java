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
package dk.statsbiblioteket.doms.radiotv.extractor.transcoder.snapshotter;

import dk.statsbiblioteket.doms.radiotv.extractor.Constants;
import dk.statsbiblioteket.doms.radiotv.extractor.ObjectStatusEnum;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.*;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import java.io.UnsupportedEncodingException;

public class SnapshotStatusExtractor {

    private static Logger log = Logger.getLogger(SnapshotStatusExtractor.class);


    public static SnapshotStatus getStatus(String shardUrl, ServletConfig config) throws UnsupportedEncodingException, ProcessorException {
        String uuid = Util.getUuid(shardUrl);
        if (uuid == null) throw new IllegalArgumentException("Invalid url - no uuid found: '" + shardUrl + "'");
        TranscodeRequest request = new TranscodeRequest(uuid);
        request.setServiceType(ServiceTypeEnum.THUMBNAIL_GENERATION);
        OutputFileUtil.getAndCreateOutputDir(request, config);
        boolean isDone = (OutputFileUtil.hasOutputFile(request, config) && !RequestRegistry.getInstance().isKnown(request));
        if (isDone) {
            log.debug("Found snapshots for '" + uuid + "'");
            SnapshotStatus status = new SnapshotStatus();
            status.setId(uuid);
            status.setSnapshotWebRoot(Util.getInitParameter(config, Constants.SNAPSHOT_SERVICE_URL));
            String[] snapshots = OutputFileUtil.getAllSnapshotFilenames(config, request);
            status.setStatus(ObjectStatusEnum.DONE);
            SnapshotFilenames names = new SnapshotFilenames();
            names.setSnapshotFilename(snapshots);
            status.setSnapshotFilenames(names);
            return status;
        } else if (RequestRegistry.getInstance().isKnown(request)) {
            log.debug("Already doing snapshots for '" + uuid + "'");
            SnapshotStatus status = new SnapshotStatus();
            status.setId(uuid);
            status.setSnapshotWebRoot(Util.getInitParameter(config, Constants.SNAPSHOT_SERVICE_URL));            
            status.setStatus(ObjectStatusEnum.STARTED);
            String[] snapshots = OutputFileUtil.getAllSnapshotFilenames(config, request);
            SnapshotFilenames names = new SnapshotFilenames();
            names.setSnapshotFilename(snapshots);
            status.setSnapshotFilenames(names);
            return status;
        } else return null;
    }

}
