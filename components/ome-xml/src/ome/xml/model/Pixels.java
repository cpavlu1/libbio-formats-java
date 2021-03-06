/*
 * ome.xml.model.Pixels
 *
 *-----------------------------------------------------------------------------
 *
 *  Copyright (C) @year@ Open Microscopy Environment
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
 * Created by melissa via xsd-fu on 2011-11-09 10:55:09-0500
 *
 *-----------------------------------------------------------------------------
 */

package ome.xml.model;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ome.xml.model.enums.*;
import ome.xml.model.primitives.*;

public class Pixels extends AbstractOMEModelObject
{
	// Base:  -- Name: Pixels -- Type: Pixels -- javaBase: AbstractOMEModelObject -- javaType: Object

	// -- Constants --

	public static final String NAMESPACE = "http://www.openmicroscopy.org/Schemas/OME/2011-06";

	/** Logger for this class. */
	private static final Logger LOGGER =
		LoggerFactory.getLogger(Pixels.class);

	// -- Instance variables --


	// Property
	private PositiveInteger sizeT;

	// Property
	private DimensionOrder dimensionOrder;

	// Property
	private Double timeIncrement;

	// Property
	private PositiveFloat physicalSizeY;

	// Property
	private PositiveFloat physicalSizeX;

	// Property
	private PositiveFloat physicalSizeZ;

	// Property
	private PositiveInteger sizeX;

	// Property
	private PositiveInteger sizeY;

	// Property
	private PositiveInteger sizeZ;

	// Property
	private PositiveInteger sizeC;

	// Property
	private PixelType type;

	// Property
	private String id;

	// Property which occurs more than once
	private List<Channel> channelList = new ArrayList<Channel>();

	// Property which occurs more than once
	private List<BinData> binDataList = new ArrayList<BinData>();

	// Property which occurs more than once
	private List<TiffData> tiffDataList = new ArrayList<TiffData>();

	// Property
	private MetadataOnly metadataOnly;

	// Property which occurs more than once
	private List<Plane> planeList = new ArrayList<Plane>();

	// Reference AnnotationRef
	private List<Annotation> annotationList = new ArrayList<Annotation>();

	// -- Constructors --

	/** Default constructor. */
	public Pixels()
	{
		super();
	}

	/** 
	 * Constructs Pixels recursively from an XML DOM tree.
	 * @param element Root of the XML DOM tree to construct a model object
	 * graph from.
	 * @param model Handler for the OME model which keeps track of instances
	 * and references seen during object population.
	 * @throws EnumerationException If there is an error instantiating an
	 * enumeration during model object creation.
	 */
	public Pixels(Element element, OMEModel model)
	    throws EnumerationException
	{
		update(element, model);
	}

	// -- Custom content from Pixels specific template --


	// -- OMEModelObject API methods --

