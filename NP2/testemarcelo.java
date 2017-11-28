import java.io.*;
import javax.swing.*;
import java.text.NumberFormat;
import java.lang.Math;
import mpi.*;

public class testemarcelo {

  public static void main(String[] args) throws Exception {
     testemarcelo s = new testemarcelo(args);	 
  }
  
  public double soma(double val[], int my_pe ){
     double s=0;
     s=val[0]+my_pe; //soma val com my_pe
	 return s;
  }

  public testemarcelo(String[] args) throws MPIException {
	  
    double      startwtime, endwtime;
    int         size, ns, my_pe, npes, pot2size;
    Status      status;
	long 		time;
	
	int iterations;
	
	double val[] = new double[1];
	double soma[] = new double[1];
	
    MPI.Init(args); //inicia parte distribuída/paralelo
	
	System.out.println("Iniciou paralelo");

    my_pe = MPI.COMM_WORLD.Rank();//pega o id do processo
    npes  = MPI.COMM_WORLD.Size();//pega quantos processos têm no distribuído
	
	System.out.println("npes="+npes);
	
	if(my_pe == 0){ //mestre
	   val[0]=Double.parseDouble(JOptionPane.showInputDialog("Digite o valor"));
	   
	   System.out.println("iniciou o mestre");
	
	   startwtime = MPI.Wtime();
	   
	   for(int m=1; m<=npes-1; m++){
	      System.out.println("Enviando val="+val[0]+" para "+m);
	      MPI.COMM_WORLD.Send(val, 0, 1, MPI.DOUBLE, m, 5);
		  System.out.println("Enviado val="+val[0]+" para "+m);
       }
	   
	   soma[0]=soma(val, my_pe);
	   System.out.println("Soma parcial="+soma[0]+" no proc="+my_pe);
	   
	   double somatotal=soma[0];
	   
	   for(int m=1; m<=npes-1; m++){
	      status = MPI.COMM_WORLD.Recv(soma, 0 , 1, MPI.DOUBLE, m, 10);
		  System.out.println("Recebido somapar="+soma[0]+" de proc "+m);
		  somatotal=somatotal+soma[0];
	   }
	   
	   endwtime = MPI.Wtime();
	   
	   //Format the Number to Display
	   NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(6);
		nf.setMinimumFractionDigits(6);
	   
	   time = (long) (endwtime - startwtime);	
	   System.out.println("Soma Final="+somatotal+" Tempo="+nf.format((double) time)+" em segs");
    }
    else{  //escravos
	  status = MPI.COMM_WORLD.Recv(val, 0 , 1, MPI.DOUBLE, 0, 5);
	  System.out.println("Recebido val="+val[0]+" em proc "+my_pe);
	  
	  soma[0]=soma(val, my_pe);
	  System.out.println("Soma parcial="+soma[0]+" no proc="+my_pe);
	  
	  MPI.COMM_WORLD.Send(soma, 0, 1, MPI.DOUBLE, 0, 10);
      System.out.println("Enviado soma="+soma[0]+" em proc "+my_pe);	  
    }	


    MPI.Finalize();  //finaliza a parte distribuída  
    if(my_pe == 0) {
	   System.out.println(" Somatorio Finalizado");

    }
  }
}

/***************************
Instruções
1)Como copiar o arquivo de uma máquina pra outra via ssh
scp testemarcelo.java superusuario@10.10.29.132:/home/superusuario/mpj

2)Para compilar a aplicação MPJ
javac -cp .:$MPJ_HOME/lib/mpj.jar testemarcelo.java

3)Para executar a aplicação MPJ (o número que vem após o -np indica o número de processos no distribuído)
mpjrun.sh -np 3 -dev niodev testemarcelo
****************************/
