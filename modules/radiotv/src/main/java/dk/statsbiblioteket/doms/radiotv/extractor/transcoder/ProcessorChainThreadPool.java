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
package dk.statsbiblioteket.doms.radiotv.extractor.transcoder;

import dk.statsbiblioteket.doms.radiotv.extractor.Constants;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.Queue;

public class ProcessorChainThreadPool {

    private static final Logger log = Logger.getLogger(ProcessorChainThreadPool.class);

    /**
     * Singleton instance
     */
    private static ProcessorChainThreadPool instance;
    private static LinkedBlockingQueue<ProcessorChainThread> theQueue;
    private static GenericObjectPool thePool;
    private static int maxActiveProcesses;

    
    
    /**
     * Singleton.
     */
    private ProcessorChainThreadPool(ServletConfig config) {
        theQueue = new LinkedBlockingQueue<ProcessorChainThread>();
        maxActiveProcesses = Integer.parseInt(config.getInitParameter(Constants.MAX_ACTIVE_PROCESSING));
        log.info("Creating thread pool with max active processes = " + maxActiveProcesses);
        thePool = new GenericObjectPool(new BasePoolableObjectFactory() {
            @Override
            public Object makeObject() throws Exception {
                return new Object();    }
        }, maxActiveProcesses);
    }

    public static synchronized ProcessorChainThreadPool getInstance(ServletConfig config) {
        if (instance == null) {
            instance = new ProcessorChainThreadPool(config);
            startHarvestingThread();
        }
        return instance;
    }

    public static synchronized void addProcessorChainThread(ProcessorChainThread thread) {
         getInstance(thread.getConfig());
         theQueue.add(thread);        
    }

    private static void startHarvestingThread() {
         new Thread() {
             @Override
             public void run() {
                 while(true) {
                     try {
                         ProcessorChainThread theThread = theQueue.take();
                         try {
                             Object lockObject = thePool.borrowObject();
                             theThread.getRequest().setLockObject(lockObject);
                             theThread.getRequest().setThePool(thePool);
                             log.info("Locking request '" + theThread.getRequest().getPid() + "' with '" + lockObject +"");
                             theThread.start();
                         } catch (Exception e) {
                             log.error("Unexpected error starting transcoding process", e);
                         }
                     }
                     catch (InterruptedException e) {
                        log.error("Unexpected error, trying to recover", e);
                     }
                 }
             }
         }.start();
    }

    public int getPosition(TranscodeRequest request) {
        Iterator<ProcessorChainThread> threads = theQueue.iterator();
        int position = 0;
        while (threads.hasNext()) {
            position++;
            if (threads.next().getRequest().getPid().equals(request.getPid())) return position;
        } return 0;
    }

}
