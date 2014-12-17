package net.homelinux.mickey.dia;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TrainName {
    private static final String CATEGORY_RE = "\\[.*\\]| ", EMPTY_STRING = "",
        LOCAL = "[普通]", RAPID = "[快速]", NEW_RAPID = "[新快速]",
        SECTIONAL_RAPID = "[区間快速]", SEMI_RAPID = "[準快速]",
        COMMUTER_EXPRESS = "[通勤快速]", SPECIAL_RAPID = "[特別快速]",
        HOME_LINER = "[ホームライナー]", EXPRESS = "[急行]",
        AIRPORT_EXPRESS = "[エアポート急行]", RAPID_EXPRESS = "[快速急行]",
        RAPID_LIMITED_EXPRESS = "[快速特急]", RAPID_LIMITED_EXPRESS2 = "[快特]",
        BUS = "[バス]", SLEEPING_EXPRESS = "[寝台急行]",
        LIMITED_EXPRESS = "[特急]", L_EXPRESS = "[Ｌ特急]",
        SLEEPING_LIMITED_EXPRESS = "[寝台特急]", SUPER_EXPRESS = "[新幹線]",
        NUMBER_RE = "(.*?)([1-9][0-9]*)号", ANY_RAPID = "快速]",
        ANY_SEMI_EXPRESS = "準急]", ANY_EXPRESS = "急行]",
        ANY_LIMITED_EXPRESS = "特急]", ANY_RAPID_EXPRESS = "快急]",
        ANY_RAPID_LIMITED_EXPRESS = "快特]";
    private static final Pattern NUMBER_PATTERN = Pattern.compile(NUMBER_RE);

    static void setName(Train train, String name) {
        name = name.replaceFirst("\\(料金不要\\)", "");
        if (name.startsWith(RAPID) || name.startsWith(SEMI_RAPID)
            || name.startsWith(COMMUTER_EXPRESS)) {
            train.setType(Train.Type.RAPID);
        } else if (name.startsWith(SPECIAL_RAPID)) {
            train.setType(Train.Type.SPECIAL_RAPID);
        } else if (name.startsWith(SECTIONAL_RAPID)) {
            train.setType(Train.Type.SECTIONAL_RAPID);
        } else if (name.startsWith(NEW_RAPID)) {
            train.setType(Train.Type.NEW_RAPID);
        } else if (name.startsWith(HOME_LINER)) {
            train.setType(Train.Type.HOME_LINER);
        } else if (name.startsWith(SLEEPING_LIMITED_EXPRESS)
                   || name.startsWith(LIMITED_EXPRESS)
                   || name.startsWith(L_EXPRESS)) {
            train.setType(Train.Type.LIMITED_EXPRESS);
        } else if (name.startsWith(AIRPORT_EXPRESS)) {
            train.setType(Train.Type.AIRPORT_EXPRESS);
        } else if (name.startsWith(EXPRESS)
                   || name.startsWith(SLEEPING_EXPRESS)) {
            train.setType(Train.Type.EXPRESS);
        } else if (name.startsWith(SUPER_EXPRESS)) {
            train.setType(Train.Type.SUPER_EXPRESS);
        } else if (name.startsWith(BUS)) {
            train.setType(Train.Type.BUS);
        } else if (name.startsWith(RAPID_EXPRESS)
                   || name.endsWith(ANY_RAPID_EXPRESS)) {
            train.setType(Train.Type.RAPID_EXPRESS);
        } else if (name.startsWith(RAPID_LIMITED_EXPRESS)
                   || name.startsWith(RAPID_LIMITED_EXPRESS2)
                   || name.endsWith(ANY_RAPID_LIMITED_EXPRESS)) {
            train.setType(Train.Type.RAPID_LIMITED_EXPRESS);
        } else if (name.contains(ANY_RAPID)) {
            train.setType(Train.Type.RAPID);
        } else if (name.contains(ANY_SEMI_EXPRESS)) {
            train.setType(Train.Type.SEMI_EXPRESS);
        } else if (name.contains(ANY_EXPRESS)) {
            train.setType(Train.Type.EXPRESS);
        } else if (name.contains(ANY_LIMITED_EXPRESS)) {
            train.setType(Train.Type.LIMITED_EXPRESS);
        } else {
            train.setType(Train.Type.LOCAL);
        }
        String trainName = name.replaceAll(CATEGORY_RE, EMPTY_STRING);
        Matcher matcher = NUMBER_PATTERN.matcher(trainName);
        if (matcher.matches()) {
            train.setName(matcher.group(1));
            train.setNumber(matcher.group(2));
        } else {
            train.setName(trainName);
        }
    }
}
