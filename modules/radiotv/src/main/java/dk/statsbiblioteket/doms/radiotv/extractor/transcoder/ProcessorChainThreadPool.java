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

import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.log4j.Logger;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.Queue;

public class ProcessorChainThreadPool {

    private static Logger log = Logger.getLogger(ProcessorChainThreadPool.class);

    /**
     * Singleton instance
     */
    private static ProcessorChainThreadPool instance;
    private static Queue<ProcessorChainThread> theQueue;
    private static GenericObjectPool thePool;
    private static int maxActiveProcesses = 2;     //TODO set from setting

    /**
     * Singleton.
     */
    private ProcessorChainThreadPool() {
        theQueue = new LinkedBlockingQueue<ProcessorChainThread>();
        GenericObjectPool thePool = new GenericObjectPool(new BasePoolableObjectFactory() {
            @Override
            public Object makeObject() throws Exception {
                return new Object();    }
        }, maxActiveProcesses);
    }

    public static synchronized ProcessorChainThreadPool getInstance() {
        if (instance == null) {
            instance = new ProcessorChainThreadPool();
            startHarvestingThread();
        }
        return instance;
    }

    public static synchronized void addProcessorChainThread(ProcessorChainThread thread) {
         getInstance();
         theQueue.add(thread);        
    }

    private static void startHarvestingThread() {
         new Thread() {
             @Override
             public void run() {
                 while(true) {
                     ProcessorChainThread theThread = theQueue.remove();
                     try {
                         Object lockObject = thePool.borrowObject();
                         theThread.setBorrowedObjectAndPool(lockObject, thePool);
                         theThread.start();
                     } catch (Exception e) {
                         log.error("Unexpected error starting transcoding process", e);
                     }
                 }
             }
         }.start();
    }

}
