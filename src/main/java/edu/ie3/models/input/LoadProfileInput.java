/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.models.input;

import edu.ie3.models.LoadProfileType;
import edu.ie3.models.timeseries.RepetitiveTimeSeries;
import edu.ie3.models.value.PowerValue;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.Map;

// TODO This is a sample implementation, please implement a real scenario
public class LoadProfileInput extends RepetitiveTimeSeries<PowerValue> {

  private final LoadProfileType type;
  private final Map<DayOfWeek, Map<Integer, PowerValue>> dayOfWeekToHourlyValues;

  public LoadProfileInput(
      LoadProfileType type, Map<DayOfWeek, Map<Integer, PowerValue>> dayOfWeekToHourlyValues) {
    this.type = type;
    this.dayOfWeekToHourlyValues = dayOfWeekToHourlyValues;
  }

  @Override
  public PowerValue calc(ZonedDateTime time) {
    return dayOfWeekToHourlyValues.get(time.getDayOfWeek()).get(time.getHour());
  }

  public LoadProfileType getType() {
    return type;
  }
}