package dk.statsbiblioteket.doms.radiotv.extractor.transcoder;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the structure of a shard as determined by the shard analyser
 */
public class ShardStructure {

    private MissingStart missingStart;
    private MissingEnd missingEnd;
    private List<Hole> holes;
    private List<Overlap> overlaps;

    public ShardStructure() {
        holes = new ArrayList<Hole>();
        overlaps = new ArrayList<Overlap>();
    }

    public boolean isNonTrivial() {
        return missingEnd != null || missingEnd != null || !holes.isEmpty() || !overlaps.isEmpty();
    }

    public MissingStart getMissingStart() {
        return missingStart;
    }

    public void setMissingStart(MissingStart missingStart) {
        this.missingStart = missingStart;
    }

    public List<Hole> getHoles() {
        return holes;
    }

    public List<Overlap> getOverlaps() {
        return overlaps;
    }

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

    }


    public static class MissingEnd {

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
                '}';
    }
}
