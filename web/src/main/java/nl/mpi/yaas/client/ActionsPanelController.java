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
package nl.mpi.yaas.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import java.util.List;
import java.util.logging.Logger;
import nl.mpi.flap.model.ModelException;
import nl.mpi.flap.model.SerialisableDataNode;
import nl.mpi.yaas.client.HistoryData.NodeActionType;
import static nl.mpi.yaas.client.HistoryData.NodeActionType.citation;
import static nl.mpi.yaas.client.HistoryData.NodeActionType.details;
import nl.mpi.yaas.common.data.DataNodeId;
import nl.mpi.yaas.common.data.IconTableBase64;

/**
 * @since Apr 11, 2014 1:39:33 PM (creation date)
 * @author Peter Withers <peter.withers@mpi.nl>
 */
public class ActionsPanelController implements HistoryListener {

    private static final Logger logger = Logger.getLogger("");
    private final DatabaseInfo databaseInfo;
    private final SearchOptionsServiceAsync searchOptionsService;
    private SerialisableDataNode dataNode = null;
    private final HistoryController historyController;
    final private RootPanel actionsTargetPanel;
    final private RootPanel detailsPanel;
    final private RootPanel homeLinkTag;
    final private RootPanel metadataSearchTag;
    final private RootPanel annotationContentSearchTag;
    final private RootPanel manageAccessRightsTag;
    final private RootPanel resourceAccessTag;
    final private RootPanel citationTag;
    final private RootPanel welcomePanelTag;
    final private RootPanel aboutTag;
    final private RootPanel viewTag;
    final private RootPanel downloadTag;
    final private RootPanel versionInfoTag;
    final private Label userLabel = new Label("User: unkown");
    final private LoginController loginController;
    final private RootPanel loginTag;
    final private RootPanel logoutTag;
    final ServiceLocations serviceLocations = GWT.create(ServiceLocations.class);

    public void historyChange() {
        final List<DataNodeId> branchSelectionList = historyController.getHistoryData().getBranchSelection();
        String nodeId = null;
        if (dataNode != null) {
            try {
                nodeId = dataNode.getID();
            } catch (ModelException exception) {
                logger.info(exception.getMessage());
            }
        }
        if (dataNode != null && nodeId != null && !branchSelectionList.isEmpty() && branchSelectionList.get(0).getIdString().equals(nodeId)) {
            // todo: if we start supporting multiple selections then this will need to change
            // the current data node is already selected so no change needed.
        } else {
            final String databaseName = historyController.getDatabaseName();
            if (databaseName != null) {
                final IconTableBase64 databaseIcons = databaseInfo.getDatabaseIcons(databaseName);
                final DataNodeLoader dataNodeLoader = new DataNodeLoader(searchOptionsService, databaseIcons, databaseName);
//                logger.info("branchSelectionList.size():" + branchSelectionList.size());
                if (branchSelectionList.isEmpty()) {
                    dataNode = null;
                    doNodeAction(historyController.getHistoryData().getNodeActionType());
                } else {
//                    logger.info("branchSelectionList.get(0).getIdString():" + branchSelectionList.get(0).getIdString());
                    dataNodeLoader.requestLoad(branchSelectionList, new DataNodeLoaderListener() {
                        public void dataNodeLoaded(List<SerialisableDataNode> dataNodeList) {
                            if (dataNodeList != null && !dataNodeList.isEmpty()) {
                                dataNode = dataNodeList.get(0);
                                logger.info(dataNode.getLabel());
                            } else {
                                logger.warning("dataNodeLoaded but the resulting list was empty");
                            }
                            doNodeAction(historyController.getHistoryData().getNodeActionType());
                        }

                        public void dataNodeLoadFailed(Throwable caught) {
                            logger.warning(caught.getMessage());
                        }
                    });
                }
            }
        }
    }

    public void userSelectionChange() {
        historyChange();
    }

