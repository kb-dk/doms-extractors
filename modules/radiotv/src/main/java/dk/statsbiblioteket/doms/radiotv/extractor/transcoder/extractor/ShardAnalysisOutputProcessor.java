/* $Id$
 * $Revision$
 * $Date$
 * $Author$
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
package dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor;

import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.OutputFileUtil;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.ProcessorChainElement;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.ProcessorException;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.TranscodeRequest;

import javax.servlet.ServletConfig;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ShardAnalysisOutputProcessor extends ProcessorChainElement {
    @Override
    protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
        if (request.getStructure().isNonTrivial()) {
             File outputDir = OutputFileUtil.getOutputDir(request, config);
            File outputFile = new File(outputDir, request.getPid()+".txt");
            FileWriter writer;
            try {
                writer = new FileWriter(outputFile);
            } catch (IOException e) {
                throw new ProcessorException(e);
            }
            try {
                writer.append(request.getStructure().toString());
            } catch (IOException e) {
                throw new ProcessorException(e);
            }
            try {
                writer.close();
            } catch (IOException e) {
                throw new ProcessorException(e);
            }
        }
    }
}
