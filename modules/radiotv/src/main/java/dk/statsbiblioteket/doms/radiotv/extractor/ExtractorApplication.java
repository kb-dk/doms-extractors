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
package dk.statsbiblioteket.doms.radiotv.extractor;

import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.*;
import dk.statsbiblioteket.util.Files;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

public class ExtractorApplication {

    private static ServletConfig config = new ServletConfig() {
        @Override
        public String getServletName() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public ServletContext getServletContext() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getInitParameter(String s) {
            if (s.equals(Constants.FILE_LOCATOR_CLASS)) {
                return "dk.statsbiblioteket.doms.radiotv.extractor.transcoder.WebserviceMediafileFinder";
            }  else if (s.equals(Constants.FILE_LOCATOR_URL)) {
                return "http://pluto.statsbiblioteket.dk/~bart/get_url.cgi?";
            } else if (s.equals(Constants.FINAL_DIR_INIT_PARAM)) {
                return "./output";
            } else if (s.equals(Constants.TEMP_DIR_INIT_PARAM)) {
                return "./tempdir";
            } else return null;
        }

        @Override
        public Enumeration<String> getInitParameterNames() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    };


    /**
     * Command line argument to test extraction of programs
     * @param args a list of xml files containing either shard metadata or
     * preingest xml files. Clip information is extracted from a list of <file>
     * elements.
     */
    public static void main(String[] args) throws IOException {
        for (String file: args) {
            File file1 = new File(file);
            String fileContent = Files.loadString(file1);
            String basename = file1.getName().replaceAll("[.]xml","");
            File finalFile = new File(config.getInitParameter(Constants.FINAL_DIR_INIT_PARAM), basename+".mp4");
            ProcessorChainElement transcoder = new TranscoderProcessor();
            ProcessorChainElement demuxer = new DemuxerProcessor();
            ProcessorChainElement parser = new ShardParserProcessor();
            transcoder.setParentElement(demuxer);
            demuxer.setParentElement(parser);
            TranscodeRequest request = new TranscodeRequest(basename);
            request.setShard(fileContent);
            if (!finalFile.exists()) {
                ProcessorChainThread thread = new ProcessorChainThread(transcoder, request, config);
                thread.run();
            }
        }
    }
}
