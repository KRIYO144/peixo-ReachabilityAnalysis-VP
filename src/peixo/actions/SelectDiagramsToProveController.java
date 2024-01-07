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


    public void checkReachability(List<peixoDiagram> diagrams) {

        SelectDiagramsToProveSolver solverLogic = new SelectDiagramsToProveSolver();
        ArrayList<ArrayList<String>> paths = new ArrayList<>();
        ArrayList<String> builtPaths = new ArrayList<>();


        for (peixoDiagram d : diagrams) {
            if (d.getModelObject() instanceof IStateDiagramUIModel) {
                IDiagramUIModel activeDiagram = d.getModelObject();
                IDiagramElement[] diagramElements = getDiagramElementsInArray(d.getModelObject());
                paths = buildPaths(diagramElements, activeDiagram.getId());
                for (ArrayList<String> selectedBuiltPath : paths) {
                    StringBuilder stringbuilder = new StringBuilder();
                    int counter = 0;
                    for (String path : selectedBuiltPath) {
                        String firstStateActivities = path.substring(0, path.indexOf("[") - 1).replaceAll("\\(", "");
//                        String transitionConstraint = path.substring(path.indexOf("[") + 1, path.indexOf("]"));
                        String transitionConstraint = path.substring(path.indexOf("$") + 1, path.lastIndexOf("$"));
                        String lastStateActivities = path.substring(path.lastIndexOf("$") + 1).replaceAll("\\)", "");
                        String[] splitFirstStateActivities = firstStateActivities.trim().split("#");
                        String[] splitLastStateActivities = lastStateActivities.trim().split("#");
                        String[] splitTransitionConstraint = transitionConstraint.trim().split(" ");

                        // Fill String with FirstState Activities


//                        if (counter == 0) {
                            for (String s : splitFirstStateActivities) {
                                if (!s.isBlank()) {
                                    stringbuilder.append(s).append(" & ");
                                }
                            }
//                            counter++;
//                        }
                        // Fill String with Transition Constraint
                        for (String s : splitTransitionConstraint) {
                            if (!s.isBlank()) {
                                stringbuilder.append(s).append(" & ");
                            } else if (s.isBlank()) {
                                stringbuilder.append(" ");
                            }
                        }
                        // Fill String with LastState Activities
//                        for (String s : splitLastStateActivities) {
//                            if (!s.isBlank()) {
//                                stringbuilder.append(s).append(" & ");
//                            }
//                        }
                    }
                    String lastStateActivities = selectedBuiltPath.get(selectedBuiltPath.size() - 1).
                            substring(selectedBuiltPath.get(selectedBuiltPath.size() - 1).
                                    lastIndexOf("$") + 1).replaceAll("\\)", "");
                    String[] splitLastStateActivities = lastStateActivities.trim().split("#");
                    for (String s : splitLastStateActivities) {
                        if (!s.isBlank()) {
                            stringbuilder.append(s).append(" & ");
                        }
                    }
                    builtPaths.add(stringbuilder.substring(0, stringbuilder.lastIndexOf("&")));
                }
                // ToDo: Aktuell geht alles außer State < State2
                int unsatisSolvers = 0;
                for (String s : builtPaths) {
                    try {
                        Solver solver = solverLogic.buildSolverLogic(s);
                        switch (solver.check()) {
                            case SATISFIABLE:
                                viewManager.showMessage("Solver is Satisfiable: " + Arrays.toString(solver.getAssertions()));
                                viewManager.showMessage("Hier ist das Model: " + solver.getModel().toString());
                                break;
                            case UNSATISFIABLE:
                                unsatisSolvers++;
                                viewManager.showMessage("Solver is Unsatisfiable: " + Arrays.toString(solver.getAssertions()));
                                viewManager.showMessage("Dieser Pfad ist nicht erreichbar: " + s);
                                break;
                            case UNKNOWN:
                                viewManager.showMessage("Solver status is Unknown: " + Arrays.toString(solver.getAssertions()));
                                break;
                        }
                    } catch (Z3Exception | IndexOutOfBoundsException exception) {
                        unsatisSolvers++;
                        viewManager.showMessage(exception.getMessage());
                    }
                }
                if (unsatisSolvers != 0) {
                    viewManager.showMessage("Einer der Pfade ist nicht erreichbar.");
                } else if (unsatisSolvers == 0) {
                    viewManager.showMessage("Alle Pfade sind erreichbar.");
                }
            }
        }
    }

    public ArrayList<ArrayList<String>> buildPaths(IDiagramElement[] diagramElements, String activeDiagram) {
        ArrayList<LinkedList<String>> pathsArrayList = new ArrayList<>();
        ArrayList<IState2> allStates = new ArrayList<>();
        ArrayList<ITransition2> allTrans = new ArrayList<>();
        ArrayList<IChoice> allChoices = new ArrayList<>();
        String initState = "";
        Graph<String, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        ArrayList<ArrayList<String>> allPathsWithTransitions = new ArrayList<>();
        ArrayList<ArrayList<String>> allPathsWithTransitionsAndStateActions = new ArrayList<>();
        // Fill Graph

        for (IDiagramElement e : diagramElements) {
            // Get All States
            if (e.getModelElement() instanceof IState2) {
                IState2 state = (IState2) e.getModelElement();
                allStates.add(state);
                g.addVertex(state.getId());
            }
            if (e.getModelElement() instanceof IChoice) {
                IChoice choice = (IChoice) e.getModelElement();
                String name = choice.getName();
                allChoices.add(choice);
                g.addVertex(choice.getId());
            }
            // Find Inital State
            if (e.getModelElement() instanceof ITransition2) {
                ITransition2 trans = (ITransition2) e.getModelElement();
                allTrans.add(trans);
                if (trans.getFrom() instanceof IInitialPseudoState) {
                    IModelElement state = trans.getTo();
                    initState = state.getId();
                }
            }
        }
        // Find all Linked states
        for (IState2 s : allStates) {
            Iterator itor = s.fromRelationshipIterator();
            while (itor.hasNext()) {
                LinkedList<String> helperList = new LinkedList<>();
                ITransition2 transition = (ITransition2) itor.next();
//                IState2 state = (IState2) transition.getTo();
                IModelElement state = transition.getTo();
                if (!Objects.equals(s.getId(), state.getId())) {
                    helperList.add(s.getId());
                    helperList.add(state.getId());
                    g.addEdge(s.getId(), state.getId());
                    pathsArrayList.add(helperList);
                }
            }
        }
        for (IChoice c : allChoices) {
            Iterator itor = c.fromRelationshipIterator();
            while (itor.hasNext()) {
                LinkedList<String> helperList = new LinkedList<>();
                ITransition2 transition = (ITransition2) itor.next();
                IModelElement choice = transition.getTo();
                if (!Objects.equals(c.getId(), choice.getId())) {
                    helperList.add(c.getName());
                    helperList.add(choice.getName());
                    g.addEdge(c.getId(), choice.getId());
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
//        viewManager.showMessage("Alle Paths: " + Arrays.toString(paths.toArray()));

        for (String path : paths) {
            String[] s = path.split("\\)], ");
            // Iterate all single paths
            for (int i = 0; i < s.length; i++) {
                String[] linked = s[i].split("\\), \\(");
                ArrayList<String> pathsWithTransitions = new ArrayList<>();
                // Iterate over a selected path
                for (int z = 0; z < linked.length; z++) {
                    StringBuilder stringBuilder = new StringBuilder();
                    String firstState = linked[z].substring(0, linked[z].indexOf(":")).replaceAll("\\[", "").replaceAll("\\(", "").replaceAll(" ", "");
                    String lastState = linked[z].substring(linked[z].indexOf(":")).replaceAll("]", "").replaceAll("\\)", "").replaceAll(":", "").replaceAll(" ", "");
                    // Start Stringbuild
                    stringBuilder.append("(").append(firstState).append(" ");
                    // Get the Transition from State to State
                    for (ITransition2 trans : allTrans) {
                        String from = trans.getFrom().getId();
                        String to = trans.getTo().getId();
                        IModelElement element;
                        if (from.equals(firstState) && to.equals(lastState)) {
                            if (trans.getGuard() != null && trans.getEffect() == null) {
                                stringBuilder.append("$[").append(trans.getGuard().getSpecification().getValueAsString()).append("]$").append(" ");
                                element = trans.getEffect();
                                // If the Transition doenst have a guard
                            }
                            if (trans.getGuard() == null && trans.getEffect() == null) {
                                stringBuilder.append("$[ ]$ ");
                            }
                            if (trans.getGuard() != null && trans.getEffect() != null) {
                                stringBuilder.append("$[").append(trans.getGuard().getSpecification().getValueAsString()).append("]").append(" ");
                            }
                            if (trans.getGuard() == null && trans.getEffect() != null) {
                                IActivity effect = (IActivity) trans.getEffect();
                                stringBuilder.append("$[").append(effect.getBody()).append("]$").append(" ");
                            }
                            if (trans.getEffect() != null && trans.getGuard() != null) {
                                IActivity effect = (IActivity) trans.getEffect();
                                stringBuilder.append("[").append(effect.getBody()).append("]$").append(" ");
                            }
                        }
                    }

                    stringBuilder.append(lastState).append(")");
                    pathsWithTransitions.add(stringBuilder.toString());
                }
                allPathsWithTransitions.add(pathsWithTransitions);
            }
        }

        for (ArrayList<String> pathsWithTrans : allPathsWithTransitions) {
            ArrayList<String> helperlist = new ArrayList<>();
            for (String link : pathsWithTrans) {
                StringBuilder stringBuilder = new StringBuilder();
                String firstState = link.substring(1, link.indexOf("[") - 2);
                String lastState = link.substring(link.lastIndexOf(("]")) + 3, link.indexOf(")"));
                String guardForTrans = link.substring(link.indexOf("["), link.indexOf("]") + 1);
                String activityForTrans = "";
                if (!(link.indexOf("[") == link.lastIndexOf("["))) {
                    activityForTrans = link.substring(link.lastIndexOf("["), link.lastIndexOf("]") + 1);
                }
                ArrayList<IModelElement> objects = new ArrayList<>();
                IDiagramElement[] diagramElementArray = pm.getProject().getDiagramById(activeDiagram).toDiagramElementArray();

                for (IDiagramElement el2 : diagramElementArray) {
                    if (el2.getModelElement() instanceof IState2) {
                        IState2 state = (IState2) el2.getModelElement();
                        if (Objects.equals(state.getId(), firstState)) {
                            objects.add(state);
                        }
                    } else if (el2.getModelElement() instanceof IChoice) {
                        IChoice choice = (IChoice) el2.getModelElement();
                        if (Objects.equals(choice.getId(), firstState)) {
                            objects.add(choice);
                        }
                    }
                }

                for (IDiagramElement el2 : diagramElementArray) {
                    if (el2.getModelElement() instanceof IState2) {
                        IState2 state = (IState2) el2.getModelElement();
                        if (Objects.equals(state.getId(), lastState)) {
                            objects.add(state);
                        }
                    } else if (el2.getModelElement() instanceof IChoice) {
                        IChoice choice = (IChoice) el2.getModelElement();
                        if (Objects.equals(choice.getId(), lastState)) {
                            objects.add(choice);
                        }
                    }
                }
                stringBuilder.append("(");
                int counter = 0;
                for (IModelElement e : objects) {
                    if (e instanceof IChoice) {
                        counter++;
                        stringBuilder.append(" ");
                        if (counter == 1) {
                            stringBuilder.append("$").append(guardForTrans).append(" ");
                            if (!activityForTrans.isBlank()) {
                                stringBuilder.append(activityForTrans).append("$").append(" ");
                            } else if (activityForTrans.isBlank()) {
                                stringBuilder.append("$ ");
                            }
                        }
                    }
                    if (e instanceof IState2) {
                        IState2 state = (IState2) e;
                        counter++;
                        if (state.getEntry() != null) {
                            stringBuilder.append("#").append(state.getEntry().getBody()).append(" ");
                        } else if (state.getEntry() == null) {
                            stringBuilder.append(" ");
                        }
                        if (state.getDoActivity() != null) {
                            stringBuilder.append("#").append(state.getDoActivity().getBody()).append(" ");
                        } else if (state.getDoActivity() == null) {
                            stringBuilder.append(" ");
                        }
                        // Here Guard
                        if (counter == 1) {
                            stringBuilder.append("$").append(guardForTrans).append(" ");
                        }
                        if (state.getExit() != null) {
                            stringBuilder.append("#").append(state.getExit().getBody()).append(" ");
                        } else if (state.getExit() == null) {
                            stringBuilder.append(" ");
                        }
                        // Here Activity of Trans
                        if (counter == 1) {
                            if (!activityForTrans.isBlank()) {
                                stringBuilder.append(activityForTrans).append("$ ");
                            } else if (activityForTrans.isBlank()) {
                                stringBuilder.append("$ ");
                            }
                        }

                    }
                }
                stringBuilder.append(")");

//                IState2 lastStateObject = (IState2) pm.getProject().getDiagramElementById(lastState).getModelElement();
//                    stringBuilder.append("(");
//                if(!objects.isEmpty()) {
//                    if (objects.get(0).getEntry() != null) {
//                        stringBuilder.append("#").append(objects.get(0).getEntry().getBody()).append(" ");
//                    } else if (objects.get(0).getEntry() == null) {
//                        stringBuilder.append(" ");
//                    }
//                    if (objects.get(0).getDoActivity() != null) {
//                        stringBuilder.append("#").append(objects.get(0).getDoActivity().getBody()).append(" ");
//                    } else if (objects.get(0).getDoActivity() == null) {
//                        stringBuilder.append(" ");
//                    }
//                    if (objects.get(0).getExit() != null) {
//                        stringBuilder.append("#").append(objects.get(0).getExit().getBody()).append(" ");
//                    } else if (objects.get(0).getExit() == null) {
//                        stringBuilder.append(" ");
//                    }
//                    stringBuilder.append(guardForTrans).append(" ");
//
//
//                    if (objects.get(1).getEntry() != null) {
//                        stringBuilder.append("#").append(objects.get(1).getEntry().getBody()).append(" ");
//                    } else if (objects.get(0).getEntry() == null) {
//                        stringBuilder.append("#").append(" ");
//                    }
//                    if (objects.get(1).getDoActivity() != null) {
//                        stringBuilder.append("#").append(objects.get(1).getDoActivity().getBody()).append(" ");
//                    } else if (objects.get(0).getEntry() == null) {
//                        stringBuilder.append(" ");
//                    }
//                    if (objects.get(1).getExit() != null) {
//                        stringBuilder.append("#").append(objects.get(1).getExit().getBody()).append(" ");
//                    } else if (objects.get(0).getEntry() == null) {
//                        stringBuilder.append(" ");
//                    }
//                }
//                stringBuilder.append(")");
                helperlist.add(stringBuilder.toString());
            }
            allPathsWithTransitionsAndStateActions.add(helperlist);
        }
        return allPathsWithTransitionsAndStateActions;

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



