/*
 * © 2021. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.datamodel.io.source.couchbase;

import com.couchbase.client.core.error.DecodingFailureException;
import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.query.QueryResult;
import edu.ie3.datamodel.io.connectors.CouchbaseConnector;
import edu.ie3.datamodel.io.factory.timeseries.TimeBasedWeatherValueData;
import edu.ie3.datamodel.io.factory.timeseries.TimeBasedWeatherValueFactory;
import edu.ie3.datamodel.io.source.IdCoordinateSource;
import edu.ie3.datamodel.io.source.WeatherSource;
import edu.ie3.datamodel.models.timeseries.individual.IndividualTimeSeries;
import edu.ie3.datamodel.models.timeseries.individual.TimeBasedValue;
import edu.ie3.datamodel.models.value.WeatherValue;
import edu.ie3.util.interval.ClosedInterval;
import edu.ie3.util.naming.NamingConvention;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.locationtech.jts.geom.Point;

/** Couchbase Source for weather data */
public class CouchbaseWeatherSource implements WeatherSource {
  private static final Logger logger = LogManager.getLogger(CouchbaseWeatherSource.class);

  private static final NamingConvention DEFAULT_NAMING_CONVENTION = NamingConvention.FLAT;
  private static final String DEFAULT_TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ssxxx";
  /** The start of the document key, comparable to a table name in relational databases */
  private static final String DEFAULT_KEY_PREFIX = "weather";

  private final TimeBasedWeatherValueFactory weatherFactory;

  private final String keyPrefix;
  private final CouchbaseConnector connector;
  private final IdCoordinateSource coordinateSource;
  private final String coordinateIdColumnName;

  /**
   * Instantiate a weather source utilising a connection to a couchbase instance obtained via the
   * connector. This convenient constructor uses the {@link
   * CouchbaseWeatherSource#DEFAULT_KEY_PREFIX} as key prefix and {@link
   * CouchbaseWeatherSource#DEFAULT_NAMING_CONVENTION} as naming convention.
   *
   * @param connector Connector, that establishes the connection to the couchbase instance
   * @param coordinateSource Source to obtain actual coordinates from
   * @param weatherFactory Factory to transfer field to value mapping into actual java object
   *     instances
   */
  public CouchbaseWeatherSource(
      CouchbaseConnector connector,
      IdCoordinateSource coordinateSource,
      TimeBasedWeatherValueFactory weatherFactory) {
    this(
        connector, coordinateSource, DEFAULT_KEY_PREFIX, DEFAULT_NAMING_CONVENTION, weatherFactory);
  }

  /**
   * Instantiate a weather source utilising a connection to a couchbase instance obtained via the
   * connector. Uses {@link CouchbaseWeatherSource#DEFAULT_NAMING_CONVENTION} as naming convention.
   *
   * @param connector Connector, that establishes the connection to the couchbase instance
   * @param coordinateSource Source to obtain actual coordinates from
   * @param keyPrefix Prefix of entries, that belong to weather
   * @param weatherFactory Factory to transfer field to value mapping into actual java object
   *     instances
   * @deprecated Use {@link CouchbaseWeatherSource#CouchbaseWeatherSource(CouchbaseConnector,
   *     IdCoordinateSource, String, NamingConvention, TimeBasedWeatherValueFactory)} instead
   */
  @Deprecated
  public CouchbaseWeatherSource(
      CouchbaseConnector connector,
      IdCoordinateSource coordinateSource,
      String keyPrefix,
      TimeBasedWeatherValueFactory weatherFactory) {
    this(connector, coordinateSource, keyPrefix, DEFAULT_NAMING_CONVENTION, weatherFactory);
  }

  /**
   * Instantiate a weather source utilising a connection to a couchbase instance obtained via the
   * connector
   *
   * @param connector Connector, that establishes the connection to the couchbase instance
   * @param coordinateSource Source to obtain actual coordinates from
   * @param keyPrefix Prefix of entries, that belong to weather
   * @param namingConvention the (case) convention, how columns are named
   * @param weatherFactory Factory to transfer field to value mapping into actual java object
   *     instances
   */
  public CouchbaseWeatherSource(
      CouchbaseConnector connector,
      IdCoordinateSource coordinateSource,
      String keyPrefix,
      NamingConvention namingConvention,
      TimeBasedWeatherValueFactory weatherFactory) {
    this.connector = connector;
    this.coordinateSource = coordinateSource;
    this.keyPrefix = keyPrefix;
    this.weatherFactory = weatherFactory;
    this.coordinateIdColumnName = weatherFactory.getCoordinateIdFieldString(namingConvention);
  }

  @Override
  public Map<Point, IndividualTimeSeries<WeatherValue>> getWeather(
      ClosedInterval<ZonedDateTime> timeInterval) {
    logger.warn(
        "By not providing coordinates you are forcing couchbase to check all possible coordinates one by one."
            + " This is not very performant. Please consider providing specific coordinates instead.");
    return getWeather(timeInterval, coordinateSource.getAllCoordinates());
  }

