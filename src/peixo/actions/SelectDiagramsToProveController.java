package peixo.actions;
//

import com.microsoft.z3.*;
import com.vp.plugin.*;
import com.vp.plugin.action.VPAction;
import com.vp.plugin.action.VPActionController;
import com.vp.plugin.diagram.IDiagramElement;
import com.vp.plugin.diagram.IDiagramUIModel;
import com.vp.plugin.diagram.IStateDiagramUIModel;
import com.vp.plugin.model.*;
import com.vp.plugin.view.IDialogHandler;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import peixo.VPPlugin;
import peixo.dialogs.SelectDiagramsToProveDialogHandler;
import peixo.datatypes.peixoDiagram;
import peixo.solver.SelectDiagramsToProveSolver;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import org.jgrapht.*;

public class SelectDiagramsToProveController implements VPActionController {
    ViewManager viewManager = VPPlugin.VIEW_MANAGER;
    ProjectManager pm = VPPlugin.PROJECT_MANAGER;


//    private Solver Solver;


    @Override
    public void performAction(VPAction vpAction) {
        try {
            IDialogHandler dialogHandler = new SelectDiagramsToProveDialogHandler();
            viewManager.showDialog(dialogHandler);
        } catch (Exception e) {
            viewManager.showMessage("ShowDialog kaputt");
        }
//        IDiagramUIModel diagram = ApplicationManager.instance().getDiagramManager().getActiveDiagram();
//        if (diagram instanceof IStateDiagramUIModel) {
//            IDiagramElement[] diagramElements = diagram.toDiagramElementArray();
//
//            for (IDiagramElement e : diagramElements) {
//                if (e.getModelElement() instanceof ITransition2) {
//                    viewManager.showMessage("Ich bin in Transition");
//                    ITransition2 trans = (ITransition2) e.getModelElement();
//                    IConstraintElement guard = trans.getGuard();
//                    viewManager.showMessage("Der Guard ist: " + guard.getSpecification().getValueAsString());
//                    viewManager.showMessage("Die Trans ist: " + trans.getName());
//                }
//                viewManager.showMessage("Name vom Element: " + e.getModelElement().getName() + " vom Typ: " + e.getModelElement().getModelType());
//            }
//        }
    }

    @Override
    public void update(VPAction vpAction) {

    }

    public ArrayList<IDiagramUIModel> getDiagrams() {
        ProjectManager projectManager = VPPlugin.PROJECT_MANAGER;
        Iterator diagramIter = projectManager.getProject().diagramIterator();
        ArrayList<IDiagramUIModel> diagramList = new ArrayList<IDiagramUIModel>() {
        };

        while (diagramIter.hasNext()) {
            IDiagramUIModel model = (IDiagramUIModel) diagramIter.next();
            diagramList.add(model);
        }
        return diagramList;
    }

    public Image getDiagramIcons(String id) {
        ExportDiagramAsImageOption option = new ExportDiagramAsImageOption(ExportDiagramAsImageOption.IMAGE_TYPE_PNG);
        option.setHeight(1000);
        option.setWidth(1000);
        option.setMaxSize(new Dimension(800, 600));
//        option.setScale(2.0F);
        ProjectManager projectManager = VPPlugin.PROJECT_MANAGER;
        DiagramManager diagramManager = VPPlugin.DIAGRAM_MANAGER;
        IDiagramUIModel activeDiagram = projectManager.getProject().getDiagramById(id);

        return ApplicationManager.instance().getModelConvertionManager().exportDiagramAsImage(activeDiagram, option);
    }

    public IDiagramElement[] getDiagramElementsInArray(IDiagramUIModel diagram) {
        return diagram.toDiagramElementArray();
    }

    public ArrayList<String> buildPaths(IDiagramElement[] diagramElements) {
        ArrayList<LinkedList<String>> pathsArrayList = new ArrayList<>();
        ArrayList<LinkedList<String>> buildPaths = new ArrayList<>();
        ArrayList<String> endingNodes = new ArrayList<>();
        ArrayList<IState2> allStates = new ArrayList<>();
        ArrayList<ITransition2> allTrans = new ArrayList<>();
        String initState = "";
        Graph<String, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);

        // Fill Graph

