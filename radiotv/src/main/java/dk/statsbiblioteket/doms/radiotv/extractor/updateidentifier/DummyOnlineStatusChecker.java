package dk.statsbiblioteket.doms.radiotv.extractor.updateidentifier;

/**
 * Created by IntelliJ IDEA.
 * User: csr
 * Date: 5/3/12
 * Time: 1:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class DummyOnlineStatusChecker implements OnlineStatusChecker {

    /**
     * Always returns false, which is the safe option.
     * @param filename
     * @return
     */
    @Override
    public boolean isOnline(String filename) {
        return false;
    }
}
