/*
 * © 2021. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.datamodel.io.factory.timeseries;

import edu.ie3.datamodel.models.value.WeatherValue;
import edu.ie3.util.TimeUtil;
import edu.ie3.util.naming.Naming;
import edu.ie3.util.naming.NamingConvention;
import java.time.ZoneId;
import java.util.*;

/**
 * Abstract factory to handle the conversion from "flat" field to value mapping onto actual {@link
 * TimeBasedValueFactory} with {@link WeatherValue}
 */
public abstract class TimeBasedWeatherValueFactory
    extends TimeBasedValueFactory<TimeBasedWeatherValueData, WeatherValue> {
  protected static final String UUID = "uuid";
  protected static final String TIME = "time";
  /* Hold a case agnostic representation of the composite word "coordinate id", that allows for later case conversion */
  protected static final Naming COORDINATE_ID_NAMING = Naming.from("coordinate", "id");

  protected final TimeUtil timeUtil;

  protected TimeBasedWeatherValueFactory() {
    this("yyyy-MM-dd'T'HH:mm:ss[.S[S][S]]'Z'");
  }

  protected TimeBasedWeatherValueFactory(String timePattern) {
    this(new TimeUtil(ZoneId.of("UTC"), Locale.GERMANY, timePattern));
  }

  protected TimeBasedWeatherValueFactory(TimeUtil timeUtil) {
    super(WeatherValue.class);
    this.timeUtil = timeUtil;
  }

  /**
   * Return the field name for the coordinate id in flat case.
   *
   * @return the field name for the coordinate id
   */
  public String getCoordinateIdFieldString() {
    return COORDINATE_ID_NAMING.flatCase();
  }

  /**
   * Return the field name for the coordinate id in desired case.
   *
   * @param convention The desired naming convention / casing
   * @return the field name for the coordinate id in appropriate case
   */
  public String getCoordinateIdFieldString(NamingConvention convention) {
    return COORDINATE_ID_NAMING.as(convention);
  }

  /**
   * Return the field name for the date time
   *
   * @return the field name for the date time
   */
  public abstract String getTimeFieldString();
}
