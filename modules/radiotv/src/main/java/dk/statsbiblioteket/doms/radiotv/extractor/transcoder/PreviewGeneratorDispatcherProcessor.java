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

import javax.servlet.ServletConfig;

/**
 * Class which sets its own child element to the appropriate preview-generator for the given media type
 */
public class PreviewGeneratorDispatcherProcessor extends ProcessorChainElement {
    @Override
    protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
        switch(request.getClipType()) {
            case MUX:
                ProcessorChainElement previewer = new DigitvPreviewExtractor();
                this.setChildElement(previewer);
                break;
            case MPEG1:
                throw new RuntimeException("not implemented");
            case MPEG2:
                throw new RuntimeException("not implemented");
            case WAV:
                throw new RuntimeException("not implemented");
                
        }
    }
}
