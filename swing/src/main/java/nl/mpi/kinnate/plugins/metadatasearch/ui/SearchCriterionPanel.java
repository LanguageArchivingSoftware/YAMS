package nl.mpi.kinnate.plugins.metadatasearch.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import nl.mpi.yaas.common.data.MetadataFileType;
import nl.mpi.yaas.common.data.QueryDataStructures.SearchNegator;
import nl.mpi.yaas.common.data.QueryDataStructures.SearchOption;
import nl.mpi.yaas.common.data.QueryDataStructures.SearchType;

/**
 * Document : SearchCriterionPanel <br> Created on Aug 31, 2012, 4:23:32 PM <br>
 *
 * @author Peter Withers <br>
 */
public class SearchCriterionPanel extends JPanel {

    final SearchOptionBox searchPathOptionBox;
    final SearchOptionBox searchFieldOptionBox;
//    final JComboBox searchNegatorOption;
    final JComboBox searchTypeOption;
    final JTextField searchText;

    public SearchCriterionPanel(ActionListener actionListener, MetadataFileType[] metadataPathTypes, MetadataFileType[] metadataFieldTypes, int shownCriterionCount) {
        super(new BorderLayout());
        final JPanel criterionPanel = new JPanel();
        criterionPanel.setLayout(new FlowLayout());

//        if (shownCriterionCount > 0) {
        JButton removeButton = new JButton("-");
        removeButton.setToolTipText("Remove this criterion");
        removeButton.setActionCommand("remove");
        removeButton.addActionListener(actionListener);
        removeButton.setPreferredSize(new Dimension(removeButton.getPreferredSize().height, removeButton.getPreferredSize().height));
        criterionPanel.add(removeButton);
//        } else {
//            JButton addExtraButton = new JButton("+");
//            addExtraButton.setToolTipText("Add another criterion");
//            addExtraButton.setActionCommand("add");
//            addExtraButton.addActionListener(actionListener);
//            addExtraButton.setPreferredSize(new Dimension(addExtraButton.getPreferredSize().height, addExtraButton.getPreferredSize().height));
//            criterionPanel.add(addExtraButton);
//        }
//        searchNegatorOption = new JComboBox(ArbilDatabase.SearchNegator.values());
//        criterionPanel.add(searchNegatorOption);

        searchPathOptionBox = new SearchOptionBox();
//        searchPathOptionBox.addItem("Loading");
        searchPathOptionBox.setTypes(metadataPathTypes);
        searchPathOptionBox.addActionListener(actionListener);
        searchPathOptionBox.setActionCommand("paths");
        criterionPanel.add(searchPathOptionBox);

        searchFieldOptionBox = new SearchOptionBox();
//        searchFieldOptionBox.addItem("Loading");
        searchFieldOptionBox.setTypes(metadataFieldTypes);
        searchFieldOptionBox.addActionListener(actionListener);
        searchFieldOptionBox.setActionCommand("fields");
        criterionPanel.add(searchFieldOptionBox);

        searchTypeOption = new JComboBox(SearchOption.values());
        criterionPanel.add(searchTypeOption);

        searchText = new JTextField();
        this.add(searchText, BorderLayout.CENTER);
        this.add(criterionPanel, BorderLayout.LINE_START);
    }

    public MetadataFileType getMetadataFileType() {
        final Object selectedTypeItem = searchPathOptionBox.getSelectedItem();
        MetadataFileType metadataFileType = null;
        if (selectedTypeItem instanceof MetadataFileType) {
            metadataFileType = (MetadataFileType) selectedTypeItem;
        }
        return metadataFileType;
    }

    public MetadataFileType getMetadataFieldType() {
        final Object selectedFieldItem = searchFieldOptionBox.getSelectedItem();
        MetadataFileType metadataFieldType = null;
        if (selectedFieldItem instanceof MetadataFileType) {
            metadataFieldType = (MetadataFileType) selectedFieldItem;
        }
        return metadataFieldType;
    }

    public SearchOption getSearchOption() {
        return SearchOption.values()[searchTypeOption.getSelectedIndex()];
    }

    public SearchNegator getSearchNegator() {
        return getSearchOption().getSearchNegator();
    }

    public SearchType getSearchType() {
        return getSearchOption().getSearchType();
    }

//    public void setFileOptions(MetadataFileType[] metadataPathTypes) {
//        searchPathOptionBox.setTypes(metadataPathTypes);
//    }
//
    public void setFieldOptions(MetadataFileType[] metadataFieldTypes) {
        searchFieldOptionBox.setTypes(metadataFieldTypes);
    }

    public String getSearchText() {
        return searchText.getText();
    }
}
