package net.homelinux.mickey.dia;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

enum Direction { DOWN, UP };

public class Ekikara2OuDia {
    private Log log = LogFactory.getLog(Ekikara2OuDia.class);
    private List<Station> tmpStationList, allStations;
    private List<Train> downTrains = new ArrayList<Train>(),
        upTrains = new ArrayList<Train>(), tmpTrainList;
    private String title, directionString;
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

    public Source fetchUrlAndParse(String url) {
        Source source = null;
        try {
            source = new Source(new URL(url));
        } catch (MalformedURLException e) {
            String message = "Malformed URL: " + url;
            log.error(message, e);
            throw new RuntimeException(message, e);
        } catch (IOException e) {
            String message = "IOException: " + url;
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
        tmpTrainList = new ArrayList<Train>();

        List<StartTag> tables = source.getAllStartTags("table");
        parseTitle((StartTag) tables.get(TABLE_NUMBER_OF_TITLE).getElement()
                   .getAllStartTags("td").get(TD_NUMBER_OF_TITLE));
        parseDirection((StartTag) tables.get(TABLE_NUMBER_OF_DIRECTION)
                       .getElement().getAllStartTags("div")
                       .get(DIV_NUMBER_OF_DIRECTION));
        List<StartTag> rows = tables.get(TABLE_NUMBER_OF_TRAIN_DATA)
            .getElement().getAllStartTags("tr");
        parseDay(rows.get(TR_NUMBER_OF_DAY).getElement()
                 .getAllStartTags("td"));
        parseTrainIdNumbers(rows.get(TR_NUMBER_OF_TRAIN_ID).getElement()
                            .getAllStartTags("td"));
        parseTrainNames(rows.get(TR_NUMBER_OF_TRAIN_NAME).getElement()
                        .getAllStartTags("td"));
        parseTrainNotes(rows.get(TR_NUMBER_OF_TRAIN_NOTE).getElement()
                        .getAllStartTags("td"));
        if (direction == Direction.UP) {
            revertProcessTables
                (rows.size() - NUMBER_OF_ROWS_BEFORE_STATIONS - 1);
        }
        for (int i = 0; i < processTables.length; i++) {
            tmpStationList = new ArrayList<Station>();
            int processTableNumber = Integer.parseInt(processTables[i])
                + NUMBER_OF_ROWS_BEFORE_STATIONS;
            if (processTableNumber >= rows.size()) {
                break;
            }
            List<StartTag> stationTimes =
                rows.get(processTableNumber).getElement()
                .getAllStartTags("td");
            parseStations(stationTimes.remove(0).getElement()
                          .getTextExtractor().toString().split(BLANK));
            stationTimes.remove(0);
            parseTrainTimes(stationTimes);
            try {
                allStations.addAll(tmpStationList);
            } catch (UnsupportedOperationException e) { }
        }
        allStations = Collections.unmodifiableList(allStations);
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

    private void parseTrainIdNumbers(List<StartTag> trainIdNumberTags) {
        if (log.isTraceEnabled()) {
            log.trace("trainIdNumberTags: " + trainIdNumberTags);
        }
        for (int i = 1; i < trainIdNumberTags.size(); i++) {
            String trainIdNumber =
                trainIdNumberTags.get(i).getElement()
                .getTextExtractor().toString();
            tmpTrainList.add(new Train(trainIdNumber));
        }
        if (log.isTraceEnabled()) {
            log.trace("tmpTrainList: " + tmpTrainList);
        }
    }

    private void parseTrainNames(List<StartTag> trainNameTags) {
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

    private void parseTrainNotes(List<StartTag> trainNoteTags) {
        trainNoteTags.remove(0);
        for (int i = 0; i < trainNoteTags.size(); i++) {
            String trainNote = trainNoteTags.get(i).getElement()
                .getTextExtractor().toString();
            if (CAUTION_MARK.equals(trainNote)) {
                tmpTrainList.get(i).setNote(CAUTION);
            }
        }
    }

    private void parseStations(String[] stations) {
        for (int i = 0; i < stations.length; i++) {
            if (!SAME_MARK.equals(stations[i])) {
                tmpStationList.add(new Station(stations[i]));
            } else {
                Station station =
                    tmpStationList.get(tmpStationList.size() - 1);
                station.setType(Station.Type.MAIN);
            }
        }
    }

    private void parseTrainTimes(List<StartTag> timesTags) {
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
        return NULL_STRING.equals(time) ? null : time;
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

    public static void main(final String... args) throws IOException {
        if (args.length == 0) {
            System.out.println(USAGE);
            return;
        }
        System.setProperty("file.encoding", "sjis");
        final Ekikara2OuDia ekikara2OuDia = new Ekikara2OuDia();
        final List<Source> sources = new ArrayList<>();
        ExecutorService pool = Executors.newCachedThreadPool();
        sources.add(ekikara2OuDia.fetchUrlAndParse(args[0]));
        for (int i = 1; i < args.length; i++) {
            final String url = args[i];
            pool.execute(new Runnable() {
                    public void run() {
                        sources.add(ekikara2OuDia.fetchUrlAndParse(url));
                    }
                });
        }
        pool.shutdown();
        try {
            pool.awaitTermination(60, SECONDS);
        } catch (InterruptedException e) {
            System.err.println(e.toString());
        }
        for (Source source : sources) {
            ekikara2OuDia.process(source);
        }
        Formatter formatter =
            new OuDiaFormatter(ekikara2OuDia.title + " " + updateDate,
                               ekikara2OuDia.allStations,
                               ekikara2OuDia.downTrains,
                               ekikara2OuDia.upTrains,
                               Arrays.toString(args) + " " + updateDate);
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
