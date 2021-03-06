/**
 * Copyright (C) 2013 The Language Archive, Max Planck Institute for
 * Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package nl.mpi.yams.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;
import nl.mpi.yams.client.DatabaseInformation;
import nl.mpi.yams.client.controllers.HistoryController;
import nl.mpi.yams.client.HistoryListener;
import nl.mpi.yams.client.SearchOptionsServiceAsync;
import nl.mpi.yams.client.controllers.SearchHandler;
import nl.mpi.yams.common.data.QueryDataStructures.CriterionJoinType;
import nl.mpi.yams.common.data.QueryDataStructures.SearchOption;
import nl.mpi.yams.common.data.SearchParameters;

/**
 * Created on : Jan 29, 2013, 2:50:44 PM
 *
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class SearchWidgetsPanel extends VerticalPanel implements HistoryListener {

    private static final Logger logger = Logger.getLogger("");
    private static final String SEARCH_LABEL = "Search";
    private static final String SEARCHING_LABEL = "<img src='./loader.gif'/>&nbsp;Searching";
    private static final String ADD_SEARCH_TERM = "add search term";
    private static final String sendButtonStyle = "sendButton";
    private static final String NO_VALUE = "<no value>";
    private static final String DEMO_LIST_BOX_STYLE = "demo-ListBox";
    private final SearchOptionsServiceAsync searchOptionsService;
    private final HistoryController historyController;
    private String lastUsedDatabase = "";
    private Button searchButton;
    private SearchHandler searchHandler;
    private final ResultsPanel resultsPanel;
    private final ValueListBox<CriterionJoinType> joinTypeListBox;
    private final VerticalPanel verticalPanel;
    private final ArrayList<SearchCriterionPanel> criterionPanelList = new ArrayList<SearchCriterionPanel>();
    private final DatabaseInformation databaseInfo;

    public SearchWidgetsPanel(SearchOptionsServiceAsync searchOptionsService, final HistoryController historyController, DatabaseInformation databaseInfo, ResultsPanel resultsPanel, DataNodeTable dataNodeTable, final ArchiveBranchSelectionPanel archiveTreePanel) {
        this.searchOptionsService = searchOptionsService;
        this.historyController = historyController;
        this.databaseInfo = databaseInfo;
        this.resultsPanel = resultsPanel;
        
        verticalPanel = new VerticalPanel();
        this.add(verticalPanel);

        initSearchHandler();
        
        final SearchCriterionPanel searchCriterionPanel = new SearchCriterionPanel(SearchWidgetsPanel.this, searchOptionsService);
        if (archiveTreePanel != null) {
            verticalPanel.add(archiveTreePanel);
            historyController.addHistoryListener(archiveTreePanel);
        }
        verticalPanel.add(searchCriterionPanel);
        criterionPanelList.add(searchCriterionPanel);
        
        Button addRowButton = new Button(ADD_SEARCH_TERM, new ClickHandler() {
            public void onClick(ClickEvent event) {
                addSearchCriterionPanel(new SearchCriterionPanel(SearchWidgetsPanel.this, SearchWidgetsPanel.this.searchOptionsService));
            }
        });
        this.add(addRowButton);
        
        final HorizontalPanel buttonsPanel = new HorizontalPanel();
        this.add(buttonsPanel);
        
        joinTypeListBox = getJoinTypeListBox();
        buttonsPanel.add(joinTypeListBox);
        buttonsPanel.add(searchButton);

        this.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    }

    public void userSelectionChange() {
        final String databaseName = historyController.getDatabaseName();
        if (databaseName != null && !databaseName.isEmpty() && !databaseName.equals(lastUsedDatabase)) {
            historyChange();
        }
    }

    public void historyChange() {
        searchHandler.updateDbName();
        final CriterionJoinType criterionJoinType = historyController.getCriterionJoinType();
        if (criterionJoinType == null) {
            joinTypeListBox.setValue(CriterionJoinType.values()[0]);
        } else {
            joinTypeListBox.setValue(criterionJoinType);
        }
        final ArrayList<SearchParameters> searchParametersList = historyController.getSearchParametersList();
        if (searchParametersList != null && !searchParametersList.isEmpty()) {
            while (searchParametersList.size() < criterionPanelList.size()) {
                removeSearchCriterionPanel(criterionPanelList.get(criterionPanelList.size() - 1));
            }
            while (searchParametersList.size() > criterionPanelList.size()) {
                addSearchCriterionPanel(new SearchCriterionPanel(SearchWidgetsPanel.this, SearchWidgetsPanel.this.searchOptionsService));
            }
            for (int panelIndex = 0; panelIndex < criterionPanelList.size(); panelIndex++) {
                final SearchParameters historyValues = searchParametersList.get(panelIndex);
                criterionPanelList.get(panelIndex).setDefaultValues(historyValues.getFileType(), historyValues.getFieldType(), historyValues.getSearchNegator(), historyValues.getSearchType(), historyValues.getSearchString());
            }
        } else {
            while (!criterionPanelList.isEmpty()) {
                removeSearchCriterionPanel(criterionPanelList.get(0));
            }
            if (historyController.getDatabaseName() != null) {
                final SearchCriterionPanel searchCriterionPanel = new SearchCriterionPanel(SearchWidgetsPanel.this, SearchWidgetsPanel.this.searchOptionsService);
                addSearchCriterionPanel(searchCriterionPanel);
            }
        }
        final String databaseName = historyController.getDatabaseName();
        if (databaseName != null && !databaseName.isEmpty() && !databaseName.equals(lastUsedDatabase)) {
            lastUsedDatabase = databaseName;
            for (SearchCriterionPanel eventCriterionPanel : criterionPanelList) {
                eventCriterionPanel.setDatabase(databaseName);
            }
        }
    }

    protected void addSearchCriterionPanel(SearchCriterionPanel criterionPanel) {
        criterionPanelList.add(criterionPanel);
        verticalPanel.add(criterionPanel);
        if (!lastUsedDatabase.isEmpty()) {
            criterionPanel.setDatabase(lastUsedDatabase);
        }
        joinTypeListBox.setVisible(criterionPanelList.size() > 1);
    }

    protected void removeSearchCriterionPanel(SearchCriterionPanel criterionPanel) {
        criterionPanelList.remove(criterionPanel);
        verticalPanel.remove(criterionPanel);
        joinTypeListBox.setVisible(criterionPanelList.size() > 1);
    }

    private void initSearchHandler() {
        searchButton = new Button(SEARCH_LABEL);
        searchButton.addStyleName(sendButtonStyle);

        searchHandler = new SearchHandler(historyController, databaseInfo, searchOptionsService, resultsPanel) {
            @Override
            protected void prepareSearch() {
                searchButton.setEnabled(false);
                searchButton.setHTML(SEARCHING_LABEL);
                ArrayList<SearchParameters> searchParametersList = new ArrayList<SearchParameters>();
                for (SearchCriterionPanel eventCriterionPanel : criterionPanelList) {
                    //logger.info("eventCriterionPanel");
                    searchParametersList.add(new SearchParameters(eventCriterionPanel.getMetadataFileType(), eventCriterionPanel.getMetadataFieldType(), eventCriterionPanel.getSearchNegator(), eventCriterionPanel.getSearchType(), eventCriterionPanel.getSearchText()));
                }
                historyController.setSearchParameters(joinTypeListBox.getValue(), searchParametersList);
            }

            protected @Override
            void finaliseSearch() {
                searchButton.setEnabled(true);
                searchButton.setHTML(SEARCH_LABEL);
            }
        };
        searchButton.addClickHandler(searchHandler);
    }

    protected ValueListBox getSearchOptionsListBox() {
        final ValueListBox<SearchOption> widget = new ValueListBox<SearchOption>(new Renderer<SearchOption>() {
            public String render(SearchOption object) {
                if (object == null) {
                    return NO_VALUE;
                } else {
                    return object.toString();
                }
            }

            public void render(SearchOption object, Appendable appendable) throws IOException {
                if (object != null) {
                    appendable.append(object.toString());
                }
            }
        });
        widget.addStyleName(DEMO_LIST_BOX_STYLE);
        widget.setValue(SearchOption.equals);
        widget.setAcceptableValues(Arrays.asList(SearchOption.values()));
        return widget;
    }

    private ValueListBox getJoinTypeListBox() {
        final ValueListBox<CriterionJoinType> widget = new ValueListBox<CriterionJoinType>(new Renderer<CriterionJoinType>() {
            public String render(CriterionJoinType object) {
                if (object == null) {
                    return NO_VALUE;
                } else {
                    return object.toString();
                }
            }

            public void render(CriterionJoinType object, Appendable appendable) throws IOException {
                if (object != null) {
                    appendable.append(object.toString());
                }
            }
        });
        widget.addStyleName(DEMO_LIST_BOX_STYLE);
        widget.setValue(CriterionJoinType.intersect);
        widget.setAcceptableValues(Arrays.asList(CriterionJoinType.values()));
//        widget.addValueChangeHandler(new ValueChangeHandler<CriterionJoinType>() {
//
//            public void onValueChange(ValueChangeEvent<CriterionJoinType> event) {
//                historyController.setCriterionJoinType(event.getValue());
//            }
//        });
        return widget;
    }

    public SearchHandler getSearchHandler() {
        return searchHandler;
    }
}
