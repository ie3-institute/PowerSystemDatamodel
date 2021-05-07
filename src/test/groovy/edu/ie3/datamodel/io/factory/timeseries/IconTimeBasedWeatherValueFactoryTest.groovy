/*
 * © 2021. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
 */
package edu.ie3.datamodel.io.factory.timeseries

import edu.ie3.datamodel.models.StandardUnits
import edu.ie3.test.common.CosmoWeatherTestData
import edu.ie3.util.TimeUtil
import edu.ie3.util.quantities.PowerSystemUnits
import edu.ie3.util.quantities.QuantityUtil
import org.locationtech.jts.geom.Point
import spock.lang.Specification
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units

class IconTimeBasedWeatherValueFactoryTest extends Specification {
	def "A time based weather value factory for ICON column scheme determines wind velocity angle correctly"() {
		given:
		def data = new TimeBasedWeatherValueData([
			"u131m": u.toString(),
			"v131m": v.toString(),
		], Mock(Point))
		def expected = Quantities.getQuantity(expectedValue, PowerSystemUnits.DEGREE_GEOM)

		when:
		def actual = IconTimeBasedWeatherValueFactory.getWindDirection(data)

		then:
		actual.getUnit() == StandardUnits.WIND_DIRECTION
		QuantityUtil.isEquivalentAbs(actual, expected, 1E-10.doubleValue())

		where:
		u    | v    || expectedValue
		0.0  | -5.0 || 0.0
		-5.0 | -5.0 || 45.0
		-5.0 | 0.0  || 90.0
		-5.0 | 5.0  || 135.0
		0.0  | 5.0  || 180.0
		5.0  | 5.0  || 225.0
		5.0  | 0.0  || 270.0
		5.0  | -5.0 || 315.0
	}

	def "A time based weather value factory for ICON column scheme determines wind velocity correctly"() {
		given:
		def data = new TimeBasedWeatherValueData([
			"u131m": u.toString(),
			"v131m": v.toString(),
		], Mock(Point))
		def expected = Quantities.getQuantity(expectedValue, PowerSystemUnits.METRE_PER_SECOND)

		when:
		def actual = IconTimeBasedWeatherValueFactory.getWindVelocity(data)

		then:
		actual.getUnit() == StandardUnits.WIND_VELOCITY
		QuantityUtil.isEquivalentAbs(actual, expected, 1E-10.doubleValue())

		where:
		u    | v    | w    || expectedValue
		0.0  | -5.0 | 0.0  || 5.0
		-5.0 | -5.0 | 10.0 || 7.071067811865
		-5.0 | 0.0  | 20.0 || 5.0
		-5.0 | 5.0  | 30.0 || 7.071067811865
		0.0  | 5.0  | 40.0 || 5.0
		5.0  | 5.0  | 50.0 || 7.071067811865
		5.0  | 0.0  | 60.0 || 5.0
		5.0  | -5.0 | 70.0 || 7.071067811865
	}

	def "A time based weather value factory for ICON column scheme builds a single time based value correctly"() {
		given:
		def factory = new IconTimeBasedWeatherValueFactory()
		def coordinate = CosmoWeatherTestData.COORDINATE_67775

		def parameter = [
			"datum"        : "2019-08-01 01:00:00",
			"albRad"      : "13.015240669",
			"asobS"       : "3.555093673828124",
			"aswdifdS"    : "1.8088226191406245",
			"aswdifuS"    : "0.5713421484374998",
			"aswdirS"     : "2.317613203124999",
			"t2m"         : "289.1179319051744",
			"tg"          : "288.4101691197649",
			"u10m"        : "0.3021732864307963",
			"u131m"       : "2.6058700426057797",
			"u20m"        : "0.32384365019387784",
			"u216m"       : "3.9015497418041756",
			"u65m"        : "1.2823686334340363",
			"v10m"        : "1.3852550649486943",
			"v131m"       : "3.8391590569599927",
			"v20m"        : "1.3726831152710628",
			"v216m"       : "4.339362039492466",
			"v65m"        : "2.809877942347672",
			"w131m"       : "-0.02633474740256081",
			"w20m"        : "-0.0100060345167524",
			"w216m"       : "-0.030348050471342078",
			"w65m"        : "-0.01817112027569893",
			"z0"           : "0.955323922526438",
			"coordinateId": "67775",
			"p131m"       : "",
			"p20m"        : "",
			"p65m"        : "",
			"sobsRad"     : "",
			"t131m"       : ""
		]
		def data = new TimeBasedWeatherValueData(parameter, coordinate)

		when:
		def actual = factory.buildModel(data)

		then:
		actual.with {
			assert it.time == TimeUtil.withDefaults.toZonedDateTime("2019-08-01 01:00:00")
			assert it.value.coordinate == coordinate
			assert it.value.solarIrradiance.directIrradiance.present
			assert it.value.solarIrradiance.directIrradiance.get() == Quantities.getQuantity(0.002317613203124999, PowerSystemUnits.KILOWATT_PER_SQUAREMETRE)
			assert it.value.solarIrradiance.diffuseIrradiance.present
			assert it.value.solarIrradiance.diffuseIrradiance.get() == Quantities.getQuantity(0.0018088226191406245, PowerSystemUnits.KILOWATT_PER_SQUAREMETRE)
			assert it.value.temperature.temperature.present
			assert QuantityUtil.isEquivalentAbs(it.value.temperature.temperature.get(), Quantities.getQuantity(15.9679319051744, Units.CELSIUS))
			assert it.value.wind.direction.present
			assert QuantityUtil.isEquivalentAbs(it.value.wind.direction.get(), Quantities.getQuantity(214.16711674907722, PowerSystemUnits.DEGREE_GEOM))
			assert it.value.wind.velocity.present
			assert QuantityUtil.isEquivalentAbs(it.value.wind.velocity.get(), Quantities.getQuantity(4.640010877529081, PowerSystemUnits.METRE_PER_SECOND))
		}
	}
}
