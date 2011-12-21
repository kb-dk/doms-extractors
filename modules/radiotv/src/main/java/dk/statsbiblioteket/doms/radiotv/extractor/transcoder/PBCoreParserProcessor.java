package dk.statsbiblioteket.doms.radiotv.extractor.transcoder;

import dk.statsbiblioteket.doms.central.CentralWebservice;
import dk.statsbiblioteket.doms.central.Relation;
import dk.statsbiblioteket.doms.radiotv.extractor.DomsClient;
import dk.statsbiblioteket.doms.radiotv.extractor.updateidentifier.BroadcastExtractor;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: csr
 * Date: 12/20/11
 * Time: 2:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class PBCoreParserProcessor extends ProcessorChainElement {

    private static Logger log = Logger.getLogger(PBCoreParserProcessor.class);

    private static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();


    SimpleDateFormat domsDateFormat = new SimpleDateFormat("yyyy-MM-ddTHH:mm:SSZ");

    @Override
    protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
        String pbcore = null;
        CentralWebservice domsAPI = DomsClient.getDOMSApiInstance(config);
        try {
            List<Relation> relations = domsAPI.getInverseRelations(request.getPid());
            for (Relation relation: relations) {
                if (relation.getPredicate().equals("http://doms.statsbiblioteket.dk/relations/default/0/1/#hasShard")) {
                    String programPid = relation.getSubject();
                    pbcore = domsAPI.getDatastreamContents(programPid, "PBCORE");
                    log.debug(pbcore);
                }
            }
        } catch (Exception e) {
            throw new ProcessorException(e);
        }
        org.w3c.dom.Document pbcoreDocument = null;
        try {
            DocumentBuilder builder = dbf.newDocumentBuilder();
            ByteArrayInputStream is = new ByteArrayInputStream(pbcore.getBytes());
            pbcoreDocument = builder.parse(is);
        } catch (Exception e) {
            throw new ProcessorException(e);
        }
        XPathFactory xpathFactory = XPathFactory.newInstance();
        Long programStartTime;
        Long programEndTime;
        try {
            String programStartString = (String) xpathFactory.newXPath().evaluate("dateAvailableStart", pbcoreDocument, XPathConstants.STRING);
            String programEndString = (String) xpathFactory.newXPath().evaluate("dateAvailableEnd", pbcoreDocument, XPathConstants.STRING);
            programStartTime = domsDateFormat.parse(programStartString).getTime();
            programEndTime = domsDateFormat.parse(programEndString).getTime();
        } catch (Exception e) {
            throw new ProcessorException(e);
        }
        request.setProgramStartTime(programStartTime);
        request.setProgramEndTime(programEndTime);
        log.debug("Identified start time '" + programStartTime);
        log.debug("Identified end time '" + programEndTime);
    }
}
