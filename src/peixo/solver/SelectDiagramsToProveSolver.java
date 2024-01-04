package peixo.solver;


import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;

import java.util.*;


public class SelectDiagramsToProveSolver {

    // This is the Main Logic of the Solver
    public Solver buildSolverLogic(String constraint) {
        HashMap<String, String> cfg = new HashMap<>();
        cfg.put("model", "true");
        cfg.put("proof", "true");
        Context ctx = new Context(cfg);
        Solver solver = ctx.mkSolver();
        String s = constraint;
        ArrayList<String> subs = new ArrayList<>();
        ArrayList<String> parsedSubs = new ArrayList<>();
        HashMap<Integer, String> map = new HashMap<Integer, String>();
        HashMap<Integer, String> parsedMap = new HashMap<Integer, String>();
        int index = 0;
        int init = 0;
        String rest = s;
        int nr = 0;
        // Cut the Constraints at "&" and put them in a Arraylist
        if (s.contains("&")) {
            while (true) {
                index = rest.indexOf("&", index);
                if (index != -1) {
                    subs.add(rest.substring(0, index - 1).trim());
                    rest = rest.substring(index + 1);
                    init = index;
                    index = rest.indexOf("&");
                    nr++;
                } else {
                    subs.add(rest.trim());
                    break;
                }
            }
        } else {
            subs.add(rest.trim());
        }
        for (int i = 0; i < subs.size(); i++) {
            map.put(i, subs.get(i));
        }

        // Get the Array to format
        // The solver gets intiated in other Method
        parsedMap = parseArithmetics(map);
        for (Map.Entry<Integer, String> entry : parsedMap.entrySet()) {
            parsedSubs.add(entry.getValue());
        }
        for (String parsed : parsedSubs) {
            if (parsed.contains("#")) {

            }
        }

        buildConstraints(parsedSubs, ctx, solver);
        return solver;
    }

