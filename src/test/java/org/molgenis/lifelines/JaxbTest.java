package org.molgenis.lifelines;

import static org.testng.Assert.assertEquals;

import java.io.StringWriter;
import java.net.MalformedURLException;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBException;

import nl.umcg.hl7.service.catalog.CatalogService;
import nl.umcg.hl7.service.catalog.GenericLayerCatalogService;

import org.hl7.v3.ED;
import org.testng.annotations.Test;

public class JaxbTest
{
	@Test
	public void testService() throws MalformedURLException
	{
		GenericLayerCatalogService svc = new CatalogService()
				.getBasicHttpBindingGenericLayerCatalogService();
	}
	
	@Test
	public void testHL7() throws JAXBException
	{
		ED ed = new ED();
		ed.getContent().add("Blah");
		StringWriter sw = new StringWriter();
		JAXB.marshal(ed, sw);
		assertEquals(sw.toString(),"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
				+ "<ED xmlns:ns2=\"urn:hl7-org:v3\">Blah</ED>\n");
	}
}
