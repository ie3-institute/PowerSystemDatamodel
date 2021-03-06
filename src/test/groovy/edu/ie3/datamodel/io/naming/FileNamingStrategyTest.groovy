/*
 * © 2021. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
 */
package edu.ie3.datamodel.io.naming

import edu.ie3.datamodel.io.csv.timeseries.ColumnScheme
import edu.ie3.datamodel.io.csv.timeseries.IndividualTimeSeriesMetaInformation
import edu.ie3.datamodel.io.csv.timeseries.LoadProfileTimeSeriesMetaInformation
import edu.ie3.datamodel.io.source.TimeSeriesMappingSource
import edu.ie3.datamodel.models.BdewLoadProfile
import edu.ie3.datamodel.models.UniqueEntity
import edu.ie3.datamodel.models.input.MeasurementUnitInput
import edu.ie3.datamodel.models.input.NodeInput
import edu.ie3.datamodel.models.input.RandomLoadParameters
import edu.ie3.datamodel.models.input.connector.LineInput
import edu.ie3.datamodel.models.input.connector.SwitchInput
import edu.ie3.datamodel.models.input.connector.Transformer2WInput
import edu.ie3.datamodel.models.input.connector.Transformer3WInput
import edu.ie3.datamodel.models.input.connector.type.LineTypeInput
import edu.ie3.datamodel.models.input.connector.type.Transformer2WTypeInput
import edu.ie3.datamodel.models.input.connector.type.Transformer3WTypeInput
import edu.ie3.datamodel.models.input.graphics.LineGraphicInput
import edu.ie3.datamodel.models.input.graphics.NodeGraphicInput
import edu.ie3.datamodel.models.input.system.BmInput
import edu.ie3.datamodel.models.input.system.ChpInput
import edu.ie3.datamodel.models.input.system.EvInput
import edu.ie3.datamodel.models.input.system.EvcsInput
import edu.ie3.datamodel.models.input.system.FixedFeedInInput
import edu.ie3.datamodel.models.input.system.HpInput
import edu.ie3.datamodel.models.input.system.LoadInput
import edu.ie3.datamodel.models.input.system.PvInput
import edu.ie3.datamodel.models.input.system.StorageInput
import edu.ie3.datamodel.models.input.system.WecInput
import edu.ie3.datamodel.models.input.system.type.BmTypeInput
import edu.ie3.datamodel.models.input.system.type.ChpTypeInput
import edu.ie3.datamodel.models.input.system.type.EvTypeInput
import edu.ie3.datamodel.models.input.system.type.HpTypeInput
import edu.ie3.datamodel.models.input.system.type.StorageTypeInput
import edu.ie3.datamodel.models.input.system.type.WecTypeInput
import edu.ie3.datamodel.models.input.thermal.CylindricalStorageInput
import edu.ie3.datamodel.models.input.thermal.ThermalHouseInput
import edu.ie3.datamodel.models.result.NodeResult
import edu.ie3.datamodel.models.result.connector.LineResult
import edu.ie3.datamodel.models.result.connector.SwitchResult
import edu.ie3.datamodel.models.result.connector.Transformer2WResult
import edu.ie3.datamodel.models.result.connector.Transformer3WResult
import edu.ie3.datamodel.models.result.system.BmResult
import edu.ie3.datamodel.models.result.system.ChpResult
import edu.ie3.datamodel.models.result.system.EvResult
import edu.ie3.datamodel.models.result.system.EvcsResult
import edu.ie3.datamodel.models.result.system.FixedFeedInResult
import edu.ie3.datamodel.models.result.system.LoadResult
import edu.ie3.datamodel.models.result.system.PvResult
import edu.ie3.datamodel.models.result.system.StorageResult
import edu.ie3.datamodel.models.result.system.WecResult
import edu.ie3.datamodel.models.result.thermal.CylindricalStorageResult
import edu.ie3.datamodel.models.result.thermal.ThermalHouseResult
import edu.ie3.datamodel.models.timeseries.individual.IndividualTimeSeries
import edu.ie3.datamodel.models.timeseries.individual.TimeBasedValue
import edu.ie3.datamodel.models.timeseries.repetitive.LoadProfileInput
import edu.ie3.datamodel.models.value.EnergyPriceValue
import edu.ie3.util.quantities.PowerSystemUnits
import spock.lang.Shared
import spock.lang.Specification
import tech.units.indriya.quantity.Quantities

import java.nio.file.Files
import java.nio.file.Paths
import java.time.ZonedDateTime

class FileNamingStrategyTest extends Specification {


	@Shared
	DefaultDirectoryHierarchy defaultHierarchy
	FlatDirectoryHierarchy flatHierarchy
	EntityPersistenceNamingStrategy simpleEntityNaming

	def setup() {
		def tmpPath = Files.createTempDirectory("psdm_file_naming_strategy")
		defaultHierarchy = new DefaultDirectoryHierarchy(tmpPath.toString(), "test_grid")
		flatHierarchy = new FlatDirectoryHierarchy()
		simpleEntityNaming = new EntityPersistenceNamingStrategy()
	}


	// TESTS FOR DEFAULT HIERARCHY