        for (IDiagramElement e : diagramElements) {
            // Get All States
            if (e.getModelElement() instanceof IState2) {
                IState2 state = (IState2) e.getModelElement();
                allStates.add(state);
                g.addVertex(state.getName());
            }
            // Find Inital State
            if (e.getModelElement() instanceof ITransition2) {
                ITransition2 trans = (ITransition2) e.getModelElement();
                allTrans.add(trans);
                if (trans.getFrom() instanceof IInitialPseudoState) {
                    IModelElement state = trans.getTo();
                    initState = state.getName();
                }
            }
        }
        // Find all Linked states
        for (IState2 s : allStates) {
            Iterator itor = s.fromRelationshipIterator();
            while (itor.hasNext()) {
                LinkedList<String> helperList = new LinkedList<>();
                ITransition2 transition = (ITransition2) itor.next();
                IState2 state = (IState2) transition.getTo();
                if (!Objects.equals(s.getId(), state.getId())) {
                    helperList.add(s.getName());
                    helperList.add(state.getName());
                    g.addEdge(s.getName(), state.getName());
                    pathsArrayList.add(helperList);
                }
            }
        }
        // Build Paths
        // Find all Ending Vertices
        Set<String> verticesWithoutSucc = g.vertexSet().stream().filter(v -> !Graphs.vertexHasSuccessors(g, v)).collect(Collectors.toSet());
        // Get all Paths from InitState to all Ending Vertices
        AllDirectedPaths<String, DefaultEdge> allDirectedPaths = new AllDirectedPaths<>(g);
        ArrayList<String> paths = new ArrayList<>();
        for (String endingState : verticesWithoutSucc) {
            String path = allDirectedPaths.getAllPaths(initState, endingState, false, 20).toString();
            paths.add(path);
        }
        return paths;

        /*
         Todo: Teste alle Transitions auf
            getFrom() == linkedStates.getKey()
            getTo() == linkedStates.getValue();
            Wenn TRUE -> Füge diese Trans dem Weg hinzu
        */

    }

    public void checkReachabilityStateMachines(List<peixoDiagram> diagrams) {
        StringBuilder stringbuilder = new StringBuilder();
        SelectDiagramsToProveSolver solverLogic = new SelectDiagramsToProveSolver();
        ArrayList<String> paths = new ArrayList<>();

        for (peixoDiagram d : diagrams) {
            if (d.getModelObject() instanceof IStateDiagramUIModel) {
                IDiagramElement[] diagramElements = getDiagramElementsInArray(d.getModelObject());
                paths = buildPaths(diagramElements);
                for (IDiagramElement e : diagramElements) {
                    if (e.getModelElement() instanceof ITransition2) {
                        ITransition2 trans = (ITransition2) e.getModelElement();
                        IConstraintElement constraint = trans.getGuard();
                        if (constraint != null) {
                            stringbuilder.append(constraint.getSpecification().getValueAsString()).append(" & ");
                        }
                    }
                    if (e.getModelElement() instanceof IState2) {
                        IState2 state = (IState2) e.getModelElement();
                    }
                }
            }
            viewManager.showMessage(stringbuilder.toString());
            try {
                Solver solver = solverLogic.buildSolverLogic(stringbuilder.toString());
                switch (solver.check()) {
                    case SATISFIABLE:
                        viewManager.showMessage("Solver is Satisfiable: " + Arrays.toString(solver.getAssertions()));
                        break;
                    case UNSATISFIABLE:
                        viewManager.showMessage("Solver is Unsatisfiable: " + Arrays.toString(solver.getAssertions()));
                        break;
                    case UNKNOWN:
                        viewManager.showMessage("Solver status is Unknown: " + Arrays.toString(solver.getAssertions()));
                        break;
                }
            } catch (Z3Exception | IndexOutOfBoundsException exception) {
                viewManager.showMessage(exception.getMessage());
            }
        }

//                        viewManager.showMessage("Es ist ein Constraint mit dem Value: " + constraint.getSpecification().getValueAsString() + "aus dem Diagram: " + d.getDiagramName());
//                    } else if (e.getModelElement() instanceof IState2) {
//                        IState2 state = (IState2) e.getModelElement();
////                        viewManager.showMessage("Es ist ein Zustand mit dem Namen: " + state.getName());
//                    }
//                }
//
//            for (IDiagramElement e : diagramElements) {
//                if (e.getModelElement() instanceof ITransition2) {
//                    viewManager.showMessage("Ich bin in Transition");
//                    ITransition2 trans = (ITransition2) e.getModelElement();
//                    IConstraintElement guard = trans.getGuard();
//                    viewManager.showMessage("Der Guard ist: " + guard.getSpecification().getValueAsString());
//                    viewManager.showMessage("Die Trans ist: " + trans.getName());
//                }
//                viewManager.showMessage("Name vom Element: " + e.getModelElement().getName() + " vom Typ: " + e.getModelElement().getModelType());
//            }
//        } }


//        viewManager.showMessage("Stringbuilder: " + stringbuilder.toString().trim().substring(0, stringbuilder.length() - 2));
    }
}


