package org.swdc.toybox.extension.fsmapper.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.swdc.toybox.extension.fsmapper.repo.impl.MappedFolderRepo;

@DatabaseTable(daoClass = MappedFolderRepo.class)
public class MappedFolder {

    @DatabaseField(generatedId = true)
    private Long id;

    @DatabaseField
    private String path;

    @DatabaseField
    private double posX;

    @DatabaseField
    private double posY;

    @DatabaseField
    private double width;

    @DatabaseField
    private double height;

    @DatabaseField
    private boolean visible;

    @DatabaseField
    private boolean locked;

    @DatabaseField(defaultValue = "LIST_VIEW")
    private ViewType viewType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public double getPosX() {
        return posX;
    }

    public void setPosX(double posX) {
        this.posX = posX;
    }

    public double getPosY() {
        return posY;
    }

    public void setPosY(double posY) {
        this.posY = posY;
    }

    public ViewType getViewType() {
        return viewType;
    }

    public void setViewType(ViewType viewType) {
        this.viewType = viewType;
    }
}
