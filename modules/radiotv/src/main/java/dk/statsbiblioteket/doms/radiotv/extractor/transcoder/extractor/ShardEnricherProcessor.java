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
package dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor;

import dk.statsbiblioteket.doms.central.CentralWebservice;
import dk.statsbiblioteket.doms.central.InvalidCredentialsException;
import dk.statsbiblioteket.doms.central.InvalidResourceException;
import dk.statsbiblioteket.doms.central.MethodFailedException;
import dk.statsbiblioteket.doms.radiotv.extractor.DomsClient;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.*;

import javax.servlet.ServletConfig;
import javax.xml.bind.*;
import java.io.StringReader;
import java.io.StringWriter;

public class ShardEnricherProcessor extends ProcessorChainElement {

    @Override
    protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
        JAXBContext context = null;
        try {
            context = JAXBContext.newInstance(ShardMetadata.class);
        } catch (JAXBException e) {
            throw new ProcessorException(e);
        }
        Unmarshaller um = null;
        try {
            um = context.createUnmarshaller();
        } catch (JAXBException e) {
            throw new ProcessorException(e);
        }
        StringReader reader = new StringReader(request.getShard());
        ShardMetadata shard = null;
        try {
            shard = (ShardMetadata) um.unmarshal(reader);
        } catch (JAXBException e) {
            throw new ProcessorException(e);
        }
        shard.setShardStructure(request.getStructure());
        Marshaller m = null;
        try {
            m = context.createMarshaller();
        } catch (JAXBException e) {
            throw new ProcessorException(e);
        }
        try {
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        } catch (PropertyException e) {
            throw new ProcessorException(e);
        }
        StringWriter writer = new StringWriter();
        try {
            m.marshal(shard, writer);
        } catch (JAXBException e) {
            throw new ProcessorException(e);
        }
        CentralWebservice domsAPI = DomsClient.getDOMSApiInstance(config);
        try {
            domsAPI.modifyDatastream(request.getUuid(), "SHARD_METADATA", writer.getBuffer().toString(), "Added output from shard analysis" );
        } catch (Exception e) {
            throw new ProcessorException(e);
        }
    }
}
