package dk.statsbiblioteket.doms.radiotv.extractor;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/bes")
public class BroadcastExtractionService {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String sayHello() {
        return "Hello Jersey";
    }



    @GET @Path("/statusofprogramclip")
	@Produces("text/plain")
	public String getProgramClipStatus(
			@DefaultValue("null") @QueryParam("channel") String channel
			) {
		System.out.println("Request status for program clip. Channel: " + channel);
		return "Program clip is not available for channel: " + channel;
	}
	
	@GET @Path("/createprogramclip")
	@Produces("text/plain")
	public String createProgramClip() {
		System.out.println("Request for generating program clip.");
		// Clip transport stream
		// Combine transport streams
		// Demux transport streams
		// Transcode transport streams
		return "Request for generating program clip.";
	}

	@GET @Path("/getprogramclip")
	@Produces("text/plain")
	public String getProgramClip() {
		System.out.println("Request for program clip.");
		return "Dette skal v�re en str�m af video...";
	}
}
