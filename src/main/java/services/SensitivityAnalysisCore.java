package services;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class SensitivityAnalysisCore {
    private List<List<BigDecimal>> subTable = new ArrayList<>();
    private SimplexCore simplexCore;


    public SensitivityAnalysisCore(SimplexCore simplexCore) {
        this.simplexCore = simplexCore;
        System.out.println("\n\n");
        System.out.println("---------------------------------------");
        System.out.println("| - - - - Analiza wrażliwości - - - - |");
        System.out.println("---------------------------------------");
    }

    public void calculatePossibleRightSideConstraintsChange() {
        List<List<Double>> greaterThanNumbers = new ArrayList<>();
        List<List<Double>> lowerThanNumbers = new ArrayList<>();

        for (int i = 0; i < simplexCore.numberOfConstraints; i++) {
            List<BigDecimal> temp = new ArrayList<>();
            for (int j = 0; j < simplexCore.artificialVariablesIndexes.size(); j++) {
                temp.add(simplexCore.listOfConstraints.get(i).get(simplexCore.artificialVariablesIndexes.get(j)));
            }
            subTable.add(temp);
        }

        BigDecimal variable;
        for (int z = 0; z < simplexCore.numberOfConstraints; z++) {
            for (int i = 0; i < simplexCore.numberOfConstraints; i++) {
                try {
                    variable = new BigDecimal(0);
                    BigDecimal indexDivide = null;
                    for (int j = 0; j < subTable.get(i).size(); j++) {
                        if (j != z) {
                            variable = (subTable.get(i).get(j).negate().multiply(simplexCore.copyOfRHS.get(j)).add(variable));
                        } else {
                            indexDivide = new BigDecimal(subTable.get(i).get(j) + "");
                        }
                    }
                    variable = variable.divide(indexDivide, simplexCore.mathContext);
                    greaterThanNumbers.add(new ArrayList<>());
                    lowerThanNumbers.add(new ArrayList<>());
                    if (indexDivide.compareTo(BigDecimal.ZERO) < 0) {
                        lowerThanNumbers.get(z).add(variable.doubleValue());
                    } else
                        greaterThanNumbers.get(z).add(variable.doubleValue());

                } catch (ArithmeticException ae) {
                    // noop
                }
            }
            double maxElement;
            double minElement;
            try {
                minElement = Collections.min(lowerThanNumbers.get(z));
            } catch (NoSuchElementException | IndexOutOfBoundsException nse) {
                minElement = Double.POSITIVE_INFINITY;
            }
            try {
                maxElement = Collections.max(greaterThanNumbers.get(z));
            } catch (NoSuchElementException | IndexOutOfBoundsException nse) {
                maxElement = Double.NEGATIVE_INFINITY;
            }

            if (simplexCore.copyOfRHSDefault.get(z).doubleValue() < 0) {
                double tmp = -maxElement;
                maxElement = -minElement;
                minElement = tmp;
            }
            if ((maxElement < 0 && minElement > 0) && simplexCore.copyOfRHSDefault.get(z).doubleValue() < 0 && (maxElement != Double.NEGATIVE_INFINITY && minElement != Double.POSITIVE_INFINITY))
                minElement = 0;
            else if ((maxElement < 0 && minElement > 0) && simplexCore.copyOfRHSDefault.get(z).doubleValue() > 0 && (maxElement != Double.NEGATIVE_INFINITY && minElement != Double.POSITIVE_INFINITY))
                maxElement = 0;

            System.out.println("Przedział prawej strony dla (" + (z + 1) + ") ograniczenia, w którym baza rozwiązania pozostaje niezmienna : (" + maxElement + " , " + minElement + ")");

            greaterThanNumbers.get(z).clear();
            lowerThanNumbers.get(z).clear();

        }
        System.out.println("\n\n");
    }

    public void calculatePossibleCoeffFunctionChange() {
        List<BigDecimal> basisCoeffValues = new ArrayList<>();
        List<BigDecimal> nonBasisCoeffValues = new ArrayList<>();
        List<Integer> basisCoeffIndexes = new ArrayList<>();
        List<Integer> nonBasisCoeffIndexes = new ArrayList<>();

        basisCoeffIndexes.addAll(simplexCore.listOfBasisIndexes);
        basisCoeffValues.addAll(basisCoeffIndexes.stream().map(a -> simplexCore.listOfVariables.get(a)).collect(Collectors.toList()));

        for (int i = 0; i < simplexCore.listOfVariables.size(); i++)
            nonBasisCoeffIndexes.add(i);
        nonBasisCoeffIndexes.removeAll(basisCoeffIndexes);
        for (int i = 0; i < nonBasisCoeffIndexes.size(); i++)
            nonBasisCoeffValues.add(simplexCore.listOfVariables.get(nonBasisCoeffIndexes.get(i)));

        for (int i = 0; i < simplexCore.numberOfVariables; i++) {
            List<BigDecimal> tableColumn = new ArrayList<>();
            for (int y = 0; y < simplexCore.numberOfConstraints; y++)
                tableColumn.add(simplexCore.listOfConstraints.get(y).get(nonBasisCoeffIndexes.get(i)));
            BigDecimal result = new BigDecimal("0");

            for (int y = 0; y < basisCoeffValues.size(); y++)
                result = result.add(basisCoeffValues.get(y).multiply(tableColumn.get(y)),simplexCore.mathContext);

            if (nonBasisCoeffIndexes.get(i) < simplexCore.numberOfVariables && simplexCore.maximization)
                System.out.println("Wartość funkcji celu nie zmieni się, przy modyfikacji współczynnika a(" + (nonBasisCoeffIndexes.get(i) + 1) + ") jeżeli wartość tej zmiennej będzie mniejsza niż : " + result);
            else if (nonBasisCoeffIndexes.get(i) < simplexCore.numberOfVariables && !simplexCore.maximization)
                System.out.println("Wartość funkcji celu nie zmieni się, przy modyfikacji współczynnika a(" + (nonBasisCoeffIndexes.get(i) + 1) + ") jeżeli wartość tej zmiennej będzie większa niż : " + result);

        }
        basisCoeffIndexes.forEach(a -> {
            if (a < simplexCore.numberOfVariables)
                System.out.println("Zmiana wartości współczynnika funkcji celu a(" + (a + 1) + "), spowoduje zmianę wartości funkcji celu.");
        });

    }
}