    public HashMap<Integer, String> parseArithmetics(HashMap<Integer, String> map) {
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            int counter = 1;
            int key = entry.getKey();
            String value = entry.getValue();
            String constName = getConstFromSubs(entry.getValue());
            String operators = getOperatorFromSubs(entry.getValue());
            for (Map.Entry<Integer, String> comparedEntry : map.entrySet()) {
                StringBuilder stringBuilder = new StringBuilder();
                int comparedKey = comparedEntry.getKey();
                if (key < comparedEntry.getKey()) {
                    String comparedConstNames = getConstFromSubs(comparedEntry.getValue());
                    if (comparedConstNames.contains(constName)) {
                        if (comparedEntry.getValue().contains("++") | comparedEntry.getValue().contains("--")) {
                            map.put(key, "# " + entry.getValue());
                            if (comparedEntry.getValue().contains(",")) {
                                String[] split = comparedEntry.getValue().split(",");
                                for (int i = 0; i < split.length; i++) {
                                    if (split[i].equals(constName)) {
                                        stringBuilder.append("# ");
                                        stringBuilder.append(split[i]);
                                        for (int z = 0; z < counter; z++) {
                                            stringBuilder.append("~");
                                            counter++;
                                        }
                                    } else if (!split[i].equals(constName)) {
                                        stringBuilder.append(split[i]);
                                    }
                                }
                                map.put(comparedKey, stringBuilder.toString());
                            } else {
                                stringBuilder.append("# ");
                                stringBuilder.append(constName.replaceAll("[\\[,\\]]", ""));
                                for (int z = 0; z < counter; z++) {
                                    stringBuilder.append("~");
                                }
                                stringBuilder.append(operators);

                                map.put(comparedKey, stringBuilder.toString());
                            }
                        } else if (!comparedEntry.getValue().contains("++") & !comparedEntry.getValue().contains("--")) {

                            if (comparedEntry.getValue().contains(",")) {
                                String[] split = comparedEntry.getValue().split(",");
                                for (int i = 0; i < split.length; i++) {
                                    if (split[i].equals(constName)) {
                                        stringBuilder.append(split[i]);
                                        for (int z = 0; z < counter; z++) {
                                            stringBuilder.append("~");
                                            counter++;
                                        }
                                        stringBuilder.append(" ").append(comparedEntry.getValue().substring(comparedEntry.getValue().indexOf(constName) + counter));
                                    } else if (!split[i].equals(constName)) {
                                        stringBuilder.append(split[i]);
                                    }
                                }
                            } else {
                                String cleanConstName = constName.replaceAll("[\\[,\\]]", "");
                                String cleanComparedConstNames = comparedConstNames.replaceAll("[\\[,\\]]", "");
                                stringBuilder.append(comparedEntry.getValue().replaceAll(cleanComparedConstNames, cleanConstName));
                                map.put(comparedKey, stringBuilder.toString());
                            }
                        }
                    } else if (comparedConstNames.replaceAll("~", "").contains(constName.replaceAll("~", ""))) {
                        if (!comparedEntry.getValue().contains("++") && !comparedEntry.getValue().contains("--")) {
                            if (comparedEntry.getValue().contains(",")) {
                                String[] split = comparedEntry.getValue().split(",");
                                for (int i = 0; i < split.length; i++) {
                                    if (split[i].equals(constName)) {
                                        stringBuilder.append(split[i]);
                                        for (int z = 0; z < counter; z++) {
                                            stringBuilder.append("~");
                                            counter++;
                                        }
                                        stringBuilder.append(" ").append(comparedEntry.getValue().substring(comparedEntry.getValue().indexOf(constName) + counter));
                                    } else if (!split[i].equals(constName)) {
                                        stringBuilder.append(split[i]);
                                    }
                                }
                            } else {
                                String cleanConstName = constName.replaceAll("[\\[,\\]]", "");
                                String cleanComparedConstNames = comparedConstNames.replaceAll("[\\[,\\]]", "");

                                stringBuilder.append(comparedEntry.getValue().replaceAll(cleanComparedConstNames, cleanConstName));
                                map.put(comparedKey, stringBuilder.toString());
                            }
                        }
                    }
                }
            }
        }

        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            String value = entry.getValue();
            int key = entry.getKey();
            String constNames = getConstFromSubs(value);

