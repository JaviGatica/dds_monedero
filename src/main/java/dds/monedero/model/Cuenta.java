package dds.monedero.model;

import dds.monedero.exceptions.MaximaCantidadDepositosException;
import dds.monedero.exceptions.MaximoExtraccionDiarioException;
import dds.monedero.exceptions.MontoNegativoException;
import dds.monedero.exceptions.SaldoMenorException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Cuenta {

	
  private double saldo; //deberia hacerse en el constructor
  private List<Movimiento> movimientos = new ArrayList<>();

  public Cuenta() {
    saldo = 0;
  }

  public Cuenta(double montoInicial) {
    saldo = montoInicial;
  } //entiendo el punto de poder crear un monedero con un saldo inicial, pero en este caso siento que es mejor inicializarlo siempre en 0 y cargarle el monto a mano

  public void setMovimientos(List<Movimiento> movimientos) {
    this.movimientos = movimientos;
  }
  
  private long cantidadDepositos() {
	  return getMovimientos().stream().filter(movimiento -> movimiento.isDeposito()).count();
  }

  public void poner(double cuanto) {
    if (cuanto <= 0) {
      throw new MontoNegativoException(cuanto + ": el monto a ingresar debe ser un valor positivo");
    }

    if (cantidadDepositos() >= 3) { // uso una funcion auxiliar en vez de resolverlo de forma a.b().c().d() >= 3
      throw new MaximaCantidadDepositosException("Ya excedio los " + 3 + " depositos diarios");
    }
    this.agregarMovimiento(LocalDate.now(), cuanto, true);
  }
  
  private double limite() {
	  double montoExtraidoHoy = getMontoExtraidoA(LocalDate.now());
	  return 1000 - montoExtraidoHoy;
  }

  public void sacar(double cuanto) {
    if (cuanto <= 0) {
      throw new MontoNegativoException(cuanto + ": el monto a ingresar debe ser un valor positivo");
    }
    if (getSaldo() - cuanto < 0) {
      throw new SaldoMenorException("No puede sacar mas de " + getSaldo() + " $");
    }
    if (cuanto > this.limite()) { //uso una funcion auxiliar
      throw new MaximoExtraccionDiarioException("No puede extraer mas de $ " + 1000
          + " diarios, límite: " + this.limite());
    }

    this.agregarMovimiento(LocalDate.now(), cuanto, false);
  } //sobrecargada, facilmente entran 2 funciones auxiliares para chequear que no se retire montos mayores al saldo o que se exceda el limite

  private void actualizarSaldo(Movimiento mov) {
	  saldo += mov.calcularValor();
  }
  
  public void agregarMovimiento(LocalDate fecha, double cuanto, boolean esDeposito) {
    Movimiento movimiento = new Movimiento(fecha, cuanto, esDeposito);
    movimientos.add(movimiento);
    actualizarSaldo(movimiento);
  }

  public double getMontoExtraidoA(LocalDate fecha) {
    return getMovimientos().stream()
        .filter(movimiento -> !movimiento.isDeposito() && movimiento.esDeLaFecha(fecha)) // ya hay una funcion que hace esto
        .mapToDouble(Movimiento::getMonto)
        .sum();
  }

  public List<Movimiento> getMovimientos() {
    return movimientos;
  }

  public double getSaldo() {
    return saldo;
  }

  public void setSaldo(double saldo) {
    this.saldo = saldo;
  }

}
