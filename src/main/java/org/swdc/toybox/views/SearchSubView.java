package org.swdc.toybox.views;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import org.swdc.fx.font.FontSize;
import org.swdc.fx.font.Fontawsome5Service;
import org.swdc.fx.view.AbstractView;
import org.swdc.fx.view.View;
import org.swdc.toybox.views.controllers.SearchSubViewController;

import java.io.File;
import java.util.List;

@View(stage = false, viewLocation = "views/main/SearchView.fxml")
public class SearchSubView extends AbstractView {

    private ToggleGroup group = new ToggleGroup();

    @Inject
    private Fontawsome5Service fontawsome5Service;

    @PostConstruct
    public void init() {
        initButtonStyle(findById("filterFolder"), "folder",true);
        initButtonStyle(findById("filterFile"), "file",true);
    }

    public void initButtonStyle(ToggleButton button, String icon, boolean state) {
        button.setPadding(new Insets(4));
        button.setText(fontawsome5Service.getFontIcon(icon));
        button.setFont(fontawsome5Service.getSolidFont(FontSize.MIDDLE_SMALL));
        button.setSelected(state);
    }

    public void setResult(List<File> files) {
        SearchSubViewController controller = getController();
        controller.setResults(files);
    }

    public boolean filterIncludeFiles() {
        ToggleButton filterFiles = findById("filterFile");
        return filterFiles.isSelected();
    }

    public boolean filterIncludeFolders() {
        ToggleButton filterFolders = findById("filterFolder");
        return filterFolders.isSelected();
    }

    public boolean hasFilter(){
        return filterIncludeFiles() || filterIncludeFolders();
    }

    public void focusList() {
        ListView<File> listView = findById("resultList");
        listView.getSelectionModel().select(0);
        listView.requestFocus();
    }

}
