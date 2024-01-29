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
    boolean buildLogic = true;


//    private Solver Solver;

    /**
     * The Method that gets called when the Class SelectDiagramsToProveController is
     * instantiated
     *
     * @param vpAction an Action from Visual Paradigm
     */
    @Override
    public void performAction(VPAction vpAction) {
        try {
            IDialogHandler dialogHandler = new SelectDiagramsToProveDialogHandler();
            viewManager.showDialog(dialogHandler);
        } catch (Exception e) {
            viewManager.showMessage("ShowDialog kaputt");
        }
    }

    @Override
    public void update(VPAction vpAction) {

    }

    /**
     * This Method returns all Diagrams within the active Project as a ArrayList<IDiagramUIModel>
     *
     * @return a ArrayList<IDiagramUIModel> with all the Diagrams of the active Project
     */
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

    /**
     * This Helper-method exports the specified Diagram as an Image
     *
     * @param id The ID of the Diagram for which the Image is exported
     * @return an Image of the specified Diagram
     */
    public Image getDiagramIcons(String id) {
        ExportDiagramAsImageOption option = new ExportDiagramAsImageOption(ExportDiagramAsImageOption.IMAGE_TYPE_PNG);
        option.setHeight(1000);
        option.setWidth(1000);
        option.setMaxSize(new Dimension(640, 800));
        ProjectManager projectManager = VPPlugin.PROJECT_MANAGER;
        IDiagramUIModel activeDiagram = projectManager.getProject().getDiagramById(id);

        return ApplicationManager.instance().getModelConvertionManager().exportDiagramAsImage(activeDiagram, option);
    }

    /**
     * This Helper-method returns an Array with the Model elements of the specified Diagram
     *
     * @param diagram The Object of the specified Diagram
     * @return IDiagramElement[] Array with Diagram elements
     */
    public IDiagramElement[] getDiagramElementsInArray(IDiagramUIModel diagram) {
        return diagram.toDiagramElementArray();
    }

    /**
     * This is the main logic of the Class SelectDiagramsToProveController.
     * <p>
     * This Method gets called from the GUI with a List of type peixoDiagram.
     * <p>
     * For every IStateDiagramUIModel in the List all directed Paths are found.
     * <p>
     * For every directed Path a String with all Activities and Guards gets built.
     * The Sequence of the Activities and Guards are like the UML Standard.
     * <p>
     * The Method buildSolverLogic gets called for every single Path.
     * If one of the Paths evaluates to UNSATISFIABLE not every Path is reachable.
     *
     * @param diagrams This List of type peixoDiagram
     * @throws Z3Exception               This gets thrown if something in class SelectDiagramsToProveSolver is wrong with the Solver
     * @throws IndexOutOfBoundsException This gets thrown if something in class SelectDiagramsToProveSolver is wrong with the preparation of the Strings
     */
    public void checkReachability(List<peixoDiagram> diagrams) {
        viewManager.clearMessages("peixo");
        SelectDiagramsToProveSolver solverLogic = new SelectDiagramsToProveSolver();

        ArrayList<ArrayList<String>> paths = new ArrayList<>();
//        ArrayList<ArrayList<String>> builtPaths = new ArrayList<>();
        HashMap<String, String> builtPaths = new HashMap<>();



        for (peixoDiagram d : diagrams) {
            if (d.getModelObject() instanceof IStateDiagramUIModel) {
                IDiagramUIModel activeDiagram = d.getModelObject();
                IDiagramElement[] diagramElements = getDiagramElementsInArray(d.getModelObject());
                paths = buildPaths(diagramElements, activeDiagram.getId());
                ArrayList<String> allStates = new ArrayList<>();
                for (ArrayList<String> selectedBuiltPath : paths) {
                    StringBuilder stringbuilder = new StringBuilder();
                    ArrayList<String> helperList = new ArrayList<>();

                    for (String path : selectedBuiltPath) {
                        if (!path.contains("§")) {
                            String firstStateActivities = path.substring(0, path.indexOf("[") - 1).replaceAll("\\(", "");
                            String transitionConstraint = path.substring(path.indexOf("$") + 1, path.lastIndexOf("$"));
                            String lastStateActivities = path.substring(path.lastIndexOf("$") + 1).replaceAll("\\)", "");
                            String[] splitFirstStateActivities = firstStateActivities.trim().split("#");
                            String[] splitLastStateActivities = lastStateActivities.trim().split("#");
                            String[] splitTransitionConstraint = transitionConstraint.trim().split(" ");

                            // Fill String with FirstState Activities
                            for (String s : splitFirstStateActivities) {
                                if (!s.isBlank()) {
                                    stringbuilder.append(s).append(" & ");
                                }
                            }

                            // Fill String with Transition Constraint
                            for (String s : splitTransitionConstraint) {
                                if (!s.isBlank()) {
                                    stringbuilder.append(s.replaceAll("#", "")).append(" & ");
                                } else if (s.isBlank()) {
                                    stringbuilder.append(" ");
                                }
                            }

                        } else {
                            helperList.add(path);
                        }
                    }
                    String lastStateActivities = selectedBuiltPath.get(selectedBuiltPath.size() - 2).
                            substring(selectedBuiltPath.get(selectedBuiltPath.size() - 2).
                                    lastIndexOf("$") + 1).replaceAll("\\)", "");
                    String[] splitLastStateActivities = lastStateActivities.trim().split("#");
                    for (String s : splitLastStateActivities) {
                        if (!s.isBlank()) {
                            stringbuilder.append(s).append(" & ");
                        }
                    }
                    builtPaths.put(stringbuilder.substring(0, stringbuilder.lastIndexOf("&")), helperList.get(0));
//                    helperList.add(stringbuilder.substring(0, stringbuilder.lastIndexOf("&")));
                }
                // ToDo: Aktuell geht alles außer State < State2

//                for (String s : builtPaths) {
                ArrayList<String> reachableStates = new ArrayList<>();
                for (Map.Entry<String, String> s : builtPaths.entrySet()) {
                    try {
//                        if (buildLogic) {
                            Solver solver = solverLogic.buildSolverLogic(s.getKey());
                            switch (solver.check()) {
                                case SATISFIABLE:
                                    String[] reachableStatesFromS = s.getValue().split(" ");
                                    Collections.addAll(reachableStates, reachableStatesFromS);
                                    for (ListIterator<String> i = reachableStates.listIterator(); i.hasNext(); ) {
                                        i.set(i.next().replaceAll("[§,(,)]", ""));
                                    }
                                    reachableStates.removeIf(String::isBlank);
                                    Set<String> set = new HashSet<>(reachableStates.size());
                                    reachableStates.removeIf(p -> !set.add(p));

                                    break;
                                case UNSATISFIABLE:
                                    break;
                                case UNKNOWN:
                                    viewManager.showMessage("Solver status is Unknown: " + Arrays.toString(solver.getAssertions()));
                                    break;
                            }
//                        }
                    } catch (Z3Exception | IndexOutOfBoundsException exception) {
                        viewManager.showMessage("Es ist ein Fehler in der Solverlogik aufgetreten", "peixo");
                        viewManager.showMessage(exception.getMessage());
                    }
                }
                for (IDiagramElement e : diagramElements) {
                    if (e.getModelElement() instanceof IState2) {
                        IState2 state = (IState2) e.getModelElement();
                        allStates.add(state.getId());

                    }
                    if (e.getModelElement() instanceof IChoice) {
                        IChoice choice = (IChoice) e.getModelElement();
                        allStates.add(choice.getId());
                    }
                }
                if (reachableStates.containsAll(allStates)) {
                    viewManager.showMessage("Alle States sind erreichbar", "peixo");
                } else {

                    List<String> unreachableStates = allStates.stream().
                            filter(e -> !reachableStates.contains(e)).collect(Collectors.toList());

                    StringBuilder unreachableStatesToPrint = new StringBuilder();
                    for (IDiagramElement e : diagramElements) {
                        if (e.getModelElement() instanceof IState2) {
                            IState2 state = (IState2) e.getModelElement();
                            for (String s : unreachableStates) {
                                if (state.getId().equals(s)) {
                                    unreachableStatesToPrint.append(state.getName()).append(", ");
                                }
                            }
                        }
                        if (e.getModelElement() instanceof IChoice) {
                            IChoice choice = (IChoice) e.getModelElement();
                            for (String s : unreachableStates) {
                                if (choice.getId().equals(s)) {
                                    unreachableStatesToPrint.append(choice.getName()).append(", ");
                                }
                            }
                        }
                    }
                    if (unreachableStates.size() == 1) {
                        viewManager.showMessage("Der State " + "\"" + unreachableStatesToPrint + "\"" + " ist nicht erreichbar.", "peixo");
                    } else if (unreachableStates.size() > 1)
                        viewManager.showMessage("Die States: " + "\"" + unreachableStatesToPrint + "\"" + " sind nicht erreichbar.", "peixo");
                }
            }
        }

    }

    /**
     * This Method gets called by Method checkReachability().
     * <p>
     * Here the Paths are found and built with the Library JGraphT
     *
     * @param diagramElements all DiagramElements of a specified Diagram
     * @param activeDiagram   the ID of the specified Diagram
     * @return ArrayList<ArrayList>String>> returns a ArrayList of all possible directed paths, the directed Paths are also in a ArrayList
     */
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

            // Find Initial State
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
//        Set<String> verticesWithoutSucc = g.vertexSet().stream().filter(v -> !Graphs.vertexHasSuccessors(g, v)).collect(Collectors.toSet());
        Set<String> allVertices = g.vertexSet();
        // Get all Paths from InitState to all Ending Vertices
        AllDirectedPaths<String, DefaultEdge> allDirectedPaths = new AllDirectedPaths<>(g);
        ArrayList<String> paths = new ArrayList<>();
        for (String endingState : allVertices) {

            try {
                if (!Objects.equals(initState, endingState)) {
                    String path = allDirectedPaths.getAllPaths(initState, endingState, false, 50).toString();
                    if (!path.equals("[]")) {
                        paths.add(path);
                    } else if (path.equals("[]")) {
                        IDiagramElement[] diagramElementArray = pm.getProject().getDiagramById(activeDiagram).toDiagramElementArray();
                        for (IDiagramElement el2 : diagramElementArray) {
                            if (el2.getModelElement() instanceof IState2) {
                                IState2 state = (IState2) el2.getModelElement();
                                if (Objects.equals(state.getId(), endingState)) {
                                    String s = state.getName();
//                                    viewManager.showMessage("Es gibt keine Transition zum Endzustand " + s, "peixo");
                                }
                            }
                        }
//                        buildLogic = false;
                    }
                }
            } catch (IllegalArgumentException exception) {
//                buildLogic = false;

//                if (initState.isBlank()) {
//                    viewManager.showMessage("Es gibt keine Transition zum Startzustand", "peixo");
//                    unsatisSolvers++;
//                }
//                if (endingState.isBlank()) {
//                    viewManager.showMessage("Es gibt keine Transition zum Endzustand", "peixo");
//                    unsatisSolvers++;
//                }
            }

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
                    // Start String build
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
                                // If the Transition does not have a guard
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
            StringBuilder statesPassed = new StringBuilder();
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
                statesPassed.append("§(");
                int counter = 0;
                for (IModelElement e : objects) {
                    if (e instanceof IChoice) {
                        IChoice choice = (IChoice) e;
                        statesPassed.append(choice.getId()).append(" ");
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
                        statesPassed.append(state.getId()).append(" ");
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
                statesPassed.append(")");

                helperlist.add(stringBuilder.toString());
//                helperlist.add(statesPassed.toString());
            }
            helperlist.add(statesPassed.toString());
            allPathsWithTransitionsAndStateActions.add(helperlist);

        }
        return allPathsWithTransitionsAndStateActions;

    }
}


