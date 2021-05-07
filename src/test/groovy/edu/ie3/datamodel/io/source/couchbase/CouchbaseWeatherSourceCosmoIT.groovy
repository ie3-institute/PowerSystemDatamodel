/*
 * © 2021. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
 */
package edu.ie3.datamodel.io.source.couchbase

import edu.ie3.datamodel.io.connectors.CouchbaseConnector
import edu.ie3.datamodel.io.factory.timeseries.CosmoTimeBasedWeatherValueFactory
import edu.ie3.datamodel.io.factory.timeseries.TimeBasedWeatherValueFactory
import edu.ie3.datamodel.models.timeseries.individual.IndividualTimeSeries
import edu.ie3.datamodel.models.timeseries.individual.TimeBasedValue
import edu.ie3.datamodel.models.value.WeatherValue
import edu.ie3.test.common.CosmoWeatherTestData
import edu.ie3.test.helper.WeatherSourceTestHelper
import edu.ie3.util.TimeUtil
import edu.ie3.util.interval.ClosedInterval
import org.locationtech.jts.geom.Point
import org.testcontainers.couchbase.BucketDefinition
import org.testcontainers.couchbase.CouchbaseContainer
import org.testcontainers.spock.Testcontainers
import org.testcontainers.utility.MountableFile
import spock.lang.Shared
import spock.lang.Specification

import java.time.ZoneId

@Testcontainers
class CouchbaseWeatherSourceCosmoIT extends Specification implements WeatherSourceTestHelper {

	@Shared
	BucketDefinition bucketDefinition = new BucketDefinition("ie3_in")

	@Shared
	CouchbaseContainer couchbaseContainer = new CouchbaseContainer("couchbase/server:latest").withBucket(bucketDefinition)
	.withExposedPorts(8091, 8092, 8093, 8094, 11210)

	@Shared
	CouchbaseWeatherSource source

	static String coordinateIdColumnName = TimeBasedWeatherValueFactory.COORDINATE_ID_NAMING.flatCase()

	def setupSpec() {
		// Copy import file with json array of documents into docker
		MountableFile couchbaseWeatherJsonsFile = MountableFile.forClasspathResource("/testcontainersFiles/couchbase/cosmo/weather.json")
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
		def weatherFactory = new CosmoTimeBasedWeatherValueFactory(new TimeUtil(ZoneId.of("UTC"), Locale.GERMANY, "yyyy-MM-dd'T'HH:mm:ssxxx"))
		source = new CouchbaseWeatherSource(connector, CosmoWeatherTestData.coordinateSource, weatherFactory)
	}

	def "The test container can establish a valid connection"() {
		when:
		def connector = new CouchbaseConnector(couchbaseContainer.connectionString, bucketDefinition.name, couchbaseContainer.username, couchbaseContainer.password)
		then:
		connector.connectionValid
	}

	def "A CouchbaseWeatherSource can read and correctly parse a single value for a specific date and coordinate"() {
		given:
		def expectedTimeBasedValue = new TimeBasedValue(CosmoWeatherTestData.TIME_15H, CosmoWeatherTestData.WEATHER_VALUE_193186_15H)

		when:
		def optTimeBasedValue = source.getWeather(CosmoWeatherTestData.TIME_15H, CosmoWeatherTestData.COORDINATE_193186)

		then:
		optTimeBasedValue.present
		equalsIgnoreUUID(optTimeBasedValue.get(), expectedTimeBasedValue)
	}

	def "A CouchbaseWeatherSource can read multiple time series values for multiple coordinates"() {
		given:
		def coordinates = [
			CosmoWeatherTestData.COORDINATE_193186,
			CosmoWeatherTestData.COORDINATE_193187
		]
		def timeInterval = new ClosedInterval(CosmoWeatherTestData.TIME_16H, CosmoWeatherTestData.TIME_17H)
		def timeSeries193186 = new IndividualTimeSeries(null,
				[
					new TimeBasedValue(CosmoWeatherTestData.TIME_16H, CosmoWeatherTestData.WEATHER_VALUE_193186_16H),
					new TimeBasedValue(CosmoWeatherTestData.TIME_17H, CosmoWeatherTestData.WEATHER_VALUE_193186_17H)]
				as Set<TimeBasedValue>)
		def timeSeries193187 = new IndividualTimeSeries(null,
				[
					new TimeBasedValue(CosmoWeatherTestData.TIME_16H, CosmoWeatherTestData.WEATHER_VALUE_193187_16H)] as Set<TimeBasedValue>)
		when:
		Map<Point, IndividualTimeSeries<WeatherValue>> coordinateToTimeSeries = source.getWeather(timeInterval, coordinates)
		then:
		coordinateToTimeSeries.keySet().size() == 2
		equalsIgnoreUUID(coordinateToTimeSeries.get(CosmoWeatherTestData.COORDINATE_193186), timeSeries193186)
		equalsIgnoreUUID(coordinateToTimeSeries.get(CosmoWeatherTestData.COORDINATE_193187), timeSeries193187)
	}



	def "A CouchbaseWeatherSource can read all weather data in a given time interval"() {
		given:
		def timeInterval = new ClosedInterval(CosmoWeatherTestData.TIME_15H, CosmoWeatherTestData.TIME_17H)
		def timeSeries193186 = new IndividualTimeSeries(null,
				[
					new TimeBasedValue(CosmoWeatherTestData.TIME_15H, CosmoWeatherTestData.WEATHER_VALUE_193186_15H),
					new TimeBasedValue(CosmoWeatherTestData.TIME_16H, CosmoWeatherTestData.WEATHER_VALUE_193186_16H),
					new TimeBasedValue(CosmoWeatherTestData.TIME_17H, CosmoWeatherTestData.WEATHER_VALUE_193186_17H)] as Set<TimeBasedValue>)
		def timeSeries193187 = new IndividualTimeSeries(null,
				[
					new TimeBasedValue(CosmoWeatherTestData.TIME_15H, CosmoWeatherTestData.WEATHER_VALUE_193187_15H),
					new TimeBasedValue(CosmoWeatherTestData.TIME_16H, CosmoWeatherTestData.WEATHER_VALUE_193187_16H)] as Set<TimeBasedValue>)
		def timeSeries193188 = new IndividualTimeSeries(null,
				[
					new TimeBasedValue(CosmoWeatherTestData.TIME_15H, CosmoWeatherTestData.WEATHER_VALUE_193188_15H)] as Set<TimeBasedValue>)
		when:
		Map<Point, IndividualTimeSeries<WeatherValue>> coordinateToTimeSeries = source.getWeather(timeInterval)
		then:
		coordinateToTimeSeries.keySet().size() == 3
		equalsIgnoreUUID(coordinateToTimeSeries.get(CosmoWeatherTestData.COORDINATE_193186).entries, timeSeries193186.entries)
		equalsIgnoreUUID(coordinateToTimeSeries.get(CosmoWeatherTestData.COORDINATE_193187).entries, timeSeries193187.entries)
		equalsIgnoreUUID(coordinateToTimeSeries.get(CosmoWeatherTestData.COORDINATE_193188).entries, timeSeries193188.entries)
	}
}
