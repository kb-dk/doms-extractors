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
package dk.statsbiblioteket.doms.radiotv.extractor.transcoder.previewer;

import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.ProcessorChainElement;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.ProcessorException;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.TranscodeRequest;

import javax.servlet.ServletConfig;
import java.io.File;

public class IdentifyLongestClipProcessor extends ProcessorChainElement {

    private static TranscodeRequest.FileClip getLongestClip(TranscodeRequest request) {
        TranscodeRequest.FileClip longestClip = null;
        long lengthOfLongestClip = 0L;
        final int clipSize = request.getClips().size();
        for (int iclip = 0; iclip < clipSize; iclip++ ) {
            TranscodeRequest.FileClip clip = request.getClips().get(iclip);
            Long clipLength = clip.getClipLength();
            Long clipOffset = clip.getStartOffsetBytes();
            // A clip can have:
            // neither length nor offset, so length = whole file
            // both length and offset, so length = length specified
            // offset, but no length, so length = filelength - offset
            // no offset, but length
            if (clipLength != null) {
                if (clipLength > lengthOfLongestClip) {
                    longestClip = clip;
                    lengthOfLongestClip = clipLength;
                }
            } else if (clipLength == null && clipOffset == null) {
               long fileLength = new File(clip.getFilepath()).length();
                if (fileLength > lengthOfLongestClip) {
                    longestClip = clip;
                    lengthOfLongestClip = fileLength;
                }
            } else if (clipLength == null && clipOffset != null) {
                clipLength = new File(clip.getFilepath()).length() - clipOffset;
                if (clipLength > lengthOfLongestClip) {
                    longestClip = clip;
                    lengthOfLongestClip = clipLength;
                }
            }
        }
        return longestClip;
    }

    @Override
    protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
        request.setLongestClip(getLongestClip(request));
    }
}
