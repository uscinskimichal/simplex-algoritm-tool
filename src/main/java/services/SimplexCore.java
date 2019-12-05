package services;

import exceptions.InfitnitySolutions;
import util.Configuration;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class SimplexCore {

    private final int numberOfConstraints;
    private int iterationNumber = 0;
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

//        try {
//            PrintStream out = new PrintStream(new FileOutputStream("SIMPLEX_LOG_" + getDateAndTime() + ".txt"));
//            System.setOut(out);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

        extractRightSidesOfConstraints();
        fixNegativeConstraint();
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
                calculateDecisionVector();
                calculateRightSideOfConstraints(getIndexVariableEnteringBasis(), decisionElement);
                if (performStopAction())
                    break;
                setNewTable();
                writeData();
                prepareStep();
                System.out.println("----------------------------------------");
            }
            printSolution();
        } catch (InfitnitySolutions nse) {
            System.out.println("Zadanie nieograniczone - nie jest możliwe ustalenie rozwiązania optymalnego.");
        }

    }

    private void extractRightSidesOfConstraints() {
        for (int i = 0; i < listOfConstraints.size(); i++) {
            int element = listOfConstraints.get(i).size() - 1;
            listRightSideOfConstraints.add(listOfConstraints.get(i).get(element));
            listOfConstraints.get(i).remove(element);
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

            }

        }
    }

    private void writeData() {
        String criteria;
        if (maximization)
            criteria = "Max";
        else
            criteria = "Min";
        System.out.println("-------------------------\nFunkcja celu : ");
        System.out.println(criteria + " -> " + listOfVariables);
        System.out.println("\nOgraniczenia : ");
        for (int i = 0; i < listOfConstraints.size(); i++)
            System.out.println(listOfConstraints.get(i) + " " + listOfConstraintsMark.get(i) + " " + listRightSideOfConstraints.get(i));
    }

    private void fixNegativeConstraint() {
        for (int i = 0; i < listRightSideOfConstraints.size(); i++) {
            if (listRightSideOfConstraints.get(i).doubleValue() < 0) {
                listRightSideOfConstraints.set(i, listRightSideOfConstraints.get(i).negate());
                for (int j = 0; j < listOfConstraints.get(i).size(); j++)
                    listOfConstraints.get(i).set(j, listOfConstraints.get(i).get(j).negate());

                if (listOfConstraintsMark.get(i).equals(">="))
                    listOfConstraintsMark.set(i, "<=");
                else if (listOfConstraintsMark.get(i).equals("<="))
                    listOfConstraintsMark.set(i, ">=");
            }
        }
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
        System.out.println("\nIndeksy bazy:");
        System.out.println(listOfBasisIndexes);
    }

    private void prepareStep() {
        listOfBasicConstraintsDividedByXi = new ArrayList<BigDecimal>(Collections.nCopies(numberOfConstraints, new BigDecimal("0.0")));
        listOfDifferenceVariablesAndCoefficients = new ArrayList<BigDecimal>(Collections.nCopies(this.listOfVariables.size(), new BigDecimal("0.0")));
        listOfCoefficientsOfVariables = new ArrayList<BigDecimal>(Collections.nCopies(this.listOfVariables.size(), new BigDecimal("0.0")));
    }

    private void calculateCoeficcients() {
        System.out.println("\nZj:");

        for (int i = 0; i < listOfVariables.size(); i++) {
            for (int j = 0; j < numberOfConstraints; j++) {

                listOfCoefficientsOfVariables.set(i,
                        listOfCoefficientsOfVariables.get(i)
                                .add(listOfConstraints.get(j).get(i)
                                        .multiply(listOfVariables.get(listOfBasisIndexes.get(j)))));

            }
        }
        System.out.println(listOfCoefficientsOfVariables);
    }

    private void calculateVariablesAndCoeffDifference() {
        System.out.println("\nCj - Zj");
        for (int i = 0; i < listOfVariables.size(); i++)
            listOfDifferenceVariablesAndCoefficients.set(i, listOfVariables.get(i).subtract(listOfCoefficientsOfVariables.get(i)));
        System.out.println(listOfDifferenceVariablesAndCoefficients);
    }

    private int getPivotIndex() {
        if (maximization)
            return getPivotElementMAX();
        else
            return getPivotElementMIN();
    }

    private int getPivotElementMAX() {
        return listOfDifferenceVariablesAndCoefficients.indexOf(Collections.max(listOfDifferenceVariablesAndCoefficients));
    }

    private int getPivotElementMIN() {
        System.out.println("Element minimalny z Zj : " + Collections.min(listOfDifferenceVariablesAndCoefficients));
        return listOfDifferenceVariablesAndCoefficients.indexOf(Collections.min(listOfDifferenceVariablesAndCoefficients));
    }

    private void calculateDecisionVector() {
        int index;
        if (maximization) {
            index = getPivotElementMAX();
            System.out.println("\nElement maksymalny z Cj - Zj : " + Collections.max(listOfDifferenceVariablesAndCoefficients));
        } else {
            index = getPivotElementMIN();
            System.out.println("\nElement minimalny z Cj - Zj : " + Collections.min(listOfDifferenceVariablesAndCoefficients));
        }


        int i;
        for (i = 0; i < numberOfConstraints; i++) {
            try {
                if (listOfConstraints.get(i).get(index).doubleValue() < 0)
                    listOfBasicConstraintsDividedByXi
                            .set(i, null);

                listOfBasicConstraintsDividedByXi
                        .set(i, listRightSideOfConstraints.get(i)
                                .divide(listOfConstraints.get(i).get(index), 4, RoundingMode.HALF_UP));


            } catch (ArithmeticException ae) {
                listOfBasicConstraintsDividedByXi.set(i, Configuration.M);
            }

        }


        try {
            this.decisionElement = Collections.min(listOfBasicConstraintsDividedByXi
                    .stream()
                    .filter(a -> a.doubleValue() >= 0)
                    .collect(Collectors.toList()));
        } catch (NoSuchElementException is) {
            throw new InfitnitySolutions();
        }
        System.out.println("\nWektor wartości ograniczeń/Xi : " + listOfBasicConstraintsDividedByXi);
        System.out.println("Element decyzyjny : " + decisionElement);
    }


    private int getIndexVariableLeavingBasis() {
        // System.out.println("WWWWWWWWWWWWWW : " + listOfBasicConstraintsDividedByXi);
        //  System.out.println("WWWWWWWWWW: " + listOfBasicConstraintsDividedByXi
        //        .indexOf(Collections.min(listOfBasicConstraintsDividedByXi
        //                .stream()
        //                .filter(a -> a.doubleValue() >= 0)
        //                .collect(Collectors.toList()))));
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
        //System.out.println("dsadsadsadsad : " + pivotIndex);
        int indexLeaving = getIndexVariableLeavingBasis();
        System.out.println("\nIndeks wychodzący z bazy : " + indexLeaving);
        int indexEntering = getIndexVariableEnteringBasis();
        System.out.println("Indeks wchodzący do bazy : " + indexEntering);
        System.out.println(" ");
        if (listOfConstraints.get(indexLeaving).get(pivotIndex).doubleValue() < 0)
            throw new InfitnitySolutions();
        BigDecimal pivotValue = listOfConstraints.get(indexLeaving).get(pivotIndex);
        listOfBasisIndexes.set(indexLeaving, indexEntering);

        try {
            for (int i = 0; i < listOfVariables.size(); i++) {
                for (int j = 0; j < numberOfConstraints; j++) {
                    if (indexLeaving == j) {
                        BigDecimal pivotResult = new BigDecimal("" + listOfConstraints.get(j).get(i).divide(pivotValue, 4, RoundingMode.HALF_UP));
                        System.out.println("Przygotowanie tabeli simplex [" + i + "][" + j + "] : " + pivotResult);
                        listOfConstraints.get(j).set(i, pivotResult);
                        continue;
                    } else if (indexEntering == i) {
                        BigDecimal pivotResult = new BigDecimal("0.0");
                        System.out.println("Przygotowanie tabeli simplex [" + i + "][" + j + "] : " + pivotResult);
                        listOfConstraints.get(j).set(i, pivotResult);
                        continue;

                    } else if (indexEntering != i && j != indexLeaving) {

                        BigDecimal pivotResult = new BigDecimal("" + listOfConstraints.get(j).get(i)
                                .subtract(
                                        (listOfConstraintsClone.get(indexLeaving).get(i).multiply(listOfConstraintsClone.get(j).get(indexEntering)))
                                                .divide(pivotValue, 4, RoundingMode.HALF_UP)));


                        System.out.println("Przygotowanie tabeli simplex [" + i + "][" + j + "] : " + listOfConstraints.get(j).get(i) + " - ("
                                + listOfConstraintsClone.get(indexLeaving).get(i) + " * " + listOfConstraintsClone.get(j).get(indexEntering)
                                + ") / " + pivotValue.doubleValue() + " = " + pivotResult);


                        listOfConstraints.get(j).set(i, pivotResult);
                    }
                }
            }
        } catch (ArithmeticException ae) {
            throw new InfitnitySolutions();
        }
    }

    private boolean checkStop() {
        if (maximization) {
            return listOfDifferenceVariablesAndCoefficients.stream().allMatch(a -> a.doubleValue() <= 0);
        } else {
            return listOfDifferenceVariablesAndCoefficients.stream().allMatch(a -> a.doubleValue() >= 0);
        }
    }

    private boolean checkIfMIsInSolution(List<BigDecimal> resultVectorList) {
        for (int i = 0; i < listOfBasisIndexes.size(); i++) {
            System.out.println("Wartość X(" + (listOfBasisIndexes.get(i) + 1) + ") wynosi : " + listOfVariables.get(listOfBasisIndexes.get(i))
                    .multiply(resultVectorList.get(listOfBasisIndexes.get(i))).abs());
            if (resultVectorList.get(listOfBasisIndexes.get(i)).doubleValue() != 0 &&
                    listOfVariables.get(listOfBasisIndexes.get(i)).abs().compareTo(Configuration.M) == 0)
                return true;
        }
        return false;
    }