	/** 
	 * Updates Pixels recursively from an XML DOM tree. <b>NOTE:</b> No
	 * properties are removed, only added or updated.
	 * @param element Root of the XML DOM tree to construct a model object
	 * graph from.
	 * @param model Handler for the OME model which keeps track of instances
	 * and references seen during object population.
	 * @throws EnumerationException If there is an error instantiating an
	 * enumeration during model object creation.
	 */
	public void update(Element element, OMEModel model)
	    throws EnumerationException
	{
		super.update(element, model);
		String tagName = element.getTagName();
		if (!"Pixels".equals(tagName))
		{
			LOGGER.debug("Expecting node name of Pixels got {}", tagName);
		}
		if (element.hasAttribute("SizeT"))
		{
			// Attribute property SizeT
			setSizeT(PositiveInteger.valueOf(
					element.getAttribute("SizeT")));
		}
		if (element.hasAttribute("DimensionOrder"))
		{
			// Attribute property which is an enumeration DimensionOrder
			setDimensionOrder(DimensionOrder.fromString(
					element.getAttribute("DimensionOrder")));
		}
		if (element.hasAttribute("TimeIncrement"))
		{
			// Attribute property TimeIncrement
			setTimeIncrement(Double.valueOf(
					element.getAttribute("TimeIncrement")));
		}
		if (element.hasAttribute("PhysicalSizeY"))
		{
			// Attribute property PhysicalSizeY
			setPhysicalSizeY(PositiveFloat.valueOf(
					element.getAttribute("PhysicalSizeY")));
		}
		if (element.hasAttribute("PhysicalSizeX"))
		{
			// Attribute property PhysicalSizeX
			setPhysicalSizeX(PositiveFloat.valueOf(
					element.getAttribute("PhysicalSizeX")));
		}
		if (element.hasAttribute("PhysicalSizeZ"))
		{
			// Attribute property PhysicalSizeZ
			setPhysicalSizeZ(PositiveFloat.valueOf(
					element.getAttribute("PhysicalSizeZ")));
		}
		if (element.hasAttribute("SizeX"))
		{
			// Attribute property SizeX
			setSizeX(PositiveInteger.valueOf(
					element.getAttribute("SizeX")));
		}
		if (element.hasAttribute("SizeY"))
		{
			// Attribute property SizeY
			setSizeY(PositiveInteger.valueOf(
					element.getAttribute("SizeY")));
		}
		if (element.hasAttribute("SizeZ"))
		{
			// Attribute property SizeZ
			setSizeZ(PositiveInteger.valueOf(
					element.getAttribute("SizeZ")));
		}
		if (element.hasAttribute("SizeC"))
		{
			// Attribute property SizeC
			setSizeC(PositiveInteger.valueOf(
					element.getAttribute("SizeC")));
		}
		if (element.hasAttribute("Type"))
		{
			// Attribute property which is an enumeration Type
			setType(PixelType.fromString(
					element.getAttribute("Type")));
		}
		if (!element.hasAttribute("ID") && getID() == null)
		{
			// TODO: Should be its own exception
			throw new RuntimeException(String.format(
					"Pixels missing required ID property."));
		}
		if (element.hasAttribute("ID"))
		{
			// ID property
			setID(String.valueOf(
						element.getAttribute("ID")));
			// Adding this model object to the model handler
			model.addModelObject(getID(), this);
		}
		// Element property Channel which is complex (has
		// sub-elements) and occurs more than once
		List<Element> Channel_nodeList =
				getChildrenByTagName(element, "Channel");
		for (Element Channel_element : Channel_nodeList)
		{
			addChannel(
					new Channel(Channel_element, model));
		}
		// Element property BinData which is complex (has
		// sub-elements) and occurs more than once
		List<Element> BinData_nodeList =
				getChildrenByTagName(element, "BinData");
		for (Element BinData_element : BinData_nodeList)
		{
			addBinData(
					new BinData(BinData_element, model));
		}
		// Element property TiffData which is complex (has
		// sub-elements) and occurs more than once
		List<Element> TiffData_nodeList =
				getChildrenByTagName(element, "TiffData");
		for (Element TiffData_element : TiffData_nodeList)
		{
			addTiffData(
					new TiffData(TiffData_element, model));
		}
		List<Element> MetadataOnly_nodeList =
				getChildrenByTagName(element, "MetadataOnly");
		if (MetadataOnly_nodeList.size() > 1)
		{
			// TODO: Should be its own Exception
			throw new RuntimeException(String.format(
					"MetadataOnly node list size %d != 1",
					MetadataOnly_nodeList.size()));
		}
		else if (MetadataOnly_nodeList.size() != 0)
		{
			// Element property MetadataOnly which is complex (has
			// sub-elements)
			setMetadataOnly(new MetadataOnly(
					(Element) MetadataOnly_nodeList.get(0), model));
		}
		// Element property Plane which is complex (has
		// sub-elements) and occurs more than once
		List<Element> Plane_nodeList =
				getChildrenByTagName(element, "Plane");
		for (Element Plane_element : Plane_nodeList)
		{
			addPlane(
					new Plane(Plane_element, model));
		}
		// Element reference AnnotationRef
		List<Element> AnnotationRef_nodeList =
				getChildrenByTagName(element, "AnnotationRef");
		for (Element AnnotationRef_element : AnnotationRef_nodeList)
		{
			AnnotationRef annotationList_reference = new AnnotationRef();
			annotationList_reference.setID(AnnotationRef_element.getAttribute("ID"));
			model.addReference(this, annotationList_reference);
		}
	}

	// -- Pixels API methods --

	public boolean link(Reference reference, OMEModelObject o)
	{
		boolean wasHandledBySuperClass = super.link(reference, o);
		if (wasHandledBySuperClass)
		{
			return true;
		}
		if (reference instanceof AnnotationRef)
		{
			Annotation o_casted = (Annotation) o;
			o_casted.linkPixels(this);
			annotationList.add(o_casted);
			return true;
		}
		LOGGER.debug("Unable to handle reference of type: {}", reference.getClass());
		return false;
	}


	// Property
	public PositiveInteger getSizeT()
	{
		return sizeT;
	}

	public void setSizeT(PositiveInteger sizeT)
	{
		this.sizeT = sizeT;
	}

	// Property
	public DimensionOrder getDimensionOrder()
	{
		return dimensionOrder;
	}

