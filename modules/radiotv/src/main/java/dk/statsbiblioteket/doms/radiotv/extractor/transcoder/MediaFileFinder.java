package dk.statsbiblioteket.doms.radiotv.extractor.transcoder;

import javax.servlet.ServletConfig;

import java.io.IOException;
import java.util.List;
import java.util.Map;
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

public interface MediaFileFinder {
    
	@Deprecated
	public String getFilePath(String filename, ServletConfig config) throws IOException;
    
    /**
     * Retrieves paths to the filenames listed. If the files are not online, the files are retrieved.
     * When the method returns, the files are guaranteed to be online.
     * 
     * Wildcards are NOT supported.
     * 
     * @param filenames - List of filenames to retrive
     * @return list of filenames with paths
     * @throws MediaFileNotFoundException if one of the ordered files does not exist.
     * @throws IOException
     */
    public Map<String, String> getFilePath(List<String> filenames) throws IOException, MediaFileNotFoundException;
}
