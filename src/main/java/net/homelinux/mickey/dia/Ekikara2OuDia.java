package net.homelinux.mickey.dia;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Ekikara2OuDia {
    private Log log = LogFactory.getLog(Ekikara2OuDia.class);
    private List<Station> allStations;
    private List<Train> downTrains = new ArrayList<Train>(),
        upTrains = new ArrayList<Train>();
    private String title, directionString;
    private boolean hasAllStationsListed;
    private boolean[] hasRuleChecked = new boolean[2];
    private static String updateDate;
    private static final int NUMBER_OF_ROWS_BEFORE_STATIONS = 5,
        TABLE_NUMBER_OF_TITLE = 3, TD_NUMBER_OF_TITLE = 2,
        TABLE_NUMBER_OF_DIRECTION = 8, DIV_NUMBER_OF_DIRECTION = 0,
        TABLE_NUMBER_OF_TRAIN_DATA = 14,
        TR_NUMBER_OF_DAY = 0, TR_NUMBER_OF_TRAIN_ID = 1,
        TR_NUMBER_OF_TRAIN_NAME = 2, TR_NUMBER_OF_TRAIN_NOTE = 3,
        TABLE_NUMBER_OF_UPDATE_DATE = 15, TD_NUMBER_OF_UPDATE_DATE = 2;
    private static final String CAUTION_MARK = "◆", CAUTION = "運転日注意",
        NULL_STRING = "", SAME_MARK = "〃", BLANK = " ", SPLIT_RE = "<[^>]+>",
        REMOVE_RE =
        "<(/|)(font|td|img|span)[^>]*>|--|==|:|直通|_|&nbsp;| +|\r\n",
        PASSAGE_MARK = "レ", PASSAGE = "passage",
        PROCESS_TABLES_SPLIT_STRING = ", *",
        USAGE = "java [-Dfile.encoding=sjis] [-DprocessTables=1] "
        + "[-DKitenJikoku=300] -jar ekikara2oudia.jar URL...";
    private String[] processTables = System.getProperty
        ("processTables", "1").split(PROCESS_TABLES_SPLIT_STRING);
    private static final Pattern
        DAY_PATTERN = Pattern.compile(".*(平日|土曜日|休日).*"),
        UPDATE_DATE_PATTERN =
        Pattern.compile(".*(更新日:\\p{Digit}{2,4}/\\p{Digit}{1,2}/\\p{Digit}{1,2}).*");

    private Direction direction;
    public Rule rule;

    public Source fetchUrlAndParse(String url)
        throws SocketTimeoutException, IOException {
        Source source = null;
        try {
            source = new Source(new URL(url));
        } catch (MalformedURLException e) {
            String message = "Malformed URL: " + url;
            log.error(message, e);
            throw new RuntimeException(message, e);
        }
        source.fullSequentialParse();
        return source;
    }

    public void process(Source source) {
        if (allStations == null) {
            allStations = new ArrayList();
        }
        List<StartTag> tables = source.getAllStartTags("table");
        parseTitle((StartTag) tables.get(TABLE_NUMBER_OF_TITLE).getElement()
                   .getAllStartTags("td").get(TD_NUMBER_OF_TITLE));
        setRuleByTitle();
        parseDirection((StartTag) tables.get(TABLE_NUMBER_OF_DIRECTION)
                       .getElement().getAllStartTags("div")
                       .get(DIV_NUMBER_OF_DIRECTION));
        List<StartTag> rows = tables.get(TABLE_NUMBER_OF_TRAIN_DATA)
            .getElement().getAllStartTags("tr");
        parseDay(rows.get(TR_NUMBER_OF_DAY).getElement()
                 .getAllStartTags("td"));
        List<Train> tmpTrainList =
            parseTrainIdNumbers(rows.get(TR_NUMBER_OF_TRAIN_ID).getElement()
                                .getAllStartTags("td"));
        parseTrainNames(rows.get(TR_NUMBER_OF_TRAIN_NAME).getElement()
                        .getAllStartTags("td"), tmpTrainList);
        parseTrainNotes(rows.get(TR_NUMBER_OF_TRAIN_NOTE).getElement()
                        .getAllStartTags("td"), tmpTrainList);
        if (direction == Direction.UP) {
            revertProcessTables
                (rows.size() - NUMBER_OF_ROWS_BEFORE_STATIONS - 1);
        }
        for (int i = 0; i < processTables.length; i++) {
            int processTableNumber = Integer.parseInt(processTables[i])
                + NUMBER_OF_ROWS_BEFORE_STATIONS;
            if (processTableNumber >= rows.size()) {
                break;
            }
            List<StartTag> stationTimes =
                rows.get(processTableNumber).getElement()
                .getAllStartTags("td");
            List<Station> tmpStationList =
                parseStations(stationTimes.remove(0).getElement()
                              .getTextExtractor().toString().split(BLANK));
            stationTimes.remove(0);
            parseTrainTimes(stationTimes, tmpTrainList, tmpStationList);
            if (!hasAllStationsListed) {
                allStations.addAll(tmpStationList);
            }
        }
        hasAllStationsListed = true;
        if (direction == direction.DOWN) {
            hasRuleChecked[0] = true;
        } else if (direction == direction.UP) {
            hasRuleChecked[1] = true;
        }
        if (log.isDebugEnabled()) {
            log.debug("allStations:" + allStations);
        }
        parseUpdateDate(tables.get(TABLE_NUMBER_OF_UPDATE_DATE).getElement()
                        .getAllStartTags("td"));

        if (log.isDebugEnabled()) {
            log.debug("tmpTrainList: " + tmpTrainList.toString());
        }
        switch (direction) {
        case DOWN:
            downTrains.addAll(tmpTrainList);
            break;
        case UP:
            upTrains.addAll(tmpTrainList);
            break;
        }
    }

    private void setRuleByTitle() {
        if (!hasAllStationsListed) {
            for (Rule r : Rule.values()) {
                if (title.contains(r.getLineName())) {
                    this.rule = r;
                }
            }
        }
    }

    private void parseTitle(StartTag td) {
        title = td.getElement().getTextExtractor().toString();
        if (log.isDebugEnabled()) {
            log.debug("title: \"" + title + "\"");
        }
    }

    private void parseDirection(StartTag div) {
        String directionString = div.getAttributes().get("class").getValue();
        if (this.directionString == null) {
            this.directionString = directionString;
        }
        if (directionString.equals(this.directionString)) {
            direction = Direction.DOWN;
        } else {
            direction = Direction.UP;
        }
        if (log.isDebugEnabled()) {
            log.debug("direction: " + direction);
        }
    }

    private void parseDay(List<StartTag> tds) {
        Matcher dayMatcher =
            DAY_PATTERN.matcher(tds.get(0).getElement().getTextExtractor()
                                .toString());
        log.debug("day tag: " + tds.get(0).getElement().getTextExtractor());
        log.debug("dayMatcher: " + dayMatcher);
        log.debug("dayMatcher.matches(): " + dayMatcher.matches());
        if (dayMatcher.matches()) {
            log.debug(dayMatcher.group(1));
            title += " " + dayMatcher.group(1);
        }
    }

    private List<Train> parseTrainIdNumbers(List<StartTag> trainIdNumberTags) {
        if (log.isTraceEnabled()) {
            log.trace("trainIdNumberTags: " + trainIdNumberTags);
        }
        List<Train> tmpTrainList = new ArrayList<Train>();
        for (int i = 1; i < trainIdNumberTags.size(); i++) {
            String trainIdNumber =
                trainIdNumberTags.get(i).getElement()
                .getTextExtractor().toString();
            tmpTrainList.add(new Train(trainIdNumber));
        }
        if (log.isTraceEnabled()) {
            log.trace("tmpTrainList: " + tmpTrainList);
        }
        return tmpTrainList;
    }

    private void parseTrainNames(List<StartTag> trainNameTags,
                                 List<Train> tmpTrainList) {
        if (log.isTraceEnabled()) {
            log.trace("trainNameTags: " + trainNameTags);
        }
        trainNameTags.remove(0);
        for (int i = 0; i < trainNameTags.size(); i++) {
            String trainName =
                trainNameTags.get(i).getElement()
                .getTextExtractor().toString().trim();
            if (log.isTraceEnabled()) {
                log.trace("trainName: '" + trainName + "'");
            }
            if (trainName.length() > 0) {
                TrainName.setName(tmpTrainList.get(i), trainName);
            }
        }
    }

    private void parseTrainNotes(List<StartTag> trainNoteTags,
                                 List<Train> tmpTrainList) {
        trainNoteTags.remove(0);
        for (int i = 0; i < trainNoteTags.size(); i++) {
            String trainNote = trainNoteTags.get(i).getElement()
                .getTextExtractor().toString();
            if (CAUTION_MARK.equals(trainNote)) {
                tmpTrainList.get(i).setNote(CAUTION);
            }
        }
    }

    private List<Station> parseStations(String[] stations) {
        if (log.isDebugEnabled()) {
            log.debug("stations[]:" + Arrays.toString(stations));
        }
        List<Station> tmpStationList = new ArrayList<Station>();
        for (int i = 0; i < stations.length; i++) {
            if (!SAME_MARK.equals(stations[i])) {
                tmpStationList.add(new Station(stations[i]));
                if (rule != null) {
                    checkRules(stations[i], tmpStationList);
                }
            } else {
                Station station =
                    tmpStationList.get(tmpStationList.size() - 1);
                station.setType(Station.Type.MAIN);
            }
        }
        return tmpStationList;
    }

    private void checkRules(String stationName, List<Station> tmpStationList) {
        if (direction == Direction.DOWN && !hasRuleChecked[0]
            && rule.getIndex() == null) {
            if (stationName.equals(rule.getStationName())) {
                rule.setIndex(tmpStationList.size());
                rule.setDirection(Direction.UP);
            }
            if (log.isDebugEnabled()) {
                log.debug("Rule:" + rule);
            }
        } else if (direction == Direction.UP && !hasRuleChecked[1]
            && rule.getIndex() == null) {
            if (stationName.equals(rule.getStationName())) {
                rule.setIndex(tmpStationList.size() - 1);
                rule.setDirection(Direction.DOWN);
            }
            if (log.isDebugEnabled()) {
                log.debug("Rule:" + rule);
            }
        }
    }

    private void parseTrainTimes(List<StartTag> timesTags,
                                 List<Train> tmpTrainList,
                                 List<Station> tmpStationList) {
        for (int i = 0; i < tmpTrainList.size(); i++) {
            String timesTagString =
                timesTags.get(i).getElement().toString();
            timesTagString =
                timesTagString.replaceAll(REMOVE_RE, NULL_STRING);
            timesTagString = timesTagString.replaceAll(PASSAGE_MARK, PASSAGE);
            String[] trainTimes = timesTagString.split(SPLIT_RE, -1);
            if (log.isTraceEnabled()) {
                log.trace("timesTagString: " + timesTagString);
                log.trace("trainTimes.length: " + trainTimes.length);
                log.trace(tmpTrainList.get(i).getIdNumber() + ":"
                          + Arrays.toString(trainTimes));
            }
            int stationCounter = 0, timeCounter = 0;
            List<String[]> stationTime = new ArrayList<String[]>();
            for (Station station : tmpStationList) {
                String[] times = new String[2];
                if (timeCounter >= trainTimes.length) {
                    break;
                }
                if (station.getType() == Station.Type.MAIN) {
                    times[0] = nullCheck(trainTimes[timeCounter]);
                    if (++timeCounter >= trainTimes.length) {
                        break;
                    }
                }
                times[1] = nullCheck(trainTimes[timeCounter++]);
                stationTime.add(times);
            }
            if (direction == Direction.DOWN) {
                tmpTrainList.get(i).addTime(stationTime);
            } else if (direction == Direction.UP) {
                tmpTrainList.get(i).getTime().addAll(0, stationTime);
            }
        }
    }

    private String nullCheck(String time) {
        return NULL_STRING.equals(time) ? null : time.trim();
    }

    private void parseUpdateDate(List<StartTag> tds) {
        for (StartTag td : tds) {
            String tdString = td.getElement().getTextExtractor().toString();
            Matcher updateDateMatcher = UPDATE_DATE_PATTERN.matcher(tdString);
            if (log.isDebugEnabled()) {
                log.debug("tdString: " + tdString);
                log.debug("updateDateMatcher.matches(): "
                          + updateDateMatcher.matches());
            }
            if (updateDateMatcher.matches()) {
                updateDate = updateDateMatcher.group(1);
                break;
            }
        }
    }

    private void revertProcessTables(int size) {
        processTables = System.getProperty
            ("processTables", "1").split(PROCESS_TABLES_SPLIT_STRING);
        String[] tmp = new String[processTables.length];
        for (int i = 0; i < processTables.length; i++) {
            tmp[i] = String.valueOf(size - Integer.parseInt(processTables[i]));
        }
        processTables = tmp;
    }

    public void adjust() {
        if (rule == null) {
            return;
        }
        int index = allStations.size() - rule.getIndex();
        List<Train> trains =
            rule.getDirection() == Direction.UP ? upTrains : downTrains;
        for (Train train : trains) {
            List<String[]> time = train.getTime();
            if (time.get(index - 1)[1] != null && time.get(index)[1] != null) {
                time.add(index, new String[] { null, PASSAGE });
            } else {
                time.add(index, new String[2]);
            }
        }
        if (rule.getDirection() == Direction.DOWN) {
            allStations.add(index, new Station(rule.getStationName()));
        }
        if (log.isDebugEnabled()) {
            log.debug("rule:" + rule);
        }
        if (log.isTraceEnabled()) {
            if (rule.getDirection() == Direction.UP) {
                log.trace("upTrains:" + upTrains);
            } else {
                log.trace("downTrains:" + downTrains);
                log.trace("allStations:" + allStations);
            }
        }
    }

    public static void main(final String... args)
        throws SocketTimeoutException, IOException {
        if (args.length == 0) {
            System.out.println(USAGE);
            return;
        }
        System.setProperty("file.encoding", "sjis");
        final Ekikara2OuDia ekikara2OuDia = new Ekikara2OuDia();
        final Map<String, Source> sourceMap =
            new TreeMap<>(new Comparator<String>() {
                    public int compare(String url0, String url1) {
                        if (args[0].contains("/down")
                            || (url0.contains("/down")
                                && url1.contains("/down"))
                            || (url0.contains("/up") && url1.contains("/up"))) {
                            return url0.compareTo(url1);
                        }
                        return url1.compareTo(url0);
                    }
            });
        ExecutorService pool = Executors.newCachedThreadPool();
        for (String arg : args) {
            final String url = arg;
            pool.execute(new Runnable() {
                    public void run() {
                        try {
                            sourceMap.put(url, ekikara2OuDia.fetchUrlAndParse(url));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        }
        pool.shutdown();
        try {
            pool.awaitTermination(60, SECONDS);
        } catch (InterruptedException e) {
            System.err.println(e.toString());
        }
        for (String url : sourceMap.keySet()) {
            ekikara2OuDia.process(sourceMap.get(url));
        }
        if (ekikara2OuDia.rule != null) {
            ekikara2OuDia.adjust();
        }
        Formatter formatter =
            new OuDiaFormatter(ekikara2OuDia.title + " " + updateDate,
                               ekikara2OuDia.allStations,
                               ekikara2OuDia.downTrains,
                               ekikara2OuDia.upTrains,
                               Arrays.toString(args).replaceAll(",", "")
                               + " " + updateDate);
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(System.out, "Shift_JIS");
            writer.write(formatter.format());
            writer.close();
        } catch (UnsupportedEncodingException e) {}
    }

    public String getTitle() {
        return title;
    }
    public List<Station> getAllStations() {
        return allStations;
    }
    public List<Train> getDownTrains() {
        return downTrains;
    }
    public List<Train> getUpTrains() {
        return upTrains;
    }
    public String getUpdateDate() {
        return updateDate;
    }
}
