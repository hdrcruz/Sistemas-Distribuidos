/**
 * name
 */
public class Trapezio {

    static double f(double x) {
        return 2*(Math.pow(x, 3.0) + Math.pow(x, 2.0) + x + 1.0);
     }

    static double integrar(double a, double b, double h) {
        
        double n = (b - a)/h;
        double soma = 0.5 * (f(a) + f(b));    // area
        for (int i = 1; i < n; i++) {
           double x = a + h * i;
           soma = soma + f(x);
        }

        return soma * h;
     }

     static double integrar(double a, double b, int n) {
        
        double h = (b - a)/n;
        double soma = 0.5 * (f(a) + f(b));    // area
        for (int i = 1; i < n; i++) {
           double x = a + h * i;
           soma = soma + f(x);
        }

        return soma * h;
     }

     public static void main(String[] args) { 
        double a = Double.parseDouble(args[0]);
        double b = Double.parseDouble(args[1]);
        double h = Double.parseDouble(args[2]);
        //int n = Integer.parseInt(args[2]);
        System.out.println(integrar(a, b, h));
     }
}