            if (value.contains("#")) {
                for (Map.Entry<Integer, String> comparedEntry : map.entrySet()) {
                    StringBuilder stringBuilder = new StringBuilder();
                    if (key < comparedEntry.getKey()) {
                        String comparedConstnames = getConstFromSubs(comparedEntry.getValue());
                        if (comparedConstnames.contains(constNames)) {
                            String cleanConstName = constNames.replaceAll("[\\[,\\]]", "");
                            String cleanComparedConstNames = comparedConstnames.replaceAll("[\\[,\\]]", "");
                            stringBuilder.append(comparedEntry.getValue().replaceAll(cleanComparedConstNames, cleanConstName + "~"));
                            map.put(comparedEntry.getKey(), stringBuilder.toString());
                        }
                    }
                }
            }

        }


        return map;
    }

    // get Functions
    public String getConstFromSubs(String sub) {
        ArrayList<String> constList = new ArrayList<>();

//        String clean = sub.replaceAll("[0-9]", "");
        String clean = sub;
        if (sub.contains("<")) {
            clean = clean.replaceAll("[<]", " ");
        }
        if (sub.contains(">")) {
            clean = clean.replaceAll("[>]", " ");
        }
        if (sub.contains("=")) {
            clean = clean.replaceAll("[=]", " ");
        }
        if (sub.contains("+")) {
            clean = clean.replaceAll("[+]", " ");
        }
        if (sub.contains("*")) {
            clean = clean.replaceAll("[*]", " ");
        }
        if (sub.contains("#")) {
            clean = clean.replaceAll("[#]", " ");
        }

        String[] cleanArray = clean.split(" ");
        for (String s : cleanArray) {
            if (s.matches(".*[a-zA-Z]+.*")) {
                constList.add(s);
            }
        }
        return constList.toString();
    }

    public String getOperatorFromSubs(String sub) {
        String clean = sub.replaceAll("[a-z,A-Z,0-9]", "");
        clean = clean.replaceAll("~", "");
        clean = clean.replaceAll("#", "");
        clean = clean.replaceAll(" ", "");

        return clean;
    }

    public String getArithFromSubs(String sub) {
        String clean = sub.replaceAll("[a-z,A-Z,0-9]", "");
        clean = clean.replaceAll("~", "");
        clean = clean.replaceAll("#", "");
        clean = clean.replaceAll(" ", "");
        if (clean.length() != 1) {
            if (!Character.isWhitespace(clean.charAt(0))) {
                clean = clean.substring(0, 1);

            }
        }
        return clean;
    }


    public String getAlphaNumericFromSubs(String sub) {
        String clean = sub.replaceAll("[<,>,=,|,+]", " ");
        return clean;
    }

    public ArrayList<String> getORSubs(String sub) {
        ArrayList<String> subs = new ArrayList<>();
        ArrayList<String> changes = new ArrayList<>();
        if (sub.contains("|")) {
            subs.add(sub.substring(0, sub.indexOf("|") - 1).trim());
            subs.add(sub.substring(sub.indexOf("|") + 1).trim());
            Iterator<String> iter = subs.iterator();
            while (iter.hasNext()) {
                String s = iter.next();
                if (s.contains("|")) {
                    changes.add(s.substring(0, s.indexOf("|") - 1).trim());
                    changes.add(s.substring(s.indexOf("|") + 1).trim());
                    iter.remove();
                    System.out.println("Changes : " + changes);
                }
            }
        }
        subs.addAll(changes);

        System.out.println("OR SUBS:" + subs);
        return subs;
    }

    public void buildConstraints(ArrayList<String> subs, Context ctx, Solver solver) {
        // The Subs here are already free of & Operators
        Iterator<String> itor = subs.iterator();
        ArrayList<String> correctedSubs = new ArrayList<String>();
        while (itor.hasNext()) {
            String s = itor.next();
            // If Subs are marked as Arithmetic operations they are parsed in other method
            if (s.charAt(0) == '#') {
                String constNames = getConstFromSubs(s);
                String SMTLIBString = buildArithmetic(constNames, s);
                BoolExpr f = ctx.parseSMTLIB2String(SMTLIBString, null, null, null, null)[0];
                solver.add(f);
                // Todo: Hier soll die methode aufgerufen werden wegen der arith. subs
            } else if (s.charAt(0) != '#') {

                String constName = getConstFromSubs(s);
                String declareString = buildDeclareString(constName);
                String assertString = buildAssertString(constName, s);


                if (s.contains("<")) {
                    String s2 = declareString + assertString + ")";
                    BoolExpr f = ctx.parseSMTLIB2String(s2, null, null, null, null)[0];
                    solver.add(f);
                } else if (s.contains(">")) {
                    String s2 = declareString + assertString + ")";
                    BoolExpr f = ctx.parseSMTLIB2String(s2, null, null, null, null)[0];
                    solver.add(f);
                } else if (s.contains("=")) {
                    String s2 = declareString + assertString + ")";
                    BoolExpr f = ctx.parseSMTLIB2String(s2, null, null, null, null)[0];
                    solver.add(f);
                }
//            } else if (s.contains("+")) {
//                String s2 = declareString + assertString + ")";
//                BoolExpr f = ctx.parseSMTLIB2String(s2, null, null, null, null)[0];
//                solver.add(f);
//            }
            }
        }
    }

    // build Assert Strings
    public String buildAssertString(String constNames, String sub) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(assert ");
        if (constNames.contains("|")) {
            stringBuilder.append("(or");
            ArrayList<String> orSubs = getORSubs(sub);

            for (String s : orSubs) {
                stringBuilder.append("( ");
                String operators = getOperatorFromSubs(s);
                String constNames1 = getConstFromSubs(s);
                String alphaNumeric = getAlphaNumericFromSubs(s);
                stringBuilder.append(operators.charAt(0)).append(" ");
                for (int i = 0; i < alphaNumeric.length(); i++) {
                    if (!Character.isWhitespace(alphaNumeric.charAt(i))) {
                        stringBuilder.append(alphaNumeric.charAt(i)).append(" ");
                    }
                }
                stringBuilder.append(" )");
            }
            stringBuilder.append(")");
        } else if (sub.contains("<")) {
            stringBuilder.append("(< ")
                    .append(sub.substring(0, sub.indexOf("<")).trim()).append(" ")
                    .append(sub.substring(sub.indexOf("<") + 1).trim()).append(")");
        } else if (sub.contains(">")) {
            stringBuilder.append("(> ")
                    .append(sub.substring(0, sub.indexOf(">")).trim()).append(" ")
                    .append(sub.substring(sub.indexOf(">") + 1).trim()).append(")");
        } else if (sub.contains("=")) {
            stringBuilder.append("(= ")
                    .append(sub.substring(0, sub.indexOf("=")).trim()).append(" ")
                    .append(sub.substring(sub.indexOf("=") + 1).trim()).append(")");
        } else if (sub.contains("+")) {
            stringBuilder.append("(+ ")
                    .append(sub.substring(0, sub.indexOf("+")).trim()).append(" ")
                    .append(sub.substring(sub.indexOf("+") + 1).trim()).append(")");
        }

        return stringBuilder.toString();
    }

    // build Declare Strings
    public String buildDeclareString(String constNames) {
        String clean = constNames.replaceAll("\\[", "");
        clean = clean.replaceAll("]", "");
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder alreadyUsed = new StringBuilder();
        String[] constList = clean.split(",");
        for (String constName : constList) {
            if (!constName.isBlank()) {
                if (!alreadyUsed.toString().contains(constName)) {
                    stringBuilder.append("(declare-const ");
                    stringBuilder.append(constName);
                    stringBuilder.append(" Int)");
                    alreadyUsed.append(constName);
                }
            }
        }
//            if (!Character.isWhitespace(constNames.charAt(i)) && Character.isLetter(constNames.charAt(i))) {

        return stringBuilder.toString();
    }

    public String buildArithmetic(String constNames, String parsedSub) {
        String clean = constNames.replaceAll("#", "").replaceAll("[\\[,\\]]", "");
        String operator = getArithFromSubs(parsedSub);
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder alreadyUsed = new StringBuilder();

        String[] constList = clean.split(",");
        for (String constName : constList) {
            if (!constName.isBlank()) {
                if (!alreadyUsed.toString().contains(constName)) {
                    stringBuilder.append("(declare-const ");
                    stringBuilder.append(constName);
                    stringBuilder.append(" Int) ");
                    stringBuilder.append("(declare-const ");
                    stringBuilder.append(constName).append("~");
                    stringBuilder.append(" Int) ");
                    alreadyUsed.append(constName);
                    alreadyUsed.append(constName).append("~");
                }
            }

            stringBuilder.append("(assert (= ");
            stringBuilder.append(constName).append("~");
            stringBuilder.append("( ");
            stringBuilder.append(operator).append(" ");
            stringBuilder.append(constName);
            stringBuilder.append(" 1");
            stringBuilder.append(")))");
            //Todo: Mark Strings of Arith with #
            //  hier sollen die fertigen strings für SMTLIB() gebaut werden
            //  debug die map und gucke of diese richtig erstellt wird.
        }
        return stringBuilder.toString();
    }
}
