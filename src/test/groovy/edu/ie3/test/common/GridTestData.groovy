package edu.ie3.test.common

import edu.ie3.datamodel.models.OperationTime
import edu.ie3.datamodel.models.input.NodeInput
import edu.ie3.datamodel.models.input.OperatorInput
import edu.ie3.datamodel.models.input.connector.Transformer2WInput
import edu.ie3.datamodel.models.input.connector.Transformer3WInput
import edu.ie3.datamodel.models.input.connector.type.Transformer2WTypeInput
import edu.ie3.datamodel.models.input.connector.type.Transformer3WTypeInput
import edu.ie3.datamodel.models.voltagelevels.GermanVoltageLevelUtils
import tec.uom.se.quantity.Quantities
import tec.uom.se.unit.MetricPrefix

import static edu.ie3.util.quantities.PowerSystemUnits.DEGREE_GEOM
import static edu.ie3.util.quantities.PowerSystemUnits.KILOVOLT
import static edu.ie3.util.quantities.PowerSystemUnits.KILOVOLTAMPERE
import static edu.ie3.util.quantities.PowerSystemUnits.PU
import static tec.uom.se.unit.Units.OHM
import static tec.uom.se.unit.Units.PERCENT
import static tec.uom.se.unit.Units.SIEMENS

class GridTestData {

    private static final Transformer2WTypeInput transformerTypeBtoD = new Transformer2WTypeInput(
            UUID.randomUUID(),
            "HS-MS_1",
            Quantities.getQuantity(45.375, OHM),
            Quantities.getQuantity(102.759, OHM),
            Quantities.getQuantity(20000d, KILOVOLTAMPERE),
            Quantities.getQuantity(110d, KILOVOLT),
            Quantities.getQuantity(20d, KILOVOLT),
            Quantities.getQuantity(0d, MetricPrefix.MICRO(SIEMENS)),
            Quantities.getQuantity(0d, MetricPrefix.MICRO(SIEMENS)),
            Quantities.getQuantity(1.5, PERCENT),
            Quantities.getQuantity(0d, DEGREE_GEOM),
            false,
            0,
            -10,
            10
    )
    private static final Transformer2WTypeInput transformerTypeBtoE = new Transformer2WTypeInput(
            UUID.randomUUID(),
            "transformer_type_gedfi89fc7c895076ff25ec6d3b2e7ab9a1b24b37f73ecf30f895005d766a8d8d2774aa",
            Quantities.getQuantity(0d, OHM),
            Quantities.getQuantity(51.72750115394592, OHM),
            Quantities.getQuantity(40000d, KILOVOLTAMPERE),
            Quantities.getQuantity(110d, KILOVOLT),
            Quantities.getQuantity(10d, KILOVOLT),
            Quantities.getQuantity(0d, MetricPrefix.MICRO(SIEMENS)),
            Quantities.getQuantity(0d, MetricPrefix.MICRO(SIEMENS)),
            Quantities.getQuantity(1.777780055999756, PERCENT),
            Quantities.getQuantity(0d, DEGREE_GEOM),
            false,
            10,
            1,
            19
    )
    private static final Transformer2WTypeInput transformerTypeCtoE = new Transformer2WTypeInput(
            UUID.randomUUID(),
            "no_shunt_elements_mv-mv",
            Quantities.getQuantity(1.5, OHM),
            Quantities.getQuantity(15.5, OHM),
            Quantities.getQuantity(250d, KILOVOLTAMPERE),
            Quantities.getQuantity(20d, KILOVOLT),
            Quantities.getQuantity(10d, KILOVOLT),
            Quantities.getQuantity(0d, MetricPrefix.MICRO(SIEMENS)),
            Quantities.getQuantity(0d, MetricPrefix.MICRO(SIEMENS)),
            Quantities.getQuantity(1.5, PERCENT),
            Quantities.getQuantity(0d, DEGREE_GEOM),
            false,
            0,
            -5,
            5
    )
    private static final Transformer2WTypeInput transformerTypeCtoX = new Transformer2WTypeInput(
            UUID.randomUUID(),
            "MS-NS_1",
            Quantities.getQuantity(10.078, OHM),
            Quantities.getQuantity(23.312, OHM),
            Quantities.getQuantity(630d, KILOVOLTAMPERE),
            Quantities.getQuantity(20d, KILOVOLT),
            Quantities.getQuantity(0.4, KILOVOLT),
            Quantities.getQuantity(0d, MetricPrefix.MICRO(SIEMENS)),
            Quantities.getQuantity(0d, MetricPrefix.MICRO(SIEMENS)),
            Quantities.getQuantity(0.5, PERCENT),
            Quantities.getQuantity(0d, DEGREE_GEOM),
            false,
            0,
            -10,
            10
    )

