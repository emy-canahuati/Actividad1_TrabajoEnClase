/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pii_practicabinario;

import java.util.Scanner;
import java.util.InputMismatchException;
/**
 *
 * @author emyca
 */
public class Empresa {
    
    public static void main(String[] args) {
        Scanner lea = new Scanner(System.in);
        int opcion = 0;
        EmpleadoManager manager = new EmpleadoManager();
        do {
            System.out.println("********MENU PRINCIPAL**********");
            System.out.println("1. Agregar Empleado");
            System.out.println("2. Listar empleados no despedidos");
            System.out.println("3. Agregar venta a empleado");
            System.out.println("4. Pagar Empleado");
            System.out.println("5. Despedir Empleado");
            System.out.println("6. Reporte de Empleado");
            System.out.println("7. Salir");
            System.out.println("Escoja una opcion: ");
            try {
                opcion = lea.nextInt();
                switch (opcion) {
                    case 1: {
                        System.out.println("Ingrese el nombre del empleado:");
                        lea.nextLine();
                        String name = lea.nextLine();
                        System.out.println("Ingrese su salario:");
                        double salary = lea.nextDouble();
                        manager.addEmpleado(name, salary);
                        break;
                    }
                    case 2: {
                        manager.employeesList();
                        break;
                    }
                    case 3: {
                        System.out.println("Ingrese el codigo del empleado:");
                        int code = lea.nextInt();
                        System.out.println("Ingrese el saldo:");
                        double saldo = lea.nextDouble();
                        manager.AddSale(code, saldo);
                        break;
                    }
                    case 4: {
                        System.out.println("Ingrese el codigo del empleado:");
                        int code = lea.nextInt();
                        manager.payEmployee(code);
                        break;
                    }
                    case 5: {
                        System.out.println("Ingrese el codigo del empleado");
                        int code = lea.nextInt();
                        if(!manager.fireEmployee(code)){
                            System.out.println("No se pudo despedir al empleado");
                        }
                        break;
                    }
                    case 6: {
                        System.out.println("Ingrese el codigo del empleado:");
                        int code = lea.nextInt();
                        manager.printEmployee(code);
                        break;
                    }
                    case 7: {
                        manager.cerrar();
                        break;
                    }
                }
            } catch (InputMismatchException e) {
                System.out.println("Error: ingrese una opcion valida");
            } catch (NullPointerException e) {
                System.out.println("Error: debe agregar empleados primero");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }

        } while (opcion != 7);
                
    }
}