	def "A FileNamingStrategy with DefaultHierarchy and without pre- or suffixes should return valid directory paths for all result models"() {
		given: "a file naming strategy without pre- or suffixes"
		def strategy = new FileNamingStrategy(simpleEntityNaming, defaultHierarchy)

		when:
		def res = strategy.getDirectoryPath(modelClass)

		then:
		res.present
		res.get() == expectedString

		where:
		modelClass               || expectedString
		LoadResult               || "test_grid" + File.separator + "results" + File.separator + "participants"
		FixedFeedInResult        || "test_grid" + File.separator + "results" + File.separator + "participants"
		BmResult                 || "test_grid" + File.separator + "results" + File.separator + "participants"
		PvResult                 || "test_grid" + File.separator + "results" + File.separator + "participants"
		ChpResult                || "test_grid" + File.separator + "results" + File.separator + "participants"
		WecResult                || "test_grid" + File.separator + "results" + File.separator + "participants"
		StorageResult            || "test_grid" + File.separator + "results" + File.separator + "participants"
		EvcsResult               || "test_grid" + File.separator + "results" + File.separator + "participants"
		EvResult                 || "test_grid" + File.separator + "results" + File.separator + "participants"
		Transformer2WResult      || "test_grid" + File.separator + "results" + File.separator + "grid"
		Transformer3WResult      || "test_grid" + File.separator + "results" + File.separator + "grid"
		LineResult               || "test_grid" + File.separator + "results" + File.separator + "grid"
		SwitchResult             || "test_grid" + File.separator + "results" + File.separator + "grid"
		NodeResult               || "test_grid" + File.separator + "results" + File.separator + "grid"
		CylindricalStorageResult || "test_grid" + File.separator + "results" + File.separator + "thermal"
		ThermalHouseResult       || "test_grid" + File.separator + "results" + File.separator + "thermal"
	}

	def "A FileNamingStrategy with DefaultHierarchy and without pre- or suffixes should return valid directory paths for all input assets models"() {
		given: "a file naming strategy without pre- or suffixes"
		def strategy = new FileNamingStrategy(simpleEntityNaming, defaultHierarchy)

		when:
		def res = strategy.getDirectoryPath(modelClass)

		then:
		res.present
		res.get() == expectedString

		where:
		modelClass              || expectedString
		FixedFeedInInput        || "test_grid" + File.separator + "input" + File.separator + "participants"
		PvInput                 || "test_grid" + File.separator + "input" + File.separator + "participants"
		WecInput                || "test_grid" + File.separator + "input" + File.separator + "participants"
		ChpInput                || "test_grid" + File.separator + "input" + File.separator + "participants"
		BmInput                 || "test_grid" + File.separator + "input" + File.separator + "participants"
		EvInput                 || "test_grid" + File.separator + "input" + File.separator + "participants"
		EvcsInput               || "test_grid" + File.separator + "input" + File.separator + "participants"
		LoadInput               || "test_grid" + File.separator + "input" + File.separator + "participants"
		StorageInput            || "test_grid" + File.separator + "input" + File.separator + "participants"
		HpInput                 || "test_grid" + File.separator + "input" + File.separator + "participants"
		LineInput               || "test_grid" + File.separator + "input" + File.separator + "grid"
		SwitchInput             || "test_grid" + File.separator + "input" + File.separator + "grid"
		NodeInput               || "test_grid" + File.separator + "input" + File.separator + "grid"
		MeasurementUnitInput    || "test_grid" + File.separator + "input" + File.separator + "grid"
		Transformer2WInput      || "test_grid" + File.separator + "input" + File.separator + "grid"
		Transformer3WInput      || "test_grid" + File.separator + "input" + File.separator + "grid"
		CylindricalStorageInput || "test_grid" + File.separator + "input" + File.separator + "thermal"
		ThermalHouseInput       || "test_grid" + File.separator + "input" + File.separator + "thermal"
	}

	def "A FileNamingStrategy with DefaultHierarchy and without pre- or suffixes should return valid directory paths for all input types models"() {
		given: "a file naming strategy without pre- or suffixes"
		def strategy = new FileNamingStrategy(simpleEntityNaming, defaultHierarchy)

		when:
		def res = strategy.getDirectoryPath(modelClass)

		then:
		res.present
		res.get() == expectedString

		where:
		modelClass             || expectedString
		BmTypeInput            || "test_grid" + File.separator + "input" + File.separator + "global"
		ChpTypeInput           || "test_grid" + File.separator + "input" + File.separator + "global"
		EvTypeInput            || "test_grid" + File.separator + "input" + File.separator + "global"
		HpTypeInput            || "test_grid" + File.separator + "input" + File.separator + "global"
		StorageTypeInput       || "test_grid" + File.separator + "input" + File.separator + "global"
		WecTypeInput           || "test_grid" + File.separator + "input" + File.separator + "global"
		LineTypeInput          || "test_grid" + File.separator + "input" + File.separator + "global"
		Transformer2WTypeInput || "test_grid" + File.separator + "input" + File.separator + "global"
		Transformer3WTypeInput || "test_grid" + File.separator + "input" + File.separator + "global"
	}

	def "A FileNamingStrategy with DefaultHierarchy and without pre- or suffixes should return valid directory paths for a graphic input Model"() {
		given: "a file naming strategy without pre- or suffixes"
		def strategy = new FileNamingStrategy(simpleEntityNaming, defaultHierarchy)

		when:
		def res = strategy.getDirectoryPath(modelClass)

		then:
		res.present
		res.get() == expectedString

		where:
		modelClass       || expectedString
		NodeGraphicInput || "test_grid" + File.separator + "input" + File.separator + "graphics"
		LineGraphicInput || "test_grid" + File.separator + "input" + File.separator + "graphics"
	}

	def "A FileNamingStrategy with DefaultHierarchy and without pre- or suffix should return valid directory path for load profile time series"() {
		given:
		def strategy = new FileNamingStrategy(simpleEntityNaming, defaultHierarchy)
		def timeSeries = Mock(LoadProfileInput)

		when:
		def actual = strategy.getDirectoryPath(timeSeries)

		then:
		actual.present
		actual.get() == expected

		where:
		clazz            || expected
		LoadProfileInput || "test_grid" + File.separator + "input" + File.separator + "global"
	}

	def "A FileNamingStrategy with DefaultHierarchy and should return valid directory path for individual time series"() {
		given:
		def strategy = new FileNamingStrategy(simpleEntityNaming, defaultHierarchy)
		IndividualTimeSeries timeSeries = Mock(IndividualTimeSeries)

		when:
		def actual = strategy.getDirectoryPath(timeSeries)

		then:
		actual.present
		actual.get() == expected

		where:
		clazz                || expected
		IndividualTimeSeries || "test_grid" + File.separator + "input" + File.separator + "participants" + File.separator + "time_series"
	}