//    private boolean checkIfProblemIsUnsolveable(BigDecimal optimalValue) {
//        if (optimalValue.doubleValue() >= 0) {
//            return false;
//        } else
//            return true;
//    }

    private void printSolution() {
        List<BigDecimal> resultVectorList = new ArrayList<>(Collections.nCopies(listOfVariables.size(), new BigDecimal("0.0")));
        for (int i = 0; i < listOfBasisIndexes.size(); i++) {
            resultVectorList.set(listOfBasisIndexes.get(i),
                    listRightSideOfConstraints.get(i));
        }
        BigDecimal optimalValue = new BigDecimal("0.0");
        for (int i = 0; i < listOfVariables.size(); i++)
            optimalValue = optimalValue.add((resultVectorList.get(i).multiply(listOfVariables.get(i))));

        if (!checkIfMIsInSolution(resultVectorList)) {
            System.out.println("\nWektor bazowy rozwiązania : " + listOfBasisIndexes);
            System.out.println("Wektor rozwiązania zadania optymalnego : " + resultVectorList);
            System.out.println("Wartość funkcji : " + optimalValue); // TO DO new OptimalSolution
        } else {
            System.out.println("\nUkład ograniczeń jest sprzeczny, zadanie nie posiada rozwiązania!"); // TO DO new UnsolveableProblem
        }
    }

    private boolean performStopAction() {
        if (checkStop()) {
            System.out.println("\nWarunek stop został spełniony!\n\n");
            return true;
        } else {
            System.out.println("\nWarunek stop nie został osiągnięty - kontynuuję obliczenia.\n");
            System.out.println("----------------------------------------");
            System.out.println("\t----ITERACJA NUMER " + ++iterationNumber + " ----");
            System.out.println("----------------------------------------");
            return false;
        }
    }


    private String getDateAndTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd HH_mm_ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println(dtf.format(now));
        return dtf.format(now);
    }
}

