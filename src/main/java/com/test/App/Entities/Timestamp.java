package com.test.App.Entities;


import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "timestamps")
public class Timestamp
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date timestamp;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Timestamp timestamp1 = (Timestamp) o;
        return id == timestamp1.id &&
                Objects.equals(timestamp, timestamp1.timestamp);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, timestamp);
    }
}
