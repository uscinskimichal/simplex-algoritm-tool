package services;

import java.util.List;

public class SimplexCore {
    List<Double> variables;
    List<List<Double>> constraints;
    List<String> constraintsMark;
    boolean maximization;

    public SimplexCore(List<Double> variables, List<List<Double>> constraints , boolean maximization, List<String> constraintsMark) {
        this.variables = variables;
        this.constraints = constraints;
        this.maximization= maximization;
        this.constraintsMark=constraintsMark;
        writeData();
    }


    private void writeData(){
        variables.forEach(a-> System.out.println(a.doubleValue()));
        constraints.forEach(a ->
                    a.forEach(
                            b -> System.out.println(b.doubleValue())));
        System.out.println(maximization);
        constraintsMark.forEach(a-> System.out.println(a));
    }
}
