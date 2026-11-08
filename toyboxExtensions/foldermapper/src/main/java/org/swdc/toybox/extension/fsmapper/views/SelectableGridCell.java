package org.swdc.toybox.extension.fsmapper.views;

import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;

public class SelectableGridCell<T> extends GridCell<T> {

    public SelectableGridCell() {
        super();
        this.setOnMouseClicked(e -> {

            if (getIndex() == -1) {
                return;
            }

            SelectableGridView<T> selectableGridView = getGridView();
            MultipleSelectionModel<T> selectionModel = selectableGridView.getSelectionModel();
            if (selectionModel.getSelectionMode() ==  SelectionMode.MULTIPLE && e.isControlDown()) {
                selectionModel.select(getIndex());
            } else {
                selectionModel.clearAndSelect(getIndex());
            }

            preformSelectionChanged(null);

        });

        gridViewProperty().addListener(this::preformSelectionModelChanged);
        indexProperty().addListener(this::preformSelectionChanged);
    }

    private void preformSelectionModelChanged(ObservableValue<? extends GridView<T>> observableValue, GridView<T> oldView, GridView<T> newView) {
        SelectableGridView<T> old = (SelectableGridView<T>) oldView;
        if (old != null) {
            old.getSelectionModel().getSelectedItems().removeListener(
                    this::preformSelectionChanged
            );
        }

        SelectableGridView<T> newGrid = (SelectableGridView<T>) newView;
        if (newGrid != null) {
            newGrid.getSelectionModel().getSelectedItems().addListener(
                    this::preformSelectionChanged
            );
        }

    }

    private void preformSelectionChanged(Observable observable) {
        SelectableGridView<T> gridView = getGridView();
        MultipleSelectionModel<T> selectionModel = gridView.getSelectionModel();
        pseudoClassStateChanged(
                PseudoClass.getPseudoClass("selected"),
                selectionModel.isSelected(getIndex())
        );
    }


    public SelectableGridView<T> getGridView() {
        return (SelectableGridView<T>) super.getGridView();
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        preformSelectionChanged(null);
    }
}
