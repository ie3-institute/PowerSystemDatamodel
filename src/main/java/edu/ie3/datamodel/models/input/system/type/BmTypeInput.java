/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.datamodel.models.input.system.type;

import edu.ie3.datamodel.models.StandardUnits;
import edu.ie3.util.quantities.interfaces.Currency;
import edu.ie3.util.quantities.interfaces.DimensionlessRate;
import edu.ie3.util.quantities.interfaces.EnergyPrice;
import tec.uom.se.ComparableQuantity;

import java.util.Objects;
import java.util.UUID;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Power;

/** Describes the type of a {@link edu.ie3.datamodel.models.input.system.BmInput} */
public class BmTypeInput extends SystemParticipantTypeInput {

  /** Permissible load gradient (typically in %/h) */
  private final ComparableQuantity<DimensionlessRate> activePowerGradient; // TODO doublecheck
  /** Efficiency of converter for this type of BM (typically in %) */
  private final ComparableQuantity<Dimensionless> etaConv; // TODO doublecheck

  /**
   * @param uuid of the input entity
   * @param id of this type of BM
   * @param capex Capital expense for this type of BM (typically in €)
   * @param opex Operating expense for this type of BM (typically in €)
   * @param cosphiRated Power factor for this type of BM
   * @param activePowerGradient Maximum permissible gradient of active power change
   * @param sRated Rated apparent power for this type of BM (typically in kVA)
   * @param etaConv Efficiency of converter for this type of BM (typically in %)
   */
  public BmTypeInput(
      UUID uuid,
      String id,
      ComparableQuantity<Currency> capex, // TODO doublecheck - no return value, but superclass expects comparable
      ComparableQuantity<EnergyPrice> opex, // TODO doublecheck - no return value, but superclass expects comparable
      ComparableQuantity<DimensionlessRate> activePowerGradient, // TODO doublecheck
      ComparableQuantity<Power> sRated, // TODO doublecheck - no return value, but superclass expects comparable
      double cosphiRated,
      ComparableQuantity<Dimensionless> etaConv) { // TODO doublecheck
    super(uuid, id, capex, opex, sRated.to(StandardUnits.S_RATED), cosphiRated);
    this.activePowerGradient = activePowerGradient.to(StandardUnits.ACTIVE_POWER_GRADIENT);
    this.etaConv = etaConv.to(StandardUnits.EFFICIENCY);
  }

  public ComparableQuantity<DimensionlessRate> getActivePowerGradient() {
    return activePowerGradient;
  } // TODO doublecheck

  public ComparableQuantity<Dimensionless> getEtaConv() {
    return etaConv;
  } // TODO doublecheck

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    BmTypeInput that = (BmTypeInput) o;
    return activePowerGradient.equals(that.activePowerGradient) && etaConv.equals(that.etaConv);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), activePowerGradient, etaConv);
  }

  @Override
  public String toString() {
    return "BmTypeInput{" + "loadGradient=" + activePowerGradient + ", etaConv=" + etaConv + '}';
  }
}
