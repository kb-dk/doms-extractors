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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A singleton class used to find out the status of running transcodings
 */
public class ClipStatus {

    /**
     * This is the unique in-memory map by which we can find out what is happening
     * in a given transcoding.
     */
    private static Map<String, TranscodeRequest> map = Collections.synchronizedMap(new HashMap<String, TranscodeRequest>());

    /**
     * the unique instance
     */
    private static ClipStatus instance;

    private ClipStatus() {
    }

    public static synchronized ClipStatus getInstance() {
        if (instance == null) instance = new ClipStatus();
        return instance;
    }

    public boolean isKnown(String pid) {
        return map.containsKey(pid);
    }

    public void register(TranscodeRequest request) {
        map.put(request.getPid(), request);
    }

    public void remove(String pid) {
        map.remove(pid);
    }

    public TranscodeRequest get(String pid) {
        return map.get(pid);
    }

}
