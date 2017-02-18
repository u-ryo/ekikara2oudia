package net.homelinux.mickey.dia;

class Station {
    private String name;
    enum Type { GENERAL, MAIN };
    private Type type = Type.GENERAL;

    Station(String name) {
        this.name = name;
    }

    Station(String name, Type type) {
        this.name = name;
        this.type = type;
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

    @Override
    public String toString() {
        return "[Name: " + name + ", Type: " + type + "]";
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Station && obj.toString().equals(toString());
    }
}