	public void setDimensionOrder(DimensionOrder dimensionOrder)
	{
		this.dimensionOrder = dimensionOrder;
	}

	// Property
	public Double getTimeIncrement()
	{
		return timeIncrement;
	}

	public void setTimeIncrement(Double timeIncrement)
	{
		this.timeIncrement = timeIncrement;
	}

	// Property
	public PositiveFloat getPhysicalSizeY()
	{
		return physicalSizeY;
	}

	public void setPhysicalSizeY(PositiveFloat physicalSizeY)
	{
		this.physicalSizeY = physicalSizeY;
	}

	// Property
	public PositiveFloat getPhysicalSizeX()
	{
		return physicalSizeX;
	}

	public void setPhysicalSizeX(PositiveFloat physicalSizeX)
	{
		this.physicalSizeX = physicalSizeX;
	}

	// Property
	public PositiveFloat getPhysicalSizeZ()
	{
		return physicalSizeZ;
	}

	public void setPhysicalSizeZ(PositiveFloat physicalSizeZ)
	{
		this.physicalSizeZ = physicalSizeZ;
	}

	// Property
	public PositiveInteger getSizeX()
	{
		return sizeX;
	}

	public void setSizeX(PositiveInteger sizeX)
	{
		this.sizeX = sizeX;
	}

	// Property
	public PositiveInteger getSizeY()
	{
		return sizeY;
	}

	public void setSizeY(PositiveInteger sizeY)
	{
		this.sizeY = sizeY;
	}

	// Property
	public PositiveInteger getSizeZ()
	{
		return sizeZ;
	}

	public void setSizeZ(PositiveInteger sizeZ)
	{
		this.sizeZ = sizeZ;
	}

	// Property
	public PositiveInteger getSizeC()
	{
		return sizeC;
	}

	public void setSizeC(PositiveInteger sizeC)
	{
		this.sizeC = sizeC;
	}

	// Property
	public PixelType getType()
	{
		return type;
	}

	public void setType(PixelType type)
	{
		this.type = type;
	}

	// Property
	public String getID()
	{
		return id;
	}

	public void setID(String id)
	{
		this.id = id;
	}

	// Property which occurs more than once
	public int sizeOfChannelList()
	{
		return channelList.size();
	}

	public List<Channel> copyChannelList()
	{
		return new ArrayList<Channel>(channelList);
	}

	public Channel getChannel(int index)
	{
		return channelList.get(index);
	}

	public Channel setChannel(int index, Channel channel)
	{
		return channelList.set(index, channel);
	}

	public void addChannel(Channel channel)
	{
		channelList.add(channel);
	}

	public void removeChannel(Channel channel)
	{
		channelList.remove(channel);
	}

	// Property which occurs more than once
	public int sizeOfBinDataList()
	{
		return binDataList.size();
	}

	public List<BinData> copyBinDataList()
	{
		return new ArrayList<BinData>(binDataList);
	}

	public BinData getBinData(int index)
	{
		return binDataList.get(index);
	}

	public BinData setBinData(int index, BinData binData)
	{
		return binDataList.set(index, binData);
	}

	public void addBinData(BinData binData)
	{
		binDataList.add(binData);
	}

	public void removeBinData(BinData binData)
	{
		binDataList.remove(binData);
	}

	// Property which occurs more than once
	public int sizeOfTiffDataList()
	{
		return tiffDataList.size();
	}

	public List<TiffData> copyTiffDataList()
	{
		return new ArrayList<TiffData>(tiffDataList);
	}

	public TiffData getTiffData(int index)
	{
		return tiffDataList.get(index);
	}

	public TiffData setTiffData(int index, TiffData tiffData)
	{
		return tiffDataList.set(index, tiffData);
	}

	public void addTiffData(TiffData tiffData)
	{
		tiffDataList.add(tiffData);
	}

	public void removeTiffData(TiffData tiffData)
	{
		tiffDataList.remove(tiffData);
	}

	// Property
	public MetadataOnly getMetadataOnly()
	{
		return metadataOnly;
	}

	public void setMetadataOnly(MetadataOnly metadataOnly)
	{
		this.metadataOnly = metadataOnly;
	}

	// Property which occurs more than once
	public int sizeOfPlaneList()
	{
		return planeList.size();
	}

	public List<Plane> copyPlaneList()
	{
		return new ArrayList<Plane>(planeList);
	}

	public Plane getPlane(int index)
	{
		return planeList.get(index);
	}

	public Plane setPlane(int index, Plane plane)
	{
		return planeList.set(index, plane);
	}

	public void addPlane(Plane plane)
	{
		planeList.add(plane);
	}

