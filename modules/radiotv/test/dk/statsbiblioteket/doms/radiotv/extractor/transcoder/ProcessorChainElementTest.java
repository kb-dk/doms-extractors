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
import java.io.File;
import java.util.Arrays;
import java.util.Enumeration;

public class ProcessorChainElementTest extends TestCase {

    /**
     * Test the processor chain from after shard-parsing to the final file.
     */
    public void testChainBartMpeg() {
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
                } else if (s.equals(Constants.FINAL_DIR_INIT_PARAM)) {
                    return "./outdir" ;
                } else if (s.equals(Constants.AUDIO_BITRATE)) {
                    return "96";
                } else if (s.equals(Constants.VIDEO_BITRATE)) {
                    return "200";
                }
                else return null;
            }
            public Enumeration<String> getInitParameterNames() {
                return null;
            }
        };
        String mpegfile = "./muxdata/testdata_mpeg1_testdata.mpeg";
        assertTrue("Test file should exist: " + (new File(mpegfile)).getAbsolutePath(), (new File(mpegfile)).exists());
        TranscodeRequest.FileClip clip1 = new TranscodeRequest.FileClip(mpegfile);
        int clipLengthSeconds = 180;
        clip1.setStartOffsetBytes(1000000000L);
        clip1.setClipLength(ClipTypeEnum.MPEG1.getBitrate()*clipLengthSeconds);
        TranscodeRequest request = new TranscodeRequest("foobar");
        request.setClipType(ClipTypeEnum.MPEG1);
        request.setClips(Arrays.asList(clip1));
        request.setTotalLengthSeconds((long) clipLengthSeconds);
        ProcessorChainElement transcoder = new TranscoderProcessor();
        ProcessorChainElement demuxer = new DemuxerProcessor();
        ProcessorChainElement estimator = new EstimatorProcessor();
        ProcessorChainElement aspecter = new AspectRatioDetectorProcessor();

        transcoder.setParentElement(demuxer);
        demuxer.setParentElement(estimator);
        estimator.setParentElement(aspecter);

        ProcessorChainThread thread = new ProcessorChainThread(transcoder, request, config);
        thread.run();
        assertTrue(Util.getFlashFile(request, config).exists());
        assertTrue(Util.getFlashFile(request, config).length() > 1000000L);
    }

}
