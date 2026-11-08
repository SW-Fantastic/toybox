package org.swdc.toybox.views;

import org.swdc.fx.view.AbstractView;

import java.util.ResourceBundle;

public class PrefItem {

    private String name;

    private AbstractView view;

    private ResourceBundle bundle;

    public PrefItem(ResourceBundle bundle, String name, AbstractView view) {
        this.name = name;
        this.view = view;
        this.bundle = bundle;
    }

    public String getName() {
        if (name.startsWith("%")) {
            return bundle.getString(name.substring(1));
        }
        return name;
    }

    public AbstractView getView() {
        return view;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int hashCode() {
        return getClass().getName().hashCode() + getName().hashCode();
    }


}
