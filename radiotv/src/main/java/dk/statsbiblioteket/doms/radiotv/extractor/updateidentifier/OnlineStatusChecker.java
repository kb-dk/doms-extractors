package dk.statsbiblioteket.doms.radiotv.extractor.updateidentifier;

/**
 * Created by IntelliJ IDEA.
 * User: csr
 * Date: 5/3/12
 * Time: 1:17 PM
 * To change this template use File | Settings | File Templates.
 */
public interface OnlineStatusChecker {

    public boolean isOnline(String filename);

}
