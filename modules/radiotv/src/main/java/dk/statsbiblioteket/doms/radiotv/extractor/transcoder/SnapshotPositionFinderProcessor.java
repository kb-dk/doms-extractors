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
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SnapshotPositionFinderProcessor extends ProcessorChainElement {

    private static Logger log = Logger.getLogger(SnapshotPositionFinderProcessor.class);
    int nSnapshots;     //TODO make configurable

    /**
     * Do as follows:
     * Calculate the total length of the program and hence the offset bytes for each snapshot.
     * Also build a list for each file specifying the start and end of the file as offsets relative to the start of the
     * program. Then for each snapshot it is trivial to find in which file and where the snapshot is to be taken.
     * @param request
     * @param config
     * @throws ProcessorException
     */
    @Override
    protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
        List<StartEndPairFilePair> filePairList = new ArrayList<StartEndPairFilePair>();
        long currentPosition = 0L;  // Current position in program
        Integer programId = null;
        for (TranscodeRequest.FileClip clip: request.getClips()) {
            StartEndPairFilePair filePair = new StartEndPairFilePair();
            filePair.setFilepath(clip.getFilepath());
            Long fileLength = (new File(clip.getFilepath())).length();
            if (currentPosition == 0L) {  //First clip
                if (clip.getStartOffsetBytes() != null) {
                    filePair.setStart(0 - clip.getStartOffsetBytes());
                } else {
                    filePair.setStart(0L);
                }
                filePair.setEnd(fileLength + filePair.getStart());
                if (clip.getClipLength() != null) {
                    currentPosition = currentPosition + clip.getClipLength();
                } else {
                    currentPosition = filePair.getEnd();
                }
                filePairList.add(filePair);
            } else {   //all other clips
                currentPosition +=1 ;
                filePair.setStart(currentPosition);
                filePair.setEnd(fileLength + filePair.getStart());
                if (clip.getClipLength() != null) {
                    currentPosition = currentPosition + clip.getClipLength();
                } else {
                    currentPosition = filePair.getEnd();
                }
                filePairList.add(filePair);
            }
            log.debug("Added filePair '" + filePair.toString() + "'");
        }
        log.debug("Total program length: '" + currentPosition + "' bytes");
        List<TranscodeRequest.SnapshotPosition> snapshotPositions = new ArrayList<TranscodeRequest.SnapshotPosition>();
        for (int iSnapshot = 0; iSnapshot < nSnapshots; iSnapshot++) {
            Long bytePositionInProgram = currentPosition * (iSnapshot + 1L)/(nSnapshots +1L);
            log.debug("Finding snapshot at position '" + bytePositionInProgram + "' bytes after start of program");
            for (StartEndPairFilePair pair: filePairList) {
                if (pair.getStart() <= bytePositionInProgram && pair.getEnd() >= bytePositionInProgram) {
                    TranscodeRequest.SnapshotPosition snapshot = new TranscodeRequest.SnapshotPosition();
                    snapshot.setProgramId(programId);
                    snapshot.setFilepath(pair.getFilepath());
                    snapshot.setBytePosition(bytePositionInProgram - pair.getStart());
                    snapshotPositions.add(snapshot);
                    log.debug("Added snapshot '" + snapshot.toString() + "'");
                }
            }
        }
        request.setSnapshotPositions(snapshotPositions);
    }

    private static class StartEndPairFilePair {
        @Override
        public String toString() {
            return "StartEndPairFilePair{" +
                    "start=" + start +
                    ", end=" + end +
                    ", filepath='" + filepath + '\'' +
                    '}';
        }

        private long start;
        private long end;
        private String filepath;

        public String getFilepath() {
            return filepath;
        }

        public void setFilepath(String filepath) {
            this.filepath = filepath;
        }

        public long getStart() {
            return start;
        }

        public void setStart(long start) {
            this.start = start;
        }

        public long getEnd() {
            return end;
        }

        public void setEnd(long end) {
            this.end = end;
        }
    }

}
