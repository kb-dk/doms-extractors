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
import java.util.Arrays;
import java.util.Enumeration;

public class DemuxerProcessorTest extends TestCase {

    private File tempdir = new File("./tempdir");

    public void setUp() throws IOException {
        if (tempdir.exists()) Files.delete(tempdir);
        //TODO: check if source file exists and download by ftp if it doesn't
    }

    public void tearDown() throws IOException {
        //if (tempdir.exists()) Files.delete(tempdir);
    }

    public void testProcessSeamless() throws ProcessorException {

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
                 } else if (s.equals(Constants.DEMUXER_ALGORITHM)) {
                     return "seamless";
                 }
                 else return null;
            }
            public Enumeration<String> getInitParameterNames() {
                return null;
            }
        };

        String muxfile = "./muxdata/mux2.ts";
        TranscodeRequest.FileClip clip1 = new TranscodeRequest.FileClip(muxfile);
        clip1.setStartOffsetBytes(500000000L);
        clip1.setProgramId(2005);
        TranscodeRequest.FileClip clip2 = new TranscodeRequest.FileClip(muxfile);
        clip2.setProgramId(2005);
        TranscodeRequest.FileClip clip3 = new TranscodeRequest.FileClip(muxfile);
        clip3.setClipLength(500000000L);
        clip3.setProgramId(2005);
        TranscodeRequest request = new TranscodeRequest("foobar");
        request.setClips(Arrays.asList(new TranscodeRequest.FileClip[]{clip1,clip2,clip3}));
        (new DemuxerProcessor()).process(request, config);
        File outputFile = new File(tempdir, "foobar_first.ts");
        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 1000000L);
    }

    public void testProcessNaive() throws ProcessorException {

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
                 } else if (s.equals(Constants.DEMUXER_ALGORITHM)) {
                     return "naive";
                 }
                 else return null;
            }
            public Enumeration<String> getInitParameterNames() {
                return null;
            }
        };

        String muxfile = "./muxdata/mux2.ts";
        TranscodeRequest.FileClip clip1 = new TranscodeRequest.FileClip(muxfile);
        clip1.setStartOffsetBytes(500000000L);
        clip1.setProgramId(2005);
        TranscodeRequest.FileClip clip2 = new TranscodeRequest.FileClip(muxfile);
        clip2.setProgramId(2005);
        TranscodeRequest.FileClip clip3 = new TranscodeRequest.FileClip(muxfile);
        clip3.setClipLength(500000000L);
        clip3.setProgramId(2005);
        TranscodeRequest request = new TranscodeRequest("foobar");
        request.setClips(Arrays.asList(new TranscodeRequest.FileClip[]{clip1,clip2,clip3}));
        (new DemuxerProcessor()).process(request, config);
        File outputFile = new File(tempdir, "foobar_first.ts");
        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 1000000L);
    }

    public void testProcessChain() throws ProcessorException {

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

        String muxfile = "./muxdata/mux2.ts";
        TranscodeRequest.FileClip clip1 = new TranscodeRequest.FileClip(muxfile);
        clip1.setStartOffsetBytes(2459574L*188L);
        clip1.setProgramId(2005);
        TranscodeRequest.FileClip clip2 = new TranscodeRequest.FileClip(muxfile);
        clip2.setProgramId(2005);
        TranscodeRequest.FileClip clip3 = new TranscodeRequest.FileClip(muxfile);
        clip3.setClipLength(500000000L);
        clip3.setProgramId(2005);
        TranscodeRequest request = new TranscodeRequest("foobar");
        request.setClips(Arrays.asList(new TranscodeRequest.FileClip[]{clip1,clip2,clip3}));
        ProcessorChainElement first = new DemuxerProcessor();
        ProcessorChainElement second = new TranscoderProcessor();
        second.setParentElement(first);
        second.process(request, config);
        File outputFile = new File("./finaldir", "foobar.mp4");
        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 1000000L);
    }

}
