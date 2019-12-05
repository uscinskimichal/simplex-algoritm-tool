package services;

import util.Configuration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

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
    BigDecimal decisionElement;

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
        writeData();
        printBasisIndexes();
        try {
            while (true) {
                calculateCoeficcients();
                calculateVariablesAndCoeffDifference();
                if (checkStop())
                    break;
                calculateDecisionVector();
                calculateRightSideOfConstraints(getIndexVariableEnteringBasis(), decisionElement);
                setNewTable();
                writeData();
                prepareStep();
                System.out.println("----------------------------------------");
            }
            printSolution();
        } catch (NoSuchElementException nse) {
            System.out.println("ZADANIE NIEOGRANICZONE");
        }

    }

    private void extractRightSidesOfConstraints() {
        for (int i = 0; i < listOfConstraints.size(); i++) {
            int element = listOfConstraints.get(i).size() - 1;
            listRightSideOfConstraints.add(listOfConstraints.get(i).get(element));
            listOfConstraints.get(i).remove(element);
            System.out.println("PRAWA STRONA OGRANICZENIA :" + i + " = " + listRightSideOfConstraints.get(i));
        }
    }

    private void calculateRightSideOfConstraints(int enteringIndex, BigDecimal value) {
        boolean permit = true;
        for (int i = 0; i < listRightSideOfConstraints.size(); i++) {
            try {
                BigDecimal var = listRightSideOfConstraints.get(i).divide(listOfConstraints.get(i).get(enteringIndex), 4, RoundingMode.HALF_UP);
                if (permit && var.compareTo(value) == 0) {
                    permit = false;
                    listRightSideOfConstraints.set(i, var);
                } else
                    listRightSideOfConstraints.set(i, listRightSideOfConstraints.get(i)
                            .subtract((listOfConstraints.get(i).get(enteringIndex).multiply(value))));
            } catch (ArithmeticException ae) {
                System.out.println("LOG");
            }

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

    private void prepareStep() {
        listOfBasicConstraintsDividedByXi = new ArrayList<BigDecimal>(Collections.nCopies(numberOfConstraints, new BigDecimal("0.0")));
        listOfDifferenceVariablesAndCoefficients = new ArrayList<BigDecimal>(Collections.nCopies(this.listOfVariables.size(), new BigDecimal("0.0")));
        listOfCoefficientsOfVariables = new ArrayList<BigDecimal>(Collections.nCopies(this.listOfVariables.size(), new BigDecimal("0.0")));
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

    private int getPivotIndex() {
        if (maximization)
            return getPivotElementMAX();
        else
            return getPivotElementMIN();
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
        listRightSideOfConstraints.forEach(a -> System.out.println(" WEKTOR B : " + a.doubleValue()));
        int index;
        if (maximization)
            index = getPivotElementMAX();
        else
            index = getPivotElementMIN();

        int i;
        for (i = 0; i < numberOfConstraints; i++) {
            try {
                if (listOfConstraints.get(i).get(index).doubleValue() < 0)
                    listOfBasicConstraintsDividedByXi
                            .set(i, null);

                listOfBasicConstraintsDividedByXi
                        .set(i, listRightSideOfConstraints.get(i)
                                .divide(listOfConstraints.get(i).get(index), 4, RoundingMode.HALF_UP));


                System.out.println("DECISION ELEMENT : " + decisionElement);
            } catch (ArithmeticException ae) {
                System.out.println("ADSDASDSADASD SAD AS DSA DSA DSA");
                listOfBasicConstraintsDividedByXi.set(i, Configuration.M);
            }

        }

        this.decisionElement = Collections.min(listOfBasicConstraintsDividedByXi
                .stream()
                .filter(a -> a.doubleValue() >= 0)
                .collect(Collectors.toList()));

        listOfBasicConstraintsDividedByXi.forEach(a -> System.out.println("DECISION VECTOR : " + a));
        listRightSideOfConstraints.forEach(a -> System.out.println(" WEKTOR B : " + a.doubleValue()));
    }


    private int getIndexVariableLeavingBasis() {
        System.out.println("PEEEEPOSSS : " + listOfBasicConstraintsDividedByXi);
        System.out.println("PEEEEPO : " + listOfBasicConstraintsDividedByXi
                .indexOf(Collections.min(listOfBasicConstraintsDividedByXi
                        .stream()
                        .filter(a -> a.doubleValue() >= 0)
                        .collect(Collectors.toList()))));
        return listOfBasicConstraintsDividedByXi
                .indexOf(Collections.min(listOfBasicConstraintsDividedByXi
                        .stream()
                        .filter(a -> a.doubleValue() >= 0)
                        .collect(Collectors.toList())));
        //return listOfBasicConstraintsDividedByXi.indexOf(Collections.min(listOfBasicConstraintsDividedByXi));
    }

    private int getIndexVariableEnteringBasis() {
        if (maximization)
            return listOfDifferenceVariablesAndCoefficients.indexOf(Collections.max(listOfDifferenceVariablesAndCoefficients)); //MAX
        else
            return listOfDifferenceVariablesAndCoefficients.indexOf(Collections.min(listOfDifferenceVariablesAndCoefficients)); //MIN

    }

    private List<List<BigDecimal>> makeADeepCopyOfVariables(List<List<BigDecimal>> in) {
        List<List<BigDecimal>> listOfConstraintsClone = new ArrayList<>();
        for (int i = 0; i < listOfConstraints.size(); i++) {
            listOfConstraintsClone.add(new ArrayList<BigDecimal>());
            for (int j = 0; j < listOfConstraints.get(i).size(); j++)
                listOfConstraintsClone.get(i).add(listOfConstraints.get(i).get(j));
        }
        return listOfConstraintsClone;
    }

    private void setNewTable() {
        List<List<BigDecimal>> listOfConstraintsClone = makeADeepCopyOfVariables(listOfConstraints);
        int pivotIndex = getPivotIndex();
        System.out.println("dsadsadsadsad : " + pivotIndex);
        int indexLeaving = getIndexVariableLeavingBasis();
        System.out.println("INDEX LEAV : " + indexLeaving);
        int indexEntering = getIndexVariableEnteringBasis();
        System.out.println("INDEX INTER : " + indexEntering);
        if (listOfConstraints.get(indexLeaving).get(pivotIndex).doubleValue() < 0)
            throw new NoSuchElementException();
        BigDecimal pivotValue = listOfConstraints.get(indexLeaving).get(pivotIndex);
        listOfBasisIndexes.set(indexLeaving, indexEntering);

        for (int i = 0; i < listOfVariables.size(); i++) {
            for (int j = 0; j < numberOfConstraints; j++) {
                if (indexLeaving == j) {
                    listOfConstraints.get(j)
                            .set(i, listOfConstraints.get(j).get(i).divide(pivotValue, 4, RoundingMode.HALF_UP));
                    continue;
                } else if (indexEntering == i) {
                    listOfConstraints.get(j).set(i, new BigDecimal("0.0"));
                    continue;

                } else if (indexEntering != i && j != indexLeaving) {

                    System.out.println("Dzialanie :" + listOfConstraints.get(j).get(i) + " - ("
                            + listOfConstraintsClone.get(indexLeaving).get(i) + " * " + listOfConstraintsClone.get(j).get(indexEntering)
                            + ") / " + pivotValue.doubleValue());


                    listOfConstraints.get(j)
                            .set(i, listOfConstraints.get(j).get(i)
                                    .subtract(
                                            (listOfConstraintsClone.get(indexLeaving).get(i).multiply(listOfConstraintsClone.get(j).get(indexEntering)))
                                                    .divide(pivotValue, 4, RoundingMode.HALF_UP))
                            );
                }
            }
        }
    }

    private boolean checkStop() {
        if (maximization) {
            if (listOfDifferenceVariablesAndCoefficients.stream().allMatch(a -> a.doubleValue() <= 0))
                return true;
            else
                return false;

        } else {
            if (listOfDifferenceVariablesAndCoefficients.stream().allMatch(a -> a.doubleValue() >= 0))
                return true;
            else
                return false;
        }
    }

    private boolean checkIfProblemIsUnsolveable(BigDecimal optimalValue) {
        if (optimalValue.doubleValue() >= 0)
            return false;
        else
            return true;
    }

    private void printSolution() {
        System.out.println("VECTOR SOLUTION IS : " + listOfBasisIndexes);
        List<BigDecimal> resultVectorList = new ArrayList<>(Collections.nCopies(listOfVariables.size(), new BigDecimal("0.0")));
        for (int i = 0; i < listOfBasisIndexes.size(); i++) {
            resultVectorList.set(listOfBasisIndexes.get(i),
                    listRightSideOfConstraints.get(i));
        }

        System.out.println(resultVectorList);
        BigDecimal optimalValue = new BigDecimal("0.0");
        for (int i = 0; i < listOfVariables.size(); i++)
            optimalValue = optimalValue.add((resultVectorList.get(i).multiply(listOfVariables.get(i))));

        if (!checkIfProblemIsUnsolveable(optimalValue)) {
            System.out.println("FUNCTION VALUE IS : " + optimalValue);
        } else {
            System.out.println("ZADANIE SPRZECZNE");
        }
    }
}

