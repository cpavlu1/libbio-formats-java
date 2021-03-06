/*
 * ome.xml.r2003fc.ome.PumpNode
 *
 *-----------------------------------------------------------------------------
 *
 *  Copyright (C) 2007 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee,
 *      University of Wisconsin-Madison
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *-----------------------------------------------------------------------------
 */

/*-----------------------------------------------------------------------------
 *
 * THIS IS AUTOMATICALLY GENERATED CODE.  DO NOT MODIFY.
 * Created by curtis via xsd-fu on 2008-10-16 06:18:35-0500
 *
 *-----------------------------------------------------------------------------
 */

package ome.xml.r2003fc.ome;

import ome.xml.DOMUtil;
import ome.xml.OMEXMLNode;
import ome.xml.r2003fc.ome.*;

import org.w3c.dom.Element;

public class PumpNode extends ReferenceNode
{

	// -- Constructors --

	/** Constructs a Pump node with an associated DOM element. */
	public PumpNode(Element element)
	{
		super(element);
	}

	/**
	 * Constructs a Pump node with an associated DOM element beneath
	 * a given parent.
	 */
	public PumpNode(OMEXMLNode parent)
	{
		this(parent, true);
	}

	/**
	 * Constructs a Pump node with an associated DOM element beneath
	 * a given parent.
	 */
	public PumpNode(OMEXMLNode parent, boolean attach)
	{
		super(DOMUtil.createChild(parent.getDOMElement(),
		                          "Pump", attach));
	}

	/** 
	 * Returns the <code>LightSourceNode</code> which this reference
	 * links to.
	 */
	public LightSourceNode getLightSource()
	{
		return (LightSourceNode)
			getAttrReferencedNode("LightSource", "ID");
	}

	/**
	 * Sets the active reference node on this node.
	 * @param node The <code>LightSourceNode</code> to set as a
	 * reference.
	 */
	public void setLightSourceNode(LightSourceNode node)
	{
		setNodeID(node.getNodeID());
	}

	// -- Pump API methods --

	// *** WARNING *** Unhandled or skipped property ID

	// -- OMEXMLNode API methods --

	public boolean hasID()
	{
		return true;
	}

}
