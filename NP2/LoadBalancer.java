/**
 * name
 */
import mpi.*;
import java.text.NumberFormat;


public class LoadBalancer {

    static final int DIETAG = 2;
    static final int WORKTAG = 1;

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
        
        double a, b, n, a_local, b_local;
        double h, startwtime, endwtime;
        int my_pe, npes, size, n_local, ntasks;
        Status status;
        long time;

        double val[] = new double[3];
        double soma[] = new double[1];
        double somatotal;


        MPI.Init(args); //inicia parte distribuída/paralelo
        my_pe = MPI.COMM_WORLD.Rank();//pega o id do processo
        npes  = MPI.COMM_WORLD.Size();//pega quantos processos têm no distribuído
        startwtime = MPI.Wtime();
        //LER e ENVIAR DADOS
        if (my_pe == 0){
            System.out.println("iniciou o mestre");
            
            a = Double.parseDouble(args[3]);
            b = Double.parseDouble(args[4]);
            h = Double.parseDouble(args[5]);
            val[0] = a;
            val[1] = b;
            val[2] = h;

            n = (int)(b-a)/h; //determinar quantidade de intervalos
            size = 10;
            ntasks = (int)n/size;
            n_local = 0;
            
            for(int m=1; m<npes; m++){
                val[0] = a + n_local*size*h;
                val[1] = val[0] + size*h;
                MPI.COMM_WORLD.Send(val, 0, 3, MPI.DOUBLE, m, 5);
                System.out.println("Enviando parte = " + n_local + " para proc " + m);
                n_local++;
            }

            System.out.println(n + " " + n_local + " " + ntasks);

            somatotal = 0;

            while (n_local <= ntasks-1){
                status = MPI.COMM_WORLD.Recv(soma, 0 , 1, MPI.DOUBLE, MPI.ANY_SOURCE, 10);
                System.out.println("Recebido somapar = " + soma[0] + " de proc " + status.source);
                somatotal=somatotal+soma[0];
                val[0] = a + n_local*size*h;
                val[1] = val[0] + size*h;
                System.out.println("Enviando parte = " + n_local + " para proc " + status.source);
                MPI.COMM_WORLD.Send(val, 0, 3, MPI.DOUBLE, status.source, 5);
                n_local++;
            }

            for(int m=1; m<=npes-1; m++){
                status = MPI.COMM_WORLD.Recv(soma, 0 , 1, MPI.DOUBLE, m, 10);
                System.out.println("Recebido somapar = " + soma[0] + " de proc " + status.source);
                somatotal=somatotal+soma[0];
            }
           

            endwtime = MPI.Wtime();
       
            //Format the Number to Display
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(6);
            nf.setMinimumFractionDigits(6);
            
            time = (long) (endwtime - startwtime);	
            System.out.println("Soma total: " + somatotal + " Tempo = " + nf.format((double)time) + " em segs");

            for(int m=1; m<=npes-1; m++){
                //System.out.println("Envio de kill para proc: " + m);    
                MPI.COMM_WORLD.Send(val, 0, 3, MPI.DOUBLE, m, DIETAG);
            }

        }
        else{
            for(;;){
                status = MPI.COMM_WORLD.Recv(val, 0 , 3, MPI.DOUBLE, 0, MPI.ANY_TAG);
                if (status.tag == DIETAG){
                    //System.out.println("Encerrando processo " + my_pe);
                    break;
                }
                soma[0] = integrar(val[0], val[1], val[2]);
                MPI.COMM_WORLD.Send(soma, 0, 1, MPI.DOUBLE, 0, 10);
            }
            
        }
            

        MPI.Finalize();  //finaliza a parte distribuída 
    }

    
}