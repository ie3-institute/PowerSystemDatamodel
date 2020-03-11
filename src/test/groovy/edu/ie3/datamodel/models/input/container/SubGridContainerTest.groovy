package edu.ie3.datamodel.models.input.container

import static  edu.ie3.util.quantities.PowerSystemUnits.PU

import edu.ie3.datamodel.exceptions.AggregationException
import edu.ie3.datamodel.models.voltagelevels.GermanVoltageLevelUtils
import edu.ie3.datamodel.models.voltagelevels.VoltageLevel

import edu.ie3.datamodel.models.OperationTime
import edu.ie3.datamodel.models.input.NodeInput
import edu.ie3.datamodel.models.input.OperatorInput
import spock.lang.Shared
import spock.lang.Specification
import tec.uom.se.quantity.Quantities

class SubGridContainerTest extends Specification {
    @Shared
    RawGridElements rawGridElements = new RawGridElements()

    @Shared
    RawGridElements emptyRawGridElements = new RawGridElements()

    def setupSpec() {
        rawGridElements.add(new NodeInput(
                UUID.randomUUID(),
                OperationTime.notLimited(),
                OperatorInput.NO_OPERATOR_ASSIGNED,
                "test_node_1",
                Quantities.getQuantity(1d, PU),
                false,
                null,
                GermanVoltageLevelUtils.LV,
                1))
        rawGridElements.add(new NodeInput(
                UUID.randomUUID(),
                OperationTime.notLimited(),
                OperatorInput.NO_OPERATOR_ASSIGNED,
                "test_node_2",
                Quantities.getQuantity(1d, PU),
                false,
                null,
                GermanVoltageLevelUtils.LV,
                1))
        rawGridElements.add(new NodeInput(
                UUID.randomUUID(),
                OperationTime.notLimited(),
                OperatorInput.NO_OPERATOR_ASSIGNED,
                "test_node_3",
                Quantities.getQuantity(1d, PU),
                false,
                null,
                GermanVoltageLevelUtils.MV_10KV,
                0))
    }

    def "The SingleGridContainer should determine the predominant voltage level correctly"() {
        when:
        VoltageLevel actual = SubGridContainer.determinePredominantVoltLvl(rawGridElements)

        then:
        actual == GermanVoltageLevelUtils.LV
    }

    def "The SingleGridContainer should throw an exception, when the there is no voltage level information at all"() {
        when:
        SubGridContainer.determinePredominantVoltLvl(emptyRawGridElements)

        then:
        AggregationException ex = thrown()
        ex.message == "Cannot determine the predominant voltage level."
    }
}