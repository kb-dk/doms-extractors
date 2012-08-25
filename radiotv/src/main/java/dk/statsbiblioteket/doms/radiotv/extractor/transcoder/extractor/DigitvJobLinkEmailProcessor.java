package dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletConfig;

import org.apache.log4j.Logger;

import dk.statsbiblioteket.doms.radiotv.extractor.Constants;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.OutputFileUtil;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.ProcessorChainElement;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.ProcessorException;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.TranscodeRequest;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.Util;

public class DigitvJobLinkEmailProcessor extends ProcessorChainElement {

	private static Logger log = Logger.getLogger(DigitvJobLinkEmailProcessor.class);
	private String url;
	private boolean sendEmail;

	public DigitvJobLinkEmailProcessor(String url, boolean sendEmail) {
		this.url = url;
		this.sendEmail = sendEmail;
	}

	@Override
	protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
		if (!sendEmail) {
			log.info("Not sending email for request: " + url);
			return;
		}
		String emailAddress = Util.getInitParameter(config, Constants.DIGITV_USER_EMAIL);
		String to = emailAddress;
		String from = "digitv@statsbiblioteket.dk";
		String host = "localhost";
		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", host);
		Session session = Session.getDefaultInstance(properties);
		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.addRecipient(Message.RecipientType.TO,
					new InternetAddress(to));
			message.setSubject("BES: " + OutputFileUtil.getDigitvDoneFile(request, config).getName());
			message.setContent(
					"<h1>Use the following link to get current status</h1>" +
					"<a href=\"" + url + "\">" + url + "</a>",
					"text/html" );
			Transport.send(message);
		} catch (MessagingException mex) {
			log.error("Unable to send e-mail", mex);
		}
	}

}
