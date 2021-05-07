/*
 * © 2021. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
 */
package edu.ie3.datamodel.io.factory.timeseries

import edu.ie3.datamodel.models.StandardUnits
import edu.ie3.datamodel.models.timeseries.individual.TimeBasedValue
import edu.ie3.datamodel.models.value.WeatherValue
import edu.ie3.test.common.CosmoWeatherTestData
import edu.ie3.util.TimeUtil
import spock.lang.Specification
import tech.units.indriya.quantity.Quantities

class CosmoTimeBasedWeatherValueFactoryTest extends Specification {

	def "A PsdmTimeBasedWeatherValueFactory should be able to create time series with missing values"() {
		given:
		def factory = new CosmoTimeBasedWeatherValueFactory("yyyy-MM-dd HH:mm:ss")
		def coordinate = CosmoWeatherTestData.COORDINATE_193186
		def time = TimeUtil.withDefaults.toZonedDateTime("2019-01-01 00:00:00")

		Map<String, String> parameter = [
			"uuid"             : "980f7714-8def-479f-baae-4deed6c8d6d1",
			"time"             : TimeUtil.withDefaults.toString(time),
			"diffuseirradiance": "282.671997070312",
			"directirradiance" : "286.872985839844",
			"temperature"      : "",
			"winddirection"    : "0",
			"windvelocity"     : "1.66103506088257"
		]

		def data = new TimeBasedWeatherValueData(parameter, coordinate)

		def expectedResults = new TimeBasedValue(UUID.fromString("980f7714-8def-479f-baae-4deed6c8d6d1"),
				time, new WeatherValue(coordinate,
				Quantities.getQuantity(286.872985839844d, StandardUnits.SOLAR_IRRADIANCE),
				Quantities.getQuantity(282.671997070312d, StandardUnits.SOLAR_IRRADIANCE),
				null,
				Quantities.getQuantity(0d, StandardUnits.WIND_DIRECTION),
				Quantities.getQuantity(1.66103506088257d, StandardUnits.WIND_VELOCITY)))

		when:
		def model = factory.buildModel(data)

		then:
		model == expectedResults
	}

	def "A PsdmTimeBasedWeatherValueFactory should be able to create time series values"() {
		given:
		def factory = new CosmoTimeBasedWeatherValueFactory("yyyy-MM-dd HH:mm:ss")
		def coordinate = CosmoWeatherTestData.COORDINATE_193186
		def time = TimeUtil.withDefaults.toZonedDateTime("2019-01-01 00:00:00")

		Map<String, String> parameter = [
			"time"             : TimeUtil.withDefaults.toString(time),
			"uuid"             : "980f7714-8def-479f-baae-4deed6c8d6d1",
			"diffuseirradiance": "282.671997070312",
			"directirradiance" : "286.872985839844",
			"temperature"      : "278.019012451172",
			"winddirection"    : "0",
			"windvelocity"     : "1.66103506088257"
		]

		def data = new TimeBasedWeatherValueData(parameter, coordinate)

		def expectedResults = new TimeBasedValue(UUID.fromString("980f7714-8def-479f-baae-4deed6c8d6d1"),
				time, new WeatherValue(coordinate,
				Quantities.getQuantity(286.872985839844d, StandardUnits.SOLAR_IRRADIANCE),
				Quantities.getQuantity(282.671997070312d, StandardUnits.SOLAR_IRRADIANCE),
				Quantities.getQuantity(278.019012451172d, StandardUnits.TEMPERATURE),
				Quantities.getQuantity(0d, StandardUnits.WIND_DIRECTION),
				Quantities.getQuantity(1.66103506088257d, StandardUnits.WIND_VELOCITY)))

		when:
		def model = factory.buildModel(data)

		then:
		model == expectedResults
	}
}