  @Override
  public Map<Point, IndividualTimeSeries<WeatherValue>> getWeather(
      ClosedInterval<ZonedDateTime> timeInterval, Collection<Point> coordinates) {
    HashMap<Point, IndividualTimeSeries<WeatherValue>> coordinateToTimeSeries = new HashMap<>();
    for (Point coordinate : coordinates) {
      Optional<Integer> coordinateId = coordinateSource.getId(coordinate);
      if (coordinateId.isPresent()) {
        String query = createQueryStringForIntervalAndCoordinate(timeInterval, coordinateId.get());
        CompletableFuture<QueryResult> futureResult = connector.query(query);
        QueryResult queryResult = futureResult.join();
        List<JsonObject> jsonWeatherInputs = Collections.emptyList();
        try {
          jsonWeatherInputs = queryResult.rowsAsObject();
        } catch (DecodingFailureException ex) {
          logger.error(ex);
        }
        if (jsonWeatherInputs != null && !jsonWeatherInputs.isEmpty()) {
          Set<TimeBasedValue<WeatherValue>> weatherInputs =
              jsonWeatherInputs.stream()
                  .map(this::toTimeBasedWeatherValue)
                  .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
                  .collect(Collectors.toSet());
          IndividualTimeSeries<WeatherValue> weatherTimeSeries =
              new IndividualTimeSeries<>(null, weatherInputs);
          coordinateToTimeSeries.put(coordinate, weatherTimeSeries);
        }
      } else logger.warn("Unable to match coordinate {} to a coordinate ID", coordinate);
    }
    return coordinateToTimeSeries;
  }

  @Override
  public Optional<TimeBasedValue<WeatherValue>> getWeather(ZonedDateTime date, Point coordinate) {
    Optional<Integer> coordinateId = coordinateSource.getId(coordinate);
    if (!coordinateId.isPresent()) {
      logger.warn("Unable to match coordinate {} to a coordinate ID", coordinate);
      return Optional.empty();
    }
    try {
      CompletableFuture<GetResult> futureResult =
          connector.get(generateWeatherKey(date, coordinateId.get()));
      GetResult getResult = futureResult.join();
      JsonObject jsonWeatherInput = getResult.contentAsObject();
      return toTimeBasedWeatherValue(jsonWeatherInput);
    } catch (DecodingFailureException ex) {
      logger.error(ex);
      return Optional.empty();
    } catch (DocumentNotFoundException ex) {
      return Optional.empty();
    } catch (CompletionException ex) {
      if (ex.getCause() instanceof DocumentNotFoundException) return Optional.empty();
      else throw ex;
    }
  }

  /**
   * Generates a key for weather documents with the pattern: {@code
   * weather::<coordinate_id>::<time>}
   *
   * @param time the timestamp for the weather data
   * @param coordinateId the coordinate Id of the weather data
   * @return a weather document key
   */
  public String generateWeatherKey(ZonedDateTime time, Integer coordinateId) {
    String key = keyPrefix + "::";
    key += coordinateId + "::";
    key += time.format(DateTimeFormatter.ofPattern(DEFAULT_TIMESTAMP_PATTERN));
    return key;
  }

  /**
   * Create a query string to search for documents for the given coordinate in the given time
   * interval by querying a range of document keys
   *
   * @param timeInterval the time interval for which the documents are queried
   * @param coordinateId the coordinate ID for which the documents are queried
   * @return the query string
   */
  public String createQueryStringForIntervalAndCoordinate(
      ClosedInterval<ZonedDateTime> timeInterval, int coordinateId) {
    String basicQuery =
        "SELECT " + connector.getBucketName() + ".* FROM " + connector.getBucketName();
    String whereClause =
        " WHERE META().id >= '" + generateWeatherKey(timeInterval.getLower(), coordinateId);
    whereClause +=
        "' AND META().id <= '" + generateWeatherKey(timeInterval.getUpper(), coordinateId) + "'";
    return basicQuery + whereClause;
  }

  /**
   * Converts a JsonObject into TimeBasedWeatherValueData by extracting all fields into a field to
   * value map and then removing the coordinate from it to supply as a parameter
   *
   * @param jsonObj the JsonObject to convert
   * @return the Data object
   */
  private Optional<TimeBasedWeatherValueData> toTimeBasedWeatherValueData(JsonObject jsonObj) {
    Integer coordinateId = jsonObj.getInt(coordinateIdColumnName);
    jsonObj.removeKey(coordinateIdColumnName);
    Optional<Point> coordinate = coordinateSource.getCoordinate(coordinateId);
    if (!coordinate.isPresent()) {
      logger.warn("Unable to match coordinate ID {} to a coordinate", coordinateId);
      return Optional.empty();
    }
    Map<String, String> fieldToValueMap =
        jsonObj.toMap().entrySet().stream()
            .collect(
                Collectors.toMap(Map.Entry::getKey, entry -> String.valueOf(entry.getValue())));
    fieldToValueMap.putIfAbsent("uuid", UUID.randomUUID().toString());
    return Optional.of(new TimeBasedWeatherValueData(fieldToValueMap, coordinate.get()));
  }

  /**
   * Converts a JsonObject into a time based weather value by converting it to a
   * TimeBasedWeatherValueData first, then using the TimeBasedWeatherValueFactory to create an
   * entity
   *
   * @param jsonObj the JsonObject to convert
   * @return an optional weather value
   */
  public Optional<TimeBasedValue<WeatherValue>> toTimeBasedWeatherValue(JsonObject jsonObj) {
    Optional<TimeBasedWeatherValueData> data = toTimeBasedWeatherValueData(jsonObj);
    if (!data.isPresent()) {
      logger.warn("Unable to parse json object");
      logger.debug("The following json could not be parsed:\n{}", jsonObj);
      return Optional.empty();
    }
    TimeBasedValue<WeatherValue> timeBasedValue = weatherFactory.get(data.get()).orElse(null);
    return Optional.ofNullable(timeBasedValue);
  }
}