	public void removePlane(Plane plane)
	{
		planeList.remove(plane);
	}

	// Reference which occurs more than once
	public int sizeOfLinkedAnnotationList()
	{
		return annotationList.size();
	}

	public List<Annotation> copyLinkedAnnotationList()
	{
		return new ArrayList<Annotation>(annotationList);
	}

	public Annotation getLinkedAnnotation(int index)
	{
		return annotationList.get(index);
	}

	public Annotation setLinkedAnnotation(int index, Annotation o)
	{
		return annotationList.set(index, o);
	}

	public boolean linkAnnotation(Annotation o)
	{
		o.linkPixels(this);
		return annotationList.add(o);
	}

	public boolean unlinkAnnotation(Annotation o)
	{
		o.unlinkPixels(this);
		return annotationList.remove(o);
	}

	public Element asXMLElement(Document document)
	{
		return asXMLElement(document, null);
	}

	protected Element asXMLElement(Document document, Element Pixels_element)
	{
		// Creating XML block for Pixels

		if (Pixels_element == null)
		{
			Pixels_element =
					document.createElementNS(NAMESPACE, "Pixels");
		}

		if (sizeT != null)
		{
			// Attribute property SizeT
			Pixels_element.setAttribute("SizeT", sizeT.toString());
		}
		if (dimensionOrder != null)
		{
			// Attribute property DimensionOrder
			Pixels_element.setAttribute("DimensionOrder", dimensionOrder.toString());
		}
		if (timeIncrement != null)
		{
			// Attribute property TimeIncrement
			Pixels_element.setAttribute("TimeIncrement", timeIncrement.toString());
		}
		if (physicalSizeY != null)
		{
			// Attribute property PhysicalSizeY
			Pixels_element.setAttribute("PhysicalSizeY", physicalSizeY.toString());
		}
		if (physicalSizeX != null)
		{
			// Attribute property PhysicalSizeX
			Pixels_element.setAttribute("PhysicalSizeX", physicalSizeX.toString());
		}
		if (physicalSizeZ != null)
		{
			// Attribute property PhysicalSizeZ
			Pixels_element.setAttribute("PhysicalSizeZ", physicalSizeZ.toString());
		}
		if (sizeX != null)
		{
			// Attribute property SizeX
			Pixels_element.setAttribute("SizeX", sizeX.toString());
		}
		if (sizeY != null)
		{
			// Attribute property SizeY
			Pixels_element.setAttribute("SizeY", sizeY.toString());
		}
		if (sizeZ != null)
		{
			// Attribute property SizeZ
			Pixels_element.setAttribute("SizeZ", sizeZ.toString());
		}
		if (sizeC != null)
		{
			// Attribute property SizeC
			Pixels_element.setAttribute("SizeC", sizeC.toString());
		}
		if (type != null)
		{
			// Attribute property Type
			Pixels_element.setAttribute("Type", type.toString());
		}
		if (id != null)
		{
			// Attribute property ID
			Pixels_element.setAttribute("ID", id.toString());
		}
		if (channelList != null)
		{
			// Element property Channel which is complex (has
			// sub-elements) and occurs more than once
			for (Channel channelList_value : channelList)
			{
				Pixels_element.appendChild(channelList_value.asXMLElement(document));
			}
		}
		if (binDataList != null)
		{
			// Element property BinData which is complex (has
			// sub-elements) and occurs more than once
			for (BinData binDataList_value : binDataList)
			{
				Pixels_element.appendChild(binDataList_value.asXMLElement(document));
			}
		}
		if (tiffDataList != null)
		{
			// Element property TiffData which is complex (has
			// sub-elements) and occurs more than once
			for (TiffData tiffDataList_value : tiffDataList)
			{
				Pixels_element.appendChild(tiffDataList_value.asXMLElement(document));
			}
		}
		if (metadataOnly != null)
		{
			// Element property MetadataOnly which is complex (has
			// sub-elements)
			Pixels_element.appendChild(metadataOnly.asXMLElement(document));
		}
		if (planeList != null)
		{
			// Element property Plane which is complex (has
			// sub-elements) and occurs more than once
			for (Plane planeList_value : planeList)
			{
				Pixels_element.appendChild(planeList_value.asXMLElement(document));
			}
		}
		if (annotationList != null)
		{
			// Reference property AnnotationRef which occurs more than once
			for (Annotation annotationList_value : annotationList)
			{
				AnnotationRef o = new AnnotationRef();
				o.setID(annotationList_value.getID());
				Pixels_element.appendChild(o.asXMLElement(document));
			}
		}
		return super.asXMLElement(document, Pixels_element);
	}
}
