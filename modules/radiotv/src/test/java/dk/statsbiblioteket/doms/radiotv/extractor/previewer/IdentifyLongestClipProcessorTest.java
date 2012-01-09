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
package dk.statsbiblioteket.doms.radiotv.extractor.previewer;

import dk.statsbiblioteket.doms.radiotv.extractor.Constants;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.ProcessorChainElement;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.ProcessorException;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.ShardParserProcessor;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.TranscodeRequest;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.previewer.IdentifyLongestClipProcessor;
import junit.framework.TestCase;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;

public class IdentifyLongestClipProcessorTest extends TestCase {

    private String xml1 = "<shard_metadata>\n" +
            "  <file>\n" +
            "    <file_url>http://bitfinder.statsbiblioteket.dk/bart/mux1.1295359200-2011-01-18-15.00.00_1295362800-2011-01-18-16.00.00_dvb1-1.ts</file_url>\n" +
            "    <channel_id>213</channel_id>\n" +
            "    <program_start_offset>1410</program_start_offset>\n" +
            "    <program_clip_length>2180</program_clip_length>\n" +
            "    <file_name>mux1.1295359200-2011-01-18-15.00.00_1295362800-2011-01-18-16.00.00_dvb1-1.ts</file_name>\n" +
            "\n" +
            "    <format_uri>info:pronom/x-fmt/386</format_uri>\n" +
            "  </file>\n" +
            "  <file>\n" +
            "    <file_url>http://bitfinder.statsbiblioteket.dk/bart/mux1.1295362800-2011-01-18-16.00.00_1295366400-2011-01-18-17.00.00_dvb1-1.ts</file_url>\n" +
            "    <channel_id>213</channel_id>\n" +
            "    <program_start_offset>0</program_start_offset>\n" +
            "    <program_clip_length>90</program_clip_length>\n" +
            "\n" +
            "    <file_name>mux1.1295362800-2011-01-18-16.00.00_1295366400-2011-01-18-17.00.00_dvb1-1.ts</file_name>\n" +
            "    <format_uri>info:pronom/x-fmt/386</format_uri>\n" +
            "  </file>\n" +
            "</shard_metadata>" ;

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
        ProcessorChainElement longer = new IdentifyLongestClipProcessor();
        parser.setChildElement(longer);
        parser.processIteratively(request, config);
        assertTrue(request.getLongestClip().getFilepath().contains("15.00.00"));
    }

}
