/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pii_practicabinario;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author emyca
 */
public class EmpleadoManager {
    /*
    Formato:
    1. File Codigos.emp:
    int code - 4 bytes Mantener
    
    2. File Empleado.emp:
    int code
    String name
    double salary
    long fechaContratacion
    long fechaDespido
    
    */
    
    private RandomAccessFile rcodes, remps;
    
    public EmpleadoManager(){
        try{
            File mf= new File("company");
            mf.mkdir();
            rcodes= new RandomAccessFile("company/Codigos.emp","rw");
            remps= new RandomAccessFile("company/Empleado.emp","rw");
            
            //inicializar un dato para el archivo de codigo
            initCode();
        } catch (IOException e) {
            System.out.println("Error!");
        }
    }

    private void initCode() throws IOException {
        if (rcodes.length() == 0)//verificamos que pase solo en archivos vacios
        {
            rcodes.writeInt(1);
        }
    }

    private int getCode() throws IOException {
        rcodes.seek(0);
        int codigo = rcodes.readInt();
        rcodes.seek(0);
        rcodes.writeInt(codigo + 1);
        return codigo;
    }

    public void addEmpleado(String name, double salary) throws IOException {
        remps.seek(remps.length());
        int code = getCode();
        remps.writeInt(code);
        remps.writeUTF(name);
        remps.writeDouble(salary);
        remps.writeLong(Calendar.getInstance().getTimeInMillis());
        remps.writeLong(0);
        //Crear Folder del Empleado   
        createEmployeeFolder(code);
    }

    private String employesFolder(int code) {
        return "company/Empleado" + code;
    }

    private RandomAccessFile salesFileFor(int code) throws IOException {
        String padre = employesFolder(code);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String direccion = padre + "/ventas" + year + ".emp";
        return new RandomAccessFile(direccion, "rw");
    }

    /*Formato VentasYear.emp
    double saldo
    boolean estadoPago 
   
     */

    private void createYearSaleFileFor(int code) throws IOException {
        RandomAccessFile rventas = salesFileFor(code);
        if (rventas.length() == 0) {
            for (int mes = 0; mes < 12; mes++) {
                rventas.writeDouble(0);
                rventas.writeBoolean(false);
            }
        }
        rventas.close();
    }

    private void createEmployeeFolder(int code) throws IOException {
        File dir = new File(employesFolder(code));
        dir.mkdir();
        createYearSaleFileFor(code);
    }

    public void employeesList() throws IOException {
        remps.seek(0);
        int listado = 1;
        while (remps.getFilePointer() < remps.length()) {
            int code = remps.readInt();
            String name = remps.readUTF();
            double salario = remps.readDouble();
            Date fechaContratacion = new Date(remps.readLong());
            if (remps.readLong() == 0) {
                System.out.println(listado + ". " + code + "-" + name + "- $" + salario + "-" + fechaContratacion.toString());
                listado++;
            }
        }
    }
    
    private boolean isEmployeeActive(int code) throws IOException{
        remps.seek(0);
        while(remps.getFilePointer() < remps.length()){
            int cod=remps.readInt();
            long pos=remps.getFilePointer();
            remps.readUTF();
            remps.skipBytes(16);
            if(remps.readLong()==0 && cod==code){
                remps.seek(pos);
                return true;
            }
        }
        return false;
    }
    
    public boolean fireEmployee(int code) throws IOException{
        if (isEmployeeActive(code)){
            String name= remps.readUTF();
            remps.skipBytes(16);
            remps.writeLong(new Date().getTime());
            System.out.println("Despidiendo a: "+name);
            return true;
        }
        return false;
    }
    
    public void AddSale(int code, double ven) throws IOException{
        if(isEmployeeActive(code)){
            RandomAccessFile sales= salesFileFor(code);
            int pos =(Calendar.getInstance().get(Calendar.MONTH))*9;
            sales.seek(pos);
            double monto= sales.readDouble();
            sales.seek(pos);
            sales.writeDouble(ven+monto);
            sales.close();
        }else{
            System.out.println("No se pudo agregar la venta");
        }
    }
    //recibo
    
    private boolean isEmployeePayed(int code) throws IOException{
        RandomAccessFile rventas = salesFileFor(code);
        int posicion = (Calendar.getInstance().get(Calendar.MONTH) * 9);
        rventas.seek(posicion);
        rventas.skipBytes(8);
        boolean pagado =rventas.readBoolean();
        rventas.close();
        return pagado;
    }
    
    private RandomAccessFile billsFilefor(int code) throws IOException {
        String padre = employesFolder(code);
        String direccion = padre + "/recibos.emp";
        return new RandomAccessFile(direccion, "rw");
    }
    
    public void payEmployee(int code) throws IOException{
        if (isEmployeeActive(code) && !isEmployeePayed(code)) {
            String nombre=remps.readUTF();
            double salarioBase = remps.readDouble();
            
            RandomAccessFile rventas = salesFileFor(code);
            int mesActual =Calendar.getInstance().get(Calendar.MONTH);
            int anioActual= Calendar.getInstance().get(Calendar.YEAR);
            int posicion = (mesActual * 9);
            
            rventas.seek(posicion);
            double ventas = rventas.readDouble();
            rventas.seek(posicion + 8);
            rventas.writeBoolean(true);
            rventas.close();
            
            double sueldo = salarioBase + (ventas*0.10);
            double deduccion= sueldo*0.035;
            double total=sueldo-deduccion;
            
            RandomAccessFile recibos= billsFilefor(code);
            recibos.seek(recibos.length());
            recibos.writeLong(new Date().getTime());
            recibos.writeDouble(sueldo);
            recibos.writeDouble(deduccion);
            recibos.writeInt(anioActual);
            recibos.writeInt(mesActual+1);
            recibos.close();
    
            System.out.println("Empleado "+nombre+ " se le pago Lps." + total);
        }else{
            System.out.println("No se pudo pagar");
        } 
    }

    public void printEmployee(int code) throws IOException{
        if(isEmployeeActive(code)){
            String nombre=remps.readUTF();
            double salario = remps.readDouble();
            Date fechaContratacion= new Date(remps.readLong());
            SimpleDateFormat formato= new SimpleDateFormat("MM/dd/yy");
            System.out.println("Codigo: "+code+""
                                + "\nNombre: "+nombre+""
                                + "\nSalario: "+salario+""
                                + "\nFecha de contratacion: "+formato.format(fechaContratacion));
            
            RandomAccessFile rventas = salesFileFor(code);
            rventas.seek(0);
            double suma=0;
            for(int mes=1; mes <= 12; mes++){
                double monto= rventas.readDouble();
                rventas.skipBytes(1);
                System.out.println("Mes "+mes+": "+monto);
                suma+=monto;
            }
            rventas.close();
            System.out.println("Total de ventas del año: "+suma);
            
            RandomAccessFile recibos = billsFilefor(code);
            int totalRecibos=0;
            recibos.seek(0);
            while (recibos.getFilePointer() < recibos.length()){
                recibos.skipBytes(32);
                totalRecibos++;
            }
            recibos.close();
            System.out.println("Total de pagos realizados: "+totalRecibos);
        }else{
            System.out.println("Este empleado no existe o esta despedido");
        }
    }
    public void cerrar() throws IOException {
        remps.close();
        rcodes.close();

    }
}

