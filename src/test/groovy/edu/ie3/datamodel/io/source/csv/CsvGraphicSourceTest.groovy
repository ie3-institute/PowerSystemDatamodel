/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
 */
package edu.ie3.datamodel.io.source.csv

import edu.ie3.datamodel.io.FileNamingStrategy
import edu.ie3.datamodel.io.factory.input.graphics.LineGraphicInputEntityData
import edu.ie3.datamodel.io.factory.input.graphics.NodeGraphicInputEntityData
import edu.ie3.datamodel.io.source.RawGridSource
import edu.ie3.datamodel.models.input.NodeInput
import edu.ie3.datamodel.models.input.graphics.NodeGraphicInput
import edu.ie3.test.common.GridTestData as gtd
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.Point
import spock.lang.Specification

class CsvGraphicSourceTest extends Specification implements CsvTestDataMeta {


	def "A CsvGraphicSource should provide an instance of GraphicElements based on valid input data correctly"() {
		given:
		def typeSource = new CsvTypeSource(csvSep, typeFolderPath, fileNamingStrategy)
		def rawGridSource = new CsvRawGridSource(csvSep, gridFolderPath, fileNamingStrategy, typeSource)
		def csvGraphicSource = new CsvGraphicSource(csvSep, graphicsFolderPath, fileNamingStrategy, typeSource, rawGridSource)

		when:
		def graphicElementsOpt = csvGraphicSource.getGraphicElements()

		then:
		graphicElementsOpt.isPresent()
		graphicElementsOpt.ifPresent({ graphicElements ->
			assert (graphicElements.allEntitiesAsList().size() == 3)
			assert (graphicElements.nodeGraphics.size() == 2)
			assert (graphicElements.lineGraphics.size() == 1)
		})
	}

	def "A CsvGraphicSource should process invalid input data correctly when requested to provide an instance of GraphicElements"() {
		given:
		def typeSource = new CsvTypeSource(csvSep, typeFolderPath, fileNamingStrategy)
		def rawGridSource = Spy(CsvRawGridSource, constructorArgs: [
			csvSep,
			gridFolderPath,
			fileNamingStrategy,
			typeSource
		]) {
			// partly fake the return method of the csv raw grid source to always return empty node sets
			// -> elements to build NodeGraphicInputs are missing
			getNodes() >> new HashSet<NodeInput>()
			getNodes(_) >> new HashSet<NodeInput>()
		}

		def csvGraphicSource = new CsvGraphicSource(csvSep, graphicsFolderPath, fileNamingStrategy, typeSource, rawGridSource as RawGridSource)

		when:
		def graphicElementsOpt = csvGraphicSource.getGraphicElements()

		then:
		!graphicElementsOpt.isPresent()
	}


	def "A CsvGraphicSource should read and handle a valid node graphics file as expected"() {
		given:
		def csvGraphicSource = new CsvGraphicSource(csvSep, graphicsFolderPath, fileNamingStrategy, Mock(CsvTypeSource), Mock(CsvRawGridSource))
		def expectedNodeGraphicD = new NodeGraphicInput(
				gtd.nodeGraphicD.uuid,
				gtd.nodeGraphicD.graphicLayer,
				gtd.nodeGraphicD.path,
				gtd.nodeD,
				gtd.geoJsonReader.read("{ \"type\": \"Point\", \"coordinates\": [7.4116482, 51.4843281] }") as Point
				)
		def expectedNodeGraphicC = new NodeGraphicInput(
				gtd.nodeGraphicC.uuid,
				gtd.nodeGraphicC.graphicLayer,
				gtd.geoJsonReader.read("{ \"type\": \"LineString\", \"coordinates\": [[7.4116482, 51.4843281], [7.4116482, 51.4843281]]}") as LineString,
				gtd.nodeC,
				gtd.nodeGraphicC.point
				)

		when:
		def nodeGraphics = csvGraphicSource.getNodeGraphicInput([gtd.nodeC, gtd.nodeD] as Set)

		then:
		nodeGraphics.size() == 2
		nodeGraphics == [
			expectedNodeGraphicC,
			expectedNodeGraphicD] as Set
	}

	def "A CsvGraphicSource should read and handle a valid line graphics file as expected"() {
		given:
		def csvGraphicSource = new CsvGraphicSource(csvSep, graphicsFolderPath, fileNamingStrategy, Mock(CsvTypeSource), Mock(CsvRawGridSource))

		when:
		def lineGraphics = csvGraphicSource.getLineGraphicInput([gtd.lineCtoD] as Set)

		then:
		lineGraphics.size() == 1
		lineGraphics.first() == gtd.lineGraphicCtoD
	}

	def "A CsvGraphicSource should build node graphic entity data for valid and invalid data correctly"() {
		given:
		def csvGraphicSource = new CsvGraphicSource(csvSep, graphicsFolderPath, fileNamingStrategy, Mock(CsvTypeSource), Mock(CsvRawGridSource))
		def fieldsToAttributesMap = [
			"uuid"         : "09aec636-791b-45aa-b981-b14edf171c4c",
			"graphic_layer": "main",
			"node"         : "bd837a25-58f3-44ac-aa90-c6b6e3cd91b2",
			"path"         : "",
			"point"        : "{\"type\":\"Point\",\"coordinates\":[0.0,10],\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:4326\"}}}"
		]

		expect:
		def res = csvGraphicSource.buildNodeGraphicEntityData(fieldsToAttributesMap, nodeCollection as Set)
		res.isPresent() == isPresent

		res.ifPresent({ value ->
			assert value == new NodeGraphicInputEntityData([
				"uuid"         : "09aec636-791b-45aa-b981-b14edf171c4c",
				"graphic_layer": "main",
				"path"         : "",
				"point"        : "{\"type\":\"Point\",\"coordinates\":[0.0,10],\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:4326\"}}}"
			], gtd.nodeC)
			assert value.node == gtd.nodeC
		})


		where:
		nodeCollection         || isPresent
		[]|| false     // no nodes provide
		[gtd.nodeA, gtd.nodeB]|| false     // node cannot be found
		[gtd.nodeC]|| true      // node found

	}

	def "A CsvGraphicSource should build line graphic entity data for valid and invalid data correctly"() {
		given:
		def csvGraphicSource = new CsvGraphicSource(csvSep, graphicsFolderPath, fileNamingStrategy, Mock(CsvTypeSource), Mock(CsvRawGridSource))
		def fieldsToAttributesMap = [
			"uuid"         : "ece86139-3238-4a35-9361-457ecb4258b0",
			"graphic_layer": "main",
			"line"         : "92ec3bcf-1777-4d38-af67-0bf7c9fa73c7",
			"path"         : "{\"type\":\"LineString\",\"coordinates\":[[0.0,0.0],[0.0,10]],\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:4326\"}}}"
		]

		expect:
		def res = csvGraphicSource.buildLineGraphicEntityData(fieldsToAttributesMap, nodeCollection as Set)
		res.isPresent() == isPresent

		res.ifPresent({ value ->
			assert value == new LineGraphicInputEntityData([
				"uuid"         : "09aec636-791b-45aa-b981-b14edf171c4c",
				"graphic_layer": "main",
				"path"         : "",
				"point"        : "{\"type\":\"Point\",\"coordinates\":[0.0,10],\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:4326\"}}}"
			], gtd.lineAtoB)
			assert value.line == gtd.lineAtoB
		})


		where:
		nodeCollection || isPresent
		[]|| false     // no nodes provide
		[gtd.lineCtoD]|| false     // line cannot be found
		[gtd.lineAtoB]|| true      // line found

	}
}