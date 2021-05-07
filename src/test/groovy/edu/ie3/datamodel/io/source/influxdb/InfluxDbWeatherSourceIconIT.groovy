/*
 * © 2021. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
 */
package edu.ie3.datamodel.io.source.influxdb

import edu.ie3.datamodel.io.connectors.InfluxDbConnector
import edu.ie3.datamodel.io.factory.timeseries.CosmoTimeBasedWeatherValueFactory
import edu.ie3.datamodel.io.factory.timeseries.IconTimeBasedWeatherValueFactory
import edu.ie3.datamodel.models.timeseries.individual.IndividualTimeSeries
import edu.ie3.datamodel.models.timeseries.individual.TimeBasedValue
import edu.ie3.datamodel.models.value.WeatherValue
import edu.ie3.test.common.IconWeatherTestData
import edu.ie3.test.helper.WeatherSourceTestHelper
import edu.ie3.util.geo.GeoUtils
import edu.ie3.util.interval.ClosedInterval
import edu.ie3.util.naming.NamingConvention
import org.locationtech.jts.geom.Point
import org.testcontainers.containers.InfluxDBContainer
import org.testcontainers.spock.Testcontainers
import org.testcontainers.utility.MountableFile
import spock.lang.Shared
import spock.lang.Specification

@Testcontainers
class InfluxDbWeatherSourceIconIT extends Specification implements WeatherSourceTestHelper {

	@Shared
	InfluxDBContainer influxDbContainer = new InfluxDBContainer("1.8.4")
	.withAuthEnabled(false)
	.withDatabase("test_weather")

	@Shared
	InfluxDbWeatherSource source

	def setupSpec() {
		// Copy import file into docker and then import it via influx CLI
		// more information on file format and usage here: https://docs.influxdata.com/influxdb/v1.7/tools/shell/#import-data-from-a-file-with-import
		MountableFile influxWeatherImportFile = MountableFile.forClasspathResource("/testcontainersFiles/influxDb/icon/weather.txt")
		influxDbContainer.copyFileToContainer(influxWeatherImportFile, "/home/weather.txt")
		def execResult = influxDbContainer.execInContainer("influx", "-import", "-path=/home/weather.txt", "-precision=ms")
		println "Command \"influx -import -path=/home/weather.txt -precision=ms\" returned:"
		if(!execResult.stderr.isEmpty()) println execResult.getStderr()
		if(!execResult.stdout.isEmpty()) println execResult.getStdout()

		def connector = new InfluxDbConnector(influxDbContainer.url,"test_weather", "test_scenario")
		def weatherFactory = new IconTimeBasedWeatherValueFactory("yyyy-MM-dd'T'HH:mm:ss[.S[S][S]]'Z'")
		source = new InfluxDbWeatherSource(connector, IconWeatherTestData.coordinateSource, NamingConvention.SNAKE, weatherFactory)
	}

	def "The test container can establish a valid connection"() {
		when:
		def connector = new InfluxDbConnector(influxDbContainer.url,"test_weather", "test_scenario")
		then:
		connector.connectionValid
	}

	def "An InfluxDbWeatherSource can read and correctly parse a single value for a specific date and coordinate"() {
		given:
		def expectedTimeBasedValue = new TimeBasedValue(IconWeatherTestData.TIME_15H , IconWeatherTestData.WEATHER_VALUE_67775_15H)

		when:
		def optTimeBasedValue = source.getWeather(IconWeatherTestData.TIME_15H , IconWeatherTestData.COORDINATE_67775)

		then:
		optTimeBasedValue.present
		equalsIgnoreUUID(optTimeBasedValue.get(), expectedTimeBasedValue)
	}