	def "A FileNamingStrategy with DefaultHierarchy and without pre- or suffixes should return valid file paths for all result models"() {
		given: "a file naming strategy without pre- or suffixes"
		def strategy = new FileNamingStrategy(simpleEntityNaming, defaultHierarchy)

		when:
		def res = strategy.getFilePath(modelClass)

		then:
		res.present
		res.get() == expectedString

		where:
		modelClass               || expectedString
		LoadResult               || "test_grid" + File.separator + "results" + File.separator + "participants" + File.separator + "load_res"
		FixedFeedInResult        || "test_grid" + File.separator + "results" + File.separator + "participants" + File.separator + "fixed_feed_in_res"
		BmResult                 || "test_grid" + File.separator + "results" + File.separator + "participants" + File.separator + "bm_res"
		PvResult                 || "test_grid" + File.separator + "results" + File.separator + "participants" + File.separator + "pv_res"
		ChpResult                || "test_grid" + File.separator + "results" + File.separator + "participants" + File.separator + "chp_res"
		WecResult                || "test_grid" + File.separator + "results" + File.separator + "participants" + File.separator + "wec_res"
		StorageResult            || "test_grid" + File.separator + "results" + File.separator + "participants" + File.separator + "storage_res"
		EvcsResult               || "test_grid" + File.separator + "results" + File.separator + "participants" + File.separator + "evcs_res"
		EvResult                 || "test_grid" + File.separator + "results" + File.separator + "participants" + File.separator + "ev_res"
		Transformer2WResult      || "test_grid" + File.separator + "results" + File.separator + "grid" + File.separator + "transformer_2_w_res"
		Transformer3WResult      || "test_grid" + File.separator + "results" + File.separator + "grid" + File.separator + "transformer_3_w_res"
		LineResult               || "test_grid" + File.separator + "results" + File.separator + "grid" + File.separator + "line_res"
		SwitchResult             || "test_grid" + File.separator + "results" + File.separator + "grid" + File.separator + "switch_res"
		NodeResult               || "test_grid" + File.separator + "results" + File.separator + "grid" + File.separator + "node_res"
		CylindricalStorageResult || "test_grid" + File.separator + "results" + File.separator + "thermal" + File.separator + "cylindrical_storage_res"
		ThermalHouseResult       || "test_grid" + File.separator + "results" + File.separator + "thermal" + File.separator + "thermal_house_res"
	}

	def "A FileNamingStrategy with DefaultHierarchy and without pre- or suffixes should return valid file paths for all other input assets models"() {
		given: "a file naming strategy without pre- or suffixes"
		def strategy = new FileNamingStrategy(simpleEntityNaming, defaultHierarchy)

		when:
		def res = strategy.getFilePath(modelClass)

		then:
		res.present
		res.get() == expectedString

		where:
		modelClass              || expectedString
		LineInput               || "test_grid" + File.separator + "input" + File.separator + "grid" + File.separator + "line_input"
		SwitchInput             || "test_grid" + File.separator + "input" + File.separator + "grid" + File.separator + "switch_input"
		NodeInput               || "test_grid" + File.separator + "input" + File.separator + "grid" + File.separator + "node_input"
		MeasurementUnitInput    || "test_grid" + File.separator + "input" + File.separator + "grid" + File.separator + "measurement_unit_input"
		Transformer2WInput      || "test_grid" + File.separator + "input" + File.separator + "grid" + File.separator + "transformer_2_w_input"
		Transformer3WInput      || "test_grid" + File.separator + "input" + File.separator + "grid" + File.separator + "transformer_3_w_input"
		CylindricalStorageInput || "test_grid" + File.separator + "input" + File.separator + "thermal" + File.separator + "cylindrical_storage_input"
		ThermalHouseInput       || "test_grid" + File.separator + "input" + File.separator + "thermal" + File.separator + "thermal_house_input"
	}

	def "A FileNamingStrategy with DefaultHierarchy and without pre- or suffixes should return valid file paths for all system input assets models"() {
		given: "a file naming strategy without pre- or suffixes"
		def strategy = new FileNamingStrategy(simpleEntityNaming, defaultHierarchy)

		when:
		def res = strategy.getFilePath(modelClass)

		then:
		res.present
		res.get() == expectedString

		where:
		modelClass              || expectedString
		FixedFeedInInput        || "test_grid" + File.separator + "input" + File.separator + "participants" + File.separator + "fixed_feed_in_input"
		PvInput                 || "test_grid" + File.separator + "input" + File.separator + "participants" + File.separator + "pv_input"
		WecInput                || "test_grid" + File.separator + "input" + File.separator + "participants" + File.separator + "wec_input"
		ChpInput                || "test_grid" + File.separator + "input" + File.separator + "participants" + File.separator + "chp_input"
		BmInput                 || "test_grid" + File.separator + "input" + File.separator + "participants" + File.separator + "bm_input"
		EvInput                 || "test_grid" + File.separator + "input" + File.separator + "participants" + File.separator + "ev_input"
		LoadInput               || "test_grid" + File.separator + "input" + File.separator + "participants" + File.separator + "load_input"
		StorageInput            || "test_grid" + File.separator + "input" + File.separator + "participants" + File.separator + "storage_input"
		HpInput                 || "test_grid" + File.separator + "input" + File.separator + "participants" + File.separator + "hp_input"
		EvcsInput               || "test_grid" + File.separator + "input" + File.separator + "participants" + File.separator + "evcs_input"
	}

