package org.swdc.toybox.views;

import jakarta.inject.Inject;
import org.swdc.fx.view.AbstractView;
import org.swdc.fx.view.View;
import org.swdc.toybox.core.service.UIService;

@View(viewLocation = "views/main/FolderDialogView.fxml",title = "%toybox.view.index-paths",resizeable = false)
public class IndexFolderView extends AbstractView {

    @Inject
    private UIService uiService;

    @Override
    public void show() {
        getStage().setAlwaysOnTop(uiService.isAlwaysOnTop());
        super.show();
    }
}
