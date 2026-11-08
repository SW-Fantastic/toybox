package org.swdc.toybox.extension.fsmapper.views;

import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import org.controlsfx.control.GridView;

public class SelectableGridView<T> extends GridView<T> {


    static class GridViewSelectionModel<T> extends MultipleSelectionModel<T> {

        private ObjectProperty<ObservableList<T>> itemsProperty = null;

        private ObservableList<Integer> selectedIndices = FXCollections.observableArrayList();
        private ObservableList<T> selectedItems = FXCollections.observableArrayList();

        public GridViewSelectionModel(GridView<T> grid) {
            this.itemsProperty = grid.itemsProperty();
            this.itemsProperty.addListener(this::listRebind);
        }

        private void listRebind(Observable observable, ObservableList<T> oldItems, ObservableList<T> newItems) {
            this.clearSelection();
            if (oldItems != null) {
                oldItems.removeListener((ListChangeListener<? super T>) this::listChanged);
            }
            if (newItems != null) {
                newItems.addListener((ListChangeListener<? super T>)this::listChanged);
            }
        }

        private void listChanged(ListChangeListener.Change<? extends T> change) {
            while (change.next()) {
                int from = change.getFrom();
                if (change.wasRemoved()) {
                    int removedSize = change.getRemoved().size();
                    // 清除被移除的索引，因为它们不再存在于列表中。
                    selectedIndices.removeIf(index ->
                            index >= from && index < from + removedSize
                    );
                    for (int i = 0; i < selectedIndices.size(); i++) {
                        int index = selectedIndices.get(i);
                        if (index >= from +  removedSize) {
                            // 该索引大于removedIndex，需要减去removedSize。
                            selectedIndices.set(i, index - removedSize);
                        }
                    }
                    selectedItems.removeAll(change.getRemoved());
                }
                if (change.wasAdded()) {
                    // 添加新元素，调整已存在的索引值。
                    int addedSize = change.getAddedSize();
                    for (int i = 0; i < selectedIndices.size(); i++) {
                        int index = selectedIndices.get(i);
                        if (index >= from) {
                            // 该索引位于from之后，需要将它加上addedSize，因为新增的元素占据了原来的位置。
                            selectedIndices.set(i, index + addedSize);
                        }
                    }
                }
            }
        }

        private ObservableList<T> getItems() {
            return itemsProperty.get();
        }

        private void preformSelection(int index) {

            if (index < 0 || index >= getItems().size()) {
                throw new IndexOutOfBoundsException(getItems().size() + " is out of bounds.");
            }

            T item = getItems().get(index);
            selectedItems.add(item);
            selectedIndices.add(index);

        }

        private void preformDeselection(int index) {

            if (index < 0 || index >= getItems().size()) {
                throw new IndexOutOfBoundsException(getItems().size() + " is out of bounds.");
            }

            T item = getItems().get(index);
            selectedItems.remove(item);
            selectedIndices.remove((Integer) index);

        }


        @Override
        public ObservableList<Integer> getSelectedIndices() {
            return selectedIndices;
        }

        @Override
        public ObservableList<T> getSelectedItems() {
            return selectedItems;
        }

        @Override
        public void selectIndices(int index, int... indices) {
            if (getSelectionMode() == SelectionMode.SINGLE) {
                select(index);
            }
            for (int i : indices) {
                select(i);
            }
        }

        @Override
        public void selectAll() {
            for (int idx = 0; idx < getItems().size();idx ++) {
                select(idx);
            }
        }

        @Override
        public void clearAndSelect(int index) {
            clearSelection();
            select(index);
        }

        @Override
        public void select(int index) {
            if (index < 0 || index >= getItems().size()) {
                return;
            }
            if (getSelectionMode() == SelectionMode.SINGLE) {
                clearSelection();
            }
            preformSelection(index);

        }

        @Override
        public void select(T obj) {
            if (obj == null) {
                return;
            }
            int index = getItems().indexOf(obj);
            if (index < 0) {
                return;
            }
            preformSelection(index);
        }

        @Override
        public void clearSelection(int index) {
            if (index < 0 || index >= getItems().size()) {
                return;
            }
            preformDeselection(index);
        }

        @Override
        public void clearSelection() {
            selectedIndices.clear();
            selectedItems.clear();
        }

        @Override
        public boolean isSelected(int index) {
            return selectedIndices.contains(index);
        }

        @Override
        public boolean isEmpty() {
            return selectedIndices.isEmpty();
        }

        @Override
        public void selectPrevious() {
            select(getSelectedIndex() - 1);
        }

        @Override
        public void selectNext() {
            select(getSelectedIndex() + 1);
        }

        @Override
        public void selectFirst() {
            select(0);
        }

        @Override
        public void selectLast() {
            select(getItems().size() - 1);
        }

    }

    private ObjectProperty<MultipleSelectionModel<T>> selectionModel = null;

    public SelectableGridView() {
        super();
        this.selectionModel = new SimpleObjectProperty<>(new GridViewSelectionModel<T>(this));
    }

    public MultipleSelectionModel<T> getSelectionModel() {
        return selectionModel.get();
    }

    public ObjectProperty<MultipleSelectionModel<T>> selectionModelProperty() {
        return selectionModel;
    }

}
