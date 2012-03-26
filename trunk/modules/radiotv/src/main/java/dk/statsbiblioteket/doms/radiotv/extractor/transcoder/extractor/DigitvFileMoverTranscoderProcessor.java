package dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor;

import java.io.File;

import javax.servlet.ServletConfig;

import org.apache.log4j.Logger;

import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.OutputFileUtil;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.ProcessorChainElement;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.ProcessorException;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.TranscodeRequest;

public class DigitvFileMoverTranscoderProcessor extends ProcessorChainElement {

    private static Logger log = Logger.getLogger(DigitvFileMoverTranscoderProcessor.class);

	@Override
	protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
		File workFile = OutputFileUtil.getDigitvWorkOutputFile(request, config);
		File doneFile = OutputFileUtil.getDigitvDoneFile(request, config);
		log.debug("Moving file from workdir: " + workFile.getAbsolutePath() + " to done dir: " + doneFile.getAbsolutePath());
		boolean success = workFile.renameTo(doneFile);
		if (!success) {
			log.error("Unable to move file from workdir: " + workFile.getAbsolutePath() + " to done dir: " + doneFile.getAbsolutePath());
			throw new ProcessorException("Unable to move digitv file.");
		}
	}

}
