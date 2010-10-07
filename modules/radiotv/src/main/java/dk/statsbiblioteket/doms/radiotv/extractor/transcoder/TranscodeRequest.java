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

import java.util.List;

/**
 * Class encapsulating information about a download request. The various stages
 * in the processor chain may enrich this request with information about the status
 * of the request and may impose preconditions on which fields must be non-null.
 */
public class TranscodeRequest {

    /**
     * The pid of the program object in DOMS to which this request refers. Must be
     * non-null.
     */
    private String pid;

    private String shard;

    public String getShard() {
        return shard;
    }

    public void setShard(String shard) {
        this.shard = shard;
    }

    public String getPid() {
        return pid;
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

    /**
     * Class representing the absolute minimum information needed to clip data from a file
     */
  public static class FileClip {
        private String filepath;
        private Integer programId; //non-null only for mux'es
        private Long startOffsetBytes;
        private Long clipLength;

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
    }
    
}
