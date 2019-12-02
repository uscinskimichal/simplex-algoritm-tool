package services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import util.Configuration;

public class SimplexCore {

    private final int numberOfConstraints;
    List<BigDecimal> listOfVariables;
    List<List<BigDecimal>> listOfConstraints;
    List<String> listOfConstraintsMark;
    List<BigDecimal> listRightSideOfConstraints = new ArrayList<>();
    List<Integer> listOfBasisIndexes = new ArrayList<>();
    List<BigDecimal> listOfCoefficientsOfVariables;
    List<BigDecimal> listOfDifferenceVariablesAndCoefficients;
    List<BigDecimal> listOfBasicConstraintsDividedByXi;
    boolean maximization;

    public SimplexCore(List<BigDecimal> listOfVariables, List<List<BigDecimal>> listOfConstraints, boolean maximization, List<String> listOfConstraintsMark) {
        this.listOfVariables = listOfVariables;
        this.listOfConstraints = listOfConstraints;
        this.maximization = maximization;
        this.listOfConstraintsMark = listOfConstraintsMark;
        this.numberOfConstraints = listOfConstraints.size();
        extractRightSidesOfConstraints();
        normalize();

        listOfBasicConstraintsDividedByXi = new ArrayList<BigDecimal>(Collections.nCopies(numberOfConstraints, new BigDecimal("0.0")));
        listOfCoefficientsOfVariables = new ArrayList<BigDecimal>(Collections.nCopies(this.listOfVariables.size(), new BigDecimal("0.0")));
        listOfDifferenceVariablesAndCoefficients = new ArrayList<BigDecimal>(Collections.nCopies(this.listOfVariables.size(), new BigDecimal("0.0")));

        //writeData();
        printBasisIndexes();

        calculateCoeficcients();
        calculateVariablesAndCoeffDifference();

        calculateDecisionVector();
    }

    private void extractRightSidesOfConstraints() {
        for (int i = 0; i < listOfConstraints.size(); i++) {
            int element = listOfConstraints.get(i).size() - 1;
            listRightSideOfConstraints.add(listOfConstraints.get(i).get(element));
            listOfConstraints.get(i).remove(element);
            System.out.println("PRAWA STRONA OGRANICZENIA :" + i + " = " + listRightSideOfConstraints.get(i));
        }
    }


    private void writeData() {
        System.out.println("F : CELU:");
        listOfVariables.forEach(a -> System.out.println(a.doubleValue()));
        System.out.println("OGARNICZENIA");
        listOfConstraints.forEach(a -> {
            System.out.println("---------");
            a.forEach(
                    b -> System.out.println(b.doubleValue()));
        });
        System.out.println("KREYTERIUM");
        System.out.println(maximization);
        System.out.println("ZNAKI OGRANICZEN");
        listOfConstraintsMark.forEach(a -> System.out.println(a));
    }

    /// TO DO jezeli MAX to -MAi jezeli MIN to +MAi
    private void normalize() {
        for (int i = 0; i < listOfConstraintsMark.size(); i++) {
            if (listOfConstraintsMark.get(i).equals(">=")) {
                for (int j = 0; j < listOfConstraints.size(); j++) {
                    if (i == j) {
                        listOfConstraints.get(j).add(new BigDecimal("-1.0")); // sub Si
                        listOfConstraints.get(j).add(new BigDecimal("1.0")); // add Ai
                        listOfBasisIndexes.add(listOfConstraints.get(j).size() - 1);
                        listOfVariables.add(new BigDecimal("0.0"));
                        if (maximization)
                            listOfVariables.add(new BigDecimal("-" + Configuration.M));
                        else
                            listOfVariables.add(new BigDecimal("" + Configuration.M));

                    } else {
                        listOfConstraints.get(j).add(new BigDecimal("0.0"));
                        listOfConstraints.get(j).add(new BigDecimal("0.0"));
                    }
                }
            } else if (listOfConstraintsMark.get(i).equals("<=")) {
                for (int j = 0; j < listOfConstraints.size(); j++) {
                    if (i == j) {
                        listOfConstraints.get(j).add(new BigDecimal("1.0"));
                        listOfBasisIndexes.add(listOfConstraints.get(j).size() - 1);
                        listOfVariables.add(new BigDecimal("0.0"));
                    } else
                        listOfConstraints.get(j).add(new BigDecimal("0.0"));
                }
            } else {
                for (int j = 0; j < listOfConstraints.size(); j++) {
                    if (i == j) {
                        listOfConstraints.get(j).add(new BigDecimal("1.0"));
                        listOfBasisIndexes.add(listOfConstraints.get(j).size() - 1);
                        if (maximization)
                            listOfVariables.add(new BigDecimal("-" + Configuration.M));
                        else
                            listOfVariables.add(new BigDecimal("" + Configuration.M));
                    } else
                        listOfConstraints.get(j).add(new BigDecimal("0.0"));
                }
            }
        }
    }

    private void printBasisIndexes() {
        System.out.println("INDEKSY BAZY:");
        listOfBasisIndexes.forEach(a -> System.out.println(a.doubleValue()));
    }

    private void calculateCoeficcients() {
        System.out.println("Cj:");

        for (int i = 0; i < listOfVariables.size(); i++) {
            for (int j = 0; j < numberOfConstraints; j++) {

                listOfCoefficientsOfVariables.set(i,
                        listOfCoefficientsOfVariables.get(i)
                                .add(listOfConstraints.get(j).get(i)
                                        .multiply(listOfVariables.get(listOfBasisIndexes.get(j)))));

            }
            System.out.println(i + " " + listOfCoefficientsOfVariables.get(i));
        }
    }

    private void calculateVariablesAndCoeffDifference() {
        System.out.println(" Zj - Cj");
        for (int i = 0; i < listOfVariables.size(); i++) {
            listOfDifferenceVariablesAndCoefficients.set(i, listOfVariables.get(i).subtract(listOfCoefficientsOfVariables.get(i)));

            System.out.println(i + "  " + listOfDifferenceVariablesAndCoefficients.get(i));
        }
    }

    private int getPivotElementMAX() {
        System.out.println("MAX ELEMENT : " + Collections.max(listOfDifferenceVariablesAndCoefficients));
        return listOfDifferenceVariablesAndCoefficients.indexOf(Collections.max(listOfDifferenceVariablesAndCoefficients));
    }

    private int getPivotElementMIN() {
        System.out.println("MIN ELEMENT : " + Collections.min(listOfDifferenceVariablesAndCoefficients));
        return listOfDifferenceVariablesAndCoefficients.indexOf(Collections.min(listOfDifferenceVariablesAndCoefficients));
    }

    private void calculateDecisionVector() {
        int index;
        if (maximization)
            index = getPivotElementMAX();
        else
            index = getPivotElementMIN();

        int i = 0;
        try {
            for (i = 0; i < numberOfConstraints; i++) {
                listOfBasicConstraintsDividedByXi
                        .set(i, listRightSideOfConstraints.get(i)
                                .divide(listOfConstraints.get(i).get(index), 4, RoundingMode.HALF_UP));
            }
        } catch (ArithmeticException ae) {
            listOfBasicConstraintsDividedByXi.set(i, Configuration.M);
        }
        listOfBasicConstraintsDividedByXi.forEach(a -> System.out.println("DECISION VECTOR : " + a));
    }
}