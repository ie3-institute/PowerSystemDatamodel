/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.datamodel.io.processor;

import edu.ie3.datamodel.exceptions.EntityProcessorException;
import edu.ie3.datamodel.io.factory.input.NodeInputFactory;
import edu.ie3.datamodel.io.processor.result.ResultEntityProcessor;
import edu.ie3.datamodel.models.OperationTime;
import edu.ie3.datamodel.models.StandardUnits;
import edu.ie3.datamodel.models.UniqueEntity;
import edu.ie3.datamodel.models.voltagelevels.VoltageLevel;
import edu.ie3.util.TimeTools;
import java.beans.Introspector;
import java.lang.reflect.Method;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import javax.measure.Quantity;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.ElectricCurrent;
import javax.measure.quantity.Length;
import javax.measure.quantity.Power;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

/**
 * Internal API Interface for EntityProcessors. Main purpose is to 'de-serialize' models into a
 * fieldName -> value representation to allow for an easy processing into a database or file sink
 * e.g. .csv
 *
 * @version 0.1
 * @since 31.01.20
 */
public abstract class EntityProcessor<T extends UniqueEntity> {

  public final Logger log = LogManager.getLogger(this.getClass());
  private final Class<? extends T> registeredClass;
  protected final String[] headerElements;
  protected final LinkedHashMap<String, Method> fieldNameToMethod = new LinkedHashMap<>();

  private static final String OPERATION_TIME_FIELD_NAME = OperationTime.class.getSimpleName();
  private static final String OPERATES_FROM = "operatesFrom";
  private static final String OPERATES_UNTIL = "operatesUntil";

  private static final String VOLT_LVL_FIELD_NAME = "voltLvl";
  private static final String VOLT_LVL = NodeInputFactory.VOLT_LVL;
  private static final String V_RATED = NodeInputFactory.V_RATED;

  private static final GeoJsonWriter geoJsonWriter = new GeoJsonWriter();

  /** Field name of {@link UniqueEntity} uuid */
  private static final String UUID_FIELD_NAME = "uuid";

  /**
   * Create a new EntityProcessor
   *
   * @param registeredClass the class the entity processor should be able to handle
   */
  public EntityProcessor(Class<? extends T> registeredClass) {
    this.registeredClass = registeredClass;
    this.headerElements = registerClass(registeredClass, getAllEligibleClasses());
    TimeTools.initialize(ZoneId.of("UTC"), Locale.GERMANY, "yyyy-MM-dd HH:mm:ss");
  }

  /**
   * Register the class provided in the constructor
   *
   * @param cls class to be registered
   * @return an array of strings of all field values of the registered class
   */
  // todo JH this method has side effects that should be addressed
  private String[] registerClass(Class<? extends T> cls, List<Class<? extends T>> eligibleClasses) {
    if (!eligibleClasses.contains(cls))
      throw new EntityProcessorException(
          "Cannot register class '"
              + cls.getSimpleName()
              + "' with entity processor '"
              + this.getClass().getSimpleName()
              + "'. Eligible classes: "
              + eligibleClasses.stream()
                  .map(Class::getSimpleName)
                  .collect(Collectors.joining(", ")));
    try {
      Arrays.stream(Introspector.getBeanInfo(cls, Object.class).getPropertyDescriptors())
          // filter out properties with setters only
          .filter(pd -> Objects.nonNull(pd.getReadMethod()))
          .forEach(
              pd -> { // invoke method to get value
                if (pd.getReadMethod() != null) {

                  // OperationTime needs to be replaced by operatesFrom and operatesUntil
                  String fieldName = pd.getName();
                  if (fieldName.equalsIgnoreCase(OPERATION_TIME_FIELD_NAME)) {
                    fieldName = OPERATES_FROM;
                    fieldNameToMethod.put(OPERATES_UNTIL, pd.getReadMethod());
                  }

                  // VoltageLevel needs to be replaced by id and nominalVoltage
                  if (fieldName.equalsIgnoreCase(VOLT_LVL_FIELD_NAME)) {
                    fieldName = V_RATED;
                    fieldNameToMethod.put(VOLT_LVL, pd.getReadMethod());
                  }
                  fieldNameToMethod.put(fieldName, pd.getReadMethod());
                }
              });

    } catch (Exception e) {
      throw new EntityProcessorException(
          "Error during EntityProcessor class registration process. Exception was:" + e);
    }

    // uuid should always be the first element in the map
    String[] filteredArray =
        fieldNameToMethod.keySet().stream()
            .filter(x -> !x.toLowerCase().contains(UUID_FIELD_NAME))
            .toArray(String[]::new);

    return ArrayUtils.addAll(new String[] {UUID_FIELD_NAME}, filteredArray);
  }

