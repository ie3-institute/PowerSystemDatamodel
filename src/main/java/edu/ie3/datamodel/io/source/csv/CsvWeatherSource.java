/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.datamodel.io.source.csv;

import edu.ie3.datamodel.io.connectors.CsvFileConnector;
import edu.ie3.datamodel.io.csv.FileNamingStrategy;
import edu.ie3.datamodel.io.csv.timeseries.ColumnScheme;
import edu.ie3.datamodel.io.factory.timeseries.TimeBasedWeatherValueData;
import edu.ie3.datamodel.io.factory.timeseries.TimeBasedWeatherValueFactory;
import edu.ie3.datamodel.io.source.IdCoordinateSource;
import edu.ie3.datamodel.io.source.WeatherSource;
import edu.ie3.datamodel.models.timeseries.individual.IndividualTimeSeries;
import edu.ie3.datamodel.models.timeseries.individual.TimeBasedValue;
import edu.ie3.datamodel.models.value.Value;
import edu.ie3.datamodel.models.value.WeatherValue;
import edu.ie3.util.interval.ClosedInterval;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Point;

/** Implements a WeatherSource for CSV files by using the CsvTimeSeriesSource as a base */
public class CsvWeatherSource extends CsvDataSource implements WeatherSource {

  private final TimeBasedWeatherValueFactory weatherFactory = new TimeBasedWeatherValueFactory();
  private static final String COORDINATE_FIELD = "coordinate";

  private final Map<Point, IndividualTimeSeries<WeatherValue>> coordinateToTimeSeries;
  private final IdCoordinateSource coordinateSource;

  /**
   * Initializes a CsvWeatherSource with a {@link CsvIdCoordinateSource} instance and immediately
   * imports weather data, which will be kept for the lifetime of this source
   *
   * @param csvSep the separator string for csv columns
   * @param folderPath path to the folder holding the time series files
   * @param fileNamingStrategy strategy for the naming of time series files
   */
  public CsvWeatherSource(String csvSep, String folderPath, FileNamingStrategy fileNamingStrategy) {
    this(
        csvSep,
        folderPath,
        fileNamingStrategy,
        new CsvIdCoordinateSource(csvSep, folderPath, fileNamingStrategy));
  }

  /**
   * Initializes a CsvWeatherSource and immediately imports weather data, which will be kept for the
   * lifetime of this source
   *
   * @param csvSep the separator string for csv columns
   * @param folderPath path to the folder holding the time series files
   * @param fileNamingStrategy strategy for the naming of time series files
   * @param coordinateSource a coordinate source to map ids to points
   */
  public CsvWeatherSource(
      String csvSep,
      String folderPath,
      FileNamingStrategy fileNamingStrategy,
      IdCoordinateSource coordinateSource) {
    super(csvSep, folderPath, fileNamingStrategy);
    coordinateToTimeSeries = getWeatherTimeSeries();
    this.coordinateSource = coordinateSource;
  }

  /**
   * Creates reader for all available weather time series files and then continues to parse them
   *
   * @return a map of coordinates to their time series
   */
  private Map<Point, IndividualTimeSeries<WeatherValue>> getWeatherTimeSeries() {
    /* Get only weather time series reader */
    Map<ColumnScheme, Set<CsvFileConnector.TimeSeriesReadingData>> colTypeToReadingData =
        connector.initTimeSeriesReader(ColumnScheme.WEATHER);

    /* Reading in weather time series */
    Set<CsvFileConnector.TimeSeriesReadingData> weatherReadingData =
        colTypeToReadingData.get(ColumnScheme.WEATHER);

    return readWeatherTimeSeries(weatherReadingData);
  }

  @Override
  public Map<Point, IndividualTimeSeries<WeatherValue>> getWeather(
      ClosedInterval<ZonedDateTime> timeInterval) {
    return trimMapToInterval(coordinateToTimeSeries, timeInterval);
  }

  @Override
  public Map<Point, IndividualTimeSeries<WeatherValue>> getWeather(
      ClosedInterval<ZonedDateTime> timeInterval, Collection<Point> coordinates) {
    Map<Point, IndividualTimeSeries<WeatherValue>> filteredMap =
        coordinateToTimeSeries.entrySet().stream()
            .filter(entry -> coordinates.contains(entry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    return trimMapToInterval(filteredMap, timeInterval);
  }

  @Override
  public Optional<TimeBasedValue<WeatherValue>> getWeather(ZonedDateTime date, Point coordinate) {
    IndividualTimeSeries<WeatherValue> timeSeries = coordinateToTimeSeries.get(coordinate);
    if (timeSeries == null) return Optional.empty();
    return timeSeries.getTimeBasedValue(date);
  }

  /**
   * Trims all time series in a map to the given time interval
   *
   * @param map the map to trim the time series value of
   * @param timeInterval the interval to trim the data to
   * @return a map with trimmed time series
   */
  private Map<Point, IndividualTimeSeries<WeatherValue>> trimMapToInterval(
      Map<Point, IndividualTimeSeries<WeatherValue>> map,
      ClosedInterval<ZonedDateTime> timeInterval) {
    // decided against parallel mode here as it likely wouldn't pay off as the expected coordinate
    // count is too low
    return map.entrySet().stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                entry -> trimTimeSeriesToInterval(entry.getValue(), timeInterval)));
  }

