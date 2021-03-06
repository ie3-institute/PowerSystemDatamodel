/*
 * © 2021. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.datamodel.io.source;

import edu.ie3.datamodel.models.input.OperatorInput;
import edu.ie3.datamodel.models.input.thermal.CylindricalStorageInput;
import edu.ie3.datamodel.models.input.thermal.ThermalBusInput;
import edu.ie3.datamodel.models.input.thermal.ThermalHouseInput;
import edu.ie3.datamodel.models.input.thermal.ThermalStorageInput;
import java.util.Set;

/**
 * Interface that provides the capability to build thermal {@link
 * edu.ie3.datamodel.models.input.AssetInput} entities from persistent data e.g. .csv files or
 * databases
 *
 * @version 0.1
 * @since 08.04.20
 */
public interface ThermalSource extends DataSource {

  /**
   * Returns a unique set of {@link ThermalBusInput} instances.
   *
   * <p>This set has to be unique in the sense of object uniqueness but also in the sense of {@link
   * java.util.UUID} uniqueness of the provided {@link ThermalBusInput} which has to be checked
   * manually, as {@link ThermalBusInput#equals(Object)} is NOT restricted on the uuid of {@link
   * ThermalBusInput}.
   *
   * @return a set of object and uuid unique {@link ThermalBusInput} entities
   */
  Set<ThermalBusInput> getThermalBuses();

  /**
   * Returns a set of {@link ThermalBusInput} instances. This set has to be unique in the sense of
   * object uniqueness but also in the sense of {@link java.util.UUID} uniqueness of the provided
   * {@link ThermalBusInput} which has to be checked manually, as {@link
   * ThermalBusInput#equals(Object)} is NOT restricted on the uuid of {@link ThermalBusInput}.
   *
   * <p>In contrast to {@link #getThermalBuses()} this interface provides the ability to pass in an
   * already existing set of {@link OperatorInput} entities, the {@link ThermalBusInput} instances
   * depend on. Doing so, already loaded nodes can be recycled to improve performance and prevent
   * unnecessary loading operations.
   *
   * <p>If something fails during the creation process it's up to the concrete implementation of an
   * empty set or a set with all entities that has been able to be build is returned.
   *
   * @param operators a set of object and uuid unique {@link OperatorInput} that should be used for
   *     the returning instances
   * @return a set of object and uuid unique {@link ThermalBusInput} entities
   */
  Set<ThermalBusInput> getThermalBuses(Set<OperatorInput> operators);

  /**
   * Returns a unique set of instances of all entities implementing the {@link ThermalStorageInput}
   * abstract class.
   *
   * <p>This set has to be unique in the sense of object uniqueness but also in the sense of {@link
   * java.util.UUID} uniqueness of the provided {@link ThermalStorageInput} which has to be checked
   * manually, as {@link ThermalStorageInput#equals(Object)} is NOT restricted on the uuid of {@link
   * ThermalStorageInput}.
   *
   * @return a set of object and uuid unique {@link ThermalStorageInput} entities
   */
  Set<ThermalStorageInput> getThermalStorages();

  /**
   * Returns a unique set of instances of all entities implementing the {@link ThermalStorageInput}
   * abstract class. This set has to be unique in the sense of object uniqueness but also in the
   * sense of {@link java.util.UUID} uniqueness of the provided {@link ThermalStorageInput} which
   * has to be checked manually, as {@link ThermalStorageInput#equals(Object)} is NOT restricted on
   * the uuid of {@link ThermalStorageInput}.
   *
   * <p>In contrast to {@link #getThermalStorages()} this interface provides the ability to pass in
   * an already existing set of {@link OperatorInput} entities, the {@link ThermalStorageInput}
   * instances depend on. Doing so, already loaded nodes can be recycled to improve performance and
   * prevent unnecessary loading operations.
   *
   * <p>If something fails during the creation process it's up to the concrete implementation of an
   * empty set or a set with all entities that has been able to be build is returned.
   *
   * @param operators a set of object and uuid unique {@link OperatorInput} that should be used for
   *     the returning instances
   * @param thermalBuses a set of object and uuid unique {@link ThermalBusInput} that should be used
   *     for the returning instances
   * @return a set of object and uuid unique {@link ThermalStorageInput} entities
   */
  Set<ThermalStorageInput> getThermalStorages(
      Set<OperatorInput> operators, Set<ThermalBusInput> thermalBuses);