  /**
   * Standard call to handle an entity
   *
   * @param entity the entity that should be 'de-serialized' into a map of fieldName -> fieldValue
   * @return an optional Map with fieldName -> fieldValue or an empty optional if an error occurred
   *     during processing
   */
  public Optional<LinkedHashMap<String, String>> handleEntity(T entity) {
    if (!registeredClass.equals(entity.getClass()))
      throw new EntityProcessorException(
          "Cannot process "
              + entity.getClass().getSimpleName()
              + ".class with this EntityProcessor. Please either provide an element of "
              + registeredClass.getSimpleName()
              + ".class or create a new factory for "
              + entity.getClass().getSimpleName()
              + ".class!");
    return processEntity(entity);
  }

  /**
   * // todo JH refresh text Actual implementation of the handling process. Depends on the entity
   * that should be processed and hence needs to be implemented individually
   *
   * @param entity the entity that should be 'de-serialized' into a map of fieldName -> fieldValue
   * @return an optional Map with fieldName -> fieldValue or an empty optional if an error occurred
   *     during processing
   */
  private Optional<LinkedHashMap<String, String>> processEntity(T entity) {

    Optional<LinkedHashMap<String, String>> resultMapOpt;

    try {
      LinkedHashMap<String, String> resultMap = new LinkedHashMap<>();
      for (String fieldName : headerElements) {
        Method method = fieldNameToMethod.get(fieldName);
        Optional<Object> methodReturnObjectOpt = Optional.ofNullable(method.invoke(entity));

        if (methodReturnObjectOpt.isPresent()) {
          resultMap.put(
              fieldName, processMethodResult(methodReturnObjectOpt.get(), method, fieldName));
        } else {
          resultMap.put(fieldName, "");
        }
      }
      resultMapOpt = Optional.of(resultMap);
    } catch (Exception e) {
      log.error("Error during entity processing:", e);
      resultMapOpt = Optional.empty();
    }
    return resultMapOpt;
  }

  private String processMethodResult(Object methodReturnObject, Method method, String fieldName) {

    StringBuilder resultStringBuilder = new StringBuilder();

    switch (method.getReturnType().getSimpleName()) {
        // primitives (Boolean, Character, Byte, Short, Integer, Long, Float, Double, String,
      case "UUID":
      case "boolean":
      case "int":
      case "String":
        resultStringBuilder.append(methodReturnObject.toString());
        break;
      case "Quantity":
        resultStringBuilder.append(
            handleQuantity((Quantity<?>) methodReturnObject, fieldName)
                .orElseThrow(
                    () ->
                        new EntityProcessorException(
                            "Unable to process quantity value for attribute '"
                                + fieldName
                                + "' in result entity "
                                + getRegisteredClass().getSimpleName()
                                + ".class.")));
        break;
      case "ZonedDateTime":
        resultStringBuilder.append(processZonedDateTime((ZonedDateTime) methodReturnObject));
        break;
      case "OperationTime":
        resultStringBuilder.append(
            processOperationTime((OperationTime) methodReturnObject, fieldName));
        break;
      case "VoltageLevel":
        resultStringBuilder.append(
            processVoltageLevel((VoltageLevel) methodReturnObject, fieldName));
        break;
      case "Point":
      case "LineString":
        resultStringBuilder.append(geoJsonWriter.write((Geometry) methodReturnObject));
        break;
      case "NodeInput":
      case "Transformer3WTypeInput":
      case "Transformer2WTypeInput":
      case "LineTypeInput":
      case "OperatorInput":
        resultStringBuilder.append(((UniqueEntity) methodReturnObject).getUuid());
        break;
      case "Optional": // todo needs to be removed asap as this is very dangerous, but necessary as
        // long as #75 is not addressed
        resultStringBuilder.append(((Optional<String>) methodReturnObject).orElse(""));
        break;
      default:
        throw new EntityProcessorException(
            "Unable to process value for attribute/field '"
                + fieldName
                + "' and method return type '"
                + method.getReturnType().getSimpleName()
                + "' for method with name '"
                + method.getName()
                + "' in in entity model "
                + getRegisteredClass().getSimpleName()
                + ".class.");
    }

    return resultStringBuilder.toString();
  }

  /**
   * Standard method to process a ZonedDateTime to a String based on a method return object NOTE:
   * this method does NOT check if the provided object is of type ZonedDateTime. This has to be done
   * manually BEFORE calling this method!
   *
   * @param zonedDateTime representation of the ZonedDateTime
   * @return string representation of the ZonedDateTime
   */
  protected String processZonedDateTime(ZonedDateTime zonedDateTime) {
    return TimeTools.toString(zonedDateTime);
  }