	def "A FileNamingStrategy with DefaultHierarchy and without pre- or suffixes should return valid file paths for all input types models"() {
		given: "a file naming strategy without pre- or suffixes"
		def strategy = new FileNamingStrategy(simpleEntityNaming, defaultHierarchy)

		when:
		def res = strategy.getFilePath(modelClass)

		then:
		res.present
		res.get() == expectedString

		where:
		modelClass             || expectedString
		BmTypeInput            || "test_grid" + File.separator + "input" + File.separator + "global" + File.separator + "bm_type_input"
		ChpTypeInput           || "test_grid" + File.separator + "input" + File.separator + "global" + File.separator + "chp_type_input"
		EvTypeInput            || "test_grid" + File.separator + "input" + File.separator + "global" + File.separator + "ev_type_input"
		HpTypeInput            || "test_grid" + File.separator + "input" + File.separator + "global" + File.separator + "hp_type_input"
		LineTypeInput          || "test_grid" + File.separator + "input" + File.separator + "global" + File.separator + "line_type_input"
		StorageTypeInput       || "test_grid" + File.separator + "input" + File.separator + "global" + File.separator + "storage_type_input"
		Transformer2WTypeInput || "test_grid" + File.separator + "input" + File.separator + "global" + File.separator + "transformer_2_w_type_input"
		Transformer3WTypeInput || "test_grid" + File.separator + "input" + File.separator + "global" + File.separator + "transformer_3_w_type_input"
		WecTypeInput           || "test_grid" + File.separator + "input" + File.separator + "global" + File.separator + "wec_type_input"
	}

	def "A FileNamingStrategy with DefaultHierarchy and without pre- or suffixes should return valid directory path for a Load Parameter Model"() {
		given: "a file naming strategy without pre- or suffixes"
		def strategy = new FileNamingStrategy(simpleEntityNaming, defaultHierarchy)

		when:
		def res = strategy.getDirectoryPath(modelClass)

		then:
		res.present
		res.get() == expectedString

		where:
		modelClass           || expectedString
		RandomLoadParameters || "test_grid" + File.separator + "input" + File.separator + "global"
	}

	def "A FileNamingStrategy with DefaultHierarchy and without pre- or suffixes should return valid file path for a Load Parameter Model"() {
		given: "a file naming strategy without pre- or suffixes"
		def strategy = new FileNamingStrategy(simpleEntityNaming, defaultHierarchy)

		when:
		def res = strategy.getFilePath(modelClass)

		then:
		res.present
		res.get() == expectedString

		where:
		modelClass           || expectedString
		RandomLoadParameters || "test_grid" + File.separator + "input" + File.separator + "global" + File.separator + "random_load_parameters_input"
	}

	def "A FileNamingStrategy with DefaultHierarchy and without pre- or suffixes should return valid file paths for a graphic input Model"() {
		given: "a file naming strategy without pre- or suffixes"
		def strategy = new FileNamingStrategy(simpleEntityNaming, defaultHierarchy)

		when:
		def res = strategy.getFilePath(modelClass)

		then:
		res.present
		res.get() == expectedString

		where:
		modelClass       || expectedString
		NodeGraphicInput || "test_grid" + File.separator + "input" + File.separator + "graphics" + File.separator + "node_graphic_input"
		LineGraphicInput || "test_grid" + File.separator + "input" + File.separator + "graphics" + File.separator + "line_graphic_input"
	}

	def "A FileNamingStrategy with DefaultHierarchy and without pre- or suffix should return valid file path for individual time series"() {
		given:
		def strategy = new FileNamingStrategy(simpleEntityNaming, defaultHierarchy)
		def entries = [
			new TimeBasedValue(ZonedDateTime.now(), new EnergyPriceValue(Quantities.getQuantity(500d, PowerSystemUnits.EURO_PER_MEGAWATTHOUR)))
		] as SortedSet
		IndividualTimeSeries timeSeries = Mock(IndividualTimeSeries)
		timeSeries.uuid >> uuid
		timeSeries.entries >> entries

		when:
		def actual = strategy.getFilePath(timeSeries)

		then:
		actual.present
		actual.get() == expectedFilePath

		where:
		clazz                | uuid                                                    || expectedFilePath
		IndividualTimeSeries | UUID.fromString("4881fda2-bcee-4f4f-a5bb-6a09bf785276") || "test_grid" + File.separator + "input" + File.separator + "participants" + File.separator + "time_series" + File.separator + "its_c_4881fda2-bcee-4f4f-a5bb-6a09bf785276"
	}

	def "A FileNamingStrategy with DefaultHierarchy and with pre- or suffix should return valid file path for individual time series"() {
		given:
		def strategy = new FileNamingStrategy(new EntityPersistenceNamingStrategy("aa", "zz"), defaultHierarchy)
		def entries = [
			new TimeBasedValue(ZonedDateTime.now(), new EnergyPriceValue(Quantities.getQuantity(500d, PowerSystemUnits.EURO_PER_MEGAWATTHOUR)))
		] as SortedSet
		IndividualTimeSeries timeSeries = Mock(IndividualTimeSeries)
		timeSeries.uuid >> uuid
		timeSeries.entries >> entries

		when:
		def actual = strategy.getFilePath(timeSeries)

		then:
		actual.present
		actual.get() == expectedFileName

		where:
		clazz                | uuid                                                    || expectedFileName
		IndividualTimeSeries | UUID.fromString("4881fda2-bcee-4f4f-a5bb-6a09bf785276") || "test_grid" + File.separator + "input" + File.separator + "participants" + File.separator + "time_series" + File.separator + "aa_its_c_4881fda2-bcee-4f4f-a5bb-6a09bf785276_zz"
	}

