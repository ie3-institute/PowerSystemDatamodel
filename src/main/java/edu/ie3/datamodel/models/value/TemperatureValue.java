/*
 * © 2021. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.datamodel.models.value;

import edu.ie3.datamodel.models.StandardUnits;
import java.util.Objects;
import java.util.Optional;
import javax.measure.quantity.Temperature;
import tech.units.indriya.ComparableQuantity;

/** Describes a temperature value */
public class TemperatureValue implements Value {
  /** Temperature (typically in K) */
  private final ComparableQuantity<Temperature> temperature;

  /** @param temperature (typically in K) */
  public TemperatureValue(ComparableQuantity<Temperature> temperature) {
    this.temperature = temperature == null ? null : temperature.to(StandardUnits.TEMPERATURE);
  }

  public Optional<ComparableQuantity<Temperature>> getTemperature() {
    return Optional.ofNullable(temperature);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TemperatureValue that = (TemperatureValue) o;
    return Objects.equals(temperature, that.temperature);
  }

  @Override
  public int hashCode() {
    return Objects.hash(temperature);
  }

  @Override
  public String toString() {
    return "TemperatureValue{" + "temperature=" + temperature + '}';
  }
}
