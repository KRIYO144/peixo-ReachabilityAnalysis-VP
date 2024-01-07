package peixo.datatypes;

import com.vp.plugin.diagram.IDiagramUIModel;

public class peixoDiagram {
    private String DiagramId;
    private String DiagramName;

    private IDiagramUIModel ModelObject;

    protected peixoDiagram(String id, String name, IDiagramUIModel m) {
        this.DiagramId = id;
        this.DiagramName = name;
        this.ModelObject = m;
    }

    public IDiagramUIModel getModelObject() {
        return ModelObject;
    }

    public String getDiagramId() {
        return DiagramId;
    }

    public String getDiagramName() {
        return DiagramName;
    }

    @Override
    public String toString() {
        return DiagramName;
    }
}