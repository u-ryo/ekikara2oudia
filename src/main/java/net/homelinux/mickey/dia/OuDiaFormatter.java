package net.homelinux.mickey.dia;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OuDiaFormatter implements Formatter {
    private List<Station> stations;
    private Set<Train> downTrains, upTrains;
    private String title, comments;
    private Log log = LogFactory.getLog(OuDiaFormatter.class);

    private static final String
        LINE_SEPARATOR = "\r\n", PASSAGE = "passage", VOID_MARK = "||",
        REPLACE_RE = "^0|\\|\\||\\(|\\)", NULL_STRING = "",
        FILE_TYPE_APP_COMMENT = "FileTypeAppComment=Ekikara2OuDia",
        DIAGRAM_Y_AXIS_DEFAULT =
        "DiagramDgrYZahyouKyoriDefault=60" + LINE_SEPARATOR,

        START_TIME_SETTINGS
        = "." + LINE_SEPARATOR + "." + LINE_SEPARATOR
        + "KitenJikoku=" + System.getProperty("KitenJikoku", "300")
        + LINE_SEPARATOR,

        DISPLAY_PROPERTIES
        = "DispProp." + LINE_SEPARATOR
        + "JikokuhyouFont=PointTextHeight=9;Facename=ＭＳ ゴシック"
        + LINE_SEPARATOR
        + "DiaEkimeiFont=PointTextHeight=9;Facename=ＭＳ ゴシック"
        + LINE_SEPARATOR
        + "DiaJikokuFont=PointTextHeight=9;Facename=ＭＳ ゴシック"
        + LINE_SEPARATOR
        + "DiaRessyaFont=PointTextHeight=9;Facename=ＭＳ ゴシック"
        + LINE_SEPARATOR
        + "CommentFont=PointTextHeight=9;Facename=ＭＳ ゴシック"
        + LINE_SEPARATOR
        + "DiaMojiColor=00000000" + LINE_SEPARATOR
        + "DiaHaikeiColor=00FFFFFF" + LINE_SEPARATOR
        + "DiaRessyaColor=00000000" + LINE_SEPARATOR
        + "DiaJikuColor=00C0C0C0" + LINE_SEPARATOR
        + "EkimeiLength=6" + LINE_SEPARATOR
        + "JikokuhyouRessyaWidth=5" + LINE_SEPARATOR
        + ".";


    public OuDiaFormatter() {}

    public OuDiaFormatter(String title, List<Station> stations,
                          List<Train> downTrains, List<Train> upTrains,
                          String comments) {
        setTitle(title);
        setStations(stations);
        setDownTrains(downTrains);
        setUpTrains(upTrains);
        setComments(comments);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setStations(List<Station> stations) {
        this.stations = stations;
    }

    public void setDownTrains(List<Train> downTrains) {
        this.downTrains = new TreeSet<Train>(downTrains);
        log.trace("downTrains:" + downTrains);
    }

    public void setUpTrains(List<Train> upTrains) {
        this.upTrains = new TreeSet<Train>(upTrains);
        log.trace("upTrains:" + upTrains);
    }

    public void setComments(String comments) {
        this.comments = "Comment=Arguments: " + comments + LINE_SEPARATOR + "."
            + LINE_SEPARATOR;
    }

    public String format() {
        System.setProperty("file.encoding", "sjis");
        StringBuilder output = new StringBuilder();
        output.append("FileType=OuDia.1.02" + LINE_SEPARATOR);
        output.append("Rosen." + LINE_SEPARATOR);
        output.append("Rosenmei=" + title + LINE_SEPARATOR);
        output = buildStations(stations, output);

        // output.append(TRAIN_TYPES);
        for (Train.Type type : Train.Type.values()) {
            output.append(getTrainTypeString(type));
        }
        output.append("Dia." + LINE_SEPARATOR);
        output.append("DiaName=" + title + LINE_SEPARATOR);

        output = buildTrains(downTrains, stations, output, Direction.DOWN);
        output.append("." + LINE_SEPARATOR);
        output = buildTrains(upTrains, stations, output, Direction.UP);
        output.append(START_TIME_SETTINGS);
        output.append(DIAGRAM_Y_AXIS_DEFAULT);
        output.append(comments);
        output.append(DISPLAY_PROPERTIES);
        output.append("." + LINE_SEPARATOR);
        output.append(FILE_TYPE_APP_COMMENT + LINE_SEPARATOR);
        return new String(output);
    }

    private StringBuilder buildStations(List<Station> stations,
                                        StringBuilder output) {
        for (int i = 0; i < stations.size(); i++) {
            output.append("Eki." + LINE_SEPARATOR);
            output.append("Ekimei=" + stations.get(i).getName()
                          + LINE_SEPARATOR);
            output.append("Ekijikokukeisiki=");
            switch (stations.get(i).getType()) {
            case GENERAL:
                if (i == 0) {
                    output.append("Jikokukeisiki_NoboriChaku" + LINE_SEPARATOR);
                } else if (i == stations.size() - 1) {
                    output.append("Jikokukeisiki_KudariChaku" + LINE_SEPARATOR);
                } else {
                    output.append("Jikokukeisiki_Hatsu" + LINE_SEPARATOR);
                }
                output.append("Ekikibo=Ekikibo_Ippan" + LINE_SEPARATOR);
                break;
            case MAIN:
                output.append("Jikokukeisiki_Hatsuchaku" + LINE_SEPARATOR);
                output.append("Ekikibo=Ekikibo_Syuyou" + LINE_SEPARATOR);
                break;
            }
            output.append("." + LINE_SEPARATOR);
        }
        return output;
    }

    private StringBuilder buildTrains(Set<Train> trains, List<Station> stations,
                                      StringBuilder output,
                                      Direction direction)  {
        if (direction == Direction.DOWN) {
            output.append("Kudari." + LINE_SEPARATOR);
        } else if (direction == Direction.UP) {
            output.append("Nobori." + LINE_SEPARATOR);
        }
        for (Train train : trains) {
            output.append("Ressya." + LINE_SEPARATOR);
            if (direction == Direction.DOWN) {
                output.append("Houkou=Kudari" + LINE_SEPARATOR);
            } else if (direction == Direction.UP) {
                output.append("Houkou=Nobori" + LINE_SEPARATOR);
            }
            output.append("Syubetsu=" + train.getType().ordinal()
                          + LINE_SEPARATOR);
            output.append("Ressyabangou=" + train.getIdNumber()
                          + LINE_SEPARATOR);
            if (!NULL_STRING.equals(train.getName())) {
                output.append("Ressyamei=" + train.getName() + LINE_SEPARATOR);
                if (train.getNumber() != null) {
                    output.append("Gousuu=" + train.getNumber()
                                  + LINE_SEPARATOR);
                }
            }

            output.append("EkiJikoku=");
            List<String[]> times = train.getTime();
            for (int j = 0; j < times.size(); j++) {
                Station station = null;
                if (direction == Direction.DOWN) {
                    station = stations.get(j);
                } else if (direction == Direction.UP) {
                    station = stations.get(stations.size() - 1 - j);
                }
                String[] time = times.get(j);

                if (log.isTraceEnabled()) {
                    log.debug(train.getIdNumber() + ":" + station.getName()
                              + ":" + time[0] + ":" + time[1]);
                }
                if (PASSAGE.equals(time[0]) || PASSAGE.equals(time[1])) {
                    // passage?/passage?
                    output.append("2");
                } else if ((VOID_MARK.equals(time[0])
                            && VOID_MARK.equals(time[1]))
                           // void/void
                           || (VOID_MARK.equals(time[0]) && time[1] == null)
                           || (VOID_MARK.equals(time[1]) && time[0] == null)) {
                    output.append("3");
                } else if (time[0] == null && time[1] != null) {
                    // null/0812
                    time[1] = time[1].replaceAll(REPLACE_RE, NULL_STRING);
                    output.append("1;" + time[1]);
                    if (j == stations.size() - 1) {
                        output.append("/");
                    }
                } else if (time[0] != null && time[1] == null) {
                    // 0812/null
                    time[0] = time[0].replaceAll(REPLACE_RE, NULL_STRING);
                    output.append("1;" + time[0] + "/");
                } else if (time[0] != null && time[1] != null) {
                    // 0812/0813
                    time[0] = time[0].replaceAll(REPLACE_RE, NULL_STRING);
                    time[1] = time[1].replaceAll(REPLACE_RE, NULL_STRING);
                    output.append("1;" + time[0] + "/" + time[1]);
                }

                if (j < times.size() - 1) {
                    output.append(",");
                } else if (j == times.size() - 1) {
                    output.append(LINE_SEPARATOR);
                }
            }

            if (train.getNote() != null) {
                output.append("Bikou=" + train.getNote() + LINE_SEPARATOR);
            }
            output.append("." + LINE_SEPARATOR);
        }
        return output;
    }

    private String getTrainTypeString(Train.Type type) {
        String typeName = "";
        String simpleName = "";
        String color = "";
        int isBold = 0;
        boolean stopMark = true;

        switch (type) {
        case LOCAL:
            typeName = "普通";
            color = "00000000";
            stopMark = false;
            break;
        case RAPID:
            typeName = "快速";
            simpleName = "快速";
            color = "00FF0000";
            break;
        case SECTIONAL_RAPID:
            typeName = "区間快速";
            simpleName = "区快";
            color = "00FF0000";
            break;
        case NEW_RAPID:
            typeName = "新快速";
            simpleName = "新快";
            color = "00FF0000";
            break;
        case SPECIAL_RAPID:
            typeName = "特別快速";
            simpleName = "特快";
            color = "00FF0000";
            break;
        case HOME_LINER:
            typeName = "ホームライナー";
            simpleName = "ＨＬ";
            color = "00FF0000";
            break;
        case SEMI_EXPRESS:
            typeName = "準急";
            simpleName = "準急";
            color = "11EE1100";
            isBold = 1;
            break;
        case EXPRESS:
            typeName = "急行";
            simpleName = "急行";
            color = "0000FF00";
            isBold = 1;
            break;
        case LIMITED_EXPRESS:
            typeName = "特急";
            simpleName = "特急";
            color = "000000FF";
            isBold = 1;
            break;
        case NOZOMI_SUPER_EXPRESS:
            typeName = "のぞみ型新幹線";
            simpleName = "幹";
            color = "000000FF";
            isBold = 1;
            break;
        case HIKARI_SUPER_EXPRESS:
            typeName = "ひかり型新幹線";
            simpleName = "幹";
            color = "0000FF00";
            isBold = 1;
            break;
        case KODAMA_SUPER_EXPRESS:
            typeName = "こだま型新幹線";
            simpleName = "幹";
            color = "00400000";
            isBold = 1;
            break;
        case BUS:
            typeName = "バス";
            simpleName = "バス";
            color = "00000000";
            stopMark = false;
            break;
        case RAPID_EXPRESS:
            typeName = "快速急行";
            simpleName = "快急";
            color = "0000FF00";
            isBold = 1;
            break;
        case RAPID_LIMITED_EXPRESS:
            typeName = "快速特急";
            simpleName = "快特";
            color = "008080FF";
            isBold = 1;
            break;
        case AIRPORT_EXPRESS:
            typeName = "エアポート急行";
            simpleName = "エ急";
            color = "0000FF00";
            isBold = 1;
            break;
        }

        return "Ressyasyubetsu." + LINE_SEPARATOR
            + "Syubetsumei=" + typeName + LINE_SEPARATOR
            + (simpleName.isEmpty() ? ""
               : "Ryakusyou=" + simpleName + LINE_SEPARATOR)
            + "JikokuhyouMojiColor=" + color + LINE_SEPARATOR
            + "JikokuhyouFontIndex=0" + LINE_SEPARATOR
            + "DiagramSenColor=" + color + LINE_SEPARATOR
            + "DiagramSenStyle=SenStyle_Jissen" + LINE_SEPARATOR
            + "DiagramSenIsBold=" + isBold + LINE_SEPARATOR
            + (stopMark ? "StopMarkDrawType=EStopMarkDrawType_DrawOnStop"
               : "StopMarkDrawType=EStopMarkDrawType_Nothing") + LINE_SEPARATOR
            + "." + LINE_SEPARATOR;
    }
}