	def "A FileNamingStrategy with DefaultHierarchy and without pre- or suffix should return valid file path for load profile time series"() {
		given:
		def strategy = new FileNamingStrategy(simpleEntityNaming, defaultHierarchy)
		def timeSeries = Mock(LoadProfileInput)
		timeSeries.uuid >> uuid
		timeSeries.type >> type

		when:
		def actual = strategy.getFilePath(timeSeries)

		then:
		actual.present
		actual.get() == expectedFileName

		where:
		clazz            | uuid                                                    | type               || expectedFileName
		LoadProfileInput | UUID.fromString("bee0a8b6-4788-4f18-bf72-be52035f7304") | BdewLoadProfile.G3 || "test_grid" + File.separator + "input" + File.separator + "global" + File.separator + "lpts_g3_bee0a8b6-4788-4f18-bf72-be52035f7304"
	}

	def "A FileNamingStrategy with DefaultHierarchy and without pre- or suffixes should return valid directory path for time series mapping"() {
		given: "a file naming strategy without pre- or suffixes"
		def strategy = new FileNamingStrategy(simpleEntityNaming, defaultHierarchy)

		when:
		def res = strategy.getDirectoryPath(TimeSeriesMappingSource.MappingEntry)

		then:
		res.present
		res.get() == "test_grid" + File.separator + "input" + File.separator + "participants" + File.separator + "time_series"
	}

	def "A FileNamingStrategy with DefaultHierarchy and without pre- or suffixes should return valid file path for time series mapping"() {
		given: "a file naming strategy without pre- or suffixes"
		def strategy = new FileNamingStrategy(simpleEntityNaming, defaultHierarchy)

		when:
		def res = strategy.getFilePath(TimeSeriesMappingSource.MappingEntry)

		then:
		res.present
		res.get() == "test_grid" + File.separator + "input" + File.separator + "participants" + File.separator + "time_series" + File.separator + "time_series_mapping"
	}

	def "A FileNamingStrategy with DefaultHierarchy and pre- and suffix should return valid file path for time series mapping"() {
		given: "a file naming strategy without pre- or suffixes"
		def strategy = new FileNamingStrategy(new EntityPersistenceNamingStrategy("prefix", "suffix"), defaultHierarchy)

		when:
		def res = strategy.getFilePath(TimeSeriesMappingSource.MappingEntry)

		then:
		res.present
		res.get() == "test_grid" + File.separator + "input" + File.separator + "participants" + File.separator + "time_series" + File.separator + "prefix_time_series_mapping_suffix"
	}


	// TESTS FOR FLAT HIERARCHY

	def "A FileNamingStrategy with FlatHierarchy does return empty sub directory path for any result class"() {
		given: "a file naming strategy without pre- or suffixes"
		def strategy = new FileNamingStrategy(simpleEntityNaming, flatHierarchy)

		when:
		def actual = strategy.getDirectoryPath(modelClass as Class<? extends UniqueEntity>)

		then:
		actual == expected

		where:
		modelClass               || expected
		LoadResult               || Optional.empty()
		FixedFeedInResult        || Optional.empty()
		BmResult                 || Optional.empty()
		PvResult                 || Optional.empty()
		ChpResult                || Optional.empty()
		WecResult                || Optional.empty()
		StorageResult            || Optional.empty()
		EvcsResult               || Optional.empty()
		EvResult                 || Optional.empty()
		Transformer2WResult      || Optional.empty()
		Transformer3WResult      || Optional.empty()
		LineResult               || Optional.empty()
		SwitchResult             || Optional.empty()
		NodeResult               || Optional.empty()
		CylindricalStorageResult || Optional.empty()
		ThermalHouseResult       || Optional.empty()
	}

	def "A FileNamingStrategy with FlatHierarchy does return empty sub directory path for all input asset models"() {
		given: "a naming strategy without pre- or suffixes"
		def strategy = new FileNamingStrategy(simpleEntityNaming, flatHierarchy)

		when:
		def actual = strategy.getDirectoryPath(modelClass as Class<? extends UniqueEntity>)

		then:
		actual == expected

		where:
		modelClass               || expected
		FixedFeedInInput         || Optional.empty()
		PvInput                  || Optional.empty()
		WecInput                 || Optional.empty()
		ChpInput                 || Optional.empty()
		BmInput                  || Optional.empty()
		EvInput                  || Optional.empty()
		EvcsInput                || Optional.empty()
		LoadInput                || Optional.empty()
		StorageInput             || Optional.empty()
		HpInput                  || Optional.empty()
		LineInput                || Optional.empty()
		SwitchInput              || Optional.empty()
		NodeInput                || Optional.empty()
		MeasurementUnitInput     || Optional.empty()
		Transformer2WInput       || Optional.empty()
		Transformer3WInput       || Optional.empty()
		CylindricalStorageInput  || Optional.empty()
		ThermalHouseInput        || Optional.empty()
	}

	def "A FileNamingStrategy with FlatHierarchy does return empty sub directory path for system type and model input classes"() {
		given: "a file naming strategy without pre- or suffixes"
		def strategy = new FileNamingStrategy(simpleEntityNaming, flatHierarchy)

		when:
		def actual = strategy.getDirectoryPath(modelClass as Class<? extends UniqueEntity>)

		then:
		actual == expected

		where:
		modelClass               || expected
		BmTypeInput              || Optional.empty()
		ChpTypeInput             || Optional.empty()
		EvTypeInput              || Optional.empty()
		HpTypeInput              || Optional.empty()
		StorageTypeInput         || Optional.empty()
		WecTypeInput             || Optional.empty()
		LineTypeInput            || Optional.empty()
		Transformer2WTypeInput   || Optional.empty()
		Transformer3WTypeInput   || Optional.empty()
	}

	def "A FileNamingStrategy with FlatHierarchy does return empty sub directory path for graphics model input classes"() {
		given: "a naming strategy without pre- or suffixes"
		def strategy = new FileNamingStrategy(simpleEntityNaming, flatHierarchy)

		when:
		def actual = strategy.getDirectoryPath(modelClass as Class<? extends UniqueEntity>)

		then:
		actual == expected

		where:
		modelClass               || expected
		NodeGraphicInput         || Optional.empty()
		LineGraphicInput         || Optional.empty()
	}

