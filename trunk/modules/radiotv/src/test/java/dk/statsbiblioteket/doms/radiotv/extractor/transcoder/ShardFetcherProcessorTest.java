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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShardFetcherProcessorTest extends TestCase {

    ServletConfig config = new ServletConfig(){
        public String getServletName() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
        public ServletContext getServletContext() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
        public String getInitParameter(String s) {
            if (s.equals(Constants.DOMS_USERNAME)) return "fedoraAdmin";
            if (s.equals(Constants.DOMS_PASSWORD)) return "fedoraAdminPass";
            throw new RuntimeException("Key not defined:'" + s + "'");
        }
        public Enumeration<String> getInitParameterNames() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    };

    public void testProcessThis() throws MalformedURLException, ProcessorException {
        TranscodeRequest request = new TranscodeRequest("ef8ea1b2-aaa8-412a-a247-af682bb57d25");
        ShardFetcherProcessor processor = new ShardFetcherProcessor();
        processor.processThis(request, config);
        assertTrue(request.getShard().length()>100);
        assertTrue(request.getShard().contains("channel_id"));
    }


    public void testProcessThisII() throws ProcessorException {
    	TranscodeRequest request = new TranscodeRequest("5fc9543d-908e-40d2-b48d-e9824ed83e4e");
    	ShardFetcherProcessor shardFetcher = new ShardFetcherProcessor();
		shardFetcher.processThis(request, config);
		Pattern p = Pattern.compile("(.|\n)*<file_url>(.*)</file_url>(.|\n)*");
		Matcher m = p.matcher(request.getShard());
		assertTrue("Expected to match pattern: " + p.pattern(), m.matches());
		String shardFileUrl = m.group(2);
		String expectedResult = "http://bitfinder.statsbiblioteket.dk/bart/mux1.1287547200-2010-10-20-06.00.00_1287550800-2010-10-20-07.00.00_dvb1-1.ts"; 
		assertEquals(expectedResult, shardFileUrl);
    }

}
