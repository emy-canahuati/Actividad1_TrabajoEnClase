/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pii_practicabinario;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
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
            
        }
        System.out.println("");
        return;
    }
    
    /*
    private boolean isDespedido(int code) throws IOException {
        remps.seek(0);
        while (remps.getFilePointer() < remps.length()) {
            int codigo = remps.readInt();
            remps.readUTF();
            remps.skipBytes(16);
            long fechaD = remps.readLong();
            if (codigo == code) {
                if (fechaD == 0) {
                    return false;
                }else{
                    System.out.println("Este empleado esta despedido");
                    return true;
                }
            }
        }
        System.out.println("Este empleado no existe");
        return true ;
    }

    public void addVenta(int code, int mes, double saldo) throws IOException {
        if (isDespedido(code)) {
            return;
        }

        if(mes<1 ||mes>12){
            System.out.println("Mes invalido");
            return;
        }
        
        RandomAccessFile rventas = salesFileFor(code);
        int posicion = ((mes - 1) * 9);
        rventas.seek(posicion);
        double saldoAnterior = rventas.readDouble();
        rventas.seek(posicion);
        rventas.writeDouble(saldoAnterior + saldo);
        rventas.close();
        System.out.println("Venta agregada con exito");
    }

    public void pagarEmpleado(int code) throws IOException{
        if (isDespedido(code)) {
            return;
        }

        remps.seek(0);
        double salarioBase = 0;
        while (remps.getFilePointer() < remps.length()) {
            int codigo = remps.readInt();
            remps.readUTF();
            if (codigo == code) {
                salarioBase = remps.readDouble();
                break;
            } else {
                remps.skipBytes(24);
            }
        }

        RandomAccessFile rventas = salesFileFor(code);
        int mes = Calendar.getInstance().get(Calendar.MONTH);
        int posicion = ((mes) * 9);
        rventas.seek(posicion);
        double monto = rventas.readDouble();
        boolean pagado = rventas.readBoolean();
        if (pagado) {
            System.out.println("Ya se le pago este mes");
            rventas.close();
            return;
        }
        rventas.seek(posicion + 8);
        rventas.writeBoolean(true);
        rventas.close();
        double comision = monto * 0.10;
        double total = salarioBase + comision;
        System.out.println("Se le pago al Empleado con codigo #" + code + " un total de $" + total);
        System.out.println("Total ventas: $" + monto + "  Comision: $" + comision);

    }

    public void despedirEmpleado(int code) throws IOException {
        if (isDespedido(code)) {
            return;
        }

        remps.seek(0);
        while (remps.getFilePointer() < remps.length()) {
            int codigoEmp = remps.readInt();
            remps.readUTF();
            remps.skipBytes(16);
            if (codigoEmp == code) {
                remps.writeLong(Calendar.getInstance().getTimeInMillis());
                System.out.println("Empleado despedido");
                return;
            } else {
                remps.skipBytes(8);
            }
        }
    }

    public void cerrar() throws IOException {
        remps.close();
        rcodes.close();

    }*/
}

