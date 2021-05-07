/*
 * © 2021. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.datamodel.io.source.influxdb;

import edu.ie3.datamodel.io.connectors.InfluxDbConnector;
import edu.ie3.datamodel.io.factory.timeseries.TimeBasedWeatherValueData;
import edu.ie3.datamodel.io.factory.timeseries.TimeBasedWeatherValueFactory;
import edu.ie3.datamodel.io.source.IdCoordinateSource;
import edu.ie3.datamodel.io.source.WeatherSource;
import edu.ie3.datamodel.models.timeseries.individual.IndividualTimeSeries;
import edu.ie3.datamodel.models.timeseries.individual.TimeBasedValue;
import edu.ie3.datamodel.models.value.WeatherValue;
import edu.ie3.util.StringUtils;
import edu.ie3.util.interval.ClosedInterval;
import edu.ie3.util.naming.NamingConvention;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.locationtech.jts.geom.Point;

/** InfluxDB Source for weather data */
public class InfluxDbWeatherSource implements WeatherSource {
  private static final String BASIC_QUERY_STRING = "Select * from weather";
  private static final String WHERE = " where ";
  private static final String MEASUREMENT_NAME_WEATHER = "weather";
  private static final int MILLI_TO_NANO_FACTOR = 1000000;
  private static final NamingConvention DEFAULT_NAMING_CONVENTION = NamingConvention.SNAKE;

  private final String coordinateIdFieldName;
  private final InfluxDbConnector connector;
  private final IdCoordinateSource coordinateSource;
  private final TimeBasedWeatherValueFactory weatherValueFactory;
  private final NamingConvention namingConvention;

  /**
   * Initializes a new InfluxDbWeatherSource using the default naming convention.
   *
   * @param connector needed for database connection
   * @param coordinateSource needed to map coordinates to ID as InfluxDB does not support spatial
   *     types
   * @param weatherValueFactory instance of a time based weather value factory
   * @deprecated Use {@link InfluxDbWeatherSource#InfluxDbWeatherSource(InfluxDbConnector,
   *     IdCoordinateSource, NamingConvention, TimeBasedWeatherValueFactory)}
   */
  @Deprecated
  public InfluxDbWeatherSource(
      InfluxDbConnector connector,
      IdCoordinateSource coordinateSource,
      TimeBasedWeatherValueFactory weatherValueFactory) {
    this.connector = connector;
    this.coordinateSource = coordinateSource;
    this.namingConvention = DEFAULT_NAMING_CONVENTION;
    this.weatherValueFactory = weatherValueFactory;
    this.coordinateIdFieldName = weatherValueFactory.getCoordinateIdFieldString();
  }

  /**
   * Initializes a new InfluxDbWeatherSource
   *
   * @param connector needed for database connection
   * @param coordinateSource needed to map coordinates to ID as InfluxDB does not support spatial
   *     types
   * @param namingConvention the naming convention used for features
   * @param weatherValueFactory instance of a time based weather value factory
   */
  public InfluxDbWeatherSource(
      InfluxDbConnector connector,
      IdCoordinateSource coordinateSource,
      NamingConvention namingConvention,
      TimeBasedWeatherValueFactory weatherValueFactory) {
    this.connector = connector;
    this.coordinateSource = coordinateSource;
    this.namingConvention = namingConvention;
    this.weatherValueFactory = weatherValueFactory;
    this.coordinateIdFieldName = weatherValueFactory.getCoordinateIdFieldString();
  }

  @Override
  public Map<Point, IndividualTimeSeries<WeatherValue>> getWeather(
      ClosedInterval<ZonedDateTime> timeInterval) {
    try (InfluxDB session = connector.getSession()) {
      String query = createQueryStringForTimeInterval(timeInterval);
      QueryResult queryResult = session.query(new Query(query));
      Stream<Optional<TimeBasedValue<WeatherValue>>> optValues =
          optTimeBasedValueStream(queryResult);
      Set<TimeBasedValue<WeatherValue>> timeBasedValues =
          filterEmptyOptionals(optValues).collect(Collectors.toSet());
      Map<Point, Set<TimeBasedValue<WeatherValue>>> coordinateToValues =
          timeBasedValues.stream()
              .collect(
                  Collectors.groupingBy(
                      timeBasedWeatherValue -> timeBasedWeatherValue.getValue().getCoordinate(),
                      Collectors.toSet()));
      return coordinateToValues.entrySet().stream()
          .collect(
              Collectors.toMap(
                  Map.Entry::getKey, e -> new IndividualTimeSeries<>(null, e.getValue())));
    }
  }

  @Override
  public Map<Point, IndividualTimeSeries<WeatherValue>> getWeather(
      ClosedInterval<ZonedDateTime> timeInterval, Collection<Point> coordinates) {
    if (coordinates == null) return getWeather(timeInterval);
    Map<Point, Optional<Integer>> coordinatesToId =
        coordinates.stream().collect(Collectors.toMap(point -> point, coordinateSource::getId));
    HashMap<Point, IndividualTimeSeries<WeatherValue>> coordinateToTimeSeries = new HashMap<>();
    try (InfluxDB session = connector.getSession()) {
      for (Map.Entry<Point, Optional<Integer>> entry : coordinatesToId.entrySet()) {
        Optional<Integer> coordinateId = entry.getValue();
        if (coordinateId.isPresent()) {
          String query =
              createQueryStringForCoordinateAndTimeInterval(timeInterval, coordinateId.get());
          QueryResult queryResult = session.query(new Query(query));
          Stream<Optional<TimeBasedValue<WeatherValue>>> optValues =
              optTimeBasedValueStream(queryResult);
          Set<TimeBasedValue<WeatherValue>> timeBasedValues =
              filterEmptyOptionals(optValues).collect(Collectors.toSet());
          IndividualTimeSeries<WeatherValue> timeSeries =
              new IndividualTimeSeries<>(null, timeBasedValues);
          coordinateToTimeSeries.put(entry.getKey(), timeSeries);
        }
      }
    }
    return coordinateToTimeSeries;
  }

