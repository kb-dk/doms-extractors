package dk.statsbiblioteket.doms.radiotv.extractor.transcoder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the structure of a shard as determined by the shard analyser
 */
@XmlRootElement
public class ShardStructure {


    private MissingStart missingStart;


    private MissingEnd missingEnd;


    private List<Hole> holes;
    private List<Overlap> overlaps;
    private String shard;

    public String getShard() {
        return shard;
    }

    public void setShard(String shard) {
        this.shard = shard;
    }

    public ShardStructure() {
        holes = new ArrayList<Hole>();
        overlaps = new ArrayList<Overlap>();
    }

    public boolean isNonTrivial() {
        return missingEnd != null || missingStart != null || !holes.isEmpty() || !overlaps.isEmpty();
    }

    @XmlElement
    public MissingStart getMissingStart() {
        return missingStart;
    }

    public void setMissingStart(MissingStart missingStart) {
        this.missingStart = missingStart;
    }

      @XmlElementWrapper
    @XmlElement(name = "hole")
    public List<Hole> getHoles() {
        return holes;
    }

    @XmlElementWrapper
    @XmlElement(name = "overlap")
    public List<Overlap> getOverlaps() {
        return overlaps;
    }

    @XmlElement
    public MissingEnd getMissingEnd() {
        return missingEnd;
    }

    public void setMissingEnd(MissingEnd missingEnd) {
        this.missingEnd = missingEnd;
    }


    public void addHole(Hole hole) {
        holes.add(hole);
    }

    public void addOverlap(Overlap overlap) {
        overlaps.add(overlap);
    }


    public static class MissingStart {
       private int missingSeconds;

        public int getMissingSeconds() {
            return missingSeconds;
        }

        public void setMissingSeconds(int missingSeconds) {
            this.missingSeconds = missingSeconds;
        }

        @Override
        public String toString() {
            return "MissingStart{" +
                    "missingSeconds=" + missingSeconds +
                    '}';
        }
    }

    public static class MissingEnd {
          private int missingSeconds;

        public int getMissingSeconds() {
            return missingSeconds;
        }

        public void setMissingSeconds(int missingSeconds) {
            this.missingSeconds = missingSeconds;
        }

        @Override
        public String toString() {
            return "MissingEnd{" +
                    "missingSeconds=" + missingSeconds +
                    '}';
        }
    }

    public static class Hole {
        private String filePath1;
        private String filePath2;
        private long holeLength;

        public String getFilePath1() {
            return filePath1;
        }

        public void setFilePath1(String filePath1) {
            this.filePath1 = filePath1;
        }

        public long getHoleLength() {
            return holeLength;
        }

        public void setHoleLength(long holeLength) {
            this.holeLength = holeLength;
        }

        public String getFilePath2() {
            return filePath2;
        }

        public void setFilePath2(String filePath2) {
            this.filePath2 = filePath2;
        }

        @Override
        public String toString() {
            return "Hole{" +
                    "filePath1='" + filePath1 + '\'' +
                    ", filePath2='" + filePath2 + '\'' +
                    ", holeLength=" + holeLength +
                    '}';
        }
    }

    public static class Overlap {
        private String filePath1;
        private String filePath2;
        private int overlapType;
        private long overlapLength;

        public long getOverlapLength() {
            return overlapLength;
        }

        public void setOverlapLength(long overlapLength) {
            this.overlapLength = overlapLength;
        }

        public String getFilePath1() {
            return filePath1;
        }

        public void setFilePath1(String filePath1) {
            this.filePath1 = filePath1;
        }

        /**
         * There are four overlap types:
         *  file1 |-----------------------------------------|
         *  file2                                |------------------------------------|
         *  type0                 [------------------------------------]
         *  type1                      [-------------]
         *  type2                                  [-----]
         *  type3                                        [----------------]
         * @return
         */
        public int getOverlapType() {
            return overlapType;
        }

        public void setOverlapType(int overlapType) {
            this.overlapType = overlapType;
        }


        public String getFilePath2() {
            return filePath2;
        }

        public void setFilePath2(String filePath2) {
            this.filePath2 = filePath2;
        }

        @Override
        public String toString() {
            return "Overlap{" +
                    "filePath1='" + filePath1 + '\'' +
                    ", filePath2='" + filePath2 + '\'' +
                    ", overlapType=" + overlapType +
                    ", overlapLength=" + overlapLength +
                    '}';
        }
    }


    @Override
    public String toString() {
        return "ShardStructure{" +
                "missingStart=" + missingStart +
                ", missingEnd=" + missingEnd +
                ", holes=" + holes +
                ", overlaps=" + overlaps +
                ",\n shard='" + shard + '\'' + "\n" +
                '}';
    }

    private static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    /**
     * get an XML representation of this object
     * @return
     */
    public Document getXml() throws ProcessorException, ParserConfigurationException {
        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document doc = builder.newDocument();
        Node n1 = doc.createElement("shard_structure");
        doc.appendChild(n1);
        if (missingStart != null) {
            Node missingStartNode = doc.createElement("missing_start");
            missingStartNode.setTextContent(missingStart.getMissingSeconds()+"");
            n1.appendChild(missingStartNode);
        }
        return doc;
    }

}
