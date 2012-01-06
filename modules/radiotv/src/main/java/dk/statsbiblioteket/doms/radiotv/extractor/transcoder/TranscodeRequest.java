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

import org.apache.commons.pool.impl.GenericObjectPool;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class encapsulating information about a download request. The various stages
 * in the processor chain may enrich this request with information about the status
 * of the request and may impose preconditions on which fields must be non-null.
 */
public class TranscodeRequest {

    /**
     * By placing references to the lock objects in the TranscodeRequest we can allow the processor
     * chain to control when it frees the queue for new jobs.
     */
    private Object lockObject;
    private GenericObjectPool thePool;

    /**
     * The pid (uuid) of the program object in DOMS to which this request refers. Must be
     * non-null.
     */
    private String pid;

    private ClipTypeEnum clipType;

    private ServiceTypeEnum serviceType;


    private String videoPid;
    private String audioPid;
    private String dvbsubPid;

    private FileClip longestClip;
    private ShardStructure structure;

     private Long programStartTime;
        private Long programEndTime;

        public Long getProgramStartTime() {
            return programStartTime;
        }

        public void setProgramStartTime(Long programStartTime) {
            this.programStartTime = programStartTime;
        }

        public Long getProgramEndTime() {
            return programEndTime;
        }

        public void setProgramEndTime(Long programEndTime) {
            this.programEndTime = programEndTime;
        }

    public ShardStructure getStructure() {
        return structure;
    }

    public void setStructure(ShardStructure structure) {
        this.structure = structure;
    }

    public FileClip getLongestClip() {
        return longestClip;
    }

    public void setLongestClip(FileClip longestClip) {
        this.longestClip = longestClip;
    }

