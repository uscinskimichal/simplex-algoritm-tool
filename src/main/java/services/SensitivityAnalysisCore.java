package services;


import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class SensitivityAnalysisCore {
    MathContext mathContext = new MathContext(4, RoundingMode.HALF_EVEN);
    private List<BigDecimal> listRightSideOfConstraints;
    private List<List<BigDecimal>> subTable = new ArrayList<>();
    private final int numberOfConstraints;
    private List<Integer> artificialVariablesIndexes;
    private List<List<BigDecimal>> listOfConstraints;


    public SensitivityAnalysisCore(int numberOfConstraints, List<Integer> artificialVariablesIndexes, List<List<BigDecimal>> listOfConstraints, List<BigDecimal> listRightSideOfConstraints) {
        this.numberOfConstraints = numberOfConstraints;
        this.artificialVariablesIndexes = artificialVariablesIndexes;
        this.listOfConstraints = listOfConstraints;
        this.listRightSideOfConstraints = listRightSideOfConstraints;
        //  this.listRightSideOfConstraints = new ArrayList<>();
        // this.listRightSideOfConstraints.add(new BigDecimal(430));
        // this.listRightSideOfConstraints.add(new BigDecimal(460));
        // this.listRightSideOfConstraints.add(new BigDecimal(420));
        listRightSideOfConstraints.forEach(a -> System.out.println(a.doubleValue()));
    }

    public void calculatePossibleRightSideConstraintsChange() {
        for (int i = 0; i < numberOfConstraints; i++) {
            List<BigDecimal> temp = new ArrayList<>();
            for (int j = 0; j < artificialVariablesIndexes.size(); j++) {
                temp.add(listOfConstraints.get(i).get(artificialVariablesIndexes.get(j)));
            }
            subTable.add(temp);
        }

        System.out.println(subTable);
        System.out.println("\n" + listRightSideOfConstraints);

        BigDecimal unknown;
        for (int z = 0; z < numberOfConstraints; z++) {
            for (int i = 0; i < numberOfConstraints; i++) {
                try {
                    unknown = new BigDecimal(0);
                    BigDecimal indexDivide = null;
                    for (int j = 0; j < subTable.get(i).size(); j++) {
                        if (j != z) {
                            unknown = (subTable.get(i).get(j).negate().multiply(listRightSideOfConstraints.get(j)).add(unknown));
                        } else {
                            indexDivide = new BigDecimal(subTable.get(i).get(j) + "");
                        }

                    }
                    if (indexDivide.compareTo(BigDecimal.ZERO) < 0)
                        System.out.println("Dzielnik : " + indexDivide);
                    unknown = unknown.divide(indexDivide, mathContext);
                    System.out.println("Wartość dla ograniczenia (" + (z + 1) + ") : " + unknown.doubleValue());
                } catch (ArithmeticException | NullPointerException ex) {
                    System.out.println(ex.getClass() + "zlapalem!");
                }
            }
            System.out.println("\n\n");
        }
    }
}
// TO DO : ZROBIC PRZEDZIALY JAK SIE ZMIENIA, SPRAWDZIC CO GDY WYSTEPUJE =, ALBO >=