	def "A FileNamingStrategy with FlatHierarchy does return empty sub directory path for any other model classes"() {
		given: "a naming strategy without pre- or suffixes"
		def strategy = new FileNamingStrategy(simpleEntityNaming, flatHierarchy)

		when:
		def actual = strategy.getDirectoryPath(modelClass as Class<? extends UniqueEntity>)

		then:
		actual == expected

		where:
		modelClass               || expected
		RandomLoadParameters     || Optional.empty()
		TimeSeriesMappingSource.MappingEntry  || Optional.empty()
	}

	def "A FileNamingStrategy with FlatHierarchy does return empty sub directory path for load profile time series"() {
		given: "a naming strategy without pre- or suffixes"
		def strategy = new FileNamingStrategy(simpleEntityNaming, flatHierarchy)
		def timeSeries = Mock(LoadProfileInput)

		when:
		def actual = strategy.getDirectoryPath(timeSeries)

		then:
		actual == Optional.empty()
	}

	def "A FileNamingStrategy with FlatHierarchy does return empty sub directory path for individual time series"() {
		given: "a naming strategy without pre- or suffixes"
		def strategy = new FileNamingStrategy(simpleEntityNaming, flatHierarchy)
		def timeSeries = Mock(IndividualTimeSeries)

		when:
		def actual = strategy.getDirectoryPath(timeSeries)

		then:
		actual == Optional.empty()
	}

	def "A FileNamingStrategy with FlatHierarchy and without pre- or suffixes should return valid file paths for all result classes"() {
		given: "a naming strategy without pre- or suffixes"
		def strategy = new FileNamingStrategy(simpleEntityNaming, flatHierarchy)

		when:
		def res = strategy.getFilePath(modelClass as Class<? extends UniqueEntity>)

		then:
		res.present
		res.get() == expectedString

		where:
		modelClass               || expectedString
		LoadResult               || "load_res"
		FixedFeedInResult        || "fixed_feed_in_res"
		BmResult                 || "bm_res"
		PvResult                 || "pv_res"
		ChpResult                || "chp_res"
		WecResult                || "wec_res"
		StorageResult            || "storage_res"
		EvcsResult               || "evcs_res"
		EvResult                 || "ev_res"
		Transformer2WResult      || "transformer_2_w_res"
		Transformer3WResult      || "transformer_3_w_res"
		LineResult               || "line_res"
		SwitchResult             || "switch_res"
		NodeResult               || "node_res"
		CylindricalStorageResult || "cylindrical_storage_res"
		ThermalHouseResult       || "thermal_house_res"
	}

	def "A FileNamingStrategy with FlatHierarchy and without pre- or suffixes should return valid file paths for all other system input classes"() {
		given: "a naming strategy without pre- or suffixes"
		def strategy = new FileNamingStrategy(simpleEntityNaming, flatHierarchy)

		when:
		def res = strategy.getFilePath(modelClass as Class<? extends UniqueEntity>)

		then:
		res.present
		res.get() == expectedString

		where:
		modelClass               || expectedString
		FixedFeedInInput         || "fixed_feed_in_input"
		PvInput                  || "pv_input"
		WecInput                 || "wec_input"
		ChpInput                 || "chp_input"
		BmInput                  || "bm_input"
		EvInput                  || "ev_input"
		EvcsInput                || "evcs_input"
		LoadInput                || "load_input"
		StorageInput             || "storage_input"
		HpInput                  || "hp_input"
		LineInput                || "line_input"
		SwitchInput              || "switch_input"
		NodeInput                || "node_input"
		MeasurementUnitInput     || "measurement_unit_input"
		Transformer2WInput       || "transformer_2_w_input"
		Transformer3WInput       || "transformer_3_w_input"
		CylindricalStorageInput  || "cylindrical_storage_input"
		ThermalHouseInput        || "thermal_house_input"
	}

	def "A FileNamingStrategy with FlatHierarchy and without pre- or suffixes should return valid file paths for all system characteristic and type input classes"() {
		given: "a naming strategy without pre- or suffixes"
		def strategy = new FileNamingStrategy(simpleEntityNaming, flatHierarchy)

		when:
		def res = strategy.getFilePath(modelClass as Class<? extends UniqueEntity>)

		then:
		res.present
		res.get() == expectedString

		where:
		modelClass               || expectedString
		BmTypeInput              || "bm_type_input"
		ChpTypeInput             || "chp_type_input"
		EvTypeInput              || "ev_type_input"
		HpTypeInput              || "hp_type_input"
		StorageTypeInput         || "storage_type_input"
		WecTypeInput             || "wec_type_input"
		LineTypeInput            || "line_type_input"
		Transformer2WTypeInput   || "transformer_2_w_type_input"
		Transformer3WTypeInput   || "transformer_3_w_type_input"
	}

	def "A FileNamingStrategy with FlatHierarchy and without pre- or suffixes should return valid file paths for all graphics input classes"() {
		given: "a naming strategy without pre- or suffixes"
		def strategy = new FileNamingStrategy(simpleEntityNaming, flatHierarchy)

		when:
		def res = strategy.getFilePath(modelClass as Class<? extends UniqueEntity>)

		then:
		res.present
		res.get() == expectedString

		where:
		modelClass               || expectedString
		NodeGraphicInput         || "node_graphic_input"
		LineGraphicInput         || "line_graphic_input"
	}

