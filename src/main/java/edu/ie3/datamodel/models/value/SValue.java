/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.datamodel.models.value;

import edu.ie3.datamodel.models.StandardUnits;
import tec.uom.se.ComparableQuantity;

import java.util.Objects;
import javax.measure.quantity.Power;

/** Describes a apparent power value as a pair of active and reactive power */
public class SValue extends PValue {

  /** Reactive power */
  private ComparableQuantity<Power> q; // TODO doublecheck

  /** @param q Reactive power */
  public SValue(ComparableQuantity<Power> p, ComparableQuantity<Power> q) { // TODO doublecheck
    super(p);
    this.q = q.to(StandardUnits.REACTIVE_POWER_IN);
  }

  public ComparableQuantity<Power> getQ() {
    return q;
  } // TODO doublecheck

  public void setQ(ComparableQuantity<Power> q) {
    this.q = q.to(StandardUnits.REACTIVE_POWER_IN);
  } // TODO doublecheck

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SValue that = (SValue) o;
    return q.equals(that.q);
  }

  @Override
  public int hashCode() {
    return Objects.hash(q);
  }

  @Override
  public String toString() {
    return "SValue{" + "q=" + q + '}';
  }
}
