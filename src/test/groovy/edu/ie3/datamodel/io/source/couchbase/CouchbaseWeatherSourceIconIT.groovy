/*
 * © 2021. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
 */
package edu.ie3.datamodel.io.source.couchbase

import edu.ie3.datamodel.io.connectors.CouchbaseConnector
import edu.ie3.datamodel.io.factory.timeseries.IconTimeBasedWeatherValueFactory
import edu.ie3.datamodel.io.factory.timeseries.TimeBasedWeatherValueFactory
import edu.ie3.datamodel.models.timeseries.individual.IndividualTimeSeries
import edu.ie3.datamodel.models.timeseries.individual.TimeBasedValue
import edu.ie3.test.common.IconWeatherTestData
import edu.ie3.test.helper.WeatherSourceTestHelper
import edu.ie3.util.TimeUtil
import edu.ie3.util.interval.ClosedInterval
import org.testcontainers.couchbase.BucketDefinition
import org.testcontainers.couchbase.CouchbaseContainer
import org.testcontainers.spock.Testcontainers
import org.testcontainers.utility.MountableFile
import spock.lang.Shared
import spock.lang.Specification

import java.time.ZoneId

@Testcontainers
class CouchbaseWeatherSourceIconIT extends Specification implements WeatherSourceTestHelper {

	@Shared
	BucketDefinition bucketDefinition = new BucketDefinition("ie3_in")

	@Shared
	CouchbaseContainer couchbaseContainer = new CouchbaseContainer("couchbase/server:6.0.2").withBucket(bucketDefinition)
	.withExposedPorts(8091, 8092, 8093, 8094, 11210)

	@Shared
	CouchbaseWeatherSource source

	static String coordinateIdColumnName = TimeBasedWeatherValueFactory.COORDINATE_ID_NAMING.flatCase()

	def setupSpec() {
		// Copy import file with json array of documents into docker
		def couchbaseWeatherJsonsFile = MountableFile.forClasspathResource("/testcontainersFiles/couchbase/icon/weather.json")
		couchbaseContainer.copyFileToContainer(couchbaseWeatherJsonsFile, "/home/weather.json")

		// create an index for the document keys
		couchbaseContainer.execInContainer("cbq",
				"-e", "http://localhost:8093",
				"-u", couchbaseContainer.username,
				"-p", couchbaseContainer.password,
				"-s", "CREATE index id_idx ON `" + bucketDefinition.name + "` (META().id);")

		//import the json documents from the copied file
		couchbaseContainer.execInContainer("cbimport", "json",
				"-cluster", "http://localhost:8091",
				"--bucket", "ie3_in",
				"--username", couchbaseContainer.username,
				"--password", couchbaseContainer.password,
				"--format", "list",
				"--generate-key", "weather::%" + coordinateIdColumnName + "%::%time%",
				"--dataset", "file:///home/weather.json")

		def connector = new CouchbaseConnector(couchbaseContainer.connectionString, bucketDefinition.name, couchbaseContainer.username, couchbaseContainer.password)
		def dtfPattern = "yyyy-MM-dd'T'HH:mm:ssxxx"
		def weatherFactory = new IconTimeBasedWeatherValueFactory(new TimeUtil(ZoneId.of("UTC"), Locale.GERMANY, dtfPattern))
		source = new CouchbaseWeatherSource(connector, IconWeatherTestData.coordinateSource, weatherFactory, dtfPattern)
	}

	def "The test container can establish a valid connection"() {
		when:
		def connector = new CouchbaseConnector(couchbaseContainer.connectionString, bucketDefinition.name, couchbaseContainer.username, couchbaseContainer.password)

		then:
		connector.connectionValid
	}

	def "A CouchbaseWeatherSource can read and correctly parse a single value for a specific date and coordinate"() {
		given:
		def expectedTimeBasedValue = new TimeBasedValue(IconWeatherTestData.TIME_15H, IconWeatherTestData.WEATHER_VALUE_67775_15H)

		when:
		def optTimeBasedValue = source.getWeather(IconWeatherTestData.TIME_15H, IconWeatherTestData.COORDINATE_67775)

		then:
		optTimeBasedValue.present
		equalsIgnoreUUID(optTimeBasedValue.get(), expectedTimeBasedValue)
	}

	def "A CouchbaseWeatherSource can read multiple time series values for multiple coordinates"() {
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
		def coordinateToTimeSeries = source.getWeather(timeInterval, coordinates)

		then:
		coordinateToTimeSeries.keySet().size() == 2
		equalsIgnoreUUID(coordinateToTimeSeries.get(IconWeatherTestData.COORDINATE_67775), timeSeries67775)
		equalsIgnoreUUID(coordinateToTimeSeries.get(IconWeatherTestData.COORDINATE_67776), timeSeries67776)
	}

	def "A CouchbaseWeatherSource can read all weather data in a given time interval"() {
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
		def coordinateToTimeSeries = source.getWeather(timeInterval)

		then:
		coordinateToTimeSeries.keySet().size() == 2
		equalsIgnoreUUID(coordinateToTimeSeries.get(IconWeatherTestData.COORDINATE_67775).entries, timeSeries67775.entries)
		equalsIgnoreUUID(coordinateToTimeSeries.get(IconWeatherTestData.COORDINATE_67776).entries, timeSeries67776.entries)
	}
}
