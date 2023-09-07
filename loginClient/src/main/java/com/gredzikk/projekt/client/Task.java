package com.gredzikk.projekt.client;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;


public class Task {
    private final SimpleStringProperty name = new SimpleStringProperty(this, "name");
    private final SimpleStringProperty note = new SimpleStringProperty(this, "note");
    private final SimpleBooleanProperty done = new SimpleBooleanProperty(this, "done");

    public Task(String name, String note, boolean done) {
        setName(name);
        setNote(note);
        setDone(done);
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getNote() {
        return note.get();
    }

    public SimpleStringProperty noteProperty() {
        return note;
    }

    public void setNote(String note) {
        this.note.set(note);
    }

    public boolean getDone() {
        return done.get();
    }

    public SimpleBooleanProperty doneProperty() {
        return done;
    }

    public void setDone(boolean done) {
        this.done.set(done);
    }
}
