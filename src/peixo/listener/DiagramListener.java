package peixo.listener;

import com.vp.plugin.ApplicationManager;
import com.vp.plugin.ViewManager;
import com.vp.plugin.diagram.IDiagramElement;
import com.vp.plugin.diagram.IDiagramListener;
import com.vp.plugin.diagram.IDiagramUIModel;

public class DiagramListener implements IDiagramListener {

    ViewManager _viewManager = ApplicationManager.instance().getViewManager();

    public DiagramListener() {
    }

    @Override
    public void diagramElementAdded(IDiagramUIModel arg0, IDiagramElement arg1) {
        _viewManager.showMessage("Diagram Element " + arg1.getModelElement().getName() + " Added");
    }

    @Override
    public void diagramElementRemoved(IDiagramUIModel arg0, IDiagramElement arg1) {
        _viewManager.showMessage("Diagram Element " + arg1.getModelElement().getName() + " Removed");

    }

    @Override
    public void diagramUIModelLoaded(IDiagramUIModel arg0) {
        _viewManager.showMessage("Diagram " + arg0.getType() + " : " + arg0.getName() + " Loaded");

    }

    @Override
    public void diagramUIModelPropertyChanged(IDiagramUIModel arg0,
                                              String arg1, Object arg2, Object arg3) {
        _viewManager.showMessage("Diagram " + arg0.getType() + " : " + arg0.getName() + " Modified");

    }

    @Override
    public void diagramUIModelRenamed(IDiagramUIModel arg0) {
        System.out.println("Diagram " + arg0.getType() + " : " + arg0.getName() + " Renamed");

    }

}
