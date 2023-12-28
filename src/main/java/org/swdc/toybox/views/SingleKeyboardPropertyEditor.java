package org.swdc.toybox.views;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.swdc.fx.config.ConfigPropertiesItem;
import org.swdc.fx.config.PropEditorView;
import org.swdc.toybox.LangConstants;
import org.swdc.toybox.core.NativeKeyUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SingleKeyboardPropertyEditor extends PropEditorView {

    private HBox hbox;

    private TextField field;

    public SingleKeyboardPropertyEditor(ConfigPropertiesItem item) {
        super(item);
    }

    protected void createEditor() {

        ResourceBundle resourceBundle = getResources().getResourceBundle();

        hbox = new HBox();
        hbox.setSpacing(8);
        field = new TextField();
        Button select = new Button();
        select.setText(resourceBundle.getString(LangConstants.CONF_MODIFY));

        select.setOnAction(e-> {
            field.requestFocus();
            select.setDisable(true);
        });

        HBox.setHgrow(hbox, Priority.ALWAYS);
        hbox.getChildren().addAll(field, select);

        ConfigPropertiesItem item = getItem();
        field.setText(item.getValue() == null ? "" : item.getValue().toString());
        field.setEditable(false);
        field.setOnKeyReleased(e -> {
            if (!select.isDisabled()) {
                return;
            }
            int nCode = NativeKeyUtils.getNativeKeyCode(e.getCode());
            field.setText(NativeKeyUtils.getKeyCodeFromNative(nCode).getName());
            select.setDisable(false);
            getItem().setValue(NativeKeyUtils.keyCodeToString(new Integer[]{ nCode }));
        });

        field.focusedProperty().addListener(e -> {
            select.setDisable(false);
        });

    }


    @Override
    public Node getEditor() {
        if (hbox == null) {
            createEditor();
        }
        return hbox;
    }

    @Override
    public Object getValue() {
        if (hbox == null) {
            createEditor();
        }
        return field.getText();
    }

    @Override
    public void setValue(Object o) {
        if (o == null) {
            return;
        }
        if (hbox == null) {
            createEditor();
        }
        String text = o.toString();
        Integer[] codes = NativeKeyUtils.stringToKeyCode(text);

        StringBuilder stringBuilder = new StringBuilder();
        for (int code : codes) {
            if (stringBuilder.isEmpty()) {
                stringBuilder.append(NativeKeyUtils.getKeyCodeFromNative(code).getName());
            } else {
                stringBuilder.append(" + ").append(NativeKeyUtils.getKeyCodeFromNative(code));
            }
        }
        field.setText(stringBuilder.toString());
    }
}
