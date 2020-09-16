/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.datamodel.models.value;

import edu.ie3.datamodel.models.StandardUnits;
import edu.ie3.util.quantities.QuantityUtil;
import edu.ie3.util.quantities.interfaces.EnergyPrice;
import java.util.Objects;
import tech.units.indriya.ComparableQuantity;

/** Describes a price for energy */
public class EnergyPriceValue implements Value {
  /** Price of energy (typically in €/MWh) */
  private final ComparableQuantity<EnergyPrice> price;

  /** @param price per MWh */
  public EnergyPriceValue(ComparableQuantity<EnergyPrice> price) {
    this.price = price.to(StandardUnits.ENERGY_PRICE);
  }

  public ComparableQuantity<EnergyPrice> getPrice() {
    return price;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    EnergyPriceValue that = (EnergyPriceValue) o;
    if (!QuantityUtil.quantityIsEmpty(price)) {
      if (QuantityUtil.quantityIsEmpty(that.price)) return false;
      return price.isEquivalentTo(that.price);
    } else return QuantityUtil.quantityIsEmpty(that.price);
  }

  @Override
  public int hashCode() {
    return Objects.hash(price);
  }

  @Override
  public String toString() {
    return "EnergyPriceValue{" + "price=" + price + '}';
  }
}
