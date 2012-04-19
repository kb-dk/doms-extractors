/* $Id: AspectRatioDetectorProcessorTest.java 1720 2011-03-08 12:21:37Z csrster $
 * $Revision: 1720 $
 * $Date: 2011-03-08 13:21:37 +0100 (Tue, 08 Mar 2011) $
 * $Author: csrster $
 *
 * The Netarchive Suite - Software to harvest and preserve websites
 * Copyright 2004-2009 Det Kongelige Bibliotek and Statsbiblioteket, Denmark
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *   USA
 */
package dk.statsbiblioteket.doms.radiotv.extractor.transcoder;

import junit.framework.TestCase;

import java.util.Arrays;
import java.io.File;

public class AspectRatioDetectorProcessorTest extends TestCase {

    public void testProcessThis() throws ProcessorException {
        String muxfile = "./muxdata/mux2.ts";
        assertTrue("Test file should exist: " + (new File(muxfile)).getAbsolutePath(), (new File(muxfile)).exists());
        TranscodeRequest.FileClip clip1 = new TranscodeRequest.FileClip(muxfile);
        clip1.setProgramId(2005);
        TranscodeRequest request = new TranscodeRequest("foobar");
        request.setClips(Arrays.asList(clip1));
        request.setClipType(ClipTypeEnum.MUX);
        (new AspectRatioDetectorProcessor()).processRecursively(request, null);
        assertTrue(request.getDisplayAspectRatio()>1.7);
        assertTrue(request.getDisplayAspectRatio()<1.8);
    }


     public void testProcessThisMpeg() throws ProcessorException {
        String mpegfile = "./muxdata/testdata_mpeg1_testdata.mpeg";
        assertTrue("Test file should exist: " + (new File(mpegfile)).getAbsolutePath(), (new File(mpegfile)).exists());
        TranscodeRequest.FileClip clip1 = new TranscodeRequest.FileClip(mpegfile);
        TranscodeRequest request = new TranscodeRequest("foobar");
         request.setClipType(ClipTypeEnum.MPEG1);
        request.setClips(Arrays.asList(clip1));
        (new AspectRatioDetectorProcessor()).processRecursively(request, null);
        assertTrue(request.getDisplayAspectRatio()>1.8);
        assertTrue(request.getDisplayAspectRatio()<1.82);
    }

}