    private static final Transformer3WTypeInput transformerTypeAtoBtoC = new Transformer3WTypeInput(
            UUID.randomUUID(),
            "HöS-HS-MS_1",
            Quantities.getQuantity(120000d, KILOVOLTAMPERE),
            Quantities.getQuantity(60000d, KILOVOLTAMPERE),
            Quantities.getQuantity(40000d, KILOVOLTAMPERE),
            Quantities.getQuantity(380d, KILOVOLT),
            Quantities.getQuantity(110d, KILOVOLT),
            Quantities.getQuantity(20d, KILOVOLT),
            Quantities.getQuantity(0.3, OHM),
            Quantities.getQuantity(0.025, OHM),
            Quantities.getQuantity(0.0008, OHM),
            Quantities.getQuantity(1d, OHM),
            Quantities.getQuantity(0.08, OHM),
            Quantities.getQuantity(0.003, OHM),
            Quantities.getQuantity(40, MetricPrefix.MICRO(SIEMENS)),
            Quantities.getQuantity(1d, MetricPrefix.MICRO(SIEMENS)),
            Quantities.getQuantity(1.5, PERCENT),
            Quantities.getQuantity(0d, DEGREE_GEOM),
            0,
            -10,
            10
    )

    public static final NodeInput nodeA = new NodeInput(
            UUID.randomUUID(),
            OperationTime.notLimited(),
            OperatorInput.NO_OPERATOR_ASSIGNED,
            "node_a",
            Quantities.getQuantity(1d, PU),
            true,
            null,
            GermanVoltageLevelUtils.EHV_380KV,
            1)
    public static final NodeInput nodeB = new NodeInput(
            UUID.randomUUID(),
            OperationTime.notLimited(),
            OperatorInput.NO_OPERATOR_ASSIGNED,
            "node_b",
            Quantities.getQuantity(1d, PU),
            false,
            null,
            GermanVoltageLevelUtils.HV,
            2)
    public static final NodeInput nodeC = new NodeInput(
            UUID.randomUUID(),
            OperationTime.notLimited(),
            OperatorInput.NO_OPERATOR_ASSIGNED,
            "node_c",
            Quantities.getQuantity(1d, PU),
            false,
            null,
            GermanVoltageLevelUtils.MV_20KV,
            3)
    public static final NodeInput nodeD = new NodeInput(
            UUID.randomUUID(),
            OperationTime.notLimited(),
            OperatorInput.NO_OPERATOR_ASSIGNED,
            "node_d",
            Quantities.getQuantity(1d, PU),
            false,
            null,
            GermanVoltageLevelUtils.MV_20KV,
            4)
    public static final NodeInput nodeE = new NodeInput(
            UUID.randomUUID(),
            OperationTime.notLimited(),
            OperatorInput.NO_OPERATOR_ASSIGNED,
            "node_e",
            Quantities.getQuantity(1d, PU),
            false,
            null,
            GermanVoltageLevelUtils.MV_10KV,
            5)
    public static final NodeInput nodeF = new NodeInput(
            UUID.randomUUID(),
            OperationTime.notLimited(),
            OperatorInput.NO_OPERATOR_ASSIGNED,
            "node_f",
            Quantities.getQuantity(1d, PU),
            false,
            null,
            GermanVoltageLevelUtils.LV,
            6)
    public static final NodeInput nodeG = new NodeInput(
            UUID.randomUUID(),
            OperationTime.notLimited(),
            OperatorInput.NO_OPERATOR_ASSIGNED,
            "node_g",
            Quantities.getQuantity(1d, PU),
            false,
            null,
            GermanVoltageLevelUtils.LV,
            6)

    public static final Transformer2WInput transformerBtoD = new Transformer2WInput(
            UUID.randomUUID(),
            OperationTime.notLimited(),
            OperatorInput.NO_OPERATOR_ASSIGNED,
            "2w_single_test",
            nodeB,
            nodeD,
            1,
            transformerTypeBtoD,
            0,
            true
    )
    public static final Transformer2WInput transformerBtoE = new Transformer2WInput(
            UUID.randomUUID(),
            OperationTime.notLimited(),
            OperatorInput.NO_OPERATOR_ASSIGNED,
            "2w_v_1",
            nodeB,
            nodeE,
            1,
            transformerTypeBtoE,
            0,
            true
    )
    public static final Transformer2WInput transformerCtoE = new Transformer2WInput(
            UUID.randomUUID(),
            OperationTime.notLimited(),
            OperatorInput.NO_OPERATOR_ASSIGNED,
            "2w_v_2",
            nodeC,
            nodeE,
            1,
            transformerTypeCtoE,
            0,
            true
    )
    public static final Transformer2WInput transformerCtoF = new Transformer2WInput(
            UUID.randomUUID(),
            OperationTime.notLimited(),
            OperatorInput.NO_OPERATOR_ASSIGNED,
            "2w_parallel_1",
            nodeC,
            nodeF,
            1,
            transformerTypeCtoX,
            0,
            true
    )
    public static final Transformer2WInput transformerCtoG = new Transformer2WInput(
            UUID.randomUUID(),
            OperationTime.notLimited(),
            OperatorInput.NO_OPERATOR_ASSIGNED,
            "2w_parallel_2",
            nodeC,
            nodeF,
            1,
            transformerTypeCtoX,
            0,
            true
    )

    public static Transformer3WInput transformerAtoBtoC = new Transformer3WInput(
            UUID.randomUUID(),
            OperationTime.notLimited(),
            OperatorInput.NO_OPERATOR_ASSIGNED,
            "3w_test",
            nodeA,
            nodeB,
            nodeC,
            1,
            transformerTypeAtoBtoC,
            0,
            true
    )
}