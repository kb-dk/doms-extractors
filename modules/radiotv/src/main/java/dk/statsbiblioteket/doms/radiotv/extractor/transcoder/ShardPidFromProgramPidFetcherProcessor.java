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

import dk.statsbiblioteket.doms.central.CentralWebservice;
import dk.statsbiblioteket.doms.central.InvalidCredentialsException;
import dk.statsbiblioteket.doms.central.InvalidResourceException;
import dk.statsbiblioteket.doms.central.MethodFailedException;
import dk.statsbiblioteket.doms.central.Relation;
import dk.statsbiblioteket.doms.radiotv.extractor.Constants;
import dk.statsbiblioteket.doms.radiotv.extractor.DomsClient;
import org.apache.log4j.Logger;
import sun.misc.BASE64Encoder;

import javax.servlet.ServletConfig;
import javax.xml.ws.BindingProvider;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShardPidFromProgramPidFetcherProcessor extends ProcessorChainElement {

    private static Logger log = Logger.getLogger(ShardPidFromProgramPidFetcherProcessor.class);

    /**
     * Fetches the shard pid.
     */
    @Override
    protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
    	// New version of interface. Use when DOMS is ready in PROD
    	String programPid = request.getDomsProgramPid();
    	log.debug("Doms client looking up program pid: " + programPid);
    	try {
    		String shardPid = null;
			CentralWebservice domsAPI = DomsClient.getDOMSApiInstance(config);
			// DEBUG start
			/*
			Map<String, String> domsMock = new HashMap<String, String>(); // ProgramPid -> ShardPid
			domsMock.put("4fcbe29e-c309-4d52-84b9-23dd6d4fd865", "08e052bf-573b-454d-9db6-665da3c81c88"); // DR1 programpid=uuid%3A4fcbe29e-c309-4d52-84b9-23dd6d4fd865&title=Gintberg+på+kanten+-+Thorshavn&channel=dr1&date=1321623000000&additional_start_offset=0&additional_end_offset=0&filename_prefix=dr1_Gintberg_p__kanten___Thorshavn_2011-11-18_14-30
			domsMock.put("70d5a420-f383-459c-9d57-7e2485c38ee2", "3e884292-ae05-41c0-b6ef-3682d00444b7"); // DR2 programpid=uuid%3A70d5a420-f383-459c-9d57-7e2485c38ee2&title=Monty+Pythons+bedste&channel=dr2&date=1321636200000&additional_start_offset=0&additional_end_offset=0&filename_prefix=dr2_Monty_Pythons_bedste_2011-11-18_18-10
			Map<String, Object>  domsAPILogin = ((BindingProvider) domsAPI).getRequestContext();
			log.debug("Doms user: " + domsAPILogin.get(BindingProvider.USERNAME_PROPERTY));
			log.debug("Doms pass: " + domsAPILogin.get(BindingProvider.PASSWORD_PROPERTY));
			//List<Relation> programRelations = new ArrayList<Relation>();
			log.warn("Skipping call to DOMS !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			shardPid = domsMock.get(programPid);
			if (shardPid == null) {
				throw new RuntimeException("Mock DOMS did not contain pid " + programPid + " in map");
			}
			*/
			// DEBUG end
			List<Relation> programRelations = domsAPI.getRelations("uuid:"+ programPid);
			log.debug("Found relations: " + programRelations);
			for (Relation relation : programRelations) {
				log.debug("DOMS relation to program " + programPid + ": " + relation.getPredicate() + " - " + relation.getObject());
				if ("http://doms.statsbiblioteket.dk/relations/default/0/1/#hasShard".equals(relation.getPredicate())) {
					shardPid = relation.getObject();
				}
			}
			if (shardPid != null) {
				request.setPid(Util.getUuid(shardPid));
			} else {
				throw new ProcessorException("Could not retrieve shard pid from program pid: uuid:" + programPid);
			}
		} catch (InvalidCredentialsException e) {
			String message = "Invalid DOMS parameters i property file"; 
			log.error(message, e);
			throw new RuntimeException(message, e);
		} catch (InvalidResourceException e) {
			String message = "Invalid DOMS parameters i property file"; 
			log.error(message, e);
			throw new RuntimeException(message, e);
		} catch (MethodFailedException e) {
			String message = "DOMS method failed"; 
			log.warn(message, e);
			// Could continue, but found no means of stopping chain.
			throw new RuntimeException(message, e);
		} catch (UnsupportedEncodingException e) {
			String message = "Unsupported encoding while parsing shard uuid"; 
			log.error(message, e);
			throw new RuntimeException(message, e);
		}
    }
}