  /**
   * Trims a time series to the given time interval
   *
   * @param timeSeries the time series to trim
   * @param timeInterval the interval to trim the data to
   * @return the trimmed time series
   */
  private IndividualTimeSeries<WeatherValue> trimTimeSeriesToInterval(
      IndividualTimeSeries<WeatherValue> timeSeries, ClosedInterval<ZonedDateTime> timeInterval) {
    return new IndividualTimeSeries<>(
        timeSeries.getUuid(),
        timeSeries.getEntries().stream()
            .parallel()
            .filter(value -> timeInterval.includes(value.getTime()))
            .collect(Collectors.toSet()));
  }

  /**
   * Reads weather data to time series and maps them coordinate wise
   *
   * @param weatherReadingData Data needed for reading
   * @return time series mapped to the represented coordinate
   */
  private Map<Point, IndividualTimeSeries<WeatherValue>> readWeatherTimeSeries(
      Set<CsvFileConnector.TimeSeriesReadingData> weatherReadingData) {
    final Map<Point, IndividualTimeSeries<WeatherValue>> weatherTimeSeries = new HashMap<>();
    Function<Map<String, String>, Optional<TimeBasedValue<WeatherValue>>> fieldToValueFunction =
        this::buildWeatherValue;

    /* Reading in weather time series */
    for (CsvFileConnector.TimeSeriesReadingData data : weatherReadingData) {
      Map<Point, List<TimeBasedValue<WeatherValue>>> coordinateToValues =
          filterEmptyOptionals(
                  buildStreamWithFieldsToAttributesMap(TimeBasedValue.class, data.getReader())
                      .map(fieldToValueFunction))
              .collect(Collectors.groupingBy(tbv -> tbv.getValue().getCoordinate()));

      // We have to generate a random UUID as we'd risk running into duplicate key issues otherwise
      coordinateToValues.forEach(
          (point, timeBasedValues) -> {
            IndividualTimeSeries<WeatherValue> timeSeries =
                new IndividualTimeSeries<>(UUID.randomUUID(), new HashSet<>(timeBasedValues));
            if (weatherTimeSeries.containsKey(point)) {
              IndividualTimeSeries<WeatherValue> mergedTimeSeries =
                  mergeTimeSeries(weatherTimeSeries.get(point), timeSeries);
              weatherTimeSeries.put(point, mergedTimeSeries);
            } else {
              weatherTimeSeries.put(point, timeSeries);
            }
          });
    }
    return weatherTimeSeries;
  }

  /**
   * Builds a {@link TimeBasedValue} of type {@link WeatherValue} from given "flat " input
   * information. If the single model cannot be built, an empty optional is handed back.
   *
   * @param fieldToValues "flat " input information as a mapping from field to value
   * @return Optional time based weather value
   */
  private Optional<TimeBasedValue<WeatherValue>> buildWeatherValue(
      Map<String, String> fieldToValues) {
    /* Try to get the coordinate from entries */
    String coordinateString = fieldToValues.get(COORDINATE_FIELD);
    if (Objects.isNull(coordinateString) || coordinateString.isEmpty()) {
      log.error(
          "Cannot parse weather value. Unable to find field '{}' in data: {}",
          COORDINATE_FIELD,
          fieldToValues);
      return Optional.empty();
    }
    int coordinateId = Integer.parseInt(coordinateString);
    return coordinateSource
        .getCoordinate(coordinateId)
        .map(
            coordinate -> {
              /* Remove coordinate entry from fields */
              fieldToValues.remove(COORDINATE_FIELD);

              /* Build factory data */
              TimeBasedWeatherValueData factoryData =
                  new TimeBasedWeatherValueData(fieldToValues, coordinate);
              return weatherFactory.get(factoryData);
            })
        .orElseGet(
            () -> {
              log.error("Unable to find coordinate with id '{}'.", coordinateId);
              return Optional.empty();
            });
  }

  /**
   * Merge two individual time series into a new time series with the UUID of the first parameter
   *
   * @param a the first time series to merge
   * @param b the second time series to merge
   * @return merged time series with a's UUID
   */
  private <V extends Value> IndividualTimeSeries<V> mergeTimeSeries(
      IndividualTimeSeries<V> a, IndividualTimeSeries<V> b) {
    SortedSet<TimeBasedValue<V>> entries = a.getEntries();
    entries.addAll(b.getEntries());
    return new IndividualTimeSeries<>(a.getUuid(), entries);
  }
}
