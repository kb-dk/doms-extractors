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

import javax.servlet.ServletConfig;
import java.io.File;
import java.io.IOException;
import java.util.Date;

public class ProcessorChainThread extends Thread {

    private static Logger log = Logger.getLogger(ProcessorChainThread.class);

    private ProcessorChainElement callElement;
    private TranscodeRequest request;
    private ServletConfig config;

    private Long timestamp;

    private boolean isRecursive;

    public Long getTimestamp() {
        return timestamp;
    }

    public static ProcessorChainThread getRecursiveProcessorChainThread(ProcessorChainElement tailElement, TranscodeRequest request, ServletConfig config) {
          return new ProcessorChainThread(tailElement, request, config, true);
    }

     public static ProcessorChainThread getIterativeProcessorChainThread(ProcessorChainElement headElement, TranscodeRequest request, ServletConfig config) {
          return new ProcessorChainThread(headElement, request, config, false);
    }

    private ProcessorChainThread(ProcessorChainElement callElement, TranscodeRequest request, ServletConfig config, boolean isRecursive) {
        this.isRecursive = isRecursive;
        this.callElement = callElement;
        this.request = request;
        this.config = config;
        this.timestamp = (new Date()).getTime();
    }


    private ProcessorChainThread(ProcessorChainElement tailElement, TranscodeRequest request, ServletConfig config) {
        super("TranscodeProcessor");
        log.info("Created processor chain for '" + request.getPid() + "'");
        this.callElement = tailElement;
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
        File lockFile = null;
        try {
            try {
                Util.getTempDir(config).mkdirs();
                lockFile = Util.getLockFile(request, config);
                lockFile.createNewFile();
            } catch (IOException e) {
                log.error("Error creating lock file: '" + Util.getLockFile(request, config).getAbsolutePath() + "'");
            }
            if (isRecursive) {
                callElement.processRecursively(request, config);
            } else {
                callElement.processIteratively(request, config);
            }
        } catch (ProcessorException e) {
            log.error("Processing failed for '" + request.getPid() + "'", e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("Processing failed for '" + request.getPid() + "'", e);
            throw new RuntimeException(e);
        }
        finally {
            Util.unlockRequest(request);
            log.info("Cleaning up after processing '" + request.getPid() + "'");
            RequestRegistry.getInstance().remove(request);
            lockFile.delete();
        }
    }
}
