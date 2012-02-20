/* $Id: ExtractionStatusExtractor.java 1762 2011-03-15 15:03:07Z csrster $
 * $Revision: 1762 $
 * $Date: 2011-03-15 16:03:07 +0100 (Tue, 15 Mar 2011) $
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
package dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor;

import dk.statsbiblioteket.doms.radiotv.extractor.ObjectStatusEnum;
import dk.statsbiblioteket.doms.radiotv.extractor.Constants;

import javax.servlet.ServletConfig;
import java.io.UnsupportedEncodingException;

import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.*;
import org.apache.log4j.Logger;

public class DigitvExtractionStatusExtractor {

    private static Logger log = Logger.getLogger(DigitvExtractionStatusExtractor.class);


    public static DigitvExtractionStatus getStatus(TranscodeRequest request, ServletConfig config) throws UnsupportedEncodingException, ProcessorException {
        boolean isDone = (OutputFileUtil.hasOutputFile(request, config)) && !RequestRegistry.getInstance().isKnown(request);
        Double percentage = 0.0;
        if (isDone) {
            log.debug("Found already fully ready result for program pid: '" + request.getDomsProgramPid() + "'");
            String filename = OutputFileUtil.getDigitvDoneFile(request, config).getName();
            DigitvExtractionStatus status = new DigitvExtractionStatus();
            status.setStatus(ObjectStatusEnum.DONE);
            status.setCompletionPercentage(100.0);
            status.setFilename(filename);
            return status;
        } else if (RequestRegistry.getInstance().isKnown(request)) {
            log.debug("Already started transcoding program pid: '" + request.getDomsProgramPid() + "'");
            request = RequestRegistry.getInstance().get(request);
            DigitvExtractionStatus status = new DigitvExtractionStatus();
            if (OutputFileUtil.hasOutputFile(request, config)) {
                final long outputFileLength = OutputFileUtil.getExistingMediaOutputFile(request, config).length();
                if (request != null && request.getFinalFileLengthBytes() != null && request.getFinalFileLengthBytes() != 0) {
                    double completionPercentage = 100.0 * outputFileLength/request.getFinalFileLengthBytes();
                    completionPercentage = Math.round(completionPercentage);
                    status.setCompletionPercentage(completionPercentage);
                }
                status.setFileLengthBytes(outputFileLength);
                percentage = status.getCompletionPercentage();
            }
            String filename = OutputFileUtil.getDigitvDoneFile(request, config).getName();
            status.setFilename(filename);
            status.setOffsetStart(request.getUserAdditionalStartOffset());
            status.setOffsetEnd(request.getUserAdditionalEndOffset());
            status.setStatus(ObjectStatusEnum.STARTED);
            if (percentage != null && percentage < 0.00001) {
                status.setPositionInQueue(Util.getQueuePosition(request, config));
            }
            return status;
        } else return null;
    }
}
