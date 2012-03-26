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

@XmlRootElement(name="digitvobjectstatus")
public class DigitvExtractionStatus {

    protected ObjectStatusEnum status;
    protected String filename;
    protected Double completionPercentage = 0.0;
    protected Long fileLengthBytes;
    protected Long offsetStart;
    protected Long offsetEnd;
    protected int positionInQueue;
	protected String activeProcess;

    public ObjectStatusEnum getStatus() {
		return status;
	}
	public void setStatus(ObjectStatusEnum status) {
		this.status = status;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public Double getCompletionPercentage() {
		return completionPercentage;
	}
	public void setCompletionPercentage(Double completionPercentage) {
		this.completionPercentage = completionPercentage;
	}
	public Long getFileLengthBytes() {
		return fileLengthBytes;
	}
	public void setFileLengthBytes(Long fileLengthBytes) {
		this.fileLengthBytes = fileLengthBytes;
	}
	public Long getOffsetStart() {
		return offsetStart;
	}
	public void setOffsetStart(Long startOffset) {
		this.offsetStart = startOffset;
	}
	public Long getOffsetEnd() {
		return offsetEnd;
	}
	public void setOffsetEnd(Long endOffset) {
		this.offsetEnd = endOffset;
	}
	public int getPositionInQueue() {
		return positionInQueue;
	}
	public void setPositionInQueue(int positionInQueue) {
		this.positionInQueue = positionInQueue;
	}
	
	public String getActiveProcess() {
		return this.activeProcess;
	}

	public void setActiveProcess(String activeProcess) {
		this.activeProcess = activeProcess;
	}
}
