package services;


import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class SensitivityAnalysisCore {
    private List<List<BigDecimal>> subTable = new ArrayList<>();
    private SimplexCore simplexCore;


    public SensitivityAnalysisCore(SimplexCore simplexCore) {
        this.simplexCore = simplexCore;
    }

    public void calculatePossibleRightSideConstraintsChange() {
        List<List<Double>> greaterThanNumbers = new ArrayList<List<Double>>();
        List<List<Double>> lowerThanNumbers = new ArrayList<List<Double>>();
        double mini = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < simplexCore.numberOfConstraints; i++) {
            List<BigDecimal> temp = new ArrayList<>();
            for (int j = 0; j < simplexCore.artificialVariablesIndexes.size(); j++) {
                temp.add(simplexCore.listOfConstraints.get(i).get(simplexCore.artificialVariablesIndexes.get(j)));
            }
            subTable.add(temp);
        }

        System.out.println(subTable);
        System.out.println("\n" + simplexCore.copyOfRHS);

        BigDecimal unknown;
        for (int z = 0; z < simplexCore.numberOfConstraints; z++) {
            for (int i = 0; i < simplexCore.numberOfConstraints; i++) {
                try {
                    unknown = new BigDecimal(0);
                    BigDecimal indexDivide = null;
                    for (int j = 0; j < subTable.get(i).size(); j++) {
                        if (j != z) {
                            unknown = (subTable.get(i).get(j).negate().multiply(simplexCore.copyOfRHS.get(j)).add(unknown));
                        } else {
                            indexDivide = new BigDecimal(subTable.get(i).get(j) + "");
                        }
                    }
                    unknown = unknown.divide(indexDivide, simplexCore.mathContext);
                    greaterThanNumbers.add(new ArrayList<>());
                    lowerThanNumbers.add(new ArrayList<>());
                    if (indexDivide.compareTo(BigDecimal.ZERO) < 0) {
                        //System.out.println("Dzielnik : " + indexDivide);
                        lowerThanNumbers.get(z).add(unknown.doubleValue());
                    } else
                        greaterThanNumbers.get(z).add(unknown.doubleValue());

                    //System.out.println("Wartość dla ograniczenia (" + (z + 1) + ") : " + unknown.doubleValue());
                } catch (ArithmeticException ae) {
                    //System.out.println(ae.getClass() + "zlapalem!");
                    // greaterThanNumbers.add(Double.POSITIVE_INFINITY);
                }
            }
            double maxElement = 0;
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
            System.out.println(maxElement);
            if (maxElement == Double.NEGATIVE_INFINITY) {
                maxElement = 0;
                //double tmp = -maxElement;
                //maxElement = -minElement;
                // minElement = tmp;
            } else if (maxElement < 0 && maxElement != Double.NEGATIVE_INFINITY) {
                maxElement = -minElement;
                minElement = 0;
            }
            System.out.println("Przedział dla (" + (z + 1) + ") ograniczenia : (" + maxElement + " , " + minElement + ")");
            System.out.println(greaterThanNumbers);
            System.out.println(lowerThanNumbers);

            greaterThanNumbers.get(z).clear();
            lowerThanNumbers.get(z).clear();
            System.out.println("\n\n");
        }
    }


}