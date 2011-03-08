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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ExternalJobRunner {
     private final StringBuffer standard_out = new StringBuffer();
    private final StringBuffer standard_err = new StringBuffer();
    private int exit_code;

    /**
     * Runs an external command, blocking until the external processRecursively ends after which
     * the output and errors can be read.
     * @param command
     * @throws java.io.IOException
     * @throws InterruptedException
     */
    public ExternalJobRunner(String... command) throws IOException, InterruptedException {
        final Process p;
        if (command.length == 1) {
             p = Runtime.getRuntime().exec(command[0]);
        } else {
             p = Runtime.getRuntime().exec(command);
        }

        class StreamHarvester implements Runnable {
            public static final String OUT = "standard_out";
            public static final String ERR = "standard_err";
            private StringBuffer buffer;
            private InputStream stream;

            public boolean started = false;

            public StreamHarvester (String stream_type) {
                if (stream_type.equals(OUT)) {
                    buffer = standard_out;
                    stream = p.getInputStream();
                } else if (stream_type.equals(ERR)) {
                    buffer = standard_err;
                    stream = p.getErrorStream();
                } else {
                    throw new RuntimeException("Argument to " +
                            "StreamHarvester constructor '"+stream_type+"' is not recognised");
                }
            }

            public void run() {
                 synchronized(buffer) {
                    started = true;
                    String line;
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                    try {
                        while ((line = reader.readLine()) != null) {
                            buffer.append(line+"\n");
                        }
                    } catch (IOException e) {
                        //TODO need logging
                    }
                }
            }

        }


        StreamHarvester out_harvester = new StreamHarvester(StreamHarvester.OUT);
        StreamHarvester err_harvester = new StreamHarvester(StreamHarvester.ERR);
        (new Thread(out_harvester)).start();
        (new Thread(err_harvester)).start();
        p.waitFor();
        exit_code = p.exitValue();
        // We busy-wait here to make sure that we do not exit this method before the
        // harvester threads have got a lock on the output buffers
        while (! (out_harvester.started && err_harvester.started)) { }
    }


    public String getOutput() {
        synchronized(standard_out) {
        return standard_out.toString();
        }
    }

    public String getError() {
        synchronized(standard_err) {
        return standard_err.toString();
        }
    }

    public int getExitValue() {
        return exit_code;
    }
}
