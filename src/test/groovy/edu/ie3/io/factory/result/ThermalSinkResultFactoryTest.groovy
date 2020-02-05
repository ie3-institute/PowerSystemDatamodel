package edu.ie3.io.factory.result

import edu.ie3.io.factory.SimpleEntityData
import edu.ie3.models.StandardUnits
import edu.ie3.models.result.ThermalSinkResult
import edu.ie3.util.TimeTools
import spock.lang.Specification
import tec.uom.se.quantity.Quantities

class ThermalSinkResultFactoryTest extends Specification {

    def "A ThermalSinkResultFactory should contain all expected classes for parsing"() {
        given:
        def resultFactory = new ThermalSinkResultFactory()
        def expectedClasses = [ThermalSinkResult]

        expect:
        resultFactory.classes() == Arrays.asList(expectedClasses.toArray())
    }

    def "A ThermalSinkResultFactory should parse a WecResult correctly"() {
        given: "a system participant factory and model data"
        def resultFactory = new ThermalSinkResultFactory()

        Map<String, String> parameter = [:]
        parameter["timestamp"] = "2020-01-30 17:26:44"
        parameter["inputModel"] = "91ec3bcf-1897-4d38-af67-0bf7c9fa73c7"
        parameter["qdemand"] = "2"

        when:
        Optional<? extends ThermalSinkResult> result = resultFactory.getEntity(new SimpleEntityData(parameter, ThermalSinkResult))

        then:
        result.present
        result.get().getClass() == ThermalSinkResult
        result.get().qDemand == Quantities.getQuantity(Double.parseDouble(parameter["qdemand"]),  StandardUnits.HEAT_CAPACITY)
        result.get().timestamp == TimeTools.toZonedDateTime(parameter["timestamp"])
        result.get().inputModel == UUID.fromString(parameter["inputModel"])

    }
}
