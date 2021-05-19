/*
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.cmmn.instance;

import org.qollabor.akka.actor.command.exception.AuthorizationException;
import org.qollabor.cmmn.definition.CaseRoleDefinition;
import org.qollabor.cmmn.definition.ItemDefinition;
import org.qollabor.cmmn.definition.UserEventDefinition;
import org.qollabor.cmmn.instance.team.CurrentMember;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.Set;

public class UserEvent extends PlanItem<UserEventDefinition> {
    public UserEvent(String id, int index, ItemDefinition itemDefinition, UserEventDefinition definition, Stage stage) {
        super(id, index, itemDefinition, definition, stage, StateMachine.EventMilestone);
    }

    public Collection<CaseRoleDefinition> getAuthorizedRoles() {
        return getDefinition().getAuthorizedRoles();
    }

    @Override
    protected boolean isTransitionAllowed(Transition transition) {
        if (transition != Transition.Occur) { // Only validating whether current user can make this event 'Occur'
            return true;
        }

        Collection<CaseRoleDefinition> authorizedRoles = getAuthorizedRoles();
        if (authorizedRoles.isEmpty()) { // No roles defined, so it is allowed.
            return true;
        }

        CurrentMember currentUser = getCaseInstance().getCurrentTeamMember();
        // Now fetch roles of current user within this case and see if there is one that matches one of the authorized roles
        Set<CaseRoleDefinition> rolesOfCurrentUser = currentUser.getRoles();
        for (CaseRoleDefinition role : authorizedRoles) {
            if (rolesOfCurrentUser.contains(role)) {
                return true; // You're free to go
            }
        }
        // Apparently no matching role was found.s
        throw new AuthorizationException("User '"+currentUser.getMemberId()+"' does not have the permission to raise the event " + getName());
    }

    @Override
    protected void dumpImplementationToXML(Element planItemXML) {
        super.dumpImplementationToXML(planItemXML);
        Collection<CaseRoleDefinition> roles = getAuthorizedRoles();
        for (CaseRoleDefinition role : roles) {
            String roleName = role.getName();
            Element roleElement = planItemXML.getOwnerDocument().createElement("Role");
            planItemXML.appendChild(roleElement);
            roleElement.setAttribute("name", roleName);
        }
    }
}
