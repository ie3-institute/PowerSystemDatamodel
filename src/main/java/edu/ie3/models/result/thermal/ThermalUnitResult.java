/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.models.result.thermal;

import edu.ie3.models.StandardUnits;
import edu.ie3.models.result.ResultEntity;

import javax.measure.Quantity;
import javax.measure.quantity.Power;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

/** Representation of a result with regard to a thermal unit */
public abstract class ThermalUnitResult extends ResultEntity {

  /** Average thermal power flowing into the thermal unit (+: Power flowing into unit, -: Power flowing from unit)  */
  private Quantity<Power> qDot;

  /**
   * Constructor for the thermal result with
   *
   * @param timestamp The time, the result is related to
   * @param inputModel The input model's UUID, the result is related to
   * @param qDot Average thermal power exchanged with the unit
   */
  public ThermalUnitResult(ZonedDateTime timestamp, UUID inputModel, Quantity<Power> qDot) {
    super(timestamp, inputModel);
    this.qDot = qDot;
  }

  /**
   * Constructor for the thermal result with
   *
   * @param uuid The uuid of this result
   * @param timestamp The time, the result is related to
   * @param inputModel The input model's UUID, the result is related to
   * @param qDot Average thermal power exchanged with the unit
   */
  public ThermalUnitResult(UUID uuid, ZonedDateTime timestamp, UUID inputModel, Quantity<Power> qDot) {
    super(uuid, timestamp, inputModel);
    this.qDot = qDot;
  }

  public Quantity<Power> getqDot() {
    return qDot;
  }

  public void setqDot(Quantity<Power> qDot) {
    this.qDot = qDot.to(StandardUnits.HEAT_DEMAND);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    ThermalUnitResult that = (ThermalUnitResult) o;
    return qDot.equals(that.qDot);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), qDot);
  }
}
