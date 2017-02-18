package net.homelinux.mickey.dia;

public enum Rule {
    KAIRAKUEN("常磐線", "偕楽園");

    private String lineName;
    private String stationName;
    private Integer index;
    private Direction direction;

    public String getLineName() {
        return lineName;
    }
    public String getStationName() {
        return stationName;
    }
    public Integer getIndex() {
        return index;
    }
    public void setIndex(Integer index) {
        this.index = index;
    }
    public Direction getDirection() {
        return direction;
    }
    public void setDirection(Direction direction) {
        this.direction = direction;
    }
    private Rule(String lineName, String stationName) {
        this.lineName = lineName;
        this.stationName = stationName;
    }

    @Override
    public String toString() {
        return lineName + ":" + stationName + ":" + index + ":" + direction;
    }
}
