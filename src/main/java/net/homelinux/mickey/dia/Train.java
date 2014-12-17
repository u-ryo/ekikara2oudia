package net.homelinux.mickey.dia;

import java.util.ArrayList;
import java.util.List;

class Train implements Comparable<Train> {
    enum Type { LOCAL, RAPID, SECTIONAL_RAPID, NEW_RAPID, SPECIAL_RAPID,
                HOME_LINER, SEMI_EXPRESS, EXPRESS, LIMITED_EXPRESS,
                SUPER_EXPRESS, BUS, RAPID_EXPRESS, RAPID_LIMITED_EXPRESS,
                AIRPORT_EXPRESS
    };
    private String name, number, idNumber, note;
    private List<String[]> time = new ArrayList<String[]>();
    private Type type = Type.LOCAL;
    private int leastTime = -1, leastIndex = -1;

    Train(String idNumber) {
        this.idNumber = idNumber;
    }

    String getIdNumber() {
        return idNumber;
    }

    void setName(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }

    void setNumber(String number) {
        this.number = number;
    }

    String getNumber() {
        return number;
    }

    void setNote(String note) {
        this.note = note;
    }

    String getNote() {
        return note;
    }

    void addTime(List<String[]> time) {
        this.time.addAll(time);
    }

    List<String[]> getTime() {
        return time;
    }

    void setType(Type type) {
        this.type = type;
    }

    Type getType() {
        return type;
    }

    @Override
    public String toString() {
        StringBuilder returnString =
            new StringBuilder("[ID Number: " + idNumber + ", Name: " + name
                              + ", Number: " + number + ", Type: " + type
                              + ", Note: " + note + ", time: ");
        for (String[] t : time) {
            returnString.append("[" + t[0] + "/" + t[1] + "],");
        }
        return returnString + "], leastTime: " + leastTime + ", leastIndex: "
            + leastIndex;
    }

    private void calcLeastTime() {
        if (leastIndex > -1) {
            return;
        }
        int i = 0;
        for (String[] times : time) {
            org.apache.commons.logging.LogFactory.getLog(Train.class).trace(java.util.Arrays.toString(times)+":"+leastTime+":"+leastIndex);
            if (times != null) {
                if (times[0] != null && times[0].matches("\\d+")) {
                    leastTime = Integer.parseInt(times[0]);
                } else if (times[1] != null && times[1].matches("\\d+")) {
                    leastTime = Integer.parseInt(times[1]);
                }
            }
            if (leastTime >= 0) {
                leastIndex = i;
                if (leastTime < 300) {
                    leastTime += 2400;
                }
                break;
            }
            i++;
        }
    }

    @Override
    public int compareTo(Train train) {
        train.calcLeastTime();
        calcLeastTime();
        for (int i = 0; i < time.size(); i++) {
            if (time.get(i) == null || train.time.get(i) == null) {
                continue;
            }
            if (time.get(i)[0] != null && time.get(i)[0].matches("\\d+")
                && train.time.get(i)[0] != null
                && train.time.get(i)[0].matches("\\d+")) {
                return compare(time.get(i)[0], train.time.get(i)[0]);
            }
            if (time.get(i)[1] != null && time.get(i)[1].matches("\\d+")
                && train.time.get(i)[1] != null
                && train.time.get(i)[1].matches("\\d+")) {
                return compare(time.get(i)[1], train.time.get(i)[1]);
            }
        }
        return leastTime - train.leastTime != 0 ? leastTime - train.leastTime
            : leastIndex - train.leastIndex;
    }

    private int compare(String timeNum0, String timeNum1) {
        int time0 = Integer.parseInt(timeNum0),
            time1 = Integer.parseInt(timeNum1);
        return (time0 < 300 ? time0 + 2400 : time0)
            - (time1 < 300 ? time1 + 2400 : time1);
    }
}
