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

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="snapshotstatus")
public class SnapshotStatus {
    protected ObjectStatusEnum status;
    protected String[] snapshotFilename;
    protected String[] thumbnailFilename;

    public String[] getSnapshotFilename() {
        return snapshotFilename;
    }

    public void setSnapshotFilename(String[] snapshotFilename) {
        this.snapshotFilename = snapshotFilename;
    }

    public String[] getThumbnailFilename() {
        return thumbnailFilename;
    }

    public void setThumbnailFilename(String[] thumbnailFilename) {
        this.thumbnailFilename = thumbnailFilename;
    }

    public ObjectStatusEnum getStatus() {
        return status;
    }

    public void setStatus(ObjectStatusEnum status) {
        this.status = status;
    }

  
}
