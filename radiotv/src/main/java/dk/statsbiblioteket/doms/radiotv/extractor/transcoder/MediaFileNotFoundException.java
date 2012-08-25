package dk.statsbiblioteket.doms.radiotv.extractor.transcoder;

@SuppressWarnings("serial")
public class MediaFileNotFoundException extends Exception {

	public MediaFileNotFoundException() {
	}

	public MediaFileNotFoundException(String arg0) {
		super(arg0);
	}

	public MediaFileNotFoundException(Throwable arg0) {
		super(arg0);
	}

	public MediaFileNotFoundException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
