/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.mpi.kinnate.plugins.metadatasearch.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import nl.mpi.flap.kinnate.entityindexer.QueryException;
import nl.mpi.flap.model.DataField;
import nl.mpi.flap.model.PluginDataNode;
import nl.mpi.flap.plugin.PluginArbilTable;
import nl.mpi.flap.plugin.PluginArbilTableModel;
import nl.mpi.flap.plugin.PluginBugCatcher;
import nl.mpi.flap.plugin.PluginDialogHandler;
import nl.mpi.flap.plugin.PluginException;
import nl.mpi.flap.plugin.PluginSessionStorage;
import nl.mpi.flap.plugin.PluginWidgetFactory;
import nl.mpi.flap.plugin.WrongNodeTypeException;
import nl.mpi.kinnate.plugins.metadatasearch.data.DbTreeNode;
import nl.mpi.kinnate.plugins.metadatasearch.data.MetadataTreeNode;
import nl.mpi.yaas.common.data.MetadataFileType;
import nl.mpi.yaas.common.db.DataBaseManager;
import nl.mpi.yaas.common.db.LocalDbAdaptor;

/**
 * Document : FacetedTreePanel <br> Created on Aug 23, 2012, 3:20:13 PM <br>
 *
 * @author Peter Withers <br>
 */
public class FacetedTreePanel extends JPanel implements ActionListener {

    private DataBaseManager<DbTreeNode, DataField, MetadataFileType> yaasDatabase;
    final private PluginDialogHandler arbilWindowManager;
    private ArrayList<SearchOptionBox> searchPathOptionBoxList;
    private JProgressBar jProgressBar;
    private int actionProgressCounter = 0;
    private JTree resultsTree;
    private DefaultTreeModel defaultTreeModel;
    private PluginArbilTable arbilTable;
    private PluginArbilTableModel arbilTableModel;
    private JPanel criterionPanel;
    private MetadataFileType[] metadataFieldTypes = null;

