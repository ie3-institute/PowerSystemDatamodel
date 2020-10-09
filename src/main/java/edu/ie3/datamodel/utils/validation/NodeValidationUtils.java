/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.datamodel.utils.validation;

import edu.ie3.datamodel.exceptions.InvalidEntityException;
import edu.ie3.datamodel.exceptions.UnsafeEntityException;
import edu.ie3.datamodel.exceptions.VoltageLevelException;
import edu.ie3.datamodel.models.input.NodeInput;
import edu.ie3.datamodel.models.voltagelevels.VoltageLevel;

public class NodeValidationUtils extends ValidationUtils {

  /** Private Constructor as this class is not meant to be instantiated */
  private NodeValidationUtils() {
    throw new IllegalStateException("Don't try and instantiate a Utility class.");
  }

  /**
   * Validates a node if: <br>
   * - it is not null <br>
   * - operator is not null <br>
   * - voltage level is not null and valid <br>
   * - vTarget is not null and valid <br>
   * - subnet is not null <br>
   * - geoPosition is not null
   *
   * @param node Node to validate
   */
  public static void check(NodeInput node) {
    //Check if null
    checkNonNull(node, "a node");
    //Check if operator is null
    if (node.getOperator() == null)
      throw new InvalidEntityException("No operator assigned", node);
    //Check if valid voltage level
    try {
      checkVoltageLevel(node.getVoltLvl());
    } catch (VoltageLevelException e) {
      throw new InvalidEntityException("Element has invalid voltage level", node);
    }
    //Check if target voltage is null or invalid
    if (node.getvTarget() == null)
      throw new InvalidEntityException("vRated or vTarget is null", node);
    if (node.getvTarget().getValue().doubleValue() <= 0d)
      throw new UnsafeEntityException("vTarget is not a positive value", node);
    //Check if subnet is valid
    if (node.getSubnet() <= 0)
      throw new InvalidEntityException("Subnet can't be zero or negative", node);
    //Check if geoPosition is null
    if (node.getGeoPosition() == null)
      throw new InvalidEntityException("Node has no GeoPosition", node);
    //TODO: @NSteffan - necessary to check operator ("at least dummy")? operationTime? slack?
    //TODO OPERATOR UND OPERATIONTIME IMMER CHECKEN + OPERATIONTIME START DATE VOR END DATE (rausziehen)
  }


  //TODO @NSteffan: separate ValidationUtils for Voltage Level?
  /**
   * Validates a voltage level
   *
   * @param voltageLevel Element to validate
   * @throws VoltageLevelException If nominal voltage is not apparent or not a positive value
   */
  private static void checkVoltageLevel(VoltageLevel voltageLevel) throws VoltageLevelException {
    checkNonNull(voltageLevel, "a voltage level");
    if (voltageLevel.getNominalVoltage() == null)
      throw new VoltageLevelException(
          "The nominal voltage of voltage level " + voltageLevel + " is null");
    if (voltageLevel.getNominalVoltage().getValue().doubleValue() <= 0d)
      throw new VoltageLevelException(
          "The nominal voltage of voltage level " + voltageLevel + " must be positive!");
  }
}