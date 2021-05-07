/*
 * © 2021. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
 */
package edu.ie3.test.common

import edu.ie3.datamodel.models.StandardUnits
import edu.ie3.datamodel.models.value.WeatherValue
import tech.units.indriya.quantity.Quantities

import java.time.ZoneId
import java.time.ZonedDateTime

class CosmoWeatherTestData extends WeatherTestData {

	public static final ZonedDateTime TIME_15H = ZonedDateTime.of(2020, 04, 28, 15, 0, 0, 0, ZoneId.of("UTC"))
	public static final ZonedDateTime TIME_16H = ZonedDateTime.of(2020, 04, 28, 16, 0, 0, 0, ZoneId.of("UTC"))
	public static final ZonedDateTime TIME_17H = ZonedDateTime.of(2020, 04, 28, 17, 0, 0, 0, ZoneId.of("UTC"))


	public static final WeatherValue WEATHER_VALUE_193186_15H = new WeatherValue(
	COORDINATE_193186,
	Quantities.getQuantity(282.671997070312d, StandardUnits.SOLAR_IRRADIANCE),
	Quantities.getQuantity(286.872985839844d, StandardUnits.SOLAR_IRRADIANCE),
	Quantities.getQuantity(278.019012451172d, StandardUnits.TEMPERATURE),
	Quantities.getQuantity(0d, StandardUnits.WIND_DIRECTION),
	Quantities.getQuantity(1.66103506088257d, StandardUnits.WIND_VELOCITY)
	)
	public static final WeatherValue WEATHER_VALUE_193186_16H = new WeatherValue(
	COORDINATE_193186,
	Quantities.getQuantity(282.672d, StandardUnits.SOLAR_IRRADIANCE),
	Quantities.getQuantity(286.872d, StandardUnits.SOLAR_IRRADIANCE),
	Quantities.getQuantity(278.012d, StandardUnits.TEMPERATURE),
	Quantities.getQuantity(0d, StandardUnits.WIND_DIRECTION),
	Quantities.getQuantity(1.662d, StandardUnits.WIND_VELOCITY)
	)
	public static final WeatherValue WEATHER_VALUE_193186_17H = new WeatherValue(
	COORDINATE_193186,
	Quantities.getQuantity(282.673d, StandardUnits.SOLAR_IRRADIANCE),
	Quantities.getQuantity(286.873d, StandardUnits.SOLAR_IRRADIANCE),
	Quantities.getQuantity(278.013d, StandardUnits.TEMPERATURE),
	Quantities.getQuantity(0d, StandardUnits.WIND_DIRECTION),
	Quantities.getQuantity(1.663d, StandardUnits.WIND_VELOCITY)
	)

	public static final WeatherValue WEATHER_VALUE_193187_15H = new WeatherValue(
	COORDINATE_193187,
	Quantities.getQuantity(283.671997070312d, StandardUnits.SOLAR_IRRADIANCE),
	Quantities.getQuantity(287.872985839844d, StandardUnits.SOLAR_IRRADIANCE),
	Quantities.getQuantity(279.019012451172d, StandardUnits.TEMPERATURE),
	Quantities.getQuantity(0d, StandardUnits.WIND_DIRECTION),
	Quantities.getQuantity(1.76103506088257d, StandardUnits.WIND_VELOCITY)
	)
	public static final WeatherValue WEATHER_VALUE_193187_16H = new WeatherValue(
	COORDINATE_193187,
	Quantities.getQuantity(283.672d, StandardUnits.SOLAR_IRRADIANCE),
	Quantities.getQuantity(287.872d, StandardUnits.SOLAR_IRRADIANCE),
	Quantities.getQuantity(279.012d, StandardUnits.TEMPERATURE),
	Quantities.getQuantity(0d, StandardUnits.WIND_DIRECTION),
	Quantities.getQuantity(1.762d, StandardUnits.WIND_VELOCITY)
	)

	public static final WeatherValue WEATHER_VALUE_193188_15H = new WeatherValue(
	COORDINATE_193188,
	Quantities.getQuantity(284.671997070312d, StandardUnits.SOLAR_IRRADIANCE),
	Quantities.getQuantity(288.872985839844d, StandardUnits.SOLAR_IRRADIANCE),
	Quantities.getQuantity(280.019012451172d, StandardUnits.TEMPERATURE),
	Quantities.getQuantity(0d, StandardUnits.WIND_DIRECTION),
	Quantities.getQuantity(1.86103506088257d, StandardUnits.WIND_VELOCITY)
	)
}
