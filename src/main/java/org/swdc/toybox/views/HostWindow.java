package org.swdc.toybox.views;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.stage.StageStyle;
import org.swdc.fx.view.AbstractView;
import org.swdc.fx.view.View;

@View(windowStyle = StageStyle.UTILITY, opacity = 0.0)
public class HostWindow extends AbstractView {

    @Override
    public Node getView() {
        return new BorderPane();
    }

}
