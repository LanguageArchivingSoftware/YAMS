package nl.mpi.kinnate.plugins.metadatasearchV1;

import javax.swing.JPanel;
import nl.mpi.arbil.plugin.ArbilWindowPlugin;
import nl.mpi.arbil.plugin.PluginArbilDataNodeLoader;
import nl.mpi.arbil.plugin.PluginBugCatcher;
import nl.mpi.arbil.plugin.PluginDialogHandler;
import nl.mpi.arbil.plugin.PluginException;
import nl.mpi.arbil.plugin.PluginSessionStorage;
import nl.mpi.kinnate.plugin.AbstractBasePlugin;
import nl.mpi.kinnate.plugins.metadatasearchV1.ui.FacetedTreePanel;

/**
 * Document : FacetedPlugin <br> Created on Sep 10, 2012, 5:13:47 PM <br>
 *
 * @author Peter Withers <br>
 */
public class FacetedPlugin extends AbstractBasePlugin implements ArbilWindowPlugin {

    public FacetedPlugin() throws PluginException {
        super("Faceted Tree Plugin 0-1 (good-with-more-files)", "A plugin for Arbil that provides a faceted tree via a XML DB.", "nl.mpi.kinnate.plugins.metadatasearch");
    }

    public JPanel getUiPanel(PluginDialogHandler dialogHandler, PluginSessionStorage sessionStorage, PluginBugCatcher bugCatcher, PluginArbilDataNodeLoader arbilDataNodeLoader) throws PluginException {
        final FacetedTreePanel facetedTreePanel = new FacetedTreePanel(arbilDataNodeLoader, dialogHandler);
        // trigger the facets to load
//        new Thread(facetedTreePanel.getRunnable("add")).start();
        new Thread(facetedTreePanel.getRunnable("options")).start();
        return facetedTreePanel;
    }
}