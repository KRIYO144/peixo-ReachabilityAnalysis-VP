package peixo.solver;


import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;
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
        int index = 0;
        int init = 0;
        String rest = s;
        // Cut the Constraints at "&" and put them in a Arraylist
        if (s.contains("&")) {
            while (true) {
                index = rest.indexOf("&", index);
                if (index != -1) {
                    subs.add(rest.substring(0, index - 1).trim());
                    rest = rest.substring(index + 1);
                    init = index;
                    index = rest.indexOf("&");
                } else {
                    subs.add(rest.trim());
                    break;
                }
            }
        } else {
            subs.add(rest.trim());
        }
        // Get the Array to format
        // The solver gets intiated in other Method
        getConstraintsToSMTLIBFormat(subs, ctx, solver);
        return solver;
    }

    // get Functions
    public String getConstFromSubs(String sub) {
        String clean = sub.replaceAll("[0-9]", "");
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
        return clean;
    }

    public String getOperatorFromSubs(String sub) {
        String clean = sub.replaceAll("[a-z,A-Z,0-9]", "");
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

    public void getConstraintsToSMTLIBFormat(ArrayList<String> subs, Context ctx, Solver solver) {

        // The Subs here are already free of & Operators
        Iterator<String> itor = subs.iterator();
        ArrayList<String> correctedSubs = new ArrayList<String>();

        while (itor.hasNext()) {
            String s = itor.next();
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
            } else if (s.contains("+")) {
                String s2 = declareString + assertString + ")";
                BoolExpr f = ctx.parseSMTLIB2String(s2, null, null, null, null)[0];
                solver.add(f);
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
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder alreadyUsed = new StringBuilder();
        for (int i = 0; i < constNames.length(); i++) {
            if (!Character.isWhitespace(constNames.charAt(i)) && Character.isLetter(constNames.charAt(i))) {
                if (!alreadyUsed.toString().contains(Character.toString(constNames.charAt(i)))) {
                    stringBuilder.append("(declare-const ");
                    stringBuilder.append(constNames.charAt(i));
                    stringBuilder.append(" Int)");
                    alreadyUsed.append(constNames.charAt(i));
                }
            }
        }

        return stringBuilder.toString();
    }
}
