package org.example.solobet.parser.entitys;

import java.util.Objects;

public class Odd {
    private String name;
    private String type;
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Odd odd = (Odd) o;
        return Objects.equals(name, odd.name) &&
                Objects.equals(type, odd.type) &&
                Objects.equals(value, odd.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, value);
    }

    @Override
    public String toString() {
        return "Odd{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
