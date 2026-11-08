package org.swdc.toybox.views;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.swdc.dependency.DependencyContext;
import org.swdc.fx.font.FontSize;
import org.swdc.fx.font.Fontawsome5Service;
import org.swdc.fx.view.AbstractSwingView;
import org.swdc.fx.view.AbstractView;
import org.swdc.fx.view.View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

@View(
        viewLocation = "views/main/MainView.fxml",
        title = "%toybox.name",
        resizeable = false,
        windowStyle = StageStyle.TRANSPARENT
)
public class MainView extends AbstractView {

    @Inject
    private Fontawsome5Service fontawsome5Service;

    @Inject
    private HostWindow hostWindow;

    @PostConstruct
    public void initView(){

        Stage stage = getStage();
        stage.initOwner(hostWindow.getStage());
        stage.setWidth(1000);
        stage.setHeight(800);
        stage.setMaxHeight(800);
        stage.setMaxWidth(1000);

        setIcon(findById("locations"),"folder");
        setIcon(findById("refresh"), "sync-alt");
        setIcon(findById("config"),"cog");
        setIcon(findById("pin"),"map-marker");

    }

    public void resetAndShow() {
        this.hide();
        Stage stage = getStage();
        stage.setAlwaysOnTop(true);
        stage.show();
        stage.setAlwaysOnTop(isPinSelected());
    }

    public void setIcon(ButtonBase labeled, String icon) {
        labeled.setFont(fontawsome5Service.getSolidFont(FontSize.MIDDLE_SMALL));
        labeled.setText(fontawsome5Service.getFontIcon(icon));
    }


    public boolean isPinSelected() {
        ToggleButton button = findById("pin");
        return button.isSelected();
    }


    public void focusField(){
        TextField searchField = findById("searchField");
        searchField.requestFocus();
    }

}
