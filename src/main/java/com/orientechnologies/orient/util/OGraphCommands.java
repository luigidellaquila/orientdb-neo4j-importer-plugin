/*
 * Copyright 2017 OrientDB LTD (info--at--orientdb.com)
 * All Rights Reserved. Commercial License.
 *
 * NOTICE:  All information contained herein is, and remains the property of
 * OrientDB LTD and its suppliers, if any.  The intellectual and
 * technical concepts contained herein are proprietary to
 * OrientDB LTD and its suppliers and may be covered by United
 * Kingdom and Foreign Patents, patents in process, and are protected by trade
 * secret or copyright law.
 *
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from OrientDB LTD.
 *
 * For more information: http://www.orientdb.com
 */

package com.orientechnologies.orient.util;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.record.OVertex;
import com.orientechnologies.orient.core.sql.executor.OResultSet;

/**
 * Collects several commands executable on a OrientDb database.
 */

public class OGraphCommands {

  private static String quote =  "\"";

  /**
   * The method performs a lookup on the passed ODatabaseDocument for a OVertex, starting from a record and from a vertex type.
   * It returns the vertex if present, null if not present.
   *
   * @param orientGraph
   * @param keys
   * @param values
   * @param vertexClassName
   *
   * @return
   */
  public static OVertex getVertexByIndexedKey(ODatabaseDocument orientGraph, String[] keys, String[] values, String vertexClassName) {

    OVertex vertex = null;

    final OResultSet vertices = getVertices(orientGraph, vertexClassName, keys, values);

    if (vertices.hasNext()) {
      vertex = vertices.next().getVertex().orElse(null);
    }

    vertices.close();

    return vertex;
  }

  /**
   * Performs the lookup in class V by default.
   * @param orientGraph
   * @param keys
   * @param values
   * @return
   */
  public static OResultSet getVertices(ODatabaseDocument orientGraph, String[] keys, Object[] values) {
    return getVertices(orientGraph, "V", keys, values);
  }

  /**
   * Performs the lookup in the specified vertex class.
   * @param orientGraph
   * @param keys
   * @param values
   * @return
   */
  public static OResultSet getVertices(ODatabaseDocument orientGraph, String vertexClassName, String[] keys, Object[] values) {

    Object[] params = new Object[values.length];

    for (int i = 0; i < params.length; i++) {
      params[i] = values[i];
    }

    String query = "select * from " + vertexClassName + " where ";
    query += keys[0] + " = ?";

    int i;
    for(i=1; i<keys.length; i++) {
      query += " and " + keys[i] + " = ?";
    }
    return orientGraph.command(query, params);
  }
}
