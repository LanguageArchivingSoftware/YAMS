/*
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nl.mpi.yams.client.ui;

import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import nl.mpi.yams.client.DatabaseInformation;
import nl.mpi.yams.client.controllers.HistoryController;
import nl.mpi.yams.client.HistoryListener;
import nl.mpi.yams.client.SearchOptionsServiceAsync;

/**
 * @since May 16, 2014 2:37:56 PM (creation date)
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class SearchPanel extends VerticalPanel implements HistoryListener {

    private static final String NO__DATABASE__SELECTED = "No Database Selected";
    private final HistoryController historyController;
    private final DatabaseInformation databaseInfo;
    final Label noDatabaseLabel = new Label(NO__DATABASE__SELECTED);
    private String lastUsedDatabase = null;
    final private Image loadingImage = new Image("./loader.gif");
    private final SearchWidgetsPanel searchOptionsPanel;
    private DatabaseSelect databaseSelectBox;

    public SearchPanel(HistoryController historyController, DatabaseInformation databaseInfo, ResultsPanel resultsPanel, SearchOptionsServiceAsync searchOptionsService, DataNodeTable dataNodeTable, ArchiveBranchSelectionPanel archiveBranchSelectionPanel) {
        this.historyController = historyController;
        this.databaseInfo = databaseInfo;
        loadingImage.setVisible(false);
        DisclosurePanel disclosurePanel = new DisclosurePanel("Search Options");
        databaseSelectBox = new DatabaseSelect(historyController, databaseInfo);
        historyController.addHistoryListener(this);
        historyController.addHistoryListener(databaseSelectBox);
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.add(databaseSelectBox);
        verticalPanel.add(noDatabaseLabel);
        searchOptionsPanel = new SearchWidgetsPanel(searchOptionsService, historyController, databaseInfo, resultsPanel, dataNodeTable, archiveBranchSelectionPanel);
        historyController.addHistoryListener(searchOptionsPanel);
        searchOptionsPanel.setVisible(false);

        verticalPanel.add(searchOptionsPanel);
        disclosurePanel.setContent(verticalPanel);
        this.add(disclosurePanel);
    }

    public void historyChange() {
        final String databaseName = historyController.getDatabaseName();
        if (databaseName != null && !databaseName.equals(lastUsedDatabase)) {
            lastUsedDatabase = databaseName;
            loadingImage.setVisible(true);
            searchOptionsPanel.setVisible(false);
            noDatabaseLabel.setVisible(true);
        }
    }

    /**
     * calls {@link #userSelectionChange() } on this object and its
     * {@link SearchWidgetsPanel} and {@link DatabaseSelect} children
     */
    public void refresh() {
        userSelectionChange();
        searchOptionsPanel.userSelectionChange();
        databaseSelectBox.userSelectionChange();
    }

    public void userSelectionChange() {
        if (databaseInfo.hasDatabaseError()) {
            noDatabaseLabel.setVisible(true);
            loadingImage.setVisible(false);
        }
        if (databaseInfo.ready()) {
            noDatabaseLabel.setVisible(false);
            loadingImage.setVisible(false);
            searchOptionsPanel.setVisible(true);
        }
    }
}