	def "A FileNamingStrategy with FlatHierarchy does return valid file path for load profile time series"() {
		given: "a naming strategy without pre- or suffixes"
		def strategy = new FileNamingStrategy(simpleEntityNaming, flatHierarchy)
		def timeSeries = Mock(LoadProfileInput)
		timeSeries.uuid >> uuid
		timeSeries.type >> type

		when:
		def actual = strategy.getFilePath(timeSeries)

		then:
		actual.present
		actual.get() == expectedFilePath

		where:
		clazz            | uuid                                                    | type               || expectedFilePath
		LoadProfileInput | UUID.fromString("bee0a8b6-4788-4f18-bf72-be52035f7304") | BdewLoadProfile.G3 || "lpts_g3_bee0a8b6-4788-4f18-bf72-be52035f7304"
	}

	def "A FileNamingStrategy with FlatHierarchy does return valid file path for individual time series"() {
		given: "a naming strategy without pre- or suffixes"
		def strategy = new FileNamingStrategy(simpleEntityNaming, flatHierarchy)
		def entries = [
			new TimeBasedValue(ZonedDateTime.now(), new EnergyPriceValue(Quantities.getQuantity(500d, PowerSystemUnits.EURO_PER_MEGAWATTHOUR)))
		] as SortedSet
		def timeSeries = Mock(IndividualTimeSeries)
		timeSeries.uuid >> uuid
		timeSeries.entries >> entries

		when:
		def actual = strategy.getFilePath(timeSeries)

		then:
		actual.present
		actual.get() == expectedFilePath

		where:
		clazz                | uuid                                                    || expectedFilePath
		IndividualTimeSeries | UUID.fromString("4881fda2-bcee-4f4f-a5bb-6a09bf785276") || "its_c_4881fda2-bcee-4f4f-a5bb-6a09bf785276"
	}

	String escapedFileSeparator = File.separator == "\\" ? "\\\\" : File.separator

	def "A FileNamingStrategy with DefaultHierarchy returns correct individual time series file name pattern"() {
		given:
		def strategy = new FileNamingStrategy(simpleEntityNaming, defaultHierarchy)

		when:
		def actual = strategy.individualTimeSeriesPattern.pattern()

		then:
		actual == "test_grid" + escapedFileSeparator + "input" + escapedFileSeparator + "participants" + escapedFileSeparator + "time_series" + escapedFileSeparator + "its_(?<columnScheme>[a-zA-Z]{1,11})_(?<uuid>[a-zA-Z0-9]{8}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{12})"
	}

	def "A FileNamingStrategy with DefaultHierarchy returns correct load profile time series file name pattern"() {
		given:
		def strategy = new FileNamingStrategy(simpleEntityNaming, defaultHierarchy)

		when:
		def actual = strategy.loadProfileTimeSeriesPattern.pattern()

		then:
		actual == "test_grid" + escapedFileSeparator + "input" + escapedFileSeparator + "global" + escapedFileSeparator + "lpts_(?<profile>[a-zA-Z][0-9])_(?<uuid>[a-zA-Z0-9]{8}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{12})"
	}

	def "A FileNamingStrategy with FlatHierarchy returns correct individual time series file name pattern"() {
		given:
		def strategy = new FileNamingStrategy(simpleEntityNaming, flatHierarchy)

		when:
		def actual = strategy.individualTimeSeriesPattern.pattern()

		then:
		actual == "its_(?<columnScheme>[a-zA-Z]{1,11})_(?<uuid>[a-zA-Z0-9]{8}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{12})"
	}

	def "A FileNamingStrategy with FlatHierarchy returns correct load profile time series file name pattern"() {
		given:
		def strategy = new FileNamingStrategy(simpleEntityNaming, flatHierarchy)

		when:
		def actual = strategy.loadProfileTimeSeriesPattern.pattern()

		then:
		actual == "lpts_(?<profile>[a-zA-Z][0-9])_(?<uuid>[a-zA-Z0-9]{8}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{12})"
	}

	def "Trying to extract time series meta information throws an Exception, if it is provided a malformed string"() {
		given:
		def fns = new FileNamingStrategy(simpleEntityNaming, flatHierarchy)
		def path = Paths.get("/bla/foo")

		when:
		fns.extractTimeSeriesMetaInformation(path)

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == "Unknown format of 'foo'. Cannot extract meta information."
	}

	def "Trying to extract individual time series meta information throws an Exception, if it is provided a malformed string"() {
		given:
		def fns = new FileNamingStrategy(simpleEntityNaming, flatHierarchy)
		def fileName = "foo"

		when:
		fns.extractIndividualTimesSeriesMetaInformation(fileName)

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == "Cannot extract meta information on individual time series from 'foo'."
	}

	def "Trying to extract load profile time series meta information throws an Exception, if it is provided a malformed string"() {
		given:
		def fns = new FileNamingStrategy(simpleEntityNaming, flatHierarchy)
		def fileName = "foo"

		when:
		fns.extractLoadProfileTimesSeriesMetaInformation(fileName)

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == "Cannot extract meta information on load profile time series from 'foo'."
	}

	def "The EntityPersistenceNamingStrategy extracts correct meta information from a valid individual time series file name"() {
		given:
		def fns = new FileNamingStrategy(simpleEntityNaming, flatHierarchy)
		def path = Paths.get(pathString)

		when:
		def metaInformation = fns.extractTimeSeriesMetaInformation(path)

		then:
		IndividualTimeSeriesMetaInformation.isAssignableFrom(metaInformation.getClass())
		(metaInformation as IndividualTimeSeriesMetaInformation).with {
			assert it.uuid == UUID.fromString("4881fda2-bcee-4f4f-a5bb-6a09bf785276")
			assert it.columnScheme == expectedColumnScheme
		}

		where:
		pathString || expectedColumnScheme
		"/bla/foo/its_c_4881fda2-bcee-4f4f-a5bb-6a09bf785276.csv" || ColumnScheme.ENERGY_PRICE
		"/bla/foo/its_p_4881fda2-bcee-4f4f-a5bb-6a09bf785276.csv" || ColumnScheme.ACTIVE_POWER
		"/bla/foo/its_pq_4881fda2-bcee-4f4f-a5bb-6a09bf785276.csv" || ColumnScheme.APPARENT_POWER
		"/bla/foo/its_h_4881fda2-bcee-4f4f-a5bb-6a09bf785276.csv" || ColumnScheme.HEAT_DEMAND
		"/bla/foo/its_ph_4881fda2-bcee-4f4f-a5bb-6a09bf785276.csv" || ColumnScheme.ACTIVE_POWER_AND_HEAT_DEMAND
		"/bla/foo/its_pqh_4881fda2-bcee-4f4f-a5bb-6a09bf785276.csv" || ColumnScheme.APPARENT_POWER_AND_HEAT_DEMAND
		"/bla/foo/its_weather_4881fda2-bcee-4f4f-a5bb-6a09bf785276.csv" || ColumnScheme.WEATHER
	}

