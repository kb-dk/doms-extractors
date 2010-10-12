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

public class ProcessorChainThread extends Thread {

    private ProcessorChainElement tailElement;
    private TranscodeRequest request;
    private ServletConfig config;

    public ProcessorChainThread(ProcessorChainElement tailElement, TranscodeRequest request, ServletConfig config) {
        super("TranscodeProcessor");
        this.tailElement = tailElement;
        this.request = request;
        this.config = config;
    }

    /**
     * This method calls the given processor chain defined by the tail element, the request and the context specified
     * by the config parameter. It acts as fault barrier for the checked ProcessorExceptions thrown by the elements
     * in the chain. Checked exceptions are rethrown as RuntimeExceptions.
     */
    @Override
    public void run() {
        super.run();
        try {
            tailElement.process(request, config);
        } catch (ProcessorException e) {
            //TODO add any necessary logging
            throw new RuntimeException(e);
        } finally {
            //TODO cleanup any temporary files. Create error files.
        }
    }
}
