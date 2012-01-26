package dk.statsbiblioteket.doms.radiotv.extractor.transcoder;

import dk.statsbiblioteket.doms.central.CentralWebservice;
import dk.statsbiblioteket.doms.central.Relation;
import dk.statsbiblioteket.doms.radiotv.extractor.DomsClient;
import dk.statsbiblioteket.doms.radiotv.extractor.updateidentifier.BroadcastExtractor;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private String pbcoreNamespace = "http://www.pbcore.org/PBCore/PBCoreNamespace.html";

    public static class PBCoreNamespaceResolver implements NamespaceContext {

        @Override
        public String getNamespaceURI(String prefix) {
            if (prefix.equals("pbcore")) return  "http://www.pbcore.org/PBCore/PBCoreNamespace.html";
            else   return XMLConstants.NULL_NS_URI;
        }

        @Override
        public String getPrefix(String namespaceURI) {
            throw new RuntimeException("Not yet implemented");

        }

        @Override
        public Iterator getPrefixes(String namespaceURI) {
            throw new RuntimeException("Not yet implemented");

        }
    }

    SimpleDateFormat domsDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    Pattern gallupStartPattern = Pattern.compile(".*startDate=(.*)??,", Pattern.DOTALL);
    Pattern gallupEndPattern = Pattern.compile(".*endDate=(.*)??,", Pattern.DOTALL);
    SimpleDateFormat gallupDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");


    @Override
    protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
        String pbcore = null;
        CentralWebservice domsAPI = DomsClient.getDOMSApiInstance(config);
        pbcore = getPBCore(request, pbcore, domsAPI);
        String gallupOriginal = getGallup_Original(request,domsAPI);
        parsePBCore(request, pbcore);
        if (gallupOriginal != null) {
            gallupOriginal = gallupOriginal.replaceAll("&apos;","");
            Matcher startMatcher = gallupStartPattern.matcher(gallupOriginal);
            if (startMatcher.matches()) {
                String match = startMatcher.group(1);
                log.debug("Found TVMeter start time :" + match);
                try {
                    Date startDate = gallupDateFormat.parse(match);
                    request.setProgramStartTime(startDate.getTime());
                } catch (ParseException e) {
                    log.error("Unexpected date/time format in Gallup record: '" + match + "' does not match '" + gallupDateFormat + "'" );
                }
            } else {
                log.error("Could not find start time: '" + gallupStartPattern + "' in '" + gallupOriginal + "'");
            }
            Matcher endMatcher = gallupEndPattern.matcher(gallupOriginal);
            if (endMatcher.matches()) {
                String match = endMatcher.group(1);
                log.debug("Found TVMeter end time :" + match);
                try {
                    Date endDate = gallupDateFormat.parse(match);
                    request.setProgramEndTime(endDate.getTime());
                } catch (ParseException e) {
                    log.error("Unexpected date/time format in Gallup record: '" + match + "' does not match '" + gallupDateFormat + "'" );
                }
            }
        }
    }

    void parsePBCore(TranscodeRequest request, String pbcore) throws ProcessorException {
        org.w3c.dom.Document pbcoreDocument = null;
        dbf.setNamespaceAware(true);
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
            final XPath xPath = xpathFactory.newXPath();
            xPath.setNamespaceContext(new PBCoreNamespaceResolver());
            String programStartString = (String) xPath.evaluate("//pbcore:dateAvailableStart", pbcoreDocument, XPathConstants.STRING);
            String programEndString = (String) xPath.evaluate("//pbcore:dateAvailableEnd", pbcoreDocument, XPathConstants.STRING);
            log.debug("Found start time '" + programStartString + "'");
            log.debug("Found end time '" + programEndString + "'");
            programStartTime = domsDateFormat.parse(programStartString).getTime();
            programEndTime = domsDateFormat.parse(programEndString).getTime();
        } catch (Exception e) {
            throw new ProcessorException(e);
        }
        request.setProgramStartTime(programStartTime);
        request.setProgramEndTime(programEndTime);
        Date startDate = new Date(); startDate.setTime(programStartTime);
        Date endDate = new Date(); endDate.setTime(programEndTime);
        log.debug("Identified start time '" + programStartTime + "' (" + startDate + ")");
        log.debug("Identified end time '" + programEndTime + "' (" + endDate + ")");
    }

    private String getPBCore(TranscodeRequest request, String pbcore, CentralWebservice domsAPI) throws ProcessorException {
        try {
            final String domsPid = "uuid:" + request.getPid();
            List<Relation> relations = domsAPI.getInverseRelations(domsPid);
            if (relations == null || relations.isEmpty()) {
                throw new ProcessorException("Found no inverse relations for '" + domsPid + "'");
            }
            for (Relation relation: relations) {
                if (relation.getPredicate().equals("http://doms.statsbiblioteket.dk/relations/default/0/1/#hasShard")) {
                    String programPid = relation.getSubject();
                    pbcore = domsAPI.getDatastreamContents(programPid, "PBCORE");
                    if (pbcore == null) {
                        throw new ProcessorException("Returned null PBCORE data for '" + programPid + "'");
                    }
                    log.debug(pbcore);
                }
            }
        } catch (Exception e) {
            throw new ProcessorException(e);
        }
        return pbcore;
    }

    private String getGallup_Original (TranscodeRequest request, CentralWebservice domsAPI) throws ProcessorException {
        String pbcore = null;
        try {
            final String domsPid = "uuid:" + request.getPid();
            List<Relation> relations = domsAPI.getInverseRelations(domsPid);
            if (relations == null || relations.isEmpty()) {
                throw new ProcessorException("Found no inverse relations for '" + domsPid + "'");
            }
            for (Relation relation: relations) {
                if (relation.getPredicate().equals("http://doms.statsbiblioteket.dk/relations/default/0/1/#hasShard")) {
                    String programPid = relation.getSubject();
                    pbcore = domsAPI.getDatastreamContents(programPid, "GALLUP_ORIGINAL");
                    if (pbcore == null) {
                        log.debug("Returned null GALLUP_ORIGINAL data for '" + programPid + "'");
                    }
                    log.debug(pbcore);
                }
            }
        } catch (Exception e) {
            throw new ProcessorException(e);
        }
        return pbcore;
    }

}
