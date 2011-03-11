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

import javax.servlet.ServletConfig;
import java.util.List;

public class MuxSnapshotGeneratorProcessor extends ProcessorChainElement {

    Long blocksize = 1880L;
    Long bitrate = ClipTypeEnum.MUX.getBitrate(); // bytes/second
    int seconds = 10; //the number of seconds to use in clipping
    private String label = "snapshot";

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
        List<TranscodeRequest.SnapshotPosition> snapshots = request.getSnapshotPositions();
        int count = 0;
        for (TranscodeRequest.SnapshotPosition snapshot: snapshots) {
            int programId = snapshot.getProgramId();
            String filepath = snapshot.getFilepath();
            Long location = snapshot.getBytePosition();
            String outputFilePrefix = request.getPid() + "." + label + "." + count;
            String command = "cat <(dd if=" + filepath + " bs=" + blocksize +
                    " skip=" + location/blocksize + " count=" + seconds*bitrate/blocksize + " )|"
                    + " vlc - --program=" + programId
                    + " --video-filter scene -V dummy --demux=ts --intf dummy --play-and-exit --vout-filter deinterlace --deinterlace-mode " +
                    "linear --noaudio  " +
                    "--scene-format=png --scene-replace --scene-prefix=" + outputFilePrefix
                    + " --scene-path=" + Util.getFinalDir(config);
            FlashTranscoderProcessor.runClipperCommand(command);
            count++;
        }
    }

}
