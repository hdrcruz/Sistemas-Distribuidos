/**
 * name
 */
import mpi.*;
import java.text.NumberFormat;


public class MpjTrapezioMestre {

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

    public static void main(String[] args) throws Exception{
        
        double a, b, n, a_local, b_local, n_local;
        double h, startwtime, endwtime;
        int my_pe, npes;
        Status status;
        long time;

        double val[] = new double[3];
        double soma[] = new double[1];
        double somatotal;

        // a = 0.0;
        // b = 100.0;
        // h = 0.5;
        // val[0] = a;
        // val[1] = b;
        // val[2] = h;


        MPI.Init(args); //inicia parte distribuída/paralelo
        my_pe = MPI.COMM_WORLD.Rank();//pega o id do processo
        npes  = MPI.COMM_WORLD.Size();//pega quantos processos têm no distribuído
        startwtime = MPI.Wtime();
        //LER e ENVIAR DADOS
        if (my_pe == 0){
            System.out.println("iniciou o mestre");
            
            val[0] = Double.parseDouble(args[3]);
            val[1] = Double.parseDouble(args[4]);
            val[2] = Double.parseDouble(args[5]);
            
            for(int m=1; m<=npes-1; m++){
                MPI.COMM_WORLD.Send(val, 0, 3, MPI.DOUBLE, m, 5);
             }
        }
        else{
            status = MPI.COMM_WORLD.Recv(val, 0 , 3, MPI.DOUBLE, 0, 5);
        }

        n = (val[1]-val[0])/val[2];
        n_local = n/npes;
        a_local = val[0] + my_pe*n_local*val[2];
        b_local = a_local + n_local*val[2];
        soma[0] = integrar(a_local, b_local, val[2]);

        if (my_pe == 0) {
            somatotal = soma[0];
            System.out.println("Recebido somapar = " + soma[0] + " de proc " + my_pe);
            for(int m=1; m<=npes-1; m++){
                status = MPI.COMM_WORLD.Recv(soma, 0 , 1, MPI.DOUBLE, m, 10);
                System.out.println("Recebido somapar = " + soma[0] + " de proc " + m);
                somatotal=somatotal+soma[0];
            }
            endwtime = MPI.Wtime();

            //Format the Number to Display
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(6);
            nf.setMinimumFractionDigits(6);
            
            time = (long) (endwtime - startwtime);	
            System.out.println("Soma total: " + somatotal + " Tempo = " + nf.format((double)time) + " em segs");
        } else {
            MPI.COMM_WORLD.Send(soma, 0, 1, MPI.DOUBLE, 0, 10);
        }


        MPI.Finalize();  //finaliza a parte distribuída 
    }

    
}