package services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import util.Configuration;

public class SimplexCore {

    List<BigDecimal> variables;
    List<List<BigDecimal>> constraints;
    List<String> constraintsMark;
    List<BigDecimal> constraintsRightSide = new ArrayList<>();
    boolean maximization;

    public SimplexCore(List<BigDecimal> variables, List<List<BigDecimal>> constraints, boolean maximization, List<String> constraintsMark) {
        this.variables = variables;
        this.constraints = constraints;
        this.maximization = maximization;
        this.constraintsMark = constraintsMark;
        parseRightSidesOfConstraints();
        //writeData();
        normalizeRestrictions();
        writeData();
    }

    private void parseRightSidesOfConstraints() {
        for (int i = 0; i < constraints.size(); i++) {
            int element = constraints.get(i).size() - 1;
            constraintsRightSide.add(constraints.get(i).get(element));
            constraints.get(i).remove(element);
            System.out.println("PRAWA STRONA OGRANICZENIA :" + i + " = " + constraintsRightSide.get(i));
        }
    }


    private void writeData() {
        System.out.println("F : CELU:");
        variables.forEach(a -> System.out.println(a.doubleValue()));
        System.out.println("OGARNICZENIA");
        constraints.forEach(a -> {
            System.out.println("---------");
            a.forEach(
                    b -> System.out.println(b.doubleValue()));
        });
        System.out.println("KREYTERIUM");
        System.out.println(maximization);
        System.out.println("ZNAKI OGRANICZEN");
        constraintsMark.forEach(a -> System.out.println(a));
    }

    /// TO DO jezeli MAX to -MAi jezeli MIN to +MAi
    private void normalizeRestrictions() {
        for (int i = 0; i < constraintsMark.size(); i++) {
            if (constraintsMark.get(i).equals(">=")) {
                for (int j = 0; j < constraints.size(); j++) {
                    if (i == j) {
                        constraints.get(j).add(new BigDecimal("-1.0")); // sub Si
                        constraints.get(j).add(new BigDecimal("1.0")); // add Ai
                        variables.add(new BigDecimal("0.0"));
                        if (maximization)
                            variables.add(new BigDecimal("-" + Configuration.M));
                        else
                            variables.add(new BigDecimal("" + Configuration.M));

                    } else {
                        constraints.get(j).add(new BigDecimal("0.0"));
                        constraints.get(j).add(new BigDecimal("0.0"));
                    }
                }
            } else if (constraintsMark.get(i).equals("<=")) {
                for (int j = 0; j < constraints.size(); j++) {
                    if (i == j) {
                        constraints.get(j).add(new BigDecimal("1.0"));
                        variables.add(new BigDecimal("0.0"));
                    } else
                        constraints.get(j).add(new BigDecimal("0.0"));
                }
            } else {
                for (int j = 0; j < constraints.size(); j++) {
                    if (i == j) {
                        constraints.get(j).add(new BigDecimal("1.0"));
                        if (maximization)
                            variables.add(new BigDecimal("-" + Configuration.M));
                        else
                            variables.add(new BigDecimal("" + Configuration.M));
                    } else
                        constraints.get(j).add(new BigDecimal("0.0"));
                }
            }
        }
    }
}