    public ServiceTypeEnum getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceTypeEnum serviceType) {
        this.serviceType = serviceType;
    }

    private Set<String> audioPids = new HashSet<String>();
    public void addAudioPid(String pid) {
        audioPids.add(pid);
    }

    public Set<String> getAudioPids() {
        return audioPids;
    }

    public int getMinimumAudioPid() {
        int minimum = Integer.MAX_VALUE;
        for (String pid: audioPids) {
            Integer newPid = Integer.decode(pid);
            minimum = Math.min(newPid, minimum);
        }
        return minimum;
    }

    public String getVideoPid() {
        return videoPid;
    }

    public void setVideoPid(String videoPid) {
        this.videoPid = videoPid;
    }

    public String getAudioPid() {
        return audioPid;
    }

    public void setAudioPid(String audioPid) {
        this.audioPid = audioPid;
    }

    public String getDvbsubPid() {
        return dvbsubPid;
    }

    public void setDvbsubPid(String dvbsubPid) {
        this.dvbsubPid = dvbsubPid;
    }

    private boolean flashIsDone = false;

    public boolean isFlashIsDone() {
        return flashIsDone;
    }

    public void setFlashIsDone(boolean flashIsDone) {
        this.flashIsDone = flashIsDone;
    }

    public ClipTypeEnum getClipType() {
        return clipType;
    }

    public void setClipType(ClipTypeEnum clipType) {
        this.clipType = clipType;
    }

    public Object getLockObject() {
        return lockObject;
    }
    public Double getDisplayAspectRatio() {
        return displayAspectRatio;
    }

    public void setLockObject(Object lockObject) {
        this.lockObject = lockObject;
    }
    public void setDisplayAspectRatio(Double displayAspectRatio) {
        this.displayAspectRatio = displayAspectRatio;
    }

    public GenericObjectPool getThePool() {
        return thePool;
    }

    public void setThePool(GenericObjectPool thePool) {
        this.thePool = thePool;
    }

    private Double displayAspectRatio;

    private String shard;
    private Long totalLengthSeconds;

    private Long demuxedFileLengthBytes;

    private Long finalFileLengthBytes;

    public Long getDemuxedFileLengthBytes() {
        return demuxedFileLengthBytes;
    }

    public void setDemuxedFileLengthBytes(Long demuxedFileLengthBytes) {
        this.demuxedFileLengthBytes = demuxedFileLengthBytes;
    }

    public Long getFinalFileLengthBytes() {
        return finalFileLengthBytes;
    }

    public void setFinalFileLengthBytes(Long finalFileLengthBytes) {
        this.finalFileLengthBytes = finalFileLengthBytes;
    }

    public Long getTotalLengthSeconds() {
        return totalLengthSeconds;
    }

    public void setTotalLengthSeconds(Long totalLengthSeconds) {
        this.totalLengthSeconds = totalLengthSeconds;
    }

    public String getShard() {
        return shard;
    }

    public void setShard(String shard) {
        this.shard = shard;
    }

    public String getPid() {
        return pid;
    }

    public String getUuid() {
        if (pid.startsWith("uuid:")) {
            return pid;
        } else {
            return "uuid:" + pid;
        }
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    private List<FileClip> clips;

    public TranscodeRequest(String pid) {
        this.pid = pid;
    }

    public void setClips(List<FileClip> clips) {
        this.clips = clips;
    }

    public List<FileClip> getClips() {
        return clips;
    }

    private List<SnapshotPosition> snapshotPositions;

    public List<SnapshotPosition> getSnapshotPositions() {
        return snapshotPositions;
    }

    public void setSnapshotPositions(List<SnapshotPosition> snapshotPositions) {
        this.snapshotPositions = snapshotPositions;
    }

    @Override
    public String toString() {
        return pid + "#" + clipType;
    }

    public static class SnapshotPosition {
        @Override
        public String toString() {
            return "SnapshotPosition{" +
                    "filepath='" + filepath + '\'' +
                    ", programId=" + programId +
                    ", bytePosition=" + bytePosition +
                    '}';
        }

        private String filepath;
     private Integer programId;
     private Long bytePosition;

     public String getFilepath() {
         return filepath;
     }

     public void setFilepath(String filepath) {
         this.filepath = filepath;
     }

     public Integer getProgramId() {
         return programId;
     }

     public void setProgramId(Integer programId) {
         this.programId = programId;
     }

     public Long getBytePosition() {
         return bytePosition;
     }

     public void setBytePosition(Long bytePosition) {
         this.bytePosition = bytePosition;
     }
 }

    /**
     * Class representing the absolute minimum information needed to clip data from a file
     */
  public static class FileClip {
        private String filepath;
        private Integer programId; //non-null only for mux'es
        private Long startOffsetBytes;
        private Long clipLength;

        private Long fileStartTime;
        private Long fileEndTime;


        public Long getFileStartTime() {
            return fileStartTime;
        }

        public void setFileStartTime(Long fileStartTime) {
            this.fileStartTime = fileStartTime;
        }

        public Long getFileEndTime() {
            return fileEndTime;
        }

        public void setFileEndTime(Long fileEndTime) {
            this.fileEndTime = fileEndTime;
        }

        public FileClip(String filepath) {
            this.filepath = filepath;
        }

        public void setProgramId(Integer programId) {
            this.programId = programId;
        }

        public void setStartOffsetBytes(Long startOffsetBytes) {
            this.startOffsetBytes = startOffsetBytes;
        }

        public void setClipLength(Long clipLength) {
            this.clipLength = clipLength;
        }

        public String getFilepath() {
            return filepath;
        }

        public Integer getProgramId() {
            return programId;
        }

        public Long getStartOffsetBytes() {
            return startOffsetBytes;
        }

        public Long getClipLength() {
            return clipLength;
        }

        @Override
        public String toString() {
            return "FileClip{" +
                    "filepath='" + filepath + '\'' +
                    ", programId=" + programId +
                    ", startOffsetBytes=" + startOffsetBytes +
                    ", clipLength=" + clipLength +
                    '}';
        }
    }
    
}
