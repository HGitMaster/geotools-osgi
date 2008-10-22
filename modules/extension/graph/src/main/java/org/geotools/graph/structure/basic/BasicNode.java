/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.graph.structure.basic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;

/**
 * Basic implementation of Node. 
 *
 * @author Justin Deoliveira, Refractions Research Inc, jdeolive@refractions.net
 *
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/extension/graph/src/main/java/org/geotools/graph/structure/basic/BasicNode.java $
 */
public class BasicNode extends BasicGraphable implements Node {
  
  /** List of edges incident with the node. */
  transient private ArrayList m_edges;
  
  /**
   * Constructs a BasicNode.
   *
   */
  public BasicNode() {
    super();
    m_edges = new ArrayList();
  }

  /**
   * Adds an edge to the adjacency list of the node which is an underlying List 
   * implementation. No checking is done on the edge (duplication, looping...), 
   * it is simply added to the list.
   *  
   * @see Node#add(Edge)
   */
  public void add(Edge e) {
    m_edges.add(e);  
  }
  
  /**
   * @see Node#remove(Edge)
   */
  public void remove(Edge e) {
    m_edges.remove(e);  
  }
  
  /**
   * @see Node#getDegree()
   */
  public int getDegree() {
    //since edges that loop on a node add 2 to the degree
    // of the node, the degree is not simply the size of the edge
    // list
    int degree = 0;
    
    for (int i = 0; i < m_edges.size(); i++) {
      Edge e = (Edge)m_edges.get(i);
      if (e.getNodeA().equals(this)) degree++;
      if (e.getNodeB().equals(this)) degree++;  
    }
    
    return(degree);
  }

  /**
   * @see Node#getEdge(Node)
   */
  public Edge getEdge(Node other) {
    //must explictley check that the edge has node other, and one node this, 
    // just checking other is not good enough because of loops
    for (int i = 0; i < m_edges.size(); i++) {
      Edge e = (Edge)m_edges.get(i);
       if (
        (e.getNodeA().equals(this) && e.getNodeB().equals(other)) ||
        (e.getNodeA().equals(other) && e.getNodeB().equals(this))
      ) return(e);
    }
    return(null);
  }
  
  /**
   * @see Node#getEdges(Node)
   */
  public List getEdges(Node other) {
    //must explictley check that the edge has node other, and one node this, 
    // just checking other is not good enough because of loops
    ArrayList edges = new ArrayList();  
     for (int i = 0; i < m_edges.size(); i++) {
      Edge e = (Edge)m_edges.get(i);
      if (
        (e.getNodeA().equals(this) && e.getNodeB().equals(other)) ||
        (e.getNodeA().equals(other) && e.getNodeB().equals(this))
      ) edges.add(e); 
    }
    return(edges);
  }
  
  /**
   * @see Node#getEdges()
   */
  public List getEdges() {
    return(m_edges);  
  }
  
  /** 
   * Returns all nodes that are incident with adjacent edges minus itself. This
   * iterator is generated by calculating an underlying collection upon each 
   * method call. 
   * 
   * @see org.geotools.graph.structure.Graphable#getRelated()
   */
  public Iterator getRelated() {
    ArrayList related = new ArrayList(m_edges.size());
    for (int i = 0; i < m_edges.size(); i++) {
      Edge e = (Edge)m_edges.get(i);
      related.add(e.getOtherNode(this));
    }
    return(related.iterator());
  }
  
  /**
   * Overides the default deserialization operation. The edge adjacency list
   * of a BasicNode is not written out when the node is serialized so it must
   * be recreated upon deserialization.
   * 
   * @param in Object input stream containing serialized object.
   *
   * @throws IOException
   * @throws ClassNotFoundException
   */
  private void readObject(ObjectInputStream in)
   throws IOException, ClassNotFoundException {
     
    in.defaultReadObject();
    
    //recreate edge adjacency list
    m_edges = new ArrayList();
  }
  
}
