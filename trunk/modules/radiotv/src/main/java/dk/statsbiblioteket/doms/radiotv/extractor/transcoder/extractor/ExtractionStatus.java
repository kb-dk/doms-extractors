/* $Id: ObjectStatus.java 679 2010-09-15 12:10:51Z csrster $
 * $Revision: 679 $
 * $Date: 2010-09-15 14:10:51 +0200 (Wed, 15 Sep 2010) $
 * $Author: csrster $
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
package dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor;

import dk.statsbiblioteket.doms.radiotv.extractor.ObjectStatusEnum;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="objectstatus")
public class ExtractionStatus {

    protected ObjectStatusEnum status;
    protected Double completionPercentage = 0.0;
    protected Long flashFileLengthBytes;
    protected String serviceUrl;
    protected String streamId;
    protected String previewStreamId;
    protected int positionInQueue;
    protected Boolean previewIsComplete;
   


    public Long getFlashFileLengthBytes() {
        return flashFileLengthBytes;
    }

    public void setFlashFileLengthBytes(Long flashFileLengthBytes) {
        this.flashFileLengthBytes = flashFileLengthBytes;
    }

    public Boolean isPreviewIsComplete() {
        return previewIsComplete;
    }

    public void setPreviewIsComplete(Boolean previewIsComplete) {
        this.previewIsComplete = previewIsComplete;
    }

    public int getPositionInQueue() {
        return positionInQueue;
    }

    public void setPositionInQueue(int positionInQueue) {
        this.positionInQueue = positionInQueue;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public String getPreviewStreamId() {
        return previewStreamId;
    }

    public void setPreviewStreamId(String previewStreamId) {
        this.previewStreamId = previewStreamId;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public ExtractionStatus() {
    }

    public ObjectStatusEnum getStatus() {
        return status;
    }

    public void setStatus(ObjectStatusEnum status) {
        this.status = status;
    }

    public Double getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(Double completionPercentage) {
        this.completionPercentage = completionPercentage;
    }
}
