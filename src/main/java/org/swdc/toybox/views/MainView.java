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
public class MainView extends AbstractSwingView {

    @Inject
    private Fontawsome5Service fontawsome5Service;

    @PostConstruct
    public void initView(){
        JFrame stage = getStage();
        stage.setSize(new Dimension(1000,800));
        stage.setMaximumSize(new Dimension(1000,800));
        stage.setLocationRelativeTo(null);
        stage.addWindowListener(new WindowAdapter() {

            @Override
            public void windowDeactivated(WindowEvent e) {
                long activeFrames = Arrays.stream(JFrame.getWindows())
                        .filter(java.awt.Window::isActive)
                        .count();
                boolean noStages = Stage.getWindows()
                        .filtered(Window::isFocused)
                        .isEmpty();
                if (activeFrames == 0 && noStages) {
                    hide();
                }
            }
        });

        setIcon(findById("locations"),"folder");
        setIcon(findById("refresh"), "sync-alt");
        setIcon(findById("config"),"cog");
        setIcon(findById("pin"),"map-marker");
        setIcon(findById("exts"),"cube");
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
