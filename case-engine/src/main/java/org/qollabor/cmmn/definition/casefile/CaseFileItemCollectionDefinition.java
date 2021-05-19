package org.qollabor.cmmn.definition.casefile;

import java.util.ArrayList;
import java.util.Collection;

import org.qollabor.cmmn.definition.CMMNElementDefinition;
import org.qollabor.cmmn.definition.ModelDefinition;
import org.w3c.dom.Element;

public class CaseFileItemCollectionDefinition extends CMMNElementDefinition {
	private final Collection<CaseFileItemDefinition> items = new ArrayList();

	public CaseFileItemCollectionDefinition(Element element, ModelDefinition definition, CMMNElementDefinition parentElement)
	{
		super(element, definition, parentElement);
	}
	
	public Collection<CaseFileItemDefinition> getChildren()
	{
		return items;
	}

	public CaseFileItemDefinition getChild(String identifier) {
		return getChildren().stream().filter(i -> i.getName().equals(identifier) || i.getId().equals(identifier)).findFirst().orElse(null);
	}

	/**
	 * Returns true if an item with the identifier does not exist
	 * @param identifier
	 * @return
	 */
	public boolean isUndefined(String identifier) {
		return getChild(identifier) == null;
	}

	/**
	 * Recursively searches this level and all children until an item with the specified name is found.
	 *
	 * @param identifier
	 * @return
	 */
	public CaseFileItemDefinition findCaseFileItem(String identifier) {
		CaseFileItemDefinition item = getChild(identifier);
		if (item == null) {
			for (CaseFileItemDefinition caseFileItem : getChildren()) {
				item = caseFileItem.findCaseFileItem(identifier);
				if (item != null) {
					// Immediately return if we found one.
					return item;
				}
			}
		}
		return item;
	}
}
