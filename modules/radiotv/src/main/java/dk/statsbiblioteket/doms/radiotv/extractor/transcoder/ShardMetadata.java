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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlRootElement(name= "shard_metadata")
public class ShardMetadata {


    private List<ShardFile> shardFiles;

    private ShardStructure shardStructure;

    @XmlElement(name = "file")
    public List<ShardFile> getShardFiles() {
        return shardFiles;
    }

    public void setShardFiles(List<ShardFile> shardFiles) {
        this.shardFiles = shardFiles;
    }

    @XmlElement(required = false)
    public ShardStructure getShardStructure() {
        return shardStructure;
    }

    public void setShardStructure(ShardStructure shardStructure) {
        this.shardStructure = shardStructure;
    }

    @XmlType(propOrder = {"file_url", "channel_id", "program_start_offset", "program_clip_length", "file_name", "format_uri"})
    public static class ShardFile{
        private String file_url;
        private Long program_start_offset;
        private Long program_clip_length;
        private String file_name;
        private String format_uri;

        private int channel_id;

        @XmlElement(required = true)
        public String getFile_url() {
            return file_url;
        }

        public void setFile_url(String file_url) {
            this.file_url = file_url;
        }

        @XmlElement(required = false)
        public int getChannel_id() {
            return channel_id;
        }

        public void setChannel_id(int channel_id) {
            this.channel_id = channel_id;
        }

        @XmlElement(required = false)
        public Long getProgram_start_offset() {
            return program_start_offset;
        }

        public void setProgram_start_offset(Long program_start_offset) {
            this.program_start_offset = program_start_offset;
        }

        @XmlElement(required = false)
        public Long getProgram_clip_length() {
            return program_clip_length;
        }

        public void setProgram_clip_length(Long program_clip_length) {
            this.program_clip_length = program_clip_length;
        }

        @XmlElement(required = true)
        public String getFile_name() {
            return file_name;
        }

        public void setFile_name(String file_name) {
            this.file_name = file_name;
        }

        @XmlElement(required = true)
        public String getFormat_uri() {
            return format_uri;
        }

        public void setFormat_uri(String format_uri) {
            this.format_uri = format_uri;
        }
    }

    @Override
    public String toString() {
        return "ShardMetadata{" +
                "shardFiles=" + shardFiles +
                ", shardStructure=" + shardStructure +
                '}';
    }
}
