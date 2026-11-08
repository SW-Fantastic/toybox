package org.swdc.toybox.extension.fsmapper.entity;

import org.swdc.dependency.event.AbstractEvent;

public class FileUpdateEvent extends AbstractEvent {

    public FileUpdateEvent(MappedFolder file) {
        super(file);
    }

    @Override
    public MappedFolder getMessage() {
        return super.getMessage();
    }
}