    public ActionsPanelController(DatabaseInfo databaseInfo, SearchOptionsServiceAsync searchOptionsService, final HistoryController historyController, RootPanel welcomePanelTag, RootPanel actionsTargetPanel, RootPanel detailsPanel, RootPanel homeLinkTag, RootPanel metadataSearchTag, RootPanel annotationContentSearchTag, RootPanel manageAccessRightsTag, RootPanel resourceAccessTag, RootPanel citationTag, RootPanel aboutTag, RootPanel viewTag, RootPanel downloadTag, RootPanel versionInfoTag, RootPanel loginTag, RootPanel logoutTag, RootPanel userSpan) {
        this.databaseInfo = databaseInfo;
        this.searchOptionsService = searchOptionsService;
        this.historyController = historyController;
        this.welcomePanelTag = welcomePanelTag;
        this.actionsTargetPanel = actionsTargetPanel;
        this.detailsPanel = detailsPanel;
        this.homeLinkTag = homeLinkTag;
        this.metadataSearchTag = metadataSearchTag;
        this.annotationContentSearchTag = annotationContentSearchTag;
        this.manageAccessRightsTag = manageAccessRightsTag;
        this.resourceAccessTag = resourceAccessTag;
        this.citationTag = citationTag;
        this.aboutTag = aboutTag;
        this.viewTag = viewTag;
        this.downloadTag = downloadTag;
        this.versionInfoTag = versionInfoTag;
        this.loginTag = loginTag;
        this.logoutTag = logoutTag;
        loginController = new LoginController(this);
        userLabel.setStyleName("header");
        loginController.exportCheckLoginState(loginController);
        loginController.checkLoginState();
        loginController.startStatusTimer();
        if (userSpan != null) {
            userSpan.add(userLabel);
        }
        if (loginTag != null) {
            final Anchor loginAnchor = Anchor.wrap(loginTag.getElement());
            addPopupPanelAction(loginAnchor, serviceLocations.loginUrl());
        }
        if (logoutTag != null) {
            final Anchor loginAnchor = Anchor.wrap(logoutTag.getElement());
            addPopupPanelAction(loginAnchor, serviceLocations.logoutUrl());
        }
        if (metadataSearchTag != null) {
            addNodeAction(metadataSearchTag, NodeActionType.search);
        }
        if (annotationContentSearchTag != null) {
            addPageAction(annotationContentSearchTag, serviceLocations.trovaUrl());
        }
        if (manageAccessRightsTag != null) {
            addNodeAction(manageAccessRightsTag, NodeActionType.ams);
        }
        if (resourceAccessTag != null) {
            addNodeAction(resourceAccessTag, NodeActionType.rrs);
        }
        if (citationTag != null) {
            addNodeAction(citationTag, NodeActionType.citation);
        }
        if (aboutTag != null) {
            final Anchor aboutAnchor = Anchor.wrap(aboutTag.getElement());
            aboutAnchor.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {
                    final version versionProperties = GWT.create(version.class);
                    final DialogBox dialogBox = new DialogBox(true, true);
                    Grid grid = new Grid(5, 2);
                    dialogBox.setText("About YAMS Browser");
                    grid.setWidget(0, 0, new Label("Version:"));
                    grid.setWidget(0, 1, new Label(versionProperties.majorVersion()));
                    grid.setWidget(1, 0, new Label("Project Version:"));
                    grid.setWidget(1, 1, new Label(versionProperties.projectVersion()));
                    grid.setWidget(2, 0, new Label("Build:"));
                    grid.setWidget(2, 1, new Label(versionProperties.buildVersion()));
                    grid.setWidget(3, 0, new Label("Compile Date:"));
                    grid.setWidget(3, 1, new Label(versionProperties.compileDate()));
                    grid.setWidget(4, 0, new Label("Commit Date:"));
                    grid.setWidget(4, 1, new Label(versionProperties.lastCommitDate()));
                    dialogBox.setGlassEnabled(true);
                    dialogBox.setAnimationEnabled(true);
                    dialogBox.setWidget(grid);
                    dialogBox.center();
                }
            });
        }
        if (homeLinkTag != null) {
            final Anchor homeAnchor = Anchor.wrap(homeLinkTag.getElement());
            homeAnchor.addClickHandler(new ClickHandler() {

                public void onClick(ClickEvent event) {
                    historyController.getHistoryData().clearBranchSelection();
                    historyController.setAction(NodeActionType.home);
                }
            });
        }
        setDataNode(null);
    }

    private void addPopupPanelAction(final FocusWidget focusWidget, final String targetUrl) {
        focusWidget.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
//                final DialogBox.CaptionImpl caption = new DialogBox.CaptionImpl();
//                final DialogBox popupPanel = new DialogBox(true, true);
                PopupPanel popupPanel = new PopupPanel(true, true);
                popupPanel.setPixelSize(800, 600);
                popupPanel.setGlassEnabled(true);
                popupPanel.setAnimationEnabled(true);
//                final DialogBox.Caption caption = popupPanel.getCaption();
//                caption.addClickHandler(new ClickHandler() {
//                    public void onClick(ClickEvent event) {
//                        popupPanel.hide();
//                        loginController.checkLoginState();
//                    }
//                });
//                caption.setStylePrimaryName("load-more-btn");
//                caption.setText("close");
                try {
                    final Frame frame = new Frame(getFormattedHandleLink(targetUrl));
                    popupPanel.setWidget(frame);
                    popupPanel.center();
                } catch (ModelException exception) {
                    logger.warning(exception.getMessage());
                }
            }
        });
    }

    private void doNodeAction(final NodeActionType actionType) {
        if (detailsPanel != null) {
            detailsPanel.setVisible(false);
        }
        if (welcomePanelTag != null) {
            welcomePanelTag.setVisible(false);
        }
        actionsTargetPanel.clear();
        actionsTargetPanel.setVisible(true);
//                try {
        logger.info(actionType.name());
        switch (actionType) {
            case citation:
                final CitationPanel citationPanel = new CitationPanel();
                citationPanel.setDataNode(dataNode);
                actionsTargetPanel.add(citationPanel);
                break;
            case details:
                final MetadataDetailsPanel metadataDetailsPanel = new MetadataDetailsPanel();
//                actionsTargetPanel.add(metadataDetailsPanel);
                detailsPanel.clear();
                detailsPanel.add(metadataDetailsPanel);
                metadataDetailsPanel.setDataNode(dataNode);
                setDataNode(dataNode);
                break;
            case search:
                doPanelAction(serviceLocations.yamsUrl());
//                actionsTargetPanel.add(new SearchPanel());
                break;
            case ams:
                doPanelAction(serviceLocations.amsUrl());
                break;
            case rrs:
                doPanelAction(serviceLocations.rrsUrl());
                break;
            case home:
                setDataNode(null);
                break;
        }
//                } catch (ModelException exception) {
//                    logger.warning(exception.getMessage());
//                } 
    }

    private void addNodeAction(RootPanel rootPanel, final NodeActionType actionType) {
        final Button button = Button.wrap(rootPanel.getElement());
        button.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                doNodeAction(actionType);
            }
        });
    }

    private void doPanelAction(final String targetUrl) {
        if (detailsPanel != null) {
            detailsPanel.setVisible(false);
        }
        if (welcomePanelTag != null) {
            welcomePanelTag.setVisible(false);
        }
        actionsTargetPanel.clear();
        actionsTargetPanel.setVisible(true);
        try {
            final Frame frame = new Frame(getFormattedHandleLink(targetUrl));
            frame.setSize("100%", "100%");
            actionsTargetPanel.add(frame);
        } catch (ModelException exception) {
            logger.warning(exception.getMessage());
        }
    }

    private void addPageAction(RootPanel rootPanel, final String targetUrl) {
        final Button button = Button.wrap(rootPanel.getElement());
        button.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                try {
                    Window.open(getFormattedHandleLink(targetUrl), "targetUrl", "");
                } catch (ModelException exception) {
                    logger.warning(exception.getMessage());
                }
            }
        }
        );
    }

    private String getFormattedHandleLink(final String targetUrl) throws ModelException {
        if (dataNode != null) {
            // this should be replaced with the use of gwt messages to add the parameter
            return targetUrl.replace("{0}", dataNode.getURI());
        } else {
            return targetUrl;
        }
    }

    public final void setDataNode(SerialisableDataNode dataNode) {
        this.dataNode = dataNode;
        if (detailsPanel != null) {
            detailsPanel.setVisible(dataNode != null);
        }
        if (actionsTargetPanel != null) {
            actionsTargetPanel.clear();
            actionsTargetPanel.setVisible(false);
        }
        if (welcomePanelTag != null) {
            welcomePanelTag.setVisible(dataNode == null);
        }

        if (metadataSearchTag != null) {
            metadataSearchTag.setVisible(dataNode != null);
        }
        if (annotationContentSearchTag != null) {
            annotationContentSearchTag.setVisible(dataNode != null);
        }
        if (manageAccessRightsTag != null) {
            manageAccessRightsTag.setVisible(dataNode != null);
        }
        if (resourceAccessTag != null) {
            resourceAccessTag.setVisible(dataNode != null);
        }
        if (citationTag != null) {
            citationTag.setVisible(dataNode != null);
        }
        boolean showResourceButtons = (dataNode != null && dataNode.getType() != null && dataNode.getType().getFormat() == null);
        if (viewTag != null) {
            viewTag.setVisible(showResourceButtons);
        }
        if (downloadTag != null) {
            downloadTag.setVisible(showResourceButtons);
        }
        if (versionInfoTag != null) {
            versionInfoTag.setVisible(showResourceButtons);
        }
    }

    public void setLoginState(String userName, boolean anonymous) {
        userLabel.setText("User: " + userName);
        if (loginTag != null) {
            loginTag.setVisible(anonymous);
        }
        if (logoutTag != null) {
            logoutTag.setVisible(!anonymous);
        }
    }
}
