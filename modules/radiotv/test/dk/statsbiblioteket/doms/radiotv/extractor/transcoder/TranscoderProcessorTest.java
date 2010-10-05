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
import dk.statsbiblioteket.util.Files;
import junit.framework.TestCase;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

public class TranscoderProcessorTest extends TestCase {

    private File tempdir = new File("./tempdir");
    private File finaldir = new File("./finaldir");

    public void setUp() throws IOException {
        if (tempdir.exists()) Files.delete(tempdir);
        if (finaldir.exists()) Files.delete(finaldir);
        //TODO: check if source file exists and download by ftp if it doesn't
    }

    public void tearDown() throws IOException {
        //if (tempdir.exists()) Files.delete(tempdir);
    }

    public void testProcess() throws IOException {

        ServletConfig config = new ServletConfig(){
            public String getServletName() {
                return null;
            }
            public ServletContext getServletContext() {
                return null;
            }
            public String getInitParameter(String s) {
                 if (s.equals(Constants.TEMP_DIR_INIT_PARAM)) {
                     return "./tempdir";
                 } else if (s.equals(Constants.FINAL_DIR_INIT_PARAM)) {
                     return "./finaldir";
                 } else return null;
            }
            public Enumeration<String> getInitParameterNames() {
                return null;
            }
        };

        String inputFile = "./muxdata/foobar_first.ts";
        tempdir.mkdirs();
        Files.copy(new File(inputFile), new File(tempdir, "foobar_first.ts"), true);
        TranscodeRequest request = new TranscodeRequest("foobar");
        (new TranscoderProcessor()).process(request, config);
        File outputFile = new File(finaldir, "foobar.mp4");
        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 1000000L);
    }
}