  /**
   * Returns a unique set of {@link ThermalHouseInput} instances.
   *
   * <p>This set has to be unique in the sense of object uniqueness but also in the sense of {@link
   * java.util.UUID} uniqueness of the provided {@link ThermalHouseInput} which has to be checked
   * manually, as {@link ThermalHouseInput#equals(Object)} is NOT restricted on the uuid of {@link
   * ThermalHouseInput}.
   *
   * @return a set of object and uuid unique {@link ThermalHouseInput} entities
   */
  Set<ThermalHouseInput> getThermalHouses();

  /**
   * Returns a set of {@link ThermalHouseInput} instances. This set has to be unique in the sense of
   * object uniqueness but also in the sense of {@link java.util.UUID} uniqueness of the provided
   * {@link ThermalHouseInput} which has to be checked manually, as {@link
   * ThermalHouseInput#equals(Object)} is NOT restricted on the uuid of {@link ThermalHouseInput}.
   *
   * <p>In contrast to {@link #getThermalHouses()} this interface provides the ability to pass in an
   * already existing set of {@link OperatorInput} entities, the {@link ThermalHouseInput} instances
   * depend on. Doing so, already loaded nodes can be recycled to improve performance and prevent
   * unnecessary loading operations.
   *
   * <p>If something fails during the creation process it's up to the concrete implementation of an
   * empty set or a set with all entities that has been able to be build is returned.
   *
   * @param operators a set of object and uuid unique {@link OperatorInput} that should be used for
   *     the returning instances
   * @param thermalBuses a set of object and uuid unique {@link ThermalBusInput} that should be used
   *     for the returning instances
   * @return a set of object and uuid unique {@link ThermalHouseInput} entities
   */
  Set<ThermalHouseInput> getThermalHouses(
      Set<OperatorInput> operators, Set<ThermalBusInput> thermalBuses);

  /**
   * Returns a unique set of {@link CylindricalStorageInput} instances.
   *
   * <p>This set has to be unique in the sense of object uniqueness but also in the sense of {@link
   * java.util.UUID} uniqueness of the provided {@link CylindricalStorageInput} which has to be
   * checked manually, as {@link CylindricalStorageInput#equals(Object)} is NOT restricted on the
   * uuid of {@link CylindricalStorageInput}.
   *
   * @return a set of object and uuid unique {@link CylindricalStorageInput} entities
   */
  Set<CylindricalStorageInput> getCylindricStorages();

  /**
   * Returns a set of {@link CylindricalStorageInput} instances. This set has to be unique in the
   * sense of object uniqueness but also in the sense of {@link java.util.UUID} uniqueness of the
   * provided {@link CylindricalStorageInput} which has to be checked manually, as {@link
   * CylindricalStorageInput#equals(Object)} is NOT restricted on the uuid of {@link
   * CylindricalStorageInput}.
   *
   * <p>In contrast to {@link #getCylindricStorages()} this interface provides the ability to pass
   * in an already existing set of {@link OperatorInput} entities, the {@link
   * CylindricalStorageInput} instances depend on. Doing so, already loaded nodes can be recycled to
   * improve performance and prevent unnecessary loading operations.
   *
   * <p>If something fails during the creation process it's up to the concrete implementation of an
   * empty set or a set with all entities that has been able to be build is returned.
   *
   * @param operators a set of object and uuid unique {@link OperatorInput} that should be used for
   *     the returning instances
   * @param thermalBuses a set of object and uuid unique {@link ThermalBusInput} that should be used
   *     for the returning instances
   * @return a set of object and uuid unique {@link CylindricalStorageInput} entities
   */
  Set<CylindricalStorageInput> getCylindricStorages(
      Set<OperatorInput> operators, Set<ThermalBusInput> thermalBuses);
}
