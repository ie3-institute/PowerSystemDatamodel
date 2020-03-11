/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.datamodel.models.value;

import edu.ie3.datamodel.models.StandardUnits;
import java.util.Objects;
import javax.measure.Quantity;
import javax.measure.quantity.Power;

/** Describes a actove power value as active power */
public class PValue implements Value {

  /** Active power */
  private Quantity<Power> p;

  /** @param p Active power */
  public PValue(Quantity<Power> p) {
    this.p = p.to(StandardUnits.ACTIVE_POWER_IN);
  }

  public Quantity<Power> getP() {
    return p;
  }

  public void setP(Quantity<Power> p) {
    this.p = p.to(StandardUnits.ACTIVE_POWER_IN);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PValue that = (PValue) o;
    return p.equals(that.p);
  }

  @Override
  public int hashCode() {
    return Objects.hash(p);
  }
}