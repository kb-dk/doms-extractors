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

/**
 * This class parses a shard element in the TranscodeRequest into a
 * set fo file clips.
 */
public class ShardParserProcessor extends ProcessorChainElement {

    /**
     * Precondition: request.getShard() returns an xml fragment with the form
     * <shard_metadata><file>

     <file_url>http://bitfinder.statsbiblioteket.dk/bart/mux1.1258135200-2009-11-13-19.00.00_1258138800-2009-11-13-20.00.00_dvb1-2.ts</file_url>
     <channel_id>102</channel_id>
     <program_start_offset>1800</program_start_offset>
     <program_clip_length>1800</program_clip_length>

     <file_name>mux1.1258135200-2009-11-13-19.00.00_1258138800-2009-11-13-20.00.00_dvb1-2.ts</file_name>
     <format_uri>info:pronom/x-fmt/386</format_uri>
     </file><file>

     <file_url>http://bitfinder.statsbiblioteket.dk/bart/mux1.1258135200-2009-11-13-19.00.00_1258138800-2009-11-13-20.00.00_dvb1-2.ts</file_url>
     <channel_id>102</channel_id>
     <program_start_offset>1800</program_start_offset>
     <program_clip_length>1800</program_clip_length>

     <file_name>mux1.1258135200-2009-11-13-19.00.00_1258138800-2009-11-13-20.00.00_dvb1-2.ts</file_name>
     <format_uri>info:pronom/x-fmt/386</format_uri>
     </file></shard_metadata>
     *
     * The method parses the file elements into a list of FileClip objects
     * post-condition: request.getClips() returns a List of FileClip objects.
     * Byte offsets and lengths are calculated using bitrates based on the filenames. These values are hard-coded in
     * this class.
     *
     * @param request
     * @param config
     */
    @Override
    protected void processThis(TranscodeRequest request, ServletConfig config) {
             throw new RuntimeException("not yet implemented");
     }
}
