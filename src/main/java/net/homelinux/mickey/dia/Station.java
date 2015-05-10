package net.homelinux.mickey.dia;

class Station {
    private String name;
    enum Type { GENERAL, MAIN };
    private Type type = Type.GENERAL;

    Station(String name) {
        this.name = name;
    }

    void setType(Type type) {
        this.type = type;
    }

    Type getType() {
        return type;
    }

    String getName() {
        return name;
    }

    public String toString() {
        return "[Name: " + name + ", Type: " + type + "]";
    }
}
