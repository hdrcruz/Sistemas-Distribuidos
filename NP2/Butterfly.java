/**
 * name
 */
import mpi.*;
import java.text.NumberFormat;


public class Butterfly {

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
        int id, npes, metade;
        Status status;
        long time;

        double val[] = new double[3];
        double soma[] = new double[1];
        double somatotal;

        if (!((Integer.parseInt(args[1]) > 0) && ((Integer.parseInt(args[1]) & (Integer.parseInt(args[1]) - 1)) == 0))){
            System.out.println("Quantidade de processos deve ser potencia de 2");
            System.exit(0);
        } //verifica se número de processos é potencia de 2

        MPI.Init(args); //inicia parte distribuída/paralelo
        id = MPI.COMM_WORLD.Rank();//pega o id do processo
        npes  = MPI.COMM_WORLD.Size();//pega quantos processos têm no distribuído
        startwtime = MPI.Wtime();            

        if (id == 0){
            System.out.println("iniciou o mestre");
            
            a = Double.parseDouble(args[3]);
            b = Double.parseDouble(args[4]);
            h = Double.parseDouble(args[5]);
            val[0] = a;
            val[1] = b;
            val[2] = h;
            
            for(int m=1; m<=npes-1; m++){
                MPI.COMM_WORLD.Send(val, 0, 3, MPI.DOUBLE, m, 5);
             }
        }
        else{
            status = MPI.COMM_WORLD.Recv(val, 0 , 3, MPI.DOUBLE, 0, 5);
        }

        n = (val[1]-val[0])/val[2];
        n_local = n/npes;
        a_local = val[0] + id*n_local*val[2];
        b_local = a_local + n_local*val[2];
        somatotal = integrar(a_local, b_local, val[2]);
        metade = npes;
        do {
            metade = metade/2; 
            soma[0] = somatotal;
            if (id >= metade){
                if (id == id + metade) break;
                System.out.println("Enviando soma = " + soma[0] + " de " + id + " para proc " + (id - metade));
                MPI.COMM_WORLD.Send(soma, 0, 1, MPI.DOUBLE, id - metade, 5);
                System.out.println("soma = " + soma[0] + " enviada para proc " + (id - metade));
            }else{
                System.out.println("Esperando soma de " + (id + metade));
                status = MPI.COMM_WORLD.Recv(soma, 0 , 1, MPI.DOUBLE, MPI.ANY_SOURCE, 5);
                System.out.println("Recebido somapar = " + soma[0] + " de proc " + status.source);
                somatotal = somatotal + soma[0];
            }
        } while ( (id < metade) && (metade > 1) ) ;

        if (id == 0) {
            endwtime = MPI.Wtime();
            //Format the Number to Display
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(6);
            nf.setMinimumFractionDigits(6);
            time = (long) (endwtime - startwtime);	
            System.out.println("Soma total: " + somatotal + " Tempo = " + nf.format((double)time) + " em segs");
        }

        MPI.Finalize();  //finaliza a parte distribuída 
    }

    
}