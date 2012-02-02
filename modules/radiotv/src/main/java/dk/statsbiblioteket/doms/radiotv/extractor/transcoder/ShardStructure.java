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
@XmlRootElement(name = "shard_structure")
public class ShardStructure {


    private MissingStart missingStart;


    private MissingEnd missingEnd;


    private List<Hole> holes;
    private List<Overlap> overlaps;

    @XmlTransient
    private String shard;

    @XmlTransient
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

    @XmlElement(required = false)
    public MissingStart getMissingStart() {
        return missingStart;
    }

    public void setMissingStart(MissingStart missingStart) {
        this.missingStart = missingStart;
    }

    @XmlElementWrapper(required = true)
    @XmlElement(name = "hole")
    public List<Hole> getHoles() {
        return holes;
    }

    @XmlElementWrapper(required = true)
    @XmlElement(name = "overlap")
    public List<Overlap> getOverlaps() {
        return overlaps;
    }

    @XmlElement(required = false)
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
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MissingStart that = (MissingStart) o;

            if (missingSeconds != that.missingSeconds) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return missingSeconds;
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
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MissingEnd that = (MissingEnd) o;

            if (missingSeconds != that.missingSeconds) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return missingSeconds;
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
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Hole hole = (Hole) o;

            if (holeLength != hole.holeLength) return false;
            if (!filePath1.equals(hole.filePath1)) return false;
            if (!filePath2.equals(hole.filePath2)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = filePath1.hashCode();
            result = 31 * result + filePath2.hashCode();
            result = 31 * result + (int) (holeLength ^ (holeLength >>> 32));
            return result;
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
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Overlap overlap = (Overlap) o;

            if (overlapLength != overlap.overlapLength) return false;
            if (overlapType != overlap.overlapType) return false;
            if (!filePath1.equals(overlap.filePath1)) return false;
            if (!filePath2.equals(overlap.filePath2)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = filePath1.hashCode();
            result = 31 * result + filePath2.hashCode();
            result = 31 * result + overlapType;
            result = 31 * result + (int) (overlapLength ^ (overlapLength >>> 32));
            return result;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShardStructure that = (ShardStructure) o;

        if (!holes.equals(that.holes)) return false;
        if (missingEnd != null ? !missingEnd.equals(that.missingEnd) : that.missingEnd != null) return false;
        if (missingStart != null ? !missingStart.equals(that.missingStart) : that.missingStart != null) return false;
        if (!overlaps.equals(that.overlaps)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = missingStart != null ? missingStart.hashCode() : 0;
        result = 31 * result + (missingEnd != null ? missingEnd.hashCode() : 0);
        result = 31 * result + holes.hashCode();
        result = 31 * result + overlaps.hashCode();
        return result;
    }
}
