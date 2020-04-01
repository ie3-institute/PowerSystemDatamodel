/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.datamodel.io.factory.typeinput;

import edu.ie3.datamodel.exceptions.FactoryException;
import edu.ie3.datamodel.io.factory.SimpleEntityData;
import edu.ie3.datamodel.models.StandardUnits;
import edu.ie3.datamodel.models.input.system.type.*;
import edu.ie3.util.quantities.interfaces.Currency;
import edu.ie3.util.quantities.interfaces.DimensionlessRate;
import edu.ie3.util.quantities.interfaces.EnergyPrice;
import edu.ie3.util.quantities.interfaces.SpecificEnergy;
import tec.uom.se.ComparableQuantity;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.measure.Quantity;
import javax.measure.quantity.*;

public class SystemParticipantTypeInputFactory
    extends AssetTypeInputEntityFactory<SystemParticipantTypeInput> {
  // SystemParticipantTypeInput parameters
  private static final String CAP_EX = "capex";
  private static final String OP_EX = "opex";
  private static final String S_RATED = "srated";
  private static final String COS_PHI = "cosphi";

  // required in multiple types
  private static final String ETA_CONV = "etaconv";
  private static final String P_THERMAL = "pthermal";
  private static final String E_STORAGE = "estorage";

  // EvTypeInput
  private static final String E_CONS = "econs";

  // BmTypeInput
  private static final String ACTIVE_POWER_GRADIENT = "activepowergradient";

  // WecTypeInput
  private static final String ROTOR_AREA = "rotorarea";
  private static final String HUB_HEIGHT = "hubheight";

  // ChpTypeInput
  private static final String ETA_EL = "etael";
  private static final String ETA_THERMAL = "etathermal";
  private static final String P_OWN = "pown";

  // StorageTypeInput
  private static final String P_MAX = "pmax";
  private static final String ETA = "eta";
  private static final String DOD = "dod";
  private static final String LIFETIME = "lifetime";
  private static final String LIFECYCLE = "lifecycle";

  public SystemParticipantTypeInputFactory() {
    super(
        EvTypeInput.class,
        HpTypeInput.class,
        BmTypeInput.class,
        WecTypeInput.class,
        ChpTypeInput.class,
        StorageTypeInput.class);
  }

  @Override
  protected List<Set<String>> getFields(SimpleEntityData data) {
    Set<String> standardConstructorParams =
        newSet(ENTITY_UUID, ENTITY_ID, CAP_EX, OP_EX, S_RATED, COS_PHI);

    Set<String> constructorParameters = null;
    if (data.getEntityClass().equals(EvTypeInput.class)) {
      constructorParameters = expandSet(standardConstructorParams, E_STORAGE, E_CONS);
    } else if (data.getEntityClass().equals(HpTypeInput.class)) {
      constructorParameters = expandSet(standardConstructorParams, P_THERMAL);
    } else if (data.getEntityClass().equals(BmTypeInput.class)) {
      constructorParameters = expandSet(standardConstructorParams, ACTIVE_POWER_GRADIENT, ETA_CONV);
    } else if (data.getEntityClass().equals(WecTypeInput.class)) {
      constructorParameters =
          expandSet(standardConstructorParams, ETA_CONV, ROTOR_AREA, HUB_HEIGHT);
    } else if (data.getEntityClass().equals(ChpTypeInput.class)) { // into new file
      constructorParameters =
          expandSet(standardConstructorParams, ETA_EL, ETA_THERMAL, P_THERMAL, P_OWN);
    } else if (data.getEntityClass().equals(StorageTypeInput.class)) {
      constructorParameters =
          expandSet(
              standardConstructorParams,
              E_STORAGE,
              P_MAX,
              ACTIVE_POWER_GRADIENT,
              ETA,
              DOD,
              LIFETIME,
              LIFECYCLE);
    }

    return Collections.singletonList(constructorParameters);
  }

  @Override
  protected SystemParticipantTypeInput buildModel(SimpleEntityData data) {
    UUID uuid = data.getUUID(ENTITY_UUID);
    String id = data.getField(ENTITY_ID);
    ComparableQuantity<Currency> capEx = data.getQuantity(CAP_EX, StandardUnits.CAPEX); // TODO doublecheck - no return value, but superclass expects comparable
    ComparableQuantity<EnergyPrice> opEx = data.getQuantity(OP_EX, StandardUnits.ENERGY_PRICE); // TODO doublecheck - no return value, but superclass expects comparable
    ComparableQuantity<Power> sRated = data.getQuantity(S_RATED, StandardUnits.S_RATED); // TODO doublecheck - no return value, but superclass expects comparable
    double cosPhi = data.getDouble(COS_PHI);

    if (data.getEntityClass().equals(EvTypeInput.class))
      return buildEvTypeInput(data, uuid, id, capEx, opEx, sRated, cosPhi);
    else if (data.getEntityClass().equals(HpTypeInput.class))
      return buildHpTypeInput(data, uuid, id, capEx, opEx, sRated, cosPhi);
    else if (data.getEntityClass().equals(BmTypeInput.class))
      return buildBmTypeInput(data, uuid, id, capEx, opEx, sRated, cosPhi);
    else if (data.getEntityClass().equals(WecTypeInput.class))
      return buildWecTypeInput(data, uuid, id, capEx, opEx, sRated, cosPhi);
    else if (data.getEntityClass().equals(ChpTypeInput.class))
      return buildChpTypeInput(data, uuid, id, capEx, opEx, sRated, cosPhi);
    else if (data.getEntityClass().equals(StorageTypeInput.class))
      return buildStorageTypeInput(data, uuid, id, capEx, opEx, sRated, cosPhi);
    else
      throw new FactoryException(
          "SystemParticipantTypeInputFactory does not know how to build a "
              + data.getEntityClass().getName());
  }

  private SystemParticipantTypeInput buildEvTypeInput(
          SimpleEntityData data,
          UUID uuid,
          String id,
          ComparableQuantity<Currency> capEx, // TODO doublecheck - no return value, but superclass expects comparable
          ComparableQuantity<EnergyPrice> opEx, // TODO doublecheck - no return value, but superclass expects comparable
          ComparableQuantity<Power> sRated, // TODO doublecheck - no return value, but superclass expects comparable
          double cosPhi) {
    ComparableQuantity<Energy> eStorage = data.getQuantity(E_STORAGE, StandardUnits.ENERGY_IN); // TODO doublecheck - no return value, but superclass expects comparable
    ComparableQuantity<SpecificEnergy> eCons = data.getQuantity(E_CONS, StandardUnits.ENERGY_PER_DISTANCE); // TODO doublecheck - no return value, but superclass expects comparable

    return new EvTypeInput(uuid, id, capEx, opEx, eStorage, eCons, sRated, cosPhi);
  }

  private SystemParticipantTypeInput buildHpTypeInput(
      SimpleEntityData data,
      UUID uuid,
      String id,
      ComparableQuantity<Currency> capEx, // TODO doublecheck - no return value, but superclass expects comparable
      ComparableQuantity<EnergyPrice> opEx, // TODO doublecheck - no return value, but superclass expects comparable
      ComparableQuantity<Power> sRated, // TODO doublecheck - no return value, but superclass expects comparable
      double cosPhi) {
    ComparableQuantity<Power> pThermal = data.getQuantity(P_THERMAL, StandardUnits.ACTIVE_POWER_IN); // TODO doublecheck - no return value, but superclass expects comparable

    return new HpTypeInput(uuid, id, capEx, opEx, sRated, cosPhi, pThermal);
  }

  private SystemParticipantTypeInput buildBmTypeInput(
      SimpleEntityData data,
      UUID uuid,
      String id,
      ComparableQuantity<Currency> capEx, // TODO doublecheck - no return value, but superclass expects comparable
      ComparableQuantity<EnergyPrice> opEx, // TODO doublecheck - no return value, but superclass expects comparable
      ComparableQuantity<Power> sRated, // TODO doublecheck - no return value, but superclass expects comparable
      double cosPhi) {
    ComparableQuantity<DimensionlessRate> loadGradient = // TODO doublecheck - no return value, but superclass expects comparable
        data.getQuantity(ACTIVE_POWER_GRADIENT, StandardUnits.ACTIVE_POWER_GRADIENT);
    ComparableQuantity<Dimensionless> etaConv = data.getQuantity(ETA_CONV, StandardUnits.EFFICIENCY); // TODO doublecheck - no return value, but superclass expects comparable

    return new BmTypeInput(uuid, id, capEx, opEx, loadGradient, sRated, cosPhi, etaConv);
  }

  private SystemParticipantTypeInput buildWecTypeInput(
      SimpleEntityData data,
      UUID uuid,
      String id,
      ComparableQuantity<Currency> capEx, // TODO doublecheck - no return value, but superclass expects comparable
      ComparableQuantity<EnergyPrice> opEx, // TODO doublecheck - no return value, but superclass expects comparable
      ComparableQuantity<Power> sRated, // TODO doublecheck - no return value, but superclass expects comparable
      double cosPhi) {
    ComparableQuantity<Dimensionless> etaConv = data.getQuantity(ETA_CONV, StandardUnits.EFFICIENCY); // TODO doublecheck - no return value, but superclass expects comparable
    ComparableQuantity<Area> rotorArea = data.getQuantity(ROTOR_AREA, StandardUnits.ROTOR_AREA); // TODO doublecheck - no return value, but superclass expects comparable
    ComparableQuantity<Length> hubHeight = data.getQuantity(HUB_HEIGHT, StandardUnits.HUB_HEIGHT); // TODO doublecheck - no return value, but superclass expects comparable

    return new WecTypeInput(uuid, id, capEx, opEx, cosPhi, etaConv, sRated, rotorArea, hubHeight);
  }

  private SystemParticipantTypeInput buildChpTypeInput(
      SimpleEntityData data,
      UUID uuid,
      String id,
      ComparableQuantity<Currency> capEx, // TODO doublecheck - no return value, but superclass expects comparable
      ComparableQuantity<EnergyPrice> opEx, // TODO doublecheck - no return value, but superclass expects comparable
      ComparableQuantity<Power> sRated, // TODO doublecheck - no return value, but superclass expects comparable
      double cosPhi) {
    ComparableQuantity<Dimensionless> etaEl = data.getQuantity(ETA_EL, StandardUnits.EFFICIENCY); // TODO doublecheck - no return value, but superclass expects comparable
    ComparableQuantity<Dimensionless> etaThermal = data.getQuantity(ETA_THERMAL, StandardUnits.EFFICIENCY); // TODO doublecheck - no return value, but superclass expects comparable
    ComparableQuantity<Power> pThermal = data.getQuantity(P_THERMAL, StandardUnits.ACTIVE_POWER_IN); // TODO doublecheck - no return value, but superclass expects comparable
    ComparableQuantity<Power> pOwn = data.getQuantity(P_OWN, StandardUnits.ACTIVE_POWER_IN); // TODO doublecheck - no return value, but superclass expects comparable

    return new ChpTypeInput(
        uuid, id, capEx, opEx, etaEl, etaThermal, sRated, cosPhi, pThermal, pOwn);
  }

  private SystemParticipantTypeInput buildStorageTypeInput(
      SimpleEntityData data,
      UUID uuid,
      String id,
      ComparableQuantity<Currency> capEx, // TODO doublecheck
      ComparableQuantity<EnergyPrice> opEx, // TODO doublecheck
      ComparableQuantity<Power> sRated, // TODO doublecheck
      double cosPhi) {
    ComparableQuantity<Energy> eStorage = data.getQuantity(E_STORAGE, StandardUnits.ENERGY_IN); // TODO doublecheck
    ComparableQuantity<Power> pMax = data.getQuantity(P_MAX, StandardUnits.ACTIVE_POWER_IN); // TODO doublecheck
    ComparableQuantity<DimensionlessRate> activePowerGradient = // TODO doublecheck
        data.getQuantity(ACTIVE_POWER_GRADIENT, StandardUnits.ACTIVE_POWER_GRADIENT);
    ComparableQuantity<Dimensionless> eta = data.getQuantity(ETA, StandardUnits.EFFICIENCY); // TODO doublecheck
    ComparableQuantity<Dimensionless> dod = data.getQuantity(DOD, StandardUnits.DOD); // TODO doublecheck
    ComparableQuantity<Time> lifeTime = data.getQuantity(LIFETIME, StandardUnits.LIFE_TIME); // TODO doublecheck
    int lifeCycle = data.getInt(LIFECYCLE);

    return new StorageTypeInput(
        uuid,
        id,
        capEx,
        opEx,
        eStorage,
        sRated,
        cosPhi,
        pMax,
        activePowerGradient,
        eta,
        dod,
        lifeTime,
        lifeCycle);
  }
}
