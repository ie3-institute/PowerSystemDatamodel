/*
 * © 2021. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
 */
package edu.ie3.datamodel.io.source.sql

import edu.ie3.datamodel.io.connectors.SqlConnector
import edu.ie3.datamodel.io.factory.timeseries.IconTimeBasedWeatherValueFactory
import edu.ie3.datamodel.models.timeseries.individual.IndividualTimeSeries
import edu.ie3.datamodel.models.timeseries.individual.TimeBasedValue
import edu.ie3.datamodel.models.value.WeatherValue
import edu.ie3.test.common.IconWeatherTestData
import edu.ie3.test.helper.WeatherSourceTestHelper
import edu.ie3.util.TimeUtil
import edu.ie3.util.interval.ClosedInterval
import edu.ie3.util.naming.NamingConvention
import org.locationtech.jts.geom.Point
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.spock.Testcontainers
import org.testcontainers.utility.MountableFile
import spock.lang.Shared
import spock.lang.Specification

@Testcontainers
class SqlWeatherSourceIconIT extends Specification implements WeatherSourceTestHelper {

	@Shared
	PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:11.9")

	@Shared
	SqlWeatherSource source

	static String schemaName = "public"
	static String weatherTableName = "weather"
	static NamingConvention namingConvention = NamingConvention.SNAKE

	def setupSpec() {
		// Copy sql import script into docker
		MountableFile sqlImportFile = MountableFile.forClasspathResource("/testcontainersFiles/sql/icon/weather.sql")
		postgreSQLContainer.copyFileToContainer(sqlImportFile, "/home/weather.sql")
		// Execute import script
		postgreSQLContainer.execInContainer("psql", "-Utest", "-f/home/weather.sql")

		def connector = new SqlConnector(postgreSQLContainer.jdbcUrl, postgreSQLContainer.username, postgreSQLContainer.password)
		def weatherFactory = new IconTimeBasedWeatherValueFactory(TimeUtil.withDefaults)
		source = new SqlWeatherSource(connector, IconWeatherTestData.coordinateSource, schemaName, weatherTableName, namingConvention, weatherFactory)
	}

	def "A NativeSqlWeatherSource can read and correctly parse a single value for a specific date and coordinate"() {
		given:
		def expectedTimeBasedValue = new TimeBasedValue(IconWeatherTestData.TIME_15H, IconWeatherTestData.WEATHER_VALUE_67775_15H)
		when:
		def optTimeBasedValue = source.getWeather(IconWeatherTestData.TIME_15H, IconWeatherTestData.COORDINATE_67775)
		then:
		optTimeBasedValue.present
		equalsIgnoreUUID(optTimeBasedValue.get(), expectedTimeBasedValue )
	}

	def "A NativeSqlWeatherSource can read multiple timeseries values for multiple coordinates"() {
		given:
		def coordinates = [
			IconWeatherTestData.COORDINATE_67775,
			IconWeatherTestData.COORDINATE_67776
		]
		def timeInterval = new ClosedInterval(IconWeatherTestData.TIME_16H, IconWeatherTestData.TIME_17H)
		def timeSeries67775 = new IndividualTimeSeries(null,
				[
					new TimeBasedValue(IconWeatherTestData.TIME_16H, IconWeatherTestData.WEATHER_VALUE_67775_16H),
					new TimeBasedValue(IconWeatherTestData.TIME_17H, IconWeatherTestData.WEATHER_VALUE_67775_17H)]
				as Set<TimeBasedValue>)
		def timeSeries67776 = new IndividualTimeSeries(null,
				[
					new TimeBasedValue(IconWeatherTestData.TIME_16H, IconWeatherTestData.WEATHER_VALUE_67776_16H)] as Set<TimeBasedValue>)

		when:
		Map<Point, IndividualTimeSeries<WeatherValue>> coordinateToTimeSeries = source.getWeather(timeInterval, coordinates)

		then:
		coordinateToTimeSeries.keySet().size() == 2
		equalsIgnoreUUID(coordinateToTimeSeries.get(IconWeatherTestData.COORDINATE_67775), timeSeries67775)
		equalsIgnoreUUID(coordinateToTimeSeries.get(IconWeatherTestData.COORDINATE_67776), timeSeries67776)
	}

	def "A NativeSqlWeatherSource can read all weather data in a given time interval"() {
		given:
		def timeInterval = new ClosedInterval(IconWeatherTestData.TIME_15H, IconWeatherTestData.TIME_17H)
		def timeSeries67775 = new IndividualTimeSeries(null,
				[
					new TimeBasedValue(IconWeatherTestData.TIME_15H, IconWeatherTestData.WEATHER_VALUE_67775_15H),
					new TimeBasedValue(IconWeatherTestData.TIME_16H, IconWeatherTestData.WEATHER_VALUE_67775_16H),
					new TimeBasedValue(IconWeatherTestData.TIME_17H, IconWeatherTestData.WEATHER_VALUE_67775_17H)] as Set<TimeBasedValue>)
		def timeSeries67776 = new IndividualTimeSeries(null,
				[
					new TimeBasedValue(IconWeatherTestData.TIME_15H, IconWeatherTestData.WEATHER_VALUE_67776_15H),
					new TimeBasedValue(IconWeatherTestData.TIME_16H, IconWeatherTestData.WEATHER_VALUE_67776_16H)] as Set<TimeBasedValue>)

		when:
		Map<Point, IndividualTimeSeries<WeatherValue>> coordinateToTimeSeries = source.getWeather(timeInterval)

		then:
		coordinateToTimeSeries.keySet().size() == 2
		equalsIgnoreUUID(coordinateToTimeSeries.get(IconWeatherTestData.COORDINATE_67775).entries, timeSeries67775.entries)
		equalsIgnoreUUID(coordinateToTimeSeries.get(IconWeatherTestData.COORDINATE_67776).entries, timeSeries67776.entries)
	}
}
