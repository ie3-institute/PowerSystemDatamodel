/*
 * © 2021. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
 */
package edu.ie3.datamodel.io.source.csv

import edu.ie3.datamodel.io.naming.EntityPersistenceNamingStrategy

/**
 * Holds meta data for csv tests e.g. file and folder paths
 */
trait CsvTestDataMeta {

	static String testParticipantsBaseFolderPath = new File(getClass().getResource('/testGridFiles').toURI()).absolutePath
	static String testTimeSeriesBaseFolderPath = new File(getClass().getResource('/testTimeSeriesFiles').toURI()).absolutePath
	static String graphicsFolderPath = testParticipantsBaseFolderPath.concat(File.separator).concat("graphics")
	static String typeFolderPath = testParticipantsBaseFolderPath.concat(File.separator).concat("types")
	static String gridFolderPath = testParticipantsBaseFolderPath.concat(File.separator).concat("grid")
	static String participantsFolderPath =  testParticipantsBaseFolderPath.concat(File.separator).concat("participants")
	static String timeSeriesFolderPath =  testTimeSeriesBaseFolderPath
	static String weatherFolderPath = new File(getClass().getResource('/weather').toURI()).absolutePath
	static String cosmoWeatherFolderPath = weatherFolderPath.concat(File.separator).concat("cosmo")
	static String iconWeatherFolderPath = weatherFolderPath.concat(File.separator).concat("icon")
	static String thermalFolderPath = testParticipantsBaseFolderPath.concat(File.separator).concat("thermal")
	static String coordinatesFolderPath = testParticipantsBaseFolderPath.concat(File.separator).concat("coordinates")

	static String csvSep = ","
	static EntityPersistenceNamingStrategy entityPersistenceNamingStrategy = new EntityPersistenceNamingStrategy()
}