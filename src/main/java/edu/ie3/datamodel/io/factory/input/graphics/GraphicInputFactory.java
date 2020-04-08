/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.datamodel.io.factory.input.graphics;

import edu.ie3.datamodel.io.factory.EntityData;
import edu.ie3.datamodel.io.factory.EntityFactory;
import edu.ie3.datamodel.models.input.NodeInput;
import edu.ie3.datamodel.models.input.graphics.GraphicInput;
import java.util.*;
import org.apache.commons.lang3.ArrayUtils;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

/**
 * Abstract factory implementation for all {@link GraphicInput} elements
 *
 * @version 0.1
 * @since 08.04.20
 */
public abstract class GraphicInputFactory<T extends GraphicInput, D extends EntityData>
    extends EntityFactory<T, D> {

  private static final String UUID = "uuid";
  private static final String GRAPHIC_LAYER = "graphiclayer";
  private static final String PATH_LINE_STRING = "path";

  public GraphicInputFactory(Class<? extends T>... allowedClasses) {
    super(allowedClasses);
  }

  /**
   * Returns list of sets of attribute names that the entity requires to be built.
   *
   * <p>The mandatory attributes required to create an {@link GraphicInput} are enhanced with custom
   * attribute names that each subclass factory determines in {@link #getAdditionalFields()}.
   *
   * @param data EntityData (or subclass) containing the data
   * @return list of possible attribute sets
   */
  @Override
  protected List<Set<String>> getFields(D data) {
    Set<String> constructorParamsMin = newSet(UUID, GRAPHIC_LAYER, PATH_LINE_STRING);
    final String[] additionalFields = getAdditionalFields();
    constructorParamsMin = expandSet(constructorParamsMin, additionalFields);
    return Collections.singletonList(constructorParamsMin);
  }

  /**
   * Returns fields other than the required fields of {@link GraphicInput} that have to be present.
   *
   * @return Array of field names, can be empty but not null
   */
  protected abstract String[] getAdditionalFields();

  @Override
  protected T buildModel(D data) {
    UUID uuid = data.getUUID(UUID);

    final String graphicLayer = data.getField(GRAPHIC_LAYER);
    final LineString pathLineString =
        data.getLineString(PATH_LINE_STRING)
            .orElse(
                new GeometryFactory()
                    .createLineString(
                        ArrayUtils.addAll(
                            NodeInput.DEFAULT_GEO_POSITION.getCoordinates(),
                            NodeInput.DEFAULT_GEO_POSITION.getCoordinates())));

    return buildModel(data, uuid, graphicLayer, pathLineString);
  }

  /**
   * Creates a graphic input entity with given parameters
   *
   * @param data entity data
   * @param uuid UUID of the input entity
   * @return newly created asset object
   */
  protected abstract T buildModel(
      D data, UUID uuid, String graphicLayer, LineString pathLineString);
}
