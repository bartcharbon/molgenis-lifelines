package org.molgenis.lifelines.studydefinition;

import java.math.BigInteger;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.hl7.ActClass;
import org.molgenis.hl7.ActMood;
import org.molgenis.hl7.CD;
import org.molgenis.hl7.CE;
import org.molgenis.hl7.COCTMT090107UVAssignedPerson;
import org.molgenis.hl7.COCTMT090107UVPerson;
import org.molgenis.hl7.COCTMT150007UVContactParty;
import org.molgenis.hl7.COCTMT150007UVOrganization;
import org.molgenis.hl7.CS;
import org.molgenis.hl7.ED;
import org.molgenis.hl7.EntityClassOrganization;
import org.molgenis.hl7.EntityClassPerson;
import org.molgenis.hl7.EntityDeterminerSpecific;
import org.molgenis.hl7.II;
import org.molgenis.hl7.INT;
import org.molgenis.hl7.NullFlavor;
import org.molgenis.hl7.ON;
import org.molgenis.hl7.ObjectFactory;
import org.molgenis.hl7.PN;
import org.molgenis.hl7.POQMMT000001UVAuthor;
import org.molgenis.hl7.POQMMT000001UVComponent2;
import org.molgenis.hl7.POQMMT000001UVCustodian;
import org.molgenis.hl7.POQMMT000001UVEntry;
import org.molgenis.hl7.POQMMT000001UVQualityMeasureDocument;
import org.molgenis.hl7.POQMMT000001UVSection;
import org.molgenis.hl7.POQMMT000002UVObservation;
import org.molgenis.hl7.ParticipationType;
import org.molgenis.hl7.RoleClassAssignedEntity;
import org.molgenis.hl7.RoleClassContact;
import org.molgenis.hl7.ST;
import org.molgenis.hl7.StrucDocItem;
import org.molgenis.hl7.StrucDocList;
import org.molgenis.hl7.StrucDocText;
import org.molgenis.lifelines.resourcemanager.GenericLayerResourceManagerService;
import org.molgenis.omx.catalog.CatalogLoaderService;
import org.molgenis.omx.catalog.UnknownCatalogException;
import org.molgenis.omx.study.StudyDefinition;
import org.molgenis.omx.study.StudyDefinitionInfo;
import org.molgenis.omx.study.StudyDefinitionItem;
import org.molgenis.omx.study.StudyDefinitionService;
import org.molgenis.omx.study.UnknownStudyDefinitionException;
import org.molgenis.omx.utils.I18nTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GenericLayerStudyDefinitionService implements StudyDefinitionService
{
	@Autowired
	private GenericLayerResourceManagerService resourceManagerService;
	@Autowired
	private CatalogLoaderService catalogLoaderService;
	@Autowired
	private GenericLayerDataQueryService dataQueryService;

	/**
	 * Find the study definition with the given id
	 * 
	 * @return
	 */
	@Override
	public StudyDefinition getStudyDefinition(String id)
	{
		POQMMT000001UVQualityMeasureDocument qualityMeasureDocument = resourceManagerService.findStudyDefinitionHL7(id);
		return new QualityMeasureDocumentStudyDefinition(qualityMeasureDocument);
	}

	/**
	 * Find all studydefinitions
	 * 
	 * @return List of StudyDefinitionInfo
	 */
	@Override
	public List<StudyDefinitionInfo> findStudyDefinitions()
	{
		return resourceManagerService.findStudyDefinitions();
	}

	/**
	 * Get a specific studydefinition and save it in the database
	 */
	@Override
	public void loadStudyDefinition(String id) throws UnknownStudyDefinitionException
	{
		try
		{
			catalogLoaderService.loadCatalogOfStudyDefinition(id);
		}
		catch (UnknownCatalogException e)
		{
			throw new UnknownStudyDefinitionException(e);
		}

		dataQueryService.loadStudyDefinitionData(resourceManagerService.findStudyDefinitionHL7(id));
	}

	@Override
	public StudyDefinition persistStudyDefinition(StudyDefinition studyDefinition)
	{
		POQMMT000001UVQualityMeasureDocument eMeasure = createQualityMeasureDocument(studyDefinition);
		String id = resourceManagerService.persistStudyDefinition(eMeasure);
		studyDefinition.setId(StudyDefinitionIdConverter.studyDefinitionIdToOmxIdentifier(id));
		return studyDefinition;
	}

	private POQMMT000001UVQualityMeasureDocument createQualityMeasureDocument(StudyDefinition studyDefinition)
	{
		POQMMT000001UVQualityMeasureDocument eMeasure = new POQMMT000001UVQualityMeasureDocument();
		II typeId = new II();
		typeId.setRoot("2.16.840.1.113883.1.3");
		typeId.setExtension("POQM_HD000001");
		eMeasure.setTypeId(typeId);

		II id = new II();
		id.setRoot("2.16.840.1.113883.2.4.3.8.1000.54.7");
		eMeasure.setId(id); // id placeholder

		CE code = new CE();
		code.setCode("57024-2");
		code.setCodeSystem("2.16.840.1.113883.6.1");
		code.setDisplayName("Health Quality Measure document");
		eMeasure.setCode(code);

		ST title = new ST();
		title.getContent().add(studyDefinition.getName());
		eMeasure.setTitle(title);

		StringBuilder textBuilder = new StringBuilder("Created by ")
				.append(StringUtils.join(studyDefinition.getAuthors(), ' ')).append(" (")
				.append(studyDefinition.getAuthorEmail()).append(')');

		ED text = new ED();
		text.getContent().add(textBuilder.toString());
		eMeasure.setText(text);

		CS statusCode = new CS();
		statusCode.setCode("active");
		eMeasure.setStatusCode(statusCode);

		II setId = new II();
		setId.setRoot("1.1.1");
		setId.setExtension("example");
		eMeasure.setSetId(setId);

		INT versionNumber = new INT();
		versionNumber.setValue(new BigInteger("1"));
		eMeasure.setVersionNumber(versionNumber);

		POQMMT000001UVAuthor author = new POQMMT000001UVAuthor();
		author.setTypeCode(ParticipationType.AUT);
		COCTMT090107UVAssignedPerson assignedPerson = new COCTMT090107UVAssignedPerson();
		assignedPerson.setClassCode(RoleClassAssignedEntity.ASSIGNED);
		COCTMT090107UVPerson assignedPersonAssignedPerson = new COCTMT090107UVPerson();
		assignedPersonAssignedPerson.setClassCode(EntityClassPerson.PSN);
		assignedPersonAssignedPerson.setDeterminerCode(EntityDeterminerSpecific.INSTANCE);

		PN name = new PN();
		name.getContent().add("Onderzoeker X");
		assignedPersonAssignedPerson.getName().add(name);
		assignedPerson.setAssignedPerson(new ObjectFactory()
				.createCOCTMT090107UVAssignedPersonAssignedPerson(assignedPersonAssignedPerson));

		COCTMT150007UVOrganization representedOrganization = new COCTMT150007UVOrganization();
		representedOrganization.setClassCode(EntityClassOrganization.ORG);
		representedOrganization.setDeterminerCode(EntityDeterminerSpecific.INSTANCE);

		II representedOrganizationId = new II();
		representedOrganizationId.setRoot("2.16.840.1.113883.19.5");
		representedOrganization.getId().add(representedOrganizationId);
		ON representedOrganizationName = new ON();
		representedOrganizationName.getContent().add("UMCG");
		representedOrganization.getName().add(representedOrganizationName);
		COCTMT150007UVContactParty contactParty = new COCTMT150007UVContactParty();
		contactParty.setClassCode(RoleClassContact.CON);
		contactParty.setNullFlavor(NullFlavor.UNK);
		representedOrganization.getContactParty().add(contactParty);

		assignedPerson.setRepresentedOrganization(new ObjectFactory()
				.createCOCTMT090107UVAssignedPersonRepresentedOrganization(representedOrganization));

		author.setAssignedPerson(assignedPerson);
		eMeasure.getAuthor().add(author);

		POQMMT000001UVCustodian custodian = new POQMMT000001UVCustodian();
		custodian.setTypeCode(ParticipationType.CST);
		COCTMT090107UVAssignedPerson custodianAssignedPerson = new COCTMT090107UVAssignedPerson();
		custodianAssignedPerson.setClassCode(RoleClassAssignedEntity.ASSIGNED);
		custodian.setAssignedPerson(custodianAssignedPerson);
		eMeasure.setCustodian(custodian);

		POQMMT000001UVComponent2 component = new POQMMT000001UVComponent2();
		POQMMT000001UVSection section = new POQMMT000001UVSection();

		CD sectionCode = new CD();
		sectionCode.setCode("57025-9");
		sectionCode.setCodeSystem("2.16.840.1.113883.6.1");
		sectionCode.setDisplayName("Data Criteria section");
		section.setCode(sectionCode);

		ED sectionTitle = new ED();
		sectionTitle.getContent().add("Data criteria");
		section.setTitle(sectionTitle);

		StrucDocText sectionText = new StrucDocText();
		StrucDocList strucDocList = new StrucDocList();
		for (StudyDefinitionItem item : studyDefinition.getItems())
		{
			StrucDocItem strucDocItem = new StrucDocItem();
			strucDocItem.getContent().add(item.getName());
			strucDocList.getItem().add(strucDocItem);
		}
		sectionText.getContent().add(new ObjectFactory().createStrucDocTextList(strucDocList));
		section.setText(sectionText);

		for (StudyDefinitionItem item : studyDefinition.getItems())
		{
			POQMMT000001UVEntry entry = new POQMMT000001UVEntry();
			entry.setTypeCode("DRIV");
			POQMMT000002UVObservation observation = new POQMMT000002UVObservation();
			observation.setClassCode(ActClass.OBS);
			observation.setMoodCode(ActMood.CRT);

			String observationCodeCode = item.getCode();
			String observationCodeCodesystem = item.getCodeSystem();
			if (observationCodeCode == null || observationCodeCodesystem == null)
			{
				// TODO remove once catalogues are always loaded from LL GL
				observationCodeCode = item.getId();
				observationCodeCodesystem = "2.16.840.1.113883.2.4.3.8.1000.54.4";
			}
			CD observationCode = new CD();
			observationCode.setDisplayName(item.getName());
			observationCode.setCode(observationCodeCode);
			observationCode.setCodeSystem(observationCodeCodesystem);
			ED observationOriginalText = new ED();
			observationOriginalText.getContent().add(I18nTools.get(item.getDescription()));
			observationCode.setOriginalText(observationOriginalText);
			observation.setCode(observationCode);
			entry.setObservation(observation);
			section.getEntry().add(entry);
		}

		component.setSection(section);
		eMeasure.getComponent().add(component);

		return eMeasure;
	}
}