  /**
   * Return the weather for the given time interval AND coordinate
   *
   * @param timeInterval Queried time interval
   * @param coordinate Queried coordinate
   * @return weather data for the specified time and coordinate
   */
  public IndividualTimeSeries<WeatherValue> getWeather(
      ClosedInterval<ZonedDateTime> timeInterval, Point coordinate) {
    Optional<Integer> coordinateId = coordinateSource.getId(coordinate);
    if (!coordinateId.isPresent()) {
      return new IndividualTimeSeries<>(UUID.randomUUID(), Collections.emptySet());
    }
    try (InfluxDB session = connector.getSession()) {
      String query =
          createQueryStringForCoordinateAndTimeInterval(timeInterval, coordinateId.get());
      QueryResult queryResult = session.query(new Query(query));
      Stream<Optional<TimeBasedValue<WeatherValue>>> optValues =
          optTimeBasedValueStream(queryResult);
      return new IndividualTimeSeries<>(
          null, filterEmptyOptionals(optValues).collect(Collectors.toSet()));
    }
  }

  @Override
  public Optional<TimeBasedValue<WeatherValue>> getWeather(ZonedDateTime date, Point coordinate) {
    Optional<Integer> coordinateId = coordinateSource.getId(coordinate);
    if (!coordinateId.isPresent()) {
      return Optional.empty();
    }
    try (InfluxDB session = connector.getSession()) {
      String query = createQueryStringForCoordinateAndTime(date, coordinateId.get());
      QueryResult queryResult = session.query(new Query(query));
      return filterEmptyOptionals(optTimeBasedValueStream(queryResult)).findFirst();
    }
  }

  /**
   * Parses an influxQL QueryResult and then transforms them into a Stream of optional
   * TimeBasedValue&lt;WeatherValue&gt;, with a present Optional value, if the transformation was
   * successful and an empty optional otherwise.
   */
  private Stream<Optional<TimeBasedValue<WeatherValue>>> optTimeBasedValueStream(
      QueryResult queryResult) {
    Map<String, Set<Map<String, String>>> measurementsMap =
        InfluxDbConnector.parseQueryResult(queryResult, MEASUREMENT_NAME_WEATHER);
    return measurementsMap.get(MEASUREMENT_NAME_WEATHER).stream()
        .map(
            fieldToValue -> {
              /* The factory expects flat case id's for fields -> Convert the keys */
              Map<String, String> flatCaseFields =
                  fieldToValue.entrySet().stream()
                      .collect(
                          Collectors.toMap(
                              entry ->
                                  StringUtils.snakeCaseToCamelCase(entry.getKey()).toLowerCase(),
                              Map.Entry::getValue));

              Optional<Point> coordinate =
                  coordinateSource.getCoordinate(
                      Integer.parseInt(flatCaseFields.remove(coordinateIdFieldName)));
              if (!coordinate.isPresent()) return null;
              flatCaseFields.putIfAbsent("uuid", UUID.randomUUID().toString());

              return new TimeBasedWeatherValueData(flatCaseFields, coordinate.get());
            })
        .filter(Objects::nonNull)
        .map(weatherValueFactory::get);
  }

  private String createQueryStringForCoordinateAndTimeInterval(
      ClosedInterval<ZonedDateTime> timeInterval, int coordinateId) {
    return BASIC_QUERY_STRING
        + WHERE
        + createCoordinateConstraintString(coordinateId)
        + " and "
        + createTimeConstraint(timeInterval);
  }

  private String createQueryStringForCoordinateAndTime(ZonedDateTime date, int coordinateId) {
    return BASIC_QUERY_STRING
        + WHERE
        + createCoordinateConstraintString(coordinateId)
        + " and "
        + createTimeConstraint(date);
  }

  private String createQueryStringForTimeInterval(ClosedInterval<ZonedDateTime> timeInterval) {
    return BASIC_QUERY_STRING + WHERE + createTimeConstraint(timeInterval);
  }

  private String createTimeConstraint(ClosedInterval<ZonedDateTime> timeInterval) {
    return weatherValueFactory.getTimeFieldString()
        + " >= "
        + timeInterval.getLower().toInstant().toEpochMilli() * MILLI_TO_NANO_FACTOR
        + " and "
        + weatherValueFactory.getTimeFieldString()
        + " <= "
        + timeInterval.getUpper().toInstant().toEpochMilli() * MILLI_TO_NANO_FACTOR;
  }

  private String createTimeConstraint(ZonedDateTime date) {
    return weatherValueFactory.getTimeFieldString()
        + "="
        + date.toInstant().toEpochMilli() * MILLI_TO_NANO_FACTOR;
  }

  private String createCoordinateConstraintString(int coordinateId) {
    return weatherValueFactory.getCoordinateIdFieldString(namingConvention)
        + " = '"
        + coordinateId
        + "'";
  }

  /**
   * Removes empty Optionals
   *
   * @param elements stream to filter
   * @return filtered elements Stream
   */
  protected Stream<TimeBasedValue<WeatherValue>> filterEmptyOptionals(
      Stream<Optional<TimeBasedValue<WeatherValue>>> elements) {
    return elements.filter(Optional::isPresent).map(Optional::get).map(TimeBasedValue.class::cast);
  }
}