    public FacetedTreePanel(final PluginDialogHandler dialogHandler, final PluginBugCatcher pluginBugCatcher, PluginSessionStorage pluginSessionStorage, PluginWidgetFactory pluginWidgetFactory) {
        arbilWindowManager = dialogHandler;
        this.setLayout(new BorderLayout());
        try {
            yaasDatabase = new DataBaseManager<DbTreeNode, DataField, MetadataFileType>(DbTreeNode.class, DataField.class, MetadataFileType.class, new LocalDbAdaptor(pluginSessionStorage.getProjectDirectory()), DataBaseManager.defaultDataBase);
        } catch (QueryException exception) {
            this.add(new JLabel(exception.getMessage()), BorderLayout.CENTER);
            return;
        }
        criterionPanel = new JPanel();
        criterionPanel.setLayout(new FlowLayout());

        final JPanel criterionOuterPanel = new JPanel(new BorderLayout());

        searchPathOptionBoxList = new ArrayList<SearchOptionBox>();

        final JPanel criterionButtonsPanel = new JPanel(new BorderLayout());

        final JButton addButton = new JButton("+");
        addButton.setActionCommand("add");
        addButton.addActionListener(this);
        criterionButtonsPanel.add(addButton, BorderLayout.PAGE_START);

        final JButton removeButton = new JButton("-");
        removeButton.setActionCommand("remove");
        removeButton.addActionListener(this);
        criterionButtonsPanel.add(removeButton, BorderLayout.PAGE_END);

        criterionOuterPanel.add(new JScrollPane(criterionPanel), BorderLayout.CENTER);
        criterionOuterPanel.add(criterionButtonsPanel, BorderLayout.LINE_END);

        this.add(criterionOuterPanel, BorderLayout.PAGE_START);
        JPanel centerPanel = new JPanel(new BorderLayout());
        JPanel progressPanel = new JPanel(new BorderLayout());

        final JButton createButton = new JButton("create db");
        createButton.setActionCommand("create");
//        final JButton optionsButton = new JButton("reload options");
//        optionsButton.setActionCommand("options");
//        optionsButton.addActionListener(this);
        createButton.addActionListener(this);

        JPanel dbButtonsPanel = new JPanel();
        dbButtonsPanel.add(createButton);
//        dbButtonsPanel.add(optionsButton);

        progressPanel.add(dbButtonsPanel, BorderLayout.LINE_START);
        jProgressBar = new JProgressBar();

        progressPanel.add(jProgressBar, BorderLayout.CENTER);

        JPanel searchButtonsPanel = new JPanel();
//        final JButton searchButton = new JButton("Search");
//        searchButton.setActionCommand("search");
//        searchButton.addActionListener(this);
//        searchButtonsPanel.add(searchButton);
        progressPanel.add(searchButtonsPanel, BorderLayout.LINE_END);

        centerPanel.add(progressPanel, BorderLayout.PAGE_START);
        defaultTreeModel = new DefaultTreeModel(new DbTreeNode("Please add or select a facet"));
        resultsTree = new JTree(defaultTreeModel);
        resultsTree.setCellRenderer(new SearchTreeCellRenderer());
        resultsTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent tse) {
                ArrayList<PluginDataNode> arbilDataNodeList = new ArrayList<PluginDataNode>();
                final TreePath[] selectionPaths = resultsTree.getSelectionPaths();
                if (selectionPaths != null) {
                    for (TreePath treePath : selectionPaths) {
                        final Object lastPathComponent = treePath.getLastPathComponent();
                        if (lastPathComponent instanceof MetadataTreeNode) {
                            final PluginDataNode arbilNode = ((MetadataTreeNode) lastPathComponent).getArbilNode();
                            if (arbilNode != null) {
                                arbilDataNodeList.add(arbilNode);
                            }
                        }
                    }
                }
                arbilTableModel.removeAllArbilDataNodeRows();
                arbilTableModel.addArbilDataNodes(arbilDataNodeList.toArray(new PluginDataNode[0]));
            }
        });
        centerPanel.add(new JScrollPane(resultsTree), BorderLayout.CENTER);

        arbilTableModel = pluginWidgetFactory.createTableModel();
        arbilTable = pluginWidgetFactory.createTable(arbilTableModel, "FacetedTreeSelectionTable");
        JSplitPane jSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, centerPanel, new JScrollPane((Component) arbilTable));

        this.add(jSplitPane, BorderLayout.CENTER);
    }

    static public void main(String[] args) {
        JFrame jFrame = new JFrame("Faceted Tree Panel Test");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final PluginSessionStorage sessionStorage = new PluginSessionStorage() {
            public File getApplicationSettingsDirectory() {
                return new File("/Users/petwit2/.arbil/");
            }

            public File getProjectDirectory() {
                return new File("/Users/petwit2/.arbil/");
            }

            public File getProjectWorkingDirectory() {
                return new File("/Users/petwit2/.arbil/ArbilWorkingFiles/");
            }
        };
//        final PluginDataNodeLoader dataNodeLoader = new PluginDataNodeLoader() {
//            public PluginDataNode getPluginDataNode(Object registeringObject, final URI localUri) {
//                return new PluginDataNode() {
//                    public String getIconId() {
//                        return null;
//                    }
//
//                    public PluginDataNode[] getChildArray() {
//                        return new PluginDataNode[0];
//                    }
//
//                    @Override
//                    public String toString() {
//                        return localUri.toString();
//                    }
//
//                    public String getID() {
//                        throw new UnsupportedOperationException("Not supported yet.");
//                    }
//                };
//            }

//            public URI getNodeURI(PluginDataNode dataNode) throws WrongNodeTypeException {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
//
//            public boolean isNodeLoading(PluginDataNode dataNode) {
//                return false;
//            }
//        };
        PluginDialogHandler dialogHandler = new PluginDialogHandler() {
            public void addMessageDialogToQueue(String messageString, String messageTitle) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public boolean showConfirmDialogBox(String messageString, String messageTitle) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public int showDialogBox(String message, String title, int optionType, int messageType) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public int showDialogBox(String message, String title, int optionType, int messageType, Object[] options, Object initialValue) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public File[] showFileSelectBox(String titleText, boolean directorySelectOnly, boolean multipleSelect, Map<String, FileFilter> fileFilterMap, PluginDialogHandler.DialogueType dialogueType, JComponent customAccessory) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        PluginBugCatcher bugCatcher = new PluginBugCatcher() {
            public void logException(PluginException exception) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        PluginWidgetFactory widgetFactory = new PluginWidgetFactory() {
            public PluginArbilTable createTable(PluginArbilTableModel pluginArbilTableModel, String tableName) {
                class mockTable extends JTable implements PluginArbilTable {
                };
                return new mockTable();
            }

            public PluginArbilTableModel createTableModel() {
                return new PluginArbilTableModel() {
                    public void removeAllArbilDataNodeRows() {
//                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    public void addArbilDataNodes(PluginDataNode[] pluginArbilDataNodes) {
//                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                };
            }
        };

        final FacetedTreePanel facetedTreePanel = new FacetedTreePanel(dialogHandler, bugCatcher, sessionStorage, widgetFactory);
        jFrame.setContentPane(facetedTreePanel);
        jFrame.pack();
        jFrame.setVisible(true);
        // trigger the facets to load
//        new Thread(facetedTreePanel.getRunnable("add")).start();
        new Thread(facetedTreePanel.getRunnable("options")).start();
    }

    public void actionPerformed(ActionEvent e) {
        actionProgressCounter++;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                jProgressBar.setIndeterminate(actionProgressCounter > 0);
            }
        });
        final String actionCommand = e.getActionCommand();
        System.out.println(actionCommand);
        new Thread(getRunnable(actionCommand)).start();
    }

    private void updateTree() {
        System.out.println("updateTree");
        if (metadataFieldTypes == null) {
            System.out.println("metadataFieldTypes not loaded yet");
            return;
        }
        ArrayList<MetadataFileType> treeBranchTypeList = new ArrayList<MetadataFileType>();
        for (SearchOptionBox searchOptionBox : searchPathOptionBoxList) {
            final Object selectedTypeItem = searchOptionBox.getSelectedItem();
            if (selectedTypeItem instanceof MetadataFileType) {
                treeBranchTypeList.add((MetadataFileType) selectedTypeItem);
            }
        }
        System.out.println("run query");
        DbTreeNode rootTreeNode;
        try {
            rootTreeNode = yaasDatabase.getTreeData(treeBranchTypeList);
        } catch (QueryException exception) {
            arbilWindowManager.addMessageDialogToQueue(exception.getMessage(), "Database Error");
            rootTreeNode = new DbTreeNode();
        }
        final DbTreeNode rootTreeNodeFinal = rootTreeNode;
        rootTreeNode.setParentDbTreeNode(null, defaultTreeModel, yaasDatabase);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                defaultTreeModel.setRoot(rootTreeNodeFinal);
            }
        });
        System.out.println("done");
    }

    public Runnable getRunnable(final String actionCommand) {
        System.out.println("getRunnable: " + actionCommand);
        return new Runnable() {
            public void run() {
                System.out.println("run: " + actionCommand);
//                if ("create".equals(actionCommand)) {
//                    try {
//                        System.out.println("create db");
//                        arbilDatabase.createDatabase();
//                        System.out.println("done");
//                    } catch (QueryException exception) {
//                        arbilWindowManager.addMessageDialogToQueue(exception.getMessage(), "Database Error");
//                        exception.printStackTrace();
//                    }
//                } else 
                if ("options".equals(actionCommand)) {
                    try {
                        System.out.println("run fast options query");
                        metadataFieldTypes = yaasDatabase.getTreeFieldTypes(null, true);
                        System.out.println("done fast options query");
                        for (SearchOptionBox searchOptionBox : searchPathOptionBoxList) {
                            searchOptionBox.setTypes(metadataFieldTypes);
                        }
                        System.out.println("run detailed options query");
                        metadataFieldTypes = yaasDatabase.getTreeFieldTypes(null, false);
                        System.out.println("done detailed options query");
                        for (SearchOptionBox searchOptionBox : searchPathOptionBoxList) {
                            searchOptionBox.setTypes(metadataFieldTypes);
                        }
                    } catch (QueryException exception) {
                        arbilWindowManager.addMessageDialogToQueue(exception.getMessage(), "Database Error");
                        metadataFieldTypes = new MetadataFileType[0];
                    }
                } else if ("treechange".equals(actionCommand)) {
                    updateTree();
                } else if ("add".equals(actionCommand)) {
                    final SearchOptionBox treePathOptionBox = new SearchOptionBox();
                    if (metadataFieldTypes == null) {
                        treePathOptionBox.addItem("Loading");
                    } else {
                        treePathOptionBox.setTypes(metadataFieldTypes);
                    }
                    treePathOptionBox.addActionListener(FacetedTreePanel.this);
                    treePathOptionBox.setActionCommand("treechange");
                    searchPathOptionBoxList.add(treePathOptionBox);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            criterionPanel.add(treePathOptionBox);
                            criterionPanel.revalidate();
                            criterionPanel.repaint();
                        }
                    });
                    updateTree();
                } else if ("remove".equals(actionCommand)) {
                    if (searchPathOptionBoxList.size() > 0) {
                        final SearchOptionBox searchOptionBox = searchPathOptionBoxList.remove(searchPathOptionBoxList.size() - 1);
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                criterionPanel.remove(searchOptionBox);
                                criterionPanel.revalidate();
                                criterionPanel.repaint();
                            }
                        });
                    }
                    updateTree();
                }
                actionProgressCounter--;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        jProgressBar.setIndeterminate(actionProgressCounter > 0);
                    }
                });
            }
        };
    }
}
