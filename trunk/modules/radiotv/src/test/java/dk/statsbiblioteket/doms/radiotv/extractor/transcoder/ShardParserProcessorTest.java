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

	private static final String SERVICE_BASE_URL_STRING = "http://plufire/~bart/stage_files.cgi";

    public static String xml1 = "<shard_metadata><file>\n" +
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

    public String xml2 = ""+
    "<shard_metadata>" +
     " <file>        " +
      "  <file_url>http://bitfinder.statsbiblioteket.dk/bart/mux2.1270940400-2010-04-11-01.00.00_1270944000-2010-04-11-02.00.00_dvb1-2.ts</file_url>      " +
       " <channel_id>2010</channel_id>                                                                                                                       " +
        "<program_start_offset>1500</program_start_offset>                                                                                                      " +
       " <program_clip_length>2100</program_clip_length>                                                                                                           " +
        "<file_name>mux2.1270940400-2010-04-11-01.00.00_1270944000-2010-04-11-02.00.00_dvb1-2.ts</file_name>                                                          " +
        "<format_uri>info:pronom/x-fmt/386</format_uri>                                                                                                             " +
     " </file>                                                                                                                                                         " +
      "<file>                                                                                                                                                             " +
       " <file_url>http://bitfinder.statsbiblioteket.dk/bart/mux2.1270944000-2010-04-11-02.00.00_1270947600-2010-04-11-03.00.00_dvb1-1.ts</file_url>                       " +
        "<channel_id>2010</channel_id>                                                                                                                                        " +
        "<program_start_offset>0</program_start_offset>                                                                                                      " +
        "<program_clip_length>3600</program_clip_length>                                                                                                  " +
        "<file_name>mux2.1270944000-2010-04-11-02.00.00_1270947600-2010-04-11-03.00.00_dvb1-1.ts</file_name>                                                    " +
        "<format_uri>info:pronom/x-fmt/386</format_uri>                                                                                                            " +
      "</file>                                                                                                                                                        " +
    "</shard_metadata> ";


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
                     return SERVICE_BASE_URL_STRING;
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

    public void testNullOffsetBytes() throws ProcessorException {
            ServletConfig config = new ServletConfig(){
            public String getServletName() {
                return null;
            }
            public ServletContext getServletContext() {
                return null;
            }
            public String getInitParameter(String s) {
                 if (s.equals(Constants.FILE_LOCATOR_URL)) {
                     return SERVICE_BASE_URL_STRING;
                 } else return null;
            }
            public Enumeration<String> getInitParameterNames() {
                return null;
            }
        };
        TranscodeRequest request = new TranscodeRequest("foobar");
        request.setShard(xml2);
        ProcessorChainElement parser = new ShardParserProcessor();
        parser.processThis(request, config);
        assertNotNull(request.getClips().get(0).getStartOffsetBytes());
    }

}
