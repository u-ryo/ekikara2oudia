package net.homelinux.mickey.dia;

import java.util.ArrayList;
import java.util.List;

class Train implements Comparable<Train> {
    enum Type { LOCAL, RAPID, SECTIONAL_RAPID, NEW_RAPID, SPECIAL_RAPID,
                HOME_LINER, SEMI_EXPRESS, EXPRESS, LIMITED_EXPRESS,
                NOZOMI_SUPER_EXPRESS, HIKARI_SUPER_EXPRESS,
                KODAMA_SUPER_EXPRESS, BUS, RAPID_EXPRESS,
                RAPID_LIMITED_EXPRESS, AIRPORT_EXPRESS
    };
    private String name, number, idNumber, note;
    private List<String[]> time = new ArrayList<String[]>();
    private Type type = Type.LOCAL;

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
        return returnString + "]";
    }

    @Override
    public int compareTo(Train train) {
        Integer[] departure = new Integer[2], arrival = new Integer[2];
        for (int i = 0; i < time.size(); i++) {
            if (time.get(i)[0] != null && time.get(i)[0].matches("\\d+")) {
                int t = Integer.parseInt(time.get(i)[0]);
                t = t < 300 ? t + 2400 : t;
                arrival[0] = t - i * 2;
            }
            if (time.get(i)[1] != null && time.get(i)[1].matches("\\d+")) {
                if (departure[0] == null) {
                    int t = Integer.parseInt(time.get(i)[1]);
                    t = t < 300 ? t + 2400 : t;
                    departure[0] = t - i * 2;
                }
            }
            if (train.time.get(i)[0] != null && train.time.get(i)[0].matches("\\d+")) {
                int t = Integer.parseInt(train.time.get(i)[0]);
                t = t < 300 ? t + 2400 : t;
                arrival[1] = t - i * 2;
            }
            if (train.time.get(i)[1] != null && train.time.get(i)[1].matches("\\d+")) {
                if (departure[1] == null) {
                    int t = Integer.parseInt(train.time.get(i)[1]);
                    t = t < 300 ? t + 2400 : t;
                    departure[1] = t - i * 2;
                }
            }
            if (arrival[0] != null && departure[1] != null) {
                return arrival[0] - departure[1];
            }
            if (arrival[1] != null && departure[0] != null) {
                return departure[0] - arrival[1];
            }
            if (departure[0] != null && departure[1] != null) {
                return departure[0] - departure[1];
            }
        }
        return 0;
    }

    private int compare(String timeNum0, String timeNum1) {
        int time0 = Integer.parseInt(timeNum0),
            time1 = Integer.parseInt(timeNum1);
        return (time0 < 300 ? time0 + 2400 : time0)
            - (time1 < 300 ? time1 + 2400 : time1);
    }
}
