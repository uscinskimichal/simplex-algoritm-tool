package services;

import controllers.ResultWindowController;
import exceptions.InfitnitySolutions;
import util.Configuration;
import util.Navigate;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class SimplexCore extends Navigate {

    MathContext mathContext = new MathContext(4, RoundingMode.HALF_EVEN);
    private final int numberOfConstraints;
    private int iterationNumber = 0;
    private List<BigDecimal> listOfVariables;
    private List<List<BigDecimal>> listOfConstraints;
    private List<String> listOfConstraintsMark;
    private List<BigDecimal> listRightSideOfConstraints = new ArrayList<>();
    private List<Integer> listOfBasisIndexes = new ArrayList<>();
    private List<BigDecimal> listOfCoefficientsOfVariables;
    private List<BigDecimal> listOfDifferenceVariablesAndCoefficients;
    private List<BigDecimal> listOfBasicConstraintsDividedByXi;
    private List<BigDecimal> resultVectorList ;
    private BigDecimal optimalValue;
    boolean maximization;
    private BigDecimal decisionElement;

    public SimplexCore(List<BigDecimal> listOfVariables, List<List<BigDecimal>> listOfConstraints, boolean maximization, List<String> listOfConstraintsMark) {
        this.listOfVariables = listOfVariables;
        this.listOfConstraints = listOfConstraints;
        this.maximization = maximization;
        this.listOfConstraintsMark = listOfConstraintsMark;
        this.numberOfConstraints = listOfConstraints.size();
    }

    public void solve() {
//        PrintStream stdout = System.out;
//        try {
//            PrintStream out = new PrintStream(new FileOutputStream("SIMPLEX_LOG_" + getDateAndTime() + ".txt"));
//            System.setOut(out);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

        extractRightSidesOfConstraints();
        fixNegativeConstraint();
        normalize();

        resultVectorList = new ArrayList<>(Collections.nCopies(listOfVariables.size(), new BigDecimal("0.0")));
        listOfBasicConstraintsDividedByXi = new ArrayList<>(Collections.nCopies(numberOfConstraints, new BigDecimal("0.0")));
        listOfCoefficientsOfVariables = new ArrayList<>(Collections.nCopies(this.listOfVariables.size(), new BigDecimal("0.0")));
        listOfDifferenceVariablesAndCoefficients = new ArrayList<>(Collections.nCopies(this.listOfVariables.size(), new BigDecimal("0.0")));
        writeData();
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
//            System.setOut(stdout);
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
                BigDecimal var = listRightSideOfConstraints.get(i).divide(listOfConstraints.get(i).get(enteringIndex), mathContext);
                if (permit && var.compareTo(value) == 0) {
                    permit = false;
                    listRightSideOfConstraints.set(i, var);
                } else
                    listRightSideOfConstraints.set(i, listRightSideOfConstraints.get(i)
                            .subtract((listOfConstraints.get(i).get(enteringIndex).multiply(value, mathContext))));
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
        System.out.println("\nIndeksy bazy:");
        System.out.println(listOfBasisIndexes);
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
                        listOfConstraints.get(j).add(new BigDecimal("-1.0"));
                        listOfConstraints.get(j).add(new BigDecimal("1.0"));
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

    private void prepareStep() {
        listOfBasicConstraintsDividedByXi = new ArrayList<>(Collections.nCopies(numberOfConstraints, new BigDecimal("0.0")));
        listOfDifferenceVariablesAndCoefficients = new ArrayList<>(Collections.nCopies(this.listOfVariables.size(), new BigDecimal("0.0")));
        listOfCoefficientsOfVariables = new ArrayList<>(Collections.nCopies(this.listOfVariables.size(), new BigDecimal("0.0")));
    }

    private void calculateCoeficcients() {
        System.out.println("\nZj:");

        for (int i = 0; i < listOfVariables.size(); i++) {
            for (int j = 0; j < numberOfConstraints; j++) {

                listOfCoefficientsOfVariables.set(i,
                        listOfCoefficientsOfVariables.get(i)
                                .add(listOfConstraints.get(j).get(i)
                                        .multiply(listOfVariables.get(listOfBasisIndexes.get(j)), mathContext)));

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
                    listOfBasicConstraintsDividedByXi.set(i, null);

                listOfBasicConstraintsDividedByXi.set(i, listRightSideOfConstraints.get(i)
                        .divide(listOfConstraints.get(i).get(index), mathContext));


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
            decisionElement = null;
            throw new InfitnitySolutions();
        } finally {
            System.out.println("\nWektor wartości ograniczeń/Xi : " + listOfBasicConstraintsDividedByXi);
            System.out.println("Element decyzyjny : " + decisionElement);
        }

    }

    private int getIndexVariableLeavingBasis() {
        return listOfBasicConstraintsDividedByXi
                .indexOf(Collections.min(listOfBasicConstraintsDividedByXi
                        .stream()
                        .filter(a -> a.doubleValue() >= 0)
                        .collect(Collectors.toList())));
    }

    private int getIndexVariableEnteringBasis() {
        if (maximization)
            return listOfDifferenceVariablesAndCoefficients.indexOf(Collections.max(listOfDifferenceVariablesAndCoefficients));
        else
            return listOfDifferenceVariablesAndCoefficients.indexOf(Collections.min(listOfDifferenceVariablesAndCoefficients));

    }

    private List<List<BigDecimal>> makeADeepCopyOfVariables() {
        List<List<BigDecimal>> listOfConstraintsClone = new ArrayList<>();
        for (int i = 0; i < listOfConstraints.size(); i++) {
            listOfConstraintsClone.add(new ArrayList<>());
            for (int j = 0; j < listOfConstraints.get(i).size(); j++)
                listOfConstraintsClone.get(i).add(listOfConstraints.get(i).get(j));
        }
        return listOfConstraintsClone;
    }

    private void setNewTable() {
        List<List<BigDecimal>> listOfConstraintsClone = makeADeepCopyOfVariables();
        int pivotIndex = getPivotIndex();
        int indexLeaving = getIndexVariableLeavingBasis();
        System.out.println("\nElement wychodzący z bazy : " + listOfBasisIndexes.get(indexLeaving));
        int indexEntering = getIndexVariableEnteringBasis();
        System.out.println("Indeks wchodzący do bazy : " + indexEntering);
        System.out.println(" ");
        BigDecimal pivotValue = new BigDecimal("" + listOfConstraints.get(indexLeaving).get(pivotIndex));
        if (pivotValue.doubleValue() < 0) {
            System.out.println("Element leżący na przekątnej nowej bazy : " + pivotValue);
            System.out.println(" ");
            throw new InfitnitySolutions();
        }
        try {
            System.out.println("Element leżący na przekątnej nowej bazy : " + pivotValue);
            System.out.println(" ");
            listOfBasisIndexes.set(indexLeaving, indexEntering);
            for (int i = 0; i < listOfVariables.size(); i++) {
                for (int j = 0; j < numberOfConstraints; j++) {
                    if (indexLeaving == j) {
                        BigDecimal pivotResult = new BigDecimal("" + listOfConstraints.get(j).get(i).divide(pivotValue, mathContext));
                        System.out.println("Przygotowanie tabeli simplex [" + i + "][" + j + "] : " + listOfConstraints.get(j).get(i) + " / " + pivotValue + " = " + pivotResult);
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
                                        (listOfConstraintsClone.get(indexLeaving).get(i).multiply(listOfConstraintsClone.get(j).get(indexEntering), mathContext))
                                                .divide(pivotValue, mathContext)));


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
        if (maximization)
            return listOfDifferenceVariablesAndCoefficients.stream().allMatch(a -> a.doubleValue() <= 0);
        else
            return listOfDifferenceVariablesAndCoefficients.stream().allMatch(a -> a.doubleValue() >= 0);

    }

    private boolean checkIfMIsInSolution(List<BigDecimal> resultVectorList) {
        for (int i = 0; i < listOfBasisIndexes.size(); i++) {
            System.out.println("Wartość X(" + (listOfBasisIndexes.get(i) + 1) + ") wynosi : " + listOfVariables.get(listOfBasisIndexes.get(i))
                    .multiply(resultVectorList.get(listOfBasisIndexes.get(i)), mathContext).abs());
            if (resultVectorList.get(listOfBasisIndexes.get(i)).doubleValue() != 0 &&
                    listOfVariables.get(listOfBasisIndexes.get(i)).abs().compareTo(Configuration.M) == 0)
                return true;
        }
        return false;
    }

    private void printSolution() {

        for (int i = 0; i < listOfBasisIndexes.size(); i++) {
            resultVectorList.set(listOfBasisIndexes.get(i),
                    listRightSideOfConstraints.get(i));
        }

        if (!checkIfMIsInSolution(resultVectorList)) {
            optimalValue = new BigDecimal("0.0");
            for (int i = 0; i < listOfVariables.size(); i++)
                optimalValue = optimalValue.add((resultVectorList.get(i).multiply(listOfVariables.get(i), mathContext)));

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
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy_MM_dd HH_mm_ss_SSS");
        LocalDateTime now = LocalDateTime.now();
        return dateTimeFormatter.format(now);
    }

    public String returnStringSolution(){
        String result = "Wektor rozwiązania zadania optymalnego : " + resultVectorList + "\nWartość funkcji : " + optimalValue;
        return result;
    }
}

