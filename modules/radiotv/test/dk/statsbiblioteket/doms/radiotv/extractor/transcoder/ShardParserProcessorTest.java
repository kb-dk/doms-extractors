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

import dk.statsbiblioteket.doms.radiotv.extractor.Constants;
import junit.framework.TestCase;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;

public class ShardParserProcessorTest extends TestCase {

    private String xml1 = "<shard_metadata><file>\n" +
            "\n" +
            "     <file_url>http://bitfinder.statsbiblioteket.dk/bart/mux1.1258135200-2009-11-13-19.00.00_1258138800-2009-11-13-20.00.00_dvb1-2.ts</file_url>\n" +
            "     <channel_id>102</channel_id>\n" +
            "     <program_start_offset>1800</program_start_offset>\n" +
            "\n" +
            "     <file_name>mux1.1258135200-2009-11-13-19.00.00_1258138800-2009-11-13-20.00.00_dvb1-2.ts</file_name>\n" +
            "     <format_uri>info:pronom/x-fmt/386</format_uri>\n" +
            "     </file><file>\n" +
            "\n" +
            "     <file_url>http://bitfinder.statsbiblioteket.dk/bart/mux1.1258135200-2009-11-13-19.00.00_1258138800-2009-11-13-20.00.00_dvb1-2.ts</file_url>\n" +
            "     <channel_id>102</channel_id>\n" +
            "     <program_start_offset>1800</program_start_offset>\n" +
            "     <program_clip_length>1800</program_clip_length>\n" +
            "\n" +
            "     <file_name>mux1.1258135200-2009-11-13-19.00.00_1258138800-2009-11-13-20.00.00_dvb1-2.ts</file_name>\n" +
            "     <format_uri>info:pronom/x-fmt/386</format_uri>\n" +
            "     </file></shard_metadata>";

    public void testProcessThis() throws ProcessorException {
        ServletConfig config = new ServletConfig(){
            public String getServletName() {
                return null;
            }
            public ServletContext getServletContext() {
                return null;
            }
            public String getInitParameter(String s) {
                 if (s.equals(Constants.FILE_LOCATOR_URL)) {
                     return "http://pluto.statsbiblioteket.dk/~bart/get_url.cgi?";
                 } else if (s.equals(Constants.FILE_LOCATOR_CLASS)) {
                     return "dk.statsbiblioteket.doms.radiotv.extractor.transcoder.WebserviceMediafileFinder";
                 } else return null;
            }
            public Enumeration<String> getInitParameterNames() {
                return null;
            }
        };
        TranscodeRequest request = new TranscodeRequest("foobar");
        request.setShard(xml1);
        ProcessorChainElement parser = new ShardParserProcessor();
        parser.processThis(request, config);
        assertEquals(request.getClips().size(), 2);
        TranscodeRequest.FileClip clip1 = request.getClips().get(0);
        assertEquals(clip1.getProgramId().intValue(), 102);
        assertNotNull(clip1.getFilepath());
        assertNull(clip1.getClipLength());
    }


}
