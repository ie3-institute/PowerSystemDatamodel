/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.datamodel.io.extractor;

import edu.ie3.datamodel.exceptions.ExtractorException;
import edu.ie3.datamodel.models.input.InputEntity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A simple utility class that can be used by sinks to extract nested elements (e.g. nodes, types)
 * that should be persisted.
 *
 * @version 0.1
 * @since 31.03.20
 */
public class Extractor {

  private final List<InputEntity> extractedEntities;

  public Extractor(Nested nestedEntity) throws ExtractorException {
    this.extractedEntities = extractElements(nestedEntity);
  }

  private List<InputEntity> extractElements(Nested nestedEntity) throws ExtractorException {
    List<InputEntity> resultingList = new ArrayList<>();
    if (nestedEntity instanceof Node) {
      resultingList.add(((Node) nestedEntity).getNode());
    }
    if (nestedEntity instanceof NodeC) {
      resultingList.add(((NodeC) nestedEntity).getNodeC());
    }
    if (nestedEntity instanceof Nodes) {
      resultingList.addAll(
          Arrays.asList(((Nodes) nestedEntity).getNodeA(), ((Nodes) nestedEntity).getNodeB()));
    }
    if (nestedEntity instanceof Type) {
      resultingList.add(((Type) nestedEntity).getType());
    }
    if (resultingList.isEmpty()) {
      throw new ExtractorException(
          "The interface 'Nested' is not meant to be extended. The provided entity of class '"
              + nestedEntity.getClass().getSimpleName()
              + "' and cannot be processed by "
              + "the extractor! Currently only the interfaces 'Node', 'NodeC', ‘Nodes‘ and ‘Type' are supported!");
    }

    return Collections.unmodifiableList(resultingList);
  }

  public List<InputEntity> getExtractedEntities() {
    return extractedEntities;
  }
}