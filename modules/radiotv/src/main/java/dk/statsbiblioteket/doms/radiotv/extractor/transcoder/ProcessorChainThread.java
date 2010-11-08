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

import org.apache.log4j.Logger;
import org.apache.commons.pool.ObjectPool;

import javax.servlet.ServletConfig;

public class ProcessorChainThread extends Thread {

    private static Logger log = Logger.getLogger(ProcessorChainThread.class);

    private ProcessorChainElement tailElement;
    private TranscodeRequest request;
    private ServletConfig config;



    public ProcessorChainThread(ProcessorChainElement tailElement, TranscodeRequest request, ServletConfig config) {
        super("TranscodeProcessor");
        log.info("Created processor chain for '" + request.getPid() + "'");
        this.tailElement = tailElement;
        this.request = request;
        this.config = config;
    }

    public ServletConfig getConfig() {
        return config;
    }

    public void setConfig(ServletConfig config) {
        this.config = config;
    }

    public TranscodeRequest getRequest() {
        return request;
    }

    public void setRequest(TranscodeRequest request) {
        this.request = request;
    }

    /**
     * This method calls the given processor chain defined by the tail element, the request and the context specified
     * by the config parameter. It acts as fault barrier for the checked ProcessorExceptions thrown by the elements
     * in the chain. Checked exceptions are rethrown as RuntimeExceptions.
     */
    @Override
    public void run() {
        super.run();
        log.info("Starting processor chain for '" + request.getPid() + "'");
        try {
            tailElement.process(request, config);
        } catch (ProcessorException e) {
            log.error("Processing failed for '" + request.getPid() + "'", e);
            throw new RuntimeException(e);
        } finally {
            Util.unlockRequest(request);
            log.info("Cleaning up after processing '" + request.getPid() + "'");
            ClipStatus.getInstance().remove(request.getPid());
            //TODO cleanup any temporary files. Create error files.
        }
    }
}