	def "An InfluxDbWeatherSource can read multiple time series values for multiple coordinates"() {
		given:
		def coordinates = [
			IconWeatherTestData.COORDINATE_67775,
			IconWeatherTestData.COORDINATE_67776
		]
		def timeInterval = new ClosedInterval(IconWeatherTestData.TIME_16H , IconWeatherTestData.TIME_17H)
		def timeseries67775 = new IndividualTimeSeries(null,
				[
					new TimeBasedValue(IconWeatherTestData.TIME_16H , IconWeatherTestData.WEATHER_VALUE_67775_16H),
					new TimeBasedValue(IconWeatherTestData.TIME_17H , IconWeatherTestData.WEATHER_VALUE_67775_17H)]
				as Set<TimeBasedValue>)
		def timeseries67776 = new IndividualTimeSeries(null,
				[
					new TimeBasedValue(IconWeatherTestData.TIME_16H , IconWeatherTestData.WEATHER_VALUE_67776_16H)] as Set<TimeBasedValue>)

		when:
		def coordinateToTimeSeries = source.getWeather(timeInterval, coordinates)

		then:
		coordinateToTimeSeries.keySet().size() == 2
		equalsIgnoreUUID(coordinateToTimeSeries.get(IconWeatherTestData.COORDINATE_67775), timeseries67775)
		equalsIgnoreUUID(coordinateToTimeSeries.get(IconWeatherTestData.COORDINATE_67776), timeseries67776)
	}

	def "An InfluxDbWeatherSource can read all weather data in a given time interval"() {
		given:
		def timeInterval = new ClosedInterval(IconWeatherTestData.TIME_15H , IconWeatherTestData.TIME_17H)
		def timeseries_67775 = new IndividualTimeSeries(null,
				[
					new TimeBasedValue(IconWeatherTestData.TIME_15H ,IconWeatherTestData.WEATHER_VALUE_67775_15H),
					new TimeBasedValue(IconWeatherTestData.TIME_16H ,IconWeatherTestData.WEATHER_VALUE_67775_16H),
					new TimeBasedValue(IconWeatherTestData.TIME_17H ,IconWeatherTestData.WEATHER_VALUE_67775_17H)] as Set<TimeBasedValue>)
		def timeseries_67776 = new IndividualTimeSeries(null,
				[
					new TimeBasedValue(IconWeatherTestData.TIME_15H ,IconWeatherTestData.WEATHER_VALUE_67776_15H),
					new TimeBasedValue(IconWeatherTestData.TIME_16H ,IconWeatherTestData.WEATHER_VALUE_67776_16H)] as Set<TimeBasedValue>)

		when:
		def coordinateToTimeSeries = source.getWeather(timeInterval)

		then:
		coordinateToTimeSeries.keySet().size() == 2
		equalsIgnoreUUID(coordinateToTimeSeries.get(IconWeatherTestData.COORDINATE_67775).getEntries(), timeseries_67775.getEntries())
		equalsIgnoreUUID(coordinateToTimeSeries.get(IconWeatherTestData.COORDINATE_67776).getEntries(), timeseries_67776.getEntries())
	}

	def "An InfluxDbWeatherSource will return an equivalent to 'empty' when being unable to map a coordinate to it's ID"() {
		def validCoordinate = IconWeatherTestData.COORDINATE_67775
		def invalidCoordinate = GeoUtils.xyToPoint(48d, 7d)
		def time = IconWeatherTestData.TIME_15H
		def timeInterval = new ClosedInterval(IconWeatherTestData.TIME_15H , IconWeatherTestData.TIME_17H)
		def emptyTimeSeries = new IndividualTimeSeries(UUID.randomUUID(), Collections.emptySet())
		def timeseries_67775 = new IndividualTimeSeries(null,
				[
					new TimeBasedValue(IconWeatherTestData.TIME_15H ,IconWeatherTestData.WEATHER_VALUE_67775_15H),
					new TimeBasedValue(IconWeatherTestData.TIME_16H ,IconWeatherTestData.WEATHER_VALUE_67775_16H),
					new TimeBasedValue(IconWeatherTestData.TIME_17H ,IconWeatherTestData.WEATHER_VALUE_67775_17H)] as Set<TimeBasedValue>)

		when:
		def coordinateAtDate = source.getWeather(time, invalidCoordinate)
		def coordinateInInterval = source.getWeather(timeInterval, invalidCoordinate)
		def coordinatesToTimeSeries = source.getWeather(timeInterval, [
			validCoordinate,
			invalidCoordinate
		])

		then:
		coordinateAtDate == Optional.empty()
		equalsIgnoreUUID(coordinateInInterval, emptyTimeSeries)
		coordinatesToTimeSeries.keySet() == [validCoordinate].toSet()
		equalsIgnoreUUID(coordinatesToTimeSeries.get(validCoordinate), timeseries_67775)
	}
}