  /**
   * TODO JH
   *
   * @param operationTime
   * @param fieldName
   * @return
   */
  protected String processOperationTime(OperationTime operationTime, String fieldName) {
    StringBuilder resultStringBuilder = new StringBuilder();

    if (fieldName.equalsIgnoreCase(OPERATES_FROM))
      operationTime
          .getStartDate()
          .ifPresent(startDate -> resultStringBuilder.append(processZonedDateTime(startDate)));

    if (fieldName.equalsIgnoreCase(OPERATES_UNTIL))
      operationTime
          .getEndDate()
          .ifPresent(endDate -> resultStringBuilder.append(processZonedDateTime(endDate)));

    return resultStringBuilder.toString();
  }

  /**
   * todo JH
   *
   * @param voltageLevel
   * @return
   */
  protected String processVoltageLevel(VoltageLevel voltageLevel, String fieldName) {

    StringBuilder resultStringBuilder = new StringBuilder();
    if (fieldName.equalsIgnoreCase(VOLT_LVL)) resultStringBuilder.append(voltageLevel.getId());

    if (fieldName.equalsIgnoreCase(V_RATED))
      resultStringBuilder.append(
          handleQuantity(voltageLevel.getNominalVoltage(), fieldName)
              .orElseThrow(
                  () ->
                      new EntityProcessorException(
                          "Unable to process quantity value for attribute '"
                              + fieldName
                              + "' in result entity "
                              + getRegisteredClass().getSimpleName()
                              + ".class.")));
    ;

    return resultStringBuilder.toString();
  }

  /**
   * Standard method to process a Quantity to a String based on a method return object
   *
   * @param quantity the quantity that should be processed
   * @param fieldName the field name the quantity is set to
   * @return an optional string with the normalized to {@link StandardUnits} value of the quantity
   *     or empty if an error occurred during processing
   */
  protected Optional<String> handleQuantity(Quantity<?> quantity, String fieldName) {

    Optional<String> normalizedQuantityValue = Optional.empty();

    switch (fieldName) {
      case "p":
      case "q":
      case "energy":
      case "vTarget":
      case "vrated":
        normalizedQuantityValue = handleProcessorSpecificQuantity(quantity, fieldName);
        break;
      case "soc":
      case "vAng":
      case "vMag":
      case "iAAng":
      case "iBAng":
      case "iCAng":
        normalizedQuantityValue = quantityValToOptionalString(quantity);
        break;
      case "iAMag":
      case "iBMag":
      case "iCMag":
        normalizedQuantityValue =
            quantityValToOptionalString(
                quantity
                    .asType(ElectricCurrent.class)
                    .to(StandardUnits.ELECTRIC_CURRENT_MAGNITUDE));
        break;
      case "qDot":
        normalizedQuantityValue =
            quantityValToOptionalString(
                quantity.asType(Power.class).to(StandardUnits.Q_DOT_RESULT));
        break;
      case "fillLevel":
        normalizedQuantityValue =
            quantityValToOptionalString(
                quantity.asType(Dimensionless.class).to(StandardUnits.FILL_LEVEL));
        break;
      case "length":
        normalizedQuantityValue =
            quantityValToOptionalString(
                quantity.asType(Length.class).to(StandardUnits.LINE_LENGTH));
        break;
      default:
        log.error(
            "Cannot process quantity with value '{}' for field with name {} in model processing!",
            quantity,
            fieldName);
        break;
    }
    return normalizedQuantityValue;
  }

  /**
   * This method should handle all quantities that are model processor specific e.g. we need to
   * handle active power p different for {@link edu.ie3.datamodel.models.result.ResultEntity}s and
   * {@link edu.ie3.datamodel.models.input.system.SystemParticipantInput}s Hence from the
   * generalized method {@link this.handleQuantity()}, this allows for the specific handling of
   * child implementations. See the implementation @ {@link ResultEntityProcessor} for details.
   *
   * @param quantity the quantity that should be processed
   * @param fieldName the field name the quantity is set to
   * @return an optional string with the normalized to {@link StandardUnits} value of the quantity
   *     or empty if an error occurred during processing
   */
  protected abstract Optional<String> handleProcessorSpecificQuantity(
      Quantity<?> quantity, String fieldName);

  protected Optional<String> quantityValToOptionalString(Quantity<?> quantity) {
    return Optional.of(Double.toString(quantity.getValue().doubleValue()));
  }

  public Class<? extends T> getRegisteredClass() {
    return registeredClass;
  }

  public String[] getHeaderElements() {
    return headerElements;
  }

  protected abstract List<Class<? extends T>> getAllEligibleClasses();
}