	def "The EntityPersistenceNamingStrategy extracts correct meta information from a valid individual time series file name with pre- and suffix"() {
		given:
		def fns = new FileNamingStrategy(new EntityPersistenceNamingStrategy("prefix", "suffix"), flatHierarchy)
		def path = Paths.get(pathString)

		when:
		def metaInformation = fns.extractTimeSeriesMetaInformation(path)

		then:
		IndividualTimeSeriesMetaInformation.isAssignableFrom(metaInformation.getClass())
		(metaInformation as IndividualTimeSeriesMetaInformation).with {
			assert it.uuid == UUID.fromString("4881fda2-bcee-4f4f-a5bb-6a09bf785276")
			assert it.columnScheme == expectedColumnScheme
		}

		where:
		pathString || expectedColumnScheme
		"/bla/foo/prefix_its_c_4881fda2-bcee-4f4f-a5bb-6a09bf785276_suffix.csv" || ColumnScheme.ENERGY_PRICE
		"/bla/foo/prefix_its_p_4881fda2-bcee-4f4f-a5bb-6a09bf785276_suffix.csv" || ColumnScheme.ACTIVE_POWER
		"/bla/foo/prefix_its_pq_4881fda2-bcee-4f4f-a5bb-6a09bf785276_suffix.csv" || ColumnScheme.APPARENT_POWER
		"/bla/foo/prefix_its_h_4881fda2-bcee-4f4f-a5bb-6a09bf785276_suffix.csv" || ColumnScheme.HEAT_DEMAND
		"/bla/foo/prefix_its_ph_4881fda2-bcee-4f4f-a5bb-6a09bf785276_suffix.csv" || ColumnScheme.ACTIVE_POWER_AND_HEAT_DEMAND
		"/bla/foo/prefix_its_pqh_4881fda2-bcee-4f4f-a5bb-6a09bf785276_suffix.csv" || ColumnScheme.APPARENT_POWER_AND_HEAT_DEMAND
		"/bla/foo/prefix_its_weather_4881fda2-bcee-4f4f-a5bb-6a09bf785276_suffix.csv" || ColumnScheme.WEATHER
	}

	def "The EntityPersistenceNamingStrategy throw an IllegalArgumentException, if the column scheme is malformed."() {
		given:
		def fns = new FileNamingStrategy(simpleEntityNaming, flatHierarchy)
		def path = Paths.get("/bla/foo/its_whoops_4881fda2-bcee-4f4f-a5bb-6a09bf785276.csv")

		when:
		fns.extractTimeSeriesMetaInformation(path)

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == "Cannot parse 'whoops' to valid column scheme."
	}

	def "The EntityPersistenceNamingStrategy extracts correct meta information from a valid load profile time series file name"() {
		given:
		def fns = new FileNamingStrategy(simpleEntityNaming, flatHierarchy)
		def path = Paths.get("/bla/foo/lpts_g3_bee0a8b6-4788-4f18-bf72-be52035f7304.csv")

		when:
		def metaInformation = fns.extractTimeSeriesMetaInformation(path)

		then:
		LoadProfileTimeSeriesMetaInformation.isAssignableFrom(metaInformation.getClass())
		(metaInformation as LoadProfileTimeSeriesMetaInformation).with {
			assert uuid == UUID.fromString("bee0a8b6-4788-4f18-bf72-be52035f7304")
			assert profile == "g3"
		}
	}

	def "The EntityPersistenceNamingStrategy extracts correct meta information from a valid load profile time series file name with pre- and suffix"() {
		given:
		def fns = new FileNamingStrategy(new EntityPersistenceNamingStrategy("prefix", "suffix"), flatHierarchy)
		def path = Paths.get("/bla/foo/prefix_lpts_g3_bee0a8b6-4788-4f18-bf72-be52035f7304_suffix.csv")

		when:
		def metaInformation = fns.extractTimeSeriesMetaInformation(path)

		then:
		LoadProfileTimeSeriesMetaInformation.isAssignableFrom(metaInformation.getClass())
		(metaInformation as LoadProfileTimeSeriesMetaInformation).with {
			assert uuid == UUID.fromString("bee0a8b6-4788-4f18-bf72-be52035f7304")
			assert profile == "g3"
		}
	}


	def "The FileNamingStrategy with FlatHierarchy returns the Id Coordinate file path correctly"() {
		def fns = new FileNamingStrategy(new EntityPersistenceNamingStrategy("prefix", "suffix"), flatHierarchy)

		when:
		def idFilePath = fns.getIdCoordinateFilePath()

		then:
		idFilePath.present
		idFilePath.get() == "prefix_coordinates_suffix"
	}

	def "The FileNamingStrategy with DefaultHierarchy returns the Id Coordinate file path correctly"() {
		def fns = new FileNamingStrategy(new EntityPersistenceNamingStrategy("prefix", "suffix"), defaultHierarchy)

		when:
		def idFilePath = fns.getIdCoordinateFilePath()

		then:
		idFilePath.present
		idFilePath.get() ==  defaultHierarchy.baseDirectory.get() + File.separator + "prefix_coordinates_suffix"
	}

}